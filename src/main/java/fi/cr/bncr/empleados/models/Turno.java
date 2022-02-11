package fi.cr.bncr.empleados.models;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.models.converters.TurnoDiasConverter;
import lombok.Data;

@Data
@Entity
@Table(name = "turno")
public class Turno {
    
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column
    private int numero;

    @Column
    @Convert(converter = TurnoDiasConverter.class)
    private ArrayList<Dia> dias;

}
