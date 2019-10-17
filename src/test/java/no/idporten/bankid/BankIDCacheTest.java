package no.idporten.bankid;

import no.bbs.server.vos.BIDSessionData;
import no.idporten.bankid.config.CacheConfiguration;
import org.ehcache.CacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {BankIDMobilApplication.class, CacheConfiguration.class})
public class BankIDCacheTest {

    @Autowired
    private CacheManager cacheManager;


    @Test
    public void getSidSSN() {
        BankIDCache bankIDCache = new BankIDCache(cacheManager);
        bankIDCache.putSSN("123", "cacheHit");
        String cacheHit = bankIDCache.getSSN("123");
        assertEquals("cacheHit", cacheHit);
    }

    @Test
    public void getBidSession() {
        BankIDCache bankIDCache = new BankIDCache(cacheManager);
        BIDSessionData bidSessionData = new BIDSessionData("testTraceId");
        bankIDCache.putBIDSessionData("123", bidSessionData);
        BIDSessionData cacheHit = bankIDCache.getBIDSessionData("123");
        assertEquals(bidSessionData, cacheHit);
    }

    @Test
    public void getOscp() {
        BankIDCache bankIDCache = new BankIDCache(cacheManager);
        byte[] testOscp = "testOscp".getBytes();
        bankIDCache.putOCSP("123", testOscp);
        byte[] cacheHit = bankIDCache.getOCSP("123");
        assertEquals(testOscp, cacheHit);
    }

    @Test
    public void getTraceId() {
        BankIDCache bankIDCache = new BankIDCache(cacheManager);
        bankIDCache.putTraceId("123", "cacheHit");
        String cacheHit = bankIDCache.getTraceId("123");
        assertEquals("cacheHit", cacheHit);
    }

    @Test
    public void getMobileStatus() {
        BankIDCache bankIDCache = new BankIDCache(cacheManager);
        bankIDCache.putMobileStatus("123", BankIDMobileStatus.ERROR);
        BankIDMobileStatus cacheHit = bankIDCache.getMobileStatus("123");
        assertEquals(BankIDMobileStatus.ERROR, cacheHit);
    }

    @Test
    public void testRemoveSession() {
        BankIDCache bankIDCache = new BankIDCache(cacheManager);
        String sid = "123";
        String cacheHit = "cacheHit";
        BIDSessionData bidSessionData = new BIDSessionData("traceId");
        byte[] ocsp = "ocsp".getBytes();
        String ssn = "13094812345";

        bankIDCache.putSSN(sid, ssn);
        bankIDCache.putTraceId(sid, cacheHit);
        bankIDCache.putBIDSessionData(sid, bidSessionData);
        bankIDCache.putOCSP(sid, ocsp);

        assertEquals(ssn, bankIDCache.getSSN(sid));
        assertEquals(cacheHit, bankIDCache.getTraceId(sid));
        assertEquals(bidSessionData, bankIDCache.getBIDSessionData(sid));
        assertEquals(ocsp, bankIDCache.getOCSP(sid));

        bankIDCache.removeSession(sid);

        assertNull(bankIDCache.getSSN(sid));
        assertNull(bankIDCache.getTraceId(sid));
        assertNull(bankIDCache.getBIDSessionData(sid));
        assertNull(bankIDCache.getOCSP(sid));
    }
}