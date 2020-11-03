package no.skatteetaten.aurora.herkimer.service

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.dao.ResourceClaimEntity
import no.skatteetaten.aurora.herkimer.dao.ResourceClaimRepository
import no.skatteetaten.aurora.herkimer.dao.ResourceEntity
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.dao.ResourceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

sealed class FindParams
data class ByNameAndKind(val name: String, val resourceKind: ResourceKind) : FindParams()
data class ByClaimedBy(val claimedBy: PrincipalUID, val name: String?, val resourceKind: ResourceKind?, val onlyMyClaims: Boolean) : FindParams()

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

    fun deleteById(id: Int) = resourceRepository.deleteById(id)

    fun findAllResourcesByParams(
        findParams: FindParams,
        includeClaims: Boolean
    ): List<ResourceDto> {
        val resources = findParams.run {
            when (this) {
                is ByNameAndKind -> resourceRepository.findByKindAndName(resourceKind, name)
                is ByClaimedBy -> resourceRepository.findAllClaimedBy(claimedBy, name ?: "%", resourceKind?.toString() ?: "%")
            }
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

    fun createResourceClaim(ownerId: PrincipalUID, resourceId: Int, credentials: JsonNode): ResourceClaimDto =
        runCatching {
            resourceClaimRepository.save(
                ResourceClaimEntity(
                    ownerId = ownerId,
                    resourceId = resourceId,
                    credentials = credentials
                )
            )
        }.getOrElseReturnIfDuplicate {
            resourceClaimRepository.findByProperties(
                ownerId = ownerId,
                resourceId = resourceId,
                credentials = credentials
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
    parentId = parentId

)

fun ResourceClaimEntity.toDto() =
    ResourceClaimDto(
        id = assertNotNull(::id),
        ownerId = ownerId,
        resourceId = resourceId,
        credentials = credentials,
        createdDate = assertNotNull(::createdDate),
        modifiedDate = assertNotNull(::modifiedDate),
        createdBy = createdBy,
        modifiedBy = modifiedBy
    )
