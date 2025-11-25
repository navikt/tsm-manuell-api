package no.nav.tsm_manuell_api.oppgave.api

import no.nav.tsm_manuell_api.oppgave.service.ManuellOppgaveService
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveResponse
import no.nav.tsm_manuell_api.security.authentication.AuthorizationService
import no.nav.tsm_manuell_api.utils.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oppgave")
class ManuellOppgaveController(
    private val manuellOppgaveService: ManuellOppgaveService,
    private val authorizationService: AuthorizationService,
) {
    val logger = logger()

    @GetMapping("/{oppgaveId}")
    fun hentOppgave(
        @PathVariable oppgaveId: String,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<ManuellOppgaveResponse> {
        val accessToken = authorization.removePrefix("Bearer ")
        val hasAccess = authorizationService.hasAccess(oppgaveId, accessToken)

        if(hasAccess) {
            val manuellOppgaveResponse =
                manuellOppgaveService.hentOppgave(oppgaveId).getOrElse {
                    return ResponseEntity.notFound().build()
                }
            return ResponseEntity.ok(manuellOppgaveResponse)
        }
        else {
            logger.info("Bruker har ikke tilgang til oppgave med id $oppgaveId")
            return ResponseEntity.status(403).build()
        }
    }

    // andre endepunkt som SMMB har
    //    get("/oppgaver") { call.respond(manuellOppgaveService.getOppgaver()) }
}
