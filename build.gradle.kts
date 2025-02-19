plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.powerAssert).apply(false)
    alias(libs.plugins.kover).apply(false)
    alias(libs.plugins.ktlint).apply(false)
    alias(libs.plugins.mavenPublish).apply(false)
}
