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
repository, *not* Central), also unrelocated. Measured from the resolved
runtime graphs of both modules (the original table missed that Batik here is
the JetBrains fork group, and the `intellij-platform-java` graph adds more):

| Fork artifact                                    | Claims package                 | Treatment            |
|--------------------------------------------------|--------------------------------|----------------------|
| `org.jetbrains.intellij.deps:jdom:2.0.6`         | `org.jdom.**`                  | relocated            |
| `org.jetbrains.intellij.deps:log4j:1.2.17.2`     | `org.apache.log4j.**`          | relocated            |
| `org.jetbrains.intellij.deps:trove4j`            | `gnu.trove.**`                 | relocated            |
| `org.jetbrains.intellij.deps.fastutil`           | `it.unimi.dsi.**`              | relocated            |
| `org.jetbrains.intellij.deps.batik:batik-*` (15) | `org.apache.batik.**`          | relocated            |
| `org.jetbrains.intellij.deps:ion-java`           | `com.amazon.ion.**`            | relocated            |
| `org.jetbrains.intellij.deps:commons-imaging`    | `org.apache.commons.imaging.**`| relocated            |
| `org.jetbrains.intellij.deps.jna:jna{,-platform}`| `com.sun.jna.**`               | excluded (JNI names) |
| `org.jetbrains.intellij.deps.jcef:jcef`          | `org.cef.**`                   | shaded as is (JNI)   |
| `org.jetbrains.intellij.deps:asm-all`            | `org.jetbrains.org.objectweb`  | shaded as is         |
| `org.jetbrains.intellij.deps:jb-jdi`, `sa-jdwp`  | `com.jetbrains.{jdi,sa}`       | shaded as is         |
| `org.jetbrains.intellij.deps:java-compatibility` | `com.intellij.util.ui`         | shaded as is         |
| coverage/test-discovery/memory agents            | `com.intellij.*`, `org.jetbrains.*` | shaded as is    |
| `org.jetbrains.intellij:blockmap` (not on Central)| `com.jetbrains.plugin.blockmap`| shaded as is        |

`com.jetbrains.intellij.platform:ide-impl` additionally *vendors* two
JetBrains-authored classes inside the `io.netty.buffer` package
(`ByteBufUtf8Writer`, `ByteBufUtilEx`). They have no counterpart in genuine
Netty, so they are additive, not colliding; artifact-level filtering cannot
remove them, and they stay.

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

## Implemented shape (2026-08-12)

The hybrid fix below is implemented by
`buildSrc/src/main/kotlin/io/spine/gradle/shade/IntelliJUberJar.kt` — a single
policy object used by both IJ modules, because the layered pair demands
identical treatment:

- **Include filter**: only groups `com.jetbrains.intellij(.*)` and
  `org.jetbrains.intellij(.*)` enter the shade
  (`ShadowJar.shadeOnlyJetBrainsArtifacts()`).
- **Relocations** (see the fork table above) under the
  `io.spine.tools.ij` prefix.
