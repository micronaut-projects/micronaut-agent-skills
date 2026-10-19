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

/**
 * File and directory names of the skill layout defined by the
 * <a href="https://agentskills.io/specification#directory-structure">Agent Skills specification</a>.
 *
 * @since 0.0.1
 */
public final class SkillFiles {

    /**
     * The required skill file, {@code SKILL.md}, located in the skill directory.
     */
    public static final String SKILL_FILE_NAME = "SKILL.md";

    /**
     * Lowercase variant of the skill file name accepted as a fallback by {@link SkillReader}.
     */
    public static final String LOWERCASE_SKILL_FILE_NAME = "skill.md";

    /**
     * Optional directory holding executable code agents can run.
     */
    public static final String SCRIPTS_DIRECTORY = "scripts";

    /**
     * Optional directory holding additional documentation agents can read on demand.
     */
    public static final String REFERENCES_DIRECTORY = "references";

    /**
     * Optional directory holding static resources such as templates, images and data files.
     */
    public static final String ASSETS_DIRECTORY = "assets";

    /**
     * Delimiter that opens and closes the YAML frontmatter in {@code SKILL.md}.
     */
    public static final String FRONTMATTER_DELIMITER = "---";

    private SkillFiles() {
    }
}
