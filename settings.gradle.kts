pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.build.shared.settings") version "8.0.1"
}

rootProject.name = "micronaut-agent-skills-parent"

include("micronaut-agent-skills")
include("micronaut-agent-skills-bom")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

micronautBuild {
    importMicronautCatalog()
}
