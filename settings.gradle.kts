rootProject.name = "sphereon-kmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("convention-plugins")

    repositories {
        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://nexus.sphereon.com/repository/sphereon-opensource-snapshots")
        }
        maven {
            url = uri("https://nexus.sphereon.com/repository/sphereon-opensource-releases")
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://nexus.sphereon.com/repository/sphereon-opensource-snapshots")
        }
        maven {
            url = uri("https://nexus.sphereon.com/repository/sphereon-opensource-releases")
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}
//enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
/*
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
*/

include(
    "sphereon-kmp-common",
    "sphereon-kmp-crypto",
    "sphereon-kmp-cbor",
    "sphereon-kmp-mdl-mdoc",
)
