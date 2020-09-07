package no.skatteetaten.aurora.herkimer.controller

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
import java.util.UUID

data class ResourcePayload(
    val name: String,
    val kind: ResourceKind,
    val ownerId: UUID
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

    @GetMapping
    fun findAllByOwnerId(@RequestParam(required = false) ownerId: UUID?): AuroraResponse<Resource> =
        when (ownerId) {
            null -> resourceService.findAll()
            else -> {
                principalService.findById(ownerId)
                    ?: throw NoSuchResourceException("Could not find principal with id=$ownerId")

                resourceService.findAllByOwnerId(ownerId)
            }
        }.toResources().okResponse()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) =
        resourceService.findById(id)?.toResource()?.okResponse()
            ?: throw NoSuchResourceException("Could not find Resource with id=$id")

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody payload: ResourcePayload
    ): AuroraResponse<Resource> {
        val existingResource = resourceService.findById(id)
            ?: throw NoSuchResourceException("Could not find Resource with id=$id")

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
