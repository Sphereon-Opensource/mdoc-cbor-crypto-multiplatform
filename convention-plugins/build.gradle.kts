plugins {
    kotlin("multiplatform") version libs.versions.kotlin apply false
    `kotlin-dsl`
}

dependencies {
    implementation(libs.nexus.publish)
}