package no.skatteetaten.aurora.herkimer.service

import no.skatteetaten.aurora.herkimer.controller.DataAccessException
import no.skatteetaten.aurora.herkimer.dao.Principal
import no.skatteetaten.aurora.herkimer.dao.PrincipalRepository
import no.skatteetaten.aurora.herkimer.dao.PrincipalType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaGetter

@Service
class PrincipalService(
    private val principalRepository: PrincipalRepository
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
            cluster = cluster
        )

        return principalRepository.save(principalAd).toApplicationDeployment()
    }

    fun updateApplicationDeployment(ad: ApplicationDeployment): ApplicationDeployment {
        val adPrincipal = Principal(
            id = ad.id,
            type = PrincipalType.ApplicationDeployment,
            name = ad.name,
            environmentName = ad.environmentName,
            cluster = ad.cluster,
            businessGroup = ad.businessGroup,
            applicationName = ad.applicationName,
            createdDate = ad.createdDate,
            createdBy = ad.createdBy,
            modifiedDate = ad.modifiedDate,
            modifiedBy = ad.modifiedBy
        )

        return principalRepository.save(adPrincipal).toApplicationDeployment()
    }

    fun findApplicationDeployment(id: UUID): ApplicationDeployment? =
        principalRepository.findByIdOrNull(id)?.toApplicationDeployment()

    fun findAllUsers(): List<User> =
        principalRepository.findAllPrincipalByType(PrincipalType.User.toString()).map { it.toUser() }

    fun findAllApplicationDeployment(): List<ApplicationDeployment> =
        principalRepository.findAllPrincipalByType(PrincipalType.ApplicationDeployment.toString())
            .map { it.toApplicationDeployment() }

    fun deleteApplicationDeployment(id: UUID): Unit =
        principalRepository.deleteById(id)

    fun createUser(id: String, name: String): User {
        val principaluser = Principal(
            type = PrincipalType.User,
            name = name,
            userId = id
        )

        return principalRepository.save(principaluser).toUser()
    }

    fun findUser(id: UUID): User? = principalRepository.findByIdOrNull(id)?.toUser()

    fun updateUser(user: User): User = principalRepository.save(
        Principal(
            id = user.id,
            type = PrincipalType.User,
            name = user.name,
            userId = user.userId,
            modifiedBy = user.modifiedBy,
            modifiedDate = user.modifiedDate,
            createdBy = user.createdBy,
            createdDate = user.createdDate
        )
    ).toUser()

    fun deleteUser(id: UUID) = principalRepository.deleteById(id)
}

private fun Principal.toApplicationDeployment(): ApplicationDeployment =
    takeIf { it.type == PrincipalType.ApplicationDeployment }?.run {
        ApplicationDeployment(
            id = assertNotNull(::id),
            name = name,
            environmentName = assertNotNull(::environmentName),
            cluster = assertNotNull(::cluster),
            businessGroup = assertNotNull(::businessGroup),
            applicationName = assertNotNull(::applicationName),
            createdDate = assertNotNull(::createdDate),
            createdBy = createdBy,
            modifiedBy = modifiedBy,
            modifiedDate = assertNotNull(::modifiedDate)
        )
    } ?: throw DataAccessException("Principal with id=${this.id} is not ApplicationDeployment")

private fun Principal.toUser(): User =
    takeUnless { it.type != PrincipalType.User }?.run {
        User(
            id = assertNotNull(::id),
            userId = assertNotNull(::userId),
            name = name,
                createdDate = assertNotNull(::createdDate),
            createdBy = createdBy,
            modifiedBy = modifiedBy,
            modifiedDate = assertNotNull(::modifiedDate)
        )
    } ?: throw DataAccessException("Principal with id=${this.id} is not User")

fun <T> assertNotNull(p: KProperty0<T?>): T =
    p.get()
        ?: throw DataAccessException("Data integrity error; property ${p.javaGetter?.declaringClass?.simpleName ?: "unknown"}::${p.name} cannot be null")
