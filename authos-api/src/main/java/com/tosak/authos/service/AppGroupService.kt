package com.tosak.authos.service

import com.tosak.authos.dto.AppGroupDTO
import com.tosak.authos.dto.CreateAppGroupDTO
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.base.AuthosException
import com.tosak.authos.exceptions.unauthorized.AppGroupsNotFoundException
import com.tosak.authos.repository.AppGroupRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

// todo mozda mapa da vrakjam namesto vo appgroup dto da sa apps
@Service
open class AppGroupService(
    private val appGroupRepository: AppGroupRepository,
    private val ssoSessionService: SSOSessionService
) {

    //    @Cacheable(value = ["userGroups"], key = "#userId")
    open fun getAllGroupsForUser(userId: Int): List<AppGroup> {
        return appGroupRepository.findByUserId(userId)
            ?: throw AuthosException("Bad Request", AppGroupsNotFoundException())
    }

    @Transactional
    open fun createAppGroup(dto: CreateAppGroupDTO, user: User): AppGroupDTO {
        val group = AppGroup(
            name = dto.name, createdAt = LocalDateTime.now(), user = user, isDefault = dto.isDefault,
            mfaPolicy = dto.mfaPolicy, ssoPolicy = dto.ssoPolicy
        )
        if (group.isDefault) {
            val defaultGroup = getDefaultGroupForUser(user);
            defaultGroup.isDefault = false;
            appGroupRepository.save(defaultGroup);
        }
        val savedGr = appGroupRepository.save(group)
        return AppGroupDTO(
            savedGr.id, savedGr.name,
            savedGr.isDefault, savedGr.createdAt,
            savedGr.ssoPolicy, savedGr.mfaPolicy
        )
    }

    open fun findGroupByIdAndUser(id: Int, user: User): AppGroup {
        return appGroupRepository.findByIdAndUserId(id, user.id!!)
            ?: throw AuthosException("Bad Request", AppGroupsNotFoundException())
    }

    open fun getDefaultGroupForUser(user: User): AppGroup {
        return appGroupRepository.findAppGroupByUserIdAndIsDefault(user.id!!, true) ?: throw
        IllegalStateException("No default group present for user")
    }

    open fun deleteGroup(appGroup: AppGroup,move: Boolean = false) {
        if (appGroup.isDefault) {
            val groups = appGroupRepository.findByUserId(appGroup.user.id!!)?.filter { !it.isDefault }
            if(groups == null || groups.isEmpty()) throw IllegalStateException("At least one app group is required")
            val newDefaultGr = groups[0];
            newDefaultGr.isDefault = true;
            appGroupRepository.save(newDefaultGr);
        }
        appGroupRepository.delete(appGroup)
        ssoSessionService.terminateAllByGroup(appGroup)
    }

    @Transactional
    open fun updateGroup(appGroupDto: AppGroupDTO,user: User): AppGroup {
        val group = appGroupRepository.findGroupById(appGroupDto.id!!) ?: throw AuthosException("Bad Request", AppGroupsNotFoundException())
        if (appGroupDto.isDefault) {
            val defaultGroup = getDefaultGroupForUser(user);
            defaultGroup.isDefault = false;
            appGroupRepository.save(defaultGroup);
        }
        group.isDefault = appGroupDto.isDefault
        group.name = appGroupDto.name
        group.mfaPolicy = appGroupDto.mfaPolicy
        group.ssoPolicy = appGroupDto.ssoPolicy
        return appGroupRepository.save(group)
    }
}