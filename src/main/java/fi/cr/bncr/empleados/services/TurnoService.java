package fi.cr.bncr.empleados.services;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.models.Turno;

@Component
public class TurnoService {

    private static Logger logger  = LoggerFactory.getLogger(TurnoService.class);

    private final List<Turno> turnosDisponibles = Arrays.asList(new Turno(1L, 1, Arrays.asList(Dia.MIERCOLES, Dia.JUEVES, Dia.VIERNES, Dia.SABADO), 3),
                                                                new Turno(2L, 2, Arrays.asList(Dia.MIERCOLES, Dia.JUEVES, Dia.MARTES, Dia.SABADO), 4),
                                                                new Turno(3L, 3, Arrays.asList(Dia.MIERCOLES, Dia.LUNES, Dia.VIERNES, Dia.MARTES), 2),
                                                                new Turno(4L, 4, Arrays.asList(Dia.LUNES, Dia.JUEVES, Dia.VIERNES, Dia.MARTES), 1));

    public List<Turno> getAllTurnos(){
        return this.turnosDisponibles;
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

}
