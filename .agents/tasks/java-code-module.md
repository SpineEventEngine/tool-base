---
slug: java-code-module
branch: java-code-module
owner: claude
status: in-progress
started: 2026-08-18
---

## Goal

Java-specific code stops being spread across two modules that mix concerns.
A new `java-code` module (`io.spine.tools:java-code`) collects every
Java-specific piece from `tool-base` (`io.spine.tools.java`, `.java.fs`,
`.java.javadoc`) and from `classic-codegen` (`.java.code`: the
`Classpath`/`JavaClassName` proto pair, `Names`, `Method`). `classic-codegen`
is reduced to what its name implies — thin wrappers over JavaPoet and Roaster.

Resulting layering:

    tool-base        language-neutral: io.spine.tools.{fs,code,proto,archive,…}
        ↑
    java-code        Java specialisation: io.spine.tools.java, .java.code,
                     .java.fs, .java.javadoc
        ↑
    classic-codegen  library wrappers only: .java.code.poet, .java.code.roaster

Success: a consumer needing `Classpath` or `DefaultJavaPaths` no longer pulls
JavaPoet and Roaster; no package is owned by two artifacts; package names of
moved code are unchanged, so downstream repos need a dependency addition
rather than import edits.

## Context

Analysis (2026-08-18) of `classic-codegen` usage across all sibling repos
found a single live consumer, `core-jvm-compiler`, using roughly half the
published API; `Names`, `JavaClassName` and the `Classpath` extensions are
unreferenced anywhere outside this repo. The `io.spine.tools.java.*` packages
in `tool-base`, by contrast, are used heavily — `reference` alone appears 28
times in `core-jvm-compiler` — and also by `compiler`.

`java-code` depends on `tool-base` because `io.spine.tools.java.fs`
specialises the neutral `io.spine.tools.fs` abstractions. Verified acyclic:
no `tool-base` main source outside the moved packages references them, except
two KDoc links in `KotlinReflectExts.kt`.

Owner's decision: the JavaPoet coupling of `Method` — the
`Method(MethodSpec)` convenience constructor — becomes a Kotlin function in
`PoetExts.kt`, which moves to the `io.spine.tools.java.code.poet` package.

Full plan: `.claude/plans/fluttering-squishing-russell.md`.

## Plan

- [x] Register the module: `settings.gradle.kts`, `java-code/build.gradle.kts`,
      module comment in `build.gradle.kts`.
- [x] Move from `classic-codegen`: `java.proto`, `Names.java`, `Method.java`,
      `package-info.java`, `ClasspathExts.kt`, and the tests
      `ClasspathExtsSpec`, `JavaClassNameSpec`, `NamesTest`, `MethodSpec`.
- [x] Move from `tool-base`: `io.spine.tools.java` (`ClassExts`,
      `JavaLangExts`), `.java.fs` (`DefaultJavaPaths`, `FileName`,
      `SourceFile`, `FsTypesExts`, `package-info`), `.java.javadoc`
      (`JavadocEscaper`, `JavadocText`, `package-info`), the eight tests under
      `io/spine/tools/java/`, plus `io/spine/tools/fs/ProjectPathsSpec.kt` and
      `io/spine/tools/proto/fs/DirectorySpec.kt` (both instantiate
      `DefaultJavaPaths`; moving them avoids a test-scope dependency back on
      `java-code`).
- [x] Relocate the wrappers: `PoetExts.kt` → `.java.code.poet` (gaining
      `MethodSpec.toMethod()`), `RoasterExts.kt` → `.java.code.roaster`, with
      their specs. Drop the `Method(MethodSpec)` constructor and the now-wrong
      `@VisibleForTesting` on `Method(String)`.
- [x] Update build scripts and in-repo consumers: strip the proto plumbing
      from `classic-codegen` and point it at `java-code`; add `java-code` to
      `protobuf-setup-plugins` (main), `gradle-plugin-api` (test) and
      `psi-java` (test); de-link the KDoc in `KotlinReflectExts.kt`.
- [x] Verify: per-module builds, full `build`, `dokkaGenerate`, package
      ownership, classpath separation, dependency reports, version gate,
      pre-PR flow.

## Downstream follow-ups (other repos)

- [ ] `config` — add `const val javaCode = "$group:java-code:$version"` to
      `io/spine/dependency/local/ToolBase.kt`.
- [ ] `compiler` — add `ToolBase.javaCode` where it relies on `ToolBase.lib`
      for `isJavaLang`, `isRepeatable`, `reference`. Imports unchanged.
- [ ] `core-jvm-compiler` — add `ToolBase.javaCode`; and update the Poet and
      Roaster imports in `signal/…/rejection/{Javadoc,Methods,
      RThrowableBuilderCode,RThrowableCode}.kt` and `RejectionJavadocIgTest.kt`
      to `io.spine.tools.java.code.{poet,roaster}`.

Frozen repos need no action: `mc-java`, `ProtoData`, `javadoc-tools`, `mc-js`.

## Log

- 2026-08-18 — planned and approved; execution started. Branch renamed to
  `java-code-module`; the closed `intellij-platform-bom` decision record and its
  memories ride along on this branch.
- 2026-08-18 — two test fixtures had to follow the moved code, neither visible
  from the source tree alone:
  - `SourceFileSpec` compiles against the generated Protobuf types of
    `tool-base`'s `testFixtures` source set (`io.spine.test.code.*`,
    `io.spine.tools.type.ProjectServiceGrpc`), which `MoreKnownTypesTest` still
    uses as well. `java-code` therefore takes
    `testImplementation(testFixtures(project(":tool-base")))` plus the gRPC test
    artifacts and the `Grpc.forceArtifacts` resolution strategy, rather than the
    fixtures being split.
  - `JavaLangExtsSpec` needs the hand-written `given.annotation.Schedule`
    repeatable annotation, which lives in `tool-base`'s `test` source set and is
    thus unshareable. Nothing else references it, so the whole
    `given/annotation` package moved to `java-code`.
- 2026-08-18 — verified. `:java-code:build`, `:classic-codegen:build`,
  `:tool-base:build` and the full `build` pass; `dokkaGenerate` reports no
  unresolved links, confirming the de-linked KDoc in `KotlinReflectExts.kt`.
  Artifacts split exactly as designed: `classic-codegen` now holds two classes
  (`poet/PoetExtsKt`, `roaster/RoasterExtsKt`), `java-code` holds the four
  packages with one `package-info` each, and the two JARs share no class.
  `java-code`'s compile classpath carries neither JavaPoet nor Roaster.
  `dependencies.md` regenerated: 15 module sections, `java-code` included.
  All 36 file moves recorded by Git as renames.

  One trap worth remembering: the first artifact check showed the generated
  proto classes still inside the `classic-codegen` JAR. The cause was stale
  local residue, not the source tree — `module.gradle.kts` registers
  `$projectDir/generated` as a source directory independently of
  the `protobuf` plugin, so the pre-move output in `classic-codegen/generated`
  (gitignored, absent from a fresh clone) kept being compiled in.
  `:classic-codegen:clean` removed it. Verify artifact contents after a clean,
  never after an incremental build.
- 2026-08-18 — pre-PR flow deferred at the owner's request; more work planned
  on this branch before the PR. Nothing committed yet; no `pre-pr.ok` sentinel
  written. The whole change set is staged-but-uncommitted in the working tree.
