package com.tosak.authos.service

import com.tosak.authos.crypto.getHash
import com.tosak.authos.crypto.getSecureRandomValue
import com.tosak.authos.crypto.hex
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.PPID
import com.tosak.authos.entity.User
import com.tosak.authos.entity.compositeKeys.PPIDKey
import com.tosak.authos.exceptions.base.AuthosException
import com.tosak.authos.exceptions.demand
import com.tosak.authos.exceptions.unauthorized.PPIDNotFoundException
import com.tosak.authos.exceptions.unauthorized.InvalidPpidException
import com.tosak.authos.repository.PPIDRepository
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
    open fun getPPID(user: User, group: AppGroup,create: Boolean = true) : String{
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
 * Retrieves the user ID associated with the provided hash value.
 * Throws an exception if no matching hash is found.
 *
 * @param hash The hash value representing the PPID.
 * @return The user ID corresponding to the provided hash.
 * @throws InvalidPpidException if no PPID with the given hash is found.
 */
//    @Cacheable(value = ["ppidUsers"], key = "#hash")
    open fun getPPIDBySub(hash: String) : PPID {
        val ppid = ppidRepository.findByPpidHash(hash)
            ?: throw AuthosException("invalid subject",InvalidPpidException())
        return ppid
    }
}