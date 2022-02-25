package fi.cr.bncr.empleados.models;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiaLaboral {

    private Dia dia;
    private Rol rol;

}
