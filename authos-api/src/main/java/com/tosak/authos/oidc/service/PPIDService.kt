package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.common.utils.getHash
import com.tosak.authos.oidc.common.utils.getSecureRandomValue
import com.tosak.authos.oidc.common.utils.hex
import com.tosak.authos.oidc.entity.AppGroup
import com.tosak.authos.oidc.entity.PPID
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.entity.compositeKeys.PPIDKey
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.exceptions.unauthorized.PPIDNotFoundException
import com.tosak.authos.oidc.exceptions.unauthorized.InvalidPpidException
import com.tosak.authos.oidc.repository.PPIDRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service class responsible for managing Pairwise Pseudonymous Identifiers (PPIDs).
 * Facilitates the creation of new PPIDs or retrieval of existing ones and provides user identification based on a given PPID hash.
 */
@Service
open class PPIDService (
    private val ppidRepository: PPIDRepository
)

{

    /**
     * Retrieves an existing PPID (Pairwise Pseudonymous Identifier) for the specified user and group
     * or creates and stores a new one if no PPID exists. The PPID is generated using a secure hashing method
     * and saved in the repository for future use.
     *
     * @param user The user for whom the PPID is being retrieved or created.
     * @param group The application group associated with the user and the PPID.
     * @return The generated or retrieved PPID as a hex-encoded string.
     */
    open fun getPPIDSub(user: User, group: AppGroup, create: Boolean = true) : String{
        val ppidOpt = ppidRepository.findById(PPIDKey(group.id,user.id))
        if(ppidOpt.isPresent){
            val existingPpid = ppidOpt.get();
            return hex(getHash("${existingPpid.key.groupId}${existingPpid.key.userId}${existingPpid.salt}"))
        }
        demand(create){ AuthosException("invalid user",PPIDNotFoundException()) }
        return createPPID(user,group);

    }

    open fun createPPID(user: User, group: AppGroup) : String{
        val salt = getSecureRandomValue(8);
        val ppidHash = getHash("${group.id}${user.id}${hex(salt)}")

        val ppid = PPID(PPIDKey(group.id,user.id),hex(salt), LocalDateTime.now(),hex(ppidHash))
        ppidRepository.save(ppid)


        return hex(ppidHash);

    }
/**
 * Finds and returns the PPID subject string given a
 * Throws an exception if no matching hash is found.
 *
 * @param sub The hash value representing the subject's PPID.
 * @return PPID object
 * @throws InvalidPpidException if no PPID with the given hash is found.
 */
//    @Cacheable(value = ["ppidUsers"], key = "#hash")
    open fun getPPIDBySub(sub: String) : PPID {
        val ppid = ppidRepository.findByPpidHash(sub)
            ?: throw AuthosException("invalid subject",InvalidPpidException())
        return ppid
    }
}