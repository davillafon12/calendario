package fi.cr.bncr.empleados.apis;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fi.cr.bncr.empleados.models.Empleado;
import fi.cr.bncr.empleados.models.RestResponse;
import fi.cr.bncr.empleados.services.EmpleadoService;

@RestController
@RequestMapping("api/archivo")
public class ArchivoRestController {

    private static Logger logger  = LoggerFactory.getLogger(ArchivoRestController.class);

    @Autowired
    private EmpleadoService empleadoService;


    @PostMapping("subir")
    public RestResponse upload(@RequestParam("file") MultipartFile file){
        List<Empleado> empleados = empleadoService.loadExcelFile(file);
        if(empleados != null){
            //empleados.forEach(e -> logger.info(e.toString()));

            empleadoService.asignarSiguienteMovimiento(empleados);

            //logger.info("----------------------------------------------");
            empleados.forEach(e -> logger.info(e.toString()));

            return new RestResponse(true, "PROBANDO", null);
        }else{
            return new RestResponse(false, "Error al cargar el archivo Excel", "Objeto Nulo");
        }
    }
}
