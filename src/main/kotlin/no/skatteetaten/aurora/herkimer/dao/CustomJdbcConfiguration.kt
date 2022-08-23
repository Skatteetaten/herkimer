package no.skatteetaten.aurora.herkimer.dao

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Configuration
@EnableJdbcRepositories
@EnableJdbcAuditing
class CustomJdbcConfiguration(
    private val objectMapper: ObjectMapper
) : AbstractJdbcConfiguration() {

    override fun userConverters(): MutableList<*> {
        return mutableListOf(
            StringToObjectNode(objectMapper),
            ObjectNodeToString(objectMapper),
            PrincipalUIDToString(),
            StringToPrincipalUID()
        )
    }
}
