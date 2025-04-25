import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("io.kotest.multiplatform")
    id("module.publication")
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.npmPublish)
}

val shouldRunAwsKmsTestsProvider = System.getenv("AWS_RUN_TESTS").equals("true", ignoreCase = true)

kotlin {
    kotlin.applyDefaultHierarchyTemplate()

    jvmToolchain(21)
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
                enabled = shouldRunAwsKmsTestsProvider
            }
        }
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
                implementation(project.dependencies.platform(awssdk.bom))
                implementation(awssdk.services.kms)
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
                api(libs.slf4j.simple)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.whyoleg.cryptography.provider.jdk)
            }
        }
    }
}

buildkonfig {
    packageName = "com.sphereon.crypto.kms.aws"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "AWS_REGION",
            System.getenv("AWS_REGION"), nullable = true)
        buildConfigField(FieldSpec.Type.STRING, "AWS_ACCESS_KEY_ID",
            System.getenv("AWS_ACCESS_KEY_ID"), nullable = true)
        buildConfigField(FieldSpec.Type.STRING, "AWS_SECRET_ACCESS_KEY",
            System.getenv("AWS_SECRET_ACCESS_KEY"), nullable = true)
    }
}
