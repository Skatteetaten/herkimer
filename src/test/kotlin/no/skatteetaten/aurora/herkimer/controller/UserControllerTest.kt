package no.skatteetaten.aurora.herkimer.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
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

@AutoConfigureEmbeddedDatabase(provider = ZONKY)
@SpringBootTest(properties = ["aurora.authentication.token.value=secret_from_file", "aurora.authentication.enabled=false"])
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testDataCreators: TestDataCreators

    @Autowired
    private lateinit var flyway: Flyway

    @BeforeEach
    fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `Create User when payload is sent`() {
        mockLocalDateTimeToNow { expectedTime ->
            mockMvc.post(
                path = Path("/user"),
                headers = HttpHeaders().contentTypeJson(),
                body = UserPayload("testUser", "name")
            ) {
                statusIsOk()
                    .responseJsonPath("$.count").equalsValue(1)
                    .responseJsonPath("$.success").isTrue()
                    .responseJsonPath("$.items[0].id").isNotEmpty()
                    .responseJsonPath("$.items[0].name").equalsValue("name")
                    .responseJsonPath("$.items[0]userId").equalsValue("testUser")
                    .validateAuditing(expectedTime)
            }
        }
    }

    @Test
    fun `Create two Users when they have same property values then return same User`() {
        mockLocalDateTimeToNow { expectedTime ->
            val userPayload = UserPayload(
                name = "name",
                userId = "userid"
            )

            val firstUserResource = mockMvc.post(
                path = Path("/user/"),
                body = userPayload,
                headers = HttpHeaders().contentTypeJson(),
                fn = {}
            ).response.contentAsString

            val secondUserResource = mockMvc.post(
                path = Path("/user/"),
                body = userPayload,
                headers = HttpHeaders().contentTypeJson(),
                fn = {}
            ).response.contentAsString

            assertThat(firstUserResource).isEqualTo(secondUserResource)
        }
    }

    @Test
    fun `Return Not Found User when there are none in DB`() {
        val nonExistingUserId = PrincipalUID.randomId().toString()
        mockMvc.get(Path("/user/{nonExistingUserId}", nonExistingUserId)) {
            status(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Return User when there are one in DB`() {
        val id = testDataCreators.createUserAndReturnId()

        mockMvc.get(Path("/user/{id}", id)) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items[0].name").equalsValue("name")
                .responseJsonPath("$.items[0].id").equalsValue(id)
                .responseJsonPath("$.items[0].userId").equalsValue("testid")
        }
    }

    @Test
    fun `Return list of User when there are one in DB`() {
        testDataCreators.createUserAndReturnId()

        mockMvc.get(Path("/user/")) {
            statusIsOk()
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.success").isTrue()
                .responseJsonPath("$.items.[0].name").equalsValue("name")
                .responseJsonPath("$.items.[0].userId").equalsValue("testid")
        }
    }

    @Test
    fun `Delete User When it exists Then return HTTP_OK`() {
        val id = testDataCreators.createUserAndReturnId()

        mockMvc.delete(Path("/user/{id}", id)) {
            statusIsOk()
        }
    }

    @Test
    fun `Update User When it exists Then return HTTP_OK and updated resource`() {
        val (createdTime, id) = mockLocalDateTimeToNow {
            testDataCreators.createUserAndReturnId()
        }

        val updatedUser = UserPayload(
            name = "herkimer",
            userId = "007"
        )

        mockLocalDateTimeToNow { modifiedTime ->
            mockMvc.put(
                path = Path("/user/{id}", id),
                headers = HttpHeaders().contentTypeJson(),
                body = updatedUser
            ) {
                statusIsOk()
                    .responseJsonPath("$.count").equalsValue(1)
                    .responseJsonPath("$.success").isTrue()
                    .responseJsonPath("$.items[0].id").equalsValue(id)
                    .responseJsonPath("$.items[0].name").equalsValue("herkimer")
                    .responseJsonPath("$.items[0].userId").equalsValue("007")
                    .validateAuditing(
                        createdTime = createdTime,
                        modifiedTime = modifiedTime
                    )
            }
        }
    }

    @Test
    fun `Update User When it does not exists Then return 404 with error`() {
        val nonExistingId = PrincipalUID.randomId().toString()
        val updatedUser = UserPayload(
            name = "herkimer",
            userId = "007"
        )

        mockMvc.put(
            path = Path("/user/{id}", nonExistingId),
            headers = HttpHeaders().contentTypeJson(),
            body = updatedUser
        ) {
            status(HttpStatus.NOT_FOUND)
                .responseJsonPath("$.count").equalsValue(1)
                .responseJsonPath("$.errors[0].errorMessage").contains(nonExistingId)
        }
    }
}
