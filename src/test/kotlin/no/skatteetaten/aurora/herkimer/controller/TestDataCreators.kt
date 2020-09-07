package no.skatteetaten.aurora.herkimer.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.service.PrincipalService
import no.skatteetaten.aurora.herkimer.service.ResourceService
import org.springframework.stereotype.Component

@Component
class TestDataCreators(
    private val principalService: PrincipalService,
    private val resourceService: ResourceService,
    private val objectMapper: ObjectMapper
) {

    fun createApplicationDeploymentAndReturnId(prefix: String = Math.random().toString()) = principalService.createApplicationDeployment(
        name = "$prefix-name",
        environmentName = "$prefix-env",
        cluster = "$prefix-cluster",
        applicationName = "$prefix-whoami",
        businessGroup = "$prefix-aurora"
    ).id.toString()

    fun createUserAndReturnId() = principalService.createUser(name = "name", userId = "testid").id.toString()

    fun createResourceAndReturnId(ownerId: String = createApplicationDeploymentAndReturnId()) =
        resourceService.createResource(
            name = "resourceName",
            kind = ResourceKind.MinioPolicy,
            ownerId = PrincipalUID.fromString(ownerId)
        ).id.toString()

    fun claimResource(
        ownerOfClaim: String = createApplicationDeploymentAndReturnId(),
        resourceId: String = createResourceAndReturnId(ownerOfClaim),
        credentials: String = """{"user":"testUser"}"""
    ) = resourceService.createResourceClaim(
        PrincipalUID.fromString(ownerOfClaim),
        resourceId.toLong(),
        objectMapper.convertValue(credentials)
    )
}
