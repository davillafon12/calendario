package fi.cr.bncr.empleados.apis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.enums.Rol;
import fi.cr.bncr.empleados.models.Empleado;
import fi.cr.bncr.empleados.models.RestResponse;
import fi.cr.bncr.empleados.models.Turno;

@RestController
@RequestMapping("api/archivo")
public class ArchivoRestController {

    private static Logger logger  = LoggerFactory.getLogger(ArchivoRestController.class);

    private final List<Turno> turnosDisponibles = Arrays.asList(new Turno(1L, 1, Arrays.asList(Dia.MIERCOLES, Dia.JUEVES, Dia.VIERNES, Dia.SABADO), 3),
                                                                new Turno(2L, 2, Arrays.asList(Dia.MIERCOLES, Dia.JUEVES, Dia.MARTES, Dia.SABADO), 4),
                                                                new Turno(3L, 3, Arrays.asList(Dia.MIERCOLES, Dia.LUNES, Dia.VIERNES, Dia.MARTES), 2),
                                                                new Turno(4L, 4, Arrays.asList(Dia.LUNES, Dia.JUEVES, Dia.VIERNES, Dia.MARTES), 1));

    @PostMapping("subir")
    public RestResponse upload(@RequestParam("file") MultipartFile file){
        try {
            logger.info("Loading excel file");
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            int lastSheetNumber = workbook.getNumberOfSheets();
            String sheetName = workbook.getSheetName(lastSheetNumber-1);

            logger.info("Loading info from SHEET: {}", sheetName);

            Sheet sheet = workbook.getSheet(sheetName);
            Iterator<Row> rows = sheet.iterator();

            List<Empleado> empleados = this.getEmpleadosFromExcel(rows);

            empleados.forEach(e -> logger.info(e.toString()));

            workbook.close();

            return new RestResponse(true, "PROBANDO", null);
        } catch (IOException e) {
            return new RestResponse(false, e, e.getMessage());
        }
    }

    private List<Empleado> getEmpleadosFromExcel(Iterator<Row> rows){
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
                String empleadoInfo = "15288";
                if(empleadoInfo.equals(numero)){
                    diasNoLaborados.add(Dia.SABADO);
                    predefinido = Rol.INFORMACION;
                    backup = new Empleado();
                    backup.setNumero("17136");
                }

                empleados.add(new Empleado(Long.getLong(rowNumber+""), numero, nombre, this.getTurnoFromEmpleado(currentRow), diasNoLaborados, predefinido, Rol.NINGUNO, backup));
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

        for (Turno t : this.turnosDisponibles) {
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
        return this.getRandomTurno(diasEmpleado.contains(Dia.SABADO));
    }

    private Turno getRandomTurno(boolean trabajaSabado){
        List<Turno> turnosAplicables = this.turnosDisponibles.stream().filter( t -> {
            if(trabajaSabado){
                return t.getDias().contains(Dia.SABADO);
            }else{
                return !t.getDias().contains(Dia.SABADO);
            }
        }).collect(Collectors.toList());

        Random rand = new Random();
        return turnosAplicables.get(rand.nextInt(turnosAplicables.size()));
    }

}