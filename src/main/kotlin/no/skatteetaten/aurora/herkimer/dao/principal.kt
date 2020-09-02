package no.skatteetaten.aurora.herkimer.dao

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import java.util.UUID

data class Principal(
    @Id
    var id: UUID? = null,
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

interface PrincipalRepository : CrudRepository<Principal, UUID> {
    @Query(
        "SELECT type, name, environment_name, cluster, user_id, id, business_group, " +
            "application_name, created_date, created_by, modified_by, modified_date " +
            "FROM PRINCIPAL WHERE type LIKE :principalType"
    )
    fun findAllPrincipalByType(principalType: String): List<Principal>
}
