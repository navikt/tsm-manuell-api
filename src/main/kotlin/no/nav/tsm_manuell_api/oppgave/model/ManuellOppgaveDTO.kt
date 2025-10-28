package no.nav.tsm_manuell_api.oppgave

data class ManuellOppgaveDTO(
    val oppgaveId: Int,
    val sykmeldingRecord: SykmeldingRecord,
    val ident: String,
    val mottattDato: String,
)
