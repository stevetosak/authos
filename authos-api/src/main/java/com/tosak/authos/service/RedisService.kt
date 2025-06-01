package com.tosak.authos.service

import com.tosak.authos.exceptions.RedisKeyNotFoundException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.session.data.redis.RedisSessionExpirationStore
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

@Service
class RedisService (private val redisTemplate: RedisTemplate<String,String>){

    @Throws(RedisKeyNotFoundException::class)
    fun tryGetValue(key: String) : String? {
       val result = redisTemplate.opsForValue().get(key)
        return result;
    }
   fun hasKey(key: String) : Boolean {
       return true == redisTemplate.hasKey(key)
   }
    fun setWithTTL(key:String, value:String,expireSeconds:Long){
        redisTemplate.opsForValue().set(key,value,expireSeconds,TimeUnit.SECONDS)
    }
    fun set(key:String, value:String){
        redisTemplate.opsForValue().set(key,value)
    }
    fun delete(key:String){
        redisTemplate.delete(key)
    }
    fun forceDeleteSession(sessionId: String) {
        val keys = listOf(
            "spring:session:sessions:$sessionId",
            "spring:session:sessions:expires:$sessionId",
            "spring:session:index:*:$sessionId"
        )
        keys.forEach { delete(it) }
    }
    fun clearDb() : Int{
        val keys = redisTemplate.keys("*")
        keys.forEach{ key -> redisTemplate.delete(key)}
        val afterDel = redisTemplate.keys("*")
        assert (afterDel.isEmpty())
        return afterDel.count()
    }
}