package fi.cr.bncr.empleados.apis;

import java.util.List;
import java.util.Map;

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
import fi.cr.bncr.empleados.services.TurnoService;

@RestController
@RequestMapping("api/archivo")
public class ArchivoRestController {

    private static Logger logger  = LoggerFactory.getLogger(ArchivoRestController.class);

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private TurnoService turnoService;


    @PostMapping("subir")
    public RestResponse upload(@RequestParam("file") MultipartFile file){

        RestResponse r = new RestResponse(false, null, null);;

        //Cargamos datos preeliminares
        turnoService.loadTurnosFromFile(file);

        if(turnoService.getAllTurnos().size() > 0){
            Map<String, Map<String, Object>> reglas = empleadoService.getReglasEmpleadosFromFile(file);

            List<Empleado> empleados = empleadoService.loadEmpleadosFromFile(file, reglas);

            if(empleados.size() > 0){
            }else{
                r.setError("No hay empleados disponibles");
            }
        }else{
            r.setError("No hay turnos disponibles");
        }

        return r;

        /*List<Empleado> empleados = empleadoService.loadExcelFile(file);
        if(empleados != null){
            //empleados.forEach(e -> logger.info(e.toString()));

            empleadoService.asignarSiguienteMovimiento(empleados);

            //logger.info("----------------------------------------------");
            empleados.forEach(e -> logger.info(e.toString()));

            return new RestResponse(true, empleados, null);
        }else{
            return new RestResponse(false, "Error al cargar el archivo Excel", "Objeto Nulo");
        }*/
    }
}
