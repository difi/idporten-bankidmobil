function ventPaaMobil() {
    $.ajaxSetup({ cache: false });
    var delay = 1000;
    var timeout = 90000;
    var started = Date.now();

    if (Date.now() - started < timeout) {
        setTimeout(function(){ sjekkMobilStatus(); }, delay);
    }else {
        $('#complete').submit();
    }
}

function sjekkMobilStatus() {
    var url = '/idporten-bankid-mobil/edge';
    $.get(url, function(status) {
        if (status == 'WAIT') {
            ventPaaMobil();
        }  else {
            $('#complete').submit();
        }
    }).fail(function(jqxhr, textStatus, error) {
        $('#complete').submit();
    });
}