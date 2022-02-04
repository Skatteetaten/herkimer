package no.skatteetaten.aurora.herkimer.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.mockmvc.extensions.Path
import no.skatteetaten.aurora.mockmvc.extensions.contentTypeJson
import no.skatteetaten.aurora.mockmvc.extensions.get
import no.skatteetaten.aurora.mockmvc.extensions.patch
import no.skatteetaten.aurora.mockmvc.extensions.post
import no.skatteetaten.aurora.mockmvc.extensions.put
import no.skatteetaten.aurora.mockmvc.extensions.responseJsonPath
import no.skatteetaten.aurora.mockmvc.extensions.status
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import no.skatteetaten.aurora.mockmvc.extensions.statusIsOk

@AutoConfigureEmbeddedDatabase(provider = ZONKY)
@SpringBootTest(properties = ["aurora.authentication.token.value=secret_from_file", "aurora.authentication.enabled=false"])
@AutoConfigureMockMvc
class ApplicationDeploymentControllerTest {

    @Autowired
    private lateinit var testDataCreators: TestDataCreators

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var flyway: Flyway

    @BeforeEach
    fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `Create two ApplicationDeployment when they have same property values then return same AD`() {
        mockLocalDateTimeToNow { expectedTime ->
            val adPayload = ApplicationDeploymentPayload(
                name = "name",
                environmentName = "env",
                cluster = "cluster",
                businessGroup = "aurora"
            )

            val firstAdResource = mockMvc.post(
                path = Path("/applicationDeployment/"),
                body = adPayload,
                headers = HttpHeaders().contentTypeJson(),
                fn = {}
            ).response.contentAsString

            val secondAdResource = mockMvc.post(
                path = Path("/applicationDeployment/"),
                body = adPayload,
                headers = HttpHeaders().contentTypeJson(),
                fn = {}
            ).response.contentAsString

            assertThat(firstAdResource).isEqualTo(secondAdResource)
        }
    }

    @Test
    fun `Return Not Found ApplicationDeployment when there are none in DB`() {
        val nonExistingAdId = PrincipalUID.randomId().toString()
        mockMvc.get(Path("/applicationDeployment/{nonExistingAdId}", nonExistingAdId)) {
            status(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Update ApplicationDeployment When it does not exists Then return 404 with error`() {
        val nonExistingId = PrincipalUID.randomId().toString()
        val updatedAd = ApplicationDeploymentPayload(
            name = "herkimer",
            environmentName = "dev",
            cluster = "utv",
            businessGroup = "aurora"
        )

        mockMvc.put(
            path = Path("/applicationDeployment/{id}", nonExistingId),
            headers = HttpHeaders().contentTypeJson(),
            body = updatedAd
        ) {
            status(HttpStatus.NOT_FOUND)
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(nonExistingId)
        }
    }

    @Test
    fun `Migrate ApplicationDeployment When it does not exists Then return 404 with error`() {
        val nonExistingId = PrincipalUID.randomId().toString()
        val migratedAd = ApplicationMigrationPayload(
            environmentName = "dev",
            cluster = "utv04",
            businessGroup = "aup"
        )

        mockMvc.patch(
            path = Path("/applicationDeployment/{id}", nonExistingId),
            headers = HttpHeaders().contentTypeJson(),
            body = migratedAd
        ) {
            status(HttpStatus.NOT_FOUND)
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(nonExistingId)
        }
    }

    @Test
    fun `Migrate ApplicationDeployment When payload contains one valid property Then only the provided property is modified`() {
        val prefix = Math.random().toString()
        val adId = testDataCreators.createApplicationDeploymentAndReturnId(prefix = prefix)

        val migratedAd = ApplicationMigrationPayload(environmentName = "test-env")

        mockMvc.patch(
            path = Path("/applicationDeployment/{adId}", adId),
            headers = HttpHeaders().contentTypeJson(),
            body = migratedAd
        ) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].id").equalsValue(adId)
                .responseJsonPath("$.items[0].environmentName").equalsValue("test-env")
                .responseJsonPath("$.items[0].cluster").equalsValue("$prefix-cluster")
                .responseJsonPath("$.items[0].businessGroup").equalsValue("$prefix-aurora")
        }
    }

    @Test
    fun `Migrate ApplicationDeployment When payload contains null and empty properties Then return HTTP_BAD_REQUEST`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()

        mockMvc.patch(
            path = Path("/applicationDeployment/{adId}", adId),
            headers = HttpHeaders().contentTypeJson(),
            body = ApplicationMigrationPayload()
        ) {
            status(HttpStatus.BAD_REQUEST)
        }
    }
}
