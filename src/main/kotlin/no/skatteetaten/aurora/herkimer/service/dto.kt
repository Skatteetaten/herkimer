package no.skatteetaten.aurora.herkimer.service

import com.fasterxml.jackson.databind.node.ObjectNode
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.dao.ResourceKind
import java.time.LocalDateTime

interface PrincipalBase {
    val id: PrincipalUID
    val name: String
    val createdDate: LocalDateTime
    val createdBy: String
    val modifiedDate: LocalDateTime
    val modifiedBy: String
}

data class ApplicationDeploymentDto(
    override val id: PrincipalUID,
    override val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String,
    override val createdDate: LocalDateTime,
    override val createdBy: String,
    override val modifiedDate: LocalDateTime,
    override val modifiedBy: String
) : PrincipalBase

data class UserDto(
    override val id: PrincipalUID,
    override val name: String,
    val userId: String,
    override val createdDate: LocalDateTime,
    override val createdBy: String,
    override val modifiedDate: LocalDateTime,
    override val modifiedBy: String
) : PrincipalBase

data class ResourceDto(
    val id: Int,
    val name: String,
    val kind: ResourceKind,
    val ownerId: PrincipalUID,
    val parentId: Int?,
    val claims: List<ResourceClaimDto>? = null,
    val createdDate: LocalDateTime,
    val createdBy: String,
    val modifiedDate: LocalDateTime,
    val modifiedBy: String,
    val active: Boolean,
    val setToCooldownAt: LocalDateTime?
)

data class ResourceClaimDto(
    val id: Int,
    val ownerId: PrincipalUID,
    val name: String,
    val resourceId: Int,
    val credentials: ObjectNode,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime,
    val createdBy: String,
    val modifiedBy: String
)
