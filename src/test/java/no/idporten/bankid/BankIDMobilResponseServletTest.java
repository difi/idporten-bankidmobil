package no.idporten.bankid;

import no.idporten.bankid.config.CacheConfiguration;
import no.idporten.bankid.util.BankIDProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDMobilResponseServletTest {

    private BankIDMobilResponseServlet rs;

    @Mock
    private HttpServletRequest mockedRequest;

    @Mock
    private HttpServletResponse mockedResponse;

    @Mock
    PrintWriter mockedWriter;

    @Mock
    private HttpSession mockedSession;

    @Autowired
    BankIDProperties bankIDProperties;

    @Autowired
    BankIDCache bankIDCache;


    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(mockedRequest.getSession()).thenReturn(mockedSession);
        when(mockedSession.getAttribute("redirectUrl")).thenReturn("https://redirect.url");
        when(mockedSession.getAttribute("forceAuth")).thenReturn("true");
        when(mockedSession.getAttribute("gx_charset")).thenReturn("UTF-8");
        when(mockedSession.getAttribute("locale")).thenReturn("en");
        when(mockedSession.getAttribute("goto")).thenReturn("home");
        when(mockedSession.getAttribute("sid")).thenReturn("mysid");
        when(mockedSession.getAttribute("service")).thenReturn("BIDEksternResponse");

        when(mockedResponse.getWriter()).thenReturn(mockedWriter);
        rs = new BankIDMobilResponseServlet(bankIDProperties, bankIDCache);

    }

    @Test
    public void testServiceHttpServletRequestHttpServletResponseAuthenticated() throws IOException {
        bankIDCache.putSSN("mysid", "19096948045");
        rs.service(mockedRequest, null, mockedResponse);

        ArgumentCaptor<StringBuilder> result = ArgumentCaptor.forClass(StringBuilder.class);
        verify(mockedResponse, times(1)).setContentType("text/html");
        verify(mockedResponse, times(1)).getWriter();
        verify(mockedWriter, times(1)).append(result.capture());

        Assert.assertTrue(result.getValue().toString().contains("service=BankIDMobilEkstern"));

    }

    @Test
    public void testServiceHttpServletRequestHttpServletResponseInvalidSSN() throws IOException {
        bankIDCache.putSSN("mysid", "");
        rs.service(mockedRequest, null, mockedResponse);

        ArgumentCaptor<StringBuilder> result = ArgumentCaptor.forClass(StringBuilder.class);
        verify(mockedResponse, times(1)).setContentType("text/html");
        verify(mockedResponse, times(1)).getWriter();
        verify(mockedWriter, times(1)).append(result.capture());

        Assert.assertTrue(result.getValue().toString().contains("service=null"));

    }

    @Test
    public void testServiceHttpServletRequestHttpServletResponseError() throws IOException {
        when(mockedRequest.getParameter("idpError")).thenReturn("feil");
        when(mockedSession.getAttribute("start-service")).thenReturn("startService");
        rs.service(mockedRequest, null, mockedResponse);

        ArgumentCaptor<StringBuilder> result = ArgumentCaptor.forClass(StringBuilder.class);
        verify(mockedResponse, times(1)).setContentType("text/html");
        verify(mockedResponse, times(1)).getWriter();
        verify(mockedWriter, times(1)).append(result.capture());

        Assert.assertTrue(result.getValue().toString().contains("service=startService"));
    }

}
