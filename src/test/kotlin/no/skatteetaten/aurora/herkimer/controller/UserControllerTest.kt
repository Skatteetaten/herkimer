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
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

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
    fun `Return Not Found User when there are none in DB`() {
        val nonExistingUserId = UUID.randomUUID().toString()
        mockMvc.get(Path("/user/{nonExistingAdId}", nonExistingUserId)) {
            status(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Return User when there are one in DB`() {
        val id = createUserAndReturnId()

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
        createUser()

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
        val id = createUserAndReturnId()

        mockMvc.delete(Path("/user/{adId}", id)) {
            statusIsOk()
        }
    }

    @Test
    fun `Update User When it exists Then return HTTP_OK and updated resource`() {
        val (createdTime, id) = mockLocalDateTimeToNow {
            createUserAndReturnId()
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

    private fun createUserAndReturnId() = jacksonObjectMapper().configureDefaults()
        .readValue<AuroraResponse<UserResource>>(createUser().response.contentAsString)
        .items
        .single()
        .id

    private fun createUser(
        user: UserPayload = UserPayload(
            name = "name",
            userId = "testid"
        ),
        fn: MockMvcData.() -> Unit = {}
    ) = mockMvc.post(
        path = Path("/user/"),
        body = user,
        headers = HttpHeaders().contentTypeJson(),
        fn = fn
    )
}
