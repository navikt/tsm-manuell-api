package no.nav.tsm_manuell_api.person

import no.nav.tsm_manuell_api.person.pdl.IDENT_GRUPPE
import no.nav.tsm_manuell_api.person.pdl.IPdlClient
import no.nav.tsm_manuell_api.person.pdl.PdlPerson
import no.nav.tsm_manuell_api.utils.logger
import no.nav.tsm_manuell_api.utils.teamLogger
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlClient: IPdlClient,
) {
    private val logger = logger()
    private val teamLog = teamLogger()

    fun getPersonMedAktoerId(ident: String): Result<Person> {
        val person: PdlPerson =
            pdlClient.getPerson(ident).fold({ it }) {
                teamLog.error("Error while fetching person info for fnr=$ident", it)
                logger.error("Error while fetching person info from PDL, check secure logs")
                return Result.failure(it)
            }

        val currentIdent =
            person.identer
                .find { it.gruppe == IDENT_GRUPPE.FOLKEREGISTERIDENT && !it.historisk }
                ?.ident

        if (currentIdent == null) {
            teamLog.error("No valid FOLKEREGISTERIDENT found for person with ident $ident")
            return Result.failure(
                IllegalStateException(
                    "No valid FOLKEREGISTERIDENT found for person, see teamlog for ident"
                )
            )
        }

        if (person.navn == null) {
            teamLog.error("No name found for person with ident $ident")
            return Result.failure(
                IllegalStateException("No name found for person, see teamlog for ident")
            )
        }

        val aktoerId = person.identer.find { it.gruppe == IDENT_GRUPPE.AKTORID }?.ident

        if (aktoerId == null) {
            teamLog.error("No valid AKTORID found for person with ident $ident")
            return Result.failure(
                IllegalStateException("No valid AKTORID found for person, see teamlog for ident")
            )
        }
        return Result.success(
            Person(
                navn = person.navn,
                ident = currentIdent,
                aktoerId = aktoerId,
            )
        )
    }
}
