package fi.cr.bncr.empleados.models;

import java.util.List;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Empleado {

    private Long id;
    private String numero;
    private String nombre;
    private Turno turnoActual;

    private List<Dia> diasQueNoLabora;
    private Rol rolPredefinido;
    private Rol rolActual;
    private Empleado backup;

    @Override
    public String toString(){
        return "Numero: "+numero+" Nombre: "+nombre+" Rol Predefinido: "+rolPredefinido+" Rol Actual: "+rolActual+" Dias NO Laborales: "+diasQueNoLabora+" Turno: {"+turnoActual+"} Empleado Backup: {"+backup+"}";
    }
}
