package no.nav.tsm_manuell_api.oppgave.client

import no.nav.tsm_manuell_api.oppgave.GosysOpprettOppgave
import no.nav.tsm_manuell_api.oppgave.GosysOpprettOppgaveResponse
import no.nav.tsm_manuell_api.security.TexasClient
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

@Component
class GosysOppgaveClient(
    restClientBuilder: RestClient.Builder,
    private val texasClient: TexasClient,
    @param:Value($$"${services.gosys.oppgave.url}") private val gosysOppgaveEndpointUrl: String,
) {
    val logger = logger()
    private val restClient = restClientBuilder.baseUrl(gosysOppgaveEndpointUrl).build()

    fun opprettOppgave(opprettOppgave: GosysOpprettOppgave): Result<GosysOpprettOppgaveResponse> {
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

            //todo handle different cases before returning
            Result.success(response)
        } catch (rcre: RestClientResponseException ) {
            logger.error("Feil ved opprettelse av gosys oppgave: ${rcre.responseBodyAsString}", rcre)
            Result.failure(rcre)
        }

    }

    private fun getToken(): TexasClient.TokenResponse =
        texasClient.requestToken("fss", "oppgave")
    //TODO er dette rett verdier ?????


}