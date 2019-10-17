package no.idporten.bankid;

import no.idporten.bankid.config.CacheConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache", "idporten.redirecturl=https://test.difi.no/redirect"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDMobilResponseControllerTest {

    private BankIDMobilResponseController controller;

    @Autowired
    private WebApplicationContext springContext;

    @Mock
    HttpServletResponse mockedResponse;

    @Mock
    HttpServletRequest mockedRequest;

    @Mock
    HttpSession mockedSession;

    @Mock
    PrintWriter mockedWriter;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(mockedRequest.getSession()).thenReturn(mockedSession);

        when(mockedSession.getAttribute("redirectUrl")).thenReturn("https://redirect.url");
        when(mockedSession.getAttribute("forceAuth")).thenReturn("forceAuth");
        when(mockedSession.getAttribute("gx_charset")).thenReturn("UTF-8");
        when(mockedSession.getAttribute("locale")).thenReturn("nn");
        when(mockedSession.getAttribute("goto")).thenReturn("goto");
        when(mockedSession.getAttribute("service")).thenReturn("BIDEksternResponse");

        when(mockedResponse.getWriter()).thenReturn(mockedWriter);
        controller = new BankIDMobilResponseController();
    }

    @Test
    public void handleAuthorizationCode() throws Exception {
        String code = "fc897796-58da-4f68-91fb-f62b972fe323";
        String url = "https://test.difi.no/redirect?code=" + code;

        controller.receiveResponse(mockedRequest, code, mockedResponse);

        verify(mockedResponse, times(1)).setContentType("text/html");
        verify(mockedResponse, times(1)).getWriter();
    }

}
