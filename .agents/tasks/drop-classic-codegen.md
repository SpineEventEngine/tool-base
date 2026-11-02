---
slug: drop-classic-codegen
branch: drop-classic-codegen
owner: claude
status: in-progress
started: 2026-10-30
---

## Goal

`tool-base` no longer has the `classic-codegen` module, and the Spine SDK no
longer depends on JavaPoet or Roaster. Code that parsed Java with Roaster uses
PSI instead. Code that generated Java with JavaPoet uses the Spine Compiler's
Java code model (`io.spine.tools.compiler.jvm`) and PSI, as most
`core-jvm-compiler` generators already do.

## Context

Analysis of every sibling repo's `origin/master` (2026-10-30 for Roaster,
2026-11-02 for JavaPoet):

- **tool-base:** only `classic-codegen` used either library. After the
  Roaster removal it holds just `poet/PoetExts.kt` (`methodSpec`, `classSpec`,
  `constructorSpec`, two `codeBlock` overloads, `MethodSpec.toMethod()`) and
  its spec. No other module depends on it.
- **core-jvm-compiler:** the only code consumer. All 8 files that import
  JavaPoet are production code, and all serve rejection-throwable generation:
  - `base/.../field/{FieldType,MapFieldType,RepeatedFieldType,SingularFieldType}.java`
    use JavaPoet only as a type model (`TypeName`, `ClassName`,
    `ParameterizedTypeName`). `FieldType.name()` leaks `TypeName` only to
    `RThrowableBuilderCode`.
  - `signal/.../rejection/{RThrowableCode,RThrowableBuilderCode,Javadoc,RThrowableRenderer}.kt`
    (~690 lines) generate code with `TypeSpec`/`MethodSpec`/`JavaFile` and are
    the only users of the `classic-codegen` helpers.
- **time:** lists `toolBase.classicCodegen` only for version forcing.
- **validation:** `java-bundle` excludes JavaPoet and `Roaster.group`.
- **ProtoTap:** Roaster `force` entries and fat-JAR exclusions only.
- **config:** owns `ToolBase.classicCodegen` and the `JavaPoet` and `Roaster`
  dependency objects.
- **compiler, core-jvm and the other repos:** no use of either library.

Owner's decisions:
- Remove Roaster entirely and use PSI wherever it parsed Java; never declare
  Roaster directly as a fallback.
- Change consumers first, then the provider. Deleting the module here breaks
  nothing at once, but a consumer can't move to a `tool-base` release without
  `classic-codegen` until it stops depending on it.

How the work is split: the session that owns this checkout coordinates. Work
in other repos, and parallel work here, runs in separate sessions with their
own worktrees. Their branches are merged into `drop-classic-codegen` only with
the owner's approval. Never edit the copies of repos in a cascade workspace.

## Plan

### Phase 1 — Roaster

- [x] Analyze Roaster use across the SDK.
- [ ] core-jvm-compiler: PSI instead of Roaster in the `signal` and
      `annotation` tests; exclude `org.jboss.forge.roaster` from
      `classicCodegen`; drop the `org/jboss/forge/` and JNA fat-JAR entries.
      SpineEventEngine/core-jvm-compiler#126 (open).
- [x] tool-base: remove `RoasterExts` and `api(Roaster.*)` from
      `classic-codegen` (`ba6b78c5`, reports in `7af04c4b`). Shipped on its
      own as SpineEventEngine/tool-base#202 (merged, `2.0.0-SNAPSHOT.441`);
      `master` is merged into this branch (`1fbcea2f`).
