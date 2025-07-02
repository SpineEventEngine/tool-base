
# Plan for applying AI guidelines to `tool-base`

This plan outlines the steps to apply the AI guidelines from the `logging` project to the `tool-base` project.

## 1. Update `build.gradle.kts` files

- Remove double empty lines in the following files:
    - `/Users/sanders/Projects/Spine/tool-base/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/config/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/gradle-plugin-api/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/gradle-root-plugin/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/jvm-tool-plugins/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/jvm-tools/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/plugin-base/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/plugin-testlib/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/psi/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/psi-java/build.gradle.kts`
    - `/Users/sanders/Projects/Spine/tool-base/tool-base/build.gradle.kts`

## 2. Update Kotlin source files

- Remove double empty lines in the following files:
    - `/Users/sanders/Projects/Spine/tool-base/buildSrc/src/main/kotlin/io/spine/gradle/dart/DartEnvironment.kt`
    - `/Users/sanders/Projects/Spine/tool-base/buildSrc/src/main/kotlin/io/spine/gradle/javascript/JsEnvironment.kt`
    - `/Users/sanders/Projects/Spine/tool-base/config/buildSrc/src/main/kotlin/io/spine/gradle/dart/DartEnvironment.kt`
    - `/Users/sanders/Projects/Spine/tool-base/config/buildSrc/src/main/kotlin/io/spine/gradle/javascript/JsEnvironment.kt`
    - `/Users/sanders/Projects/Spine/tool-base/plugin-base/src/main/kotlin/io/spine/tools/gradle/protobuf/ProtobufDependencies.kt`
    - `/Users/sanders/Projects/Spine/tool-base/psi-java/src/main/kotlin/io/spine/tools/psi/java/Environment.kt`
    - `/Users/sanders/Projects/Spine/tool-base/psi-java/src/main/kotlin/io/spine/tools/psi/java/Parser.kt`
    - `/Users/sanders/Projects/Spine/tool-base/tool-base/src/main/kotlin/io/spine/tools/java/ClasspathExts.kt`
