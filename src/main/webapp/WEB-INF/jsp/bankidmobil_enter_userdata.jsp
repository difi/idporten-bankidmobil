<%@ page import="no.idporten.ui.impl.IDPortenFeedbackType" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="header.jsp" />

<main id="bankidmobil-main">
    <section class="Box">

        <div class="Box_main" id="bankidwrapper">
            <div class="fm-Progress_Container">
                <div class="fm-Progress_Dot active"></div>
                <div class="fm-Progress_Dot"></div>
            </div>

            <c:set var ="warningCode" scope = "session" value='<%= request.getSession().getAttribute("idporten.feedback.WARNING") %>'/>

            <c:if test="${warningCode != null}">
                <div class="notification notification-error with-Icon icon-error">
                    <spring:message code="${warningCode}" text="Feil input"/>
                </div>
            </c:if>

            <form action="#" class="login" method="post">
                    <fieldset>
                        <div class="fm-Fields">
                            <c:set var ="errorMobile" scope = "session" value='<%= request.getSession().getAttribute("idporten.input.CONTACTINFO_MOBILE") %>'/>
                            <div class="fm-Field ${errorMobile != null ? ' error' : ''}">
                                <label for="idporten.input.CONTACTINFO_MOBILE"><spring:message code="no.idporten.module.bankid.input.mobile" text="Mobilnummer"/></label>
                                <input  autofocus=""
                                        tabindex="1"
                                        maxlength="8"
                                        name="idporten.input.CONTACTINFO_MOBILE"
                                        type="tel" id="idporten.input.CONTACTINFO_MOBILE"
                                        placeholder="<spring:message code="no.idporten.module.bankid.input.mobile.help" text="Mobilnummer"/>"
                                        value="${requestScope.mobileNumber}"
                                        autocomplete="off" />
                            </div>

                            <c:set var ="errorBirthDate" scope = "session" value='<%= request.getSession().getAttribute("idporten.input.BIRTHDATE") %>'/>
                            <div class="fm-Field ${errorBirthDate != null ? ' error' : ''}">
                                <label for="idporten.input.BIRTHDATE"><spring:message code="no.idporten.module.bankid.input.birthdate" text="Fødselsdato"/></label>
                                <input  tabindex="2"
                                        maxlength="6"
                                        name="idporten.input.BIRTHDATE"
                                        type="tel"
                                        id="idporten.input.BIRTHDATE"
                                        placeholder="<spring:message code="no.idporten.module.bankid.input.birthdate.help" text="(6 siffer ddmmåå)"/>"
                                        value="${requestScope.birthDate}"
                                        autocomplete="off" />
                            </div>

                        </div>
                        <div class="fm-Controls with-Normal with-Action">
                            <button name="idporten.inputbutton.NEXT" id="nextbtn" tabindex="10" class="btn btn-Action" type="submit">
                                <span><spring:message code="no.idporten.button.next" text="Neste"/></span>
                            </button>
                            <button name="idporten.inputbutton.CANCEL" id="cancelButton" tabindex="11" class="btn btn-Normal">
                                <span><spring:message code="auth.ui.button.cancel" text="Avbryt"/></span>
                            </button>
                        </div>
                    </fieldset>
                </form>

        </div>
    </section>
</main>

