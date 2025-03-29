package com.tosak.authos.service

import com.tosak.authos.crypto.b64UrlSafe
import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.crypto.hex
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.PPID
import com.tosak.authos.entity.User
import com.tosak.authos.entity.compositeKeys.PPIDKey
import com.tosak.authos.exceptions.InvalidPPIDHashException
import com.tosak.authos.repository.PPIDRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PPIDService (
    private val ppidRepository: PPIDRepository
)

{

    fun getOrCreatePPID(user: User, group:AppGroup) : String{
        val ppidOpt = ppidRepository.findById(PPIDKey(group.id,user.id))

        if(ppidOpt.isPresent){
            val existingPpid = ppidOpt.get();
            return b64UrlSafe(getHash("${existingPpid.id.groupId}${existingPpid.id.userId}${existingPpid.salt}"))
        }
        val salt = getSecureRandomValue(8);
        val ppidHash = getHash("${group.id}${user.id}${salt}")

        val ppid = PPID(PPIDKey(group.id,user.id),hex(salt), LocalDateTime.now(),hex(ppidHash))
        ppidRepository.save(ppid)

        return b64UrlSafe(ppidHash);


    }
    fun getUserIdByHash(hash: String) : Int {
        val ppid = ppidRepository.findByPpidHash(hash) ?: throw InvalidPPIDHashException("Cant find ppid with matching hash value")
        return ppid.id.userId!!
    }
}