package fi.cr.bncr.empleados.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.cr.bncr.empleados.enums.Rol;

@Component
public class RolService {

    @SuppressWarnings("unused")
    private static Logger logger  = LoggerFactory.getLogger(RolService.class);

    private Map<Rol, Integer> capacidadRoles = new HashMap<Rol, Integer>(){{
        put(Rol.BACKOFFICE, 1);
        put(Rol.PLATAFORMA_EMPRESARIAL, 1);
        put(Rol.CAJA, 50);
        put(Rol.PLATAFORMA, 50);
        put(Rol.INFORMACION, 1);
        put(Rol.CAJA_RAPIDA, 1);
    }};

    public static Map<Rol, Integer> _CACHE_CAPACIDADES_ROLES = new HashMap<>();

    public RolService(){
        this.flushCache();
    }

    public void flushCache(){
        capacidadRoles.entrySet().forEach( entry -> _CACHE_CAPACIDADES_ROLES.put(entry.getKey(), entry.getValue()));
    }

    public Map<Rol, Integer> getCapacidadDeRoles(){
        return this.capacidadRoles;
    }

    public Rol getRolDisponible(Rol predefinido){
        // SI tiene un rol predefinido y hay espacio
        if(!predefinido.equals(Rol.NINGUNO) && this.hayEspacio(predefinido)){
            return predefinido;
        }

        List<Entry<Rol,Integer>> opcionesDisponibles = _CACHE_CAPACIDADES_ROLES.entrySet().stream().filter( entry -> entry.getValue().intValue() > 0).collect(Collectors.toList());

       // logger.info("-------------------------------------");
       // logger.info(opcionesDisponibles.toString());

        Random rand = new Random();
        Entry<Rol,Integer> rolEscogido = opcionesDisponibles.get(rand.nextInt(opcionesDisponibles.size()));

        //Si solo quedan opciones hay que balancear entre los dos
        if(opcionesDisponibles.size() == 2){
            if(opcionesDisponibles.get(0).getValue().intValue() != opcionesDisponibles.get(1).getValue().intValue()){
                if(opcionesDisponibles.get(0).getValue().intValue() > opcionesDisponibles.get(1).getValue().intValue()){
                    rolEscogido = opcionesDisponibles.get(0);
                }else{
                    rolEscogido = opcionesDisponibles.get(1);
                }
            }
        }

        restarRol(rolEscogido.getKey());
        return rolEscogido.getKey();
    }

    private boolean hayEspacio(Rol r){
        if(!_CACHE_CAPACIDADES_ROLES.containsKey(r)) _CACHE_CAPACIDADES_ROLES.put(r, 0);

        if(_CACHE_CAPACIDADES_ROLES.get(r).intValue() > 0){
            restarRol(r);
            return true;
        }

        return false;
    }

    private void restarRol(Rol r){
        _CACHE_CAPACIDADES_ROLES.put(r, new Integer(_CACHE_CAPACIDADES_ROLES.get(r).intValue() - 1));
    }

    public static Rol parseString(String s){
        switch(s){
            case "CAJA": return Rol.CAJA;
            case "PLATAFORMA": return Rol.PLATAFORMA;
            case "BACKOFFICE": return Rol.BACKOFFICE;
            case "INFORMACION": return Rol.INFORMACION;
            case "PLATAFORMA_EMPRESARIAL": return Rol.PLATAFORMA_EMPRESARIAL;
        }
        return Rol.NINGUNO;
    }
}
