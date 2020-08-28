package no.skatteetaten.aurora.herkimer.service

import java.util.UUID

interface PrincipalBase {
    val id: UUID
    val name: String
}

data class ApplicationDeployment(
    override val id: UUID,
    override val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String,
    val applicationName: String
) : PrincipalBase

data class User(
    override val id: UUID,
    override val name: String,
    val userId: String
) : PrincipalBase
