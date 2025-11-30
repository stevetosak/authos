import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
data class SSOSession(
    val userId: Int,
    val appId: Int,
    val groupId: Int,
    val ipAddress: String,
    val authTime: Long = Instant.now().epochSecond,
) {
    companion object {
        fun fromRequest(userId: Int, appId: Int, groupId: Int, req: HttpServletRequest) =
            SSOSession(userId, appId, groupId, req.remoteAddr)
    }
}