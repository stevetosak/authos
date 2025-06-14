package com.authos.service

import com.authos.data.AuthTokenResponse
import com.authos.data.TokenType
import com.authos.duster_client.DusterClient
import com.authos.model.DusterApp
import com.authos.model.UserInfo
import com.authos.repository.TokenRepository
import com.authos.config.AUTHOS_AUTHORIZE_URL
import com.authos.duster_client.NextAuthorizeRequestType
import com.authos.service.DusterRequestService.ResponseResult.*
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import org.jetbrains.exposed.v1.core.exposedLogger
import java.net.URLEncoder

/**
 * Service responsible for handling Duster-related OAuth requests and token exchanges.
 *
 * This service facilitates interactions with the Duster's authentication and authorization system.
 * It performs user information retrieval, token exchanges, and authorization URL generation,
 * while also managing token persistence and handling various token lifecycle operations.
 *
 * @constructor Initializes the service with a DusterClient for API interactions and a TokenRepository for token management.
 * @param client The Duster client used for API communication.
 * @param tokenRepository The token repository used for storing and retrieving tokens.
 */
class DusterRequestService(private val client: DusterClient, private val tokenRepository: TokenRepository) {
    /**
     * Fetches user information based on an access token and returns the result.
     * This method attempts to retrieve user information through `fetchUserInfo` and processes the response.
     * In case of an exception, it logs the error and returns a failure result.
     *
     * @param accessToken The access token used for fetching user information.
     * @return A `UserInfoResult` which can either be `Success` containing the pruned user information
     *         or `Failure` in case of an error.
     */
    private suspend fun userInfoResult(accessToken: String): UserInfoResult {
        return try {
            val resp = client.fetchUserInfo(accessToken)
            UserInfoResult.Success(UserInfo.getPrunedObject(resp.body()))
        } catch (e: Exception) {
            exposedLogger.error(e.stackTraceToString())
            UserInfoResult.Failure
        }
    }

    // vo Failure da go vrakjam authorize url so tocni parametri

    /**
     * Attempts to refresh the user's access and refresh tokens using the provided refresh token.
     * It saves new tokens in the token repository if the operation is successful.
     *
     * @param sub The subject identifier for the user for whom tokens are being refreshed.
     * @param refreshToken The existing refresh token used to request a new access token.
     * @return A `RefreshResult` object indicating the result of the token refresh operation.
     *         `RefreshResult.Success` if the refresh was successful, otherwise `RefreshResult.Failure`.
     */
    private suspend fun refreshTokenResult(sub: String, refreshToken: String): RefreshResult {
        return try {
            val resp = client.refreshTokenRequest(refreshToken)
            val tokenResponse: AuthTokenResponse = resp.body()
            tokenRepository.save(TokenType.ACCESS_TOKEN, sub, tokenResponse.accessToken, tokenResponse.expiresIn.toLong())
            if(tokenResponse.refreshToken != null) {
                tokenRepository.save(TokenType.REFRESH_TOKEN, sub, tokenResponse.refreshToken)
            } else {
                // najverojatno gresen scope imat pa ne mu davat refresh token
                // ovde trebit nekako da zacuvam/ signaliziram deka trebit vo sledniot
                // authorize request da imat offline_access scope
            }
            RefreshResult.Success
        } catch (e: Exception) {
            RefreshResult.Failure
        }
    }

    /**
     * Generates an authorization URL for the given application, user identifier, and state.
     * This URL is used to initiate the OAuth2 authorization process.
     *
     * @param app The application details including client information and redirect URI.
     * @param sub The identifier of the user (subject). If null or specific conditions are met, a prompt for consent will be added.
     * @param state A unique string to maintain state between the request and callback. It is also used for CSRF protection.
     * @return The complete authorization URL as a string.
     */
    suspend fun generateAuthorizeUrl(app: DusterApp, sub:String? = null, state:String): String {
        var sc = app.scope
            if(client.nextRequestType == NextAuthorizeRequestType.OFFLINE_ACCESS){
                if(!app.scope.contains("offline_access")){
                    sc = "${app.scope} offline_access"
                }
            }

        val url = "$AUTHOS_AUTHORIZE_URL?client_id=${client.dusterApp.clientId}" +
                "&redirect_uri=${Url(app.redirectUri)}&state=$state" +
                "&scope=${URLEncoder.encode(sc, "UTF-8")}" +
                "&response_type=code"



        if(sub == null || client.nextRequestType == NextAuthorizeRequestType.OFFLINE_ACCESS){
            client.nextRequestType = NextAuthorizeRequestType.AUTO
            return "$url&prompt=consent"
        };
        val idToken = tokenRepository.getToken(sub, TokenType.ID_TOKEN)

        if(idToken == null) return "$url&prompt=consent"

        return "$url&id_token_hint=$idToken&prompt=none"

    }


