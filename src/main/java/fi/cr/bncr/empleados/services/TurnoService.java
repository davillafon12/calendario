package fi.cr.bncr.empleados.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.models.Turno;

@Component
public class TurnoService {

    private static Logger logger  = LoggerFactory.getLogger(TurnoService.class);

    private static final String _SHEET_NAME = "Turnos";

    /*private final List<Turno> turnosDisponibles = Arrays.asList(new Turno(1L, 1, Arrays.asList(Dia.MIERCOLES, Dia.JUEVES, Dia.VIERNES, Dia.SABADO), 3),
                                                                new Turno(2L, 2, Arrays.asList(Dia.MIERCOLES, Dia.JUEVES, Dia.MARTES, Dia.SABADO), 4),
                                                                new Turno(3L, 3, Arrays.asList(Dia.MIERCOLES, Dia.LUNES, Dia.VIERNES, Dia.MARTES), 2),
                                                                new Turno(4L, 4, Arrays.asList(Dia.LUNES, Dia.JUEVES, Dia.VIERNES, Dia.MARTES), 1),
                                                                new Turno(5L, 5, Arrays.asList(Dia.LUNES, Dia.MARTES, Dia.MIERCOLES, Dia.JUEVES), 5),
                                                                new Turno(6L, 6, Arrays.asList(Dia.VIERNES, Dia.MARTES, Dia.MIERCOLES, Dia.SABADO), 6));*/

    private List<Turno> turnosDisponibles = new ArrayList<>();

    public List<Turno> getAllTurnos(){
        return this.turnosDisponibles;
    }

    public Turno getTurnoByNumero(int numero){
        return this.turnosDisponibles.stream().filter( t -> t.getNumero() == numero).findFirst().get();
    }

    public Turno getRandomTurno(boolean trabajaSabado){
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

    public Turno getSiguienteTurno(Turno ta){
        //logger.info(ta.toString());
        for(Turno td : this.turnosDisponibles){

            //logger.info(td.getNumero()+"|"+ta.getSiguienteTurno());

            if(td.getNumero() == ta.getSiguienteTurno()) return td;
        }
        throw new IllegalStateException("No existe un turno siguiente disponible para este turno ["+ta.getSiguienteTurno()+"]");
    }

    public void loadTurnosFromFile(Workbook workbook){
        logger.info(">>>> Cargando Turnos del Archivo");

        logger.info("Loading info from SHEET: {}", _SHEET_NAME);

        Sheet sheet = workbook.getSheet(_SHEET_NAME);
        Iterator<Row> rows = sheet.iterator();
        this.loadTurnosFromExcel(rows);

        logger.info("TURNOS CARGADOS ----------------------------------------------");
        this.turnosDisponibles.forEach(e -> logger.info(e.toString()));
    }

    private void loadTurnosFromExcel(Iterator<Row> rows){
        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            // skip header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            String numero = currentRow.getCell(0) == null ? "" : currentRow.getCell(0).toString().trim().replace(".0", "");
            if(!numero.isEmpty()){
                String siguiente = currentRow.getCell(1) == null ? "" : currentRow.getCell(1).toString().trim().replace(".0", "");
                List<Dia> dias = this.getDiasFromFile(currentRow);

                //logger.info("{} {} {} {}", numero, numero, dias, siguiente);
                //logger.info("{} {} {} {}", Long.parseLong(numero), Integer.parseInt(numero), dias, siguienteTurno);
                this.turnosDisponibles.add(new Turno(Long.parseLong(numero), Integer.parseInt(numero), dias, Integer.parseInt(siguiente)));
            }
        }
    }

    private List<Dia> getDiasFromFile(Row eRow){
        List<Dia> dias = new ArrayList<>();
        for(int index = 2; index <= 8; index++){
            if(eRow.getCell(index) == null ? false : !eRow.getCell(index).toString().trim().isEmpty()){
                switch(index){
                    case 2: dias.add(Dia.LUNES); continue;
                    case 3: dias.add(Dia.MARTES); continue;
                    case 4: dias.add(Dia.MIERCOLES); continue;
                    case 5: dias.add(Dia.JUEVES); continue;
                    case 6: dias.add(Dia.VIERNES); continue;
                    case 7: dias.add(Dia.SABADO); continue;
                    case 8: dias.add(Dia.DOMINGO); continue;
                }
            }
        }
        return dias;
    }

}
