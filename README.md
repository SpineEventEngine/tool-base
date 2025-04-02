# tool-base

[![Ubuntu build][ubuntu-build-badge]][gh-actions]
[![codecov][codecov-badge]][codecov] &nbsp;
[![license][license-badge]][license]


Common code for development tools of the Spine SDK.

## Modules

* [`tool-base`](tool-base) — common components for building build-time tools, including file
  manipulations, Protobuf reflection, simple code generation, etc.


* [`plugin-base`](plugin-base) — abstractions for building Gradle plugins.


* [`plugin-testlib`](plugin-testlib) — test fixtures for testing Gradle plugins.


* [`psi`](psi) — utilities and Kotlin extensions for working with language independent
  part of [IntelliJ Platform PSI](https://plugins.jetbrains.com/docs/intellij/psi.html).


* [`psi-java`](psi-java) — utilities and Kotlin extensions for working with
  [Java PSI](https://plugins.jetbrains.com/docs/intellij/psi.html). 
                                                                                    

* [`root`](root) — provides Gradle plugins with the root [`spine`][spine-extension] and
  [`spineSettings`][spine-settings-extension] extensions of a Gradle project.


* [`plugin-api`](plugin-api) — API for libraries that deal with root Gradle extensions or
  act as [Spine Compiler][spine-compiler] plugins.


* `intellij-platform` and `intellij-platform-java` are modules for producing fat
   JARs for corresponding IntelliJ Platform components.  

## Language versions

 * **Java** — [see `BuildSettings.kt`](buildSrc/src/main/kotlin/BuildSettings.kt)


 * **Kotlin** — [see `Kotlin.kt`](buildSrc/src/main/kotlin/io/spine/dependency/lib/Kotlin.kt)

[gh-actions]: https://github.com/SpineEventEngine/tool-base/actions
[ubuntu-build-badge]: https://github.com/SpineEventEngine/tool-base/actions/workflows/build-on-ubuntu.yml/badge.svg
[codecov-badge]: https://codecov.io/gh/SpineEventEngine/tool-base/branch/master/graph/badge.svg
[license-badge]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0
[codecov]: https://codecov.io/gh/SpineEventEngine/tool-base
[spine-compiler]: https://github.com/SpineEventEngine/ProtoData
[spine-extension]: root/src/main/kotlin/io/spine/tools/gradle/root/SpineProjectExtension.kt
[spine-settings-extension]: root/src/main/kotlin/io/spine/tools/gradle/root/SpineSettingsExtension.kt 
