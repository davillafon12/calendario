package fi.cr.bncr.empleados.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestResponse {
    
    private Boolean success;
    private Object data;
    private String error;

}
