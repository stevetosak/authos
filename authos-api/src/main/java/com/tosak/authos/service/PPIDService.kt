package com.tosak.authos.service

import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.crypto.hex
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.PPID
import com.tosak.authos.entity.User
import com.tosak.authos.entity.compositeKeys.PPIDKey
import com.tosak.authos.exceptions.unauthorized.InvalidPPIDHashException
import com.tosak.authos.repository.PPIDRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class PPIDService (
    private val ppidRepository: PPIDRepository
)

{

    open fun getOrCreatePPID(user: User, group: AppGroup) : String{
        val ppidOpt = ppidRepository.findById(PPIDKey(group.id,user.id))

        if(ppidOpt.isPresent){
            val existingPpid = ppidOpt.get();
            return hex(getHash("${existingPpid.id.groupId}${existingPpid.id.userId}${existingPpid.salt}"))
        }

        val salt = getSecureRandomValue(8);
        val ppidHash = getHash("${group.id}${user.id}${hex(salt)}")

        val ppid = PPID(PPIDKey(group.id,user.id),hex(salt), LocalDateTime.now(),hex(ppidHash))
        ppidRepository.save(ppid)

        println("returned HASH: ${hex(ppidHash)} SAVED HASH : ${ppid.ppidHash}")

        return hex(ppidHash);


    }
//    @Cacheable(value = ["ppidUsers"], key = "#hash")
    open fun getUserIdByHash(hash: String) : Int {
        val ppid = ppidRepository.findByPpidHash(hash) ?: throw InvalidPPIDHashException(
            "Cant find ppid with matching hash value"
        )
        return ppid.id.userId!!
    }
}