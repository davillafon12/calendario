$(document).ready(function() {
    $('#dataTable').DataTable({
        language: {
            url: '/vendor/datatables/es_es.json'
        }
    });

    $(".opcion-dia").click(toggleDia);
});

function toggleDia(e){
    var elem = $(e.target).parent();
    
    if(elem.hasClass("btn-light")){
        elem.removeClass("btn-light");
        elem.addClass("btn-success");
    }else{
        elem.addClass("btn-light");
        elem.removeClass("btn-success");
    }
}