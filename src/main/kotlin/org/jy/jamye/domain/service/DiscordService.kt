package org.jy.jamye.domain.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import javax.naming.AuthenticationException

@Service
class DiscordService {
    private val CLIENT_ID = ""
    private val CLIENT_SECRET = ""
    private val REDIRECT_URI = "http://localhost:8081/oauth/redirect"
    private val API_ENDPOINT = "https://discord.com/api/v10"

    fun getAccessToken(code: String): String {
        val restTemplate = RestTemplate()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            setBasicAuth(CLIENT_ID, CLIENT_SECRET) // Basic Auth 설정
        }

        val body: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("redirect_uri", REDIRECT_URI)
        }

        val requestEntity = HttpEntity(body, headers)

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

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }

        val requestEntity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange("https://discord.com/api/users/@me", HttpMethod.GET, requestEntity, Map::class.java)

        return response.body?.get("id") as? String ?: throw AuthenticationException("디스코드에 연결할 수 없습니다.")
    }

    private val BOT_TOKEN = ""

    fun sendDmToUser(discordId: String): String {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bot $BOT_TOKEN")
            contentType = MediaType.APPLICATION_JSON
        }

        // 1. DM 채널 생성
        val channelRequest = HttpEntity(mapOf("recipient_id" to discordId), headers)
        val channelResponse = restTemplate.postForEntity("https://discord.com/api/v9/users/@me/channels", channelRequest, Map::class.java)

        val channelId = channelResponse.body?.get("id") as? String ?: throw AuthenticationException("디스코드에 연결할 수 없습니다.")

        //디스코드 전송
        sendDiscordDm(channelId, "디스코드 연동이 완료되었습니다!")
        return channelId
    }

    fun sendDiscordDm(
        channelId: String,
        message: String
    ) {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bot $BOT_TOKEN")
            contentType = MediaType.APPLICATION_JSON
        }
        val messageRequest = HttpEntity(mapOf("content" to message), headers)
        restTemplate.postForEntity(
            "https://discord.com/api/v9/channels/$channelId/messages",
            messageRequest,
            Void::class.java
        )

    }

}