package no.nav.tsm_manuell_api.security.authentication

import no.nav.tsm_manuell_api.oppgave.repository.OppgaveRepository
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val istilgangskontrollClient: IstilgangskontrollClient,
    private val oppgaveRepository: OppgaveRepository,
) {
    val logger = logger()

    fun hasAccess(oppgaveId: String, accessToken: String): Boolean {

        val ident = oppgaveRepository.finnIdent(oppgaveId.toInt())
        if(ident == null) {
            logger.info("did not find oppgave with id: $oppgaveId")
            return false
        }
        val tilgang = istilgangskontrollClient.sjekkVeiledersTilgang(accessToken = accessToken, ident = ident)
            .getOrElse {
                logger.info("Failed to check access for oppgave with id: $oppgaveId")
                return false
            }
        return tilgang.erGodkjent

    }


}