---
slug: plain-jar-plugins
branch: plain-jar-plugins
owner: claude
status: in-review
started: 2026-10-30
---

## Goal

`protobuf-setup-plugins` and `jvm-tool-plugins` are published as plain JARs
whose POMs and Gradle module metadata declare their dependencies, instead of
uber JARs carrying unrelocated copies of ToolBase modules, Base, Logging,
Guava, Gson, and the Protobuf Gradle plugin (issue #200).

## Context

- In `2.0.0-SNAPSHOT.430`, `protobuf-setup-plugins` is 5.4 MB with 3,396
  classes, and `jvm-tool-plugins` is 10.5 MB with 3,382 classes. The
  `pluginMaven` JAR of each module is byte-identical to its `-all` (`fatJar`) JAR.
- The embedded copies of `plugin-base` and others shadow the versions resolved
  for a build, which is how `core-jvm-compiler#124` failed with
  `NoSuchMethodError` on `Projects.javaCompileTasksFor()`.
- Relocation is not an option: the public API of `protobuf-setup-plugins` takes
  and extends `code`, `plugin-base`, Base, and Protobuf Gradle plugin types.
- The uber JARs were meant to spare Plugin Portal users a Spine Maven
  repository. That premise does not hold:
  - no ToolBase plugin has been published to the Portal, because all versions
    are snapshots and `publishPlugins` is disabled for them;
  - Spine artifacts are not on Maven Central;
  - `root-gradle-plugins` and `compiler-gradle-plugin` already declare ToolBase
    modules as plain dependencies.
- `jvm-tool-plugins-all` is consumed through `ToolBase.jvmToolPlugins` and
  `ToolBase.jvmToolPluginDogfooding` of `config`. The plain `jvm-tool-plugins`
  artifact exists for every published version, so `config` can switch to it
  first. It must switch before `ToolBase.version` reaches the version of this
  change, which publishes no `-all` artifacts.
- `protobuf-setup-plugins-all` has no consumers.
- This supersedes `unbundle-protobuf-runtime`: with plain JARs, the Protobuf
  runtime is an ordinary dependency, so `unbundleProtobufRuntime()` goes.

## Plan

- [x] `protobuf-setup-plugins`: `module` instead of `uber-jar-module`; no
      `fatJar` rename or `unbundleProtobufRuntime()`; read `SpinePublishing`
      from the root project, since `module` creates no local extension; the
      types in the public API (`code`, `plugin-base`, Base, Protobuf Gradle
      plugin) become `api`.
- [x] `jvm-tool-plugins`: the same; its public API exposes only `jvm-tools`,
      which is `api` already; drop the unused Kotlin Gradle plugin API.
- [x] Drop the plugins and settings that `module` already applies.
- [x] Bump the version: `2.0.0-SNAPSHOT.440`, breaking, as it removes the
      `-all` artifacts.
- [x] Verify: `build`; the JARs hold only own classes; the POMs and `.module`
      files declare the dependencies; no `-all` publications; the public API
      references only `api` dependencies.
- [ ] `config` PR: `ToolBase.jvmToolPlugins` and `jvmToolPluginDogfooding`
      point to `jvm-tool-plugins`.
- [ ] ToolBase PR.

## Log

- 2026-10-30 — investigated; plan drafted.
- 2026-10-30 — implemented; `build` green. In the version published to Maven
  Local, `protobuf-setup-plugins` is 47 KB with 35 classes, `jvm-tool-plugins`
  is 30 KB with 18 classes, both `bundling = external`, no `-all` artifacts.
- 2026-10-30 — `javap -protected`: the public and protected signatures of
  `protobuf-setup-plugins` reference Protobuf Gradle plugin, Base, `code`,
  `plugin-base`, and `jvm-tools` types, all reachable via `api`. Those of
  `jvm-tool-plugins` reference only Gradle and JDK types.
- 2026-10-30 — dogfooded: with the plain JARs from Maven Local on the root
  `buildscript` classpath (temporary edit, reverted), `fixtures`, `java-code`,
  `proto-code`, and `protobuf-setup-plugins` build from clean with
  `--no-build-cache`.
- 2026-10-30 — pre-PR review: version reclassified as breaking (precedent: #188,
  `.404 → .410`); the unused Kotlin Gradle plugin API removed from the metadata
  of `jvm-tool-plugins`; redundant plugin applications dropped. Logging looks
  unused in `protobuf-setup-plugins`, but `javac` needs it: `FileDescriptorSuperset`
  of `proto-code` is `WithLogging`, and `proto-code` keeps Logging `implementation`.
