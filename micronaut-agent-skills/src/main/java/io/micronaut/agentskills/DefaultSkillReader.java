/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.agentskills;

import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.yaml.YamlObjectMapper;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Default {@link SkillReader} backed by Micronaut Serialization's YAML support.
 *
 * @since 0.0.1
 */
@Singleton
public class DefaultSkillReader implements SkillReader {

    private static final Argument<JsonNode> JSON_NODE = Argument.of(JsonNode.class);
    private static final Argument<SkillFrontmatter> FRONTMATTER = Argument.of(SkillFrontmatter.class);
    private static final List<String> SKILL_FILE_NAMES =
        List.of(SkillFiles.SKILL_FILE_NAME, SkillFiles.LOWERCASE_SKILL_FILE_NAME);
    private static final String ALLOWED_KEYS = new TreeSet<>(SkillFrontmatter.KEYS).toString();

    private final ObjectMapper yamlMapper;

    /**
     * Creates the reader.
     *
     * @param yamlMapper The YAML object mapper.
     */
    public DefaultSkillReader(YamlObjectMapper yamlMapper) {
        this.yamlMapper = yamlMapper;
    }

    @Override
    public Optional<Path> findSkillFile(Path skillDirectory) {
        for (String fileName : SKILL_FILE_NAMES) {
            Path candidate = skillDirectory.resolve(fileName);
            if (Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    @Override
    public Skill read(Path skillDirectory) {
        Path skillFile = findSkillFile(skillDirectory)
            .orElseThrow(() -> new SkillParseException("Missing required file: " + SkillFiles.SKILL_FILE_NAME));
        String content;
        try {
            content = Files.readString(skillFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + skillFile, e);
        }
        return parse(content);
    }

    @Override
    public Skill parse(String content) {
        if (!content.startsWith(SkillFiles.FRONTMATTER_DELIMITER)) {
            throw new SkillParseException(SkillFiles.SKILL_FILE_NAME + " must start with YAML frontmatter ("
                + SkillFiles.FRONTMATTER_DELIMITER + ")");
        }
        String[] parts = content.split(SkillFiles.FRONTMATTER_DELIMITER, 3);
        if (parts.length < 3) {
            throw new SkillParseException(SkillFiles.SKILL_FILE_NAME + " frontmatter not properly closed with "
                + SkillFiles.FRONTMATTER_DELIMITER);
        }
        SkillFrontmatter frontmatter = toFrontmatter(parseYaml(parts[1]));
        return new Skill(frontmatter, parts[2].strip());
    }

    private JsonNode parseYaml(String yaml) {
        JsonNode node;
        try {
            node = yamlMapper.readValue(yaml.getBytes(StandardCharsets.UTF_8), JSON_NODE);
        } catch (IOException | RuntimeException e) {
            throw new SkillParseException("Invalid YAML in frontmatter: " + e.getMessage(), e);
        }
        if (node == null || !node.isObject()) {
            throw new SkillParseException(SkillFiles.SKILL_FILE_NAME + " frontmatter must be a YAML mapping");
        }
        return node;
    }

    private SkillFrontmatter toFrontmatter(JsonNode frontmatterNode) {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : frontmatterNode.entries()) {
            fields.put(entry.getKey(), entry.getValue());
        }
        rejectUnexpectedKeys(fields);
        requireKeys(fields);
        JsonNode metadata = fields.get(SkillFrontmatter.METADATA_KEY);
        if (metadata != null && metadata.isObject()) {
            fields.put(SkillFrontmatter.METADATA_KEY, coerceValuesToStrings(metadata));
        }
        try {
            return yamlMapper.readValueFromTree(JsonNode.createObjectNode(fields), FRONTMATTER);
        } catch (IOException e) {
            throw new SkillParseException("Invalid frontmatter: " + e.getMessage(), e);
        }
    }

    private static void rejectUnexpectedKeys(Map<String, JsonNode> fields) {
        List<String> unexpected = fields.keySet().stream()
            .filter(key -> !SkillFrontmatter.KEYS.contains(key))
            .sorted()
            .toList();
        if (!unexpected.isEmpty()) {
            throw new SkillParseException("Unexpected fields in frontmatter: " + String.join(", ", unexpected)
                + ". Only " + ALLOWED_KEYS + " are allowed.");
        }
    }

    private static void requireKeys(Map<String, JsonNode> fields) {
        List<String> errors = new ArrayList<>();
        for (String key : new TreeSet<>(SkillFrontmatter.REQUIRED_KEYS)) {
            JsonNode value = fields.get(key);
            if (value == null) {
                errors.add("Missing required field in frontmatter: " + key);
            } else if (value.isNull()) {
                // Let the validator report the blank value instead of failing to deserialize it.
                fields.put(key, JsonNode.createStringNode(""));
            }
        }
        if (!errors.isEmpty()) {
            throw new SkillParseException(errors);
        }
    }

    private static JsonNode coerceValuesToStrings(JsonNode metadata) {
        Map<String, JsonNode> coerced = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : metadata.entries()) {
            JsonNode value = entry.getValue();
            if (value.isValueNode() && !value.isNull() && !value.isString()) {
                value = JsonNode.createStringNode(value.coerceStringValue());
            }
            coerced.put(entry.getKey(), value);
        }
        return JsonNode.createObjectNode(coerced);
    }
}
