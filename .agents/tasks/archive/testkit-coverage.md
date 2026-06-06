# Credit Gradle TestKit worker coverage to Kover

## Problem

Plugin tests that drive the plugin under test through Gradle TestKit's
`GradleRunner` (e.g. `LibrarySettingsPluginSpec`, `SettingsPluginSpec`) execute
that plugin in a **separate Gradle worker JVM**. Kover/JaCoCo instrument only the
test JVM, so the out-of-process execution is not credited — e.g.
`io.spine.tools.gradle.lib.LibrarySettingsPlugin.apply(Settings)` showed 0% line
coverage although it is functionally tested.

`Plugin<Settings>` classes cannot be unit-tested in-process: `org.gradle.testfixtures`
offers only `ProjectBuilder` (for `Project`), with no public `Settings` builder, so
`GradleRunner` is the only way to exercise a settings plugin.

## Approach

Attach the JaCoCo agent to the TestKit worker JVM and merge the resulting
execution data into the Kover report. Spans the shared test harness and build
config, so it affects every Gradle-plugin module and warrants its own review.

1. **buildSrc — `io.spine.gradle.testing.enableTestKitCoverage()`**
   (`TestKitCoverage.kt`). Resolves the standalone JaCoCo agent
   (`org.jacoco:org.jacoco.agent:<Jacoco.version>:runtime`) and passes its path
   plus a per-module exec dir (`build/jacoco-testkit`) to the `test` task as
   system properties. Wipes the exec dir before each run. Applied from
   `gradle-plugin-api` and `gradle-root-plugin` build scripts.

2. **plugin-testlib — `GradleRunner.enableTestKitCoverage()`**
   (`TestKitCoverage.kt`). When the system properties are present, writes a
   `gradle.properties` carrying `org.gradle.jvmargs=-javaagent:…=destfile=…,append=true,…`
   into the TestKit directory (the worker's Gradle user home) and points the
   runner at it. One stable TestKit home per module → one worker daemon →
   `append=true` accumulates all cases into `build/jacoco-testkit/testkit.exec`.
   Wired into both `GradleProject.runner` and the `runGradleBuild()` helper.
   No-op unless the module opted in, so other consumers are unaffected.

   The worker daemon flushes on shutdown ("after the tests complete"), before the
   Kover report task runs — confirmed empirically.

3. **buildSrc — `KoverConfig`**. Adds the per-module `testkit.exec` to the
   `total` report's `additionalBinaryReports`, both per subproject (so
   `:<module>:koverXmlReport` credits it) and at the root rollup. With
   `useJacoco(...)`, Kover loads these `.exec` files through JaCoCo's
   `ExecFileLoader`.

## Verification (JDK 17)

`./gradlew :gradle-plugin-api:koverXmlReport --rerun-tasks`

| Class.method                         | Before | After |
|--------------------------------------|--------|-------|
| `LibrarySettingsPlugin.apply`        | 0/4    | 4/4   |
| `LibrarySettingsPlugin.<init>`       | 0/2    | 2/2   |
| `SettingsPlugin.apply` (root-plugin) | 0/3    | 3/3   |

The root `:koverXmlReport` credits the same classes (no double counting).
`ProjectExtsKt.spineExtension` stays 0/1 — it is a `reified inline` function and
is inherently uncreditable by line coverage; excluded from expectations.

Pre-existing detekt findings in `GradleProjectSetup.kt:140` and `Sources.kt:76`
are unrelated to this change (untouched files).

## Status: done — delete on merge to master.
