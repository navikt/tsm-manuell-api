package no.nav.tsm_manuell_api.sykmelding

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tsm.sykmelding.input.core.model.Rule
import no.nav.tsm.sykmelding.input.core.model.RuleType
import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm_manuell_api.metrics.INCOMING_MESSAGE_COUNTER
import no.nav.tsm_manuell_api.metrics.MESSAGE_STORED_IN_DB_COUNTER
import no.nav.tsm_manuell_api.oppgave.GosysOppgaveService
import no.nav.tsm_manuell_api.oppgave.ManuellOppgaveService
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
            logger.info(
                "Sykmelding mottatt for manuell behandling, sykmeldingId=$sykmeldingId . Sjekker om den er behandlet tidligere..."
            )

            val person = finnAktorId(sykmeldingRecord) ?: return
            val validationRules = sykmeldingRecord.validation.rules
            if (containsPending(validationRules) && !containsOk(validationRules)) {
                handleOpprettManuellOppgave(sykmeldingRecord, metadata, person)
            } else if (containsOk(validationRules)) {
                logger.info(
                    "Sykmelding med id: $sykmeldingId inneholder nå RuleType.OK, er dermed behandlet manuelt tidligere. Sletter eventuell manuell oppgave."
                )
                manuellOppgaveService.slettOppgave(sykmeldingId)
            } else {
                logger.info(
                    "Sykmelding med id: $sykmeldingId inneholder ikke RuleType.PENDING eller inneholder RuleType.OK, hopper over manuell behandling"
                )
            }
        } else {
            logger.info("Mottatt tombstone for sykmelding med id $sykmeldingId")
            manuellOppgaveService.slettOppgave(sykmeldingId)
        }
    }

    private fun handleOpprettManuellOppgave(
        sykmeldingRecord: SykmeldingRecord,
        metadata: Map<String, ByteArray>,
        person: Person
    ) {
        val sykmeldingId = sykmeldingRecord.sykmelding.id
        logger.info("Mottatt en sykmelding $sykmeldingId der det skal opprettes manuell oppgave")
        INCOMING_MESSAGE_COUNTER.inc()

        if (manuellOppgaveService.erManuellOppgaveOpprettet(sykmeldingId)) {
            logger.warn(
                "Manuell oppgave med sykmeldingId $sykmeldingId er allerede opprettet i databasen."
            )
        } else {
            val gosysOppgave =
                gosysOppgaveService.opprettGosysOppgave(sykmeldingRecord, person.aktoerId)
            manuellOppgaveService.opprettManuellOppgave(sykmeldingRecord, gosysOppgave)

            // manuellOppgaveService.sendSykmeldingRecord()
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

    fun containsOk(rules: List<Rule>): Boolean = rules.any { it.type == RuleType.OK }
}
