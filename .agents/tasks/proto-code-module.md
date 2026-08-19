---
slug: proto-code-module
branch: java-code-module
owner: claude
status: in-progress
started: 2026-08-18
---

## Goal

Collect the Protobuf-specific parts of `tool-base` into a `proto-code` module
(`io.spine.tools:proto-code`), continuing the language-by-language split that
produced `fs`, `java-code`, `js-code` and `dart-code`:

- `io.spine.tools.proto.fs` — the `proto` source directory;
- `io.spine.tools.type` — descriptor set merging and known-type extension;
- `io.spine.tools.code.proto` — the code generator request;
- the `proto` source set (`spine/tools/proto/proto.proto`), whose generated
  types land in `io.spine.tools.proto.code` — the very package the module is
  named after.

## Context

Verified before the move: no cycle — nothing left in `tool-base` references
any of the three packages. Dependencies of the moved code:

- `proto.fs.Directory` extends `SourceCodeDirectory` and takes an
  `AbstractDirectory` → `api(project(":fs"))`;
- `FileDescriptorSuperset` reads descriptor sets out of archives via
  `io.spine.tools.archive`, which stays in `tool-base`, and the types do not
  reach its public API → `implementation(project(":tool-base"))`;
- `FileDescriptorSuperset` is `WithLogging` → `Logging`.

`tool-base` keeps the Protobuf plugins: its `testFixtures` source set still
holds fourteen `.proto` files even though `src/main/proto` is now empty.

`DirectorySpec` — the test for the moved `Directory` — was parked in
`java-code` during the earlier extraction because it parents the `proto` root
on `DefaultJavaPaths`. It follows its subject here, so `proto-code` takes
`testImplementation(project(":java-code"))`. No cycle: `java-code` depends
only on `fs`.

Consumers, all keeping their imports since package names are unchanged:
`protobuf-setup-plugins` in this repo; `core-jvm-compiler` and `mc-java`
(`io.spine.tools.proto.code.{ProtoTypeName, ProtoOption}`), `compiler` and
`ProtoTap` (`CodeGeneratorRequestWriter`), `base-libraries`, `core-java-1x`
and `model-tools` (`MoreKnownTypes`).

## Plan

- [x] Create `proto-code`; move the three packages, the `proto` source set,
      and `DirectorySpec`.
- [x] Repoint `protobuf-setup-plugins` at `proto-code`.
- [ ] Verify: `clean build dokkaGenerate`, artifact/package ownership,
      dependency reports; commit when green.

## Downstream follow-ups (other repos)

- [ ] `config` — add a `protoCode` constant to
      `io/spine/dependency/local/ToolBase.kt` (alongside `fs`, `javaCode`,
      `jsCode`, `dartCode`).
- [ ] `core-jvm-compiler`, `compiler`, `ProtoTap`, `base-libraries` — add
      `ToolBase.protoCode`; imports unchanged.

## Log

- 2026-08-18 — moved. Two couplings surfaced only at compile time, neither
  visible to an import scan:
  - `FileDescriptorSupersetTest` uses `PersonProto`, `ProjectProto` and
    `TaskProto`, generated from `tool-base`'s `testFixtures` protos into
    `io.spine.tools.type` — **the same package as the code under test**, so
    they need no `import` and an import-based scan cannot see them. Resolved
    with `testImplementation(testFixtures(project(":tool-base")))`, as
    `java-code` already does.
  - Those fixtures carry gRPC-generated code whose versions `tool-base`
    supplies through a `resolutionStrategy`, not through the coordinates.
    Consuming the fixtures without that strategy fails resolution with
    "Could not find io.grpc:grpc-protobuf:" — note the empty version.
    `proto-code` now mirrors the strategy, as `java-code` does.
