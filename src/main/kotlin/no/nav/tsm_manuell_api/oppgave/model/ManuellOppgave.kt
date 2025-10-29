package no.nav.tsm_manuell_api.oppgave.model

import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord

data class ManuellOppgave(
    val sykmeldingRecord: SykmeldingRecord,
)

enum class ManuellOppgaveStatus {
    APEN,
    FERDIGSTILT,
    FEILREGISTRERT,
    DELETED,
}
