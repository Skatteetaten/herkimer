package no.skatteetaten.aurora.herkimer.service

import java.time.LocalDateTime
import java.util.UUID

interface PrincipalBase {
    val id: UUID
    val name: String
    val createdDate: LocalDateTime
    val createdBy: String
    val modifiedDate: LocalDateTime
    val modifiedBy: String
}

data class ApplicationDeployment(
    override val id: UUID,
    override val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String,
    val applicationName: String,
    override val createdDate: LocalDateTime,
    override val createdBy: String,
    override val modifiedDate: LocalDateTime,
    override val modifiedBy: String
) : PrincipalBase

data class User(
    override val id: UUID,
    override val name: String,
    val userId: String,
    override val createdDate: LocalDateTime,
    override val createdBy: String,
    override val modifiedDate: LocalDateTime,
    override val modifiedBy: String
) : PrincipalBase
