package no.nav.tsm_manuell_api.oppgave.model

data class SaksbehandlersVurdering(
    val status: Status,
)


enum class Status {
    GODKJENT,
    UGYLDIG_TILBAKEDATERING,
    TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER,
    DELVIS_GODKJENT,
}
