package org.jy.jamye.job

import org.jy.jamye.common.client.RedisClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PostJob(private val redisClient: RedisClient) {
    @Scheduled(cron = "0 0 12 * * *") //매일 12시
    fun luckyDrawCountReset() {
        redisClient.deleteKeys(mutableSetOf("luckyDraw"))
    }
}