package no.skatteetaten.aurora.herkimer.dao

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Configuration
@EnableJdbcRepositories
@EnableJdbcAuditing
class JdbcConfiguration(
    private val objectMapper: ObjectMapper
) : AbstractJdbcConfiguration() {

    @Bean
    override fun jdbcCustomConversions() =
        JdbcCustomConversions(
            listOf(
                StringToJsonNode(objectMapper),
                JsonNodeToString(objectMapper),
                PrincipalUIDToString(),
                StringToPrincipalUID()
            )
        )
}
