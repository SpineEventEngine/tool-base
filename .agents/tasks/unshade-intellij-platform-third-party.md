---
slug: unshade-intellij-platform-third-party
branch: unshade-intellij-platform-third-party
owner: claude
status: in-progress
started: 2026-08-12
---

## Goal

The `intellij-platform` uber JAR stops bundling unrelocated third-party classes,
so that it can no longer shadow genuine artifacts on a consumer's build
classpath. Third-party components that exist on Maven Central become normal
dependencies of the published artifact; JetBrains forks that cannot be, are
relocated.

## Context — the incident that surfaced this

In `delivery-server`, `./gradlew :delivery-server-cloud-run:jibDockerBuild`
failed with:

    NoSuchMethodError:
        'void org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
             .putArchiveEntry(TarArchiveEntry)'

The Jib Gradle plugin 3.4.4 compiles against `commons-compress:1.26.0`, where
that overload exists. `io.spine.tools:intellij-platform` — reaching the root
buildscript classpath transitively via `compiler-gradle-plugin` → `psi-java` →
`psi` — bundles `org.apache.commons.compress.**` from **1.21**, unrelocated.
The root buildscript classloader is a parent of every subproject's plugin
classloader, and parent-first delegation served the 1.21 copy to Jib.

Diagnosis required loading the class from the buildscript classloader and
printing its code source; `./gradlew buildEnvironment` is *misleading* here —
it reports a clean, resolvable `commons-compress:1.26.0` because the shaded
copy is not a dependency at all, it is bytecode inside another artifact.
For the same reason no version constraint, BOM, or `force(...)` can fix it.

