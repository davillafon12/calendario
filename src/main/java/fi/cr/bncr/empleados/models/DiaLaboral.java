package fi.cr.bncr.empleados.models;

import java.io.Serializable;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiaLaboral implements Serializable{

    private Dia dia;
    private Rol rol;

}
