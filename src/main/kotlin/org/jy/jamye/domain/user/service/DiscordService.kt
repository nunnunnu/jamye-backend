package org.jy.jamye.domain.user.service

import org.jy.jamye.domain.post.model.PostType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import javax.naming.AuthenticationException

@Service
class DiscordService {
    @Value("\${discord.clientId}")
    var CLIENT_ID: String? = null
    var API_ENDPOINT = "https://discord.com/api/v10"

    @Value("\${discord.secretKey}")
    var CLIENT_SECRET:String? = null

    @Value("\${discord.redirect}")
    var REDIRECT_URI: String? = null

    @Value("\${discord.bot_token_key}")
    var BOT_TOKEN:String? = null

    val log: Logger = LoggerFactory.getLogger(DiscordService::class.java)

    fun getAccessToken(code: String): String {
        val restTemplate = RestTemplate()
        log.info("[알람함 discord 연동] 1. 엑세스 토큰 발급 - HTTP header 생성")
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            setBasicAuth(CLIENT_ID!!, CLIENT_SECRET!!) // Basic Auth 설정
        }

        log.info("[알람함 discord 연동] 1. 엑세스 토큰 발급 - HTTP body 생성")
        val body: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("redirect_uri", REDIRECT_URI)
        }

        val requestEntity = HttpEntity(body, headers)

        log.info("[알람함 discord 연동] 1. 엑세스 토큰 발급 - HTTP response 조회")
        val response = restTemplate.exchange(
            "$API_ENDPOINT/oauth2/token",
            HttpMethod.POST,
            requestEntity,
            Map::class.java
        )
        return response.body?.get("access_token") as? String ?: throw AuthenticationException("디스코드에 연결할 수 없습니다.")
    }

    fun getUserInfo(accessToken: String): String {
        val restTemplate = RestTemplate()

        log.info("[알람함 discord 연동] 2. 엑세스 토큰 -> 유저정보 조회 - HTTP header 생성")
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }

        val requestEntity = HttpEntity<Void>(headers)
        log.info("[알람함 discord 연동] 2. 엑세스 토큰 -> 유저정보 조회 - HTTP response 조회")
        val response = restTemplate.exchange("https://discord.com/api/users/@me", HttpMethod.GET, requestEntity, Map::class.java)

        return response.body?.get("id") as? String ?: throw AuthenticationException("디스코드에 연결할 수 없습니다.")
    }

    fun sendDmToUser(discordId: String): String {
        log.info("[알람함 discord 연동] 3. jamye bot 채널 생성 - HTTP header 생성")
        val restTemplate = RestTemplate()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bot $BOT_TOKEN")
            contentType = MediaType.APPLICATION_JSON
        }

        log.info("[알람함 discord 연동] 3. jamye bot 채널 생성 - DM 채널 ID 조회")
        val channelRequest = HttpEntity(mapOf("recipient_id" to discordId), headers)
        val channelResponse = restTemplate.postForEntity("https://discord.com/api/v9/users/@me/channels", channelRequest, Map::class.java)

        val channelId = channelResponse.body?.get("id") as? String ?: throw AuthenticationException("디스코드에 연결할 수 없습니다.")

        log.info("[알람함 discord 연동] 3. jamye bot 채널 생성 - 디스코드 첫 메세지 전송")
        sendDiscordDm(channelId, "디스코드 연동이 완료되었습니다!")
        return channelId
    }

    @Value("\${front.url}")
    private val frontUrl: String? = null

    fun sendDiscordDm(
        channelId: String,
        message: String,
        groupSeq: Long? = null,
        postSeq: Long? = null,
        postType: PostType? = null
    ) {
        var msg = message
        val restTemplate = RestTemplate()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bot $BOT_TOKEN")
            contentType = MediaType.APPLICATION_JSON
        }
        if (groupSeq != null) {
            msg += "\n[그룹 링크: $frontUrl/group$groupSeq]"
            if (postSeq != null && postType != null) {
                msg += "\n[잼얘 링크: $frontUrl/jamye/${postType.url}$postSeq]"
            }
        }
        val messageRequest = HttpEntity(mapOf("content" to msg), headers)
        restTemplate.postForEntity(
            "https://discord.com/api/v9/channels/$channelId/messages",
            messageRequest,
            Void::class.java
        )
    }

}