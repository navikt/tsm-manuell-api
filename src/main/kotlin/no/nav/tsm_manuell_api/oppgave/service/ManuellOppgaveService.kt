package no.nav.tsm_manuell_api.oppgave.service

import java.time.LocalDateTime
import no.nav.tsm.sykmelding.input.core.model.SykmeldingRecord
import no.nav.tsm_manuell_api.oppgave.client.ISyfoSmManuellClient
import no.nav.tsm_manuell_api.oppgave.model.*
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

    fun hentOppgave(oppgaveId: String): Result<ManuellOppgaveResponse> {
        val dto =
            oppgaveRepository.hentManuellOppgave(oppgaveId)
                ?: return Result.failure(Exception("Fant ikke oppgave med id $oppgaveId"))

        val manuellOppgaveResponse = mapToManuellOppgaveResponse(dto)
        return Result.success(manuellOppgaveResponse)
    }

    private fun mapToManuellOppgaveResponse(dto: ManuellOppgaveDTO): ManuellOppgaveResponse {
        val oppgaveId = dto.oppgaveid
        requireNotNull(oppgaveId)
        return ManuellOppgaveResponse(
            oppgaveId = oppgaveId,
            sykmelding = dto.sykmelding,
            ident = dto.ident,
            ferdigstilt = dto.ferdigstilt,
            mottattDato = dto.mottattDato,
            status = dto.status,
            statusTimestamp = dto.statusTimestamp,
        )
    }

    fun hentUlosteOppgaver(): List<UlosteOppgave> {
        val ulosteOppgaver = oppgaveRepository.hentUlosteOppgaver()

        return ulosteOppgaver
    }

    fun hentOppgaveBySykmeldingId(sykmeldingId: String): Result<ManuellOppgaveIds> {
        val dto =
            oppgaveRepository.hentManuellOppgaveForSykmeldingId(sykmeldingId)
                ?: return Result.failure(
                    Exception("Fant ingen oppgåver med sykmeldingId $sykmeldingId")
                )
        val res = mapTomanuellOppgaveIdsResponse(dto)
        return Result.success(res)
    }

    private fun mapTomanuellOppgaveIdsResponse(dto: ManuellOppgaveDTO): ManuellOppgaveIds {
        val oppgaveId = dto.oppgaveid
        requireNotNull(oppgaveId)

        return ManuellOppgaveIds(
            oppgaveId = oppgaveId,
            sykmeldingId = dto.sykmelding.id,
        )
    }

    fun finnesOppgave(oppgaveId: String): Boolean {
        return oppgaveRepository.finnesOppgave(oppgaveId)
    }

    fun ferdigstillManuellBehandling(
        oppgaveId: String,
        navEining: String,
        rettleiar: String,
        accessToken: String,
        merknadar: List<Merknad>?
    ) {
        TODO("IMPLEMENT")
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
