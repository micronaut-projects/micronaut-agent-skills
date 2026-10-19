package io.micronaut.agentskills;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class SkillFrontmatterValidationTest {

    @Inject
    Validator validator;

    @ParameterizedTest
    @ValueSource(strings = {"pdf-processing", "a", "data-2024", "x1-y2-z3", "code-review", "ünïcode-name"})
    void validNames(String name) {
        assertTrue(validator.validate(frontmatter(name)).isEmpty());
    }

    @Test
    void nameMayBe64Characters() {
        assertTrue(validator.validate(frontmatter("a".repeat(64))).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "PDF-Processing", "-pdf", "pdf-", "pdf--processing", "pdf_processing",
        "pdf processing", "pdf.processing", "pdf/processing"})
    void invalidNames(String name) {
        assertEquals(List.of("name"), paths(validator.validate(frontmatter(name))));
    }

    @Test
    void nameMustNotExceed64Characters() {
        assertEquals(List.of("name"), paths(validator.validate(frontmatter("a".repeat(65)))));
    }

    @Test
    void nameMustNotBeNull() {
        assertEquals(List.of("name"), paths(validator.validate(new SkillFrontmatter(null, "d", null, null, null, null))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void descriptionMustNotBeBlank(String description) {
        SkillFrontmatter frontmatter = new SkillFrontmatter("skill", description, null, null, null, null);
        assertEquals(List.of("description"), paths(validator.validate(frontmatter)));
    }

    @Test
    void descriptionMayBe1024Characters() {
        SkillFrontmatter frontmatter = new SkillFrontmatter("skill", "d".repeat(1024), null, null, null, null);
        assertTrue(validator.validate(frontmatter).isEmpty());
    }

    @Test
    void descriptionMustNotExceed1024Characters() {
        SkillFrontmatter frontmatter = new SkillFrontmatter("skill", "d".repeat(1025), null, null, null, null);
        assertEquals(List.of("description"), paths(validator.validate(frontmatter)));
    }

    @Test
    void compatibilityIsOptional() {
        assertTrue(validator.validate(frontmatter("skill")).isEmpty());
        assertTrue(validator.validate(withCompatibility("Requires git and docker")).isEmpty());
        assertTrue(validator.validate(withCompatibility("c".repeat(500))).isEmpty());
    }

    @Test
    void compatibilityMustNotBeEmptyWhenPresent() {
        assertEquals(List.of("compatibility"), paths(validator.validate(withCompatibility(""))));
    }

    @Test
    void compatibilityMustNotExceed500Characters() {
        assertEquals(List.of("compatibility"), paths(validator.validate(withCompatibility("c".repeat(501)))));
    }

    @Test
    void metadataIsOptional() {
        assertTrue(validator.validate(withMetadata(null)).isEmpty());
        assertTrue(validator.validate(withMetadata(Map.of())).isEmpty());
        assertTrue(validator.validate(withMetadata(Map.of("author", "example-org", "version", "1.0"))).isEmpty());
    }

    @Test
    void metadataKeysMustNotBeBlank() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("", "value");
        List<String> paths = paths(validator.validate(withMetadata(metadata)));
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).startsWith("metadata"), paths.get(0));
    }

    @Test
    void metadataValuesMustNotBeNull() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("author", null);
        List<String> paths = paths(validator.validate(withMetadata(metadata)));
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).startsWith("metadata"), paths.get(0));
    }

    @Test
    void allowedToolsIsAFreeFormString() {
        SkillFrontmatter frontmatter = new SkillFrontmatter("skill", "d", null, null, null, "Bash(git:*) Bash(jq:*) Read");
        assertTrue(validator.validate(frontmatter).isEmpty());
    }

    @Test
    void skillCascadesValidationToFrontmatter() {
        Skill skill = new Skill(frontmatter("Bad Name"), "body");
        assertEquals(List.of("frontmatter.name"), paths(validator.validate(skill)));
        assertTrue(validator.validate(new Skill(frontmatter("good-name"), "body")).isEmpty());
    }

    @Test
    void skillRequiresFrontmatterAndBody() {
        assertEquals(List.of("body", "frontmatter"), paths(validator.validate(new Skill(null, null))));
    }

    private static SkillFrontmatter frontmatter(String name) {
        return new SkillFrontmatter(name, "A description of what this skill does and when to use it.", null, null, null, null);
    }

    private static SkillFrontmatter withCompatibility(String compatibility) {
        return new SkillFrontmatter("skill", "d", null, compatibility, null, null);
    }

    private static SkillFrontmatter withMetadata(Map<String, String> metadata) {
        return new SkillFrontmatter("skill", "d", null, null, metadata, null);
    }

    private static <T> List<String> paths(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(v -> v.getPropertyPath().toString()).distinct().sorted().toList();
    }
}
