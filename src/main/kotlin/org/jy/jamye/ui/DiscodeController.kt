package org.jy.jamye.ui

import org.jy.jamye.domain.service.DiscordService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/discord")
class DiscordController(
    private val discordService: DiscordService,
) {
    @GetMapping("/oauth/callback")
    fun handleDiscordOAuth(@RequestParam code: String): String {
        val accessToken = discordService.getAccessToken(code) ?: return "토큰 획득 실패"
        val userId = discordService.getUserInfo(accessToken) ?: return "사용자 정보 획득 실패"

        discordService.sendDmToUser(userId)

        return "디스코드 연동 성공! 메시지가 전송되었습니다."
    }
}
