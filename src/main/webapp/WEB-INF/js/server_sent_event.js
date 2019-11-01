function setUpEmitter(sessionId) {
    var source = new EventSource('/idporten-bankid-mobil/mobilstatusEmitter/' + sessionId);
    source.addEventListener('message', function (e) {
        $('#complete').submit();
    });
    source.onerror = function(e) {
        sjekkLagretStatus();
    };
}

function sjekkLagretStatus() {
    var url = '/idporten-bankid-mobil/edge';
    $.get(url, function(status) {
        //Fortsetter å vente
        if (status == 'WAIT') {
        }  else {
            $('#complete').submit();
        }
    }).fail(function(jqxhr, textStatus, error) {
        $('#complete').submit();
    });
}