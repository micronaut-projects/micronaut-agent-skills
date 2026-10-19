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
package io.micronaut.agentskills.cli;

import io.micronaut.agentskills.SkillValidator;
import io.micronaut.configuration.picocli.PicocliRunner;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Command line tool that validates every Agent Skill found in a directory.
 *
 * <p>Each direct sub-directory of the given directory is treated as a skill and validated with
 * {@link SkillValidator}. Invalid skills are listed together with their errors and the process exits with
 * {@link #EXIT_INVALID} when at least one skill is invalid.</p>
 *
 * @since 0.0.1
 */
@Command(name = "micronaut-agent-skills-cli",
    description = "Validates every Agent Skill found in a directory against the Agent Skills specification.",
    mixinStandardHelpOptions = true)
public class MicronautAgentSkillsCliCommand implements Callable<Integer> {

    /**
     * Exit code when every skill is valid.
     */
    public static final int EXIT_VALID = 0;

    /**
     * Exit code when at least one skill is invalid.
     */
    public static final int EXIT_INVALID = 1;

    /**
     * Exit code when the given path is not a directory or cannot be read.
     */
    public static final int EXIT_USAGE = 2;

    @Parameters(index = "0", paramLabel = "<directory>",
        description = "Directory containing one sub-directory per skill, each with a SKILL.md file.")
    Path directory;

    @Option(names = {"-v", "--verbose"}, description = "Also list the skills that are valid.")
    boolean verbose;

    @Spec
    CommandSpec spec;

    private final SkillValidator skillValidator;

    /**
     * Creates the command.
     *
     * @param skillValidator The skill validator.
     */
    @Inject
    public MicronautAgentSkillsCliCommand(SkillValidator skillValidator) {
        this.skillValidator = skillValidator;
    }

    /**
     * Entry point.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        Integer exitCode = PicocliRunner.call(MicronautAgentSkillsCliCommand.class, args);
        System.exit(exitCode == null ? EXIT_USAGE : exitCode);
    }

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        PrintWriter err = spec.commandLine().getErr();
        if (!Files.isDirectory(directory)) {
            err.println("Not a directory: " + directory);
            err.flush();
            return EXIT_USAGE;
        }
        List<Path> skillDirectories;
        try (Stream<Path> children = Files.list(directory)) {
            skillDirectories = children.filter(Files::isDirectory).sorted().toList();
        } catch (IOException e) {
            err.println("Cannot read directory " + directory + ": " + e.getMessage());
            err.flush();
            return EXIT_USAGE;
        }
        if (skillDirectories.isEmpty()) {
            out.println("No skills found in " + directory);
            out.flush();
            return EXIT_VALID;
        }
        int invalid = 0;
        for (Path skillDirectory : skillDirectories) {
            List<String> errors = skillValidator.validate(skillDirectory);
            String name = skillDirectory.getFileName().toString();
            if (errors.isEmpty()) {
                if (verbose) {
                    out.println(name + ": valid");
                }
            } else {
                invalid++;
                out.println(name + ": invalid");
                for (String error : errors) {
                    out.println("  - " + error);
                }
            }
        }
        int total = skillDirectories.size();
        out.println(total + " skill" + (total == 1 ? "" : "s") + " checked, " + (total - invalid) + " valid, " + invalid + " invalid");
        out.flush();
        return invalid == 0 ? EXIT_VALID : EXIT_INVALID;
    }
}
