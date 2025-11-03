package no.nav.tsm_manuell_api.person.pdl

import no.nav.tsm.sykmelding.input.core.model.metadata.Navn
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate

@Profile("local", "test")
@Component
class MockPdlClient : IPdlClient {
    override fun getPerson(fnr: String): Result<PdlPerson> {
        if (fnr == "does-not-exist")
            return Result.failure(IllegalStateException("Could not find person in pdl cache"))

        return Result.success(
            PdlPerson(
                navn =
                    Navn(
                        fornavn = "Ola",
                        mellomnavn = null,
                        etternavn = "Nordmann",
                    ),
                foedselsdato = LocalDate.of(1991, 4, 12),
                identer =
                    listOf(
                        Ident(
                            ident = fnr,
                            gruppe = IDENT_GRUPPE.FOLKEREGISTERIDENT,
                            historisk = false,
                        ),
                        Ident(
                            ident = "1234567890123",
                            gruppe = IDENT_GRUPPE.AKTORID,
                            historisk = false,
                        ),
                    ),
            ),
        )
    }
}
