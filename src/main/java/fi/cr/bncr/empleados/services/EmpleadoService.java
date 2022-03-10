package fi.cr.bncr.empleados.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.enums.Rol;
import fi.cr.bncr.empleados.models.DiaLaboral;
import fi.cr.bncr.empleados.models.Empleado;
import fi.cr.bncr.empleados.models.Turno;
import fi.cr.bncr.empleados.processors.AddSiguienteTurnoEmpleadoProcessor;
import fi.cr.bncr.empleados.processors.AsignarRolEmpleadoProcessor;
import fi.cr.bncr.empleados.processors.GenerarDiasLaboralesProcessor;
import fi.cr.bncr.empleados.utils.Utils;

@Component
public class EmpleadoService {

    private static Logger logger  = LoggerFactory.getLogger(EmpleadoService.class);

    private static final String _REGLAS_SHEET_NAME = "Reglas";

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private AddSiguienteTurnoEmpleadoProcessor addSiguienteTurnoEmpleadoProcessor;

    @Autowired
    private AsignarRolEmpleadoProcessor asignarRolEmpleadoProcessor;

    @Autowired GenerarDiasLaboralesProcessor generarDiasLaboralesProcessor;

    @Autowired
    private RolService rolService;


    @Async
    public Future<List<Empleado>> loadEmpleadosFromFile(MultipartFile file){
        logger.info(">>>> Cargando empleados desde el archivo");
        List<Empleado> empleados = null;
        try {
            Workbook workbook;
            workbook = new XSSFWorkbook(file.getInputStream());
            int lastSheetNumber = workbook.getNumberOfSheets();
            String sheetName = workbook.getSheetName(lastSheetNumber-1);

            logger.info("Loading info from SHEET: {}", sheetName);

            Sheet sheet = workbook.getSheet(sheetName);
            Iterator<Row> rows = sheet.iterator();
            empleados = this.getEmpleadosFromExcel(rows);
            workbook.close();

            logger.info("EMPLEADOS CARGADOS ----------------------------------------------");
            empleados.forEach(e -> logger.info(e.toString()));
        } catch (IOException e) {
            logger.error("No se pudo cargar el archivo excel", e);
        }
        return new AsyncResult<List<Empleado>>(empleados);
    }

