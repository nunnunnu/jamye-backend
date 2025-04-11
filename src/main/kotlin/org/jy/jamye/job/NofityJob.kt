package org.jy.jamye.job

import org.jy.jamye.domain.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NotifyJob(private val userService: UserService) {
    val log: Logger = LoggerFactory.getLogger(NotifyJob::class.java)
    @Scheduled(cron = "0 0 0 * * *") //매일 자정
    fun deleteNotify() {
        log.info("[notifyDeleteJob] 알람함 삭제 실행")
        userService.deleteNotify(LocalDateTime.now().minusMonths(1))
    }
}