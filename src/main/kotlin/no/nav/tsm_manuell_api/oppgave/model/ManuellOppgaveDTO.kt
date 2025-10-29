package no.nav.tsm_manuell_api.oppgave.model

import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord

data class ManuellOppgaveDTO(
    val oppgaveId: Int,
    val sykmeldingRecord: SykmeldingRecord,
    val ident: String,
    val mottattDato: String,
)
