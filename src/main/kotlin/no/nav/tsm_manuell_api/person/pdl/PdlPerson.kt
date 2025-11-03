package no.nav.tsm_manuell_api.person.pdl

import no.nav.tsm.sykmelding.input.core.model.metadata.Navn
import java.time.LocalDate

data class PdlPerson(
    val navn: Navn?,
    val foedselsdato: LocalDate?,
    val identer: List<Ident>,
)

data class Ident(
    val ident: String,
    val gruppe: IDENT_GRUPPE,
    val historisk: Boolean,
)

enum class IDENT_GRUPPE {
    AKTORID,
    FOLKEREGISTERIDENT,
    NPID,
}
