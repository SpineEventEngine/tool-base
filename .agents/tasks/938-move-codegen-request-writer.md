---
slug: 938-move-codegen-request-writer
branch: claude/busy-dirac-wwflbm
owner: claude
status: in-progress
started: 2026-06-10
---

## Goal

`CodeGeneratorRequestWriter` lives in the `tool-base` module under
`io.spine.tools.code.proto`, with its spec, and the build is green.
This is the receiving half of
[base-libraries#938](https://github.com/SpineEventEngine/base-libraries/issues/938).

## Context

- The class is protoc-plugin tooling; its only consumers are the protoc-plugin
  entry points of the Compiler and ProtoTap. Both already depend on ToolBase.
- Repackaged from `io.spine.code.proto` to `io.spine.tools.code.proto` to avoid
  a split package across artifacts (see the issue discussion).
- `replaceExtension` is still consumed from `io.spine.io` (`base`); it moves to
  ToolBase later under base-libraries#939.
- The removal half happens in `base-libraries` on the same-named branch.

## Plan

- [x] Add `CodeGeneratorRequestWriter.kt` to
  `tool-base/src/main/kotlin/io/spine/tools/code/proto/`.
- [x] Add `CodeGeneratorRequestWriterSpec.kt` (with a local `constructRequest`
  helper) to `tool-base/src/test/kotlin/io/spine/tools/code/proto/`.
- [x] Bump version `2.0.0-SNAPSHOT.398` -> `2.0.0-SNAPSHOT.399`.
- [ ] `./gradlew build` green; commit regenerated dependency reports if any.
  - Blocked in the sandbox: all Spine artifact repositories return 403 for
    the buildscript dependency `io.spine.tools:protobuf-setup-plugins`, so
    no Gradle build can run here at all. Verification is delegated to PR CI.
  - Local mitigations: the moved sources are unchanged from `base` (which
    built and passed tests) except the package line, and every consumed API
    (`replaceExtension`, `decodeBase64`, `toBase64Encoded`,
    `extensionRegistry`, `KClass.parse`, `toJson`) was verified present in
    `base` at the pinned version `2.0.0-SNAPSHOT.404`.
- [x] Push and open a draft PR.

## Log

- 2026-06-10 — drafted; executing autonomously per issue #938.
- 2026-06-10 — code added and version bumped; sandbox cannot resolve Spine
  snapshot artifacts (403 on all repos), so the build runs on PR CI instead.
