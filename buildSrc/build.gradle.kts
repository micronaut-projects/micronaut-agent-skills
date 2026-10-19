plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // The version is written to buildSrc/gradle.properties by the io.micronaut.build.shared.settings plugin
    implementation(providers.gradleProperty("micronaut-build-version").map { "io.micronaut.build.internal:micronaut-gradle-plugins:$it" })
}
