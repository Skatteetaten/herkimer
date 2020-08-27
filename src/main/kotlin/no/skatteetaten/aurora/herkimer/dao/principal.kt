package no.skatteetaten.aurora.herkimer.dao

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.util.UUID

data class Principal(
    @Id
    val id: UUID,
    val type: PrincipalType,
    val name: String,
    val environmentName: String? = null,
    val cluster: String? = null,
    val businessGroup: String? = null,
    val applicationName: String? = null,
    val userId: String? = null
)

enum class PrincipalType {
    ApplicationDeployment, User
}

interface PrincipalRepository : CrudRepository<Principal, UUID> {
    @Query("SELECT type, name, environment_name, cluster, user_id, id, business_group, application_name FROM PRINCIPAL WHERE type LIKE :principalType")
    fun findAllPrincipalByType(principalType: String): List<Principal>
}
