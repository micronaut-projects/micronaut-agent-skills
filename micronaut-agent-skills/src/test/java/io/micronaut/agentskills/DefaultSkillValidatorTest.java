package io.micronaut.agentskills;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class DefaultSkillValidatorTest {

    @Inject
    SkillValidator skillValidator;

    @Test
    void validSkillDirectoryHasNoErrors(@TempDir Path dir) throws IOException {
        Path skillDir = skill(dir, "pdf-processing", "name: pdf-processing\ndescription: Extract PDF text.\n");
        assertEquals(List.of(), skillValidator.validate(skillDir));
    }

    @Test
    void missingPath(@TempDir Path dir) {
        Path missing = dir.resolve("missing");
        assertEquals(List.of("Path does not exist: " + missing), skillValidator.validate(missing));
    }

    @Test
    void notADirectory(@TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("SKILL.md"), "");
        assertEquals(List.of("Not a directory: " + file), skillValidator.validate(file));
    }

    @Test
    void missingSkillFile(@TempDir Path dir) {
        assertEquals(List.of("Missing required file: SKILL.md"), skillValidator.validate(dir));
    }

    @Test
    void parseErrorsAreReported(@TempDir Path dir) throws IOException {
        Path skillDir = skill(dir, "pdf-processing", "license: MIT\n");
        assertEquals(List.of("Missing required field in frontmatter: description",
            "Missing required field in frontmatter: name"), skillValidator.validate(skillDir));
    }

    @Test
    void directoryNameMustMatchSkillName(@TempDir Path dir) throws IOException {
        Path skillDir = skill(dir, "pdf", "name: pdf-processing\ndescription: Extract PDF text.\n");
        assertEquals(List.of("Directory name 'pdf' must match skill name 'pdf-processing'"),
            skillValidator.validate(skillDir));
    }

    @Test
    void constraintViolationsAreReported(@TempDir Path dir) throws IOException {
        Path skillDir = skill(dir, "PDF-Processing", "name: PDF-Processing\ndescription: \"\"\ncompatibility: \"\"\n");
        List<String> errors = skillValidator.validate(skillDir);
        assertEquals(3, errors.size(), errors.toString());
        assertTrue(errors.get(0).startsWith("frontmatter.compatibility: "), errors.toString());
        assertTrue(errors.get(1).startsWith("frontmatter.description: "), errors.toString());
        assertTrue(errors.get(2).startsWith("frontmatter.name: "), errors.toString());
    }

    @Test
    void parsedSkillCanBeValidatedWithoutDirectory() {
        Skill skill = new Skill(new SkillFrontmatter("pdf-processing", "d", null, null, null, null), "");
        assertEquals(List.of(), skillValidator.validate(skill, null));
        assertEquals(List.of("Directory name 'other' must match skill name 'pdf-processing'"),
            skillValidator.validate(skill, "other"));
    }

    @Test
    void bundledFixtureIsValid() {
        Path skillDir = Path.of("src", "test", "resources", "skills", "pdf-processing");
        assertEquals(List.of(), skillValidator.validate(skillDir));
    }

    private static Path skill(Path parent, String directoryName, String frontmatter) throws IOException {
        Path skillDir = Files.createDirectory(parent.resolve(directoryName));
        Files.writeString(skillDir.resolve("SKILL.md"), "---\n" + frontmatter + "---\n\nInstructions.\n");
        return skillDir;
    }
}
