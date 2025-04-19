package org.jy.jamye.job

import org.jy.jamye.config.CacheConfig
import org.jy.jamye.domain.post.model.Tag
import org.jy.jamye.infra.post.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CacheJob(
    private val cacheManager: CacheManager,
    private val tagRepository: TagRepository
) {
    val log = LoggerFactory.getLogger(CacheJob::class.java)!!
    @Scheduled(fixedDelay = 600000)
    fun refreshCache() {
        log.info("[tagCache reload]")
        val cache: Cache = cacheManager.getCache(CacheConfig.CacheType.TAG_CACHE.cacheName)!!
        val groupTagMap:Map<Long, List<Tag>> = tagRepository.findAll().groupBy{ it.groupSeq }
        cache.clear()
        val groupSeq = groupTagMap.keys.toSet()
        groupTagMap.entries.forEach {(groupSeq, tag) ->  cache.put(groupSeq, tag) }
    }
}