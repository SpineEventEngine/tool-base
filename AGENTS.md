# `AGENTS.md`

> Guidance for Large Language Model (LLM) Agents (ChatGPT & Codex) collaborating on this project.

---
## Table of Contents
1. [The purpose of this document](#the-purpose-of-this-document)
2. [Project overview](#project-overview)
3. [Agent roles](#agent-roles)
4. [Coding guidelines](#coding-guidelines)
5. [Running builds](#running-builds)
6. [Incrementing a version](#incrementing-a-patch-version)
7. [Documentation Tasks](#documentation-tasks)
8. [Testing Responsibilities](#testing-responsibilities)
9. [Safety Rules](#safety-rules)
10. [Interaction Tips](#interaction-tips)

## 🧠 The purpose of this document

This file defines how LLM agents (e.g. **ChatGPT** and **Codex**) should
**interact with the codebase**, **generate content**, and **assist in development workflows**
for this project, which is based on **Kotlin** and **Java**.

It outlines conventions, expectations, and usage goals to ensure
**productive, safe, and consistent collaboration**.
                                                            
### Terminology
- **LLM**: Use when referring to the general technology.
- **Agents**: Refers to ChatGPT, Codex, or any LLM.
 - Specific names (**ChatGPT** / **Codex** / **Claude**): Only when functionality diverges.

---

## 🛠️ Project overview

- **Languages**: Kotlin (primary), Java (secondary).
- **Build Tool**: Gradle with Kotlin DSL.
- **Architecture**: Event-driven Domain-Driven Design (DDD).
- **Testing**: JUnit 5
- **Style**: Kotlin idiomatic code preferred over Java-style code
- **Tools Used**: Gradle plugins, IntelliJ IDEA Platform, KSP, KotlinPoet 

---

## 🤖 Agent roles

### 🗨️ ChatGPT

- Explain code, APIs, and architecture
- Refactor or simplify logic
- Help with design decisions and trade-offs
- Generate documentation (KDoc, Markdown)
- Suggest tests and edge cases
- Summarize diffs or changes
- Generate Gradle configurations
- Assist with naming and conceptual clarity

### 🔧 Codex — code completion and code generation

- Complete functions, classes, and tests
- Generate Kotlin idioms (e.g., extension functions, DSLs)
- Follow patterns from nearby code
- Generate test scaffolds and fixtures
- Fill in `when` branches, sealed class hierarchies, etc.

---

## 🧾 Coding guidelines for Agents

### ✅ Preferred

1. Use **idiomatic Kotlin**, including:
    - Extension functions
    - `when` expressions
    - Smart casts
    - Data classes
    - Sealed classes
2. Apply **Java interop** only when needed (e.g., using annotations or legacy libraries)
3. Use **Kotlin DSL** when modifying or generating Gradle files
4. Generate code that **compiles cleanly** and **passes static analysis**
5. Respect **existing architecture**, naming conventions, and project structure
6. Use `@file:JvmName`, `@JvmStatic`, etc., where appropriate.

### ❌ Avoid

- Java-style verbosity (e.g., builders with setters)
- Redundant null checks (`?.let` misuse)
- Using `!!` unless clearly justified
- Mixing Groovy and Kotlin DSLs in build logic
- Overuse of reflection unless requested

### General guidance
- Adhere to the [Spine Event Engine Documentation](https://github.com/SpineEventEngine/documentation/wiki)
  for coding style and contribution procedures. 

- The conventions on the [Spine Event Engine Documentation](https://github.com/SpineEventEngine/documentation/wiki)
  page and other pages in this Wiki area **take precedence over** standard Kotlin or
  Java conventions.
 
- Write clear, incremental commits with descriptive messages.
- Include automated tests for any code change that alters functionality.
- Keep pull requests focused and small.

### Naming convention for variables
- Prefer simple nouns over composite nouns. E.g., `user` is better than `userAccount`.

---
## Incrementing a patch version for each pull request

### We use semver
The version number of the project is kept in the file named `version.gradle.kts` which resides
in the root of the project.

The version numbers in these files follow the conventions of
[Semantic Versioning 2.0.0](https://semver.org/).

### Incrementing the version
When creating a pull request, the version **must** be updated by incrementing
the **last component** of the version number by one.
For example, the version `"2.0.0-SNAPSHOT.42"` should become `"2.0.0-SNAPSHOT.43"`   

If the last component has leading zeroes, keep the padding of zeroes so that the width of
the last component stays the same. For example, the version `"2.0.0-SNAPSHOT.009"` should become
`"2.0.0-SNAPSHOT.010"`.

### What if?
Not incrementing the version will result in **build failure** because we have a GitHub workflow
which checks for the increment.

### Resolving conflicts in `version.gradle.kts`
A branch conflict over the version number should be resolved as described below.
 * If a merged branch has a number which is less than that of the current branch, the version of
   the current branch stays.
 * If the merged branch has the number which is greater or equal to that of the current branch,
   the number should be increased by one.
---

## Running builds

1. When modifying code, run `./gradlew build` before committing.
2. If Protobuf (`.proto`) files are modified **always** run `./gradlew clean build`.
3. Documentation-only changes do not require running tests.

---

## 📁 Project structure expectations

```yaml
<module1>
  src/
  ├── main/
  │ ├── kotlin/ # Kotlin source files
  │ └── java/ # Legacy Java code
  ├── test/
  │ └── kotlin/ # Unit and integration tests
  build.gradle.kts # Kotlin-based build configuration
<module2>
<module3>
build.gradle.kts # Kotlin-based build configuration
settings.gradle.kts # Project structure and settings
README.md # Project overview
AGENTS.md # LLM agent instructions (this file)

```
---

## 📄 Documentation tasks

- Generate and update **KDoc** for `public` and `internal` APIs
- Suggest better **names** and **abstractions**
- Help format inline comments and design rationale
- Generate changelogs based on Git commit summaries

---

## 🧪 Testing responsibilities

- Generate tests for:
    - Public functions
    - Edge cases
    - Extension functions and DSLs

- Suggest:
    - Test coverage improvements
    - Performance testing
    - Property-based testing

### Testing guidelines
 - Do not use mocks, use stubs.
 - Prefer [Kotest assertions](https://kotest.io/docs/assertions/assertions.html) over
   assertions from JUnit or Google Truth.
---

## 🚨 Safety rules for Agents

- Do **not** auto-update external dependencies without explicit request.
- Do **not** inject analytics or telemetry code.
- Flag any usage of unsafe constructs (e.g., reflection, I/O on the main thread).
- Avoid generating blocking calls inside coroutines.

---

## 💬 Interaction tips

- Human programmers may use inline comments to guide agents:
  ```kotlin
    // ChatGPT: Suggest a refactor for better readability.
    // Codex: Complete the missing branches in this `when` block.
    // ChatGPT: explain this logic.
    // Codex: complete this function.
   ```
- Agents, should ensure pull request messages are concise and descriptive:
  ```text
  feat(chatgpt): suggested DSL refactoring for query handlers  
  fix(codex): completed missing case in sealed class hierarchy
  ```
- Encourage `// TODO:` or `// FIXME:` comments to be clarified by ChatGPT.

- When agents or humans add TODO comments, they **must** follow the format described on
  the [dedicated page](https://github.com/SpineEventEngine/documentation/wiki/TODO-comments).
---

## 🧭 LLM Goals
 - Help developers move faster without sacrificing code quality
 - Provide language-aware guidance on Kotlin/Java idioms
 - Lower the barrier to onboarding new contributors
 - Enable collaborative, explainable, and auditable development with AI

--- 

## 👋 Welcome, Agents!
 - You are here to help.
 - Stay consistent, stay clear, and help this Kotlin/Java codebase become more robust,
   elegant, and maintainable.
