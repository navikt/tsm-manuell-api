package no.nav.tsm_manuell_api.oppgave.repository

import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgave
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveDTO
import no.nav.tsm_manuell_api.oppgave.model.UlosteOppgave
import no.nav.tsm_manuell_api.utils.objectMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class OppgaveRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {
    fun opprettManuellOppgave(manuellOppgave: ManuellOppgave) {
        val sql =
            """
            INSERT INTO manuelloppgave (
                id, 
                sykmelding, 
                pasientIdent, 
                ferdigstilt, 
                oppgaveid, 
                status, 
                status_timestamp
            ) VALUES (
                :id, 
                :sykmelding::jsonb, 
                :pasientIdent, 
                :ferdigstilt, 
                :oppgaveid, 
                :status, 
                :statusTimestamp
            )
        """
                .trimIndent()

        val params =
            mapOf(
                "id" to manuellOppgave.sykmelding.id,
                "sykmelding" to objectMapper.writeValueAsString(manuellOppgave.sykmelding),
                "pasientIdent" to manuellOppgave.sykmelding.pasient.fnr,
                "ferdigstilt" to manuellOppgave.ferdigstilt,
                "oppgaveid" to manuellOppgave.oppgaveId,
                "status" to manuellOppgave.status?.name,
                "statusTimestamp" to manuellOppgave.statusTimestamp
            )

        namedParameterJdbcTemplate.update(sql, params)
    }

    fun hentManuellOppgaveForSykmeldingId(sykmeldingId: String): ManuellOppgaveDTO? {
        val sql =
            """
            SELECT 
                id,
                sykmelding,
                pasientIdent,
                ferdigstilt,
                oppgaveid,
                status,
                status_timestamp
            FROM manuelloppgave 
            WHERE id = :sykmeldingId
        """
                .trimIndent()

        val params = mapOf("sykmeldingId" to sykmeldingId)

        return namedParameterJdbcTemplate
            .query(sql, params) { rs, _ ->
                val sykmeldingJson = rs.getString("sykmelding")
                val sykmelding =
                    objectMapper.readValue(
                        sykmeldingJson,
                        no.nav.tsm.sykmelding.input.core.model.Sykmelding::class.java
                    )

                ManuellOppgaveDTO(
                    oppgaveid = rs.getObject("oppgaveid") as? Int,
                    sykmelding = sykmelding,
                    ident = rs.getString("pasientIdent"),
                    ferdigstilt = rs.getBoolean("ferdigstilt"),
                    mottattDato = sykmelding.metadata.mottattDato.toString(),
                    status = rs.getString("status"),
                    statusTimestamp =
                        rs.getTimestamp("status_timestamp")?.toLocalDateTime()?.toLocalDate()
                )
            }
            .firstOrNull()
    }

    fun erManuellOppgaveOpprettet(sykmeldingId: String): Boolean {
        val sql =
            """
            SELECT true 
            FROM manuelloppgave 
            WHERE id = :sykmeldingId
        """
                .trimIndent()

        val params = mapOf("sykmeldingId" to sykmeldingId)

        return namedParameterJdbcTemplate
            .query(sql, params) { rs, _ -> rs.getBoolean(1) }
            .firstOrNull()
            ?: false
    }

    fun oppdaterManuellOppgave(sykmeldingId: String, status: String) {
        TODO("IMPLEMENT")
    }

    fun oppdaterManuellOppgaveUtenOpprinneligValidationResult(
        sykmeldingId: String,
        status: String
    ) {
        TODO("IMPLEMENT")
    }

    fun slettOppgave(sykmeldingId: String) {
        TODO("IMPLEMENT")
    }

    fun oppdaterOppgaveHendelse(oppgaveId: String, hendelse: String) {
        TODO("IMPLEMENT")
    }

    fun finnesOppgave(sykmeldingId: String): Boolean {
        TODO("IMPLEMENT")
    }

    fun finnesSykmelding(sykmeldingId: String): Boolean {
        TODO("IMPLEMENT")
    }

    fun hentManuellOppgave(oppgaveId: String): ManuellOppgaveDTO? {
        TODO("IMPLEMENT")
    }

    fun hentKompletteManuellOppgave(oppgaveId: String) {
        TODO("IMPLEMENT")
    }

    fun hentUlosteOppgaver(): List<UlosteOppgave> {
        TODO("IMPLEMENT")
    }
}
