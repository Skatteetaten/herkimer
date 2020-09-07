package no.skatteetaten.aurora.herkimer.controller

import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import no.skatteetaten.aurora.herkimer.service.ApplicationDeploymentDto
import no.skatteetaten.aurora.herkimer.service.ResourceDto
import no.skatteetaten.aurora.herkimer.service.UserDto
import java.time.LocalDateTime
import java.util.UUID

data class AuroraResponse<T : ResourceBase>(
    val success: Boolean = true,
    val message: String = "OK",
    val items: List<T> = emptyList(),
    val errors: List<ErrorResponse> = emptyList(),
    val count: Int = items.size + errors.size
)

interface ResourceBase {
    val id: String
    val createdDate: LocalDateTime
    val modifiedDate: LocalDateTime
    val createdBy: String
    val modifiedBy: String
}

data class ErrorResponse(val errorMessage: String)

data class ApplicationDeployment(
    override val id: String,
    val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String,
    val applicationName: String,
    override val createdDate: LocalDateTime,
    override val modifiedDate: LocalDateTime,
    override val createdBy: String,
    override val modifiedBy: String
) : ResourceBase

fun ApplicationDeploymentDto.toResource() =
    ApplicationDeployment(
        id = id.toString(),
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

data class User(
    override val id: String,
    val userId: String,
    val name: String,
    override val createdBy: String,
    override val createdDate: LocalDateTime,
    override val modifiedBy: String,
    override val modifiedDate: LocalDateTime
) : ResourceBase

fun UserDto.toResource() =
    User(
        id = id.toString(),
        userId = userId,
        name = name,
        createdBy = createdBy,
        createdDate = createdDate,
        modifiedBy = modifiedBy,
        modifiedDate = modifiedDate
    )

data class Resource(
    override val id: String,
    val name: String,
    val kind: ResourceKind,
    val ownerId: UUID,
    override val createdDate: LocalDateTime,
    override val modifiedDate: LocalDateTime,
    override val createdBy: String,
    override val modifiedBy: String
) : ResourceBase

fun ResourceDto.toResource() =
    Resource(
        id = id.toString(),
        name = name,
        kind = kind,
        ownerId = ownerId,
        createdDate = createdDate,
        modifiedDate = modifiedDate,
        createdBy = createdBy,
        modifiedBy = modifiedBy
    )

@JvmName("applicationDeploymentsToResources")
fun List<ApplicationDeploymentDto>.toResources() = this.map { it.toResource() }

@JvmName("usersToResources")
fun List<UserDto>.toResources() = this.map { it.toResource() }

@JvmName("ResourceResourceToResources")
fun List<ResourceDto>.toResources() = this.map { it.toResource() }

inline fun <reified T : ResourceBase> T.okResponse() = AuroraResponse(items = listOf(this))
inline fun <reified T : ResourceBase> List<T>.okResponse() = AuroraResponse(items = this)
