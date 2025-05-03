package com.tosak.authos.entity
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.entity.compositeKeys.RedirectIdKey
import com.vladmihalcea.hibernate.type.array.StringArrayType
import jakarta.persistence.*
import lombok.NoArgsConstructor
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.Type
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "app")
class App (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_id_seq")
    @SequenceGenerator(name = "app_id_seq", sequenceName = "app_id_seq", allocationSize = 1)
    val id : Int? = null,
    val name : String = "",
    @Column(name = "client_id")
    val clientId : String = "",
    @Column(name = "client_secret")
    val clientSecret : String = "",
    @Column(name = "client_secret_expires_at")
    val clientSecretExpiresAt : LocalDateTime? = null,
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    @Column(name = "grant_types", columnDefinition = "text[]")
    @Type(value = StringArrayType::class)
    val grantTypes: Array<String> = arrayOf(),
    @Column(name = "logo_uri")
    val logoUri: String = "",
    @Column(name = "scopes", columnDefinition = "text[]")
    @Type(value = StringArrayType::class)
    val scopes : Array<String> = arrayOf(),
    @Column(name = "client_uri")
    val clientUri : String = "",
    @Column(name = "response_types", columnDefinition = "text[]")
    @Type(value = StringArrayType::class)
    val responseTypes: Array<String> = arrayOf(),
    @Column(name = "short_description")
    val shortDescription : String = "",
    @Column(name = "token_endpoint_auth_method")
    val tokenEndpointAuthMethod : String = "",
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    val user: User = User(),
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    val group: AppGroup = AppGroup(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app_id", cascade = [jakarta.persistence.CascadeType.ALL], orphanRemoval = true)
    val redirectUris : MutableList<RedirectUri> = mutableListOf(),
){


    fun toDTO(): AppDTO {
        return AppDTO(id,name,redirectUris.map { uri -> uri.id?.redirectUri },
            clientId,clientSecret,shortDescription,createdAt,
            group,logoUri,scopes.toList(),responseTypes.toList(),grantTypes.toList(),
            tokenEndpointAuthMethod)
    }
    fun addRedirectUris(uris: Collection<String>) {
        uris.forEach { uri ->
            redirectUris.add(RedirectUri(RedirectIdKey(this.id!!, uri)))
        }
    }
}