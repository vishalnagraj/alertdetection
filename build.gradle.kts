plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
    dependencies {
        // Classpath for Google Services plugin
        classpath("com.google.gms:google-services:4.4.2")

        // Android Gradle plugin
        classpath("com.android.tools.build:gradle:8.1.0") // Updated to the latest stable version
    }
}