package no.nav.tsm_manuell_api.oppgave.api

import no.nav.tsm_manuell_api.oppgave.ManuellOppgaveService
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oppgave")
class ManuellOppgaveController(
    private val manuellOppgaveService: ManuellOppgaveService,
) {

    @GetMapping("/{oppgaveId}")
    fun hentOppgave(@PathVariable oppgaveId: String): ResponseEntity<ManuellOppgaveResponse> {
        val manuellOppgaveResponse =
            manuellOppgaveService.hentOppgave(oppgaveId).getOrElse {
                return ResponseEntity.notFound().build()
            }
        return ResponseEntity.ok(manuellOppgaveResponse)
    }

    //    get("/oppgaver") { call.respond(manuellOppgaveService.getOppgaver()) }
    //    get("/oppgave/sykmelding/{sykmeldingId}") {
}
