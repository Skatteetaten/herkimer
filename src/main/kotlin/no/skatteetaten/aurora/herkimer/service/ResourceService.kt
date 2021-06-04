package no.skatteetaten.aurora.herkimer.service

import java.time.LocalDateTime
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import com.fasterxml.jackson.databind.node.ObjectNode
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.dao.ResourceClaimEntity
import no.skatteetaten.aurora.herkimer.dao.ResourceClaimRepository
import no.skatteetaten.aurora.herkimer.dao.ResourceEntity
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.dao.ResourceRepository

sealed class FindParams
data class ByNameAndKind(val name: String, val resourceKind: ResourceKind) : FindParams()
data class ByClaimedBy(
    val claimedBy: PrincipalUID,
    val name: String?,
    val resourceKind: ResourceKind?,
    val onlyMyClaims: Boolean
) : FindParams()

@Component
class ResourceService(
    private val resourceRepository: ResourceRepository,
    private val resourceClaimRepository: ResourceClaimRepository
) {
    fun createResource(name: String, kind: ResourceKind, ownerId: PrincipalUID, parentId: Int?): ResourceDto =
        runCatching {
            resourceRepository.save(
                ResourceEntity(
                    kind = kind,
                    name = name,
                    ownerId = ownerId,
                    parentId = parentId
                )
            )
        }.getOrElseReturnIfDuplicate {
            resourceRepository.findByKindAndNameAndOwnerId(
                kind = kind,
                name = name,
                ownerId = ownerId,
                parentId = parentId
            )
        }.toDto()

    fun findById(id: Int, includeClaims: Boolean = false) =
        resourceRepository.findByIdOrNull(id)
            ?.let {
                val claimsOrNull =
                    if (includeClaims) it.claims.map(ResourceClaimEntity::toDto)
                    else null

                it.toDto(claimsOrNull)
            }

    fun updateResource(updateResourceDto: ResourceDto): ResourceDto = updateResourceDto.run {
        resourceRepository.save(
            ResourceEntity(
                id = id,
                kind = kind,
                name = name,
                ownerId = ownerId,
                parentId = parentId,
                createdDate = createdDate,
                modifiedDate = modifiedDate,
                createdBy = createdBy,
                modifiedBy = modifiedBy
            )
        )
    }.toDto()

    fun updateActive(id: Int, active: Boolean): ResourceDto? {
        if (active) {
            resourceRepository.activateResourceById(id)
        } else {
            resourceRepository.deactiveResourceById(id, LocalDateTime.now())
        }

        return findById(id)
    }

    fun findAllResourcesByParams(
        findParams: FindParams,
        includeClaims: Boolean,
        includeDeactivated: Boolean
    ): List<ResourceDto> {
        val resources = findParams.run {
            when (this) {
                is ByNameAndKind -> {
                    resourceRepository.findByKindAndName(resourceKind, name)
                }
                is ByClaimedBy -> {
                    resourceRepository.findAllClaimedBy(
                        claimedBy,
                        name ?: "%",
                        resourceKind?.toString() ?: "%"
                    )
                }
            }
        }.let { resources ->
            if (!includeDeactivated) {
                resources.filter { it.active }
            } else resources
        }

        return when {
            !includeClaims -> resources.map { it.toDto() }
            findParams is ByClaimedBy && findParams.onlyMyClaims -> resources.map { resource ->
                val onlyMyClaims = resource.claims
                    .filter { it.ownerId == findParams.claimedBy }
                    .map(ResourceClaimEntity::toDto)

                resource.toDto(
                    formattedClaims = onlyMyClaims
                )
            }
            else -> {
                resources.map { resource ->

                    resource.toDto(
                        formattedClaims = resource.claims.map(ResourceClaimEntity::toDto)
                    )
                }
            }
        }
    }

    fun createResourceClaim(
        ownerId: PrincipalUID,
        resourceId: Int,
        credentials: ObjectNode,
        name: String
    ): ResourceClaimDto =
        runCatching {
            resourceClaimRepository.save(
                ResourceClaimEntity(
                    ownerId = ownerId,
                    resourceId = resourceId,
                    credentials = credentials,
                    name = name
                )
            )
        }.getOrElseReturnIfDuplicate {
            resourceClaimRepository.findByProperties(
                ownerId = ownerId,
                resourceId = resourceId,
                credentials = credentials,
                name = name
            )
        }.toDto()
}

fun ResourceEntity.toDto(formattedClaims: List<ResourceClaimDto>? = null) = ResourceDto(
    id = assertNotNull(::id),
    name = name,
    kind = kind,
    ownerId = ownerId,
    createdDate = assertNotNull(::createdDate),
    createdBy = createdBy,
    modifiedDate = assertNotNull(::modifiedDate),
    modifiedBy = modifiedBy,
    claims = formattedClaims,
    parentId = parentId,
    active = active,
    setToCooldownAt = setToCooldownAt
)

fun ResourceClaimEntity.toDto() =
    ResourceClaimDto(
        id = assertNotNull(::id),
        ownerId = ownerId,
        name = name,
        resourceId = resourceId,
        credentials = credentials,
        createdDate = assertNotNull(::createdDate),
        modifiedDate = assertNotNull(::modifiedDate),
        createdBy = createdBy,
        modifiedBy = modifiedBy
    )
