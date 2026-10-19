package io.micronaut.agentskills.cli;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicronautAgentSkillsCliCommandTest {

    private static final String VALID_SKILL = """
        ---
        name: pdf-processing
        description: Extracts text and tables from PDF files.
        ---
        # PDF Processing
        """;

    private static final String UNKNOWN_FIELD_SKILL = """
        ---
        name: broken-skill
        description: Uses a field the specification does not define.
        mcp: true
        ---
        # Broken
        """;

    private static final String MISMATCHED_NAME_SKILL = """
        ---
        name: some-other-name
        description: The directory name does not match.
        ---
        # Mismatch
        """;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void captureOutput() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void allSkillsValidExitsWithZero(@TempDir Path skills) throws IOException {
        writeSkill(skills, "pdf-processing", VALID_SKILL);

        int exitCode = run(skills.toString());

        assertEquals(MicronautAgentSkillsCliCommand.EXIT_VALID, exitCode);
        String output = output();
        assertFalse(output.contains("pdf-processing"));
        assertTrue(output.contains("1 skill checked, 1 valid, 0 invalid"));
    }

    @Test
    void verboseListsValidSkills(@TempDir Path skills) throws IOException {
        writeSkill(skills, "pdf-processing", VALID_SKILL);

        int exitCode = run("--verbose", skills.toString());

        assertEquals(MicronautAgentSkillsCliCommand.EXIT_VALID, exitCode);
        assertTrue(output().contains("pdf-processing: valid"));
    }

    @Test
    void invalidSkillsAreReportedWithErrors(@TempDir Path skills) throws IOException {
        writeSkill(skills, "pdf-processing", VALID_SKILL);
        writeSkill(skills, "broken-skill", UNKNOWN_FIELD_SKILL);
        writeSkill(skills, "mismatch", MISMATCHED_NAME_SKILL);
        Files.createDirectory(skills.resolve("empty"));

        int exitCode = run(skills.toString());

        assertEquals(MicronautAgentSkillsCliCommand.EXIT_INVALID, exitCode);
        String output = output();
        assertTrue(output.contains("broken-skill: invalid"));
        assertTrue(output.contains("  - Unexpected fields in frontmatter: mcp."));
        assertTrue(output.contains("mismatch: invalid"));
        assertTrue(output.contains("  - Directory name 'mismatch' must match skill name 'some-other-name'"));
        assertTrue(output.contains("empty: invalid"));
        assertTrue(output.contains("  - Missing required file: SKILL.md"));
        assertFalse(output.contains("pdf-processing"));
        assertTrue(output.contains("4 skills checked, 1 valid, 3 invalid"));
    }

    @Test
    void directoryWithoutSkillsIsValid(@TempDir Path skills) {
        int exitCode = run(skills.toString());

        assertEquals(MicronautAgentSkillsCliCommand.EXIT_VALID, exitCode);
        assertTrue(output().contains("No skills found in " + skills));
    }

    @Test
    void missingDirectoryIsAUsageError(@TempDir Path skills) {
        Path missing = skills.resolve("missing");

        int exitCode = run(missing.toString());

        assertEquals(MicronautAgentSkillsCliCommand.EXIT_USAGE, exitCode);
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("Not a directory: " + missing));
    }

    @Test
    void bundledFixtureIsValid() {
        Path skills = Path.of("..", "micronaut-agent-skills", "src", "test", "resources", "skills");

        int exitCode = run("-v", skills.toString());

        assertEquals(MicronautAgentSkillsCliCommand.EXIT_VALID, exitCode);
        assertTrue(output().contains("pdf-processing: valid"));
    }

    private int run(String... args) {
        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            Integer exitCode = PicocliRunner.call(MicronautAgentSkillsCliCommand.class, ctx, args);
            return exitCode == null ? -1 : exitCode;
        }
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private static void writeSkill(Path skills, String directoryName, String content) throws IOException {
        Path skillDirectory = Files.createDirectory(skills.resolve(directoryName));
        Files.writeString(skillDirectory.resolve("SKILL.md"), content);
    }
}
