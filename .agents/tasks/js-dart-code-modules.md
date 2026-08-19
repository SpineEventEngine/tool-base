---
slug: js-dart-code-modules
branch: java-code-module
owner: claude
status: in-progress
started: 2026-08-18
---

## Goal

`tool-base` stops carrying language-specific code. Following the `java-code`
extraction, two more modules take the remaining language packages:

- `js-code` (`io.spine.tools:js-code`) — `io.spine.tools.js.code`,
  `io.spine.tools.js.fs`;
- `dart-code` (`io.spine.tools:dart-code`) — `io.spine.tools.dart`,
  `io.spine.tools.dart.fs`.

`tool-base` is left language-neutral: `io.spine.tools.{fs,code,proto,archive,
io,kotlin,type,…}`.

## Context

Both packages specialize the neutral `io.spine.tools.fs` abstractions
(`DefaultPaths`, `AbstractDirectory`, `SourceDir`, `FileReference`,
`ExternalModules`, `FileWithImports`) and `io.spine.tools.code`
(`SourceSetName`, `Element`), so each new module depends on `tool-base` — the
same intrinsic edge as `java-code`. `dart-code` additionally needs `Logging`,
since `ImportStatement` is `WithLogging`.

Verified before the move:

- no cycle — no `tool-base` source outside those packages references them,
  in main or test;
- no in-repo consumers in other modules;
- the moved tests need no generated Protobuf fixtures and no `given.*`
  fixtures, unlike `java-code`'s — they use only Base, `tool-base`, TestLib
  and Guava;
- neither package carries `.proto` files, so neither module needs the
  Protobuf plugins.

Cross-repo, only `mc-js` (2026-06-04) consumes `io.spine.tools.js.*`
(`TypeName` ×12, `fs.FileName` ×11, `JsFiles` ×6, `DefaultJsPaths` ×4,
`FieldName` ×4, `MethodReference` ×4, `LibraryFile.INDEX` ×2). **Nothing
anywhere consumes `io.spine.tools.dart.*`.** Package names are unchanged, so
consumers need a dependency addition rather than import edits.

## Plan

- [x] Register both modules: `settings.gradle.kts`, build scripts, `README.md`.
- [x] Move `io.spine.tools.js` (`code`, `fs`) — 16 files — into `js-code`.
- [x] Move `io.spine.tools.dart` (root, `fs`) — 16 files — into `dart-code`.
- [ ] Verify: `clean build dokkaGenerate`, artifact/package ownership,
      dependency reports.
- [ ] Commit when green.

## Downstream follow-ups (other repos)

- [ ] `config` — add `jsCode` and `dartCode` constants to
      `io/spine/dependency/local/ToolBase.kt`.
- [ ] `mc-js` — add `ToolBase.jsCode`; imports unchanged.

## Log

- 2026-08-18 — created after the `java-code` extraction, applying the same
  layering. Both modules built green on the first attempt; unlike `java-code`,
  neither needed a test-fixture migration.
