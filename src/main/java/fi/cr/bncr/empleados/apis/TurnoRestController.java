package fi.cr.bncr.empleados.apis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.cr.bncr.empleados.models.RestResponse;
import fi.cr.bncr.empleados.services.TurnoService;

@RestController
@RequestMapping("api/turno")
public class TurnoRestController {
    
    @Autowired
    TurnoService service;

    @GetMapping("lista")
    public RestResponse getList(){        
        return new RestResponse(true, service.getAllTurnos(), null);
    }
}
