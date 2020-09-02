package no.skatteetaten.aurora.herkimer.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.skatteetaten.aurora.herkimer.configureDefaults
import no.skatteetaten.aurora.mockmvc.extensions.MockMvcData
import no.skatteetaten.aurora.mockmvc.extensions.Path
import no.skatteetaten.aurora.mockmvc.extensions.contentTypeJson
import no.skatteetaten.aurora.mockmvc.extensions.delete
import no.skatteetaten.aurora.mockmvc.extensions.get
import no.skatteetaten.aurora.mockmvc.extensions.post
import no.skatteetaten.aurora.mockmvc.extensions.put
import no.skatteetaten.aurora.mockmvc.extensions.responseJsonPath
import no.skatteetaten.aurora.mockmvc.extensions.status
import no.skatteetaten.aurora.mockmvc.extensions.statusIsOk
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
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Create ApplicationDeployment when body has ApplicationDeployment`() {
        mockLocalDateTimeToNow { expectedTime ->
            createApplicationDeployment {
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
        val adId = createApplicationDeploymentAndReturnId()

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
        createApplicationDeployment(
            ad = ApplicationDeploymentPayload(
                name = "testApp",
                environmentName = "dev",
                cluster = "utv",
                applicationName = "name",
                businessGroup = "aurora"
            )
        )

        mockMvc.get(Path("/applicationDeployment/")) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].id").isNotEmpty()
                .responseJsonPath("$.items[0].name").equalsValue("testApp")
        }
    }

    @Test
    fun `Delete ApplicationDeployment When it exists Then return HTTP_OK`() {
        val adId = createApplicationDeploymentAndReturnId()

        mockMvc.delete(Path("/applicationDeployment/{adId}", adId)) {
            statusIsOk()
        }
    }

    @Test
    fun `Update ApplicationDeployment When it exists Then return HTTP_OK and updated resource`() {
        val adId = createApplicationDeploymentAndReturnId()

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

    private fun createApplicationDeploymentAndReturnId() = jacksonObjectMapper()
        .configureDefaults()
        .readValue<AuroraResponse<ApplicationDeploymentResource>>(createApplicationDeployment().response.contentAsString)
        .items
        .single()
        .id

    private fun createApplicationDeployment(
        ad: ApplicationDeploymentPayload = ApplicationDeploymentPayload(
            name = "name",
            environmentName = "env",
            cluster = "cluster",
            applicationName = "whoami",
            businessGroup = "aurora"
        ),
        fn: MockMvcData.() -> Unit = {}
    ) = mockMvc.post(
        path = Path("/applicationDeployment/"),
        body = ad,
        headers = HttpHeaders().contentTypeJson(),
        fn = fn
    )
}
