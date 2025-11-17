package no.nav.tsm_manuell_api.person

import no.nav.tsm.sykmelding.input.core.model.metadata.Navn

data class Person(
    val navn: Navn,
    val ident: String,
    val aktoerId: String,
)
