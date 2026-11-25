# Old World Trader — Offline Android Strategy Game

Old World Trader is an offline-first medieval trading and city/kingdom strategy game for Android (Kotlin + Jetpack Compose). Build a trading empire through city markets, ships, travel, ownership, upgrades, research and warfare.

## Quality and size policy

The former 50 MB target is removed. There is **no hard APK-size cap**. A finished game may reach roughly **200–400 MB** if high-quality original maps, city/ship/building artwork, animations, sound and gameplay content justify it. Prioritize a rich, polished game over artificially shrinking it; still optimize assets sensibly and avoid duplicate or wasteful files.

## Current implemented systems (repository snapshot)

- City founding, ownership, building upgrades, population and annual advancement
- City-specific market pricing, stock, cargo and transaction validation
- Deterministic seasonal supply/demand events and trend-risk indicators
- Shop purchase, stocking, customer sales and cash collection
- Kingdom rank progression and founding discounts
- Army recruitment, deterministic battle/conquest logic
- Fleet, ship/cargo and travel systems
- Research technology tree
- Offline save/load with persisted game state

The feature list above is a source-code snapshot, not a claim of complete end-to-end phone QA. See `REQUIREMENTS.md` and `docs/PROGRESS_BASELINE.md` for remaining work and verified completion tracking.

## Build and testing

GitHub Actions runs unit tests before the debug APK build. Open **Actions** → **Build Old World Trader APK** and download the `OldWorldTrader-debug-apk` artifact from a successful run. Physical phone smoke testing is still required before release readiness is claimed.

The game is intended to work offline; no backend, account, ads or game API is required.
