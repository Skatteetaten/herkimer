package no.skatteetaten.aurora.herkimer.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
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

    fun createApplicationDeploymentAndReturnId(prefix: String = Math.random().toString()) =
        principalService.createApplicationDeployment(
            name = "$prefix-name",
            environmentName = "$prefix-env",
            cluster = "$prefix-cluster",
            businessGroup = "$prefix-aurora"
        ).id.toString()

    fun createUserAndReturnId() = principalService.createUser(name = "name", userId = "testid").id.toString()

    fun createResourceAndReturnId(
        ownerId: String = createApplicationDeploymentAndReturnId(),
        kind: ResourceKind = ResourceKind.MinioPolicy,
        name: String = "resourceName"
    ) =
        resourceService.createResource(
            name = name,
            kind = kind,
            ownerId = PrincipalUID.fromString(ownerId),
            parentId = null
        ).id.toString()

    fun claimResource(
        ownerOfClaim: String = createApplicationDeploymentAndReturnId(),
        resourceId: String = createResourceAndReturnId(ownerId = ownerOfClaim, name = "myName-${Math.random()}"),
        credentials: ObjectNode = objectMapper.readTree("""{"user":"testUser"}""") as ObjectNode,
        name: String = "READ"
    ) = resourceService.createResourceClaim(
        ownerId = PrincipalUID.fromString(ownerOfClaim),
        resourceId = resourceId.toInt(),
        credentials = credentials,
        name = name
    )
}
