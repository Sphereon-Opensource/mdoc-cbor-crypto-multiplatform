import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
//    alias(libs.plugins.androidLibrary)
    kotlin("multiplatform") version "2.0.0"
    kotlin("plugin.serialization") version "1.9.23"
    id("io.kotest.multiplatform") version "5.8.1"
//    id("com.google.devtools.ksp") version "2.0.0-RC3-1.0.20"
    id("module.publication")
}

group = "com.sphereon"
version = "0.1.0-SNAPSHOT"

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
        moduleName = "@sphereon/mdoc"
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
        /*  browser {
              testTask {
                  useKarma {
                      useChrome()
                  }
              }
          }*/
        /*nodejs {
            generateTypeScriptDefinitions()
            testTask {
                debug = true
                useMocha()
            }
        }*/

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
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.cbor)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.4")
                implementation("dev.whyoleg.cryptography:cryptography-core:0.3.1")
                implementation("dev.whyoleg.cryptography:cryptography-serialization-pem:0.3.1")
                implementation("dev.whyoleg.cryptography:cryptography-serialization-asn1:0.3.1")
                implementation("dev.whyoleg.cryptography:cryptography-serialization-asn1-modules:0.3.1")
                implementation("dev.whyoleg.cryptography:cryptography-random:0.3.1")
                implementation("co.touchlab:kermit:2.0.4")


            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0-RC")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-jdk:0.3.1")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.3.1")
                implementation(npm("@auth0/cose", "1.0.2"))
                implementation(npm("pkijs", "3.2.4"))
                implementation(npm("@js-joda/timezone", "2.3.0"))
                val pathToLocalNpmModule = rootProject.projectDir.resolve("src/jsMain/resources/mdoc-js-api").canonicalPath
                implementation(npm("@sphereon-internal/mdoc-js-api", "file:$pathToLocalNpmModule"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotest.property)
            }
        }
        /* val nativeMain by getting {
             dependencies {}
         }
         val nativeTest by getting*/
    }
}

/*
android {
    namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
*/
dependencies {
//    add("kspJs", "deezer.kustomexport:compiler:0.8.2")
}
