
plugins {
    kotlin("jvm")
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.kotlin.serialization)
}

sourceSets.all {
    java.srcDir("src/$name/kotlin")
}

dependencies {
    implementation(projects.ktorClientBrotli)
    implementation(projects.piped)
    implementation(projects.invidious)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.serialization.json)

    // JSON parser - Gson (test only)
    testImplementation(libs.gson)

    // Test engine(s)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
}
