---
slug: split-tool-base-core
branch: java-code-module
owner: claude
status: done
started: 2026-08-18
---

## Goal

Finish the split of `tool-base` by extracting its last three packages into
modules of their own:

- `kotlin-code` ← `io.spine.tools.kotlin`
- `code` ← `io.spine.tools.code`
- `archive` ← `io.spine.tools.archive`

`tool-base` is left holding `io.spine.tools.OsFamily` and its `package-info`
alone — four classes, plus the `testFixtures` source set that several modules
still share.

## Context

The extraction was cheap because the owner's commit `c9a9cad3` ("Use
title-case utility from Base Libraries") had just replaced
`io.spine.tools.titlecaseFirstChar` with `io.spine.string.titleCase` in
`SourceSetName` and `SourceSetBasedName`. That removed the last edge from
`io.spine.tools.code` back to the `io.spine.tools` root package, so `code`
depends on Base alone.

Scopes were chosen from where the types actually appear:

| Module | Declares | Why |
|---|---|---|
| `plugin-base` | `api(":code")` | `SourceSetName` is in the signatures of `JavaConfigurationName` and `SourceSet.named` |
| `protobuf-setup-plugins` | `implementation(":code")` | follows the module's own convention |
| `jvm-tool-plugins` | `implementation(":code")` | `ArtifactMetaPlugin` uses `SourceSetName` internally |
| `dart-code` | `implementation(":code")`, `implementation(":tool-base")` | `Element` is implemented by a package-private class; `OsFamily` is read inside method bodies |
| `proto-code` | `implementation(":archive")` | replaces its `tool-base` dependency outright |
| `java-code` | `testImplementation(":code")` | `ClassExtsSpec` uses `Line` |

Two dependencies were dropped as dead: `plugin-base` on `tool-base` (it used
nothing but `io.spine.tools.code`) and `tool-base` on `Logging` (nothing logs
there any more).

## Log

- 2026-08-18 — moved, wired and verified with `clean build dokkaGenerate
  --no-build-cache`.

  `:code:dokkaGenerateModuleHtml` failed on the first run: Dokka is configured
  to fail on warnings, and `SourceSetBasedName` still carried a KDoc link to
  `[titlecassed][String.titlecaseFirstChar]` — a symbol `c9a9cad3` had
  removed. The link was already dead inside `tool-base`; splitting the package
  into a module of its own is what made Dokka notice. Repointed at
  `[title-cased][String.titleCase]`, the utility the code actually calls.
