package no.nav.tsm_manuell_api.oppgave.client

import no.nav.tsm_manuell_api.oppgave.model.GosysOpprettOppgave
import no.nav.tsm_manuell_api.oppgave.model.GosysOpprettOppgaveResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local", "test")
@Component
class MockGosysOppgaveClient : IGosysOppgaveClient {
    override fun opprettOppgave(
        opprettOppgave: GosysOpprettOppgave
    ): Result<GosysOpprettOppgaveResponse> {

        return Result.success(
            GosysOpprettOppgaveResponse(
                id = 123,
                versjon = 1,
                status = null,
                tildeltEnhetsnr = null,
                mappeId = null,
                endretTidspunkt = null,
                beskrivelse = null,
                fristFerdigstillelse = null
            )
        )
    }
}
