---
slug: remove-tool-base-module
branch: java-code-module
owner: claude
status: done
started: 2026-08-18
---

## Goal

Retire the `tool-base` module. After the package-by-package split, it held
a single enum — `io.spine.tools.OsFamily` — plus its `package-info` and test.

## Outcome

`OsFamily` was **not** moved. Base Libraries already publishes the same enum as
`io.spine.environment.OsFamily` in its `environment` module, so `dart-code` —
the only consumer — now depends on `Base.environment` and imports that.

An earlier attempt promoted the enum into a new `io.spine.os` package of the
`base` module of `base-libraries`, which would have required a Base release and
a `config` bump to reach this repository. That work is superseded and its
uncommitted files in `base-libraries` should be discarded.

Note that the repository is still named `tool-base` — `rootProject.name` in
`settings.gradle.kts` and the `gitHub("tool-base")` publishing destination are
untouched. Only the *module* is gone.

## Changes

- `dart-code` — `ProtocPluginPath` and `PubCache` import
  `io.spine.environment.OsFamily`; the module declares
  `implementation(Base.environment)` and drops `project(":tool-base")`.
- `settings.gradle.kts` — the `"tool-base"` include entry removed.
- `README.md` — the module entry removed.
- The module directory deleted.

## Downstream follow-up (other repos)

- [ ] `config` — `io/spine/dependency/local/ToolBase.kt` still declares
      `const val lib = "$group:tool-base:$version"`. The artifact will no
      longer be published, so the constant and every use of `ToolBase.lib`
      needs removing across the SDK. Not touched here: `config` is a shared
      submodule, and the change affects every consumer repository.

## Log

- 2026-08-18 — removed; verified with `clean build dokkaGenerate
  --no-build-cache`. `spine-environment:2.0.0-SNAPSHOT.440` resolves from the
  remote repository, so no version bump was needed and none was kept.
