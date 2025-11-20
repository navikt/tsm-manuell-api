package no.nav.tsm_manuell_api.oppgave.client

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local", "test")
@Component
class MockSyfoSmManuellClient : ISyfoSmManuellClient {
    override fun hentOppgaveId(sykmeldingId: String): Result<SmmOppgaveResponse> {
        return Result.success(SmmOppgaveResponse(oppgaveId = 456, sykmeldingId = sykmeldingId))
    }
}
