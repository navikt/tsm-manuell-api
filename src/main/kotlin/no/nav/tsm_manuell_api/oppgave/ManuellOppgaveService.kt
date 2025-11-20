package no.nav.tsm_manuell_api.oppgave

import java.time.LocalDateTime
import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm_manuell_api.oppgave.client.ISyfoSmManuellClient
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgave
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveStatus
import no.nav.tsm_manuell_api.oppgave.repository.OppgaveRepository
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service

@Service
class ManuellOppgaveService(
    private val oppgaveRepository: OppgaveRepository,
    private val oppgaveService: GosysOppgaveService,
    private val syfoSmManuellClient: ISyfoSmManuellClient,
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
        logger.info(
            "Sykmelding har blitt behandlet tidligere og er OK, Sletter oppgave med sykmeldingId $sykmeldingId"
        )
        oppgaveRepository.slettOppgave(sykmeldingId)
    }

    fun isOpprettetManuellOppgave(sykmeldingId: String): Boolean {
        TODO("IMPLEMENT")
    }

    fun erManuellOppgaveOpprettet(sykmeldingId: String): Boolean {
        return oppgaveRepository.erManuellOppgaveOpprettet(sykmeldingId)
    }

    fun temporaryLagreManuellOppgave(sykmeldingRecord: SykmeldingRecord) {
        val syfosmManuellOppgave =
            syfoSmManuellClient.hentOppgaveId(sykmeldingRecord.sykmelding.id).getOrElse { null }

        val manuellOppgave =
            ManuellOppgave(
                sykmelding = sykmeldingRecord.sykmelding,
                ferdigstilt = false,
                oppgaveId = syfosmManuellOppgave?.oppgaveId,
                status = ManuellOppgaveStatus.APEN,
                statusTimestamp = LocalDateTime.from(sykmeldingRecord.validation.timestamp)
            )
        oppgaveRepository.opprettManuellOppgave(manuellOppgave)
        logger.info(
            "Manuell oppgave lagret i databasen med sykmeldingId${manuellOppgave.sykmelding.id} og oppgaveId ${manuellOppgave.oppgaveId}"
        )
    }
    //    fun lagreManuellOppgave(
    //        sykmeldingRecord: SykmeldingRecord,
    //        gosysOppgave: GosysOpprettOppgaveResponse
    //    ) {
    //        val manuellOppgave = mapToManuellOppgave(sykmeldingRecord, gosysOppgave)
    //        oppgaveRepository.opprettManuellOppgave(manuellOppgave)
    //        logger.info(
    //            "Manuell oppgave lagret i databasen med sykmeldingId
    // ${manuellOppgave.sykmelding.id} og oppgaveId ${manuellOppgave.oppgaveId}"
    //        )
    //    }

    //    private fun mapToManuellOppgave(
    //        sykmeldingRecord: SykmeldingRecord,
    //        oppgave: GosysOpprettOppgaveResponse
    //    ): ManuellOppgave {
    //        return ManuellOppgave(
    //            sykmelding = sykmeldingRecord.sykmelding,
    //            ferdigstilt = false,
    //            oppgaveId = oppgave.id,
    //            status = statusMap[oppgave.status] ?: ManuellOppgaveStatus.APEN,
    //            statusTimestamp = oppgave.endretTidspunkt?.toLocalDateTime() ?:
    // LocalDateTime.from(sykmeldingRecord.validation.timestamp)
    //        )
    //    }
}