The consumer-side workaround (delivery-server PR #62) declares the genuine
`commons-compress` **first** on the root buildscript classpath so it wins by
ordering. It works, but classpath order is load-bearing and every consumer
repo needs to know the trick. This task removes the cause.

## Analysis

### How the third-party code gets in

`intellij-platform/build.gradle.kts` applies `uber-jar-module`, whose
`shadowJar` shades the **entire resolved runtime classpath** of the declared
`com.jetbrains.intellij.platform:{core,util,core-impl,code-style}` (v213.7172.53)
dependencies. The `exclude(...)` list filters resource *directories* (icons,
templates, `kotlin/**`, …) but no *artifacts* — and there is no `relocate(...)`.
The third-party code is not vendored by JetBrains inside their jars; it arrives
as ordinary Maven dependencies declared in their POMs, and Shadow swallows it
all. The published `fatJar` publication is `artifact(tasks.shadowJar)` with no
component, so the POM declares **zero** dependencies: 28 MB, 575 packages,
opaque to dependency resolution.

### What is bundled (measured from `2.0.0-SNAPSHOT.404`)

Genuine Maven Central artifacts, bundled unrelocated:

| Bundled component                                                                        | Version (per IJ 213 POMs) | Packages                                           |
|------------------------------------------------------------------------------------------|---------------------------|----------------------------------------------------|
| `com.google.guava:guava` (+`failureaccess`)                                              | 31.0.1-jre                | `com.google.common.**`, `com.google.thirdparty.**` |
| `org.apache.commons:commons-compress`                                                    | 1.21                      | `org.apache.commons.compress.**`                   |
| `com.github.ben-manes.caffeine:caffeine`                                                 | (transitive)              | `com.github.benmanes.caffeine.**`                  |
| `io.netty` (buffer, util — partial)                                                      | (transitive)              | `io.netty.buffer.**`, `io.netty.util.**`           |
| `com.fasterxml:aalto-xml`, `org.codehaus:stax2`                                          | 1.3.0                     | `com.fasterxml.aalto.**`, `org.codehaus.stax2.**`  |
| `oro:oro`                                                                                | 2.0.8                     | `org.apache.oro.**`                                |
| `org.lz4:lz4-pure-java`                                                                  | 1.8.0                     | `net.jpountz.**`                                   |
| `dk.brics:automaton`                                                                     | 1.12-1                    | `dk.brics.automaton.**`                            |
| `org.jspecify`, `net.jcip`, `org.jetbrains:annotations`                                  | —                         | annotations                                        |
| Batik + xmlgraphics, `org.imgscalr`, `net.n3.nanoxml`, `it.unimi.dsi` (fastutil classes) | —                         | various                                            |

JetBrains **forks** (published only in the JetBrains `intellij-dependencies`
repository, *not* Central), also unrelocated:

| Fork artifact                                  | Claims package        |
|------------------------------------------------|-----------------------|
| `org.jetbrains.intellij.deps:jdom:2.0.6`       | `org.jdom.**`         |
| `org.jetbrains.intellij.deps:log4j:1.2.17.2`   | `org.apache.log4j.**` |
| `org.jetbrains.intellij.deps:trove4j`          | `gnu.trove.**`        |
| `org.jetbrains.intellij.deps.jna:jna-platform` | `com.sun.jna.**`      |
| `org.jetbrains.intellij.deps.fastutil`         | `it.unimi.dsi.**`     |

### The two IJ fat JARs are a layered pair, not independent bundles

`intellij-platform-java/build.gradle.kts` (`tasks.shadowJar`, `doFirst` block)
computes its exclusions **from the actual contents of the `intellij-platform`
JAR**: it visits every path in the sibling archive and excludes exactly those
paths from its own shade. Consequences for this task:

- Removing an entry from the `intellij-platform` shade silently *adds* it to
  the `intellij-platform-java` shade on the next build (measured: excluding
  JNA from the platform JAR resurfaced all 1477 `com/sun/jna` entries in the
  java JAR). Exclusions of shared transitives must live in the shared
  `uber-jar-module` list, and the eventual artifact include-filters must be
  applied to **both** modules consistently.
- `intellij-platform-java` additionally bundles its own unrelocated
  third-party code not present in the platform JAR, measured in
  `2.0.0-SNAPSHOT.410`: `com/pty4j/**` (83 entries,
  `org.jetbrains.pty4j:pty4j` exists on Central) and `com/jediterm/**`
  (187 entries, `org.jetbrains.jediterm` exists on Central) — terminal
  machinery, unused by headless PSI, candidates for the same treatment as
  the rest of the Central-artifact bucket.

### Measured collision surface in one consumer

Intersecting the bundle with `delivery-server`'s root buildscript classpath
(96 artifacts): **6 artifacts / 58 packages** overlap — `commons-compress`
(35 pkgs, broke), `guava` (18 pkgs), `caffeine`, `jspecify`, `failureaccess`,
`j2objc-annotations`. Today the genuine Guava 33.6.0 and Caffeine 3.2.4 win
only because resolution *order* happens to place them before the uber JAR —
order is not a contract, and the Guava skew (31.0.1 vs 33.6.0) is two-plus
major versions. `org.apache.log4j` is a latent trap of the same kind: the
log4j2 `log4j-1.2-api` bridge claims the identical package.

## Recommended fix (hybrid)

1. **Central artifacts → normal dependencies.** In `uber-jar-module` (or the
   `intellij-platform` build script), filter the shade to JetBrains platform
   code only, e.g.:

       tasks.shadowJar {
           dependencies {
               include(dependency("com.jetbrains.intellij.platform:.*"))
               include(dependency("org.jetbrains.intellij.deps.*:.*"))
           }
       }

   and declare the excluded Central artifacts (`guava`, `commons-compress`,
   `caffeine`, `aalto-xml`, `oro`, `lz4-pure-java`, `automaton`, annotations, …)
   as `runtime` dependencies in the `fatJar` publication's POM (via
   `pom.withXml` or a proper component). Pin the versions the IJ POMs declare;
   Gradle conflict resolution then upgrades them in consumers — which is
   exactly what fixes the Jib case: 1.26.0 wins *by resolution*, not by
   classpath order, and the 1.26 API is backward-compatible with 1.21.

2. **JetBrains forks → relocate.** `org.jdom`, `org.apache.log4j`,
   `gnu.trove`, fastutil classes stay in the shade (declaring
   them as POM deps would force every consumer to add the JetBrains repo), but
   get `relocate("org.jdom", "io.spine.tools.ij.jdom")` etc. These are forks —
   no Central artifact is interchangeable with them, so relocation is the only
   way to stop them claiming public package names. Keep IntelliJ's own
   `com.intellij.**` / `org.jetbrains.**` unrelocated: platform code resolves
   its classes reflectively from string names, and Spine's `psi` / `psi-java`
   reference them directly.

   **Exception — JNA cannot be relocated.** The `jna`/`jna-platform` fork
   bundles native `libjnidispatch` binaries whose JNI entry points encode the
   Java class names literally (`Java_com_sun_jna_Native_...`); relocating
   `com.sun.jna` breaks the binding with `UnsatisfiedLinkError`. JNA is
   instead *excluded* from the shade (done, see Log). This is safe: JNA is
   IntelliJ's OS-integration layer, guarded behind `JnaLoader` and never
   exercised by headless PSI code — the same category as the pty4j/winp/
   `bin/**` exclusions already in place. Because the platform classes stay
   unrelocated, genuine `net.java.dev.jna:jna:5.9.0` from Central is a
   drop-in if a consumer ever needs the code path (the fork's `5.9.0.26` is
   upstream 5.9.0 + JetBrains build number). If step 1's POM-dependency
   machinery lands, JNA can join the declared `runtime` deps instead.

