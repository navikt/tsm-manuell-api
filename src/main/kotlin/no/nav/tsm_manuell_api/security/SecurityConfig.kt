package no.nav.tsm_manuell_api.security

import java.time.Instant
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain

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
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
            cors { disable() }
        }
        return http.build()
    }


}
