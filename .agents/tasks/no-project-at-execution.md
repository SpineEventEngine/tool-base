---
slug: no-project-at-execution
branch: no-project-at-execution
owner: claude
status: in-review
started: 2026-10-07
---

## Goal
`GeneratedSourcePlugin` no longer calls `Task.project` while `generateProto` runs, so
Gradle 9 stops reporting "Invocation of Task.project at execution time has been
deprecated", and the copy of the generated sources behaves as before.

## Context
- Observed in `testlib`'s `generateTestProto` (`protobuf-setup-plugins`
  2.0.0-SNAPSHOT.423, Gradle 9.7.1).
- The `doLast` action calls `project.copy { }` and resolves
  `$projectDir/generated/<sourceSet>` through `project`, both at execution time.

## Plan
- [x] Add a regression spec that fails while the deprecation is reported.
- [x] Copy through an injected `FileSystemOperations`; resolve the target directory
      at configuration time.
- [x] Bump the version (no commit): `2.0.0-SNAPSHOT.424`.
- [x] Run the module tests and `build`.

## Log
- 2026-10-07 — drafted and started.
- 2026-10-07 — new spec fails on `master` with the deprecation, passes with the fix.
  Ad-hoc `--configuration-cache` run: fails on `master` ("invocation of 'Task.project'
  at execution time is unsupported"), stores the cache entry with the fix.
- 2026-10-07 — spec switched to `--configuration-cache` per review (independent of the
  deprecation wording); `InjectedFileSystem` made `private` (verified Gradle decorates it).
  Reuse of the cache entry verified ad hoc. `./gradlew build dokkaGenerate` green;
  module: 26 tests, 0 failures. Left uncommitted per the user's request.
