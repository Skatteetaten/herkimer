package no.skatteetaten.aurora.herkimer.controller

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
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
class ResourceControllerTest {

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
    fun `Create Resource when payload is sent`() {

        mockLocalDateTimeToNow { expectedTime ->
            val ownerId = testDataCreators.createApplicationDeploymentAndReturnId()

            mockMvc.post(
                path = Path("/resource"),
                headers = HttpHeaders().contentTypeJson(),
                body = ResourcePayload(
                    name = "minio resource",
                    kind = ResourceKind.MinioPolicy,
                    ownerId = UUID.fromString(ownerId)
                )
            ) {
                statusIsOk()
                    .responseJsonPath("$.count").equalsValue(1)
                    .responseJsonPath("$.success").isTrue()
                    .responseJsonPath("$.items[0].id").isNotEmpty()
                    .validateAuditing(expectedTime)
            }
        }
    }

    @Test
    fun `Return Not Found Resource when there are none in DB`() {
        mockMvc.get(Path("/resource/${-10L}")) {
            status(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Return Resource when there are one in DB`() {
        val ownerId = testDataCreators.createApplicationDeploymentAndReturnId()
        val id = testDataCreators.createResourceAndReturnId(ownerId)

        mockMvc.get(Path("/resource/{id}", id)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].name").equalsValue("resourceName")
                .responseJsonPath("$.items[0].ownerId").equalsValue(ownerId)
                .responseJsonPath("$.items[0].id").equalsValue(id)
                .responseJsonPath("$.items[0].kind").equalsValue("MinioPolicy")
        }
    }

    @Test
    fun `Return list of Resource when there are one in DB`() {
        testDataCreators.createResourceAndReturnId()

        mockMvc.get(Path("/resource/")) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items.[0].name").equalsValue("resourceName")
                .responseJsonPath("$.items.[0].ownerId").isNotEmpty()
                .responseJsonPath("$.items[0].id").isNotEmpty()
                .responseJsonPath("$.items[0].kind").equalsValue("MinioPolicy")
        }
    }

    @Test
    fun `Return list of Resource for a ownerId when there are several in DB`() {
        val ownerId = testDataCreators.createApplicationDeploymentAndReturnId()

        repeat(4) {
            testDataCreators.createResourceAndReturnId()
        }

        repeat(2) {
            testDataCreators.createResourceAndReturnId(ownerId)
        }

        mockMvc.get(Path("/resource?ownerId={ownerId}", ownerId)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(2)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items.[0].ownerId").equalsValue(ownerId)
                .responseJsonPath("$.items.[1].ownerId").equalsValue(ownerId)
        }
    }

    @Test
    fun `Delete Resource When it exists Then return HTTP_OK`() {
        val id = testDataCreators.createResourceAndReturnId()

        mockMvc.delete(Path("/resource/{id}", id)) {
            statusIsOk()
        }
    }

    @Test
    fun `Update Resource When it exists Then return HTTP_OK and updated resource`() {
        val id = testDataCreators.createResourceAndReturnId()
        val newOwnerId = testDataCreators.createApplicationDeploymentAndReturnId()

        val updateResource = ResourcePayload(
            name = "newName",
            kind = ResourceKind.ManagedOracleSchema,
            ownerId = UUID.fromString(newOwnerId)
        )

        mockMvc.put(
            path = Path("/resource/{id}", id),
            headers = HttpHeaders().contentTypeJson(),
            body = updateResource
        ) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].id").equalsValue(id)
                .responseJsonPath("$.items[0].name").equalsValue("newName")
                .responseJsonPath("$.items[0].kind").equalsValue("ManagedOracleSchema")
                .responseJsonPath("$.items[0].ownerId").equalsValue(newOwnerId)
        }
    }
}
