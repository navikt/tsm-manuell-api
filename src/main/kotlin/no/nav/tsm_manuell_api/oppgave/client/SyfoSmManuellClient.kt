package no.nav.tsm_manuell_api.oppgave.client

import no.nav.tsm_manuell_api.security.TexasClient
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

interface ISyfoSmManuellClient {
    fun hentOppgaveId(sykmeldingId: String): Result<SmmOppgaveResponse>
}

@Profile("!local & !test")
@Component
class SyfoSmManuellClient(
    restClientBuilder: RestClient.Builder,
    private val texasClient: TexasClient,
    @param:Value($$"${services.syfosmmanuell.url}") private val smmEndpointUrl: String,
) : ISyfoSmManuellClient {
    val logger = logger()
    private val restClient = restClientBuilder.baseUrl(smmEndpointUrl).build()

    override fun hentOppgaveId(sykmeldingId: String): Result<SmmOppgaveResponse> {
        val (accessToken) = getToken()
        return try {
            val response: SmmOppgaveResponse? =
                restClient
                    .get()
                    .uri("/api/v1/oppgave/sykmelding/{sykmeldingId}", sykmeldingId)
                    .headers {
                        it.set("Nav-Consumer-Id", "tsm-manuell-api")
                        it.set("Authorization", "Bearer $accessToken")
                    }
                    .retrieve()
                    .body(SmmOppgaveResponse::class.java)
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(IllegalStateException("Response body was null"))
            }
        } catch (e: RestClientResponseException) {
            logger.error("Feil ved henting av oppgaveId knytt til sykmeldingId ${sykmeldingId} fra SMM: ${e.responseBodyAsString}", e)
            Result.failure(e)
        }
    }

    private fun getToken(): TexasClient.TokenResponse =
        texasClient.requestToken("teamsykmelding", "syfosmmanuell-backend")
}

data class SmmOppgaveResponse(val oppgaveId: Int, val sykmeldingId: String)
