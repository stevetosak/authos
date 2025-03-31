package com.tosak.authos.service

import com.tosak.authos.exceptions.RedisKeyNotFoundException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService (private val redisTemplate: RedisTemplate<String,String>){

    fun tryGetValue(key: String) : String{
       val result =  redisTemplate.opsForValue().get(key)
        if(result != null){
            return result
        } else throw RedisKeyNotFoundException("Key does not exist. It probably expired")

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
}