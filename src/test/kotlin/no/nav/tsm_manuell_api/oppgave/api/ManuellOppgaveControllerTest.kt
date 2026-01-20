package no.nav.tsm_manuell_api.oppgave.api

import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.tsm.sykmelding.input.core.model.*
import no.nav.tsm.sykmelding.input.core.model.Pasient
import no.nav.tsm.sykmelding.input.core.model.metadata.*
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveResponse
import no.nav.tsm_manuell_api.oppgave.service.ManuellOppgaveService
import no.nav.tsm_manuell_api.security.authentication.AuthorizationService
import no.nav.tsm_manuell_api.utils.LoggerUtils
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(
    controllers = [ManuellOppgaveController::class],
    excludeAutoConfiguration =
        [SecurityAutoConfiguration::class, UserDetailsServiceAutoConfiguration::class]
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
    properties =
        [
            "spring.security.oauth2.client.registration.pdlcache-m2m.client-id=test",
            "spring.security.oauth2.client.registration.pdlcache-m2m.client-secret=test",
            "spring.security.oauth2.client.provider.aad.token-uri=http://localhost:8080/token"
        ]
)
class ManuellOppgaveControllerTest {

    @Autowired private lateinit var mockMvc: MockMvc

    @MockitoBean private lateinit var manuellOppgaveService: ManuellOppgaveService

    @MockitoBean private lateinit var authorizationService: AuthorizationService

    @MockitoBean private lateinit var loggerUtils: LoggerUtils

