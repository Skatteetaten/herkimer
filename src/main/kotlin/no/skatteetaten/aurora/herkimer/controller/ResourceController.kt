package no.skatteetaten.aurora.herkimer.controller

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.service.PrincipalService
import no.skatteetaten.aurora.herkimer.service.ResourceService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class ResourcePayload(
    val name: String,
    val kind: ResourceKind,
    val ownerId: PrincipalUID
)

data class ResourceClaimPayload(
    val ownerId: PrincipalUID,
    val credentials: JsonNode
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
                ownerId = existingOwner.id
            )
        }.toResource()
            .okResponse()
    }

    @PostMapping("/{resourceId}/claims")
    fun createClaimforResource(
        @PathVariable resourceId: Long,
        @RequestBody payload: ResourceClaimPayload
    ): AuroraResponse<ResourceClaim> {

        requireNotNull(principalService.findById(payload.ownerId)) {
            "Cannot create claim for resource with id=$resourceId. Owner does not exist, ownerId=${payload.ownerId}"
        }

        requireNotNull(resourceService.findById(resourceId)) {
            "Cannot create claim. Resource with id=$resourceId does not exist."
        }

        return resourceService.createResourceClaim(payload.ownerId, resourceId, payload.credentials)
            .toResource()
            .okResponse()
    }

    @GetMapping
    fun findAllClaimedBy(
        @RequestParam(required = true) claimedBy: PrincipalUID,
        @RequestParam(required = false, defaultValue = "true") includeClaims: Boolean,
        @RequestParam(required = false, defaultValue = "true") onlyMyClaims: Boolean
    ): AuroraResponse<Resource> {
        return if (principalService.findById(claimedBy) == null) AuroraResponse()
        else resourceService.findAllClaimedBy(claimedBy, includeClaims, onlyMyClaims).toResources().okResponse()
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long,
        @RequestParam(required = false, defaultValue = "false") includeClaims: Boolean
    ) =
        resourceService.findById(id, includeClaims)?.toResource()?.okResponse()
            ?: throw NoSuchResourceException("Could not find Resource with id=$id")

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
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

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = resourceService.deleteById(id)
}
