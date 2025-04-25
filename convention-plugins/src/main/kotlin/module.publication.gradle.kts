import dev.petuska.npm.publish.extension.NpmPublishExtension

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Kotlin Multiplatform library template")
            description.set("Dummy library to test deployment to Maven Central")
            url.set("https://github.com/Kotlin/multiplatform-library-template")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("JetBrains")
                    name.set("JetBrains Team")
                    organization.set("JetBrains")
                    organizationUrl.set("https://www.jetbrains.com")
                }
            }
            scm {
                url.set("https://github.com/Kotlin/multiplatform-library-template")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}

plugins.withId("com.github.node-gradle.node") {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    val nodeJsVersion = libs.findVersion("nodejs").get().requiredVersion

    extensions.configure<com.github.gradle.node.NodeExtension> {
        version.set(nodeJsVersion)
        download.set(true)
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

    val nodeDir = rootProject.layout.projectDirectory.dir(".gradle/nodejs/node-v$nodeJsVersion-$osName-x64")

    extensions.configure<NpmPublishExtension> {
        nodeHome.set(nodeDir.get().asFile)
    }
}
