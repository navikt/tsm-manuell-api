package no.nav.tsm_manuell_api.sykmelding

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.sykmelding.input.core.model.RuleType
import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm_manuell_api.metrics.INCOMING_MESSAGE_COUNTER
import no.nav.tsm_manuell_api.metrics.MESSAGE_STORED_IN_DB_COUNTER
import no.nav.tsm_manuell_api.oppgave.ManuellOppgaveService
import no.nav.tsm_manuell_api.oppgave.OppgaveService
import no.nav.tsm_manuell_api.utils.logger
import no.nav.tsm_manuell_api.utils.objectMapper
import org.springframework.stereotype.Service

@Service
class MottattSykmeldingService(
    private val manuellOppgaveService: ManuellOppgaveService,
    private val oppgaveService: OppgaveService,
) {
    val logger = logger()

    suspend fun handleMottattSykmelding(
        sykmeldingId: String,
        sykmeldingRecordValue: String?,
        metadata: Map<String, ByteArray>
    ) {
        // TODO her må vi vel sjekke om det er noko vi skal ta med vidare eller ikkje... er den
        // pending etc - det som var den gamle merknaden
        if (sykmeldingRecordValue == null) {
            logger.info("Mottatt tombstone for sykmelding med id $sykmeldingId")
            manuellOppgaveService.slettOppgave(sykmeldingId)
        } else {
            val sykmeldingRecord: SykmeldingRecord = objectMapper.readValue(sykmeldingRecordValue)
            logger.info("Sykmelding mottatt for manuell behandling, sykmeldingId=$sykmeldingId")

            val containsPending =
                sykmeldingRecord.validation.rules.any { it.type == RuleType.PENDING }
            val containsOk = sykmeldingRecord.validation.rules.any { it.type == RuleType.OK }

            if (containsPending && !containsOk) {
                handleOpprettManuellOppgave(sykmeldingRecord, metadata)
            } else if (containsOk) {
                logger.info(
                    "Sykmelding med id: $sykmeldingId inneholder RuleType.OK, er dermed behandlet manuelt tidligere. Sletter eventuell manuell oppgave."
                )
                manuellOppgaveService.slettOppgave(sykmeldingId)
            } else {
                logger.info(
                    "Sykmelding med id: $sykmeldingId inneholder ikke RuleType.PENDING eller inneholder RuleType.OK, hopper over manuell behandling"
                )
            }
        }
    }

    private fun handleOpprettManuellOppgave(
        sykmeldingRecord: SykmeldingRecord,
        metadata: Map<String, ByteArray>
    ) {
        val sykmeldingId = sykmeldingRecord.sykmelding.id
        logger.info("Mottatt en manuell oppgave $sykmeldingId")
        INCOMING_MESSAGE_COUNTER.inc()

        if (oppgaveService.erOppgaveOpprettet(sykmeldingId)) {
            logger.warn(
                "Manuell oppgave med sykmeldingId $sykmeldingId er allerede opprettet i databasen."
            )
        } else {
            val gosysOppgave = oppgaveService.opprettGosysOppgave(sykmeldingRecord)
            oppgaveService.opprettManuellOppgave(sykmeldingRecord, gosysOppgave)

            // manuellOppgaveService.sendSykmeldingRecord()
            MESSAGE_STORED_IN_DB_COUNTER.inc()
        }
    }
}