    /**
     * Attempts to exchange an access token for user information or triggers a refresh token exchange if the access
     * token is not available or invalid.
     *
     * @param sub The unique identifier (subject) associated with the user for whom the token exchange is being attempted.
     * @return A [ResponseResult] which is either [ResponseResult.Success] containing the retrieved user information
     * or [ResponseResult.Failure] in case of any errors during the token exchange process.
     */
    suspend fun tryAccessTokenExchange(sub: String): ResponseResult {
        val accessToken = tokenRepository.getToken(sub, TokenType.ACCESS_TOKEN)
        if (accessToken == null){
            println("Access token not present. Trying refresh token exchange.")
            return tryRefreshTokenExchange(sub)
        }
        println("Fetched access token.")

        return when (val result = userInfoResult(accessToken)) {
            is UserInfoResult.Success -> Success(result.data)
            is UserInfoResult.Failure -> tryRefreshTokenExchange(sub)
        }
    }

    // refresh sa pret samo ako e konfiguriran offline access
    // ovie status ne trebit da sa http tukucustom errors

    /**
     * Attempts to refresh the token associated with the given subject identifier.
     * If a valid refresh token is available, it tries to generate a new access token.
     * If the refresh token is missing or the token exchange fails, it updates the
     * client's request type to require offline access for future authorization attempts.
     *
     * @param sub The subject identifier for which the token exchange is attempted.
     * @return A ResponseResult object indicating the outcome. A `Success` result
     *         contains the data payload if the operation succeeds, while a `Failure`
     *         result provides the HTTP status and a descriptive error message in case of failure.
     */
    private suspend fun tryRefreshTokenExchange(sub: String): ResponseResult {
        val refreshToken = tokenRepository.getToken(sub, TokenType.REFRESH_TOKEN)
        println("Fetched refresh token: $refreshToken")
        if (refreshToken == null) {
            client.nextRequestType = NextAuthorizeRequestType.OFFLINE_ACCESS
            println("Refresh token not present.")
            return Failure(HttpStatusCode.BadRequest, "No refresh token present")
        }
        return when (refreshTokenResult(sub, refreshToken)) {
            // ako e sporo vaka, direk ke go pustam novoiot access token
            is RefreshResult.Success -> {
                println("Successfully obtained new access token. Trying access token exchange.")
                tryAccessTokenExchange(sub)
            }
            is RefreshResult.Failure -> {
                client.nextRequestType = NextAuthorizeRequestType.OFFLINE_ACCESS
                Failure(HttpStatusCode.BadRequest, "Could not refresh token")
            }
        }
    }


    /**
     * Represents the result of a response operation that can either be successful or a failure.
     * This sealed class is used to encapsulate the outcome of operations and provide detailed
     * information when an operation fails.
     */
    sealed class ResponseResult {
        /**
         * Represents a successful result of an operation.
         *
         * This class is a specific type of ResponseResult and is used to indicate that
         * an operation was completed successfully. The resulting data from the operation
         * is provided as a property.
         *
         * @property data Represents the result of a successful operation.
         */
        data class Success(val data: Any) : ResponseResult()
        /**
         * Represents an operation failure result with a status and a message.
         *
         * This class is a part of the `ResponseResult` sealed class hierarchy and is used
         * to encapsulate information about an unsuccessful operation. It typically indicates
         * the HTTP status code and a descriptive error message relevant to the failure.
         *
         * @property status The HTTP status code associated with the failure.
         * @property message A descriptive message providing details about the failure.
         */
        data class Failure(val status: HttpStatusCode, val message: String) : ResponseResult()
    }

    /**
     * Represents the result of an operation involving user information retrieval.
     *
     * It is a sealed class that defines two possible outcomes for the operation:
     * - A successful result containing user-related data.
     * - A failure result indicating that the operation was unsuccessful.
     */
    private sealed class UserInfoResult {
        /**
         * Represents a successful result containing user information.
         *
         * This is a data class that encapsulates the result of operations
         * fetching or processing user information where the operation is successful.
         *
         * @property data The user information or data associated with the successful result.
         */
        data class Success(val data: Any) : UserInfoResult()
        /**
         * Represents a failure outcome in the process of obtaining user information.
         *
         * This object is used when an attempt to retrieve user information fails due
         * to an exception or error during the operation. It signifies an unsuccessful
         * result within the `UserInfoResult` sealed class hierarchy, allowing easier
         * handling of both success and failure cases in the workflow.
         */
        object Failure : UserInfoResult()
    }

    /**
     * Represents the result of a token refresh operation.
     */
    private sealed class RefreshResult {
        /**
         * Represents a successful result of a token refresh operation.
         *
         * Used to indicate that a new access token was successfully obtained and stored.
         */
        object Success : RefreshResult()
        /**
         * Represents a failure result in the refresh token exchange process.
         *
         * This object is a part of the `RefreshResult` sealed class, indicating that the refresh
         * token operation did not succeed. It is used to explicitly handle scenarios where
         * the refresh token exchange process encounters an error or exception.
         *
         * Typical usage can involve returning this result when refresh token processing
         * fails due to invalid tokens, network issues, or other unexpected errors. This allows
         * higher-level business logic to recognize and respond to the failure state appropriately.
         */
        object Failure : RefreshResult()
    }
}