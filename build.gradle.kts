allprojects {
    group = "com.sphereon"
    version = "0.2.0-SNAPSHOT"
}

plugins {
    kotlin("multiplatform") version libs.versions.kotlin apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
    id("io.kotest.multiplatform") version libs.versions.kotest apply false
//    id("com.google.devtools.ksp") version "2.0.0-RC3-1.0.20"
//    kotlin("jvm") apply false
    id("module.publication") apply false
    kotlin("jvm") version libs.versions.kotlin
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
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

