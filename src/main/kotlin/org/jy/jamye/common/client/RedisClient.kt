package org.jy.jamye.common.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.common.exception.SessionExpiredException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisClient(private val redisTemplate: RedisTemplate<String, String>) {
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
        if(!hasKey(refreshToken)){
            throw SessionExpiredException()
        }
        return getValue(refreshToken)!!
    }

    fun setIdByRefreshToken(refreshToken: String, userId: String) {
        setValue(refreshToken, userId)
    }

    fun setBlackList(accessToken: String) {
        setValue("BLACKLIST${accessToken}", accessToken)

    }

    fun deleteRefreshToken(refreshToken: String, userId: String) {
        val refreshId = getValue(refreshToken)
        if(refreshId != null && refreshId == userId) {
            redisTemplate.delete(refreshToken)
        }
    }
}