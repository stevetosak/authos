package com.tosak.authos.service

import com.tosak.authos.entity.App
import com.tosak.authos.entity.SSOSession
import com.tosak.authos.entity.User
import com.tosak.authos.repository.SSOSessionRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SSOSessionService(
    private val ssoSessionRepository: SSOSessionRepository
)
{

    fun createSession(user: User, app: App,request:HttpServletRequest,httpSession: HttpSession) {
        if(httpSession.isNew){
            httpSession.setAttribute("uid", user.id)
        }

        println("SESSION ID ON CREATE: ${httpSession.id}")
        val uid = httpSession.getAttribute("uid");
        require (uid != null && uid == user.id) { "Invalid user id session" }


        println("${httpSession.getAttribute("uid")} USERID" )

        val existingSession = ssoSessionRepository.findActiveSessionByUserAndApp(app.id!!,user.id!!)
        if (existingSession == null) {
            val ssoSession = SSOSession(null,request.remoteAddr,
                LocalDateTime.now(),LocalDateTime.now().plusHours(12),
                request.getHeader("User-Agent"),
                LocalDateTime.now(),user,app);
            ssoSessionRepository.save(ssoSession);

        }else {
           val ssoSession = SSOSession(existingSession.id,existingSession.ipAddress,
                existingSession.createdAt,existingSession.expiresAt,
                existingSession.userAgent,
                LocalDateTime.now(),existingSession.user,existingSession.app);
            ssoSessionRepository.save(ssoSession);
        }

    }
}