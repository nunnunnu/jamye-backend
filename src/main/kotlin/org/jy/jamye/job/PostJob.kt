package org.jy.jamye.job

import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.infra.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PostJob(
    private val redisClient: RedisClient,
    private val tagRepo: TagRepository) {
    val log = LoggerFactory.getLogger(PostJob::class.java)
    @Scheduled(cron = "0 0 0 * * *") //매일 12시
    fun luckyDrawCountReset() {
        log.info("[LuckyDraw 횟수 초기화] 매일 자정 실행")
        redisClient.deleteKeys(mutableSetOf("luckyDraw"))
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun tagTotalCount() {
        log.info("[태그 총 갯수 update] 매일 자정 실행 - 성능 보고 수정 필요")
        tagRepo.postTotalCount()
    }
}