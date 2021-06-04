package no.skatteetaten.aurora.herkimer.controller

import java.util.UUID
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import com.fasterxml.jackson.databind.node.ObjectNode
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
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
import no.skatteetaten.aurora.mockmvc.extensions.status
import no.skatteetaten.aurora.mockmvc.extensions.statusIsOk

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

    private val mapper = TestObjectMapperConfigurer.objectMapper

    @BeforeEach
    fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `Create Resource when ownerId does not exist then return 404`() {
        val ownerId = PrincipalUID.randomId()

        mockMvc.post(
            path = Path("/resource"),
            headers = HttpHeaders().contentTypeJson(),
            body = ResourcePayload(
                name = "minio resource",
                kind = ResourceKind.MinioPolicy,
                ownerId = ownerId
            )
        ) {
            status(HttpStatus.NOT_FOUND)
                .responseJsonPath("$.errors.length()").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(ownerId.toString())
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
    fun `Throw illegalArgument when findingAllResources without queryparams`() {
        mockMvc.get(Path("/resource")) {
            status(HttpStatus.BAD_REQUEST)
            responseJsonPath("$.errors.length()").equalsValue(1)
            responseJsonPath("$.errors[0].errorMessage").contains("claimedBy is not specified name and resourceKind is required.")
        }
    }

    @Test
    fun `Claim resource when resource does not exist then return 400`() {
        val ownerId = testDataCreators.createApplicationDeploymentAndReturnId()
        val nonExistingResourceId = "0"

        mockMvc.post(
            path = Path("/resource/{id}/claims", nonExistingResourceId),
            headers = HttpHeaders().contentTypeJson(),
            body = ResourceClaimPayload(
                ownerId = PrincipalUID.fromString(ownerId),
                credentials = mapper.readTree("""{}"""),
                name = "ADMIN"
            )
        ) {
            status(HttpStatus.BAD_REQUEST)
                .responseJsonPath("$.errors.length()").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(nonExistingResourceId)
        }
    }

    @Test
    fun `Claim resource when principal does not exist then return 400`() {

        val ownerId = PrincipalUID.randomId()
        mockMvc.post(
            path = Path("/resource/{id}/claims", "0"),
            headers = HttpHeaders().contentTypeJson(),
            body = ResourceClaimPayload(
                ownerId = ownerId,
                credentials = mapper.readTree("""{}"""),
                name = "ADMIN"
            )
        ) {
            status(HttpStatus.BAD_REQUEST)
                .responseJsonPath("$.errors.length()").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(ownerId.toString())
        }
    }

    @Test
    fun `Resource creation should be idempotent`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()

        val resourcePayload = ResourcePayload(
            name = "minio resource",
            kind = ResourceKind.MinioPolicy,
            ownerId = PrincipalUID.fromString(adId)
        )
        val firstInsertedResourceId = testDataCreators.createResourceAndReturnId(
            ownerId = adId,
            kind = resourcePayload.kind,
            name = resourcePayload.name
        )

        mockMvc.post(
            path = Path("/resource"),
            headers = HttpHeaders().contentTypeJson(),
            body = resourcePayload
        ) {
            statusIsOk()
            responseJsonPath("$.success").isTrue()
            responseJsonPath("$.items[0].name").equalsValue(resourcePayload.name)
            responseJsonPath("$.items[0].kind").equalsValue(resourcePayload.kind.toString())
            responseJsonPath("$.items[0].ownerId").equalsValue(resourcePayload.ownerId.toString())
            responseJsonPath("$.items[0].id").equalsValue(firstInsertedResourceId)
        }
    }

    @Test
    fun `ResourceClaim creation should be idempotent`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()

        val resourceId = testDataCreators.createResourceAndReturnId(ownerId = adId)
        val credentials = mapper.readTree("""{"name":"tull"}""")
        val resourceClaimPayload = ResourceClaimPayload(
            ownerId = PrincipalUID.fromString(adId),
            credentials = credentials,
            name = "ADMIN"
        )

        val initialClaim = testDataCreators.claimResource(
            ownerOfClaim = adId,
            resourceId = resourceId,
            credentials = credentials as ObjectNode,
            name = "ADMIN"
        )

        mockMvc.post(
            path = Path("/resource/{id}/claims", resourceId),
            headers = HttpHeaders().contentTypeJson(),
            body = resourceClaimPayload
        ) {
            statusIsOk()
            responseJsonPath("$.success").isTrue()
            responseJsonPath("$.items.length()").equalsValue(1)
            responseJsonPath("$.items[0].id").equalsValue(initialClaim.id.toString())
        }
    }

    @Test
    fun `Return claimed resources filtered by name and kind when resource and claim exists`() {
        repeat(5) {
            val resourceId = testDataCreators.createResourceAndReturnId(
                kind = ResourceKind.ManagedOracleSchema,
                name = UUID.randomUUID().toString()
            )
            testDataCreators.claimResource(resourceId = resourceId)
        }

        val adId = testDataCreators.createApplicationDeploymentAndReturnId()
        val resourceName = "myresource"
        val kind = ResourceKind.ManagedPostgresDatabase
        val resourceId = testDataCreators.createResourceAndReturnId(kind = kind, name = resourceName)
        testDataCreators.claimResource(resourceId = resourceId, ownerOfClaim = adId)

        mockMvc.get(Path("/resource?claimedBy={adId}&name={name}&kind={kind}", adId, resourceName, kind.toString())) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items.[0].name").equalsValue(resourceName)
                .responseJsonPath("$.items.[0].ownerId").isNotEmpty()
                .responseJsonPath("$.items[0].id").isNotEmpty()
                .responseJsonPath("$.items[0].kind").equalsValue(kind.toString())
                .responseJsonPath("$.items[0].claims.length()").equalsValue(1)
                .responseJsonPath("$.items[0].claims[0].ownerId").equalsValue(adId)
        }
    }

    @Test
    fun `Return claimed resources when resource and claim exists`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()
        val resourceId = testDataCreators.createResourceAndReturnId()
        testDataCreators.claimResource(resourceId = resourceId, ownerOfClaim = adId)

        mockMvc.get(Path("/resource?claimedBy={adId}", adId)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items.[0].name").equalsValue("resourceName")
                .responseJsonPath("$.items.[0].ownerId").isNotEmpty()
                .responseJsonPath("$.items[0].id").isNotEmpty()
                .responseJsonPath("$.items[0].kind").equalsValue("MinioPolicy")
                .responseJsonPath("$.items[0].claims.length()").equalsValue(1)
                .responseJsonPath("$.items[0].claims[0].ownerId").equalsValue(adId)
        }
    }

    @Test
    fun `Return list of resources claimed by applicationDeployment and do not include claims when there are several in DB`() {
        val ownerId = testDataCreators.createApplicationDeploymentAndReturnId()

        repeat(4) {
            testDataCreators.createResourceAndReturnId()
        }

        repeat(2) {
            testDataCreators.claimResource(
                ownerOfClaim = ownerId
            )
        }

        mockMvc.get(Path("/resource?claimedBy={ownerId}&includeClaims=false", ownerId)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(2)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items.[0].ownerId").equalsValue(ownerId)
                .responseJsonPath("$.items.[1].ownerId").equalsValue(ownerId)
        }
    }

    @Test
    fun `Return list of Resource claimedBy ApplicationDeployment and include all claims when there are several in DB`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()
        val resourceId = testDataCreators.createResourceAndReturnId(adId)
        testDataCreators.claimResource(
            resourceId = resourceId,
            ownerOfClaim = adId
        )

        val otherClaim = testDataCreators.claimResource(resourceId = resourceId)

        repeat(4) {
            testDataCreators.claimResource()
        }

        mockMvc.get(Path("/resource?claimedBy={claimedBy}&onlyMyClaims=false", adId)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].claims.length()").equalsValue(2)
                .responseJsonPath("$.items[0].claims[0].ownerId").equalsValue(adId)
                .responseJsonPath("$.items[0].claims[1].ownerId").equalsValue(otherClaim.ownerId.toString())
        }
    }

    @Test
    fun `Update Resource When it does not exist Then return 404 with error`() {
        val nonExistingId = "0"
        val updateResource = ResourcePayload(
            name = "newName",
            kind = ResourceKind.ManagedOracleSchema,
            ownerId = PrincipalUID.randomId()
        )

        mockMvc.put(
            path = Path("/resource/{id}", nonExistingId),
            headers = HttpHeaders().contentTypeJson(),
            body = updateResource
        ) {
            status(HttpStatus.NOT_FOUND)
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(nonExistingId)
        }
    }

    @Test
    fun `Update Resource When owner does not exist Then return 400 with error`() {
        val id = testDataCreators.createResourceAndReturnId()
        val updateResource = ResourcePayload(
            name = "newName",
            kind = ResourceKind.ManagedOracleSchema,
            ownerId = PrincipalUID.randomId()
        )

        mockMvc.put(
            path = Path("/resource/{id}", id),
            headers = HttpHeaders().contentTypeJson(),
            body = updateResource
        ) {
            status(HttpStatus.BAD_REQUEST)
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(id)
        }
    }

    @Test
    fun `Get resource When it is deactivated Then return HTTP_OK with no resources`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()
        val id = testDataCreators.createResourceAndReturnId(adId)
        testDataCreators.claimResource(ownerOfClaim = adId, resourceId = id)
        testDataCreators.deactivateResourceById(id)

        mockMvc.get(
            path = Path("/resource?claimedBy={id}", adId)
        ) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(0)
                .responseJsonPath("$.success").isTrue()
        }
    }

    @Test
    fun `Get resource When it is deactivated Then return HTTP_OK with the resource`() {
        val adId = testDataCreators.createApplicationDeploymentAndReturnId()
        val id = testDataCreators.createResourceAndReturnId(adId)
        testDataCreators.claimResource(ownerOfClaim = adId, resourceId = id)
        testDataCreators.deactivateResourceById(id)

        mockMvc.get(
            path = Path("/resource?claimedBy={id}&includeDeactivated=true", adId)
        ) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].active").isFalse()
        }
    }

    @Test
    fun `PATCH resource to active=true When it is deactivated return HTTP_OK`() {
        val id = testDataCreators.createResourceAndReturnId()
        testDataCreators.deactivateResourceById(id)

        mockMvc.patch(
            path = Path("/resource/{id}", id),
            headers = HttpHeaders().contentTypeJson(),
            body = UpdateResourcePayload(active = true)
        ) {
            statusIsOk()
            responseJsonPath("$.count").equalsValue(1)
            responseJsonPath("$.success").isTrue()
            responseJsonPath("$.items[0].id").equalsValue(id)
            responseJsonPath("$.items[0].active").isTrue()

            results.andExpect(jsonPath("$.items[0].setToCooldownAt").doesNotExist())
        }
    }

    @Test
    fun `PATCH resource When it does not exist return HTTP_NOT_FOUND`() {
        mockMvc.patch(
            path = Path("/resource/{id}", "90000"),
            headers = HttpHeaders().contentTypeJson(),
            body = UpdateResourcePayload(active = false)
        ) {
            status(HttpStatus.NOT_FOUND)
        }
    }
}
