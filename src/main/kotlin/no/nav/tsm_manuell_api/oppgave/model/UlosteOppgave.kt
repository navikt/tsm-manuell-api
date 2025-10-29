package no.nav.tsm_manuell_api.oppgave.model

import java.time.LocalDateTime

data class UlosteOppgave(
    val oppgaveId: Int,
    val mottattDato: LocalDateTime,
    val status: ManuellOppgaveStatus? = null,
)
