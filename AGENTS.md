# `AGENTS.md`

> Guidance for Large Language Model (LLM) Agents (ChatGPT & Codex) collaborating on this project.

---

## 🧠 The purpose of this document

This file defines how LLM agents (e.g. **ChatGPT** and **Codex**) should **interact with the codebase**, **generate content**, and **assist in development workflows** for this project, which is based on **Kotlin** and **Java**. It outlines conventions, expectations, and usage goals to ensure **productive, safe, and consistent collaboration**.

---

## 🛠️ Project overview

- **Languages**: Kotlin (primary), Java (secondary).
- **Build Tool**: Gradle with Kotlin DSL.
- **Architecture**: Event-driven Domain-Driven Design (DDD).
- **Testing**: JUnit 5
- **Style**: Kotlin idiomatic code preferred over Java-style code
- **Tools Used**: KotlinPoet, KSP, Gradle plugins, IntelliJ IDEA

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

### 🔧 Codex (code completion/generation)

- Complete functions, classes, and tests
- Generate Kotlin idioms (e.g., extension functions, DSLs)
- Follow patterns from nearby code
- Generate test scaffolds and fixtures
- Fill in `when` branches, sealed class hierarchies, etc

---

## 🧾 Coding guidelines for Agents

### ✅ Preferred

- Use **idiomatic Kotlin**, including:
    - Extension functions
    - `when` expressions
    - Smart casts
    - Data classes
    - Sealed classes
- Apply **Java interop** only when needed (e.g., using annotations or legacy libraries)
- Use **Kotlin DSL** when modifying or generating Gradle files
- Generate code that **compiles cleanly** and **passes static analysis**
- Respect **existing architecture**, naming conventions, and project structure
- Use `@file:JvmName`, `@JvmStatic`, etc., where appropriate.

### ❌ Avoid

- Java-style verbosity (e.g., builders with setters)
- Redundant null checks (`?.let` misuse)
- Using `!!` unless clearly justified
- Mixing Groovy and Kotlin DSLs in build logic
- Overuse of reflection unless requested

### General guidance

- Write clear, incremental commits with descriptive messages.
- Include automated tests for any code change that alters functionality.
- Keep pull requests focused and small.
- Adhere to the [Spine Event Engine Documentation](https://github.com/SpineEventEngine/documentation/wiki) for coding style and
  contribution procedures.

### Naming convention for variables
- Prefer simple nouns over composite nouns. E.g., `user` is better than `userAccount`.

---

## Running builds

- When modifying code, run `./gradlew build` before committing.
- If Protobuf (`.proto`) files are modified **always** run `./gradlew clean build`.
- Documentation-only changes do not require running tests.

---

## Incrementing a patch version for each pull request

When creating a pull request, update the `version.gradle.kts` file in the root of a project
by incrementing the last component of the version number by 1.

If the last component has leading zeroes, keep the padding of zeroes so that the width of
the last component stays the same. For example, the version `"2.0.0-SNAPSHOT.009"` should become
`"2.0.0-SNAPSHOT.010"`.

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

## 📄 Documentation Tasks for ChatGPT

- Generate and update **KDoc** for public APIs
- Draft/update **README.md**, **CONTRIBUTING.md**
- Suggest better **names** and **abstractions**
- Help format inline comments and design rationale
- Generate changelogs based on Git commit summaries

---

## 🧪 Testing Responsibilities

- **Codex** should generate tests for:
    - Public functions
    - Edge cases
    - Custom serializers / deserializers
    - Extension functions and DSLs

- **ChatGPT** may suggest:
    - Test coverage improvements
    - Property-based testing
    - Mocking and test lifecycle setups

---

## 🚨 Safety Rules for Agents

- Do **not** auto-update external dependencies without explicit request
- Do **not** inject analytics or telemetry code
- Flag any usage of unsafe constructs (e.g., reflection, I/O on main thread)
- Avoid generating blocking calls inside coroutines

---

## 💬 Interaction tips

- Use comments like `// ChatGPT: explain this logic` or `// Codex: complete this function`
- Use Git commit messages like:

  ```text
  feat(chatgpt): suggested DSL refactoring for query handlers  
  fix(codex): completed missing case in sealed class hierarchy  

  ```
- Encourage `// TODO:` or `// FIXME:` comments to be clarified by ChatGPT

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
