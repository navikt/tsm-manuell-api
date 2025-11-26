package no.nav.tsm_manuell_api.sykmelding

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.sykmelding.input.core.model.Rule
import no.nav.tsm.sykmelding.input.core.model.RuleType
import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm_manuell_api.metrics.INCOMING_MESSAGE_COUNTER
import no.nav.tsm_manuell_api.metrics.MESSAGE_STORED_IN_DB_COUNTER
import no.nav.tsm_manuell_api.metrics.SYKMELDING_COUNT_STATUS
import no.nav.tsm_manuell_api.oppgave.service.GosysOppgaveService
import no.nav.tsm_manuell_api.oppgave.service.ManuellOppgaveService
import no.nav.tsm_manuell_api.person.Person
import no.nav.tsm_manuell_api.person.PersonService
import no.nav.tsm_manuell_api.utils.logger
import no.nav.tsm_manuell_api.utils.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MottattSykmeldingService(
    private val manuellOppgaveService: ManuellOppgaveService,
    private val gosysOppgaveService: GosysOppgaveService,
    private val personService: PersonService,
    @param:Value($$"${nais.cluster}") private val clusterName: String,
) {
    val logger = logger()

    fun handleMottattSykmelding(
        sykmeldingId: String,
        sykmeldingRecordValue: ByteArray?,
        metadata: Map<String, ByteArray>
    ) {

        if (sykmeldingRecordValue != null) {
            val sykmeldingRecord: SykmeldingRecord = objectMapper.readValue(sykmeldingRecordValue)

            val validationRules = sykmeldingRecord.validation.rules
            val latestStatus = sykmeldingRecord.validation.status

            if (latestStatus == RuleType.PENDING) {
                // val person = finnAktorId(sykmeldingRecord) ?: return
                handleOpprettManuellOppgave(sykmeldingRecord, metadata, null)
                SYKMELDING_COUNT_STATUS.labels(latestStatus.name.lowercase()).inc()
            } else if (containsPending(validationRules)) {
                logger.info(
                    "Sykmelding med id: $sykmeldingId har nå status $latestStatus, er dermed behandlet manuelt tidligere. Sletter eventuell manuell oppgave."
                )
                SYKMELDING_COUNT_STATUS.labels("pending_${latestStatus.name.lowercase()}").inc()
                manuellOppgaveService.slettOppgave(sykmeldingId)
            } else {
                SYKMELDING_COUNT_STATUS.labels(latestStatus.name.lowercase()).inc()
            }
        } else {
            logger.info("Mottatt tombstone for sykmelding med id $sykmeldingId")
            manuellOppgaveService.slettOppgave(sykmeldingId)
            SYKMELDING_COUNT_STATUS.labels("deleted").inc()
        }
    }

    private fun handleOpprettManuellOppgave(
        sykmeldingRecord: SykmeldingRecord,
        metadata: Map<String, ByteArray>,
        person: Person?
    ) {
        val sykmeldingId = sykmeldingRecord.sykmelding.id
        logger.info("Mottatt en sykmelding $sykmeldingId der det skal opprettes manuell oppgave")
        INCOMING_MESSAGE_COUNTER.inc()

        if (manuellOppgaveService.erManuellOppgaveOpprettet(sykmeldingId)) {
            logger.warn(
                "Manuell oppgave med sykmeldingId $sykmeldingId er allerede opprettet i databasen."
            )
        } else {
            // **IMPORTANT **Temporarily disable create gosys oppgave until we are ready to put this
            // into production and turn off Syfossmmanuell. This avoids us having two apps creating
            // gosys oppgave. - DO NOT DELETE
            //            val gosysOppgave =
            //                gosysOppgaveService.opprettGosysOppgave(sykmeldingRecord,
            // person.aktoerId)

            //            manuellOppgaveService.lagreManuellOppgave(sykmeldingRecord, gosysOppgave)
            manuellOppgaveService.temporaryLagreManuellOppgave(sykmeldingRecord)

            //             manuellOppgaveService.sendSykmeldingRecord()
            MESSAGE_STORED_IN_DB_COUNTER.inc()
        }
    }

    fun finnAktorId(sykmeldingRecord: SykmeldingRecord): Person? {
        val person: Person =
            personService.getPersonMedAktoerId(sykmeldingRecord.sykmelding.pasient.fnr).getOrElse {
                if (clusterName == "dev-gcp") {
                    logger.warn(
                        "Person ikke funnet i PDL for sykmelding ${sykmeldingRecord.sykmelding.id}, hopper over..."
                    )
                    return null
                }
                logger.error(
                    "Person ikke funnet i PDL for sykmelding ${sykmeldingRecord.sykmelding.id} "
                )
                throw it
            }
        return person
    }

    fun containsPending(rules: List<Rule>): Boolean = rules.any { it.type == RuleType.PENDING }

}
