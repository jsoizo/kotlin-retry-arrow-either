import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.powerAssert)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.mavenPublish)
}

group = "com.jsoizo"
version = "0.1.0"
val projectName = "kotlin-retry-arrow-either"

kotlin {
    explicitApi()

    jvmToolchain(11)

    jvm()

    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    /* https://kotlinlang.org/docs/native-target-support.html#tier-1 */

    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    /* https://kotlinlang.org/docs/native-target-support.html#tier-2 */

    linuxX64()
    linuxArm64()

    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()

    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    iosArm64()

    /* https://kotlinlang.org/docs/native-target-support.html#tier-3 */
    mingwX64()

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.contracts.ExperimentalContracts")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(libs.kotlin.retry)
                implementation(libs.arrow.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
            }
        }
    }
}

android {
    namespace = "com.jsoizo.retryeither"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
powerAssert {
    functions = listOf("kotlin.assert", "kotlin.test.assertTrue", "kotlin.test.assertEquals", "kotlin.test.assertNull")
    includedSourceSets = listOf("commonMain")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), projectName, version.toString())

    val repo = "github.com/jsoizo/$projectName"
    val repoHttpUrl = "https://$repo"
    val repoGitUrl = "git://$repo"

    pom {
        name = projectName
        description = "An extension library for kotlin-retry that provides retry functionality for Arrow's Either type."
        inceptionYear = "2025"
        url = repoHttpUrl
        licenses {
            license {
                name.set("MIT")
                url.set("$repoHttpUrl/blob/master/LICENSE")
            }
        }
        scm {
            url.set(repoHttpUrl)
            connection.set("scm:git:$repoGitUrl.git")
            developerConnection.set(repoHttpUrl)
        }
        developers {
            developer {
                name.set("jsoizo")
            }
        }
    }
}