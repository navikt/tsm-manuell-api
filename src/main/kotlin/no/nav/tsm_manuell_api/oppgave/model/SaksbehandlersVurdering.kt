package no.nav.tsm_manuell_api.oppgave.model

data class SaksbehandlersVurdering(
    val status: SaksbehandlersVurderingStatus,
    val merknad: Merknad? = null,
) {
    fun toMerknad(): Merknad? {
        return when (status) {
            SaksbehandlersVurderingStatus.UGYLDIG_TILBAKEDATERING -> {
                Merknad(
                    type = SaksbehandlersVurderingStatus.UGYLDIG_TILBAKEDATERING.name,
                    beskrivelse = null,
                )
            }
            SaksbehandlersVurderingStatus.TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER -> {
                Merknad(
                    type =
                        SaksbehandlersVurderingStatus.TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER
                            .name,
                    beskrivelse = null,
                )
            }
            SaksbehandlersVurderingStatus.DELVIS_GODKJENT -> {
                Merknad(
                    type = SaksbehandlersVurderingStatus.DELVIS_GODKJENT.name,
                    beskrivelse = null,
                )
            }
            else -> {
                null
            }
        }
    }
}

enum class SaksbehandlersVurderingStatus {
    GODKJENT,
    UGYLDIG_TILBAKEDATERING,
    TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER,
    DELVIS_GODKJENT,
}
