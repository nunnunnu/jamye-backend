package org.jy.jamye.common.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jy.jamye.application.dto.DeleteVote
import org.jy.jamye.application.dto.NotifyDto
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.BadCredentialsException
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
            throw BadCredentialsException("다시 로그인해주세요")
        }
        return getValue(refreshToken)!!
    }

    fun setIdByRefreshToken(refreshToken: String, userId: String) {
        setValue(refreshToken, userId)
    }

    fun getNotifyList(userSeq: Long): MutableSet<NotifyDto> {
        val mapper = ObjectMapper()

        val notifyList: MutableSet<NotifyDto> = if (redisTemplate.opsForValue().get(userSeq).isNullOrBlank()) mutableSetOf()
        else mapper.readValue(redisTemplate.opsForValue().get(userSeq), object : TypeReference<MutableSet<NotifyDto>>() {})

        return notifyList
    }

    fun notifyBox(userSeq: Long, message: String, groupSeq: Long, postSeq: Long) {
        val notifyList = getNotifyList(userSeq)
        val notifyDto = NotifyDto(message = message, groupSeq = groupSeq, postSeq = postSeq)
        notifyList.add(notifyDto)
        val mapper = ObjectMapper()
        println(notifyDto)
        val jsonString = mapper.writeValueAsString(notifyDto)
        println(jsonString)
        setValue(userSeq.toString(), jsonString)
    }
}