rootProject.name = "sphereon-kmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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

    versionCatalogs {
        create("awssdk") {
            from("aws.sdk.kotlin:version-catalog:1.4.31")
        }
    }
}

include(
    "sphereon-kmp-common",
    "sphereon-kmp-cbor",
    "sphereon-kmp-crypto",
    "sphereon-kmp-crypto-kms",
    "sphereon-kmp-crypto-kms-azure",
    "sphereon-kmp-crypto-kms-aws",
    "sphereon-kmp-crypto-kms-ecdsa",
    "sphereon-kmp-ades-client",
    "sphereon-kmp-mdoc-core",
    "sphereon-kmp-mdoc-datatransfer-ble"
)

