package no.skatteetaten.aurora.herkimer.controller

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.skatteetaten.aurora.mockmvc.extensions.Path
import no.skatteetaten.aurora.mockmvc.extensions.contentTypeJson
import no.skatteetaten.aurora.mockmvc.extensions.delete
import no.skatteetaten.aurora.mockmvc.extensions.get
import no.skatteetaten.aurora.mockmvc.extensions.post
import no.skatteetaten.aurora.mockmvc.extensions.put
import no.skatteetaten.aurora.mockmvc.extensions.responseJsonPath
import no.skatteetaten.aurora.mockmvc.extensions.status
import no.skatteetaten.aurora.mockmvc.extensions.statusIsOk
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import java.util.UUID

@AutoConfigureEmbeddedDatabase
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
    fun `Create ApplicationDeployment when body has ApplicationDeployment`() {
        mockLocalDateTimeToNow { expectedTime ->
            val adPayload = ApplicationDeploymentPayload(
                name = "name",
                environmentName = "env",
                cluster = "cluster",
                applicationName = "whoami",
                businessGroup = "aurora"
            )
            mockMvc.post(
                path = Path("/applicationDeployment/"),
                body = adPayload,
                headers = HttpHeaders().contentTypeJson()
            ) {
                statusIsOk()
                    .responseJsonPath("$.count").equalsValue(1)
                    .responseJsonPath("$.success").isTrue()
                    .responseJsonPath("$.items[0].id").isNotEmpty()
                    .responseJsonPath("$.items[0].name").equalsValue("name")
                    .validateAuditing(expectedTime)
            }
        }
    }

    @Test
    fun `Return Not Found ApplicationDeployment when there are none in DB`() {
        val nonExistingAdId = UUID.randomUUID().toString()
        mockMvc.get(Path("/applicationDeployment/{nonExistingAdId}", nonExistingAdId)) {
            status(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Return ApplicationDeployment when there are one in DB`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()

        mockMvc.get(Path("/applicationDeployment/{adId}", adId)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].name").equalsValue("name")
                .responseJsonPath("$.items[0].id").equalsValue(adId)
        }
    }

    @Test
    fun `Return list of ApplicationDeployment when there are one in DB`() {
        testDataCreators.createApplicationDeploymentAndReturnId()

        mockMvc.get(Path("/applicationDeployment/")) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].id").isNotEmpty()
                .responseJsonPath("$.items[0].name").equalsValue("name")
        }
    }

    @Test
    fun `Delete ApplicationDeployment When it exists Then return HTTP_OK`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()

        mockMvc.delete(Path("/applicationDeployment/{adId}", adId)) {
            statusIsOk()
        }
    }

    @Test
    fun `Update ApplicationDeployment When it exists Then return HTTP_OK and updated resource`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()

        val updatedAd = ApplicationDeploymentPayload(
            name = "herkimer",
            environmentName = "dev",
            cluster = "utv",
            businessGroup = "aurora",
            applicationName = "whoami"
        )

        mockMvc.put(
            path = Path("/applicationDeployment/{adId}", adId),
            headers = HttpHeaders().contentTypeJson(),
            body = updatedAd
        ) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].id").equalsValue(adId)
                .responseJsonPath("$.items[0].name").equalsValue("herkimer")
        }
    }
}
