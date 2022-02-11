package fi.cr.bncr.empleados.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.cr.bncr.empleados.models.Turno;
import fi.cr.bncr.empleados.models.repository.TurnoRepository;

@Component
public class TurnoService {

    @Autowired
    private TurnoRepository repo;
    
    public List<Turno> getAllTurnos(){
        return repo.findAll();
    }
}
