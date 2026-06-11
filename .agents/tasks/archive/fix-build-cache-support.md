---
slug: fix-build-cache-support
branch: claude/busy-dirac-wwflbm
owner: claude
status: in-review
started: 2026-06-10
---

## Goal

Builds of projects using the `io.spine.generated-sources` and
`io.spine.descriptor-set-file` plugins succeed with `org.gradle.caching=true`:
after `./gradlew clean build`, the copied generated code under
`$projectDir/generated/<sourceSet>` and the `desc.ref` file are present even
when `generateProto` is restored from the Gradle build cache.

## Context

With the build cache on, `clean build` fails in the `classic-codegen` module
(reproduced locally): `compileKotlin` reports `Unresolved reference 'Classpath'`
because no generated code is present.

Root cause — same pattern as SpineEventEngine/compiler#67. Both plugins in
`protobuf-setup-plugins` do work in `doLast` of the cacheable
`GenerateProtoTask` without declaring outputs:

- `GeneratedSourcePlugin` copies protoc output into
  `$projectDir/generated/<sourceSet>/` (undeclared — the build-breaker:
  `clean` deletes `generated/`, the task is restored `FROM-CACHE`, the
  `doLast` copy never runs, compilation fails).
- `DescriptorSetFilePlugin` writes `desc.ref` next to the descriptor set file
  (undeclared — missing from resources/JARs after a cache restore).

The descriptor set file itself is already a declared output of
`GenerateProtoTask` (verified for protobuf-gradle-plugin 0.10.0 in
compiler#67), so only `desc.ref` needs declaring.

This repository's own build applies the *published*
`protobuf-setup-plugins:2.0.0-SNAPSHOT.381` (dogfooding via the root
buildscript classpath), so `org.gradle.caching` stays disabled in
`gradle.properties` until a fixed version is published and the dogfooded
`ToolBase.version` is bumped (decision confirmed by the user).

Known limitation (out of scope): in repos where the Spine Compiler launch
task also writes into `generated/<sourceSet>` (e.g. `validation`), the
overlapping outputs degrade cacheability of the codegen tasks; correctness
is preserved because Gradle re-executes instead of restoring.

## Plan

- [x] Diagnose and reproduce on `classic-codegen` (`build` → `clean` →
      `build` with `--build-cache`).
- [x] `GeneratedSourcePlugin`: declare `$projectDir/generated/<sourceSet>`
      as an output of `GenerateProtoTask`.
- [x] `DescriptorSetFilePlugin`: declare `desc.ref`
      (`DescriptorSetReferenceFile.NAME`) as an output.
- [x] `gradle.properties`: keep `org.gradle.caching` commented out; explain
      the dogfooding gate.
- [x] Integration tests in `protobuf-setup-plugins` (TestKit, per existing
      spec style): `build` → `clean` → `build` with the cache ON (assert
      `generateProto` is `FROM_CACHE`, generated sources + `desc.ref`
      restored, compilation of code referencing a generated class succeeds)
      and with the cache OFF (assert re-execution works as before).
- [x] Verify: new tests fail without the fix, pass with it; module build
      green.

## Log

- 2026-06-10 — Investigated; reproduced on `classic-codegen` with the
  dogfooded `.381` plugins; user chose to keep `org.gradle.caching` disabled
  until dogfooding catches up.
- 2026-06-10 — The new integration test exposed a second gap: in a pure-Java
  project `compileJava` does not depend on `generateProto` because
  `configureSourceSetDirs()` severs the dependency carried by the protoc
  output dirs and adds the `generated/` dirs as plain `File`s. (Masked in
  Spine repos by the Kotlin compile dependency from `setupKotlinCompile()`.)
  Fixed by adding the dirs via `project.files(dir).builtBy(task)`.
- 2026-06-10 — Verified: with the output declarations temporarily removed,
  the cache-on test fails and the cache-off test passes; with the fix both
  pass. `:protobuf-setup-plugins:build` and `dokkaGenerate` are green
  (11 test suites, 0 failures).
- 2026-06-10 — Reviewed by `spine-code-review` and `kotlin-engineer` agents:
  both approve. Applied follow-ups: `internal` on `BuildCacheSpec`,
  explicit `this@configureSourceSetDirs` in `builtBy`, fixed stale
  `copyGeneratedFiles` KDoc. Final build: 22 tests, 0 failures.
