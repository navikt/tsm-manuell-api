package no.nav.tsm_manuell_api.sykmelding

import com.fasterxml.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import no.nav.tsm_manuell_api.metrics.INCOMING_MESSAGE_COUNTER
import no.nav.tsm_manuell_api.metrics.MESSAGE_STORED_IN_DB_COUNTER
import no.nav.tsm_manuell_api.oppgave.ManuellOppgaveService
import no.nav.tsm_manuell_api.oppgave.OppgaveService
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgave
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveStatus
import no.nav.tsm_manuell_api.utils.logger
import no.nav.tsm_manuell_api.utils.objectMapper
import org.springframework.stereotype.Service

@Service
class MottattSykmeldingService(
    private val manuellOppgaveService: ManuellOppgaveService,
    private val oppgaveService: OppgaveService,
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

    suspend fun handleMottattSykmelding(
        sykmeldingId: String,
        sykmeldingRecordValue: String?,
        metadata: Map<String, ByteArray>
    ) {

        if (sykmeldingRecordValue == null) {
            logger.info("Mottatt tombstone for sykmelding med id $sykmeldingId")
            manuellOppgaveService.slettOppgave(sykmeldingId)
        } else {
            val manuellOppgave: ManuellOppgave = objectMapper.readValue(sykmeldingRecordValue)
            logger.info("Sykmelding mottatt for manuell behandling, sykmeldingId=$sykmeldingId")

            handleOpprettManuellOppgave(manuellOppgave, metadata)
        }
    }

    private fun handleOpprettManuellOppgave(
        manuellOppgave: ManuellOppgave,
        metadata: Map<String, ByteArray>
    ) {
        val sykmeldingId = manuellOppgave.sykmeldingRecord.sykmelding.id
        logger.info("Mottatt en manuell oppgave $sykmeldingId")
        INCOMING_MESSAGE_COUNTER.inc()

        if (oppgaveService.erOppgaveOpprettet(sykmeldingId)) {
            logger.warn(
                "Manuell oppgave med sykmeldingId $sykmeldingId er allerede opprettet i databasen."
            )
        } else {
            val oppgave = oppgaveService.opprettGosysOppgave(manuellOppgave)
            val status = statusMap[oppgave.status] ?: ManuellOppgaveStatus.APEN
            val statusTimestamp = oppgave.endretTidspunkt?.toLocalDateTime() ?: LocalDateTime.now()
            oppgaveService.opprettManuellOppgave(
                manuellOppgave,
                oppgave.id,
                status,
                statusTimestamp
            )
            logger.info(
                "Manuell oppgave lagret i databasen med sykmeldingId $sykmeldingId og oppgaveId ${oppgave.id}"
            )

            // manuellOppgaveService.sendSykmeldingRecord()
            MESSAGE_STORED_IN_DB_COUNTER.inc()
        }
    }
}
