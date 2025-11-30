// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false
}

// Load API_KEY from local.properties or environment for BuildConfig
val localProperties = java.util.Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val API_KEY: String = (
        localProperties.getProperty("API_KEY")
            ?: System.getenv("API_KEY")
            ?: (providers.gradleProperty("API_KEY").orNull)
            ?: ""
        )
extra["API_KEY"] = API_KEY