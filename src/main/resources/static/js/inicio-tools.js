const _ORDEN_DIAS = ["LUNES","MARTES","MIERCOLES","JUEVES","VIERNES","SABADO","DOMINGO"];

const _DIAS = {
    lunes:"LUNES",
    martes:"MARTES",
    miercoles:"MIERCOLES",
    jueves:"JUEVES",
    viernes:"VIERNES",
    sabado:"SABADO",
    domingo:"DOMINGO"
};

var _ROLES = {};
_ROLES["NINGUNO"] = {enum:"NINGUNO", nombre: "Ninguno", abreviatura: "Ninguno", totales:{}};
_ROLES["CAJA"] = {enum:"CAJA", nombre: "Caja", abreviatura: "Caja", totales:{}};
_ROLES["CAJA_RAPIDA"] = {enum:"CAJA_RAPIDA", nombre: "Caja Rápida", abreviatura: "C. Rápida", totales:{}};
_ROLES["PLATAFORMA"] = {enum:"PLATAFORMA", nombre: "Plataforma", abreviatura: "Plat.", totales:{}};
_ROLES["PLATAFORMA_EMPRESARIAL"] = {enum:"PLATAFORMA_EMPRESARIAL", nombre: "Plataforma Empresarial", abreviatura: "Plat. Emp.", totales:{}};
_ROLES["INFORMACION"] = {enum:"INFORMACION", nombre: "Información", abreviatura: "Info.", totales:{}};
_ROLES["BACKOFFICE"] = {enum:"BACKOFFICE", nombre: "Backoffice", abreviatura: "Backoffice", totales:{}};

$(document).ready(function(){

    $("#but_upload").click(generarMovimiento);

    $("#btn_generar_movimiento").click(generarMovimiento);

    $("#btn_descargar_excel").click(descargarArchivoExcel);
});

function loadEmpleados(empleados){
    limpiarTotales();
    var html = "";
    var htmlActual = "";
    for(var index in empleados){
        var e = empleados[index];
        htmlActual += generarFilaActual(e);
        html += generarFilaSiguiente(e);
    }

    $("#tabla_empleados tbody").html(html);
    $("#tabla_empleados_actual tbody").html(htmlActual);


    generarTotales();
}

function generarMovimiento(){

    var fd = new FormData();
    var files = $('#file')[0].files;

    // Check file selected or not
    if(files.length > 0 ){
        fd.append('file',files[0]);

        $.ajax({
            url: 'api/archivo/subir',
            type: 'post',
            data: fd,
            contentType: false,
            processData: false,
            beforeSend: function(){
                $("#modal_carga").show();
                $("#contenedor_siguiente_turno, #contenedor_actual_turno, #contenedor_totales").addClass("hide");
            },
            success: function(response){
                $("#modal_carga").hide();
                if(response.success){
                    loadEmpleados(response.data);
                    $("#contenedor_carga_archivo").hide();
                    $("#contenedor_siguiente_turno, #contenedor_actual_turno, #contenedor_totales").removeClass("hide");
                }else{
                    $("#contenedor_carga_archivo").show();
                    alert("Hubo un error al cargar el archivo excel!");
                }
            },
        });
    }else{
        alert("Por favor seleccione un archivo");
    }
}

function getClassForDia(dl){
    if(dl.rol != ""){
        return "labora";
    }else{
        return "no-labora";
    }
}

function getFinalRol(dl){
    if(typeof _ROLES[dl.rol] !== "undefined"){

        return _ROLES[dl.rol].abreviatura;
    }else{
        return "";
    }
}

function generarFilaActual(e){
    var htmlActual = "<tr><td class='"+(e.turnoFijo > 0 ? "fijo" : "" )+"'>"+e.numero+"</td>"+
                "<td class='"+(e.turnoFijo > 0 ? "fijo" : "" )+"'>"+e.nombre+"</td>";


    for(var index in _ORDEN_DIAS){
        var dia = _ORDEN_DIAS[index];
        var dl = getDiaFromLista(e.diasLaboresActuales, dia);
        htmlActual += "<td class='text-center "+getClassForDia(dl)+"'>"+getFinalRol(dl)+"</td>";
    }

    htmlActual += "<td class='text-center'></td>"+
                    "<td class='text-center'></td></tr>";

   return htmlActual;
}

function generarFilaSiguiente(e){
    var htmlSiguiente = "<tr><td class='"+(e.turnoFijo > 0 ? "fijo" : "" )+"'>"+e.numero+"</td>"+
                "<td class='"+(e.turnoFijo > 0 ? "fijo" : "" )+"'>"+e.nombre+"</td>";


    for(var index in _ORDEN_DIAS){
        var dia = _ORDEN_DIAS[index];
        var dl = getDiaFromLista(e.diasLaboresSiguientes, dia);
        htmlSiguiente += "<td class='text-center "+getClassForDia(dl)+"'>"+getFinalRol(dl)+"</td>";

        //Sumamos el rol
        if(typeof _ROLES[dl.rol] !== "undefined"){//Existe rol
            if(typeof _ROLES[dl.rol].totales[dl.dia] == "undefined") _ROLES[dl.rol].totales[dl.dia] = 0;

            _ROLES[dl.rol].totales[dl.dia]++;
        }
    }

    htmlSiguiente += "<td class='text-center'></td>"+
                    "<td class='text-center'></td></tr>";

    return htmlSiguiente;
}

function getDiaFromLista(lista, dia){
    for(var index in lista){
        var dl = lista[index];
        if(dl.dia == dia) return dl;
    }
    return {dia:dia, rol:""}
}

function generarTotales(){
    var html = "";
    var totalesDias = {};
    for(var index in _ROLES){
        var rol = _ROLES[index];
        html += "<tr><td>"+rol.nombre+"</td>";

        for(var index in _ORDEN_DIAS){
            var dia = _ORDEN_DIAS[index];
            var total = 0;
            if(typeof rol.totales[dia] != "undefined"){
                total = rol.totales[dia];
            }

            if(typeof totalesDias[dia] == "undefined") totalesDias[dia] = 0;

            totalesDias[dia] += total;

            html += "<td>"+total+"</td>";
        }

        html += "</tr>";
    }

    html += "<tr><td>Totales</td>";
    for(var index in _ORDEN_DIAS){
        var dia = _ORDEN_DIAS[index];
        html += "<td>"+totalesDias[dia]+"</td>";
    }
    html += "</tr>";

    //console.log(totalesDias);
    $("#tabla_roles_totales tbody").html(html);
}

function limpiarTotales(){
    for(var index in _ROLES){
        _ROLES[index].totales = {};
    }
}

function descargarArchivoExcel(){

}