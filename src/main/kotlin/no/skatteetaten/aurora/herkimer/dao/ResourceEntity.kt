package no.skatteetaten.aurora.herkimer.dao

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import java.util.UUID

@Table("resource")
data class ResourceEntity(
    @Id
    val id: Long? = null,
    val kind: ResourceKind,
    val name: String,
    val ownerId: UUID,

    @CreatedDate
    val createdDate: LocalDateTime? = null,
    @LastModifiedDate
    val modifiedDate: LocalDateTime? = null,

    val createdBy: String = "aurora",
    val modifiedBy: String = "aurora"
)

enum class ResourceKind {
    MinioPolicy, ManagedPostgresDatabase, ManagedOracleSchema, ExternalSchema
}

interface ResourceRepository : CrudRepository<ResourceEntity, Long> {

    @Query("SELECT id, kind, name, owner_id, created_date, modified_date, created_by, modified_by " +
        "FROM RESOURCE WHERE owner_id = :ownerId")
    fun findAllByOwnerId(ownerId: UUID): List<ResourceEntity>
}
