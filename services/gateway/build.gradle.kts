plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    id("io.micronaut.application") version "4.5.3"
    id("com.gradleup.shadow") version "8.3.6"
    id("io.micronaut.aot") version "4.5.3"
}

version = "0.1"
group = "org.anaphase.gateway"

val kotlinVersion=project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.openapi:micronaut-openapi")
    ksp("io.micronaut.security:micronaut-security-annotations")
    ksp("io.micronaut.validation:micronaut-validation-processor")
    implementation("io.micronaut:micronaut-aop")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut:micronaut-retry")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("io.micronaut.discovery:micronaut-discovery-client")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-observation-http")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut.redis:micronaut-redis-lettuce")
    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.security:micronaut-security-oauth2")
    implementation("io.micronaut.session:micronaut-session")
    implementation("io.micronaut.tracing:micronaut-tracing-jaeger")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut.test:micronaut-test-rest-assured")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
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
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
        configurationProperties.put("micronaut.security.jwks.enabled","false")
        configurationProperties.put("micronaut.security.openid-configuration.enabled","false")
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}


