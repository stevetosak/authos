package com.tosak.authos.service

import com.tosak.authos.entity.AppGroup
import com.tosak.authos.exceptions.AppGroupsNotFoundException
import com.tosak.authos.repository.AppGroupRepository
import org.springframework.stereotype.Service

@Service
class AppGroupService (private val appGroupRepository: AppGroupRepository) {

    fun getAllGroupsForUser(userId: Int) : List<AppGroup> {
        return appGroupRepository.findByUserId(userId) ?: throw AppGroupsNotFoundException("Could not find app groups for given user")
    }
}