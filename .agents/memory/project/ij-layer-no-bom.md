---
name: ij-layer-no-bom
description: Why the IntelliJ Platform layer publishes no BOM — the uber POMs already are the contract.
metadata:
  type: project
  since: 2026-08-18
---

The IntelliJ Platform layer (`intellij-platform`, `intellij-platform-java`)
deliberately publishes **no BOM artifact** and no `<dependencyManagement>`
table. The `.410`-era POMs — the complete flattened runtime closure as
pinned `runtime` entries with wildcard exclusions — are the whole contract.

**Why:** A constraint only says something a dependency doesn't when the
module is *not* already in the consumer's graph; for anyone using the fat
JARs, all entries are. Probes (2026-08-18) showed Gradle derives platform
variants for any POM-only module, mapping `<dependencyManagement>` (and only
it) to constraints — so if an importable version table is ever needed, an
inline table in the existing uber POMs suffices; a separate BOM module is
never needed. Downstream ("Phase B", `compiler` repo) is better served
deriving its CLI contract from its own resolved graph, which the IJ entries
reach transitively — an imported IJ table could only agree with that or be
stale. Decision history: `git log --follow .agents/tasks/intellij-platform-bom.md`.

**How to apply:** Do not re-propose an `intellij-platform-bom` artifact or
restructure the uber POMs without a concrete consumer that needs a
declarative `platform(...)` import and cannot derive from resolution.
If that consumer appears, add an inline `<dependencyManagement>` table in
`declareUnshadedDependencies` (one function) — and note the derived platform
variants exist only while the `fatJar` publications stay POM-only (no Gradle
Module Metadata).

Related: [[proportional-machinery]]
