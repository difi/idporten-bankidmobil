package no.idporten.bankid;

import no.bbs.server.exception.BIDException;
import no.bbs.server.implementation.BIDFacade;
import no.bbs.server.vos.InitSessionInfo;
import no.idporten.bankid.config.CacheConfiguration;
import no.idporten.bankid.util.BankIDProperties;
import no.idporten.domain.auth.AuthType;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDMobilAuthorizeControllerTest {

    @Autowired
    private BankIDMobilAuthorizeController logic;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private BIDFacade facade;

    @Autowired
    private BankIDFacadeWrapper facadeWrapper;

    @Autowired
    private BankIDProperties bankIDProperties;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPad; CPU OS 5_1_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B206 Safari/7534.48.3");
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn("mysid");

    }

    @Test
    public void dummy() {
        assertNull(null);
    }




}
