package no.nav.tsm_manuell_api.oppgave

import no.nav.tsm_manuell_api.metrics.OPPRETT_OPPGAVE_COUNTER
import no.nav.tsm_manuell_api.oppgave.client.GosysOppgaveClient
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service

@Service
class OppgaveService(
    private val gosysOppgaveClient: GosysOppgaveClient,
) {
    val logger = logger()

    fun opprettGosysOppgave(manuellOppgave: ManuellOppgave): GosysOpprettOppgaveResponse  {
        val gosysOpprettOppgave = mapTilOpprettOppgave(manuellOppgave)
        val gosysOppgaveResponse = gosysOppgaveClient.opprettOppgave(gosysOpprettOppgave).fold( { it }) {
            logger.error("Feil ved opprettelse av Gosys oppgave for sykmelding ${manuellOppgave.sykmeldingRecord.sykmelding.id}: ${it.message}")
            throw RuntimeException("Feil ved opprettelse av Gosys oppgave")
        }
            OPPRETT_OPPGAVE_COUNTER.inc()
            logger.info("Opprettet Gosys oppgave med id ${gosysOppgaveResponse.id} for sykmelding ${manuellOppgave.sykmeldingRecord.sykmelding.id}")

        return gosysOppgaveResponse
    }

    fun ferdigstillOppgave(manuellOppgave: ManuellOppgave, enhet: String?, veileder: String?) {
        TODO("Not yet implemented")
    }


    private fun mapTilOpprettOppgave(manuellOppgave: ManuellOppgave): GosysOpprettOppgave {
        TODO("Not yet implemented")
    }






}