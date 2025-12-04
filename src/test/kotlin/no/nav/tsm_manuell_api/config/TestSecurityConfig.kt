package no.nav.tsm_manuell_api.config

import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

@TestConfiguration
@Profile("test")
class TestSecurityConfig {

    @Bean
    @Primary
    fun testClientRegistrationRepository(): ClientRegistrationRepository {
        val registration =
            ClientRegistration.withRegistrationId("test")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri("http://localhost:8080/token")
                .build()

        return InMemoryClientRegistrationRepository(registration)
    }

    @Bean(initMethod = "migrate")
    fun flyway(dataSource: DataSource): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
    }
}
