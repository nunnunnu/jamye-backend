package org.jy.jamye.common.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jy.jamye.application.dto.DeleteVote
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

    fun getLuckyDrawMap(): MutableMap<Long, Int> {
        val mapper = ObjectMapper()

        val luckyDrawMap: MutableMap<Long, Int> = if (redisTemplate.opsForValue().get("luckyDraw").isNullOrBlank()) HashMap()
        else mapper.readValue(redisTemplate.opsForValue().get("luckyDraw"), object : TypeReference<MutableMap<Long, Int>>() {})

        return luckyDrawMap
    }

    fun setLuckyDrawMap(userSeq: Long) {
        val luckyDrawMap = getLuckyDrawMap()

        var count = luckyDrawMap.getOrDefault(userSeq, 0)
        luckyDrawMap[userSeq] = ++count
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

    fun reVoteCheck(key: String): Boolean{
        val result = redisTemplate.hasKey(key)
        if(result) {
            deleteKeys(mutableSetOf(key))
        }
        return result
    }

    fun hasKey(key: String): Boolean{
        return redisTemplate.hasKey(key)
    }
}