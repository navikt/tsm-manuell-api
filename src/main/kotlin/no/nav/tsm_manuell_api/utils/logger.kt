package no.nav.tsm_manuell_api.utils

import com.nimbusds.jwt.SignedJWT
import java.time.ZonedDateTime.now
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun <reified T> T.teamLogger(): Logger =
    LoggerFactory.getLogger("teamlog.${T::class.java.name}")

inline fun <reified T> T.auditLogger(): Logger =
    LoggerFactory.getLogger("auditLogger.${T::class.java.name}")

@Component
class LoggerUtils {
    val teamLogger = teamLogger()

    fun createcCefMessage(
        fnr: String?,
        accessToken: String,
        operation: Operation,
        requestPath: String,
        permit: Permit,
    ): String {
        val application = "tsm-manuell-api"

        val navEmail = getNavEpostFromToken(accessToken)
        val now = now().toInstant().toEpochMilli()
        val subject = fnr?.padStart(11, '0')
        val duidStr = subject?.let { " duid=$it" } ?: ""

        return "CEF:0|Sykemeldingregistrering|$application|1.0|${operation.logString}|Sporingslogg|INFO|end=$now$duidStr" +
            " suid=$navEmail request=$requestPath flexString1Label=Decision flexString1=$permit"
    }

    enum class Operation(val logString: String) {
        READ("audit:access"),
        WRITE("audit:update"),
        UNKNOWN("audit:unknown"),
    }

    enum class Permit(val logString: String) {
        PERMIT("Permit"),
        DENY("Deny"),
    }

    fun logNAVEpostFromTokenToTeamLogsWhenNoAccess(accessToken: String, path: String) {
        try {
            val navEmail = getNavEpostFromToken(accessToken)
            teamLogger.info(
                "Logger ut navEpost: {}, har ikkje tilgong til path: {}",
                navEmail,
                path
            )
        } catch (exception: Exception) {
            teamLogger.info("Fekk ikkje henta ut navEpost", exception)
        }
    }

    private fun getNavEpostFromToken(accessToken: String): String {
        val signedJwt = SignedJWT.parse(accessToken)
        return signedJwt.jwtClaimsSet.getStringClaim("preferred_username")
    }
}
