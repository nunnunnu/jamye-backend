package org.jy.jamye.common.client

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


}