package no.skatteetaten.aurora.herkimer.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.service.ByClaimedBy
import no.skatteetaten.aurora.herkimer.service.ByNameAndKind
import no.skatteetaten.aurora.herkimer.service.PrincipalService
import no.skatteetaten.aurora.herkimer.service.ResourceService

data class ResourcePayload(
    val name: String,
    val kind: ResourceKind,
    val ownerId: PrincipalUID,
    val parentId: Int? = null
)

data class ResourceClaimPayload(
    val ownerId: PrincipalUID,
    val credentials: JsonNode,
    val name: String
)

@RestController
@RequestMapping("/resource")
class ResourceController(
    private val resourceService: ResourceService,
    private val principalService: PrincipalService
) {
    @PostMapping
    fun create(@RequestBody payload: ResourcePayload): AuroraResponse<Resource> {
        val existingOwner = principalService.findById(payload.ownerId)
            ?: throw NoSuchResourceException("Could not find Principal with id=${payload.ownerId}")

        return payload.run {
            resourceService.createResource(
                name = name,
                kind = kind,
                ownerId = existingOwner.id,
                parentId = parentId
            )
        }.toResource()
            .okResponse()
    }

    @PostMapping("/{resourceId}/claims")
    fun createClaimforResource(
        @PathVariable resourceId: Int,
        @RequestBody payload: ResourceClaimPayload
    ): AuroraResponse<ResourceClaim> {

        requireNotNull(principalService.findById(payload.ownerId)) {
            "Cannot create claim for resource with id=$resourceId. Owner does not exist, ownerId=${payload.ownerId}"
        }

        requireNotNull(resourceService.findById(resourceId)) {
            "Cannot create claim. Resource with id=$resourceId does not exist."
        }

        require(payload.credentials is ObjectNode) {
            "Credentials has to be JSON object. Arrays are not allowed."
        }

        return resourceService.createResourceClaim(payload.ownerId, resourceId, payload.credentials, payload.name)
            .toResource()
            .okResponse()
    }

    @GetMapping
    fun findAllResourcesByFilters(
        @RequestParam(required = false) claimedBy: PrincipalUID?,
        @RequestParam(required = false, defaultValue = "true") includeClaims: Boolean,
        @RequestParam(required = false, defaultValue = "true") onlyMyClaims: Boolean,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) resourceKind: ResourceKind?,
        @RequestParam(defaultValue = "false") includeDeactivated: Boolean
    ): AuroraResponse<Resource> {
        if (claimedBy != null && principalService.findById(claimedBy) == null) return AuroraResponse()

        val params = when {
            claimedBy != null -> ByClaimedBy(claimedBy, name, resourceKind, onlyMyClaims)
            name != null && resourceKind != null -> ByNameAndKind(name, resourceKind)
            else -> throw IllegalArgumentException("When claimedBy is not specified name and resourceKind is required.")
        }

        return resourceService.findAllResourcesByParams(params, includeClaims, includeDeactivated)
            .toResources()
            .okResponse()
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Int,
        @RequestParam(required = false, defaultValue = "false") includeClaims: Boolean
    ) =
        resourceService.findById(id, includeClaims)?.toResource()?.okResponse()
            ?: throw NoSuchResourceException("Could not find Resource with id=$id")

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody payload: ResourcePayload
    ): AuroraResponse<Resource> {
        val existingResource = resourceService.findById(id)
            ?: throw NoSuchResourceException("Could not find Resource with id=$id")

        requireNotNull(principalService.findById(payload.ownerId)) {
            "The provided ownerId for the resource does not exist, resourceId=$id and ownerId=${payload.ownerId}"
        }

        return payload.run {
            resourceService.updateResource(
                existingResource.copy(
                    name = name,
                    ownerId = ownerId,
                    kind = kind
                )
            ).toResource()
                .okResponse()
        }
    }

    @PatchMapping("/{id}")
    fun updateResource(
        @PathVariable id: Int,
        @RequestBody updateRequest: UpdateResourcePayload
    ): AuroraResponse<Resource> {
        return resourceService.updateActive(id, updateRequest.active)
            ?.toResource()
            ?.okResponse()
            ?: throw NoSuchResourceException("Could not find Resource with id=$id")
    }
}

data class UpdateResourcePayload(
    val active: Boolean
)
