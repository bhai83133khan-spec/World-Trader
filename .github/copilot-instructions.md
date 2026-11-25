# Copilot Instructions — Old World Trader

## Priority
Work toward a complete, playable, offline-first medieval trading and empire strategy game. Make real source-code changes, tests, and focused commits; do not substitute status comments or planning documents for implementation.

## Quality and size policy
- The former 50 MB APK target is cancelled; there is no hard size limit.
- A final game size of approximately 200–400 MB is acceptable when justified by high-quality original artwork, maps, city/port/ship/building assets, animations, audio and gameplay content.
- Prioritize quality, complete gameplay and coherent art direction over artificial size reduction.
- Still optimize and deduplicate assets; do not inflate size with redundant or low-value files.
- Keep all assets bundled and gameplay offline.

## Priority gameplay slice
Address city travel/entry and market differentiation first if regressions remain:
1. Fix travel arrival/current-port state so destinations can be visited and markets entered without owning cities.
2. Keep ownership checks for founding/ownership actions, not basic visiting/trading.
3. Ensure deterministic, differentiated port prices, stock, supply/demand and trend behavior.
4. Add focused tests for travel gating, market access and price differentiation.
5. Continue ship/fleet and sea-trading implementation only after core travel/market defects are resolved.

## Next feature slice: Ships and Sea Trading
Implement end-to-end ship trading using existing Kotlin/Compose and save-state patterns:
1. Ship model: type, purchase price, cargo capacity, owned/selected state.
2. Ship purchase UI and atomic affordability/wallet validation.
3. Integrate selected ship cargo capacity into market transactions; preserve stock, affordability, inventory and shop validation.
4. Multiple deterministic port routes, travel duration/cost, and no trading or duplicate departure while at sea.
5. Persist ships, selected ship, cargo, current port and voyage state; use safe defaults for older saves.
6. Add tests for purchase, capacity, routes, travel, persistence and invalid actions.
7. Connect all features to the actual UI and provide clear feedback.

## Further gameplay priorities
- City/kingdom progression, population, ownership, entry and capture
- Army recruitment, defense, AI kingdoms and autonomous attacks
- Battle outcomes, casualty/survivor/loot calculations and Battle Report
- City-specific economy, resource inventories, prices, income and upgrades
- Research/technology gates and progression effects
- Save migration, offline recovery, QA and release verification
- Separate, functional mobile screens/navigation and original lightweight graphics/icon

## Agent coordination
- Before taking UI/graphics-only work, inspect open PRs and any visible Copilot coding-agent task.
- Avoid overlapping a UI/graphics task if a Copilot coding-agent PR/task is already assigned or active.
- Do not claim Copilot is assigned or active unless GitHub evidence confirms it.
- If assignment validation fails, do not repeatedly retry invalid usernames/assignments; record the blocker and continue safe OpenCode work.

## Working rules
- Inspect repository architecture and existing code before changes; reuse conventions.
- Keep offline; do not add network permissions, accounts, ads, backend or APIs.
- Prefer focused, reviewable commits.
- Run relevant tests and CI before claiming success; if blocked, state the exact blocker and continue safe work.
- Update REQUIREMENTS.md only when implementation and verification justify marking features complete.
- Fix failures rather than only reporting them.
- Do not claim APK release readiness until a verified CI artifact exists and phone QA limitations are stated.
