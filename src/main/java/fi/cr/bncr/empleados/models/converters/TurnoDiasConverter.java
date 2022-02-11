package fi.cr.bncr.empleados.models.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

import fi.cr.bncr.empleados.enums.Dia;

public class TurnoDiasConverter implements AttributeConverter<ArrayList<Dia>, String> {

    @Override
    public String convertToDatabaseColumn(ArrayList<Dia> dias) {
        List<String> dbNames = new ArrayList<>();
        dias.forEach( d -> {
            switch(d){
                case LUNES: dbNames.add("L"); break; 
                case MARTES: dbNames.add("M"); break; 
                case MIERCOLES: dbNames.add("X"); break; 
                case JUEVES: dbNames.add("J"); break; 
                case VIERNES: dbNames.add("V"); break; 
                case SABADO: dbNames.add("S"); break; 
                case DOMINGO: dbNames.add("D"); break; 
            }            
        });
        return String.join("|", dbNames);
    }

    @Override
    public ArrayList<Dia> convertToEntityAttribute(String dbData) {
        List<String> days = Arrays.asList(dbData.split("|"));
        ArrayList<Dia> dias = new ArrayList<>();
        days.forEach( d -> {
            switch(d){
                case "L": dias.add(Dia.LUNES); break;
                case "M": dias.add(Dia.MARTES); break;
                case "X": dias.add(Dia.MIERCOLES); break;
                case "J": dias.add(Dia.JUEVES); break;
                case "V": dias.add(Dia.VIERNES); break;
                case "S": dias.add(Dia.SABADO); break;
                case "D": dias.add(Dia.DOMINGO); break;
            }
        });
        return dias;
    }
    
}
