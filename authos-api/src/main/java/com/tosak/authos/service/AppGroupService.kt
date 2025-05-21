package com.tosak.authos.service

import com.tosak.authos.dto.AppGroupDTO
import com.tosak.authos.entity.App
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.exceptions.AppGroupsNotFoundException
import com.tosak.authos.repository.AppGroupRepository
import org.springframework.stereotype.Service

@Service
class AppGroupService (private val appGroupRepository: AppGroupRepository) {

    fun getAllGroupsForUser(userId: Int) : List<AppGroup> {
        return appGroupRepository.findByUserId(userId) ?: throw AppGroupsNotFoundException("Could not find app groups for given user")
    }
    fun groupApps(apps: List<App>) : List<AppGroupDTO>{
        val groupMap: MutableMap<Int,AppGroupDTO> = HashMap()

        apps.forEach{app ->
            if(!groupMap.containsKey(app.group.id!!)){
                groupMap[app.group.id!!] = AppGroupDTO(app.group.id,app.group.name, mutableListOf(),app.group.createdAt)
            }
            groupMap[app.group.id]?.apps?.add(app.toDTO())
        }

        return groupMap.values.toList()
    }
}