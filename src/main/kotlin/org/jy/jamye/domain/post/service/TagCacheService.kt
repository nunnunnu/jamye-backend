package org.jy.jamye.domain.post.service

import org.jy.jamye.config.CacheConfig
import org.jy.jamye.domain.post.model.Tag
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class TagCacheService(
    cacheManager: CacheManager,
) {
    private val tagCache: Cache = cacheManager.getCache(CacheConfig.CacheType.TAG_CACHE.cacheName)!!

    fun getTagList(key: Any): List<Tag> {
        val tags = tagCache.get(key)?.get() as? List<Tag>
        return tags ?: emptyList()
    }


}