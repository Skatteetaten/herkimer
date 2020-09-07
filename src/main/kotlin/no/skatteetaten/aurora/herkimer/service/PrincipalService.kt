package no.skatteetaten.aurora.herkimer.service

import no.skatteetaten.aurora.herkimer.controller.DataAccessException
import no.skatteetaten.aurora.herkimer.dao.PrincipalEntity
import no.skatteetaten.aurora.herkimer.dao.PrincipalRepository
import no.skatteetaten.aurora.herkimer.dao.PrincipalType
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
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
    ): ApplicationDeploymentDto {
        val principalAd = PrincipalEntity(
            type = PrincipalType.ApplicationDeployment,
            name = name,
            environmentName = environmentName,
            businessGroup = businessGroup,
            applicationName = applicationName,
            cluster = cluster
        )

        return try {
            principalRepository.save(principalAd).toApplicationDeployment()
        } catch (ex: DbActionExecutionException) {
            if (ex.cause is DuplicateKeyException) {
                principalRepository.findApplicationDeploymentByProperties(
                    name = name,
                    environmentName = environmentName,
                    cluster = cluster,
                    businessGroup = businessGroup,
                    applicationName = applicationName
                ).toApplicationDeployment()
            } else throw ex
        }
    }

    fun updateApplicationDeployment(ad: ApplicationDeploymentDto): ApplicationDeploymentDto {
        val adPrincipal = PrincipalEntity(
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

    fun findById(id: PrincipalUID): PrincipalBase? = principalRepository.findByIdOrNull(id)?.toPrincipalBase()

    fun findApplicationDeployment(id: PrincipalUID): ApplicationDeploymentDto? =
        principalRepository.findByIdOrNull(id)?.toApplicationDeployment()

    fun findAllUsers(): List<UserDto> =
        principalRepository.findAllPrincipalByType(PrincipalType.User.toString()).map { it.toUser() }

    fun findAllApplicationDeployment(): List<ApplicationDeploymentDto> =
        principalRepository.findAllPrincipalByType(PrincipalType.ApplicationDeployment.toString())
            .map { it.toApplicationDeployment() }

    fun deleteApplicationDeployment(id: PrincipalUID): Unit =
        principalRepository.deleteById(id)

    fun createUser(userId: String, name: String): UserDto {
        val principaluser = PrincipalEntity(
            type = PrincipalType.User,
            name = name,
            userId = userId
        )

        return try {
            principalRepository.save(principaluser)
        } catch (ex: DbActionExecutionException) {
            if (ex.cause is DuplicateKeyException) {
                principalRepository.findUserByProperties(
                    name = name,
                    userId = userId
                )
            } else throw ex
        }.toUser()
    }

    fun findUser(id: PrincipalUID): UserDto? = principalRepository.findByIdOrNull(id)?.toUser()

    fun updateUser(userDto: UserDto): UserDto = principalRepository.save(
        PrincipalEntity(
            id = userDto.id,
            type = PrincipalType.User,
            name = userDto.name,
            userId = userDto.userId,
            modifiedBy = userDto.modifiedBy,
            modifiedDate = userDto.modifiedDate,
            createdBy = userDto.createdBy,
            createdDate = userDto.createdDate
        )
    ).toUser()

    fun deleteUser(id: PrincipalUID) = principalRepository.deleteById(id)
}

private fun PrincipalEntity.toPrincipalBase() =
    when (type) {
        PrincipalType.ApplicationDeployment -> toApplicationDeployment()
        PrincipalType.User -> toUser()
    }

private fun PrincipalEntity.toApplicationDeployment(): ApplicationDeploymentDto =
    takeUnless { it.type != PrincipalType.ApplicationDeployment }?.run {
        ApplicationDeploymentDto(
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
    } ?: throw DataAccessException("Principal with id=$id is not ApplicationDeployment")

private fun PrincipalEntity.toUser(): UserDto =
    takeUnless { it.type != PrincipalType.User }?.run {
        UserDto(
            id = assertNotNull(::id),
            userId = assertNotNull(::userId),
            name = name,
            createdDate = assertNotNull(::createdDate),
            createdBy = createdBy,
            modifiedBy = modifiedBy,
            modifiedDate = assertNotNull(::modifiedDate)
        )
    } ?: throw DataAccessException("Principal with id=$id is not User")

fun <T> assertNotNull(p: KProperty0<T?>): T =
    p.get()
        ?: throw DataAccessException("Data integrity error; property ${p.javaGetter?.declaringClass?.simpleName ?: "unknown"}::${p.name} cannot be null")
