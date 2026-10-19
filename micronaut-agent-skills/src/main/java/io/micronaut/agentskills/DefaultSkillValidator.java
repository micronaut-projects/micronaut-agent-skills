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

import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link SkillValidator} that combines {@link SkillReader} with Jakarta Bean Validation.
 *
 * @since 0.0.1
 */
@Singleton
public class DefaultSkillValidator implements SkillValidator {

    private final SkillReader skillReader;
    private final Validator validator;

    /**
     * Creates the validator.
     *
     * @param skillReader The skill reader.
     * @param validator   The bean validator.
     */
    public DefaultSkillValidator(SkillReader skillReader, Validator validator) {
        this.skillReader = skillReader;
        this.validator = validator;
    }

    @Override
    public List<String> validate(Path skillDirectory) {
        if (!Files.exists(skillDirectory)) {
            return List.of("Path does not exist: " + skillDirectory);
        }
        if (!Files.isDirectory(skillDirectory)) {
            return List.of("Not a directory: " + skillDirectory);
        }
        if (skillReader.findSkillFile(skillDirectory).isEmpty()) {
            return List.of("Missing required file: " + SkillFiles.SKILL_FILE_NAME);
        }
        Skill skill;
        try {
            skill = skillReader.read(skillDirectory);
        } catch (SkillParseException e) {
            return e.getErrors();
        }
        Path fileName = skillDirectory.toAbsolutePath().normalize().getFileName();
        return validate(skill, fileName == null ? null : fileName.toString());
    }

    @Override
    public List<String> validate(Skill skill, @Nullable String directoryName) {
        List<String> errors = new ArrayList<>();
        validator.validate(skill).stream()
            .map(DefaultSkillValidator::describe)
            .sorted()
            .forEach(errors::add);
        if (directoryName != null) {
            String name = skill.frontmatter().name();
            String normalizedName = Normalizer.normalize(name.strip(), Normalizer.Form.NFKC);
            String normalizedDirectoryName = Normalizer.normalize(directoryName, Normalizer.Form.NFKC);
            if (!normalizedDirectoryName.equals(normalizedName)) {
                errors.add("Directory name '" + directoryName + "' must match skill name '" + name + "'");
            }
        }
        return errors;
    }

    private static String describe(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