- **POM generation** (`MavenPublication.declareUnshadedDependencies()`):
  every resolved runtime artifact outside the shaded and dropped groups
  becomes a `runtime` dependency; sibling uber modules arriving as project
  dependencies are declared too (`intellij-platform-java` → `intellij-platform`).
  Versions are pinned **as resolved in this repo** — i.e. after the Spine
  version forcing (e.g. Guava `33.6.0-jre`, not IJ's requested `31.0.1-jre`)
  — which is exactly the combination the repo's own test suites run against.
  `commons-compress` stays at IJ's `1.21` and upgrades in consumers by
  resolution, which is the designed fix for the Jib incident.
- **Dropped groups** (neither shaded nor declared; users add genuine
  artifacts explicitly if ever needed): `org.jetbrains.kotlin(x)` (provided,
  as before), `org.jetbrains.pty4j`, `org.jetbrains.jediterm` (terminal),
  `org.jvnet.winp` (Windows process management; its DLLs were excluded
  from the old shade anyway).
- **Central audit**: every POM candidate of both modules verified against
  `repo1.maven.org` — 85/86 present; the miss (`org.jetbrains.intellij:blockmap`)
  is shaded instead (self-namespaced package). `marketplace-zip-signer`'s
  Central POM does not reference `blockmap`, so declaring it is safe.
- **Subtraction vs Shadow transforms**: the `intellij-platform-java`
  subtraction filter sees pre-transform source paths while the sibling JAR
  stores post-transform ones. `IntelliJUberJar.sourceFormOf()` reverse-maps
  all three transforms (relocated paths, relocated `META-INF/services` file
  names, `.shadow.kotlin_module` renames); without it the fork classes and
  metadata duplicate into the java JAR.

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

- [x] `psi` / `psi-java` public API does not leak types from the newly
      relocated fork packages — no Spine source in this repo imports
      `org.jdom`, `gnu.trove`, `org.apache.log4j`, `it.unimi`,
      `org.apache.oro`, `net.jpountz`, or `dk.brics` (grepped 2026-08-12).
- [x] IntelliJ 213 code does not reflectively load fork classes by string
      FQN — Shadow rewrites class-name string constants under the relocated
      prefixes in all shaded bytecode; `psi` / `psi-java` suites pass against
      the relocated JARs (148 tests). Residual risk: FQNs assembled by
      concatenation at runtime; none surfaced in tests.
- [x] The Guava the consumers resolve (33.x) keeps IJ 213 working — the POM
      pins the repo-resolved `33.6.0-jre`, and this repo's suites have always
      run IJ 213 against the forced 33.x by construction.
- [x] License report / notices still cover the remaining bundled forks —
      the reports enumerate configuration dependencies, which the shade
      filter does not alter; the full `build` (incl. report regeneration)
      passes. The regenerated `docs/dependencies/*` changes are version-bump
      catch-up only.

## Verification

- [x] `tool-base`: full build passes (241 tasks); both JARs inspected — no
      `com/google/common`, `org/apache/commons/compress`, unrelocated
      `org/jdom` etc. entries; generated POMs list the Central artifacts as
      `runtime` deps (platform: 20, java: 86 incl. the sibling JAR).
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
- 2026-08-12 (main fix) — implemented the hybrid plan (see "Implemented
  shape"). New `IntelliJUberJar.kt` policy in `buildSrc`; both IJ module
  scripts apply the include filter, relocations, and POM generation; dead
  pty4j / purejavacomm / winp path excludes removed from `uber-jar-module`.
  Measured results:
  - `intellij-platform`: 25.8 → **17.4 MB**; POM declares **20** runtime
    deps (incl. `commons-compress:1.21`, `guava:33.6.0-jre`); zero
    third-party packages left; relocated trees `io/spine/tools/ij/{jdom
    (234), log4j (326), trove (457), unimi/dsi (2090), batik (2065)}`.
  - `intellij-platform-java`: 115.1 → **78.6 MB**; POM declares **86**
    runtime deps incl. `io.spine.tools:intellij-platform`; relocated
    `io/spine/tools/ij/{ion (420), imaging (471)}`; jackson/httpclient/
    xstream/icu4j/gson/… all unbundled into the POM.
  - Cross-JAR duplicate files: `META-INF/MANIFEST.MF` only (the
    `sourceFormOf` reverse mapping removed 13 duplicated
    `.shadow.kotlin_module` files and 2 relocated batik service files
    discovered during verification).
  - `:psi:test` + `:psi-java:test`: 148 passed, 0 failed.
  - Kotlin block comments nest: a literal `META-INF/*.kotlin_module` glob in
    KDoc broke `buildSrc` compilation ("unclosed comment") until rephrased.
- 2026-08-12 (pre-PR review round) — five reviewers (spine-code-review,
  kotlin-engineer, review-docs, dependency-audit, gradle-review) all
  APPROVE after fixes: added `IntelliJUberJarSpec` (7 tests, buildSrc);
  repaired a dangling KDoc link (`unrelocatedFormOf` → `sourceFormOf`);
  CC-hardening in `intellij-platform-java` (sibling JAR tracked via
  `inputs.file` instead of bare `dependsOn` — the up-to-date/build-cache
  staleness gap is closed; `ArchiveOperations` instead of ambient
  `Project.zipTree`; `pathsToExclude` is a cleared `Set`). JAR entry
  listings verified byte-identical before/after the fixes.
  Measured CC status: `generatePomFileForFatJarPublication` executes under
  `--configuration-cache`, but the entry is discarded — the `withXml`
  action's captured `Configuration` is not serializable (a `Provider` of
  resolution results would be), and the root build script's `gcloud` call
  is another pre-existing blocker. Full CC-readiness is out of scope; the
  repository does not enable the configuration cache.
