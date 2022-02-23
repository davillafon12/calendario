package fi.cr.bncr.empleados.models;

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
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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
    private List<Dia> dias;

    private int siguienteTurno;

    @Override
    public String toString(){
        return "Numero: "+numero+" Siguiente: "+siguienteTurno+" Dias: "+join(",", dias);
    }

    private static String join(String separator, List<Dia> input) {

        if (input == null || input.size() <= 0) return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.size(); i++) {

            sb.append(input.get(i));

            // if not the last item
            if (i != input.size() - 1) {
                sb.append(separator);
            }

        }

        return sb.toString();

    }

}
