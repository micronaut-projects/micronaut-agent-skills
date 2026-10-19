package io.micronaut.agentskills;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class DefaultSkillReaderTest {

    private static final String MINIMAL = """
        ---
        name: skill-name
        description: A description of what this skill does and when to use it.
        ---
        """;

    private static final String FULL = """
        ---
        name: pdf-processing
        description: Extract PDF text, fill forms, merge files. Use when handling PDFs.
        license: Apache-2.0
        compatibility: Requires Python 3.14+ and uv
        metadata:
          author: example-org
          version: "1.0"
        allowed-tools: Bash(git:*) Bash(jq:*) Read
        ---

        # PDF processing

        See [the reference guide](references/REFERENCE.md) for details.
        """;

    @Inject
    SkillReader skillReader;

    @Test
    void parsesMinimalExample() {
        Skill skill = skillReader.parse(MINIMAL);
        assertEquals("skill-name", skill.frontmatter().name());
        assertEquals("A description of what this skill does and when to use it.", skill.frontmatter().description());
        assertNull(skill.frontmatter().license());
        assertNull(skill.frontmatter().compatibility());
        assertNull(skill.frontmatter().metadata());
        assertNull(skill.frontmatter().allowedTools());
        assertEquals("", skill.body());
    }

    @Test
    void parsesEveryOptionalField() {
        Skill skill = skillReader.parse(FULL);
        SkillFrontmatter expected = new SkillFrontmatter("pdf-processing",
            "Extract PDF text, fill forms, merge files. Use when handling PDFs.", "Apache-2.0",
            "Requires Python 3.14+ and uv", Map.of("author", "example-org", "version", "1.0"),
            "Bash(git:*) Bash(jq:*) Read");
        assertEquals(expected, skill.frontmatter());
        assertEquals("# PDF processing\n\nSee [the reference guide](references/REFERENCE.md) for details.", skill.body());
    }

    @Test
    void bodyKeepsLaterDelimiters() {
        Skill skill = skillReader.parse(MINIMAL + "\nIntro\n\n---\n\nAfter a horizontal rule\n");
        assertEquals("Intro\n\n---\n\nAfter a horizontal rule", skill.body());
    }

    @Test
    void numericMetadataValuesAreCoercedToStrings() {
        Skill skill = skillReader.parse("""
            ---
            name: skill-name
            description: d
            metadata:
              version: 1.0
              count: 3
              flag: true
            ---
            """);
        assertEquals(Map.of("version", "1.0", "count", "3", "flag", "true"), skill.frontmatter().metadata());
    }

    @Test
    void contentMustStartWithFrontmatter() {
        SkillParseException e = assertThrows(SkillParseException.class, () -> skillReader.parse("# No frontmatter\n"));
        assertEquals("SKILL.md must start with YAML frontmatter (---)", e.getMessage());
    }

    @Test
    void frontmatterMustBeClosed() {
        SkillParseException e = assertThrows(SkillParseException.class,
            () -> skillReader.parse("---\nname: skill-name\ndescription: d\n"));
        assertEquals("SKILL.md frontmatter not properly closed with ---", e.getMessage());
    }

    @Test
    void frontmatterMustBeValidYaml() {
        SkillParseException e = assertThrows(SkillParseException.class,
            () -> skillReader.parse("---\nname: [unclosed\ndescription: d\n---\n"));
        assertTrue(e.getMessage().startsWith("Invalid YAML in frontmatter: "), e.getMessage());
    }

    @Test
    void frontmatterMustBeAMapping() {
        SkillParseException e = assertThrows(SkillParseException.class,
            () -> skillReader.parse("---\n- name\n- description\n---\n"));
        assertEquals("SKILL.md frontmatter must be a YAML mapping", e.getMessage());
    }

    @Test
    void unknownKeysAreRejected() {
        SkillParseException e = assertThrows(SkillParseException.class, () -> skillReader.parse("""
            ---
            name: skill-name
            description: d
            version: 2
            author: me
            ---
            """));
        assertEquals("Unexpected fields in frontmatter: author, version. "
            + "Only [allowed-tools, compatibility, description, license, metadata, name] are allowed.", e.getMessage());
    }

    @Test
    void requiredKeysAreReported() {
        SkillParseException e = assertThrows(SkillParseException.class, () -> skillReader.parse("---\nlicense: MIT\n---\n"));
        assertEquals(List.of("Missing required field in frontmatter: description",
            "Missing required field in frontmatter: name"), e.getErrors());
    }

    @Test
    void nullRequiredValuesBecomeEmptyStrings() {
        Skill skill = skillReader.parse("---\nname:\ndescription: d\n---\n");
        assertEquals("", skill.frontmatter().name());
    }

    @Test
    void readsSkillFileFromDirectory(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("SKILL.md"), MINIMAL);
        assertEquals("skill-name", skillReader.read(dir).frontmatter().name());
        assertEquals(dir.resolve("SKILL.md"), skillReader.findSkillFile(dir).orElseThrow());
    }

    @Test
    void acceptsLowercaseSkillFile(@TempDir Path dir) throws IOException {
        Path skillFile = Files.writeString(dir.resolve("skill.md"), MINIMAL);
        // Case-insensitive file systems resolve SKILL.md to the same file, so compare identity rather than names.
        assertTrue(Files.isSameFile(skillFile, skillReader.findSkillFile(dir).orElseThrow()));
        assertEquals("skill-name", skillReader.read(dir).frontmatter().name());
    }

    @Test
    void missingSkillFileIsAParseError(@TempDir Path dir) {
        assertTrue(skillReader.findSkillFile(dir).isEmpty());
        SkillParseException e = assertThrows(SkillParseException.class, () -> skillReader.read(dir));
        assertEquals("Missing required file: SKILL.md", e.getMessage());
    }
}
