package fi.cr.bncr.empleados.helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import fi.cr.bncr.empleados.models.Empleado;

@Component
public class FileHelper {

    private static Logger logger = LoggerFactory.getLogger(FileHelper.class);

    private String relativePath;

    public FileHelper(){
        try{
            File f = new File(System.getProperty("java.class.path"));
            File dir = f.getAbsoluteFile().getParentFile();
            this.relativePath = dir.toString();
            //Si tiene : quiere decir que estamos corriendolo desde el IDE y no como jar
            if(this.relativePath.contains(":")){
                String path = FileHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                this.relativePath = URLDecoder.decode(path, "UTF-8").replace("classes/", "");
            }
            logger.info("PATH para archivos es: {}", this.relativePath);
        }catch(Exception e){
            logger.error("No se pudo cargar el PATH para archivos", e);
        }
    }

    @Async
    public void saveBinaryExcelFile(MultipartFile multipartFile){
        try{
            String path = this.relativePath + "excel.tmp";
            File file = new File(path);

            logger.info(">>>> Guardando el excel en el archivo: {}", path);

            try (OutputStream os = new FileOutputStream(file)) {
                os.write(multipartFile.getBytes());
            }
        }catch(Exception e){
            logger.error("No se pudo guardar el archivo excel", e);
        }
    }

    @Async
    public void saveBinaryEmpleados(List<Empleado> empleados){
        try{
            String path = this.relativePath + "empleados.tmp";
            File file = new File(path);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(empleados);

            logger.info(">>>> Guardando empleados en el archivo: {}", path);

            try (OutputStream os = new FileOutputStream(file)) {
                os.write(bos.toByteArray());
            }
        }catch(Exception e){
            logger.error("No se pudo guardar el archivo excel", e);
        }
    }

    public InputStream getExcelFileIS(){
        try{
            String path = this.relativePath + "excel.tmp";
            logger.info(">>>> Cargando el archivo excel: {}", path);
            return new FileInputStream(path);
        }catch(Exception e){
            logger.error("No se pudo cargar el archivo excel", e);
        }
        return null;
    }

}
