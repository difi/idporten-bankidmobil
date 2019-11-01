package no.idporten.bankid;

import no.bbs.server.implementation.BIDFacade;
import no.bbs.server.vos.BIDSessionData;
import no.bbs.server.vos.CertificateStatus;
import no.idporten.bankid.config.CacheConfiguration;
import no.idporten.bankid.util.BankIDProperties;
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
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDMobilServletTest {


    @Mock
    private HttpServletRequest mockedRequest;

    @Mock
    private HttpServletResponse mockedResponse;

    @Mock
    private HttpSession mockedSession;

    @Mock
    private PrintWriter mockedPrintWriter;

    @Mock
    private BIDFacade facade;

    @Autowired
    private BankIDProperties bankIDProperties;

    @Autowired
    private BankIDFacadeWrapper bankIDFacadeWrapper;

    private BankIDMobilServlet bs;

    @Autowired
    private BankIDCache bankIDCache;

    @Before
    public void setUp() throws Exception {
        //BankIDProperties.CONFIG.setRuntimeProperty(BankIDProperties.isBankIdReset, "true");
        MockitoAnnotations.initMocks(this);
        when(mockedRequest.getSession()).thenReturn(mockedSession);
        when(mockedResponse.getWriter()).thenReturn(mockedPrintWriter);
        when(mockedSession.getId()).thenReturn("c3e15351db476d89d8b473279217");
        bankIDFacadeWrapper.setFacade(facade);
        bs = new BankIDMobilServlet(bankIDProperties, bankIDFacadeWrapper, bankIDCache);
    }

    @Test
    public void testServiceServletRequestServletResponseNullRequest() throws IOException {
        bs.service(mockedRequest, null, null, null, null, null, mockedResponse);
        verify(mockedPrintWriter, times(1)).flush();
    }

    @Test
    public void testServiceServletRequestServletResponseInitAuthRequest() throws IOException {
        String encKey = "A+Me5563j3NjM3CnxT1UwAYX3TwOoyziXmLzrKauG4yhFMIVDhXVkzsrwtj2NYQNFDKzWvrUJSOcDUmk/tCHJetE+xwuX6z/blL8Gb7hBatzULjXdmJ2+ApjQ3K8rRMrgTWhPNwvx/kRII4Ju06AdRcxbsXrJDypOJFrqHEMddQdtCkXQPM7HibAMHBa+Zbu+cuEbXqqxue6OPjuzZEds/o85xi0LY0Dg0Jrb0Ed9fVNO6Gaf0hLxtLS/LSj2PuAjrvtiOMmvifyR/grn4OWZeAmM1mdrJSkcCiQnz4PaDeInEiTf7K6nfKZJwm1llrYVtCR/OXEAZk85zs5ld3n5w==";
        String encAuth = "40bnSzKDoU59Mb1S/ti6ZkChIp8=";
        String encData = "DiRa10wwrYvW2pkIk7ht0qy8VdAtvUiuo23jVpkoRNo1RyLPreHEXghDYwghRpW4nbR0sFaodt0ds0vjsPiF/5dQqvJhcseiYi1SIHBdLBtPyZj94jUMQxKp5yDOCuD56QV+sorJed89hv8Cg3Ipz/8fP0FVHttJ34mdd+Lz5ZcDk/fNcmT8MnYRjVY6MKS2TiNIKhAyfuV1kO2rgzioUQ==";
        String operation = "initAuth";
        String sid = "c3e15351db476d89d8b473279217";
        bs.service(mockedRequest, operation, sid, encKey, encData, encAuth, mockedResponse);
        assertNotNull(bankIDCache.getBIDSessionData("c3e15351db476d89d8b473279217"));
        verify(mockedPrintWriter, times(1))
                .println((String) isNull());
        verify(mockedPrintWriter, times(1))
                .flush();
    }

    @Test
    public void testServiceServletRequestServletResponseVerifyAuthRequest() throws IOException {

        String encKey = "Z0PM1Hkk11Lwz6DVlHBoL53dSbqgzqyGgZgtFZA1xPHSFhOb64PUSKQTGN7xaz1g/90mh9PYACujy3eOx+KxiSRW9ijipy1V7hUja94oohkwrKW12C0wh+4vwzlOkpxRu7EJ1Fht5JHsdvvGoC3GVJ+6mAT/LOyos9LlpJtsR+v89e62TjS/i2Q3IeiLVCWzqrPGVnFK54WP+rskCt2wTpcdy3Qz59ChKSdvYW9cjt19/MuizruJZUtM1g/R2epl2233PX10gyWjoDuAkq2jnencCqT0RSo8CY9alhH/OVsgXV9q4+ase4uPmI/oNICNUdb3K3zHMxcQejKx2M8Hlw==";
        String encAuth = "1VErw/cKkweADgZ69Ux6KkIvCdY=";
        String encData = "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";
        String operation = "verifyAuth";
        String sid = "c3e15351db476d89d8b473279217";
        bankIDCache.putBIDSessionData(sid, createBIDSessionData());
        bs.service(mockedRequest, operation, sid, encKey, encData, encAuth, mockedResponse);

        verify(mockedPrintWriter, times(1))
                .println((String) isNull());
        verify(mockedPrintWriter, times(1))
                .flush();
    }

    private BIDSessionData createBIDSessionData() {
        BIDSessionData sessionData = new BIDSessionData();
        CertificateStatus certificateStatus = new CertificateStatus();
        certificateStatus.setOcspResponse("ocsp".getBytes());
        certificateStatus.setAddInfoSSN("13094645456");
        sessionData.setCertificateStatus(certificateStatus);
        return sessionData;
    }


}
