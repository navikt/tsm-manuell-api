package no.nav.tsm_manuell_api.oppgave.repository

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import no.nav.tsm.sykmelding.input.core.model.*
import no.nav.tsm.sykmelding.input.core.model.Pasient
import no.nav.tsm.sykmelding.input.core.model.metadata.*
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgave
import no.nav.tsm_manuell_api.oppgave.model.ManuellOppgaveStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Integration tests for OppgaveRepository using Postgres Testcontainers. Tests the database
 * operations for creating and checking manuell oppgave existence.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class OppgaveRepositoryTest {

    companion object {
        @Container
        val postgres =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("testdb")
                withUsername("test")
                withPassword("test")
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.flyway.url", postgres::getJdbcUrl)
            registry.add("spring.flyway.user", postgres::getUsername)
            registry.add("spring.flyway.password", postgres::getPassword)
        }
    }

    @Autowired private lateinit var oppgaveRepository: OppgaveRepository

    @Autowired private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @BeforeEach
    fun setup() {
        jdbcTemplate.jdbcTemplate.execute("TRUNCATE TABLE manuelloppgave CASCADE")
    }

    @Test
    fun `opprettManuellOppgave should create entry in database using ManuellOppgave object`() {
        // Given: A ManuellOppgave object
        val manuellOppgave =
            createTestManuellOppgave("test-sykmelding-1", ManuellOppgaveStatus.APEN)

        // When: Calling opprettManuellOppgave
        oppgaveRepository.opprettManuellOppgave(manuellOppgave)

        // Then: Oppgave should exist in database
        val exists = oppgaveRepository.erManuellOppgaveOpprettet("test-sykmelding-1")
        assertTrue(exists)
    }

    @Test
    fun `opprettManuellOppgave should correctly store all fields`() {
        // Given: A ManuellOppgave with specific values
        val sykmeldingId = "test-sykmelding-3"
        val oppgaveId = 98765
        val status = ManuellOppgaveStatus.APEN
        val manuellOppgave = createTestManuellOppgave(sykmeldingId, status, oppgaveId)

        // When: Creating the oppgave
        oppgaveRepository.opprettManuellOppgave(manuellOppgave)

        // Then: All fields should be stored correctly
        val result = oppgaveRepository.hentManuellOppgaveForSykmeldingId(sykmeldingId)
        assertNotNull(result)
        assertEquals(sykmeldingId, result!!.sykmelding.id)
        assertEquals("12345678910", result.ident)
        assertEquals(false, result.ferdigstilt)
        assertEquals(oppgaveId, result.oppgaveid)
        assertEquals(status.name, result.status)
    }

    @Test
    fun `opprettManuellOppgave should handle null oppgaveid and status`() {
        // Given: A ManuellOppgave with null oppgaveid and status
        val sykmeldingId = "test-sykmelding-4"
        val manuellOppgave = createTestManuellOppgave(sykmeldingId, null, null)

        // When: Creating the oppgave
        oppgaveRepository.opprettManuellOppgave(manuellOppgave)

        // Then: Oppgave should be created with null values
        val result = oppgaveRepository.hentManuellOppgaveForSykmeldingId(sykmeldingId)
        assertNotNull(result)
        assertEquals(sykmeldingId, result!!.sykmelding.id)
        assertEquals(null, result.oppgaveid)
        assertEquals(null, result.status)
    }

    @Test
    fun `opprettManuellOppgave should serialize sykmelding as JSON`() {
        // Given: A ManuellOppgave
        val sykmeldingId = "test-sykmelding-5"
        val manuellOppgave =
            createTestManuellOppgave(sykmeldingId, ManuellOppgaveStatus.FERDIGSTILT)

        // When: Creating the oppgave
        oppgaveRepository.opprettManuellOppgave(manuellOppgave)

        // Then: Sykmelding should be retrievable and contain correct data
        val result = oppgaveRepository.hentManuellOppgaveForSykmeldingId(sykmeldingId)
        assertNotNull(result)
        assertNotNull(result!!.sykmelding)
        assertEquals(sykmeldingId, result.sykmelding.id)
        assertEquals("12345678910", result.sykmelding.pasient.fnr)
    }

    @Test
    fun `erManuellOppgaveOpprettet should return false when oppgave does not exist`() {
        // When: Check for non-existent oppgave
        val exists = oppgaveRepository.erManuellOppgaveOpprettet("non-existent-id")

        // Then: Should return false
        assertFalse(exists)
    }

    @Test
    fun `erManuellOppgaveOpprettet should return true when oppgave exists`() {
        // Given: Insert a manuell oppgave
        val sykmeldingId = "test-sykmelding-2"
        val manuellOppgave =
            createTestManuellOppgave(sykmeldingId, ManuellOppgaveStatus.FERDIGSTILT)
        oppgaveRepository.opprettManuellOppgave(manuellOppgave)

        // When: Check if oppgave exists
        val exists = oppgaveRepository.erManuellOppgaveOpprettet(sykmeldingId)

        // Then: Should return true
        assertTrue(exists)
    }

    private fun createTestManuellOppgave(
        sykmeldingId: String,
        status: ManuellOppgaveStatus?,
        oppgaveId: Int? = 12345
    ): ManuellOppgave {
        val sykmelding = createTestXmlSykmelding(sykmeldingId)

        return ManuellOppgave(
            sykmelding = sykmelding,
            ferdigstilt = false,
            oppgaveId = oppgaveId,
            status = status,
            statusTimestamp = LocalDateTime.now(),
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
