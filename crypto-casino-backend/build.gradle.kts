plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.web3j") version "4.12.3"
    id("io.spring.javaformat") version "0.0.47"
}

group = "hu.bme.aut"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val springVersion = "3.5.6"
    val springSecurityVersion = "6.5.5"
    val projectReactorVersion = "3.7.11"
    val jUnitVersion = "1.14.0"
    val lombokVersion = "1.18.42"
    val postgresVersion = "42.7.8"
    val web3jCoreVersion = "4.12.3"
    val mapStructVersion = "1.6.3"
    val jwtVersion = "0.12.6"
    val springDotEnv = "4.0.0"

    developmentOnly("org.springframework.boot:spring-boot-devtools:$springVersion")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springVersion")
    implementation("org.web3j:core:$web3jCoreVersion")
    implementation("org.mapstruct:mapstruct:$mapStructVersion")
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    implementation("me.paulschwarz:spring-dotenv:$springDotEnv")

    compileOnly("org.projectlombok:lombok:$lombokVersion")

    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapStructVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
    testImplementation("org.springframework.security:spring-security-test:$springSecurityVersion")
    testImplementation("io.projectreactor:reactor-test:$projectReactorVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$jUnitVersion")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresVersion")
}

tasks.named("processResources") {
    dependsOn(":generateContractWrappers")
}

tasks.named("checkFormatMain") {
    dependsOn(":generateContractWrappers")
}

tasks.withType<io.spring.javaformat.gradle.tasks.CheckFormat> {
    exclude("org/web3j/*")
}
