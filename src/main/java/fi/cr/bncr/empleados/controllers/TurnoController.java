package fi.cr.bncr.empleados.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TurnoController {
    
    @GetMapping("/turnos")
    public String inicio(Model m){
        m.addAttribute("title", "Calendario - Turnos");
        return "turno/inicio";
    }
}
