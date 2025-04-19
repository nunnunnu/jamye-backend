package org.jy.jamye.ui.user

import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.user.service.DiscordService
import org.jy.jamye.domain.user.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/discord")
class DiscordController(
    private val discordService: DiscordService,
    private val userService: UserService
) {
    val log: Logger = LoggerFactory.getLogger(DiscordController::class.java)
    @GetMapping("/oauth/callback")
    fun handleDiscordOAuth(@RequestParam code: String,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseDto<Nothing> {
        log.info("[알람함 discord 연동] start")
        val accessToken = discordService.getAccessToken(code)
        val discordId = discordService.getUserInfo(accessToken)

        val channelId = discordService.sendDmToUser(discordId)

        userService.discordConnect(user.username, channelId)
        log.info("[알람함 discord 연동] end")
        return ResponseDto()
    }

    @Value("\${discord.clientId}")
    private val discordClientId: String? = null

    @GetMapping("/client-id")
    fun getDiscordClientId(): ResponseDto<String> {
        log.info("[discord client-id 조회]")
        return ResponseDto(data = discordClientId)
    }
}
