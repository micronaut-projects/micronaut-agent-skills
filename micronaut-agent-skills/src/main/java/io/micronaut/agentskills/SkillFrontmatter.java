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

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.KebabCaseStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * YAML frontmatter of a {@code SKILL.md} file as defined by the
 * <a href="https://agentskills.io/specification">Agent Skills specification</a>.
 *
 * <p>Property names are serialized in kebab case, so {@link #allowedTools()} maps to the
 * {@code allowed-tools} key. Optional properties that are {@code null} or empty are omitted
 * when serialized with Micronaut Serialization's default {@code NON_EMPTY} inclusion.</p>
 *
 * <p>The record only carries the per-field constraints of the specification. The rules that
 * involve the surrounding directory (the name must match the parent directory name) and the
 * rejection of unknown frontmatter keys are enforced by {@link SkillReader} and
 * {@link SkillValidator}.</p>
 *
 * @param name          Required. Skill name: 1 to {@value #NAME_MAX_LENGTH} characters, lowercase letters, digits
 *                      and hyphens only, no leading, trailing or consecutive hyphens.
 * @param description   Required. What the skill does and when to use it: 1 to {@value #DESCRIPTION_MAX_LENGTH}
 *                      characters.
 * @param license       Optional. License name or reference to a bundled license file.
 * @param compatibility Optional. Environment requirements: 1 to {@value #COMPATIBILITY_MAX_LENGTH} characters when
 *                      present.
 * @param metadata      Optional. Arbitrary string-to-string mapping for client-specific properties.
 * @param allowedTools  Optional and experimental. Space-separated string of pre-approved tools. Serialized as
 *                      {@code allowed-tools}.
 * @since 0.0.1
 */
@Serdeable(naming = KebabCaseStrategy.class)
public record SkillFrontmatter(
    @NotBlank @Size(max = NAME_MAX_LENGTH) @Pattern(regexp = NAME_REGEX) String name,
    @NotBlank @Size(max = DESCRIPTION_MAX_LENGTH) String description,
    @Nullable String license,
    @Nullable @Size(min = 1, max = COMPATIBILITY_MAX_LENGTH) String compatibility,
    @Nullable Map<@NotBlank String, @NotNull String> metadata,
    @Nullable String allowedTools) {

    /**
     * Maximum length of {@link #name()}.
     */
    public static final int NAME_MAX_LENGTH = 64;

    /**
     * Maximum length of {@link #description()}.
     */
    public static final int DESCRIPTION_MAX_LENGTH = 1024;

    /**
     * Maximum length of {@link #compatibility()}.
     */
    public static final int COMPATIBILITY_MAX_LENGTH = 500;

    /**
     * Regular expression a {@link #name()} must match: one or more segments of lowercase letters and digits
     * separated by single hyphens. It rejects uppercase characters, leading, trailing and consecutive hyphens.
     */
    public static final String NAME_REGEX = "^[\\p{Ll}\\p{Nd}]+(?:-[\\p{Ll}\\p{Nd}]+)*$";

    /**
     * Frontmatter key of {@link #name()}.
     */
    public static final String NAME_KEY = "name";

    /**
     * Frontmatter key of {@link #description()}.
     */
    public static final String DESCRIPTION_KEY = "description";

    /**
     * Frontmatter key of {@link #license()}.
     */
    public static final String LICENSE_KEY = "license";

    /**
     * Frontmatter key of {@link #compatibility()}.
     */
    public static final String COMPATIBILITY_KEY = "compatibility";

    /**
     * Frontmatter key of {@link #metadata()}.
     */
    public static final String METADATA_KEY = "metadata";

    /**
     * Frontmatter key of {@link #allowedTools()}.
     */
    public static final String ALLOWED_TOOLS_KEY = "allowed-tools";

    /**
     * Every frontmatter key defined by the specification. Any other key is rejected by {@link SkillReader}.
     */
    public static final Set<String> KEYS = Set.of(
        NAME_KEY, DESCRIPTION_KEY, LICENSE_KEY, COMPATIBILITY_KEY, METADATA_KEY, ALLOWED_TOOLS_KEY);

    /**
     * Frontmatter keys that must be present.
     */
    public static final Set<String> REQUIRED_KEYS = Set.of(NAME_KEY, DESCRIPTION_KEY);
}
