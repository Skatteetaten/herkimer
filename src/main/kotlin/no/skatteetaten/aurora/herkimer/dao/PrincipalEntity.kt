package no.skatteetaten.aurora.herkimer.dao

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jdbc.repository.query.Query
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
    val applicationName: String? = null,
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
    @Query(
        "SELECT type, name, environment_name, cluster, user_id, id, business_group, " +
            "application_name, created_date, created_by, modified_by, modified_date " +
            "FROM PRINCIPAL WHERE type LIKE :principalType"
    )
    fun findAllPrincipalByType(principalType: String): List<PrincipalEntity>

    @Query(
        "SELECT type, name, environment_name, cluster, user_id, id, business_group, " +
            "application_name, created_date, created_by, modified_by, modified_date " +
            "FROM PRINCIPAL WHERE name=:name AND environment_name=:environmentName AND " +
            "cluster=:cluster AND business_group=:businessGroup AND " +
            "application_name=:applicationName"
    )
    fun findApplicationDeploymentByProperties(
        name: String,
        environmentName: String,
        applicationName: String,
        cluster: String,
        businessGroup: String
    ): PrincipalEntity

    @Query(
        "SELECT type, name,  user_id, id, created_date, created_by, modified_by, modified_date " +
            "FROM PRINCIPAL WHERE name=:name AND user_id=:userId"
    )
    fun findUserByProperties(
        name: String,
        userId: String
    ): PrincipalEntity
}
