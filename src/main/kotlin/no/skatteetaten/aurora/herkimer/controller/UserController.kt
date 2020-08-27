package no.skatteetaten.aurora.herkimer.controller

import no.skatteetaten.aurora.herkimer.service.PrincipalService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
/*
    @PostMapping("/user")
    fun createUser(
        @RequestBody user: User
    ) = userRepository.save(user)

    @GetMapping("/user")
    fun getAllUsers() = userRepository.findAll().toList()
*/
}

@RestController
@RequestMapping("/principal")
class PrincipalController(
    private val principalService: PrincipalService
) {

/*
    @GetMapping("/{principalId}")
    fun getPrincipalById(@PathVariable principalId: Long): ResponseEntity<PrincipalDto> =
        principalService.findPrincipalById(principalId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
*/
}

@RestController
@RequestMapping("/resource")
class ResourceController {
    @GetMapping
    fun getAllResourcesByPrincipalId(
        @RequestParam principalId: String
    ) = ""
}
