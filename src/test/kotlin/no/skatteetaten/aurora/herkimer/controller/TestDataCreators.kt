package no.skatteetaten.aurora.herkimer.controller

import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.service.PrincipalService
import no.skatteetaten.aurora.herkimer.service.ResourceService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TestDataCreators(private val principalService: PrincipalService, private val resourceService: ResourceService) {

    fun createApplicationDeploymentAndReturnId() = principalService.createApplicationDeployment(
        name = "name",
        environmentName = "env",
        cluster = "cluster",
        applicationName = "whoami",
        businessGroup = "aurora"
    ).id.toString()

    fun createUserAndReturnId() = principalService.createUser(name = "name", id = "testid").id.toString()

    fun createResourceAndReturnId(ownerId: String = createApplicationDeploymentAndReturnId()) =
        resourceService.createResource(
            name = "resourceName",
            kind = ResourceKind.MinioPolicy,
            ownerId = UUID.fromString(ownerId)
        ).id.toString()
}
