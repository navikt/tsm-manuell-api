package no.nav.tsm_manuell_api.utils

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

interface IAuditLoggingService {
    fun hasReadPermitLogMessage(ident: String, accessToken: String)

    fun missingPermitLogMessage(accessToken: String, path: String, operation: LoggerUtils.Operation)

    fun ferdigstillOppgaveLogMessage(oppgaveId: String, accessToken: String)
}

@Service
@Profile("!local && !test")
class AuditLoggingService(private val loggerUtils: LoggerUtils) : IAuditLoggingService {
    val logger = logger()
    val auditLogger = auditLogger()

    override fun hasReadPermitLogMessage(ident: String, accessToken: String) {
        val path = "/api/oppgave/{oppgaveId}"
        auditLogger.info(
            loggerUtils.createcCefMessage(
                fnr = ident,
                accessToken = accessToken,
                operation = LoggerUtils.Operation.READ,
                requestPath = path,
                permit = LoggerUtils.Permit.PERMIT
            )
        )
    }

    override fun missingPermitLogMessage(
        accessToken: String,
        path: String,
        operation: LoggerUtils.Operation
    ) {
        loggerUtils.logNAVEpostFromTokenToTeamLogsWhenNoAccess(accessToken, path)

        auditLogger.info(
            loggerUtils.createcCefMessage(
                fnr = null,
                accessToken = accessToken,
                operation = operation,
                requestPath = path,
                permit = LoggerUtils.Permit.DENY,
            )
        )
    }

    override fun ferdigstillOppgaveLogMessage(oppgaveId: String, accessToken: String) {
        auditLogger.info(
            loggerUtils.createcCefMessage(
                fnr = null,
                accessToken = accessToken,
                operation = LoggerUtils.Operation.WRITE,
                requestPath = "/api/vurderingmanuelloppgave/$oppgaveId",
                permit = LoggerUtils.Permit.PERMIT
            )
        )
    }
}

@Service
@Profile("local", "test")
class MockAuditLoggingService : IAuditLoggingService {
    val logger = logger()

    override fun hasReadPermitLogMessage(ident: String, accessToken: String) {
        logger.info("Lokal profil er aktiv - bruker har tilgong til oppgåve")
    }

    override fun missingPermitLogMessage(
        accessToken: String,
        path: String,
        operation: LoggerUtils.Operation
    ) {
        logger.info("Lokal profil er aktiv - bruker har ikkje tilgong til oppgåve")
    }

    override fun ferdigstillOppgaveLogMessage(oppgaveId: String, accessToken: String) {
        logger.info("Lokal profil er aktiv - oppgave er ferdigstilt")
    }
}
