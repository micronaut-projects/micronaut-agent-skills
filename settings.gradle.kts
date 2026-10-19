pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.build.shared.settings") version "8.1.5"
}

rootProject.name = "micronaut-agent-skills-parent"

include("micronaut-agent-skills")
include("micronaut-agent-skills-cli")
include("micronaut-agent-skills-bom")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

micronautBuild {
    importMicronautCatalog()
    importMicronautCatalog("micronaut-serde")
    importMicronautCatalog("micronaut-validation")
}
