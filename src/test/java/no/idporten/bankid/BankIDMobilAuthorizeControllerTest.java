package no.idporten.bankid;

import no.bbs.server.implementation.BIDFacade;
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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDMobilAuthorizeControllerTest {

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
    public void startAuthorizeShouldThenDisplayEnterUserdataPage() throws Exception {
        final String redirectUrl = "http://redirect.url";
        performAuthorizeGet(mockMvc, redirectUrl);
    }

    protected static MvcResult performAuthorizeGet(MockMvc mockMvc, String redirectUrl) throws Exception {
        return mockMvc.perform(
                get("/authorize")
                        .param("redirectUrl", redirectUrl)
                        .param("service", "service")
                        .param("gx_charset", "gx_charset")
                        .param("locale", "nb")
                        .param("start-service", "start-service"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("bankidmobil_enter_userdata"))
                .andReturn();
    }

    @Test
    public void startAuthorizeAndInputMobileInfoShouldThenDisplayShowReferencePage() throws Exception {
        final String redirectUrl = "http://redirect.url";
        //perform GET
        final MvcResult mvcResult1 = performAuthorizeGet(mockMvc, redirectUrl);

        MockHttpSession session1 = (MockHttpSession) mvcResult1.getRequest().getSession();
        String codeWords = "ADJEKTIV SUBSTANTIV";

        //perform POST
        final MvcResult mvcResult2 = mockAndPerformAuthorizePost(session1, codeWords);
        // verify
        final String codeWordsPresented = (String) mvcResult2.getRequest().getAttribute("code");
        assertEquals(codeWords, codeWordsPresented);
    }

    private MvcResult mockAndPerformAuthorizePost(MockHttpSession session, String codeWords) throws Exception {
        final TransactionAndStatus t = new TransactionAndStatus();
        t.setStatusCode("0");
        when(facade.requestMobileAction(any(MobileInfo.class))).thenReturn(t);
        when(bankIDCache.getMobileStatus(anyString())).thenReturn(BankIDMobileStatus.WAIT);
        when(facade.generateMerchantReference("no_NO")).thenReturn(codeWords);
        final MvcResult mvcResult = mockMvc.perform(
                post("/authorize")
                        .sessionAttr(BankIDProperties.HTTP_SESSION_STATE, STATE_USERDATA)
                        .param(IDPORTEN_INPUT_PREFIX + IDPortenInputType.CONTACTINFO_MOBILE, "99999999")
                        .param(IDPORTEN_INPUT_PREFIX + IDPortenInputType.BIRTHDATE, "111279")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("bankidmobil_show_reference"))
                .andReturn();
        return mvcResult;
    }

}
