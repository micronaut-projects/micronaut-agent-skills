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

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Contents of a {@code SKILL.md} file: the YAML frontmatter and the Markdown instructions that follow it.
 *
 * @param frontmatter The parsed frontmatter.
 * @param body        The Markdown instructions after the frontmatter, with surrounding whitespace removed.
 *                    The specification places no restriction on its format.
 * @since 0.0.1
 */
@Introspected
public record Skill(@NotNull @Valid SkillFrontmatter frontmatter, @NotNull String body) {
}
