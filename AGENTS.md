# Repository Guidance

This repository is the Micronaut Agent Skills module (`io.micronaut.agentskills`). It was generated from
`micronaut-projects/micronaut-project-template`. Keep root guidance short and update it when the module layout changes.

## Repository Shape

- `micronaut-agent-skills/` publishes `io.micronaut.agentskills:micronaut-agent-skills`.
- `micronaut-agent-skills-bom/` publishes `io.micronaut.agentskills:micronaut-agent-skills-bom`. Keep dependency-management changes separate from module implementation changes when possible.
- `buildSrc/src/main/kotlin/io.micronaut.build.internal.agent-skills-*.gradle.kts` contains the Kotlin DSL precompiled convention plugins applied by the modules. All build scripts use the Gradle Kotlin DSL (`*.gradle.kts`).
- `gradle/libs.versions.toml` is the version catalog. Only `managed-` libraries and `boms-` entries appear in the published BOM.
- `.agents/skills/` is shared agent guidance synced from the template repository. Do not edit synced skills here; change them in the template.

## Synced Files

- Several files (workflows, `MAINTAINING.md`, `SECURITY.md`, `LICENSE`, `config/`, Gradle wrapper, `.agents/skills/`) are overwritten by the template's files-sync workflow. Repo-specific changes to them do not survive the next sync unless a `.lock` file sits next to them.
- `CONTRIBUTING.md` and `README.md` are owned by this repository.

## Contributing Guidelines

- Before opening or updating a pull request, read this repository's `CONTRIBUTING.md` and follow every repo-specific PR requirement it names.
- Treat contributor-checklist items as handoff requirements. If a requirement is not applicable, state that explicitly in the PR description or handoff note.
- For UI-visible changes, confirm whether screenshots or other visual evidence are required and include them in the PR description; if screenshots cannot be provided, explain why and describe the verification that was performed.

## Documentation

- User guide sources live in `src/main/docs/guide`, with navigation in `src/main/docs/guide/toc.yml`.
- Build guide output with `./gradlew publishGuide` or `./gradlew pG`; build guide plus Javadocs with `./gradlew docs`.
- There are currently no `doc-examples/` snippets. Prefer runnable snippets if examples are introduced.
- Release-note behavior is maintained through `.github/release.yml`, `.github/workflows/release.yml`, and the release process documented in `MAINTAINING.md`.

## Verification

- Use `./gradlew check` for general validation.
- Use `./gradlew publishGuide` after guide or `toc.yml` changes.
- Use `./gradlew docs` when API docs or release documentation output matters.
