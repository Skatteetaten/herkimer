package no.skatteetaten.aurora.herkimer.controller

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.mockmvc.extensions.Path
import no.skatteetaten.aurora.mockmvc.extensions.TestObjectMapperConfigurer
import no.skatteetaten.aurora.mockmvc.extensions.contentTypeJson
import no.skatteetaten.aurora.mockmvc.extensions.get
import no.skatteetaten.aurora.mockmvc.extensions.patch
import no.skatteetaten.aurora.mockmvc.extensions.post
import no.skatteetaten.aurora.mockmvc.extensions.put
import no.skatteetaten.aurora.mockmvc.extensions.responseJsonPath
import no.skatteetaten.aurora.mockmvc.extensions.statusIsOk

@AutoConfigureEmbeddedDatabase(provider = ZONKY)
@SpringBootTest(properties = ["aurora.authentication.token.value=secret_from_file", "aurora.authentication.enabled=false"])
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class ResourceControllerContractTest {

    @Autowired
    private lateinit var testDataCreators: TestDataCreators

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var flyway: Flyway

    private val mapper = TestObjectMapperConfigurer.objectMapper

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
                    ownerId = PrincipalUID.fromString(ownerId)
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
    fun `Return Resource with claims when there are one in DB`() {
        val ownerId = testDataCreators.createApplicationDeploymentAndReturnId()
        val id = testDataCreators.createResourceAndReturnId(ownerId)
        testDataCreators.claimResource(ownerOfClaim = ownerId, resourceId = id)

        mockMvc.get(Path("/resource/{id}?includeClaims=true", id)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].claims.length()").equalsValue(1)
                .responseJsonPath("$.items[0].claims[0].ownerId").equalsValue(ownerId)
        }
    }

    @Test
    fun `Claim resource with applicationDeployment when there are several resources in DB`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()
        val resourceId = testDataCreators.createResourceAndReturnId(ownerId = adId)

        repeat(5) {
            testDataCreators.createResourceAndReturnId()
        }
        val credentials = mapper.readTree("""{"password":"superPassword"}""")

        mockMvc.post(
            path = Path("/resource/{id}/claims", resourceId),
            headers = HttpHeaders().contentTypeJson(),
            body = ResourceClaimPayload(
                ownerId = PrincipalUID.fromString(adId),
                credentials = credentials,
                name = "ADMIN"
            )
        ) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].credentials").equalsObject(credentials)
        }
    }

    @Test
    fun `Update Resource When it exists Then return HTTP_OK and updated resource`() {
        val id = testDataCreators.createResourceAndReturnId()
        val newOwnerId = testDataCreators.createApplicationDeploymentAndReturnId()

        val updateResource = ResourcePayload(
            name = "newName",
            kind = ResourceKind.ManagedOracleSchema,
            ownerId = PrincipalUID.fromString(newOwnerId)
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

    @Test
    fun `PATCH Resource with active=false When it exists Then return HTTP_OK and updated resource`() {
        mockLocalDateTimeToNow { expectedTime ->
            val id = testDataCreators.createResourceAndReturnId()

            val patchedResource = UpdateResourcePayload(
                active = false
            )

            mockMvc.patch(
                path = Path("/resource/{id}", id),
                headers = HttpHeaders().contentTypeJson(),
                body = patchedResource
            ) {
                statusIsOk()
                    .responseJsonPath("$.count").equalsValue(1)
                    .responseJsonPath("$.success").isTrue()
                    .responseJsonPath("$.items[0].id").equalsValue(id)
                    .responseJsonPath("$.items[0].active").equalsValue(false)
                    .responseJsonPath("$.items[0].setToCooldownAt").equalsValue(expectedTime.toString())
            }
        }
    }
}
