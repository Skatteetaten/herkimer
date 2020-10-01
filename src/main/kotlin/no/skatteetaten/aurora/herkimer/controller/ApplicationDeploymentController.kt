package no.skatteetaten.aurora.herkimer.controller

import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.service.PrincipalService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class ApplicationDeploymentPayload(
    val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String
)

@RestController
@RequestMapping("/applicationDeployment")
class ApplicationDeploymentController(
    private val principalService: PrincipalService
) {
    @GetMapping("/{id}")
    fun findById(@PathVariable id: PrincipalUID): AuroraResponse<ApplicationDeployment> =
        principalService.findApplicationDeployment(id)?.toResource()?.okResponse()
            ?: throw NoSuchResourceException("Could not find ApplicationDeployment with id=$id")

    @GetMapping
    fun findAll() =
        principalService.findAllApplicationDeployment()
            .toResources()
            .okResponse()

    @PostMapping
    fun create(@RequestBody payload: ApplicationDeploymentPayload) =
        payload.run {
            principalService.createApplicationDeployment(
                name,
                environmentName,
                cluster,
                businessGroup
            ).toResource()
                .okResponse()
        }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: PrincipalUID,
        @RequestBody payload: ApplicationDeploymentPayload
    ): AuroraResponse<ApplicationDeployment> {
        val existingAd = principalService.findApplicationDeployment(id)
            ?: throw NoSuchResourceException("Could not find ApplicationDeployment with id=$id")
        return payload.run {
            principalService.updateApplicationDeployment(
                existingAd.copy(
                    name = name,
                    businessGroup = businessGroup,
                    cluster = cluster,
                    environmentName = environmentName
                )
            ).toResource()
                .okResponse()
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: PrincipalUID) = principalService.deleteApplicationDeployment(id)
}
