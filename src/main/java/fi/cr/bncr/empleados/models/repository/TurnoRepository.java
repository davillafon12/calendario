package fi.cr.bncr.empleados.models.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.cr.bncr.empleados.models.Turno;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
}
