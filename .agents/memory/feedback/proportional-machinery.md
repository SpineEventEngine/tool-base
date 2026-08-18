---
name: proportional-machinery
description: Keep build machinery proportional; no external tooling for verification — Gradle API is enough.
metadata:
  type: feedback
  since: 2026-08-18
---

When a task can be met by a small amount of Gradle-API code, do not grow it
into multi-task machinery (generated checked-in files, cross-module
verification tasks, drift checks), and do not introduce external tooling —
even session-scoped — such as installing Maven to verify a published POM.

**Why:** The `intellij-platform-bom` work (2026-08-18) was restarted by the
owner after the first implementation accumulated a generated `contract.txt`,
per-module slice tasks, and `verifyContract`/`updateContract` wiring, and then
proposed downloading Maven for an interop check. Verdict: "way too
complicated… Gradle API should be enough for the task at hand."

**How to apply:** Prefer execution-time `pom.withXml` / resolution over new
task graphs and generated files. Verify published artifacts with a bare Gradle
consumer project, not with foreign build tools. When a plan step seems to
require a tool absent from the machine, treat that as a sign the plan is
over-scoped and simplify instead of provisioning the tool.
