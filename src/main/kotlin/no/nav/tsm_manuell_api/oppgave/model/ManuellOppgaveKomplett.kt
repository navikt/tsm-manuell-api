package no.nav.tsm_manuell_api.oppgave.model

import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm.sykmelding.input.core.model.ValidationResult

data class ManuellOppgaveKomplett(
    val sykmeldingRecord: SykmeldingRecord,
    val oppgaveid: String,
    val ferdigstilt: Boolean,
    val opprinneligValidationResult: ValidationResult?, // is this needed?
) {}
