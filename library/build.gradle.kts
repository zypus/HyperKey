import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.yours-valentiine"
version = "0.2.0-beta02"

kotlin {
    jvm()
    androidLibrary {
        namespace = "io.github.yoursvalentiine"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.test.ui)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(project.group.toString(), "hyperkey", project.version.toString())

    pom {
        name = "HyperKey"
        description = "A Kotlin DSL for declarative keyboard shortcut handling in Compose Multiplatform"
        url = "https://github.com/yours-valentiine/HyperKey"

        licenses {
            license {
                name = "BSD 3-Clause License"
                url = "https://opensource.org/license/bsd-3-clause"
            }
        }

        developers {
            developer {
                id = "yours.valentiine"
                name = "yours.valentiine"
            }
        }

        scm {
            url = "https://github.com/yours-valentiine/HyperKey"
            connection = "https://github.com/yours-valentiine/HyperKey.git"
        }
    }
}
