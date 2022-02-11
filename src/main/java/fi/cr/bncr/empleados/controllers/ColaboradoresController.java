package fi.cr.bncr.empleados.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ColaboradoresController {
    
    @GetMapping("/colaboradores")
    public String principal(Model m){
        m.addAttribute("title", "Calendario - Colaboradores");
        return "colaboradores/principal";
    }
}
