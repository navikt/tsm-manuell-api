package no.nav.tsm_manuell_api.oppgave

import java.time.LocalDateTime
import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm_manuell_api.oppgave.model.GosysOpprettOppgaveResponse
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgave
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveStatus
import no.nav.tsm_manuell_api.oppgave.repository.OppgaveRepository
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service

@Service
class ManuellOppgaveService(
    private val oppgaveRepository: OppgaveRepository,
    private val oppgaveService: GosysOppgaveService,
) {
    val logger = logger()

    companion object {
        private val statusMap =
            mapOf(
                "FERDIGSTILT" to ManuellOppgaveStatus.FERDIGSTILT,
                "FEILREGISTRERT" to ManuellOppgaveStatus.FEILREGISTRERT,
                null to ManuellOppgaveStatus.DELETED,
            )
    }

    fun slettOppgave(sykmeldingId: String) {
        val manuellOppgave = oppgaveRepository.hentManuellOppgaveForSykmeldingId(sykmeldingId)

        manuellOppgave?.let {
            // TODO: ferdigstillOppgave is not yet implemented for ManuellOppgaveDTO
            // if (!it.ferdigstilt) {
            //     oppgaveService.ferdigstillOppgave(
            //         manuellOppgave = it,
            //         enhet = null,
            //         veileder = null,
            //     )
            // }
            val oppgaveIdString =
                it.oppgaveid?.toString() ?: it.oppgaveid?.toString() ?: sykmeldingId
            val antallSlettedeOppgaver = oppgaveRepository.slettOppgave(oppgaveIdString)
            logger.info("Slettet $antallSlettedeOppgaver oppgaver")
        }
    }

    fun isOpprettetManuellOppgave(sykmeldingId: String): Boolean {
        TODO("IMPLEMENT")
    }

    fun erManuellOppgaveOpprettet(sykmeldingId: String): Boolean {
        return oppgaveRepository.erManuellOppgaveOpprettet(sykmeldingId)
    }

    fun opprettManuellOppgave(
        sykmeldingRecord: SykmeldingRecord,
        gosysOppgave: GosysOpprettOppgaveResponse
    ) {
        val manuellOppgave = mapToManuellOppgave(sykmeldingRecord, gosysOppgave)
        oppgaveRepository.opprettManuellOppgave(manuellOppgave)
        logger.info(
            "Manuell oppgave lagret i databasen med sykmeldingId ${manuellOppgave.sykmelding.id} og oppgaveId ${manuellOppgave.oppgaveId}"
        )
    }

    private fun mapToManuellOppgave(
        sykmeldingRecord: SykmeldingRecord,
        oppgave: GosysOpprettOppgaveResponse
    ): ManuellOppgave {
        return ManuellOppgave(
            sykmelding = sykmeldingRecord.sykmelding,
            ferdigstilt = false,
            oppgaveId = oppgave.id,
            status = statusMap[oppgave.status] ?: ManuellOppgaveStatus.APEN,
            statusTimestamp = oppgave.endretTidspunkt?.toLocalDateTime() ?: LocalDateTime.now()
        )
    }
}
