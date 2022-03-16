package fi.cr.bncr.empleados.apis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fi.cr.bncr.empleados.helpers.FileHelper;
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

    @Autowired
    private FileHelper fileHelper;


    @PostMapping("subir")
    public RestResponse upload(@RequestParam("file") MultipartFile file){

        RestResponse r = new RestResponse(false, null, null);;

        try{
            //Cargamos datos preeliminares
            //Cargamos Turnos
            turnoService.loadTurnosFromFile(file);
            //Cargamos Reglas
            Future<Map<String, Map<String, Object>>> reglasF = empleadoService.getReglasEmpleadosFromFile(file);
            //Cargamos Empleados
            Future<List<Empleado>> empleadosF = empleadoService.loadEmpleadosFromFile(file);

            Map<String, Map<String, Object>> reglas = reglasF.get();
            List<Empleado> empleados = empleadosF.get();

            if(turnoService.getAllTurnos().size() > 0){
                if(empleados.size() > 0){
                    //Aplicamos reglas
                    empleadoService.aplicarReglasEmpleados(empleados, reglas);

                    //Aplicamos siguiente movimiento
                    empleadoService.asignarSiguienteMovimiento(empleados);

                    //Guardamos el excel para bretearlo despues
                    fileHelper.saveBinaryExcelFile(file);

                    //Guardamos empleados
                    fileHelper.saveBinaryEmpleados(empleados);

                    r.setSuccess(true);
                    r.setData(empleados);
                }else{
                    r.setError("No hay empleados disponibles");
                }
            }else{
                r.setError("No hay turnos disponibles");
            }
        }catch(Exception e){
            r.setError("Error al ejecutar metodos Async");
            r.setData(e.getMessage());
            logger.error("Error al ejecutar metodos ASYNC", e);
        }

        return r;
    }

    @GetMapping("/download-excel")
    public ResponseEntity<Resource> getFile() {
        String filename = "movimiento.xlsx";
        InputStreamResource file = new InputStreamResource(fileHelper.getExcelFileIS());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(file);
    }
}
