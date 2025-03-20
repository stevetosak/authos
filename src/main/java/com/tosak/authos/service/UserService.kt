package com.tosak.authos.service

import com.tosak.authos.repository.UserRepository
import com.tosak.authos.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.InvalidParameterException
import java.util.Optional

@Service
class UserService @Autowired constructor(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun nativeLogin(email: String, password: String): User {
        val userOpt: Optional<User> = userRepository.findByEmail(email)
        if (!userOpt.isPresent) {
            throw UsernameNotFoundException("User not found")
        }
        if(!passwordEncoder.matches(password,userOpt.get().password)) throw InvalidParameterException("Credentials do not match")
        return userOpt.get()
    }

}