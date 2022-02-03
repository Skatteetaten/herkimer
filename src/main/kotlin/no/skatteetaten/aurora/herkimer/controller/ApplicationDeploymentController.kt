package no.skatteetaten.aurora.herkimer.controller

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import javax.validation.Valid
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import no.skatteetaten.aurora.herkimer.dao.PrincipalUID
import no.skatteetaten.aurora.herkimer.service.PrincipalService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.fasterxml.jackson.annotation.JsonInclude

data class ApplicationDeploymentPayload(
    val name: String,
    val environmentName: String,
    val cluster: String,
    val businessGroup: String
)

@RequireAtLeastOnePropValue
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicationMigrationPayload(
    val environmentName: String? = null,
    val cluster: String? = null,
    val businessGroup: String? = null
)

@RestController
@RequestMapping("/applicationDeployment")
class ApplicationDeploymentController(
    private val principalService: PrincipalService
) {
    @GetMapping("/{id}")
    fun findById(@PathVariable id: PrincipalUID): AuroraResponse<ApplicationDeployment> =
        principalService.findApplicationDeployment(id)?.toResource()?.okResponse()
            ?: throw NoSuchResourceException("Could not find ApplicationDeployment with id=$id")

    @GetMapping
    fun findAll() =
        principalService.findAllApplicationDeployment()
            .toResources()
            .okResponse()

    @PostMapping
    fun create(@RequestBody payload: ApplicationDeploymentPayload) =
        payload.run {
            principalService.createApplicationDeployment(
                name,
                environmentName,
                cluster,
                businessGroup
            ).toResource()
                .okResponse()
        }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: PrincipalUID,
        @RequestBody payload: ApplicationDeploymentPayload
    ): AuroraResponse<ApplicationDeployment> {
        val existingAd = principalService.findApplicationDeployment(id)
            ?: throw NoSuchResourceException("Could not find ApplicationDeployment with id=$id")
        return payload.run {
            principalService.updateApplicationDeployment(
                existingAd.copy(
                    name = name,
                    businessGroup = businessGroup,
                    cluster = cluster,
                    environmentName = environmentName
                )
            ).toResource()
                .okResponse()
        }
    }

    @PatchMapping("/{id}")
    fun migrate(
        @PathVariable id: PrincipalUID,
        @Valid @RequestBody payload: ApplicationMigrationPayload
    ): AuroraResponse<ApplicationDeployment> {
        val existingAd = principalService.findApplicationDeployment(id)
            ?: throw NoSuchResourceException("Could not find ApplicationDeployment with id=$id")
        return payload.run {
            principalService.updateApplicationDeployment(
                existingAd.copy(
                    businessGroup = businessGroup ?: existingAd.businessGroup,
                    cluster = cluster ?: existingAd.cluster,
                    environmentName = environmentName ?: existingAd.environmentName
                )
            ).toResource()
                .okResponse()
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: PrincipalUID) = principalService.deleteApplicationDeployment(id)
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RequireAtLeastOnePropValueValidator::class])
annotation class RequireAtLeastOnePropValue(
    val message: String = "at least one property must have a valid value",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

// validate that object contains at least one declared property that is non-null and not empty string
class RequireAtLeastOnePropValueValidator : ConstraintValidator<RequireAtLeastOnePropValue, Any> {
    override fun isValid(p0: Any?, p1: ConstraintValidatorContext?): Boolean {
        if (p0 == null) {
            return false
        }
        var valid = false

        p0.javaClass.kotlin.declaredMemberProperties.forEach {
            val v = it.get(p0)
            if (v != null) {
                valid = if (v is String) {
                    v != "" // if string ensure it is not blank
                } else {
                    true // if not string assume non-null value is valid
                }
            }
        }

        return valid
    }
}
