package no.skatteetaten.aurora.herkimer.service

import no.skatteetaten.aurora.herkimer.dao.ResourceEntity
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.dao.ResourceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ResourceService(
    private val resourceRepository: ResourceRepository,
    private val principalService: PrincipalService
) {
    fun createResource(name: String, kind: ResourceKind, ownerId: UUID): ResourceDto = resourceRepository.save(
        ResourceEntity(
            kind = kind,
            name = name,
            ownerId = ownerId
        )
    ).toDto()

    fun findAll() = resourceRepository.findAll().map { it.toDto() }
    fun findById(id: Long) = resourceRepository.findByIdOrNull(id)?.toDto()

    fun findAllByOwnerId(ownerId: UUID) = resourceRepository.findAllByOwnerId(ownerId).map { it.toDto() }

    private fun ResourceEntity.toDto() = ResourceDto(
        assertNotNull(::id),
        name,
        kind,
        ownerId,
        assertNotNull(::createdDate),
        createdBy,
        assertNotNull(::modifiedDate),
        modifiedBy
    )

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
}
