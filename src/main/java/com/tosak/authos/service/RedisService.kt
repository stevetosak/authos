//package com.tosak.authos.service
//
//import com.tosak.authos.exceptions.RedisKeyExpiredException
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.stereotype.Service
//
//@Service
//class RedisService (private val redisTemplate: RedisTemplate<String,Any>){
//    fun cacheWithTTL(key:String, value:String,expireSeconds:Long){
//        redisTemplate.opsForValue().set(key,value,expireSeconds)
//    }
//    fun <T>getIfNotExpired(key:String) : T{
//       val result: T? =  redisTemplate.opsForValue().get(key) as? T
//        if(result != null){
//            return result
//        } else throw RedisKeyExpiredException("Key expired")
//
//    }
//    fun delete(key:String){
//        redisTemplate.delete(key)
//    }
//}