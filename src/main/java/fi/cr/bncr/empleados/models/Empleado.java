package fi.cr.bncr.empleados.models;

import java.io.Serializable;
import java.util.List;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.enums.Rol;
import fi.cr.bncr.empleados.processors.BaseProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Empleado implements Serializable{

    private Long id;
    private String numero;
    private String nombre;
    private Turno turnoActual;
    private Turno turnoSiguiente;

    private List<Dia> diasQueNoLabora;
    private Rol rolPredefinido;
    private Rol rolSiguiente;
    private List<String> backup;

    private List<DiaLaboral> diasLaboresActuales;
    private List<DiaLaboral> diasLaboresSiguientes;

    private int turnoFijo;

    @Override
    public String toString(){
        return "Numero: "+numero+" Nombre: "+nombre+" Rol Predefinido: "+rolPredefinido+
        " Rol Siguiente: "+rolSiguiente+" Dias NO Laborales: "+diasQueNoLabora+" Turno Actual: {"+turnoActual+"} Turno Siguiente: {"+turnoSiguiente+"} Empleado Backup: {"+backup+"}" +
        " Dias Laborales Actuales: "+diasLaboresActuales+" Dias Laborales Siguientes: "+diasLaboresSiguientes+" Turno Fijo: "+turnoFijo;
    }

    public Empleado processar(BaseProcessor<Empleado> processor){
        return processor.process(this);
    }
}
