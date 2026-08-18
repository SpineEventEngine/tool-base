---
slug: fs-module
branch: java-code-module
owner: claude
status: in-progress
started: 2026-08-18
---

## Goal

Extract `io.spine.tools.fs` into its own `fs` module, and free the language
modules (`java-code`, `js-code`, `dart-code`) from depending on the whole of
`tool-base` for file system types.

## Context

Extracting `io.spine.tools.fs` naively is impossible: a three-package cycle
runs across the cut, invisible while everything sits in one module but fatal
as a circular Gradle project dependency.

    io.spine.tools.fs     ──▶ io.spine.tools.code.SourceSetName
                                (Generated.java, SourceRoot.java)
    io.spine.tools.code   ──▶ io.spine.tools.titlecaseFirstChar
                                (SourceSetName.kt, SourceSetBasedName.kt)
    io.spine.tools (root) ──▶ io.spine.tools.fs.DirectoryName
                                (StandardTypesExts.kt)

A fourth, one-way edge is fine and stays: `io.spine.tools.proto.fs.Directory
extends SourceCodeDirectory`, so `tool-base` depends on `fs`.

Owner's decision (2026-08-18): sever the cycle at its source rather than drag
`io.spine.tools.code` into `fs` or split that package across two artifacts.
`Generated.dir` and `SourceRoot.subDir` now take a plain `String` source-set
name instead of `SourceSetName`. The cost is the lost type safety on those two
methods and a downstream ripple; the gain is that `fs` depends on nothing but
Base, and the cut is a genuine layering rather than a re-parcelling.

## Plan

- [x] Sever `fs → code`: `Generated.dir(String)`, `SourceRoot.subDir(String,
      String)`; update the `DefaultJavaPaths` / `DefaultJsPaths` overrides and
      the six in-repo test call sites (which now pass `"main"` and no longer
      import `SourceSetName` at all).
- [x] Create `fs` (`io.spine.tools:fs`) and move the package — 17 main and
      9 test files. It depends on `Base` alone.
- [x] Repoint dependencies: `tool-base` → `fs`; `java-code` and `js-code`
      → `fs` **instead of** `tool-base`; `dart-code` → `fs` **and**
      `tool-base`.
- [ ] Verify: `clean build dokkaGenerate`, artifact/package ownership,
      dependency reports; commit when green.

## Outcome of the question "can the `xx-code` modules depend only on `fs`?"

- `java-code` — **yes.** Its only non-`fs` need was `SourceSetName`, removed by
  the API change.
- `js-code` — **yes**, for the same reason.
- `dart-code` — **no.** `ImportStatement` is an `io.spine.tools.code.Element`,
  and `ProtocPluginPath` / `PubCache` branch on `io.spine.tools.OsFamily`.
  Neither type is file-system related, so neither belongs in `fs`. Freeing
  `dart-code` would mean moving `Element` and `OsFamily` somewhere shared —
  a separate decision, not attempted here.

## Downstream follow-ups (other repos)

- [ ] `config` — add an `fs` constant to
      `io/spine/dependency/local/ToolBase.kt` (alongside `javaCode`, `jsCode`,
      `dartCode`).
- [ ] `mc-js` — **breaking**: 8 call sites pass `SourceSetName` to
      `generated().dir(...)` and must pass a `String`
      (`ResolveImports.java:58`, `GenerateJsonParsers.java:96`,
      `GivenProject.java:65`, `CreateParsersTest.java:76`,
      `GenerateIndexFileTest.java:59`, `CodeGenStepTest.java:64,85,114`).
- [ ] `compiler`, `core-jvm-compiler` — no call sites of the changed methods;
      they need only the new module coordinates.

## Log

- 2026-08-18 — cycle found and severed; module extracted.
