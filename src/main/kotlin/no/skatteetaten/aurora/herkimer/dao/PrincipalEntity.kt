package no.skatteetaten.aurora.herkimer.dao

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Table("principal")
data class PrincipalEntity(
    @Id
    var id: PrincipalUID? = null,
    val type: PrincipalType,
    val name: String,
    val environmentName: String? = null,
    val cluster: String? = null,
    val businessGroup: String? = null,
    val userId: String? = null,

    @CreatedDate
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    val modifiedDate: LocalDateTime? = null,

    val createdBy: String = "aurora",
    val modifiedBy: String = "aurora"
)

enum class PrincipalType {
    ApplicationDeployment, User
}

@Repository
interface PrincipalRepository : CrudRepository<PrincipalEntity, PrincipalUID> {
    fun findByType(principalType: PrincipalType): List<PrincipalEntity>

    fun findByNameAndEnvironmentNameAndClusterAndBusinessGroup(
        name: String,
        environmentName: String,
        cluster: String,
        businessGroup: String
    ): PrincipalEntity

    fun findByNameAndUserId(
        name: String,
        userId: String
    ): PrincipalEntity
}
