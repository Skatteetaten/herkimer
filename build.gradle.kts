plugins {
    id("java")
    id("idea")
    id("no.skatteetaten.gradle.aurora") version "4.5.4"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql:42.4.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.12.5")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("no.skatteetaten.aurora:mockmvc-extensions-kotlin:1.1.8")
    testImplementation("io.zonky.test:embedded-database-spring-test:2.1.1")
    testImplementation("io.zonky.test:embedded-postgres:2.0.0")
}

aurora {
    useKotlinDefaults
    useSpringBootDefaults

    versions {
        javaSourceCompatibility = "17"
    }
}
