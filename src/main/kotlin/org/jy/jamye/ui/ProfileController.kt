package org.jy.jamye.ui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ProfileController(
    private val env: Environment
) {
    val log: Logger = LoggerFactory.getLogger(ProfileController::class.java)

    @GetMapping("/profile")
    fun profile(): String {
        val profiles: List<String> = env.activeProfiles.toList()
        log.info("[profile] 현재 실행중인 ActiveProfiles: {}", profiles)
        val realProfiles = listOf("real", "real1", "real2")
        val defaultProfile = if(profiles.isEmpty()) {
            "default"
        } else profiles[0]
        log.info("[profile] defaultProfile {}", defaultProfile)
        val filter = profiles.filter { realProfiles.contains(it) }
        val result = if(filter.isEmpty()) defaultProfile else filter[0]
        log.info("[profile] result {}", result)
        return result
    }
}