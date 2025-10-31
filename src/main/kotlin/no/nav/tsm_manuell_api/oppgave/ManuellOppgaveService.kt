package no.nav.tsm_manuell_api.oppgave

import no.nav.tsm_manuell_api.oppgave.repository.OppgaveRepository
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service

@Service
class ManuellOppgaveService(
    private val oppgaveRepository: OppgaveRepository,
    private val oppgaveService: OppgaveService,
) {
    val logger = logger()

    fun slettOppgave(sykmeldingId: String) {
        val manuellOppgave = oppgaveRepository.hentManuellOppgaveForSykmeldingId(sykmeldingId)

        manuellOppgave?.let {
            if (!it.ferdigstilt) {
                oppgaveService.ferdigstillOppgave(
                    manuellOppgave = it,
                    enhet = null,
                    veileder = null,
                )
            }
            val antallSlettedeOppgaver = oppgaveRepository.slettOppgave(it.oppgaveid)
            logger.info("Slettet $antallSlettedeOppgaver oppgaver")
        }
    }

    fun isOpprettetManuellOppgave(sykmeldingId: String): Boolean {
        TODO("IMPLEMENT")
    }
}
