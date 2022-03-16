package fi.cr.bncr.empleados.models;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import fi.cr.bncr.empleados.enums.Dia;
import fi.cr.bncr.empleados.models.converters.TurnoDiasConverter;
import fi.cr.bncr.empleados.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
@Table(name = "turno")
public class Turno implements Serializable{

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private int numero;

    @Column
    @Convert(converter = TurnoDiasConverter.class)
    private List<Dia> dias;

    private int siguienteTurno;

    @Override
    public String toString(){
        return "Numero: "+numero+" Siguiente: "+siguienteTurno+" Dias: "+Utils.join(",", dias);
    }



}
