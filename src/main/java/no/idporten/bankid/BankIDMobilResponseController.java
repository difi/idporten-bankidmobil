package no.idporten.bankid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@RequestMapping
@Controller
@Slf4j
public class BankIDMobilResponseController {

    @Value("${idporten.redirecturl}")
    private String redirectUrl;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/authorizationCode")
    public void receiveResponse(HttpServletRequest request,
                                @RequestParam String code,
                                HttpServletResponse response) throws URISyntaxException, IOException {
        HttpSession session = request.getSession();
        String url = UriComponentsBuilder.newInstance()
                .uri(new URI((String) session.getAttribute("redirectUrl")))
                .queryParam("code", code)
                .queryParam("ForceAuth", session.getAttribute("forceAuth"))
                .queryParam("gx_charset", request.getSession().getAttribute("gx_charset"))
                .queryParam("locale", request.getSession().getAttribute("locale"))
                .queryParam("goto", request.getSession().getAttribute("goto"))
                .queryParam("service", request.getSession().getAttribute("service"))
                .build()
                .toUriString();
        renderHelpingPage(response, url);
    }

    private void renderHelpingPage(HttpServletResponse response, String url) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(top(url));
        result.append(footer());
        response.setContentType(getContentType());
        response.getWriter().append(result);
    }

    private String top(String url) {
        return "<html>" +
                "<head><title>Submit This Form</title></head>" +
                "<body onload=\"javascript:document.forms[0].submit()\">" +
                "<form target=\"_parent\" method=\"post\" action=\"" + url + "\">";
    }

    private String footer() {
        return "<noscript><input type=\"submit\" value=\"Click to redirect\"></noscript>" +
                "</form>" +
                "</body>" +
                "</html>";
    }

    private String getContentType() {
        return "text/html";
    }


}
