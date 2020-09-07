package no.skatteetaten.aurora.herkimer.dao

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Table("resource_claim")
data class ResourceClaimEntity(

    @Id
    val id: Long? = null,
    val ownerId: PrincipalUID,
    val resourceId: Long,
    val credentials: JsonNode,

    @CreatedDate
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    val modifiedDate: LocalDateTime? = null,

    val createdBy: String = "aurora",
    val modifiedBy: String = "aurora"
)

@Repository
interface ResourceClaimRepository : CrudRepository<ResourceClaimEntity, Long> {

    @Query("SELECT id, owner_id, resource_id, credentials, created_date, modified_date, created_by, modified_by " +
        "FROM resource_claim WHERE resource_id=:resourceId")
    fun findAllByResourceId(resourceId: Long): List<ResourceClaimEntity>

    @Query("SELECT id, owner_id, resource_id, credentials, created_date, modified_date, created_by, modified_by " +
        "FROM resource_claim WHERE owner_id=:ownerId")
    fun findAllByOwnerId(ownerId: PrincipalUID): List<ResourceClaimEntity>
}
