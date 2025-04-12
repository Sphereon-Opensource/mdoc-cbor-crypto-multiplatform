allprojects {
    group = "com.sphereon.kmp"
    version = "0.2.0-SNAPSHOT.25"
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.kotest) apply false
//    id("com.google.devtools.ksp") version "2.0.0-RC3-1.0.20"
//    kotlin("jvm") apply false
    id("module.publication") apply false
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}



kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}
repositories {
    mavenCentral()
}
