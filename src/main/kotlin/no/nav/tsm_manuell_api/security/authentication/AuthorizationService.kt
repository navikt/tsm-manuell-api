package no.nav.tsm_manuell_api.security.authentication

import no.nav.tsm_manuell_api.oppgave.repository.OppgaveRepository
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val istilgangskontrollClient: IIstilgangskontrollClient,
    private val oppgaveRepository: OppgaveRepository,
) {
    val logger = logger()

    fun hasAccess(oppgaveId: String, accessToken: String): Boolean {

        val id = oppgaveId.toIntOrNull() ?: return false
        val ident = oppgaveRepository.finnIdent(id)
        if (ident == null) {
            logger.info("did not find oppgave with oppgaveId: $id")
            return false
        }
        val tilgang =
            istilgangskontrollClient
                .sjekkVeiledersTilgang(accessToken = accessToken, ident = ident)
                .getOrElse {
                    logger.info("Failed to check access for oppgave with oppgaveId: $id")
                    return false
                }
        return tilgang.erGodkjent
    }
}