    public List<Empleado> getEmpleadosFromExcel(Iterator<Row> rows){

        List<Empleado> empleados = new ArrayList<>();
        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            // skip header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            String numero = currentRow.getCell(0) == null ? "" : currentRow.getCell(0).toString().trim().replace(".0", "");
            String nombre = currentRow.getCell(1) == null ? "" : currentRow.getCell(1).toString().trim();
            if(!numero.isEmpty()){
                //Por defecto nadie trabaja los domingos
                List<Dia> diasNoLaborados = new ArrayList<>();
                diasNoLaborados.add(Dia.DOMINGO);

                int turnoFijo = 0;
                Rol rolDefinido = Rol.NINGUNO;
                List<String> backups = null;



                empleados.add(new Empleado(Long.getLong(rowNumber+""), numero, nombre, this.getTurnoFromEmpleado(currentRow), null, diasNoLaborados, rolDefinido, Rol.NINGUNO, backups, this.getDiasActualesLaboradosFromEmpleado(currentRow), null, turnoFijo));
            }
        }
        return empleados;
    }

    private Turno getTurnoFromEmpleado(Row eRow){
        List<Dia> diasEmpleado = new ArrayList<>();
        for(int index = 2; index <= 8; index++){
            if(eRow.getCell(index) == null ? false : !eRow.getCell(index).toString().trim().isEmpty()){
                switch(index){
                    case 2: diasEmpleado.add(Dia.LUNES); continue;
                    case 3: diasEmpleado.add(Dia.MARTES); continue;
                    case 4: diasEmpleado.add(Dia.MIERCOLES); continue;
                    case 5: diasEmpleado.add(Dia.JUEVES); continue;
                    case 6: diasEmpleado.add(Dia.VIERNES); continue;
                    case 7: diasEmpleado.add(Dia.SABADO); continue;
                    case 8: diasEmpleado.add(Dia.DOMINGO); continue;
                }
            }
        }

        //logger.info("-> DIAS: "+diasEmpleado);

        for (Turno t : turnoService.getAllTurnos()) {
            int index = -1;
            for(Dia d : diasEmpleado){
                index++;

                //Si el dia del empleado no esta dentro de este turno, rompe y busque en otro turno
                if(!t.getDias().contains(d)) break;

                //logger.info(t.getDias()+"  --->   "+diasEmpleado);
                //Si el tamaño de la lista de empleado es del mismo tamaño de la lista de turno GANAMOS
                if(index+1 == t.getDias().size()) return t;
            }
        }


        //Si llego a este punto es porque no calza con un turno predefinido... debe ser un turno provisional
        //Le vamos a asignar un turno que concuerde con si trabajo o no un sabado
        return turnoService.getRandomTurno(diasEmpleado.contains(Dia.SABADO));
    }

    private Rol getRolFromEmpleado(String contenido){

        if(contenido.contains("caja") && contenido.contains("rap")){
            return Rol.CAJA_RAPIDA;
        }else if(contenido.contains("caja")){
            return Rol.CAJA;
        }else if(contenido.contains("plat") && contenido.contains("emp")){
            return Rol.PLATAFORMA_EMPRESARIAL;
        }else if(contenido.contains("plat")){
            return Rol.PLATAFORMA;
        }else if(contenido.contains("back")){
            return Rol.BACKOFFICE;
        }else if(contenido.contains("info")){
            return Rol.INFORMACION;
        }

        return Rol.NINGUNO;
    }

    private List<DiaLaboral> getDiasActualesLaboradosFromEmpleado(Row eRow){
        List<DiaLaboral> diasLaborados = new ArrayList<>();
        for(int index = 2; index <= 8; index++){
            if(eRow.getCell(index) == null ? false : !eRow.getCell(index).toString().trim().isEmpty()){
                String contenido = eRow.getCell(index) == null ? "" : eRow.getCell(index).toString().trim().toLowerCase();
                switch(index){
                    case 2: diasLaborados.add(new DiaLaboral(Dia.LUNES, this.getRolFromEmpleado(contenido))); continue;
                    case 3: diasLaborados.add(new DiaLaboral(Dia.MARTES, this.getRolFromEmpleado(contenido))); continue;
                    case 4: diasLaborados.add(new DiaLaboral(Dia.MIERCOLES, this.getRolFromEmpleado(contenido))); continue;
                    case 5: diasLaborados.add(new DiaLaboral(Dia.JUEVES, this.getRolFromEmpleado(contenido))); continue;
                    case 6: diasLaborados.add(new DiaLaboral(Dia.VIERNES, this.getRolFromEmpleado(contenido))); continue;
                    case 7: diasLaborados.add(new DiaLaboral(Dia.SABADO, this.getRolFromEmpleado(contenido))); continue;
                    case 8: diasLaborados.add(new DiaLaboral(Dia.DOMINGO, this.getRolFromEmpleado(contenido))); continue;
                }
            }
        }

        return diasLaborados;
    }

    public void asignarSiguienteMovimiento(List<Empleado> empleados){

        //Asignarle rol a los que tiene rol predefinido
        empleados.stream().filter( e -> !e.getRolPredefinido().equals(Rol.NINGUNO)).forEach( e -> e.processar(asignarRolEmpleadoProcessor));

        //Asignarles turnos y roles
        empleados.stream().forEach( e -> e.
            processar(addSiguienteTurnoEmpleadoProcessor)
            .processar(asignarRolEmpleadoProcessor)
            .processar(generarDiasLaboralesProcessor));

        //Liberamos la cache de conteo de roles
        rolService.flushCache();

        //Rellenamos los roles que no tienen personal
        this.reasignarRolesFaltantesPorDia(empleados);

        //Balanceamos cantidad de cajas y plataformas por dia
        this.balancearCajasYPlataformas(empleados);
    }

    private void balancearCajasYPlataformas(List<Empleado> empleados){
        //Hacemos una sumatoria para saber que dias no tiene un rol en especifico
        //y para ver cuales dias hay que nivelar
        Map<Dia, Map<Rol, Integer>> reparticionDeRoles = this.getSumatoriaRolesPorDia(empleados);
        logger.info(">>>> Hay que balancear cajas y plataformas");
        reparticionDeRoles.entrySet().stream()
            .forEach( entry -> {
                Dia dia = entry.getKey();
                Map<Rol, Integer> conteo = entry.getValue();

                //logger.info("REVISAR SI HAY QUE nivelar Cajas y Plataforma para el dia {}, Caja: {}, Plataforma: {}", dia, conteo.get(Rol.CAJA), conteo.get(Rol.PLATAFORMA));
                while(Utils.existeDiferencia(conteo.get(Rol.CAJA).intValue(), conteo.get(Rol.PLATAFORMA).intValue(), 2)){
                    logger.info("Hay que nivelar Cajas y Plataforma para el dia {}, Caja: {}, Plataforma: {}", dia, conteo.get(Rol.CAJA), conteo.get(Rol.PLATAFORMA));

                    Rol rolACompensar = conteo.get(Rol.CAJA).intValue() < conteo.get(Rol.PLATAFORMA).intValue() ? Rol.CAJA : Rol.PLATAFORMA;
                    Rol rolARestar = rolACompensar.equals(Rol.CAJA) ? Rol.PLATAFORMA : Rol.CAJA;

                    //Buscamos los empleados que trabajen ese dia con el rol a restar
                    List<Empleado> empleadosConRolARestar = empleados.stream()
                                                                        .filter( e -> e.getDiasLaboresSiguientes().stream().anyMatch( dl -> dl.getDia().equals(dia) && dl.getRol().equals(rolARestar)))
                                                                        .collect(Collectors.toList());

                    //Seleccionamos un empleado al azar para asignarle el nuevo rol
                    Random random = new Random();
                    Empleado empleadoSeleccionado = empleadosConRolARestar.get(random.nextInt(empleadosConRolARestar.size()));

                    empleadoSeleccionado.getDiasLaboresSiguientes().stream().filter( dl -> dl.getDia().equals(dia)).forEach( dl -> dl.setRol(rolACompensar));

                    //Actualizamos valores en matriz
                    conteo.put(rolACompensar, conteo.get(rolACompensar).intValue() + 1);
                    conteo.put(rolARestar, conteo.get(rolARestar).intValue() - 1);
                }
            });
    }

    private void reasignarRolesFaltantesPorDia(List<Empleado> empleados){
        //Hacemos una sumatoria para saber que dias no tiene un rol en especifico
        //y para ver cuales dias hay que nivelar
        Map<Dia, Map<Rol, Integer>> reparticionDeRoles = this.getSumatoriaRolesPorDia(empleados);
        //reparticionDeRoles.entrySet().stream().forEach( entry -> logger.info(entry.getKey()+" | "+entry.getValue()));
        logger.info(">>>> Hay que rellenar puestos en estos dias");
        reparticionDeRoles.entrySet().stream()
            .forEach( entry -> {
                Dia dia = entry.getKey();
                List<Rol> rolesSinAsignar = entry.getValue().entrySet().stream()
                                            .filter( entry3 -> entry3.getValue().intValue() == 0) //Deme los elementos con cantidades cero
                                            .map( entry4 -> entry4.getKey()).collect(Collectors.toList()); //Deme la lista de roles con cero

                rolesSinAsignar.stream().forEach( r -> {
                    logger.info("Dia: {} Rol: {}", dia, r);

                    boolean empleadoHaSidoAsignado = false;
                    //Revisamos si hay empleados con dicho rol en especifico
                    //primero debemos ver quien es su backup
                    List<Empleado> empleadoFuera = empleados.stream()
                                                            .filter(e -> e.getBackup() != null && e.getRolPredefinido() != null)
                                                            .filter( e -> e.getBackup().size()>0 && r.equals(e.getRolPredefinido())) //Con esto traemos empleados que tengan este rol predeterminado y tengan backups
                                                            .filter( e -> !e.getDiasLaboresSiguientes().stream().anyMatch( dl -> dl.getDia().equals(dia))) //Con esto traemos empleados que no trabajen este dia
                                                            .collect(Collectors.toList());
                    //Si existe un empleado que tiene informacion directa para remplazar
                    //con otro empleado, nos vamos por esta opcion
                    if(!empleadoFuera.isEmpty()){
                        Empleado eFuera = empleadoFuera.get(0);
                        logger.info("El empleado {} esta fuera y debe ser remplazado por {}", eFuera.getNumero()+" | "+eFuera.getNombre(), eFuera.getBackup());

                        for(String numeroE : eFuera.getBackup()){
                            logger.info("BUSCANDO INFO DEL EMPLEADO: {}", numeroE);
                            List<Empleado> empleadosBackupDisponibles = empleados.stream()
                                                                        .filter( e -> e.getNumero().equals(numeroE)) //Buscar al backup primero
                                                                        .filter( e -> e.getDiasLaboresSiguientes().stream().anyMatch( dl -> dl.getDia().equals(dia))) //Dicho empleado trabaja ese dia
                                                                        .collect(Collectors.toList());
                            logger.info("EMPLEADOS ENCONTRADOS: {}", empleadosBackupDisponibles);
                            if(!empleadosBackupDisponibles.isEmpty()){
                                Empleado empleadoSeleccionadoBackup = empleadosBackupDisponibles.get(0);
                                logger.info("El empleado backup {} es remplazo para el dia {} en el rol {}", empleadoSeleccionadoBackup.getNumero()+" | "+empleadoSeleccionadoBackup.getNombre(), dia, r);
                                //Asignamos el rol al otro empleado
                                empleadoSeleccionadoBackup.getDiasLaboresSiguientes().stream().filter( dl -> dl.getDia().equals(dia)).forEach( dl -> dl.setRol(r));

                                empleadoHaSidoAsignado = true;
                                break;
                            }
                        }

                    }

                    if(!empleadoHaSidoAsignado){
                        //Si no hay backup pues asignamos a lo random
                        List<Empleado> empleadosDisponiblesRandom = empleados.stream()
                                                                    .filter( e -> e.getDiasLaboresSiguientes().stream().anyMatch( dl -> dl.getDia().equals(dia))) //Traemos clientes que trabajan este dia
                                                                    .filter( e -> e.getRolSiguiente().equals(Rol.CAJA) || e.getRolSiguiente().equals(Rol.PLATAFORMA)) //Traemos clientes que trabajan en plataforma o caja
                                                                    .collect(Collectors.toList());

                        Random random = new Random();
                        Empleado empleadoSeleccionadoRandom = empleadosDisponiblesRandom.get(random.nextInt(empleadosDisponiblesRandom.size()));
                        logger.info("El empleado {} es remplazo para el dia {} en el rol {}", empleadoSeleccionadoRandom.getNumero()+" | "+empleadoSeleccionadoRandom.getNombre(), dia, r);
                        empleadoSeleccionadoRandom.getDiasLaboresSiguientes().stream().filter( dl -> dl.getDia().equals(dia)).forEach( dl -> dl.setRol(r));
                    }
                });
            });
    }

    private Map<Dia, Map<Rol, Integer>> getSumatoriaRolesPorDia(List<Empleado> empleados){
        Map<Dia, Map<Rol, Integer>> reparticionDeRoles = this.getConteoBarebone();

        for(Empleado e : empleados){
            for(DiaLaboral dl : e.getDiasLaboresSiguientes()){
                Integer valor = reparticionDeRoles.get(dl.getDia()).get(dl.getRol());
                reparticionDeRoles.get(dl.getDia()).put(dl.getRol(), valor.intValue() + 1);
            }
        }
        return reparticionDeRoles;
    }

    private Map<Dia, Map<Rol, Integer>> getConteoBarebone(){
        Map<Dia, Map<Rol, Integer>> barebone = new HashMap<>();
        List<Dia> dias = new ArrayList<Dia>(EnumSet.allOf(Dia.class));
        List<Rol> roles = new ArrayList<Rol>(EnumSet.allOf(Rol.class));
        for(Dia d : dias){
            if(d.equals(Dia.DOMINGO)) continue;
            barebone.put(d, new HashMap<Rol, Integer>());
            for(Rol r : roles){
                if(r.equals(Rol.NINGUNO)) continue;

                //Estos roles no existen los sabados
                if((r.equals(Rol.BACKOFFICE) || r.equals(Rol.PLATAFORMA_EMPRESARIAL) || r.equals(Rol.INFORMACION)) && d.equals(Dia.SABADO)) continue;

                barebone.get(d).put(r, 0);
            }
        }
        return barebone;
    }

    @Async
    public Future<Map<String, Map<String, Object>>> getReglasEmpleadosFromFile(MultipartFile file){
        logger.info(">>>> Cargando Reglas del Archivo");
        Map<String, Map<String, Object>> reglas = new HashMap<>();
        try {
            Workbook workbook;
            workbook = new XSSFWorkbook(file.getInputStream());

            logger.info("Loading info from SHEET: {}", _REGLAS_SHEET_NAME);

            Sheet sheet = workbook.getSheet(_REGLAS_SHEET_NAME);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                String numeroEmpleado = currentRow.getCell(0) == null ? "" : currentRow.getCell(0).toString().trim().replace(".0", "");
                String diasNoLaboraRAW = currentRow.getCell(2) == null ? "" :  currentRow.getCell(2).toString().trim().toUpperCase();
                String turnoFijoRAW = currentRow.getCell(3) == null ? "" :  currentRow.getCell(3).toString().trim().replace(".0", "");
                String rolDefinidoRAW = currentRow.getCell(4) == null ? null :  currentRow.getCell(4).toString().trim().toUpperCase();
                String backupsRAW = currentRow.getCell(5) == null ? null :  currentRow.getCell(5).toString().trim().replace(".0", "");

                if(!numeroEmpleado.isEmpty()){
                    Map<String, Object> e = new HashMap<>();
                    if(!diasNoLaboraRAW.isEmpty()){
                        List<Dia> dias = Arrays.asList(diasNoLaboraRAW.split(",")).stream().map(EmpleadoService::diaFromString).collect(Collectors.toList());
                        e.put("diasNoLaborales", dias);
                    }
                    if(!turnoFijoRAW.isEmpty()){
                        e.put("turnoFijo", Integer.parseInt(turnoFijoRAW));
                    }
                    if(!rolDefinidoRAW.isEmpty()){
                        e.put("rolDefinido", RolService.parseString(rolDefinidoRAW));
                    }
                    if(!backupsRAW.isEmpty()){
                        e.put("backups", Arrays.asList(backupsRAW.split(",")));
                    }

                    reglas.put(numeroEmpleado, e);
                }
            }
            workbook.close();
        } catch (IOException e) {
            logger.error("No se pudo cargar el archivo excel para obetener las reglas de empleado", e);
        }
        logger.info("REGLAS CARGADAS ----------------------------------------------");
        reglas.keySet().stream().forEach( k -> logger.info(k+" >> "+reglas.get(k)));

        return new AsyncResult<Map<String, Map<String, Object>>>(reglas);
    }

    public void aplicarReglasEmpleados(List<Empleado> empleados, Map<String, Map<String, Object>> reglas){
        logger.info(">>>> Aplicando Reglas a Empleados");
        empleados.stream()
            .filter( e -> reglas.keySet().contains(e.getNumero()))
            .forEach( e -> aplicarReglaEmpleado(e, reglas.get(e.getNumero())));
    }

    @SuppressWarnings("unchecked")
    public void aplicarReglaEmpleado(Empleado e, Map<String, Object> regla){
        logger.info(">>>> Aplicando Regla a Empleado: {}", e.getNumero()+" | "+e.getNombre());
        //Revisamos si empleado tiene reglas
        if(regla.containsKey("diasNoLaborales")){
            List<Dia> diasR = (List<Dia>) regla.get("diasNoLaborales");
            e.getDiasQueNoLabora().addAll(diasR);
        }

        if(regla.containsKey("turnoFijo")){
            Integer turnoFijoR = (Integer) regla.get("turnoFijo");
            e.setTurnoFijo(turnoFijoR.intValue());
        }

        if(regla.containsKey("rolDefinido")){
            e.setRolPredefinido((Rol) regla.get("rolDefinido"));
        }

        if(regla.containsKey("backups")){
            e.setBackup((List<String>) regla.get("backups"));
        }
    }

    static Dia diaFromString(String s){
        switch(s){
            case "LUNES": return Dia.LUNES;
            case "MARTES": return Dia.MARTES;
            case "MIERCOLES": return Dia.MIERCOLES;
            case "JUEVES": return Dia.JUEVES;
            case "VIERNES": return Dia.VIERNES;
            case "SABADO": return Dia.SABADO;
            case "DOMINGO": return Dia.DOMINGO;
        }
        return null;
    }

}
