package fi.cr.bncr.empleados.processors;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import fi.cr.bncr.empleados.models.DiaLaboral;
import fi.cr.bncr.empleados.models.Empleado;

@Component
public class GenerarDiasLaboralesProcessor implements BaseProcessor<Empleado> {

    @Override
    public Empleado process(Empleado e) {

        if(e.getDiasLaboresSiguientes() == null)
            e.setDiasLaboresSiguientes(new ArrayList<DiaLaboral>());

        e.getTurnoSiguiente().getDias().forEach( d -> e.getDiasLaboresSiguientes().add(new DiaLaboral(d,e.getRolSiguiente())));

        return e;
    }

}
