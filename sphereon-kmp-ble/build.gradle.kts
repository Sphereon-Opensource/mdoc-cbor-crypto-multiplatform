plugins {
    kotlin("multiplatform") version libs.versions.kotlin
    id("com.android.library")
//    kotlin("jvm") version libs.versions.kotlin
}



kotlin {

    androidTarget()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.io.core)
                implementation(projects.sphereonKmpCommon)
                implementation(projects.sphereonKmpCbor)
                implementation(projects.sphereonKmpCrypto)
                implementation(projects.sphereonKmpMdocCore)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.whyoleg.cryptography.core)
                implementation(libs.kermit)

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.android)
                implementation("androidx.core:core-ktx:1.6.0")
                implementation("androidx.appcompat:appcompat:1.3.1")
            }
        }
    }
    jvmToolchain(17)
}


android {
    namespace = "com.sphereon.ble"
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21 // Set according to your minimum supported version
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
