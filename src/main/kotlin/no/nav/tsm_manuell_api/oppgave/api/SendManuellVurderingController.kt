package no.nav.tsm_manuell_api.oppgave.api

import no.nav.tsm_manuell_api.oppgave.model.SaksbehandlersVurdering
import no.nav.tsm_manuell_api.oppgave.service.ManuellOppgaveService
import no.nav.tsm_manuell_api.security.authentication.AuthorizationService
import no.nav.tsm_manuell_api.utils.LoggerUtils
import no.nav.tsm_manuell_api.utils.auditLogger
import no.nav.tsm_manuell_api.utils.logger
import no.nav.tsm_manuell_api.utils.teamLogger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vurderingmanuelloppgave")
class SendManuellVurderingController(
    private val authorizationService: AuthorizationService,
    private val manuellOppgaveService: ManuellOppgaveService,
    private val loggerUtils: LoggerUtils,
) {

    val logger = logger()
    val teamLogger = teamLogger()
    val auditLogger = auditLogger()

    @PostMapping("/{oppgaveid}")
    fun sendOppgaveTilManuellVurdering(
        @PathVariable oppgaveId: String,
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("X-Nav-Enhet") navEining: String,
        @RequestBody saksbehandlersVurdering: SaksbehandlersVurdering,
    ): ResponseEntity<Void> {
        logger.info("Har motteke kall til /api/vurderingmanuelloppgave/$oppgaveId")

        if (!manuellOppgaveService.finnesOppgave(oppgaveId)) {
            return ResponseEntity.notFound().build()
        }

        val accessToken = authorization.removePrefix("Bearer ")
        val hasAccess =
            authorizationService.hasAccess(
                oppgaveId,
                accessToken,
                "/api/vurderingmanuelloppgave/$oppgaveId"
            )

        if (navEining.isNullOrEmpty()) {
            logger.error("Manglar X-Nav-Enhet i header")
            return ResponseEntity.badRequest().build()
        }
        when (hasAccess) {
            true -> {
                val merknad = saksbehandlersVurdering.toMerknad()

                val rettleiar = authorizationService.hentRettleiarIdent(oppgaveId)
                teamLogger.info(
                    "Ferdigstill manuelloppgåve for ${saksbehandlersVurdering.status} for oppgåve: $oppgaveId av rettleiar: $rettleiar"
                )
                manuellOppgaveService.ferdigstillManuellBehandling(
                    oppgaveId = oppgaveId,
                    navEining = navEining,
                    rettleiar = rettleiar,
                    accessToken = accessToken,
                    merknadar = merknad?.let { listOf(it) },
                )

                auditLogger.info("Ferdigstilt manuell oppgave for oppgave: $oppgaveId")
                return ResponseEntity.noContent().build()
            }
            false -> {
                loggerUtils.logNAVEpostFromTokenToTeamLogsWhenNoAccess(
                    accessToken,
                    "/api/vurderingmanuelloppgave/$oppgaveId"
                )
                return ResponseEntity.status(403).build()
            }
        }
    }
}
