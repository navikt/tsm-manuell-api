package no.nav.tsm_manuell_api.security.authentication

import no.nav.tsm_manuell_api.oppgave.repository.OppgaveRepository
import no.nav.tsm_manuell_api.utils.AuditLoggingService
import no.nav.tsm_manuell_api.utils.IAuditLoggingService
import no.nav.tsm_manuell_api.utils.auditLogger
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val istilgangskontrollClient: IIstilgangskontrollClient,
    private val oppgaveRepository: OppgaveRepository,
    private val auditLoggingService: IAuditLoggingService
) {
    val logger = logger()

    fun hasAccess(oppgaveId: String, accessToken: String): Boolean {
        val id = oppgaveId.toIntOrNull() ?: return false
        val ident = oppgaveRepository.finnIdent(id)
        if (ident == null) {
            logger.info("Fant ikkje oppgåve med oppgaveId: $id")
            return false
        }
        val tilgang =
            istilgangskontrollClient
                .sjekkVeiledersTilgang(accessToken = accessToken, ident = ident)
                .getOrElse {
                    logger.info("Feila tilgangssjekk for oppgåve --- oppgaveId: $id")
                    auditLoggingService.missingReadPermitLogMessage(accessToken)
                    return false
                }
        auditLoggingService.hasReadPermitLogMessage(ident, accessToken)
        return tilgang.erGodkjent
    }
}
