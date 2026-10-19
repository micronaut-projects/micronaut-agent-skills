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

import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads {@code SKILL.md} files into {@link Skill} instances.
 *
 * <p>Reading only checks the structure of the file: frontmatter delimiters, valid YAML, known keys and the
 * presence of the required keys. Field constraints and the directory name rule are checked by
 * {@link SkillValidator}.</p>
 *
 * @since 0.0.1
 */
@DefaultImplementation(DefaultSkillReader.class)
public interface SkillReader {

    /**
     * Locates the skill file inside a skill directory. {@value SkillFiles#SKILL_FILE_NAME} is preferred and
     * {@value SkillFiles#LOWERCASE_SKILL_FILE_NAME} is accepted as a fallback.
     *
     * @param skillDirectory The skill directory.
     * @return The skill file, or empty if the directory contains none.
     */
    Optional<Path> findSkillFile(Path skillDirectory);

    /**
     * Reads the skill file of a skill directory.
     *
     * @param skillDirectory The skill directory.
     * @return The parsed skill.
     * @throws SkillParseException          If the skill file is missing or cannot be parsed.
     * @throws java.io.UncheckedIOException If the skill file cannot be read.
     */
    Skill read(Path skillDirectory);

    /**
     * Parses the contents of a skill file.
     *
     * @param content The raw contents of a {@code SKILL.md} file.
     * @return The parsed skill.
     * @throws SkillParseException If the content cannot be parsed.
     */
    Skill parse(String content);
}
