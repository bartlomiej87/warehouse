import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.openapi.generator") version "5.3.0"
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
}

group = "com.challenge"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val spec = "$rootDir/docs/openapi/api.yml"
val generatedSourcesDir = "$buildDir/generated"

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(spec)
    apiPackage.set("com.challenge.warehouse.api")
    modelPackage.set("com.challenge.warehouse.api.model")
    outputDir.set(generatedSourcesDir)
    additionalProperties.set(
        mapOf("interfaceOnly" to "true")
    )
    globalProperties.set(
        mapOf(
            "apis" to "",
            "models" to "",
        )
    )
}

sourceSets {
    main {
        java {
            srcDir(generatedSourcesDir)
        }
    }
}

extra["testcontainersVersion"] = "1.16.2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.13.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.0")
    implementation("javax.validation:validation-api:2.0.1.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.testcontainers:junit-jupiter")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    dependsOn("openApiGenerate")
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
