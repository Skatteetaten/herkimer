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

data class ApplicationDeploymentPayload(
    val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String,
    val applicationName: String
)

@RestController
@RequestMapping("/applicationDeployment")
class ApplicationDeploymentController(
    private val principalService: PrincipalService
) {
    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID) = AuroraResponse(
        items = listOf(principalService.findApplicationDeployment(id).toResource())
    )

    @GetMapping
    fun findAll() = AuroraResponse(items = principalService.findAllApplicationDeployment().map { it.toResource() })

    @PostMapping
    fun create(@RequestBody payload: ApplicationDeploymentPayload) =
        AuroraResponse(
            items = listOf(
                payload.run {

                    principalService.createApplicationDeployment(
                        name,
                        environmentName,
                        cluster,
                        businessGroup,
                        applicationName
                    )
                }.toResource()
            )
        )

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody payload: ApplicationDeploymentPayload
    ): AuroraResponse<ApplicationDeploymentResource> {
        val existingAd = principalService.findApplicationDeployment(id)
        return AuroraResponse(
            items = listOf(
                payload.run {
                    principalService.updateApplicationDeployment(
                        existingAd.copy(
                            name = name,
                            applicationName = applicationName,
                            businessGroup = businessGroup,
                            cluster = cluster,
                            environmentName = environmentName
                        )
                    )
                }.toResource()
            )
        )
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = principalService.deleteApplicationDeployment(id)
}
