package no.skatteetaten.aurora.herkimer

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {
    @Bean
    // TOOD: Only for develop purposes
    fun flywayMigrationStrategy(): FlywayMigrationStrategy = FlywayMigrationStrategy {
        it.clean()
        it.migrate()
    }

    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper().configureDefaults()
}

fun ObjectMapper.configureDefaults(): ObjectMapper = this.registerKotlinModule()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
    .enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
    .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    .registerModule(Jdk8Module())
    .registerModule(JavaTimeModule())
    .registerModule(ParameterNamesModule())
