plugins {
    id("java")
    id("idea")
    id("no.skatteetaten.gradle.aurora") version "4.4.9"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql")
    // Nexus IQ violation fixes:
    implementation("ch.qos.logback:logback-core:1.2.7")
    implementation("org.apache.kafka:kafka-clients:2.7.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("no.skatteetaten.aurora:mockmvc-extensions-kotlin:1.1.7")
    testImplementation("io.zonky.test:embedded-database-spring-test:2.1.1")
    testImplementation("io.zonky.test:embedded-postgres:1.3.1")
}

aurora {
    useKotlinDefaults
    useSpringBootDefaults
}
