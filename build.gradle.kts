import dev.petuska.npm.publish.extension.NpmPublishExtension

allprojects {
    group = "com.sphereon.kmp"
    version = "0.2.15"

    val npmVersion by extra { getNpmVersion() }
}

plugins {
    kotlin("multiplatform") version libs.versions.kotlin apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
    id("io.kotest.multiplatform") version libs.versions.kotest apply false
//    id("com.google.devtools.ksp") version "2.0.0-RC3-1.0.20"
//    kotlin("jvm") apply false
    id("module.publication") apply false
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.npmPublish)
}

tasks.named<com.github.gradle.node.task.NodeSetupTask>("nodeSetup") {
    doFirst {
        println("➡ nodeSetup running")
        println("➡ node download = ${project.extensions.getByType<com.github.gradle.node.NodeExtension>().download.get()}")
        println("➡ node version = ${project.extensions.getByType<com.github.gradle.node.NodeExtension>().version.get()}")
        println("➡ node distBaseUrl = ${project.extensions.getByType<com.github.gradle.node.NodeExtension>().distBaseUrl.get()}")
    }
}

fun getNpmVersion(): String {
    val baseVersion = project.version.toString()
    if (!baseVersion.endsWith("-SNAPSHOT")) {
        return baseVersion
    }

    // For SNAPSHOT versions, create an unstable.<commit-hash> version
    val versionBase = baseVersion.removeSuffix("-SNAPSHOT")

    // Get git commit hash
    val gitCommitHash = try {
        val process = ProcessBuilder("git", "rev-parse", "--short=7", "HEAD")
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        process.inputStream.bufferedReader().use { it.readLine() }
    } catch (e: Exception) {
        "unknown"
    }

    return "$versionBase-unstable.$gitCommitHash"
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
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

subprojects {

    plugins.withType<MavenPublishPlugin> {
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "sphereon-opensource"
                    val snapshotsUrl = "https://nexus.sphereon.com/repository/sphereon-opensource-snapshots/"
                    val releasesUrl = "https://nexus.sphereon.com/repository/sphereon-opensource-releases/"
                    url = uri(if (version.toString().contains("SNAPSHOT")) snapshotsUrl else releasesUrl)
                    credentials {
                        username = System.getenv("NEXUS_USERNAME")
                        password = System.getenv("NEXUS_PASSWORD")
                    }
                }
            }

            // Ensure unique coordinates for different publication types
            publications.withType<MavenPublication> {
                val publicationName = name
                if (publicationName == "kotlinMultiplatform") {
                    artifactId = "${project.name}-multiplatform"
                } else if (publicationName == "mavenKotlin") {
                    artifactId = "${project.name}-jvm"
                }
            }
        }


    }


    plugins.withId("com.github.node-gradle.node") {
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
        val nodeJsVersion = libs.findVersion("nodejs").get().requiredVersion

        extensions.configure<com.github.gradle.node.NodeExtension> {
            version.set(nodeJsVersion)
            download.set(true)
            workDir.set(layout.projectDirectory.dir(".gradle/nodejs"))
            nodeProjectDir.set(layout.projectDirectory.dir(".gradle"))
        }
    }

    plugins.withId("dev.petuska.npm.publish") {
        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
        val nodeJsVersion = libs.findVersion("nodejs").get().requiredVersion
        val osName = System.getProperty("os.name").let {
            when {
                it.startsWith("Windows") -> "win"
                it.startsWith("Mac") -> "darwin"
                it.startsWith("Linux") -> "linux"
                else -> error("Unsupported OS: $it")
            }
        }

        val nodeDir = rootProject.layout.projectDirectory.dir("/opt/hostedtoolcache/node/$nodeJsVersion/x64")

        extensions.configure<NpmPublishExtension> {
            nodeHome.set(nodeDir.asFile)
        }
    }
}
