package no.skatteetaten.aurora.herkimer.controller

import no.skatteetaten.aurora.herkimer.service.ApplicationDeployment
import no.skatteetaten.aurora.herkimer.service.User

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
    val applicationName: String
) : Resource()

fun ApplicationDeployment.toResource() =
    ApplicationDeploymentResource(
        id = this.id.toString(),
        name = this.name,
        environmentName = this.environmentName,
        cluster = this.cluster,
        businessGroup = this.businessGroup,
        applicationName = this.applicationName
    )

@JvmName("applicationDeploymentsToResources")
fun List<ApplicationDeployment>.toResources() = this.map { it.toResource() }

@JvmName("usersToResources")
fun List<User>.toResources() = this.map { it.toResource() }

data class UserResource(
    val id: String,
    val userId: String,
    val name: String
) : Resource()

fun User.toResource() =
    UserResource(
        id = this.id.toString(),
        userId = this.userId,
        name = this.name
    )

inline fun <reified T : Resource> T.okResponse() = AuroraResponse(items = listOf(this))
inline fun <reified T : Resource> List<T>.okResponse() = AuroraResponse(items = this)
