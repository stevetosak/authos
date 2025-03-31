package com.tosak.authos.service

import com.tosak.authos.dto.CreateUserAccountDTO
import com.tosak.authos.entity.User
import com.tosak.authos.exceptions.unauthorized.UserNotFoundException
import com.tosak.authos.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.InvalidParameterException
import java.util.Optional

@Service
class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun verifyCredentials(email: String, password: String): User {
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


    fun getById(id: Int): User {
        return userRepository.findUserById(id) ?: throw UserNotFoundException(
            "User with id $id not found"
        )
    }



    fun createUser(
        dto: CreateUserAccountDTO
    ): User {

        if (userRepository.existsByEmail(dto.email.trim().lowercase())) {
            throw IllegalArgumentException("Email already in use")
        }

        return User()

        TODO()


    }

}