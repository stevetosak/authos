package com.tosak.authos.oidc.common.pojo
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml

@Component
class ClaimConfig {
    var data = HashMap<String, List<String>>()

    @PostConstruct
    fun init() {
        try {
            val inputStream = javaClass.getResourceAsStream("/scopes_to_claims.yml")
                ?: throw IllegalArgumentException("scopes_to_claims.yml not found")

            val yaml = Yaml()
            val loaded: Map<String, Map<String, List<String>>> = yaml.load(inputStream)
            data = loaded["scopes_to_claims"] as HashMap<String, List<String>>
            println("data: $data")
        } catch (error: Exception) {
            println("YAML load error: ${error.message}")
        }
    }
}
