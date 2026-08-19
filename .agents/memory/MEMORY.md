# Team memory index

One line per memory. Scan at the start of every session.
See [README.md](README.md) for the format and routing rules.

## Feedback (validated patterns & corrections)

- [copilot-review-request](feedback/copilot-review-request.md) — GraphQL `requestReviews` with `botIds: ["BOT_kgDOCnlnWA"]`; REST endpoint silently no-ops on re-requests.
- [proportional-machinery](feedback/proportional-machinery.md) — keep build machinery proportional; no external tools for verification — Gradle API suffices.

## Project (durable context & rationale)

- [ij-layer-no-bom](project/ij-layer-no-bom.md) — the IJ layer publishes no BOM; the uber POMs are the contract, and platform variants can be derived from them if ever needed.

## Reference (external systems)

- [cache-warm-window](reference/cache-warm-window.md) — How prompt cache entries are shared between sibling-repo sessions and how to maximise overlap.
- [stale-generated-from-build-cache](reference/stale-generated-from-build-cache.md) — renamed proto packages come back from the Gradle build cache; verify with `--no-build-cache`.
- [anthropic-api-caching](reference/anthropic-api-caching.md) — Pattern and pricing for adding prompt caching to any direct Anthropic API call.
