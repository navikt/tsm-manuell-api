package no.nav.tsm_manuell_api.security.authentication

import no.nav.tsm_manuell_api.oppgave.repository.OppgaveRepository
import no.nav.tsm_manuell_api.utils.IAuditLoggingService
import no.nav.tsm_manuell_api.utils.LoggerUtils
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val istilgangskontrollClient: IIstilgangskontrollClient,
    private val oppgaveRepository: OppgaveRepository,
    private val auditLoggingService: IAuditLoggingService
) {
    val logger = logger()

    fun hasAccess(oppgaveId: String, accessToken: String, path: String): Boolean {
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
                    auditLoggingService.missingPermitLogMessage(
                        accessToken,
                        path,
                        LoggerUtils.Operation.READ
                    )
                    return false
                }
        auditLoggingService.hasReadPermitLogMessage(ident, accessToken)
        return tilgang.erGodkjent
    }

    fun hentRettleiarIdent(oppgaveId: String): String {
        val jwt =
            (SecurityContextHolder.getContext().authentication?.principal as? Jwt)
                ?: throw IllegalStateException(
                    "Kunne ikkje hente JWT fra SecurityContext ved forsøk på å ferdigstille oppgåve: $oppgaveId"
                )

        return jwt.getClaim<String>("NAVident")
            ?: throw IllegalStateException(
                "NAVident claim manglar i accessToken ved forsøk på å ferdigstille oppgåve: $oppgaveId"
            )
    }
}
