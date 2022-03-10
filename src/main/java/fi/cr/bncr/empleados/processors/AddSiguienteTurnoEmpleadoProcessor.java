package fi.cr.bncr.empleados.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.models.Empleado;
import fi.cr.bncr.empleados.models.Turno;
import fi.cr.bncr.empleados.services.TurnoService;
import fi.cr.bncr.empleados.utils.Utils;

@Component
public class AddSiguienteTurnoEmpleadoProcessor implements BaseProcessor<Empleado>{

    private static Logger logger  = LoggerFactory.getLogger(AddSiguienteTurnoEmpleadoProcessor.class);

    @Autowired
    TurnoService turnoService;

    @Override
    public Empleado process(Empleado empleado) {
        Turno actual = empleado.getTurnoActual();
        Turno siguiente = empleado.getTurnoFijo() > 0 ? turnoService.getTurnoByNumero(empleado.getTurnoFijo()) : turnoService.getSiguienteTurno(actual);

        if(this.tieneRestriccionDeDiaLaboral(empleado, siguiente)){
            logger.info("HAY QUE CAMBIARLE EL TURNO A: "+empleado.getNombre());
            //logger.info(siguiente.toString());
            siguiente = this.getTurnoQueCalce(empleado);
        }

        empleado.setTurnoSiguiente(siguiente);
        return empleado;
    }

    private boolean tieneRestriccionDeDiaLaboral(Empleado e, Turno t){
        String diasTurno = Utils.join(",", t.getDias());
        for(Dia d : e.getDiasQueNoLabora()){
            if(diasTurno.contains(d.toString())) return true;
        }
        return false;
    }

    private Turno getTurnoQueCalce(Empleado empleado){
        List<Turno> turnosDisponibles = new ArrayList<>();

        for(Turno t : turnoService.getAllTurnos()){
            //if(t.getNumero() == empleado.getTurnoActual().getNumero()) continue; //Evitar asignar el mismo turno

            int items = 0;
            for(Dia d : empleado.getDiasQueNoLabora()){ // D,S
                //logger.info("DIA: "+d+" TURNO: "+t.getDias());
                if(t.getDias().contains(d)) items++;
                //logger.info("PASOOOO");
            }

            if(items == 0){
                turnosDisponibles.add(t);
            }
        }

        //logger.info("TURNOS DISPONIBLES");
        //logger.info(turnosDisponibles.toString());
        if(turnosDisponibles.size() == 0){
            throw new IllegalStateException("No hay turno disponible para el empleado: "+empleado.getNombre());
        }

        Random rand = new Random();
        return turnosDisponibles.get(rand.nextInt(turnosDisponibles.size()));
    }

}
