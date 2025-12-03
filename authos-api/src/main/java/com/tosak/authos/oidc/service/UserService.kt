package com.tosak.authos.oidc.service

import com.tosak.authos.oidc.common.dto.CreateUserAccountDTO
import com.tosak.authos.oidc.common.dto.QrCodeDTO
import com.tosak.authos.oidc.entity.AppGroup
import com.tosak.authos.oidc.entity.User
import com.tosak.authos.oidc.exceptions.unauthorized.InvalidUserCredentials
import com.tosak.authos.oidc.common.pojo.strategy.MFATokenStrategy
import com.tosak.authos.oidc.common.utils.AESUtil
import com.tosak.authos.oidc.repository.AppGroupRepository
import com.tosak.authos.oidc.repository.UserRepository
import com.tosak.authos.oidc.common.utils.JwtTokenFactory
import com.tosak.authos.oidc.common.utils.b64UrlSafeDecoder
import com.tosak.authos.oidc.common.utils.b64UrlSafeEncoder
import com.tosak.authos.oidc.exceptions.base.AuthosException
import com.tosak.authos.oidc.exceptions.base.HttpUnauthorizedException
import com.tosak.authos.oidc.common.utils.demand
import com.tosak.authos.oidc.exceptions.base.HttpBadRequestException
import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.qr.ZxingPngQrGenerator
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import dev.samstevens.totp.util.Utils.getDataUriForImage
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.http.HttpHeaders
import java.time.Duration
import java.time.LocalDateTime
import java.util.Optional

@Service
open class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val appGroupRepository: AppGroupRepository,
    private val ppidService: PPIDService,
    private val tokenFactory: JwtTokenFactory,
    private val appGroupService: AppGroupService,
    private val tokenService: TokenService,
    private val mailService: MailService,
    private val aesUtil: AESUtil,
    private val jwtTokenFactory: JwtTokenFactory,
) {

    @Value("\${authos.cookie.domain}")
    lateinit var cookieDomain: String

    @Value("\${authos.api.host}")
    lateinit var apiHost: String

    open fun verifyCredentials(email: String, password: String): User {
        val userOpt: Optional<User> = userRepository.findByEmail(email)

        //todo proverka dali e active user acc

        demand(userOpt.isPresent && passwordEncoder.matches(password, userOpt.get().password))
        { AuthosException("invalid_credentials", "Could not authenticate user") }


        return userOpt.get();
    }


    //    @Cacheable(value = ["users"], key = "#id")
    open fun getById(id: Int): User {
        return userRepository.findUserById(id) ?: throw AuthosException("invalid_user", "Could not identify user",HttpUnauthorizedException())
    }

    open fun getUserFromAuthentication(authentication: Authentication?): User {

        demand(authentication != null && authentication.principal != null && authentication.principal is User)
        { AuthosException("Unauthorized", " Authentication object invalid", HttpUnauthorizedException()) }

        return authentication!!.principal as User
    }


    open fun getMfaCookieHeader(user: User) : HttpHeaders {
        val mfaToken = jwtTokenFactory.createToken(MFATokenStrategy(user,apiHost))
        val mfaCookie = ResponseCookie
            .from("MFA_TOKEN",mfaToken.serialize())
            .domain(cookieDomain)
            .sameSite("None")
            .maxAge(Duration.ofMinutes(5))
            .path("/")
            .httpOnly(true)
            .secure(true)
            .build()

        val headers = HttpHeaders()
        headers.add("Set-Cookie", mfaCookie.toString())
        return headers;
    }


    @Transactional
    open fun createAccount(
        dto: CreateUserAccountDTO
    ): User {

        if (userRepository.existsByEmail(dto.email.trim().lowercase())) {
            throw IllegalArgumentException("Email already in use")
        }

        val user = userRepository.save(
            User(
                null,
                dto.email,
                password = passwordEncoder.encode(dto.password),
                dto.number,
                givenName = dto.firstName,
                familyName = dto.lastName
            )
        )
        appGroupRepository.save(AppGroup(name = "Default Group", user = user, isDefault = true))
        val token = tokenService.generateRegistrationConfirmationToken(user)
        mailService.sendRegistrationConfirmationEmail(user, token)
        return user

    }

    open fun activateAccount(user: User) {
        user.isActive = true
        userRepository.save(user)
    }

    open fun generateTotpSecret(user: User) {
        val secret = DefaultSecretGenerator(32).generate()
        user.totpSecret = b64UrlSafeEncoder(aesUtil.encrypt(secret));
        userRepository.save(user)
    }

    open fun getTotpQr(user: User): QrCodeDTO {

        // todo ako mu e enabled mfa ne trebit nisto da vratit, samo error
        if (user.mfaEnabled) {
            throw IllegalStateException("mfa already enabled")
        }

        if (user.totpSecret.isNullOrBlank()) {
            throw IllegalStateException("totp secret not set")
        }

        val secret = aesUtil.decryptBytes(b64UrlSafeDecoder(user.totpSecret!!))
        val qrData: QrData = QrData.Builder()
            .label(user.email)
            .issuer("authos.imaps.mk")
            .secret(secret)
            .digits(6)
            .period(30)
            .build()

        val generator = ZxingPngQrGenerator();
        val imageData: ByteArray = generator.generate(qrData)
        val mimeType = generator.imageMimeType
        val dataUri = getDataUriForImage(imageData, mimeType)

        println("secret qr: $secret")

        return QrCodeDTO(dataUri, secret)

    }

    open fun verifyTotp(user: User, otp: String): Boolean {

        demand(otp.length == 6) { AuthosException("bad_otp","otp must be exactly 6 digits") }
        demand(!user.totpSecret.isNullOrBlank()) {
            IllegalStateException("totp secret not present",)
        }

        val timeProvider = SystemTimeProvider()
        val codeGenerator = DefaultCodeGenerator()
        val codeVerifier = DefaultCodeVerifier(codeGenerator, timeProvider)
        codeVerifier.setTimePeriod(30)
        codeVerifier.setAllowedTimePeriodDiscrepancy(2)
        val result = codeVerifier.isValidCode(aesUtil.decryptBytes(b64UrlSafeDecoder(user.totpSecret!!)), otp)

        println("totp result $result")


        return result

    }

    open fun enableTotp(user: User, otp: String): Boolean {
        val result = verifyTotp(user, otp)
        if (result) {
            user.mfaEnabled = true
            userRepository.save(user)
        }
        return result
    }

    open fun disableTotp(user: User) {
        user.totpSecret = null
        user.mfaEnabled = false
        userRepository.save(user)
    }

    open fun onLoginSuccess(user: User) {
        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)
    }


}