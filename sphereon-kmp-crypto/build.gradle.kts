import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
//    alias(libs.plugins.androidLibrary)
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("io.kotest.multiplatform")
    id("module.publication")
}


rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().download = false
    // "true" for default behavior
}
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.the<YarnRootExtension>().download = false
    // "true" for default behavior
}
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING // NONE | FAIL
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true // true
}


/*
ksp {
    arg("erasePackage", "true")
}*/
repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://raw.githubusercontent.com/a-sit-plus/gradle-conventions-plugin/mvn/repo")
        name = "aspConventions"
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        name = "bigNum"
    }
    maven(url = "https://raw.githubusercontent.com/Deezer/KustomExport/mvn-repo")
}
/*
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.js.ExperimentalJsExport"
}
*/


kotlin {
    kotlin.applyDefaultHierarchyTemplate()

    jvmToolchain(17)
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    /*  androidTarget {
          publishLibraryVariants("release")
          compilations.all {
              kotlinOptions {
                  jvmTarget = JavaVersion.VERSION_17.toString()
              }
          }
      }*/
    js(IR) {
        moduleName = "@sphereon/kmp-crypto"
        nodejs {
//            useEsModules() // Enables ES2015 modules

            testTask {
                useMocha()
            } // To run tests with Node.js.

        }
        browser {
//            useEsModules() // Enables ES2015 modules

            testTask {
                useMocha()
            }
        }

        binaries.library()
        generateTypeScriptDefinitions()
    }
    /*
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }*/


    sourceSets {
        all{
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        val commonMain by getting {
            dependencies {
                implementation(projects.sphereonKmpCommon)
                implementation(projects.sphereonKmpCbor)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.io.core)
                implementation(libs.whyoleg.cryptography.core)
                implementation(libs.whyoleg.cryptography.serialization.pem)
                implementation(libs.whyoleg.cryptography.serialization.asn1)
                implementation(libs.whyoleg.cryptography.serialization.asn1.modules)
                implementation(libs.whyoleg.cryptography.random)
                implementation(libs.kermit)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(projects.sphereonKmpCommon)
                implementation(npm("@js-joda/core", "5.6.3"))
                implementation(npm("@js-joda/timezone", "2.3.0"))
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.3.1")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotest.property)
                implementation(npm("@js-joda/core", "5.6.3"))
                implementation(npm("@js-joda/timezone", "2.3.0"))
                implementation(npm("@sphereon/ssi-sdk-ext.x509-utils", "0.24.1-unstable.9"))
                /*// Internal mock test JS API. Uses the exposed Crypto interfaces of this module
                val pathToLocalNpmModule = projectDir.resolve("src/jsMain/resources/crypto-x509-example-js").canonicalPath
                implementation(npm("@sphereon-internal/crypto-x509-example-js", "file:$pathToLocalNpmModule"))*/


            }
        }

        /* val nativeMain by getting {
             dependencies {}
         }
         val nativeTest by getting*/
    }
}
