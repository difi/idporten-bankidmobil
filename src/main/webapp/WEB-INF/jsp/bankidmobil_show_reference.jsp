<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="core" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="header.jsp" />


<head>
    <title>Tittel</title>
    <script src="/idporten-bankid-mobil/js/jquery-3.2.1.min.js"></script>
    <script src="/idporten-bankid-mobil/js/bankIDMobil.js"></script>
    <script src="/idporten-bankid-mobil/js/server_sent_event.js"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            var eventSourceValue = <%= request.getSession().getAttribute("eventsourceEnabled") %>;
            if (!!window.EventSource && eventSourceValue) {
                var sessionId = "<%=request.getSession().getAttribute("sid")%>";
                setUpEmitter(sessionId);
            } else {
                ventPaaMobil();
            }
        });
    </script>
</head>
<main id="bankidmobil-main">
    <section class="Box" >
        <div id="bankidwrapper">
            <div class="fm-Progress_Container">
                <div class="fm-Progress_Dot"></div>
                <div class="fm-Progress_Dot active"></div>
            </div>

            <form id="complete" method="post" action="bidresponse">
                <fieldset>
                    <div class="notification with-Icon with-Link icon-sms">
                        <spring:message code="no.idporten.module.bankid.showcode.code" text="referanseord"/>
                        <strong class=""><%= request.getAttribute("code") %></strong>
                        <br/><spring:message code="no.idporten.module.bankid.showcode.info" text="Følg instruksene på mobilen din"/>
                    </div>
                    <div class="fm-Controls with-Normal">
                        <button name="idporten.inputbutton.CANCEL" id="cancelButton" tabindex="10" class="btn btn-Normal">
                            <span><spring:message code="auth.ui.button.cancel" text="Avbryt"/></span>
                        </button>
                    </div>
                </fieldset>
            </form>
        </div>
    </section>
</main>