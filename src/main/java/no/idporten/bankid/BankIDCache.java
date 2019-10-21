package no.idporten.bankid;

import lombok.extern.slf4j.Slf4j;
import no.bbs.server.vos.BIDSessionData;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Slf4j
@Component
public class BankIDCache {

    private final CacheManager cacheManager;

    @Autowired
    public BankIDCache(@Qualifier("ehcacheManager") CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    BankIDMobileStatus getMobileStatus(String sid) {
        Cache<String, BankIDMobileStatus> mobileStatusCache = cacheManager.getCache("mobileStatus", String.class, BankIDMobileStatus.class);
        return mobileStatusCache.get(sid);
    }

    void putMobileStatus(String sid, BankIDMobileStatus mobileStatus) {
        Cache<String, BankIDMobileStatus> mobileStatusCache = cacheManager.getCache("mobileStatus", String.class, BankIDMobileStatus.class);
        mobileStatusCache.put(sid, mobileStatus);
    }

    String getSSN(String sid) {
        Cache<String, String> sidSSNCache = cacheManager.getCache("sidSSN", String.class, String.class);
        return sidSSNCache.get(sid);
    }

    void putSSN(String sid, String ssn) {
        Cache<String, String> sidSSNCache = cacheManager.getCache("sidSSN", String.class, String.class);
        sidSSNCache.put(sid, ssn);
    }

    String getSID(String uuid) {
        Cache<String, String> uuidSidCache = cacheManager.getCache("uuidSid", String.class, String.class);
        return uuidSidCache.get(uuid);
    }

    void removeUuidSID(String uuid) {
        Cache<String, String> uuidSidCache = cacheManager.getCache("uuidSid", String.class, String.class);
        uuidSidCache.remove(uuid);
    }

    void putSID(String uuid, String sid) {
        Cache<String, String> uuidSidCache = cacheManager.getCache("uuidSid", String.class, String.class);
        uuidSidCache.put(uuid, sid);
    }


    BIDSessionData getBIDSessionData(String sid) {
        Cache<String, BIDSessionData> bidSessionCache = cacheManager.getCache("bidSession", String.class, BIDSessionData.class);
        return bidSessionCache.get(sid);
    }

    void putBIDSessionData(String sid, BIDSessionData ssn) {
        Cache<String, BIDSessionData> bidSessionCache = cacheManager.getCache("bidSession", String.class, BIDSessionData.class);
        bidSessionCache.put(sid, ssn);
    }

    void putOCSP(String sid, byte[] ocsp) {
        Cache<String, byte[]> ocspCache = cacheManager.getCache("ocsp", String.class, byte[].class);
        ocspCache.put(sid, ocsp);
    }

    byte[] getOCSP(String sid) {
        Cache<String, byte[]> ocspCache = cacheManager.getCache("ocsp", String.class, byte[].class);
        return ocspCache.get(sid);
    }

    void putTraceId(String sid, String traceId) {
        Cache<String, String> traceIdCache = cacheManager.getCache("traceId", String.class, String.class);
        traceIdCache.put(sid, traceId);
    }

    String getTraceId(String sid) {
        Cache<String, String> traceIdCache = cacheManager.getCache("traceId", String.class, String.class);
        return traceIdCache.get(sid);
    }

    void putEmitter(String sid, SseEmitter emitter) {
        Cache<String, SseEmitter> emitterCache = cacheManager.getCache("emitter", String.class, SseEmitter.class);
        emitterCache.put(sid, emitter);
    }

    SseEmitter getEmitter(String sid) {
        Cache<String, SseEmitter> emitterCache = cacheManager.getCache("emitter", String.class, SseEmitter.class);
        return emitterCache.get(sid);
    }

    void removeEmitter(String sid) {
        log.debug("Remove emitter " + sid);
        Cache<String, SseEmitter> emitterCache = cacheManager.getCache("emitter", String.class, SseEmitter.class);
        emitterCache.remove(sid);
    }

    void removeSession(String sid) {
        Cache<String, String> traceIdCache = cacheManager.getCache("traceId", String.class, String.class);
        traceIdCache.remove(sid);
        Cache<String, String> sidSSNCache = cacheManager.getCache("sidSSN", String.class, String.class);
        sidSSNCache.remove(sid);
        Cache<String, byte[]> ocspCache = cacheManager.getCache("ocsp", String.class, byte[].class);
        ocspCache.remove(sid);
        Cache<String, BIDSessionData> bidSessionCache = cacheManager.getCache("bidSession", String.class, BIDSessionData.class);
        bidSessionCache.remove(sid);
        Cache<String, BankIDMobileStatus> mobileStatusCache = cacheManager.getCache("mobileStatus", String.class, BankIDMobileStatus.class);
        mobileStatusCache.remove(sid);
        Cache<String, SseEmitter> emitterCache = cacheManager.getCache("emitter", String.class, SseEmitter.class);
        emitterCache.remove(sid);
    }

}
