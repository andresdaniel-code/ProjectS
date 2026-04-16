plugins {
    // Plugins principales de Android y Kotlin
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.compose.compiler) apply false

    // Plugin de Firebase Google Services
    id("com.google.gms.google-services") version "4.4.4" apply false
}