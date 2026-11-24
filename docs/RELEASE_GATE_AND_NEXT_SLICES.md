# Old World Trader — Release Gate and Next Gameplay Slices

This document is a delivery gate, not a claim that the listed systems are implemented. Keep REQUIREMENTS.md as the source of truth for completion.

## Release gate (must all pass before calling the game release-ready)
- [ ] Debug APK builds successfully from GitHub Actions on the current main commit.
- [ ] Install and launch on an Android phone; no crash on cold start.
- [ ] Founding a city, upgrading a building, advancing a year, recruiting, and attempting conquest all produce valid state transitions.
- [ ] Save is restored after force-stop/relaunch; corrupted or missing save data falls back safely.
- [ ] Market: buying is rejected when stock, gold, or cargo capacity is insufficient; selling is rejected when cargo is insufficient; port stocks remain isolated.
- [ ] Trend-risk labels are stable at the exact boundaries and at extreme integer prices.
- [ ] Seasonal price/event output is deterministic for the same city, commodity, and year.
- [ ] UI shows confirmation for irreversible/high-cost actions and communicates rejection reasons.
- [ ] Offline behavior verified with network disabled; no network permission is required for core play.
- [ ] Tutorial/help explains the first city, market, cargo, and annual progression.
- [ ] Final checklist is reviewed against REQUIREMENTS.md; no unchecked requirement is represented as complete.

## Priority implementation slices

### Slice A — City dashboard (first)
Build a single owned-city detail screen and wire it to the existing game state:
1. Summary cards: population, treasury, defense, housing, welfare, electricity.
2. Building list with level, effect, upgrade cost, and unmet prerequisites.
3. Annual progression preview before applying it; explain income/expense changes.
4. Power Plant state gates powered infrastructure; no silent unlocks.
5. Unit tests for prerequisite checks, affordability, and level caps.

Acceptance: city summary values match the underlying state; unaffordable upgrades and unmet requirements are blocked with a user-readable reason; successful upgrade persists after restart.

### Slice B — Trade ownership and shop
Add shop ownership and inventory as explicit state:
1. Purchase gate and one-time price.
2. Inventory limits and stock replenishment rules.
3. Buy/sell at own city separate from port markets.
4. Tests for insufficient funds, capacity, repeated purchase, and save/reload.

### Slice C — Maritime travel
Separate city travel from country-level travel:
1. Ship roster and selected vessel.
2. Cargo capacity tied to vessel.
3. Route cost/duration with clear preview.
4. Arrival updates destination market context without mutating unrelated city stock.
5. Tests for route validation, insufficient travel funds, capacity, and route-state persistence.

### Slice D — Kingdom and progression
Only after the city/trade/travel loops are stable:
1. Defense and conquest resolution with transparent calculations.
2. Kingdom/country ownership state.
3. Research gates tied to clear prerequisites.
4. Intelligence reports clearly marked as estimates.

## QA record to fill per release candidate
- Commit SHA:
- Workflow run URL / status:
- APK artifact and size (only if produced):
- Device model / Android version:
- Cold-start result:
- Save/reload result:
- Offline result:
- Gameplay acceptance tests:
- Known issues:
- Release decision: PASS / HOLD

## Percentage policy
A slice is counted complete only after implementation is wired into the app, tests pass, and the relevant acceptance checks are verified. Documentation or a partial implementation alone does not raise the verified completion percentage.
