package no.skatteetaten.aurora.herkimer.service

import java.util.UUID

interface PrincipalBase {
    val name: String
}

data class ApplicationDeployment(
    val id: UUID,
    override val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String,
    val applicationName: String
) : PrincipalBase

data class User(
    val id: UUID? = null,
    val userId: String,
    override val name: String
) : PrincipalBase
