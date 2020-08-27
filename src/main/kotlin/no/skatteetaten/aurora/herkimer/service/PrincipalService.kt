package no.skatteetaten.aurora.herkimer.service

import no.skatteetaten.aurora.herkimer.controller.DataAccessException
import no.skatteetaten.aurora.herkimer.controller.NoSuchResourceException
import no.skatteetaten.aurora.herkimer.dao.Principal
import no.skatteetaten.aurora.herkimer.dao.PrincipalRepository
import no.skatteetaten.aurora.herkimer.dao.PrincipalType
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaGetter

@Service
class PrincipalService(
    private val principalRepository: PrincipalRepository,
    private val jdbcAggregateTemplate: JdbcAggregateTemplate
) {

    fun createApplicationDeployment(
        name: String,
        environmentName: String,
        cluster: String,
        businessGroup: String,
        applicationName: String
    ): ApplicationDeployment {
        val principalAd = Principal(
            type = PrincipalType.ApplicationDeployment,
            name = name,
            environmentName = environmentName,
            businessGroup = businessGroup,
            applicationName = applicationName,
            cluster = cluster,
            id = UUID.randomUUID()
        )

        return jdbcAggregateTemplate.insert(principalAd).toApplicationDeployment()
    }

    fun updateApplicationDeployment(ad: ApplicationDeployment): ApplicationDeployment {
        val adPrincipal = Principal(
            id = ad.id,
            type = PrincipalType.ApplicationDeployment,
            name = ad.name,
            environmentName = ad.environmentName,
            cluster = ad.cluster,
            businessGroup = ad.businessGroup,
            applicationName = ad.applicationName
        )

        return principalRepository.save(adPrincipal).toApplicationDeployment()
    }

    fun findApplicationDeployment(id: UUID): ApplicationDeployment =
        principalRepository.findByIdOrNull(id)?.toApplicationDeployment()
            ?: throw NoSuchResourceException(listOf(id.toString()))

    fun findAllApplicationDeployment(): List<ApplicationDeployment> =
        principalRepository.findAllPrincipalByType(PrincipalType.ApplicationDeployment.toString())
            .map { it.toApplicationDeployment() }

    fun deleteApplicationDeployment(id: UUID): Unit =
        principalRepository.deleteById(id)
}

private fun Principal.toApplicationDeployment() =
    if (this.type == PrincipalType.ApplicationDeployment) {
        ApplicationDeployment(
            id = this.id,
            name = this.name,
            environmentName = assertNotNull(this::environmentName),
            cluster = assertNotNull(this::cluster),
            businessGroup = assertNotNull(this::businessGroup),
            applicationName = assertNotNull(this::applicationName)

        )
    } else {
        throw DataAccessException("Principal with id=${this.id} is not ApplicationDeployment")
    }

fun <T> assertNotNull(p: KProperty0<T?>): T =
    p.get()
        ?: throw DataAccessException("Data integrity error; property ${p.javaGetter?.declaringClass?.simpleName ?: "unknown"}::${p.name} cannot be null")

private fun Principal.toUser() = User(this.id, assertNotNull(this::userId), this.name)