- [x] ProtoTap: remove Roaster `force` entries and fat-JAR exclusions
      (SpineEventEngine/ProtoTap#25, merged).
- [ ] validation: drop the `Roaster.group` exclusion in `java-bundle` once
      it uses `tool-base` `.441` or later.

### Phase 2 — JavaPoet in core-jvm-compiler

Stacked on #126 (branch `claude/drop-roaster-968ba6`), which changes the same
build lines and rejection tests. The PR targets that branch, and GitHub moves
it to `master` once #126 merges. It bumps to `2.0.0-SNAPSHOT.104`, since #126
already takes `.103`.

- [ ] `base`: move the `FieldType` family to the Compiler's `JavaTypeName`,
      `ClassName`, `ParameterizedTypeName`; adjust `RThrowableBuilderCode`.
- [ ] `signal`: rewrite rejection-throwable generation without JavaPoet, using
      the Compiler's code model (`CodeBlock`, `MethodDeclaration`,
      `FieldDeclaration`, renderers) and PSI (`createMethodFromText`).
  - Generated rejections are API that users compile against. Beyond the
    existing tests, generate rejections from the test protos before and after
    and diff them; review every difference.
- [ ] Build cleanup:
  - remove `ToolBase.classicCodegen` from `base`, `signal` and `routing`
    (unused there);
  - remove the version-forcing entries in the root `build.gradle.kts` and
    `tests/build.gradle.kts`;
  - remove `implementation(JavaPoet.lib)` from `tests/custom-annotations` and
    `tests/factories` (no file there uses JavaPoet);
  - in `compiler-plugins`, remove the `pomDependency(ToolBase.classicCodegen, …)`
    entry and the `"com/squareup/javapoet/"` allowed package. Keep `kotlinpoet`.
  - Check: no `com/squareup/javapoet/` in the shadow JAR;
    `verifyBundledPackages` passes.

### Phase 3 — tool-base

- [x] Delete the module: `git rm -r classic-codegen`, its `settings.gradle.kts`
      entry, and any mention in docs; regenerate the dependency reports.
      No docs mentioned it; the reports drop its section and `javapoet`.
- [ ] Bump the version once for the branch (`version-bumped`), run `pre-pr`,
      and open the PR from `drop-classic-codegen`.
  - Bumped to `2.0.0-SNAPSHOT.450`: removing a published artifact is
    breaking, as with #201 (`.431` → `.440`), and #205 holds `.443`.

### Phase 4 — after the tool-base release

- [ ] core-jvm-compiler: upgrade to the release (only after Phase 2).
- [ ] time: remove `toolBase.classicCodegen` from the version-forcing lists in
      `build.gradle.kts` and `tests/build.gradle.kts`.
- [ ] validation: remove the JavaPoet exclusion from `java-bundle`.
- [ ] config: retire `ToolBase.classicCodegen` and the `JavaPoet` and `Roaster`
      dependency objects, following config's deprecation conventions. The
      exclusions added by #126 reference `Roaster.group`, so they go first.

## Log

- 2026-10-30 — Branch cut from `master` after `./config/pull` (`5e1b824c`);
  Roaster analysis done.
- 2026-11-02 — #126 opened; Roaster removed from `classic-codegen` and merged
  into this branch; ProtoTap cleanup under way.
- 2026-11-02 — JavaPoet analysis done; Phases 2–4 drafted, awaiting approval.
- 2026-11-02 — The Roaster removal went to `master` as #202, bumped to `.441`.
- 2026-11-02 — Phase 2 approved, in its own session, stacked on #126.
- 2026-11-02 — #202 and #204 merged (`.442`). ProtoTap#25 merged. The config
  `Roaster` deprecation is under way; the only references left are
  core-jvm-compiler's `compiler-plugins` exclusion and validation's
  `java-bundle` exclusion.
- 2026-11-02 — `Roaster` deprecated in config: SpineEventEngine/config#829
  (open). Phase 2 work has #126's latest commits merged in, and its build is
  rerunning.
- 2026-11-02 — `master` (`.442`) merged into this branch (`1fbcea2f`). Its
  tree now differs from `master` only by this file; Phase 3 is unblocked.
- 2026-11-02 — Phase 3: module deleted, bumped to `.450`; `./gradlew build`
  passes.
