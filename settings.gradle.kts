pluginManagement {
    includeBuild("convention-plugins")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
//enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
/*
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
*/

rootProject.name = "mdoc"
