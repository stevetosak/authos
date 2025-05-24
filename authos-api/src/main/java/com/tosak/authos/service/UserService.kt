package com.tosak.authos.service

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.entity.AppGroup
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.AuthenticationNotPresentException
import com.tosak.authos.exceptions.unauthorized.UserNotFoundException
import com.tosak.authos.repository.AppGroupRepository
import com.tosak.authos.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.InvalidParameterException
import java.util.Optional

@Service
open class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val appGroupRepository: AppGroupRepository,
    private val ppidService: PPIDService
) {

    open fun verifyCredentials(email: String, password: String): User {
        val userOpt: Optional<User> = userRepository.findByEmail(email)
        if (!userOpt.isPresent) {
            throw UserNotFoundException("Mail does not exist")
        }
        if (!passwordEncoder.matches(
                password,
                userOpt.get().password
            )
        ) throw InvalidParameterException("Credentials do not match")

        return userOpt.get();
    }


//    @Cacheable(value = ["users"], key = "#id")
    open fun getById(id: Int): User {
        return userRepository.findUserById(id) ?: throw UserNotFoundException(
            "User with id $id not found"
        )
    }
    open fun getUserFromAuthentication(authentication: Authentication?): User {
        if(authentication == null || authentication.principal == null || authentication.principal !is User)
            throw AuthenticationNotPresentException("Authentication not present")

        return authentication.principal as User
    }



    @Transactional
    open fun createUser(
        dto: CreateUserAccountDTO
    ): User {

        if (userRepository.existsByEmail(dto.email.trim().lowercase())) {
            throw IllegalArgumentException("Email already in use")
        }

        val user = User(null,dto.email, password = passwordEncoder.encode(dto.password),dto.number, givenName = dto.firstName, familyName = dto.lastName)

        //todo procedura vo bazava za da ne mozit da imat dve default grupi za eden user
        val defaultGroup = AppGroup(name = "Default Group", user = user, isDefault = true)
        val savedUser = userRepository.save(user)
        appGroupRepository.save(defaultGroup)
        return savedUser



    }

}