package no.idporten.bankid.config;

import no.bbs.server.vos.BIDSessionData;
import no.idporten.bankid.BankIDMobileStatus;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Configuration
@ConditionalOnExpression("'${spring.cache.type}'!='none'")
public class CacheConfiguration {
    @Value("${cache.ttl-in-ms:5000}")
    private int ttl;

    @Bean(name = "ehcacheManager")
    public CacheManager cacheManager() {
        return CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("sidSSN",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .withCache("bidSession",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BIDSessionData.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .withCache("uuidSid",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .withCache("ocsp",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .withCache("traceId",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .withCache("mobileStatus",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BankIDMobileStatus.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .withCache("emitter",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, SseEmitter.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())
                .build(true);
    }

}
