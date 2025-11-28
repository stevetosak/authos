import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.servlet.http.HttpServletRequest
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class SSOSession(
    val userId: Int,
    val appId: Int,
    val groupId: Int,
    val ipAddress: String,
    val createdAt: String = LocalDateTime.now().toString(),
) {
    companion object {
        fun fromRequest(userId: Int, appId: Int, groupId: Int, req: HttpServletRequest) =
            SSOSession(userId, appId, groupId, req.remoteAddr)
    }
}