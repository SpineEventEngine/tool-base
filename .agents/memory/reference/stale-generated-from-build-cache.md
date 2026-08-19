---
name: stale-generated-from-build-cache
description: After renaming a proto package, the Gradle build cache restores the old generated sources — verify artifacts with --no-build-cache.
metadata:
  type: reference
  since: 2026-08-18
---

`gradle.properties` sets `org.gradle.caching=true`. After a `.proto` file's
`package` / `java_package` is renamed, a normal build regenerates the **new**
package *and* restores the **old** one from the build cache, so the published
JAR carries both. The generated files still cite the pre-rename path in their
`// source:` header even though no such file exists on disk.

Confirmed 2026-08-18 while renaming the `tool-base` test fixtures from
`io.spine.tools.type` to `io.spine.test.tools.type`: a verified-empty
`generated/` directory refilled with both packages on the next `build`;
the same build with `--no-build-cache` produced only the new one.

**Why:** the task that copies protoc output into `<module>/generated` has
under-declared inputs, so its cache key does not change when the set of
generated file names changes, and a cache hit restores the stale file set.

**How to apply:** when a change renames or deletes generated sources, verify
artifact contents with `--no-build-cache`; a plain `clean build` is not
enough. Note also that `./gradlew clean build` in one invocation is unreliable
here — `org.gradle.parallel=true` does not order `clean` before the
generators, so run `clean` as its own invocation. Related:
[[proportional-machinery]].