    @Test
    fun `hentOppgave should return 200 OK with ManuellOppgaveResponse when oppgave exists`() {
        // Given
        val oppgaveId = "test-sykmelding-123"
        val expectedOppgaveId = 12345
        val expectedResponse = createTestManuellOppgaveResponse(oppgaveId, expectedOppgaveId)

        `when`(authorizationService.hasAccess(oppgaveId, "test-token", "path")).thenReturn(true)
        `when`(manuellOppgaveService.hentOppgave(oppgaveId))
            .thenReturn(Result.success(expectedResponse))
        `when`(
                loggerUtils.createcCefMessage(
                    fnr = expectedResponse.ident,
                    accessToken = "test-token",
                    operation = LoggerUtils.Operation.READ,
                    requestPath = "/api/oppgave/{oppgaveId}",
                    permit = LoggerUtils.Permit.PERMIT
                )
            )
            .thenReturn("CEF message")

        // When/Then
        mockMvc
            .perform(
                get("/api/oppgave/{oppgaveId}", oppgaveId)
                    .header("Authorization", "Bearer test-token")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.oppgaveId").value(expectedOppgaveId))
            .andExpect(jsonPath("$.ident").value("12345678910"))
            .andExpect(jsonPath("$.ferdigstilt").value(false))
            .andExpect(jsonPath("$.status").value("APEN"))
            .andExpect(jsonPath("$.sykmelding.id").value(oppgaveId))
            .andExpect(jsonPath("$.sykmelding.pasient.fnr").value("12345678910"))
            .andExpect(jsonPath("$.sykmelding.pasient.navn.fornavn").value("Test"))
            .andExpect(jsonPath("$.sykmelding.pasient.navn.etternavn").value("Testesen"))
    }

    @Test
    fun `hentOppgave should return 404 NOT FOUND when oppgave does not exist`() {
        // Given
        val oppgaveId = "non-existent-oppgave"

        `when`(authorizationService.hasAccess(oppgaveId, "test-token", "path")).thenReturn(true)
        `when`(manuellOppgaveService.hentOppgave(oppgaveId))
            .thenReturn(Result.failure(Exception("Fant ikke oppgave med id $oppgaveId")))

        // When/Then
        mockMvc
            .perform(
                get("/api/oppgave/{oppgaveId}", oppgaveId)
                    .header("Authorization", "Bearer test-token")
            )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `hentOppgave should return correct mottattDato format`() {
        // Given
        val oppgaveId = "test-sykmelding-456"
        val expectedResponse = createTestManuellOppgaveResponse(oppgaveId, 67890)

        `when`(authorizationService.hasAccess(oppgaveId, "test-token", "path")).thenReturn(true)
        `when`(manuellOppgaveService.hentOppgave(oppgaveId))
            .thenReturn(Result.success(expectedResponse))
        `when`(
                loggerUtils.createcCefMessage(
                    fnr = expectedResponse.ident,
                    accessToken = "test-token",
                    operation = LoggerUtils.Operation.READ,
                    requestPath = "/api/oppgave/{oppgaveId}",
                    permit = LoggerUtils.Permit.PERMIT
                )
            )
            .thenReturn("CEF message")

        // When/Then
        mockMvc
            .perform(
                get("/api/oppgave/{oppgaveId}", oppgaveId)
                    .header("Authorization", "Bearer test-token")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mottattDato").exists())
            .andExpect(jsonPath("$.mottattDato").isString)
    }

    @Test
    fun `hentOppgave should include sykmelding medical information`() {
        // Given
        val oppgaveId = "test-sykmelding-789"
        val expectedResponse = createTestManuellOppgaveResponse(oppgaveId, 11111)

        `when`(authorizationService.hasAccess(oppgaveId, "test-token", "path")).thenReturn(true)
        `when`(manuellOppgaveService.hentOppgave(oppgaveId))
            .thenReturn(Result.success(expectedResponse))
        `when`(
                loggerUtils.createcCefMessage(
                    fnr = expectedResponse.ident,
                    accessToken = "test-token",
                    operation = LoggerUtils.Operation.READ,
                    requestPath = "/api/oppgave/{oppgaveId}",
                    permit = LoggerUtils.Permit.PERMIT
                )
            )
            .thenReturn("CEF message")

        // When/Then
        mockMvc
            .perform(
                get("/api/oppgave/{oppgaveId}", oppgaveId)
                    .header("Authorization", "Bearer test-token")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sykmelding.medisinskVurdering.hovedDiagnose.kode").value("L87"))
            .andExpect(
                jsonPath("$.sykmelding.medisinskVurdering.hovedDiagnose.tekst")
                    .value("Muskel-/skjelettlidelse")
            )
            .andExpect(jsonPath("$.sykmelding.aktivitet[0].fom").value("2024-01-01"))
            .andExpect(jsonPath("$.sykmelding.aktivitet[0].tom").value("2024-01-07"))
    }

    @Test
    fun `hentOppgave should include arbeidsgiver information`() {
        // Given
        val oppgaveId = "test-sykmelding-999"
        val expectedResponse = createTestManuellOppgaveResponse(oppgaveId, 22222)

        `when`(authorizationService.hasAccess(oppgaveId, "test-token", "path")).thenReturn(true)
        `when`(manuellOppgaveService.hentOppgave(oppgaveId))
            .thenReturn(Result.success(expectedResponse))
        `when`(
                loggerUtils.createcCefMessage(
                    fnr = expectedResponse.ident,
                    accessToken = "test-token",
                    operation = LoggerUtils.Operation.READ,
                    requestPath = "/api/oppgave/{oppgaveId}",
                    permit = LoggerUtils.Permit.PERMIT
                )
            )
            .thenReturn("CEF message")

        // When/Then
        mockMvc
            .perform(
                get("/api/oppgave/{oppgaveId}", oppgaveId)
                    .header("Authorization", "Bearer test-token")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sykmelding.arbeidsgiver.navn").value("Test Arbeidsgiver AS"))
            .andExpect(jsonPath("$.sykmelding.arbeidsgiver.yrkesbetegnelse").value("Kontoransatt"))
            .andExpect(jsonPath("$.sykmelding.arbeidsgiver.stillingsprosent").value(100))
    }

    private fun createTestManuellOppgaveResponse(
        sykmeldingId: String,
        oppgaveId: Int
    ): ManuellOppgaveResponse {
        val sykmelding = createTestXmlSykmelding(sykmeldingId)

        return ManuellOppgaveResponse(
            oppgaveId = oppgaveId,
            sykmelding = sykmelding,
            ident = "12345678910",
            ferdigstilt = false,
            mottattDato = sykmelding.metadata.mottattDato.toString(),
            status = "APEN",
            statusTimestamp = LocalDate.now(),
        )
    }

    private fun createTestXmlSykmelding(sykmeldingId: String): XmlSykmelding {
        val now = OffsetDateTime.now()

        return XmlSykmelding(
            id = sykmeldingId,
            metadata =
                SykmeldingMetadata(
                    mottattDato = now,
                    genDate = now,
                    behandletTidspunkt = now,
                    regelsettVersjon = "1",
                    avsenderSystem = AvsenderSystem(navn = "Test System", versjon = "1.0"),
                    strekkode = null
                ),
            pasient =
                Pasient(
                    navn = Navn(fornavn = "Test", mellomnavn = null, etternavn = "Testesen"),
                    navKontor = "0315",
                    navnFastlege = "Dr. Lege",
                    fnr = "12345678910",
                    kontaktinfo = emptyList()
                ),
            medisinskVurdering =
                MedisinskVurdering(
                    hovedDiagnose =
                        DiagnoseInfo(
                            system = DiagnoseSystem.ICPC2,
                            kode = "L87",
                            tekst = "Muskel-/skjelettlidelse"
                        ),
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskade = null,
                    skjermetForPasient = false,
                    syketilfelletStartDato = null,
                    annenFraversArsak = null
                ),
            aktivitet =
                listOf(
                    Gradert(
                        fom = LocalDate.of(2024, 1, 1),
                        tom = LocalDate.of(2024, 1, 7),
                        grad = 100,
                        reisetilskudd = false
                    )
                ),
            arbeidsgiver =
                EnArbeidsgiver(
                    navn = "Test Arbeidsgiver AS",
                    yrkesbetegnelse = "Kontoransatt",
                    stillingsprosent = 100,
                    meldingTilArbeidsgiver = null,
                    tiltakArbeidsplassen = null
                ),
            behandler =
                Behandler(
                    navn = Navn(fornavn = "Dr.", mellomnavn = null, etternavn = "Lege"),
                    adresse =
                        Adresse(
                            type = AdresseType.POSTADRESSE,
                            gateadresse = "Testveien 1",
                            postnummer = "0123",
                            poststed = "Oslo",
                            postboks = null,
                            kommune = "Oslo",
                            land = "Norge"
                        ),
                    ids = listOf(PersonId(id = "12345678901", type = PersonIdType.FNR)),
                    kontaktinfo = emptyList()
                ),
            sykmelder =
                Sykmelder(
                    ids = listOf(PersonId(id = "12345678901", type = PersonIdType.FNR)),
                    helsepersonellKategori = HelsepersonellKategori.LEGE
                ),
            prognose = null,
            tiltak = null,
            bistandNav = BistandNav(bistandUmiddelbart = false, beskrivBistand = null),
            tilbakedatering = Tilbakedatering(kontaktDato = null, begrunnelse = null),
            utdypendeOpplysninger = emptyMap()
        )
    }
}
