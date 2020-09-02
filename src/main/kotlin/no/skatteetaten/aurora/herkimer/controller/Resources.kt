package no.skatteetaten.aurora.herkimer.controller

import no.skatteetaten.aurora.herkimer.service.ApplicationDeployment
import no.skatteetaten.aurora.herkimer.service.User
import java.time.LocalDateTime

data class AuroraResponse<T : Resource>(
    val success: Boolean = true,
    val message: String = "OK",
    val items: List<T> = emptyList(),
    val errors: List<ErrorResponse> = emptyList(),
    val count: Int = items.size + errors.size
)

abstract class Resource
data class ErrorResponse(val errorMessage: String)

data class ApplicationDeploymentResource(
    val id: String,
    val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String,
    val applicationName: String,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
    val createdBy: String,
    val modifiedBy: String
) : Resource()

fun ApplicationDeployment.toResource() =
    ApplicationDeploymentResource(id = id.toString(),
        name = name,
        environmentName = environmentName,
        cluster = cluster,
        businessGroup = businessGroup,
        applicationName = applicationName,
        modifiedDate = modifiedDate,
        modifiedBy = modifiedBy,
        createdDate = createdDate,
        createdBy = createdBy
    )

@JvmName("applicationDeploymentsToResources")
fun List<ApplicationDeployment>.toResources() = this.map { it.toResource() }

@JvmName("usersToResources")
fun List<User>.toResources() = this.map { it.toResource() }

data class UserResource(
    val id: String,
    val userId: String,
    val name: String,
    val createdBy: String,
    val createdDate: LocalDateTime,
    val modifiedBy: String,
    val modifiedDate: LocalDateTime
) : Resource()

fun User.toResource() =
    UserResource(
        id = id.toString(),
        userId = userId,
        name = name,
        createdBy = createdBy,
        createdDate = createdDate,
        modifiedBy = modifiedBy,
        modifiedDate = modifiedDate
    )

inline fun <reified T : Resource> T.okResponse() = AuroraResponse(items = listOf(this))
inline fun <reified T : Resource> List<T>.okResponse() = AuroraResponse(items = this)
