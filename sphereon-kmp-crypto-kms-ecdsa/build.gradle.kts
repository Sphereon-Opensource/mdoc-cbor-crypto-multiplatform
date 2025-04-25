import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.npmPublish)
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("io.kotest.multiplatform")
    id("module.publication")
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().download = false
}
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.the<YarnRootExtension>().download = false
}
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        name = "bigNum"
    }
    maven(url = "https://raw.githubusercontent.com/Deezer/KustomExport/mvn-repo")
}

kotlin {
    kotlin.applyDefaultHierarchyTemplate()

    jvmToolchain(21)
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    js(IR) {
        nodejs {
            testTask {
                // useMocha()
            }
        }
        browser {
            testTask {
                // useMocha()
            }
        }

        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        val commonMain by getting {
            dependencies {
                implementation(projects.sphereonKmpCommon)
                implementation(projects.sphereonKmpCbor)
                implementation(projects.sphereonKmpCrypto)
                implementation(projects.sphereonKmpCryptoKms)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.whyoleg.cryptography.core)
                implementation(libs.kotlinx.io.core)
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
        val jvmTest by getting {
            dependencies {
                implementation(libs.whyoleg.cryptography.provider.jdk)
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotest.property)
                implementation(libs.whyoleg.cryptography.provider.webcrypto)

            }
        }
    }
}


npmPublish {
    val nodeJsVersion = libs.versions.nodejs.get()
    val osName = System.getProperty("os.name").let {
        when {
            it.startsWith("Windows") -> "win"
            it.startsWith("Mac") -> "darwin"
            it.startsWith("Linux") -> "linux"
            else -> throw GradleException("Unsupported OS: $it")
        }
    }

    val nodeDir = file("${ getLayout().buildDirectory}/nodejs/node-v$nodeJsVersion-$osName-x64")
    nodeHome.set(nodeDir)

    registries {
        register("npmjs") {
            uri.set("https://registry.npmjs.org")
            authToken.set(System.getenv("NPM_TOKEN") ?: "")
        }
    }
    packages {
        named("js") {
            packageJson {
                "name" by "@sphereon/kmp-kms-ecdsa"
                "version" by rootProject.extra["npmVersion"] as String
            }
            scope.set("@sphereon")
            packageName.set("kmp-kms-ecdsa")
        }
    }
}

