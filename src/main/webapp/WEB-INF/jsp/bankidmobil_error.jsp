<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="header.jsp" />

<main id="bankidmobil-main">
    <section class="Box">
        <div id="bankidwrapper">
            <div class="notification notification-error with-Icon icon-error">
                <spring:message code="no.idporten.module.bankid.error.text1"/>
                <br><spring:message code="no.idporten.module.bankid.error.text2"/>
                <c:if test="${not empty errorCode}">
                    <br><spring:message code="no.idporten.module.bankid.error.text3"/> <c:out value="${errorCode}" escapeXml="true"/>
                </c:if>
            </div>

            <form method="post">
                <fieldset>
                    <div class="fm-Controls with-Action">
                        <button name="idporten.inputbutton.CLOSE" id="closeButton" tabindex="10" class="btn btn-Action">
                            <span><spring:message code="auth.ui.button.retry" text="Prøv på nytt"/></span>
                        </button>
                    </div>
                </fieldset>
            </form>
        </div>
    </section>
</main>

