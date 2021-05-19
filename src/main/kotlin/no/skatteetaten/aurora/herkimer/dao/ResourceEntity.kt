package no.skatteetaten.aurora.herkimer.dao

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Table("resource")
data class ResourceEntity(
    @Id
    val id: Int? = null,
    val kind: ResourceKind,
    val name: String,
    val ownerId: PrincipalUID,
    val parentId: Int?,
    @Column("resource_id")
    val claims: Set<ResourceClaimEntity> = emptySet(),

    @CreatedDate
    val createdDate: LocalDateTime? = null,
    @LastModifiedDate
    val modifiedDate: LocalDateTime? = null,

    val createdBy: String = "aurora",
    val modifiedBy: String = "aurora"
)

enum class ResourceKind {
    MinioPolicy, MinioObjectArea, ManagedPostgresDatabase, ManagedOracleSchema, ExternalSchema, PostgresDatabaseInstance, OracleDatabaseInstance, StorageGridTenant, StorageGridObjectArea
}

@Repository
interface ResourceRepository : CrudRepository<ResourceEntity, Int> {
    @Query(
        "SELECT * FROM resource r " +
            "INNER JOIN resource_claim rc on r.id = rc.resource_id " +
            "WHERE rc.owner_id=:claimedBy AND r.name LIKE :name AND r.kind LIKE :resourceKind"
    )
    fun findAllClaimedBy(claimedBy: PrincipalUID, name: String, resourceKind: String): Set<ResourceEntity>

    fun findByKindAndName(kind: ResourceKind, name: String): Set<ResourceEntity>

    fun findByKindAndNameAndOwnerId(kind: ResourceKind, name: String, ownerId: PrincipalUID, parentId: Int?): ResourceEntity
}
