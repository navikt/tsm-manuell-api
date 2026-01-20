package no.nav.tsm_manuell_api.security.authentication

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit
import no.nav.tsm_manuell_api.security.TexasClient
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

interface IIstilgangskontrollClient {
    fun sjekkVeiledersTilgang(accessToken: String, ident: String): Result<Tilgang>
}

@Profile("!local && !test")
@Component
class IstilgangskontrollClient(
    restClientBuilder: RestClient.Builder,
    private val texasClient: TexasClient,
    @param:Value($$"${services.teamsykefravr.istilgangskontroll.url}")
    private val isTilgangskontrollUrl: String,
) : IIstilgangskontrollClient {
    val logger = logger()
    private val restClient = restClientBuilder.baseUrl(isTilgangskontrollUrl).build()

    val istilgangskontrollCache: Cache<Map<String, String>, Tilgang> =
        Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(100).build()

    companion object {
        const val NAV_PERSONIDENT_HEADER = "nav-personident"
    }

    override fun sjekkVeiledersTilgang(accessToken: String, ident: String): Result<Tilgang> {
        val (accessToken) = getToken()

        val cache =
            istilgangskontrollCache.getIfPresent(mapOf(Pair(accessToken, ident)))?.let {
                logger.debug("Traff cache for istilgangskontroll")
                it
            }
        if (cache != null) return Result.success(cache)

        return try {
            val response =
                restClient
                    .get()
                    .uri("/api/tilgang/navident/person")
                    .headers {
                        it.set("Nav-Consumer-Id", "tsm-manuell-api")
                        it.set("Authorization", "Bearer $accessToken")
                        it.set(NAV_PERSONIDENT_HEADER, ident)
                    }
                    .retrieve()
                    .body(Tilgang::class.java)

            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(IllegalStateException("Response body was null"))
            }
        } catch (e: RestClientResponseException) {
            logger.error("Feil ved sjekk av veilders tilgang: ${e.responseBodyAsString}", e)
            Result.failure(e)
        }
    }

    private fun getToken(): TexasClient.TokenResponse =
        texasClient.requestToken("teamsykefravr", "istilgangkontroll")
}

data class Tilgang(
    val erGodkjent: Boolean,
)
