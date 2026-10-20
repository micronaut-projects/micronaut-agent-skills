import io.micronaut.build.TestFramework

plugins {
    id("io.micronaut.build.internal.agent-skills-module")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    annotationProcessor(mnValidation.micronaut.validation.processor)

    // @Serdeable and KebabCaseStrategy appear on public types
    api(mnSerde.micronaut.serde.api)
    // jakarta.validation constraints appear on public record components
    api(mnValidation.validation)
    // YAML frontmatter parsing
    implementation(mnSerde.micronaut.serde.yaml)
    // jakarta.validation.Validator used by SkillValidator
    implementation(mnValidation.micronaut.validation)

    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mnTest.junit.jupiter.params)
    testRuntimeOnly(mnLogging.logback.classic)
}

micronautBuild {
    testFramework = TestFramework.JUNIT6
}

tasks.withType<Test> {
    useJUnitPlatform()
}
