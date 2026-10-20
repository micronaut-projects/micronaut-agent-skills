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

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Thrown when a {@code SKILL.md} file cannot be parsed: missing or malformed frontmatter, invalid YAML,
 * unknown or missing frontmatter keys.
 *
 * @since 0.0.1
 */
public class SkillParseException extends RuntimeException {

    private final List<String> errors;

    /**
     * Creates an exception with a single error.
     *
     * @param message The error message.
     */
    public SkillParseException(String message) {
        this(message, null);
    }

    /**
     * Creates an exception with a single error and a cause.
     *
     * @param message The error message.
     * @param cause   The cause, if any.
     */
    public SkillParseException(String message, @Nullable Throwable cause) {
        super(message, cause);
        this.errors = List.of(message);
    }

    /**
     * Creates an exception reporting several errors at once.
     *
     * @param errors The error messages. Must not be empty.
     */
    public SkillParseException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    /**
     * Returns the individual error messages.
     *
     * @return An unmodifiable, non-empty list of error messages.
     */
    public List<String> getErrors() {
        return errors;
    }
}
