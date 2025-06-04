plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    id("io.micronaut.application") version "4.5.3"
    id("com.gradleup.shadow") version "8.3.6"
    id("io.micronaut.aot") version "4.5.3"
}

version = "0.2"  // Updated to Phase 2
group = "org.anaphase.gateway"

val kotlinVersion = project.properties.get("kotlinVersion")

repositories {
    mavenCentral()
}

dependencies {
    // ===================================================================
    // KSP Processors
    // ===================================================================
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.openapi:micronaut-openapi")
    ksp("io.micronaut.validation:micronaut-validation-processor")

    // ===================================================================
    // Core Micronaut Dependencies
    // ===================================================================
    implementation("io.micronaut:micronaut-aop")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut:micronaut-retry")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")

    // ===================================================================
    // PHASE 2: Reactive & Rate Limiting Dependencies
    // ===================================================================
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("org.reactivestreams:reactive-streams")
    implementation("io.projectreactor:reactor-core")

    // Future Redis support for distributed rate limiting
    implementation("io.micronaut.redis:micronaut-redis-lettuce")

    // ===================================================================
    // Caching & Performance
    // ===================================================================
    implementation("io.micronaut.cache:micronaut-cache-caffeine")

    // ===================================================================
    // Monitoring & Metrics
    // ===================================================================
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut.micrometer:micronaut-micrometer-observation-http")

    // ===================================================================
    // Security (JWT ready for Phase 2.2)
    // ===================================================================
    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")

    // ===================================================================
    // Validation
    // ===================================================================
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("jakarta.validation:jakarta.validation-api")

    // ===================================================================
    // Kotlin Support
    // ===================================================================
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin.kotlin-stdlib-jdk8:${kotlinVersion}")

    // ===================================================================
    // OpenAPI Documentation
    // ===================================================================
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")

    // ===================================================================
    // Runtime Dependencies
    // ===================================================================
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.yaml:snakeyaml")

    // ===================================================================
    // Testing Dependencies
    // ===================================================================
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.micronaut.test:micronaut-test-rest-assured")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")

    // ===================================================================
    // AOT Plugins
    // ===================================================================
    aotPlugins(platform("io.micronaut.platform:micronaut-platform:4.8.2"))
    aotPlugins("io.micronaut.security:micronaut-security-aot")
}

application {
    mainClass = "org.anaphase.gateway.ApplicationKt"
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("org.anaphase.gateway.*")
    }
    aot {
        // Optimized AOT configuration for Phase 2
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
        configurationProperties.put("micronaut.security.jwks.enabled", "false")
    }
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}

// Kotlin compiler options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}