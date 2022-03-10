package fi.cr.bncr.empleados.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.enums.Rol;
import fi.cr.bncr.empleados.models.DiaLaboral;
import fi.cr.bncr.empleados.models.Empleado;

@Component
public class GenerarDiasLaboralesProcessor implements BaseProcessor<Empleado> {

    @Override
    public Empleado process(Empleado e) {

        if(e.getDiasLaboresSiguientes() == null)
            e.setDiasLaboresSiguientes(new ArrayList<DiaLaboral>());



        e.getTurnoSiguiente().getDias().forEach( d -> {
            Rol rolAAsignar = e.getRolSiguiente();

            if((e.getRolSiguiente().equals(Rol.BACKOFFICE) || e.getRolSiguiente().equals(Rol.PLATAFORMA_EMPRESARIAL) || e.getRolSiguiente().equals(Rol.INFORMACION)) && d.equals(Dia.SABADO)){
                //Hay roles que no se deben asignar los sabados
                //Si es el caso debemos asignarlo a caja o plataforma
                List<Rol> paEscoger = new ArrayList<Rol>(){{
                                        add(Rol.CAJA);
                                        add(Rol.PLATAFORMA);
                                    }};
                Random random = new Random();
                rolAAsignar = paEscoger.get(random.nextInt(paEscoger.size()));
            }

            e.getDiasLaboresSiguientes().add(new DiaLaboral(d,rolAAsignar));
        });

        return e;
    }

}
