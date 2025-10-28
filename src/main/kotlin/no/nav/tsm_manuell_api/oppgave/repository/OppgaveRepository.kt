package no.nav.tsm_manuell_api.oppgave.repository

import no.nav.tsm_manuell_api.oppgave.ManuellOppgaveKomplett
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Repository
@Transactional
class OppgaveRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {
    fun opprettManuellOppgave(sykmeldingId: String) {
        TODO("IMPLEMENT")
    }
    fun hentManuellOppgaveForSykmeldingId(sykmeldingId: String): ManuellOppgaveKomplett? {
        TODO("IMPLEMENT")
    }
    fun erManuellOppgaveOpprettet(sykmeldingId: String): Boolean {
        TODO("IMPLEMENT")
    }
    fun oppdaterManuellOppgave(sykmeldingId: String, status: String) {
        TODO("IMPLEMENT")
    }

    fun oppdaterManuellOppgaveUtenOpprinneligValidationResult(sykmeldingId: String, status: String) {
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
