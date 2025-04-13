package org.jy.jamye.config

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        val caches = CacheType.entries.map { cacheType ->
            CaffeineCache(
                cacheType.cacheName,
                buildCache(cacheType.expiredTimeAsSec, cacheType.maximumSize)
            )
        }
        cacheManager.setCaches(caches)
        return cacheManager
    }

    fun buildCache(expiredTimeAsSec: Long, maximumSize: Long): Cache<Any, Any> {
        return Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(expiredTimeAsSec, TimeUnit.SECONDS)
            .maximumSize(maximumSize)
            .build();
  }

  @AllArgsConstructor
  @Getter
  enum class CacheType(var cacheName: String,
                        var expiredTimeAsSec: Long,
                        var maximumSize: Long) {

    USER_CACHE("userCache", 60 * 60 * 24, 1000),
    GROUP_CACHE("groupCache", 60 * 60 * 24, 1000),
    GROUP_EXIST_CACHE("groupExistCache", 60 * 60 * 24 * 7, 1000),
    ;

  }
}
