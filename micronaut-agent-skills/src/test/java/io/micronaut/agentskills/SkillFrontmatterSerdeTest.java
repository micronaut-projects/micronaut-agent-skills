package io.micronaut.agentskills;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class SkillFrontmatterSerdeTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void optionalPropertiesAreOmittedWhenNull() throws IOException {
        SkillFrontmatter frontmatter = new SkillFrontmatter("pdf-processing", "Extract PDF text.", null, null, null, null);
        assertEquals("{\"name\":\"pdf-processing\",\"description\":\"Extract PDF text.\"}",
            objectMapper.writeValueAsString(frontmatter));
    }

    @Test
    void emptyMetadataIsOmitted() throws IOException {
        SkillFrontmatter frontmatter = new SkillFrontmatter("pdf-processing", "Extract PDF text.", null, null, Map.of(), null);
        assertEquals("{\"name\":\"pdf-processing\",\"description\":\"Extract PDF text.\"}",
            objectMapper.writeValueAsString(frontmatter));
    }

    @Test
    void allowedToolsIsSerializedInKebabCase() throws IOException {
        SkillFrontmatter frontmatter = new SkillFrontmatter("pdf-processing", "Extract PDF text.", "Apache-2.0",
            "Requires Python 3.14+ and uv", Map.of("author", "example-org"), "Bash(git:*) Read");
        String json = objectMapper.writeValueAsString(frontmatter);
        assertTrue(json.contains("\"allowed-tools\":\"Bash(git:*) Read\""), json);
        assertFalse(json.contains("allowedTools"), json);
        assertTrue(json.contains("\"license\":\"Apache-2.0\""), json);
        assertTrue(json.contains("\"compatibility\":\"Requires Python 3.14+ and uv\""), json);
        assertTrue(json.contains("\"metadata\":{\"author\":\"example-org\"}"), json);
    }

    @Test
    void roundTrip() throws IOException {
        SkillFrontmatter frontmatter = new SkillFrontmatter("pdf-processing", "Extract PDF text.", "Apache-2.0",
            "Requires Python 3.14+ and uv", Map.of("author", "example-org", "version", "1.0"), "Bash(git:*) Read");
        String json = objectMapper.writeValueAsString(frontmatter);
        assertEquals(frontmatter, objectMapper.readValue(json, SkillFrontmatter.class));
    }

    @Test
    void allowedToolsIsDeserializedFromKebabCase() throws IOException {
        SkillFrontmatter frontmatter = objectMapper.readValue(
            "{\"name\":\"x\",\"description\":\"y\",\"allowed-tools\":\"Bash\"}", SkillFrontmatter.class);
        assertEquals("Bash", frontmatter.allowedTools());
        assertNull(frontmatter.license());
        assertNull(frontmatter.compatibility());
        assertNull(frontmatter.metadata());
    }

    @Test
    void missingRequiredPropertiesDeserializeAsNull() throws IOException {
        // Serialization does not enforce presence; @NotBlank reports it during validation and
        // DefaultSkillReader rejects SKILL.md files that lack the required keys.
        assertNull(objectMapper.readValue("{\"description\":\"y\"}", SkillFrontmatter.class).name());
        assertNull(objectMapper.readValue("{\"name\":\"x\"}", SkillFrontmatter.class).description());
    }

    @Test
    void unknownPropertiesAreIgnoredByPlainSerialization() throws IOException {
        // The reader is responsible for rejecting unknown frontmatter keys; see DefaultSkillReaderTest.
        SkillFrontmatter frontmatter = objectMapper.readValue(
            "{\"name\":\"x\",\"description\":\"y\",\"foo\":\"bar\"}", SkillFrontmatter.class);
        assertEquals("x", frontmatter.name());
    }
}
