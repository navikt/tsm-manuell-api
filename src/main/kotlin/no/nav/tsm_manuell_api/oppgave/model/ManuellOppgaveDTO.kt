package no.nav.tsm_manuell_api.oppgave.model

import no.nav.tsm.sykmelding.input.core.model.Sykmelding
import no.nav.tsm.sykmelding.input.core.model.ValidationResult

data class ManuellOppgaveDTO(
    val oppgaveId: Int,
    val sykmelding: Sykmelding,
    val ident: String,
    val mottattDato: String,
    val validationResult: ValidationResult,
)
