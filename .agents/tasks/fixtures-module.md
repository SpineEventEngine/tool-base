---
slug: fixtures-module
branch: java-code-module
owner: claude
status: done
started: 2026-08-18
---

## Goal

Prune the unused Protobuf fixtures of `tool-base` and move the rest into
a `fixtures` module which is explicitly **not published**.

## What was removed

Of the thirteen fixture protos, five generated types that no test in this
repository references, directly or through another fixture:

- `spine/method/messages_with_vbuilders.proto`
- `spine/method/rejections.proto`
- `spine/method/uuid_messages.proto`
- `spine/test/code/generate/nested_field_scanner_test.proto`
- `spine/test/code/outer_class_test.proto`

Method: map every generated top-level type back to its source proto through
the `// source:` header of the generated Java, then search all 199 test
sources for each type name, and separately search for the proto file names and
Protobuf packages in case of string references. The remaining eight protos
have no imports pointing at the removed five, so nothing dangles.

Two false positives had to be discarded by hand: `Project` and `Task` match
Gradle's own types across `buildSrc` and the plugin modules, not the fixture
messages.

## What was kept

- `spine/test/code/{inherit_all,nested,no_outer_classname,no_package,}source_file_test.proto`
  — consumed by `java-code`'s `SourceFileSpec`;
- `spine/test/tools/type/{person,project,task}.proto` — consumed by
  `proto-code`'s `FileDescriptorSupersetTest`, and `project.proto`'s service by
  `SourceFileSpec`.

## Notes

The module holds the protos in its **main** source set rather than
`testFixtures`, so consumers write `testImplementation(project(":fixtures"))`.
It is kept out of `spinePublishing.modules` in the root build script, and the
reason is documented both there and in the module's own build script: these
declarations are renamed and deleted as the repository's tests change, so no
downstream project may depend on them.

With the fixtures gone, `tool-base` needs no `java-test-fixtures` plugin, no
Protobuf plugins and no gRPC. Its build script is now four lines of
dependencies for two classes.

## Log

- 2026-08-18 — pruned, moved and verified with `clean build dokkaGenerate
  --no-build-cache`. The `fixtures` JAR carries only the kept types, and
  `:fixtures:tasks` registers no `publish*` task at all.