### Alternatives considered

- **Relocate everything** (Guava included): hermetic, but rewrites far more
  bytecode, risks breaking IntelliJ's reflective service loading, and if any
  exposed IJ API signature mentions a Guava type, the relocated type leaks
  into Spine's API. The hybrid keeps relocation confined to the forks.
- **Thin JAR + JetBrains repositories**: no shading at all; consumers add
  `intellij-dependencies` / `intellij-repository` to every build. That is the
  configuration burden the uber JAR exists to avoid — rejected.
- **Do nothing; document the consumer workaround**: classpath-order pinning
  must then be replicated (and never "tidied" away) in every consumer, and it
  cannot protect two colliding *bundled* copies. Rejected.

### Caveats to verify during implementation

- [ ] `psi` / `psi-java` public API does not leak types from the newly
      relocated fork packages.
- [ ] IntelliJ 213 code does not reflectively load fork classes by
      string FQN (grep the platform sources for `"org.jdom`, `"org.apache.log4j`).
- [ ] The Guava the consumers resolve (33.x) keeps IJ 213 working — IJ uses a
      narrow, stable Guava surface, but run the `psi`-dependent test suites.
- [ ] License report / notices still cover the remaining bundled forks.

## Verification

- [ ] `tool-base`: full build; inspect the new JAR — no `com/google/common`,
      `org/apache/commons/compress`, unrelocated `org/jdom` etc. entries;
      published POM lists the Central artifacts as `runtime` deps.
- [ ] Consumer proof: in `delivery-server`, drop the `CommonsCompress`
      classpath-ordering workaround (`build.gradle.kts`, buildscript block) and
      the `buildSrc/.../CommonsCompress.kt` declaration, bump the tool
      versions, and confirm `:delivery-server-cloud-run:jibDockerBuild`
      succeeds *without* it.

## Log

- 2026-08-12 — drafted from the `delivery-server` incident analysis
  (PR SpineEventEngine/delivery-server#62, workaround commit `96d489d5`).
- 2026-08-12 — excluded `com/sun/jna/**` from the `intellij-platform` shade
  (`intellij-platform/build.gradle.kts`). Measured in `2.0.0-SNAPSHOT.404`:
  1384 classes + 25 native `jnidispatch` binaries (~3.9 MB) from
  `org.jetbrains.intellij.deps.jna:jna{,-platform}:5.9.0.26`, entering as
  `runtime` deps of `com.jetbrains.intellij.platform:util`. Verified
  `intellij-platform-java` bundles no `com/sun/jna` entries (0) — no change
  needed there. `com/sun/jna/` is the only `com/sun/*` subtree in the JAR,
  so the glob cannot over-match.
- 2026-08-12 (later) — the "no change needed there" conclusion above was
  wrong: the java JAR was JNA-free only because of the subtraction mechanism
  (see Analysis). Moved the `com/sun/jna/**` exclusion into the shared
  `uber-jar-module` list, deleted the verbatim ~120-line duplicate of
  `excludeFiles()` (and the redundant `tasks.shadowJar` block) from
  `intellij-platform/build.gradle.kts`, and fixed the dead
  `resources/com/pti4j/**` typo → `resources/com/pty4j/**` (now actually
  excludes the pty4j native binaries from the java JAR). Verified:
  platform / jvm-tool-plugins / protobuf-setup-plugins JAR entry listings
  identical before vs after; java JAR delta is exactly −1477 `com/sun/jna`
  entries and −33 `resources/` tree entries (31 pty4j natives + 2 emptied
  parent dirs), nothing added; 119.1 → 115.1 MB.
