// If you don't need any common settings/dependencies/... for everything, remove this convention plugin and the reference to it in `io.micronaut.build.internal.agent-skills-module.gradle.kts` file
import io.micronaut.build.MicronautBuildExtension
import io.micronaut.build.compat.BinaryCompatibibilityExtension

// There is no released version to compare against yet, so skip the binary compatibility check until 1.0.0 is out.
pluginManager.withPlugin("io.micronaut.build.internal.binary-compatibility-check") {
    extensions.configure<MicronautBuildExtension> {
        (this as ExtensionAware).extensions.configure<BinaryCompatibibilityExtension> {
            enabledAfter("1.0.0")
        }
    }
}
