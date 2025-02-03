package org.jy.jamye.job

import org.jy.jamye.domain.service.UserService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NotifyJob(private val userService: UserService) {
    @Scheduled(cron = "0 0 12 * * *") //매일 12시
    fun deleteNotify() {
        userService.deleteNotify(LocalDateTime.now().minusMonths(1))
    }
}