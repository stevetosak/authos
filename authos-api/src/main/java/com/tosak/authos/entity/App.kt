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
    val name : String = "",
    @Column(name = "client_id")
    val clientId : String = "",
    @Column(name = "client_secret")
    val clientSecret : String = "",
    @Column(name = "client_secret_expires_at")
    val clientSecretExpiresAt : LocalDateTime? = null,
    @Column(name = "created_at")
    val createdAt : LocalDateTime = LocalDateTime.now(),
    @Column(name = "grant_types")
    var grantTypes: String = "",
    @Column(name = "logo_uri")
    val logoUri: String = "",
    @Column(name = "scopes")
    var scopes : String = "",
    @Column(name = "client_uri")
    val clientUri : String = "",
    @Column(name = "response_types")
    var responseTypes: String = "",
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
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "app", cascade = [jakarta.persistence.CascadeType.ALL], orphanRemoval = true)
    val redirectUris : MutableList<RedirectUri> = mutableListOf(),


){

    @Transient
    lateinit var scopesCollection: MutableList<String>
    @Transient
    lateinit var grantTypesCollection: MutableList<String>
    @Transient
    lateinit var responseTypesCollection: MutableList<String>


    fun toDTO(): AppDTO {
        return AppDTO(id,name,redirectUris.map { uri -> uri.id?.redirectUri },
            clientId,clientSecret,shortDescription,createdAt,
            group,logoUri,scopesCollection,responseTypesCollection,grantTypesCollection,
            tokenEndpointAuthMethod)
    }
    fun addRedirectUris(uris: Collection<String>) {
        uris.forEach { uri ->
            redirectUris.add(RedirectUri(RedirectUriId(this.id, uri)))
        }
    }

    @PrePersist
    @PreUpdate
    fun prePersist() {

        scopes = serializeTransientLists(scopesCollection," ")
        grantTypes = serializeTransientLists(grantTypesCollection,";")
        responseTypes = serializeTransientLists(responseTypesCollection,";")

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