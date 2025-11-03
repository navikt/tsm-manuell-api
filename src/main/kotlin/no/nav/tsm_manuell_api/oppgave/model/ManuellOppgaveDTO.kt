package no.nav.tsm_manuell_api.oppgave.model

import no.nav.tsm.sykmelding.input.core.model.Sykmelding
import no.nav.tsm.sykmelding.input.core.model.ValidationResult
import java.time.LocalDate

data class ManuellOppgaveDTO(
    val oppgaveid: Int?,
    val sykmelding: Sykmelding,
    val ident: String,
    val ferdigstilt: Boolean,
    val mottattDato: String,
    val status: String?,
    val statusTimestamp: LocalDate?,
)
