package no.nav.tsm_manuell_api.oppgave.client

import no.nav.tsm_manuell_api.oppgave.model.GosysOpprettOppgave
import no.nav.tsm_manuell_api.oppgave.model.GosysOpprettOppgaveResponse
import no.nav.tsm_manuell_api.security.TexasClient
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

interface IGosysOppgaveClient {
    fun opprettOppgave(opprettOppgave: GosysOpprettOppgave): Result<GosysOpprettOppgaveResponse>
}

@Profile("!local & !test")
@Component
class GosysOppgaveClient(
    restClientBuilder: RestClient.Builder,
    private val texasClient: TexasClient,
    @param:Value($$"${services.gosys.oppgave.url}") private val gosysOppgaveEndpointUrl: String,
) : IGosysOppgaveClient {
    val logger = logger()
    private val restClient = restClientBuilder.baseUrl(gosysOppgaveEndpointUrl).build()

    override fun opprettOppgave(
        opprettOppgave: GosysOpprettOppgave
    ): Result<GosysOpprettOppgaveResponse> {
        val (accessToken) = getToken()

        return try {
            val response =
                restClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers {
                        it.set("Nav-Consumer-Id", "tsm-manuell-api")
                        it.set("Authorization", "Bearer $accessToken")
                    }
                    .body(opprettOppgave)
                    .retrieve()
                    .body<GosysOpprettOppgaveResponse>()

            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(IllegalStateException("Response body was null"))
            }
        } catch (e: RestClientResponseException) {
            logger.error("Feil ved opprettelse av gosys oppgave: ${e.responseBodyAsString}", e)
            Result.failure(e)
        }
    }

    private fun getToken(): TexasClient.TokenResponse = texasClient.requestToken("fss", "oppgave")
    // TODO er dette rett verdier ?????

}
