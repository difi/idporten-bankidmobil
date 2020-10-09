package no.idporten.bankid;

import no.bbs.server.implementation.BIDFacade;
import no.bbs.server.vos.BIDSessionData;
import no.bbs.server.vos.CertificateStatus;
import no.bbs.server.vos.MobileInfo;
import no.bbs.server.vos.TransactionAndStatus;
import no.idporten.bankid.config.CacheConfiguration;
import no.idporten.bankid.util.BankIDProperties;
import no.idporten.ui.impl.IDPortenInputType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static no.idporten.bankid.BankIDMobilAuthorizeController.IDPORTEN_INPUT_PREFIX;
import static no.idporten.bankid.BankIDMobilAuthorizeController.STATE_USERDATA;
import static no.idporten.bankid.BankIDMobilAuthorizeControllerTest.performAuthorizeGet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDMobileFlowTest {

    private MockMvc mockMvc;

    @MockBean
    private BIDFacade facade;

    @MockBean
    private BankIDFacadeWrapper bankIDFacadeWrapper;

    @MockBean
    private BankIDCache bankIDCache;

    @Autowired
    private WebApplicationContext springContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(springContext).build();
        MockitoAnnotations.initMocks(this);
        when(bankIDFacadeWrapper.getFacade()).thenReturn(facade);
    }

    @Test
    public void testCompleteAutenticationShouldReturnHTMLWithRedirectAction() throws Exception {

        final String redirectUrl = "http://redirect.url";
        // Perform Authorize GET
        final MvcResult mvcResult1 = performAuthorizeGet(mockMvc, redirectUrl);
        MockHttpSession session1 = (MockHttpSession) mvcResult1.getRequest().getSession();
        final String sid = (String) session1.getAttribute("sid");
        String codeWords = "ADJEKTIV SUBSTANTIV";

        // Perform Authorize POST
        final MvcResult mvcResult2 = mockAndPerformAuthorizePost(session1, codeWords, "99999999");
        final String codeWordsPresented = (String) mvcResult2.getRequest().getAttribute("code");
        assertEquals(codeWords, codeWordsPresented);
        MockHttpSession session2 = (MockHttpSession) mvcResult2.getRequest().getSession();

        when(bankIDCache.getTraceId(sid)).thenReturn(sid);
        final BIDSessionData bidSessionData = new BIDSessionData();
        bidSessionData.setCertificateStatus(new CertificateStatus());
        when(bankIDCache.getBIDSessionData(sid)).thenReturn(bidSessionData);
        // BankID has recived our request to start authorize (they will start this request)
        mockMvc.perform(
                post("/bankid")
                        .session(session2)
                        .param("operation", "initAuth")
                        .param("sid", sid)
                        .param("encKey", "encKey")
                        .param("encData", "encData")
                        .param("encAuth", "encAuth"))
                .andDo(print())
                .andExpect(status().isOk());
        // Users confirms on mobile phone...
        // BankID confirms user has finished authorization with bankid
        mockMvc.perform(
                post("/bankid")
                        .session(session2)
                        .param("operation", "verifyAuth")
                        .param("sid", sid)
                        .param("encKey", "encKey")
                        .param("encData", "encData")
                        .param("encAuth", "encAuth"))
                .andDo(print())
                .andExpect(status().isOk());


        when(bankIDCache.getMobileStatus(anyString())).thenReturn(BankIDMobileStatus.FINISHED);

        final MvcResult mvcResult3 = mockMvc.perform(
                post("/bidresponse")
                        .session(session2))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        final String content = mvcResult3.getResponse().getContentAsString();

        assertTrue(content.contains("action=\"" + redirectUrl + "?code=" + sid));
    }


    private MvcResult mockAndPerformAuthorizePost(MockHttpSession session, String codeWords, String phoneNumber) throws Exception {
        final TransactionAndStatus t = new TransactionAndStatus();
        t.setStatusCode("0");
        when(facade.requestMobileAction(any(MobileInfo.class))).thenReturn(t);
        when(bankIDCache.getMobileStatus(anyString())).thenReturn(BankIDMobileStatus.WAIT);
        when(facade.generateMerchantReference("no_NO")).thenReturn(codeWords);
        final MvcResult mvcResult = mockMvc.perform(
                post("/authorize")
                        .sessionAttr(BankIDProperties.HTTP_SESSION_STATE, STATE_USERDATA)
                        .param(IDPORTEN_INPUT_PREFIX + IDPortenInputType.CONTACTINFO_MOBILE, phoneNumber)
                        .param(IDPORTEN_INPUT_PREFIX + IDPortenInputType.BIRTHDATE, "111279")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("bankidmobil_show_reference"))
                .andReturn();
        return mvcResult;
    }

}
