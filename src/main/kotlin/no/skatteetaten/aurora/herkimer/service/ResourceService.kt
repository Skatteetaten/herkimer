package no.skatteetaten.aurora.herkimer.service

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.herkimer.dao.ResourceClaimEntity
import no.skatteetaten.aurora.herkimer.dao.ResourceClaimRepository
import no.skatteetaten.aurora.herkimer.dao.ResourceEntity
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.dao.ResourceRepository
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ResourceService(
    private val resourceRepository: ResourceRepository,
    private val resourceClaimRepository: ResourceClaimRepository
) {
    fun createResource(name: String, kind: ResourceKind, ownerId: PrincipalUID): ResourceDto = resourceRepository.save(
        ResourceEntity(
            kind = kind,
            name = name,
            ownerId = ownerId
        )
    ).toDto()

    fun findAll() = resourceRepository.findAll().map { it.toDto() }
    fun findById(id: Long, includeClaims: Boolean = false) =
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
                createdDate = createdDate,
                modifiedDate = modifiedDate,
                createdBy = createdBy,
                modifiedBy = modifiedBy
            )
        )
    }.toDto()

    fun deleteById(id: Long) = resourceRepository.deleteById(id)

    fun findAllClaimedBy(claimedBy: PrincipalUID, includeClaims: Boolean, onlyMyClaims: Boolean): List<ResourceDto> {
        val resources = resourceRepository.findAllClaimedBy(claimedBy)
        return when {
            !includeClaims -> resources.map { it.toDto() }
            !onlyMyClaims -> resources.map { resource ->
                resource.toDto(
                    formattedClaims = resource.claims.map(ResourceClaimEntity::toDto)
                )
            }
            else -> {
                resources.map { resource ->
                    val onlyMyClaims = resource.claims
                        .filter { it.ownerId == claimedBy }
                        .map(ResourceClaimEntity::toDto)

                    resource.toDto(
                        formattedClaims = onlyMyClaims
                    )
                }
            }
        }
    }

    fun createResourceClaim(ownerId: PrincipalUID, resourceId: Long, credentials: JsonNode): ResourceClaimDto =
        resourceClaimRepository.save(
            ResourceClaimEntity(
                ownerId = ownerId,
                resourceId = resourceId,
                credentials = credentials
            )
        ).toDto()
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
    claims = formattedClaims

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
