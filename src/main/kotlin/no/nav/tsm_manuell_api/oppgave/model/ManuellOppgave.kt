package no.nav.tsm_manuell_api.oppgave.model

import java.time.LocalDateTime
import no.nav.tsm.sykmelding.input.core.model.Sykmelding

data class ManuellOppgave(
    val sykmelding: Sykmelding,
    val ferdigstilt: Boolean = false,
    val oppgaveId: Int? = null,
    val status: ManuellOppgaveStatus? = null,
    val statusTimestamp: LocalDateTime,
)

enum class ManuellOppgaveStatus {
    APEN,
    FERDIGSTILT,
    FEILREGISTRERT,
    DELETED,
}
