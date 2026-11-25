# Copilot Instructions — Old World Trader

## Priority
Work toward a playable offline release. Make real source-code changes, tests, and commits; do not substitute status comments or planning documents for implementation.

## Priority bugfix slice (before Ships and Sea Trading)
Address issue #3 first: city travel/entry access and city-specific market pricing.
1. Fix travel arrival/current-port state so listed destinations can be visited and their markets entered without owning the city.
2. Keep ownership checks only for founding/ownership actions (e.g. shop building), not basic visiting/trading.
3. Make prices differentiated per city/port and commodity using deterministic modifiers; prices should remain consistent in a port unless market rules update them.
4. Add focused tests for travel/entry gating and multiple ports showing distinct prices.
5. Run unit tests and debug APK build; report artifact and any phone smoke-test limitation.
Only after this bugfix slice is implemented and verified, resume the Ships and Sea Trading feature below.

## Next feature slice: Ships and Sea Trading
Implement a small, end-to-end ship trading loop using the existing Kotlin/Compose architecture and game state/persistence patterns:
1. Add a ship model with name/type, purchase price, cargo capacity, and owned/selected state.
2. Add a shipyard/shop UI where players can purchase a ship only if they can afford it; validate the purchase and deduct gold atomically.
3. Integrate ship cargo capacity into the existing market trading flow. Preserve current stock, affordability, inventory, and quantity validations; do not break shop inventory.
4. Add at least two port-to-port routes with deterministic travel duration and travel cost. Prevent trading or duplicate travel actions while at sea; apply travel completion consistently.
5. Persist ship ownership, selected ship, cargo, current port, and travel state across app restarts using the current offline save mechanism. Use safe defaults for older saves.
6. Add unit tests for purchase affordability, capacity boundaries, route cost/time, travel state, persistence round-trip, and invalid actions.
7. Wire the feature into Compose UI with clear state, feedback, and costly-action confirmation where appropriate.

## Working rules
- Inspect existing architecture before changing files; reuse existing domain/state/UI conventions.
- Keep all gameplay offline; do not add network permissions, accounts, ads, or APIs.
- Prefer focused, reviewable commits. Run relevant tests and Android build before claiming success.
- Update REQUIREMENTS.md only when implementation and verification evidence justify marking an item complete.
- If build or tests fail, fix the failure rather than only reporting it.
- Do not claim APK release readiness until CI build artifact exists and smoke-test limitations are clearly stated.
