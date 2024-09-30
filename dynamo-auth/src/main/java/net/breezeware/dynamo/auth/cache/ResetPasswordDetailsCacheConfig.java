package net.breezeware.dynamo.auth.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.breezeware.dynamo.auth.dto.ResetPasswordDetails;

/**
 * Improves performance by caching the reset password details and reusing this
 * for subsequent processes.
 */
@Component
public class ResetPasswordDetailsCacheConfig {
    public static final String RESET_PASSWORD_DETAILS_CACHE = "resetPasswordDetailsCache";
    private CacheManager cacheManager;

    public ResetPasswordDetailsCacheConfig(@Value("${cache.heap-size}") int cacheHeapSize) {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        cacheManager.createCache(RESET_PASSWORD_DETAILS_CACHE,
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(String.class, ResetPasswordDetails.class,
                                ResourcePoolsBuilder.heap(cacheHeapSize))
                        .withExpiry(ExpiryPolicyBuilder.noExpiration()));
    }

    public Cache<String, ResetPasswordDetails> getResetPasswordDetailsCache() {
        return cacheManager.getCache(RESET_PASSWORD_DETAILS_CACHE, String.class, ResetPasswordDetails.class);
    }

}
