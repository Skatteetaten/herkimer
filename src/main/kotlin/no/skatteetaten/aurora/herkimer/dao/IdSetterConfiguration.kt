package no.skatteetaten.aurora.herkimer.dao

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent
import java.util.UUID

@Configuration
class IdSetterConfiguration(
    private val principalRepository: PrincipalRepository
) {
    @Bean
    fun idSettingPrincipal(): ApplicationListener<BeforeSaveEvent<Any>> =
        ApplicationListener {
            val entity = it.entity
            if (entity is PrincipalEntity) {
                if (entity.id == null) {
                    entity.id = getUniquePrincipalId()
                }
            }
        }

    private fun getUniquePrincipalId(): PrincipalUID =
        (0..10).fold(
            PrincipalUID.randomId()
        ) { acc, i ->
            if (!principalRepository.existsById(acc)) return acc
            PrincipalUID.randomId()
        }
}

data class PrincipalUID private constructor(private val value: String) {
    companion object {
        private const val ID_LENGTH = 10
        fun randomId() = PrincipalUID(
            UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, ID_LENGTH)
        )

        @JsonCreator
        fun fromString(shortIdString: String): PrincipalUID {
            require(shortIdString.length == ID_LENGTH) {
                "Failed to convert string to PrincipalUID. It has to have length of $ID_LENGTH"
            }
            return PrincipalUID(shortIdString)
        }
    }

    @JsonValue
    override fun toString(): String = value
}
