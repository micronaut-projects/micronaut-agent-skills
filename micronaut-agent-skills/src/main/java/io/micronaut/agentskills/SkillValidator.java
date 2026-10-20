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

import io.micronaut.context.annotation.DefaultImplementation;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * Validates skills against the <a href="https://agentskills.io/specification">Agent Skills specification</a>.
 *
 * <p>Mirrors the {@code skills-ref validate} reference command: every problem found is reported as a
 * human-readable message and an empty result means the skill is valid.</p>
 *
 * @since 0.0.1
 */
@DefaultImplementation(DefaultSkillValidator.class)
public interface SkillValidator {

    /**
     * Validates a skill directory: the path must be an existing directory containing a skill file that parses,
     * satisfies every field constraint and whose name matches the directory name.
     *
     * @param skillDirectory The skill directory.
     * @return The validation errors. Empty when the skill is valid.
     */
    List<String> validate(Path skillDirectory);

    /**
     * Validates an already parsed skill.
     *
     * @param skill         The skill.
     * @param directoryName The name of the directory holding the skill, or {@code null} to skip the directory name
     *                      check.
     * @return The validation errors. Empty when the skill is valid.
     */
    List<String> validate(Skill skill, @Nullable String directoryName);
}
