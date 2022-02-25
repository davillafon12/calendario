package fi.cr.bncr.empleados.processors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.cr.bncr.empleados.enums.Rol;
import fi.cr.bncr.empleados.models.Empleado;
import fi.cr.bncr.empleados.services.RolService;

@Component
public class AsignarRolEmpleadoProcessor implements BaseProcessor<Empleado>{

    @Autowired
    private RolService rolService;

    @Override
    public Empleado process(Empleado e) {
        if(e.getRolSiguiente().equals(Rol.NINGUNO)){
            e.setRolSiguiente(rolService.getRolDisponible(e.getRolPredefinido()));
        }
        return e;
    }



}
