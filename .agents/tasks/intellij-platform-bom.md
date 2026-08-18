---
slug: intellij-platform-bom
branch: intellij-platform-bom
owner: claude
status: done
started: 2026-08-13
related-memories:
  - ij-layer-no-bom
  - proportional-machinery
---

## Goal

*(as drafted)* The third-party surface of the IntelliJ Platform layer becomes
a published Gradle platform: `io.spine.tools:intellij-platform-bom`, imported
by both uber modules, whose POM entries become version-less.

**Outcome: resolved with no changes — the layer already publishes its
contract, and the BOM would have had no reader.** See the decision below.

## Decision (2026-08-18)

No BOM artifact, no `<dependencyManagement>` restructuring, no changes to
`tool-base`. Rationale, established stepwise in session with the owner:

1. **Direct consumers gain nothing.** The `.410` POMs already declare the
   complete flattened closure (20 entries for `intellij-platform`, 83 for
   `intellij-platform-java`, a strict subset relation at identical versions)
   as pinned `runtime` dependencies with wildcard exclusions, resolving
   conflict-free under `failOnVersionConflict()`. A constraint only adds
   information over a dependency when the module is *not* otherwise in the
   graph; for JAR consumers, all entries are.
2. **Gradle's `platform()` is a call-site decision.** Probes against Maven
   Local confirmed: Gradle derives platform variants for every POM-only
   Maven module, mapping only the `<dependencyManagement>` block to
   constraints (regular `<dependencies>` do not participate, and the JAR is
   not an artifact of the platform variant). So *if* an importable table is
   ever wanted, an inline `<dependencyManagement>` in the existing uber POMs
   makes each coordinate both library and platform — no separate BOM module
   is ever needed. (This derivation works *because* the `fatJar`
   publications are POM-only; adding Gradle Module Metadata would stop it.)
3. **Phase B does not need the import.** The compiler repo's platform
   contract should derive from `compiler-cli`'s *own* resolved runtime
   graph — the IJ entries flow in transitively — because the CLI's contract
   must state what the CLI actually resolves, including local forces and
   upgrades. An imported IJ table could only agree with that or be stale.
   The `cliProvidedModules` exclusion set is a set of module names; versions
   never participate.

## Follow-up (other repos)

- [ ] `core-jvm-compiler`: reword the Phase B note in
      `.agents/tasks/unshade-core-jvm-plugins.md` — the CLI contract derives
      from `compiler-cli`'s own resolved graph; importing an IJ-layer BOM is
      optional structure, not a prerequisite, and no such BOM exists.

## Log

- 2026-08-13 — drafted from the `core-jvm-compiler` session (Phase A done
  there; see PR #109). Shape decision at the time: full integration — BOM +
  version-less uber-module POM entries via `dependencyManagement` import.
- 2026-08-18 — session: two implementation shapes explored and reverted
  before the premise was re-examined.
  - First cut: generated, checked-in `contract.txt` + slice/verify/update
    task wiring + a proposal to install Maven for interop checks. Owner
    verdict: too complicated; Gradle API suffices (memory
    `proportional-machinery`). Fully reverted; Maven Local restored.
  - Second plan (not implemented): single POM-only BOM module. Owner
    challenged the direction — the uber modules *are* the platform. Probes
    confirmed the owner's model is Gradle's own (see Decision, item 2).
  - Premise review: asked what problem the BOM solves. Answer: nothing for
    direct consumers; only a declarative convenience for Phase B, which is
    better served deriving from its own graph (Decision, items 1 and 3).
    Task closed with no changes. Durable rationale recorded in project
    memory `ij-layer-no-bom`.
