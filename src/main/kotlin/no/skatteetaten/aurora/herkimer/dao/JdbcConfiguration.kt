package no.skatteetaten.aurora.herkimer.dao

import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent
import java.util.UUID

@Configuration
@EnableJdbcRepositories
@EnableJdbcAuditing
class JdbcConfiguration : AbstractJdbcConfiguration() {
    @Bean
    fun idSettingPrincipal(): ApplicationListener<BeforeSaveEvent<Any>> =
        ApplicationListener {
            val entity = it.entity
            if (entity is PrincipalEntity) {
                if (entity.id == null) {
                    entity.id = UUID.randomUUID()
                }
            }
        }
}
