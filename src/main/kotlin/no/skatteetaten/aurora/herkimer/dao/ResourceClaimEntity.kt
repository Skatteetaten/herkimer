package no.skatteetaten.aurora.herkimer.dao

import com.fasterxml.jackson.databind.node.ObjectNode
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
    val id: Int? = null,
    val ownerId: PrincipalUID,
    val resourceId: Int,
    val name: String,
    val credentials: ObjectNode,

    @CreatedDate
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    val modifiedDate: LocalDateTime? = null,

    val createdBy: String = "aurora",
    val modifiedBy: String = "aurora"
)

@Repository
interface ResourceClaimRepository : CrudRepository<ResourceClaimEntity, Int> {
    @Query(
        "SELECT id, owner_id, credentials, resource_id, name, created_date, modified_date, created_by, modified_by " +
            "FROM resource_claim WHERE owner_id= :ownerId AND resource_id= :resourceId AND credentials= :credentials and name= :name"
    )
    fun findByProperties(
        ownerId: PrincipalUID,
        resourceId: Int,

        // Spring Data JDBC is not able to use ObjectNode here, although we have a custom converter
        credentials: String,
        name: String
    ): ResourceClaimEntity
}
