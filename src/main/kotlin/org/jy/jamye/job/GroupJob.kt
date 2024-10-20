package org.jy.jamye.job

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.common.client.RedisClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class GroupJob(private val redisClient: RedisClient) {
    val log = LoggerFactory.getLogger(GroupJob::class.java)
    @Scheduled(cron = "1 * * * * ?")
    fun groupDeleteVote() {
        log.info("---그룹 삭제 집계 시작---")
        val value = redisClient.getValue("deleteVotes")
        if(!value.isNullOrBlank()) {
            val deleteVoteMap = ObjectMapper().readValue(redisClient.getValue("deleteVotes"), object : TypeReference<MutableMap<Long, DeleteVote>>() {})
            deleteVoteMap.entries.forEach{ (key, value)->
                log.info("---{}번 그룹 집계 시작", key)
                val startDateAsLocalDateTime = value.startDateAsLocalDateTime()
                if(startDateAsLocalDateTime.plusDays(7).isBefore(LocalDateTime.now())) {
                    if(value.agreeUserSeqs.size > (value.standardVoteCount/2)) {
                        log.info("---{}번 그룹 삭제 유저 과반수 동의---")
                        log.info("---삭제 start---")
                        TODO("그룹/ 게시물 삭제 로직 구현")
                        log.info("---삭제 end---")
                    }
                }
            }

        }
        log.info("---그룹 삭제 집계 종료---")
    }
}