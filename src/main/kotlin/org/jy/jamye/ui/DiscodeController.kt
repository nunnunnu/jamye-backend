package org.jy.jamye.ui

import org.jy.jamye.common.io.ResponseDto
import org.jy.jamye.domain.service.DiscordService
import org.jy.jamye.domain.service.UserService
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
    @GetMapping("/oauth/callback")
    fun handleDiscordOAuth(@RequestParam code: String,
                           @AuthenticationPrincipal user: UserDetails
                           ): ResponseDto<Nothing> {
        val accessToken = discordService.getAccessToken(code)
        val discordId = discordService.getUserInfo(accessToken)

        val channelId = discordService.sendDmToUser(discordId)

        userService.discordConnect(user.username, channelId)

        return ResponseDto()
    }

    @Value("\${discord.clientId}")
    private val discordClientId: String? = null

    @GetMapping("/client-id")
    fun getDiscordClientId(): ResponseDto<String> {
        return ResponseDto(data = discordClientId)
    }
}
