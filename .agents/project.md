# Project: tool-base

## Overview

`tool-base` provides the foundational infrastructure and shared components for development tools and Gradle plugins within the Spine SDK. It simplifies the creation of build-time tools by offering common utilities for file manipulation, Protobuf reflection, code generation, and IntelliJ PSI integration.

## Architecture

This repository serves as a library and a set of Gradle plugins. It defines the core API for other Spine tools, such as the root `spine` and `spineSettings` extensions for Gradle.

Key patterns and components:
- **Multi-module Gradle project**: Orchestrates various tool-related functionalities.
- **Gradle API extensions**: Modules like `gradle-root-plugin` and `plugin-base` provide abstractions and root extensions for Spine-specific Gradle DSL.
- **PSI Utilities**: `psi` and `psi-java` provide Kotlin-friendly wrappers for IntelliJ Platform PSI.
- **Fat JARs**: `intellij-platform` modules produce shadowed fat JARs for IntelliJ components.
- **Public API boundaries**: Defined primarily in `gradle-plugin-api` and `tool-base`.

Constraints:
- Maintain compatibility with JVM 17 and Kotlin 2.3.20.

Read [`.agents/jvm-project.md`](jvm-project.md) for build stack, coding style, tests, and versioning.
