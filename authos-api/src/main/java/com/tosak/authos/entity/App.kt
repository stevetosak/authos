package com.tosak.authos.entity
import com.tosak.authos.dto.AppDTO
import com.tosak.authos.entity.compositeKeys.RedirectUriId
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.springframework.boot.convert.Delimiter
import java.time.LocalDateTime
import kotlin.jvm.Transient

@Entity
@Table(name = "app")
class App (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_id_seq")
    @SequenceGenerator(name = "app_id_seq", sequenceName = "app_id_seq", allocationSize = 1)
    val id : Int? = null,
    var name : String = "",
    @Column(name = "client_id")
    var clientId : String = "",
    @Column(name = "client_secret")
    var clientSecret : String = "",
    @Column(name = "client_secret_expires_at")
    var clientSecretExpiresAt : LocalDateTime? = null,
    @Column(name = "created_at")
    var createdAt : LocalDateTime = LocalDateTime.now(),
    @Column(name = "grant_types")
    var grantTypes: String = "",
    @Column(name = "logo_uri")
    var logoUri: String? = null,
    @Column(name = "scopes")
    var scopes : String = "",
    @Column(name = "client_uri")
    var clientUri : String? = null,
    @Column(name = "response_types")
    var responseTypes: String = "",
    @Column(name = "short_description")
    var shortDescription : String? = null,
    @Column(name = "token_endpoint_auth_method")
    var tokenEndpointAuthMethod : String = "",
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    val user: User = User(),
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    var group: AppGroup = AppGroup(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app", cascade = [CascadeType.ALL], orphanRemoval = true)
    var redirectUris : MutableList<RedirectUri> = mutableListOf(),

){
    @Transient
    lateinit var scopesCollection: MutableList<String>
    @Transient
    lateinit var grantTypesCollection: MutableList<String>
    @Transient
    lateinit var responseTypesCollection: MutableList<String>

    constructor(appDTO: AppDTO) : this(
        id = appDTO.id,
        name = appDTO.name,
        clientId = appDTO.clientId,
        clientSecret = appDTO.clientSecret,
        clientSecretExpiresAt = null,
        createdAt = appDTO.createdAt,
        grantTypes = "",
        logoUri = appDTO.logoUri,
        scopes = "",
        clientUri = appDTO.appUrl,
        responseTypes = "",
        shortDescription = appDTO.shortDescription,
        tokenEndpointAuthMethod = appDTO.tokenEndpointAuthMethod ?: "",
        group = appDTO.group,
        redirectUris = appDTO.redirectUris.map { uri -> RedirectUri(RedirectUriId(appDTO.id,uri ?: "")) }.toMutableList(),
    ) {
        this.scopesCollection = appDTO.scopes.toMutableList()
        this.grantTypesCollection = appDTO.grantTypes.toMutableList()
        this.responseTypesCollection = appDTO.responseTypes.toMutableList()
    }


    fun toDTO(): AppDTO {
        return AppDTO(id,name,redirectUris.map { uri -> uri.id?.redirectUri },
            clientId,clientSecret,clientSecretExpiresAt,shortDescription,createdAt,
            group,logoUri,clientUri,scopesCollection,responseTypesCollection,grantTypesCollection,
            tokenEndpointAuthMethod)
    }
    fun addRedirectUris(uris: Collection<String>) {
        uris.forEach { uri ->
            redirectUris.add(RedirectUri(RedirectUriId(this.id, uri),this))
        }
    }

    @PrePersist
    @PreUpdate
    fun prePersist() {
        scopesCollection = scopes.split(" ").toMutableList()
        grantTypesCollection = grantTypes.split(";").toMutableList()
        responseTypesCollection = responseTypes.split(";").toMutableList()

    }

    companion object{
        fun serializeTransientLists(collection: Collection<String>,delimiter: String): String{
            val sb : StringBuilder = StringBuilder()
            var delim = ""

            for(f in collection) {
                sb.append(delim).append(f)
                delim = delimiter
            }
            return sb.toString()
        }
    }

    @PostLoad
    fun postLoad() {
        scopesCollection = scopes.split(" ").toMutableList()
        grantTypesCollection = grantTypes.split(";").toMutableList()
        responseTypesCollection = responseTypes.split(";").toMutableList()
    }
}