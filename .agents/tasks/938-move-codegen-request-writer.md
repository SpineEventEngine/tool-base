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

- [ ] Add `CodeGeneratorRequestWriter.kt` to
  `tool-base/src/main/kotlin/io/spine/tools/code/proto/`.
- [ ] Add `CodeGeneratorRequestWriterSpec.kt` (with a local `constructRequest`
  helper) to `tool-base/src/test/kotlin/io/spine/tools/code/proto/`.
- [ ] Bump version `2.0.0-SNAPSHOT.398` -> `2.0.0-SNAPSHOT.399`.
- [ ] `./gradlew build` green; commit regenerated dependency reports if any.
- [ ] Push and open a draft PR.

## Log

- 2026-06-10 — drafted; executing autonomously per issue #938.
