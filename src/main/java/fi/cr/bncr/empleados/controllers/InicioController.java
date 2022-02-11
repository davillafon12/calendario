package fi.cr.bncr.empleados.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {
    
    @GetMapping("/")
	public String inicio(Model m) {
		m.addAttribute("title", "Calendario - Inicio");
		return "index";
	}

}
