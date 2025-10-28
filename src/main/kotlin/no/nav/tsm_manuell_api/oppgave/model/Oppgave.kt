package no.nav.tsm_manuell_api.oppgave.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class GosysOpprettOppgave(
    val tildeltEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val aktoerId: String? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val tema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val aktivDato: LocalDate,
    val fristFerdigstillelse: LocalDate? = null,
    val prioritet: String,
)

data class FerdigstillOppgave(
    val versjon: Int,
    val id: Int,
    val status: OppgaveStatus,
    val tildeltEnhetsnr: String?,
    val tilordnetRessurs: String?,
    val mappeId: Int?,
)

data class EndreOppgave(
    val versjon: Int,
    val id: Int,
    val beskrivelse: String,
    val fristFerdigstillelse: LocalDate,
    val mappeId: Int?,
    val mappeNavn: String,
    val tildeltEnhetsnr: String,
    val tilordnetRessurs: String? = null,
)

data class GjenopprettOppgave(
    val versjon: Int,
    val beskrivelse: String,
    val id: Int,
    val fristFerdigstillelse: LocalDate,
    val mappeId: Int?,
    val tildeltEnhetsnr: String?,
    val status: OppgaveStatus
)

data class GosysOpprettOppgaveResponse(
    val id: Int,
    val versjon: Int,
    val status: String? = null,
    val tildeltEnhetsnr: String? = null,
    val mappeId: Int? = null,
    val endretTidspunkt: ZonedDateTime? = null,
    val beskrivelse: String? = null,
    val fristFerdigstillelse: LocalDate? = null,
)

enum class OppgaveStatus(val status: String) {
    OPPRETTET("OPPRETTET"),
    AAPNET("AAPNET"),
    UNDER_BEHANDLING("UNDER_BEHANDLING"),
    FERDIGSTILT("FERDIGSTILT"),
    FEILREGISTRERT("FEILREGISTRERT"),
}
