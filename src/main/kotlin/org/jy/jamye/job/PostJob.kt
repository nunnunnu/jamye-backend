package org.jy.jamye.job

import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.common.client.RedisClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PostJob(private val redisClient: RedisClient) {
    val log = LoggerFactory.getLogger(PostJob::class.java)
    @Scheduled(cron = "1 * * * * ?")
    fun postGetLimitCountReset() {
        log.info("스케쥴러 실행 test")
        val value = redisClient.getValue("deleteVote")
        if(!value.isNullOrBlank()) {
            val map = value as Map<Long, DeleteVote>
        }
    }
}