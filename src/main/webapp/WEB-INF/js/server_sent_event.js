function setUpEmitter(sessionId) {
    var source = new EventSource('/idporten-bankid-mobil/mobilstatusEmitter/' + sessionId);
    source.addEventListener('message', function (e) {
        $('#complete').submit();
    });
    source.onerror = function(e) {
        $('#complete').submit();
    };
}