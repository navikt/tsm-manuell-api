package no.nav.tsm_manuell_api.security.authentication

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local", "test")
@Component
class MockIsTilgangskontrollClient : IIstilgangskontrollClient {
    override fun sjekkVeiledersTilgang(accessToken: String, ident: String): Result<Tilgang> {
        return if (ident != "Z999999") Result.success(Tilgang(true))
        else Result.success(Tilgang(false))
    }
}
