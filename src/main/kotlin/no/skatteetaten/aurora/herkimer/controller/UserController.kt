package no.skatteetaten.aurora.herkimer.controller

import no.skatteetaten.aurora.herkimer.service.PrincipalService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class UserPayload(
    val userId: String,
    val name: String
)

@RestController
@RequestMapping("/user")
class UserController(private val principalService: PrincipalService) {
    @PostMapping
    fun createUser(
        @RequestBody userPayload: UserPayload
    ): AuroraResponse<UserResource> =
        userPayload.run {
            principalService.createUser(
                id = userId,
                name = name
            ).toResource()
                .okResponse()
        }

    @GetMapping
    fun getAllUsers(): AuroraResponse<UserResource> =
        principalService.findAllUsers()
            .toResources()
            .okResponse()

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID) =
        principalService.findUser(id)?.toResource()?.okResponse()
            ?: throw NoSuchResourceException("Could not find User with id=$id")

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody payload: UserPayload
    ): AuroraResponse<UserResource> {
        val existingUser = principalService.findUser(id)
            ?: throw NoSuchResourceException("Could not find User with id=$id")

        return payload.run {
            principalService.updateUser(
                existingUser.copy(
                    name = name,
                    userId = userId
                )
            ).toResource()
                .okResponse()
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = principalService.deleteUser(id)
}
