package no.idporten.bankid;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.idporten.bankid.config.CacheConfiguration;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDJSTokenControllerTest {

    private MockMvc mockMvc;

    @Autowired
    BankIDCache bankIDCache;

    @Autowired
    private WebApplicationContext springContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(springContext).build();
    }

    @Test
    public void handleAuthorizationCodeGrant() throws Exception {
        String code = "fc897796-58da-4f68-91fb-f62b972fe323";
        String sid = "ASDF24513";
        String ssn = "23079422487";
        byte[] ocsp = "ocsp osv greier skikkelig lang".getBytes();
        bankIDCache.putSID(code, sid);
        bankIDCache.putSSN(sid, ssn);
        bankIDCache.putOCSP(sid, ocsp);
        mockMvc.perform(post("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("code", code))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.ssn").value(containsString(ssn)))
                .andExpect(jsonPath("$.ocsp").value(containsString(Base64.encodeBase64String(ocsp))));
        assertNull(bankIDCache.getSSN(sid));
        assertNull(bankIDCache.getSID(code));

    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}