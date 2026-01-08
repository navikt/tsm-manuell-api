package no.nav.tsm_manuell_api.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain
import java.time.Instant

@Configuration
@Profile("!local && !test")
class SecurityConfig {

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/**")
            csrf { disable() }
            oauth2ResourceServer { jwt {} }
            //            oauth2Client {}
            authorizeHttpRequests {
                authorize("/internal/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            cors { disable() }
        }
        return http.build()
    }
}

@Configuration
@Profile("local", "test")
class LocalSecurityConfig {

    @Bean
    fun localSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/**")
            csrf { disable() }
            oauth2ResourceServer { jwt {} }
            authorizeHttpRequests {
                authorize("/internal/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            cors { disable() }
        }
        return http.build()
    }

    @Bean
    fun mockJwtDecoder(): JwtDecoder {
        return JwtDecoder { token ->
            val parts = token.split(".")
            val claims = if (parts.size >= 2) {
                try {
                    parseClaimsFromToken(token)
                } catch (e: Exception) {
                    createDefaultClaims()
                }
            } else {
                createDefaultClaims()
            }

            Jwt.withTokenValue(token)
                .headers { it.putAll(mapOf("alg" to "none", "typ" to "JWT")) }
                .claims { it.putAll(claims) }
                .build()
        }
    }

    private fun parseClaimsFromToken(token: String): Map<String, Any> {
        val parts = token.split(".")
        val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]))
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        val claims = mapper.readValue(payload, Map::class.java) as MutableMap<String, Any>
        
        // Convert timestamp claims to Instant
        claims["iat"]?.let { if (it is Number) claims["iat"] = Instant.ofEpochSecond(it.toLong()) }
        claims["exp"]?.let { if (it is Number) claims["exp"] = Instant.ofEpochSecond(it.toLong()) }
        claims["nbf"]?.let { if (it is Number) claims["nbf"] = Instant.ofEpochSecond(it.toLong()) }
        
        return claims
    }

    private fun createDefaultClaims(): Map<String, Any> {
        return mapOf(
            "sub" to "local-user",
            "oid" to "local-oid-12345",
            "preferred_username" to "local.user@nav.no",
            "iat" to Instant.now().epochSecond,
            "exp" to Instant.now().plusSeconds(3600).epochSecond
        )
    }
}
