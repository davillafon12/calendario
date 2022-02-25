package fi.cr.bncr.empleados.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class EmpleadoService {

    private static Logger logger  = LoggerFactory.getLogger(EmpleadoService.class);

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private AddSiguienteTurnoEmpleadoProcessor addSiguienteTurnoEmpleadoProcessor;

    @Autowired
    private AsignarRolEmpleadoProcessor asignarRolEmpleadoProcessor;

    @Autowired GenerarDiasLaboralesProcessor generarDiasLaboralesProcessor;

    @Autowired
    private RolService rolService;


    public List<Empleado> loadExcelFile(MultipartFile file){
        logger.info("Loading excel file");
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
        } catch (IOException e) {
            logger.error("No se pudo cargar el archivo excel", e);
        }
        return empleados;
    }

    public List<Empleado> getEmpleadosFromExcel(Iterator<Row> rows){
        logger.info("Loading employees");
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
                List<Dia> diasNoLaborados = new ArrayList<>();
                diasNoLaborados.add(Dia.DOMINGO);

                Rol predefinido = Rol.NINGUNO;
                Empleado backup = null;

                //Excepciones y salvedades
                if("15288".equals(numero)){
                    diasNoLaborados.add(Dia.SABADO);
                    predefinido = Rol.INFORMACION;
                    backup = new Empleado();
                    backup.setNumero("17136");
                }

                if("14668".equals(numero)){
                    predefinido = Rol.BACKOFFICE;
                    backup = new Empleado();
                    backup.setNumero("16980");
                }

                if("17136".equals(numero)){
                    diasNoLaborados.add(Dia.LUNES);
                    diasNoLaborados.add(Dia.MARTES);
                }

                if("17144".equals(numero) || "13214".equals(numero)){
                    diasNoLaborados.add(Dia.JUEVES);
                    diasNoLaborados.add(Dia.SABADO);
                }

                if("16807".equals(numero)){
                    diasNoLaborados.add(Dia.VIERNES);
                    diasNoLaborados.add(Dia.SABADO);
                }

                if("11938".equals(numero)){
                    diasNoLaborados.add(Dia.LUNES);
                    diasNoLaborados.add(Dia.VIERNES);
                }

                if("16570".equals(numero)){
                    diasNoLaborados.add(Dia.LUNES);
                    diasNoLaborados.add(Dia.JUEVES);
                }

                empleados.add(new Empleado(Long.getLong(rowNumber+""), numero, nombre, this.getTurnoFromEmpleado(currentRow), null, diasNoLaborados, predefinido, Rol.NINGUNO, backup, this.getDiasActualesLaboradosFromEmpleado(currentRow), null));
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

        if(contenido.contains("caja")){
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

        rolService.flushCache();
    }


}
