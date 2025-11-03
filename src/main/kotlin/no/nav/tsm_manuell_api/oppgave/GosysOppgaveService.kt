package no.nav.tsm_manuell_api.oppgave

import no.nav.tsm.sykmelding.input.core.model.Aktivitet
import no.nav.tsm.sykmelding.input.core.model.Sykmelding
import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm_manuell_api.metrics.OPPRETT_OPPGAVE_COUNTER
import no.nav.tsm_manuell_api.oppgave.client.GosysOppgaveClient
import no.nav.tsm_manuell_api.oppgave.model.GosysOpprettOppgave
import no.nav.tsm_manuell_api.oppgave.model.GosysOpprettOppgaveResponse
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveKomplett
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class GosysOppgaveService(
    private val gosysOppgaveClient: GosysOppgaveClient,
) {
    val logger = logger()

    fun opprettGosysOppgave(sykmeldingRecord: SykmeldingRecord): GosysOpprettOppgaveResponse {
        val gosysOpprettOppgave = mapTilOpprettOppgave(sykmeldingRecord)
        val gosysOppgaveResponse =
            gosysOppgaveClient.opprettOppgave(gosysOpprettOppgave).fold({ it }) {
                logger.error(
                    "Feil ved opprettelse av Gosys oppgave for sykmelding ${sykmeldingRecord.sykmelding.id}: ${it.message}"
                )
                throw RuntimeException("Feil ved opprettelse av Gosys oppgave")
            }
        OPPRETT_OPPGAVE_COUNTER.inc()
        logger.info(
            "Opprettet Gosys oppgave med id ${gosysOppgaveResponse.id} for sykmelding ${sykmeldingRecord.sykmelding.id}"
        )

        return gosysOppgaveResponse
    }

    fun ferdigstillOppgave(
        manuellOppgave: ManuellOppgaveKomplett,
        enhet: String?,
        veileder: String?
    ) {
        TODO("Not yet implemented")
    }

    private fun mapTilOpprettOppgave(sykmeldingRecord: SykmeldingRecord): GosysOpprettOppgave {
        return GosysOpprettOppgave(
            opprettetAvEnhetsnr = "9999",
            aktoerId = sykmeldingRecord.sykmelding.pasient.fnr, //er dette aktoerId?
            behandlesAvApplikasjon = "SMM",
            beskrivelse = "Manuell vurdering av sykmelding for periode: ${getFomTomTekst(sykmeldingRecord.sykmelding)}",
            tema = "SYM",
            oppgavetype = "BEH_EL_SYM",
            behandlingstype = "ae0239",
            aktivDato = LocalDate.now(),
            fristFerdigstillelse = omTreUkedager(LocalDate.now()),
            prioritet = "HOY",
        )
    }

    fun omTreUkedager(idag: LocalDate): LocalDate =
        when (idag.dayOfWeek) {
            DayOfWeek.SUNDAY -> idag.plusDays(4)
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY -> idag.plusDays(3)
            else -> idag.plusDays(5)
        }

    private fun getFomTomTekst(sykmelding: Sykmelding): String {
        val aktiviteter = sykmelding.aktivitet
        return "${formaterDato(aktiviteter.sortedAktivitetFOMDate().first().fom)} - " +
            formaterDato(aktiviteter.sortedAktivitetTOMDate().last().tom)
    }

    private fun List<Aktivitet>.sortedAktivitetFOMDate(): List<Aktivitet> = sortedBy { it.fom }

    private fun List<Aktivitet>.sortedAktivitetTOMDate(): List<Aktivitet> = sortedBy { it.tom }

    private fun formaterDato(dato: LocalDate): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return dato.format(formatter)
    }
}
