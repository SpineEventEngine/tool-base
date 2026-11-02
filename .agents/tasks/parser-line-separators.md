---
slug: parser-line-separators
branch: claude/github-issue-203-488cfd
owner: claude
status: in-review
started: 2026-11-02
---

## Goal

`io.spine.tools.psi.java.Parser.parse()` accepts Java source text with any line separators
(`\n`, `\r\n`, `\r`, or a mix), producing the same `PsiJavaFile` as for the `\n` version
of the text. Callers no longer need to call `convertLineSeparators()` before parsing.

## Context

- Issue: https://github.com/SpineEventEngine/tool-base/issues/203
- PSI accepts only `\n`; `DocumentImpl` asserts on `\r`, failing on code generated on Windows.
- `StringUtilRt.convertLineSeparators()` (IntelliJ `util-rt` 213.7172.53) allocates a buffer
  only on the first `\r`; otherwise it returns the very same `String`. So `Parser.parse()`
  calls `convertLineSeparators()` unconditionally — no guard of its own, no extra copy
  for `\n`-only text. The no-copy contract is documented and tested on `convertLineSeparators()`.
- Tests build `\r\n`, `\r`, and mixed variants with `io.spine.string.Separator`.

## Plan

- [x] Add `ParserSpec` cases for `\n`, `\r\n`, `\r`, and mixed separators; see them fail.
- [x] Call `convertLineSeparators()` in `Parser.parse()`; update its KDoc.
- [x] Document and pin the no-copy contract of `convertLineSeparators()` in `psi`.
- [x] Bump the version; run `build` and `dokkaGenerate`.

## Log

- 2026-11-02 — drafted and started.
- 2026-11-02 — dropped the `Separator.CR` guard: `StringUtilRt` already skips the copy.
- 2026-11-02 — before the fix, the `CRLF`, `CR`, and mixed cases failed with
  `AssertionError: Wrong line separators`; after it, `build dokkaGenerate` passes.
  Version bumped to `2.0.0-SNAPSHOT.442`.
