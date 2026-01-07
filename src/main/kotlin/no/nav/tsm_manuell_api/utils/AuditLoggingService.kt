package no.nav.tsm_manuell_api.utils

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


interface IAuditLoggingService {
    fun hasReadPermitLogMessage(ident: String, accessToken: String)
    fun missingReadPermitLogMessage(accessToken: String)

}

@Service
@Profile("!local && !test")
class AuditLoggingService(private val accessLoggerUtils: AccessLoggerUtils) : IAuditLoggingService {
    val logger = logger()
    val auditLogger = auditLogger()

    override fun hasReadPermitLogMessage(ident: String, accessToken: String) {
        val path = "/api/oppgave/{oppgaveId}"
        auditLogger.info(accessLoggerUtils.createcCefMessage(
            fnr = ident,
            accessToken = accessToken,
            operation = AccessLoggerUtils.Operation.READ,
            requestPath = path,
            permit = AccessLoggerUtils.Permit.PERMIT
        ))
    }

    override fun missingReadPermitLogMessage(accessToken: String) {
        val path = "/api/oppgave/{oppgaveId}"
        accessLoggerUtils.logNAVEpostFromTokenToTeamLogsWhenNoAccess(accessToken, path)

        auditLogger.info(accessLoggerUtils.createcCefMessage(
            fnr = null,
            accessToken = accessToken,
            operation = AccessLoggerUtils.Operation.READ,
            requestPath = path,
            permit = AccessLoggerUtils.Permit.DENY,
        ))
    }

}

@Service
@Profile("local", "test")
class MockAuditLoggingService : IAuditLoggingService {
    val logger = logger()

    override fun hasReadPermitLogMessage(ident: String, accessToken: String) {
        logger.info("Lokal profil er aktiv - bruker har tilgong til oppgåve")
    }

    override fun missingReadPermitLogMessage(accessToken: String) {
        logger.info("Lokal profil er aktiv - bruker har ikkje tilgong til oppgåve")
    }

}