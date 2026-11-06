import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val jamendoClientId = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .map { content ->
        Properties().apply { load(content.reader()) }
            .getProperty("jamendo.client.id")
            .orEmpty()
    }
    .orElse("")

android {
    namespace = "br.com.musicflow.network"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        buildConfigField("String", "JAMENDO_CLIENT_ID", jamendoClientId.get().toJavaStringLiteral())
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}

private fun String.toJavaStringLiteral(): String = buildString {
    append('"')
    for (character in this@toJavaStringLiteral) {
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n', '\r' -> Unit
            else -> append(character)
        }
    }
    append('"')
}
