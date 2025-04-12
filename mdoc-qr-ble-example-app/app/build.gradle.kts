plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    androidTarget()
    /*iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "com.sphereon.mdoc.example.app.ios")
            binaryOption("bundleShortVersionString", "0.1.0")
            binaryOption("bundleVersion", "1")
            export(libs.kotlinx.coroutines.core)
        }
    }
    js {
        moduleName = "sample"
        browser {
            commonWebpackConfig {
                outputFileName = "sample.js"
            }
        }
        binaries.executable()
    }*/
    /*listOf(
        macosX64(),
        macosArm64(),
    ).forEach { target ->
        target.binaries.executable {
            baseName = "Mdoc QR BLE Example App"
            entryPoint = "com.sphereon.mdoc.example.app.main"
        }
    }
*/
    applyDefaultHierarchyTemplate()

    sourceSets {
        val composeMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(composeMain)
//        iosMain.get().dependsOn(composeMain)
//        jsMain.get().dependsOn(composeMain)

        val notJsMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(notJsMain)
//        appleMain.get().dependsOn(notJsMain)

        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            implementation(projects.sphereonKmpMdocCore)
            implementation(projects.sphereonKmpCrypto)
            implementation(projects.sphereonKmpCryptoKms)
            implementation(projects.sphereonKmpMdocDatatransferBle)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.bundles.krayon)
            implementation(libs.bundles.voyager)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kable.core)
            implementation(libs.khronicle)
            implementation(libs.serialization)
            implementation(projects.mdocQrBleExampleApp.bluetooth)
            implementation(projects.mdocQrBleExampleApp.permissions)
            api(libs.whyoleg.cryptography.core)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
        }

        composeMain.dependencies {
            implementation(libs.krayon.compose)
        }

        notJsMain.dependencies {
            implementation(libs.androidx.lifecycle)
        }
    }
}

android {
    namespace = "com.sphereon.mdoc.example.app"
    compileSdk = libs.versions.android.compile.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.sphereon.mdoc.example.app.android"
        minSdk = libs.versions.android.min.get().toInt()
        targetSdk = libs.versions.android.target.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures.compose = true

    // Provides `java.time` for kotlinx.datetime on API < 26.
    // https://github.com/Kotlin/kotlinx-datetime?tab=readme-ov-file#using-in-your-projects
    compileOptions.isCoreLibraryDesugaringEnabled = true
}

dependencies {
    // Provides `java.time` for kotlinx.datetime on Android API < 26.
    // https://github.com/Kotlin/kotlinx-datetime?tab=readme-ov-file#using-in-your-projects
    coreLibraryDesugaring(libs.desugar)
}
