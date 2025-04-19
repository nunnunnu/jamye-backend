package org.jy.jamye.common.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jy.jamye.application.group.DeleteVote
import org.jy.jamye.common.exception.SessionExpiredException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisClient(private val redisTemplate: RedisTemplate<String, String>) {
    val log: Logger = LoggerFactory.getLogger(RedisClient::class.java)
    fun getValue(key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }

    fun getAndDelete(key: String): String? {
        return redisTemplate.opsForValue().getAndDelete(key)
    }

    fun setValue(key: String, value: String) {
        redisTemplate.opsForValue().set(key, value)
    }

    fun setValueAndExpireTimeMinutes(key: String, value: String, expireTime: Long) {
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.MINUTES)
    }

    fun deleteKeys(inviteCodes: MutableSet<String>) {
        inviteCodes.forEach { redisTemplate.delete(it) }

    }

    fun getDeleteVoteMap(): MutableMap<Long, DeleteVote> {
        val mapper = ObjectMapper()

        val deleteVoteMap: MutableMap<Long, DeleteVote> = if (redisTemplate.opsForValue().get("deleteVotes").isNullOrBlank()) HashMap()
        else mapper.readValue(redisTemplate.opsForValue().get("deleteVotes"), object : TypeReference<MutableMap<Long, DeleteVote>>() {})

        return deleteVoteMap
    }

    fun getLuckyDrawMap(): MutableMap<String, Int> {
        val mapper = ObjectMapper()

        val luckyDrawMap: MutableMap<String, Int> = if (redisTemplate.opsForValue().get("luckyDraw").isNullOrBlank()) HashMap()
        else mapper.readValue(redisTemplate.opsForValue().get("luckyDraw"), object : TypeReference<MutableMap<String, Int>>() {})

        return luckyDrawMap
    }

    fun setLuckyDrawMap(key: String) {
        val luckyDrawMap = getLuckyDrawMap()

        var count = luckyDrawMap.getOrDefault(key, 0)
        luckyDrawMap[key] = ++count
        val jsonData = ObjectMapper().writeValueAsString(luckyDrawMap)
        setValue("luckyDraw", jsonData)
    }

    fun setValueObject(key: String, deleteVoteMap: MutableMap<Long, DeleteVote>) {
        val mapper = ObjectMapper()
        val jsonString = mapper.writeValueAsString(deleteVoteMap)
        redisTemplate.opsForValue().set("deleteVotes", jsonString)
    }

    fun setValueObjectExpireDay(key: String, value: String, expireTime: Long) {
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.DAYS)
    }

    fun reVoteCheckAndDeleteReVoteInfo(key: String): Boolean{
        val result = redisTemplate.hasKey(key)
        if(result) {
            deleteKeys(mutableSetOf(key))
        }
        return result
    }
    fun reVoteCheck(key: String): Boolean{
        return redisTemplate.hasKey(key)
    }
    fun hasKey(key: String): Boolean{
        return redisTemplate.hasKey(key)
    }

    fun getIdByRefreshToken(refreshToken: String): String {
        log.info("[refreshToken 검증] start")
        if(!hasKey(refreshToken)){
            log.info("[refreshToken 검증] 실패 - refresh token 없음")
            throw SessionExpiredException()
        }
        log.info("[refreshToken 검증] end")
        return getValue(refreshToken)!!
    }

    fun setIdByRefreshToken(refreshToken: String, userId: String) {
        log.info("[refreshToken] redis 저장")
        setValue(refreshToken, userId)
    }

    fun setBlackList(accessToken: String) {
        log.info("[엑세스 토큰 블랙리스트 저장]")
        setValue("BLACKLIST${accessToken}", accessToken)

    }

    fun deleteRefreshToken(refreshToken: String, userId: String) {
        log.info("[refresh token 삭제]")
        val refreshId = getValue(refreshToken)
        if(refreshId != null && refreshId == userId) {
            log.info("[refresh token 삭제] 권한 확인 완료. 삭제 처리")
            redisTemplate.delete(refreshToken)
        }
    }
}