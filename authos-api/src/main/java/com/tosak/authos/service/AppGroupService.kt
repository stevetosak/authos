package com.tosak.authos.service

import com.tosak.authos.dto.AppDTO
import com.tosak.authos.dto.AppGroupDTO
import com.tosak.authos.dto.CreateAppGroupDTO
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.AppGroupsNotFoundException
import com.tosak.authos.repository.AppGroupRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

// todo mozda mapa da vrakjam namesto vo appgroup dto da sa apps
@Service
open class AppGroupService (private val appGroupRepository: AppGroupRepository) {

//    @Cacheable(value = ["userGroups"], key = "#userId")
    open fun getAllGroupsForUser(userId: Int) : List<AppGroup> {
        return appGroupRepository.findByUserId(userId) ?: throw AppGroupsNotFoundException("Could not find app groups for given user")
    }
    @Transactional
    open fun createAppGroup(dto: CreateAppGroupDTO, user: User): AppGroupDTO {
        val group = AppGroup(name = dto.name, createdAt = LocalDateTime.now(), user = user, isDefault = dto.isDefault,
            mfaPolicy = dto.mfaPolicy, ssoPolicy = dto.ssoPolicy)
        if(group.isDefault){
           val defaultGroup = appGroupRepository.findAppGroupByIsDefault(true) ?: throw AppGroupsNotFoundException("Could not find app group")
            defaultGroup.isDefault = false;
            appGroupRepository.save(defaultGroup);
        }
        val savedGr = appGroupRepository.save(group)
        return AppGroupDTO(savedGr.id,savedGr.name,
            savedGr.isDefault,savedGr.createdAt,
            savedGr.ssoPolicy,savedGr.mfaPolicy
        )
    }
}