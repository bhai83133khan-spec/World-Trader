# Old World Trader — Fixed Progress Baseline v1.0

Date established: 2026-11-25

Progress is calculated from fixed, weighted milestones (100 total points). Work-in-progress is not counted as complete. This baseline corrects prior rough estimates; it is **not evidence of code regression**. A percentage only changes when a milestone's verification status changes or when a documented baseline correction is made.

| Milestone | Weight | Status | Evidence / remaining |
|---|---:|---|---|
| Basic city loop: founding, upgrades, yearly advance, starter recruitment/conquest | 15 | Complete | README implementation snapshot |
| Full city dashboards and services (housing/population, hospital, police, welfare, electricity, prosperity) | 10 | Not complete | Resource stock/production is partial; full service effects and resource consumption/trade remain incomplete |
| Market engine: city-specific prices, stock/cargo, buy/sell validation | 15 | Complete | REQUIREMENTS.md + README implementation snapshot |
| Seasonal supply/demand price events | 5 | Complete | REQUIREMENTS.md + README |
| Trend-risk classification and boundary/extreme tests | 5 | Complete | 8%/20% thresholds; latest documentation commit |
| Separate own-city/country trade and shop ownership/stock management | 10 | Not complete | Shop ownership, stock and revenue are wired/tested; separate country-level trade remains incomplete |
| Ships, cargo upgrades, ports/routes, city/country travel, fleet selection | 15 | Complete | FleetSystem/FleetDashboard, City/Country travel, persistence and unit tests; CI run #199 passed |
| Advanced army, city defense, kingdom/country and intelligence systems | 10 | Not complete | Garrison, defense, deterministic conquest, enemy turns and battle reports are implemented/tested; broader kingdom/country intelligence remains |
| Research/technology and progression gates | 5 | Complete | Four research technologies with prerequisite/population gates, integrated effects, save/load, UI and tests; CI run #199 passed |
| Offline persistence and recovery | 5 | Complete | REQUIREMENTS.md marked complete; full device restore still requires QA |
| Tutorial, UX polish, QA, build/release checks | 5 | Not complete | Not yet verified end-to-end |

**Verified milestone total: 65 / 100 = 65%.**

The earlier 28% was an unweighted estimate and is not comparable. No feature is claimed to have been added by this baseline commit. CI/device status must be reported separately; an empty status response is not a green build.


## Implementation update — 2026-11-28

Local food, timber, and iron stocks with annual production from Farm/Sawmill/Iron Mine upgrades, bounded stock, save/load migration, and city/kingdom display are CI-verified partial city-management progress. Annual food consumption (1 unit per 10 people) and a food-shortage population penalty capped at 10% per year are now integrated into the yearly settlement and forecast UI Ships/sea trading and research/technology progression now count as complete after CI run #199 passed unit tests, `assembleDebug`, and APK artifact upload. Garrison station/withdraw, capture cleanup, defense and upkeep are integrated/tested, but the broader kingdom/country/intelligence milestone remains incomplete. The verified fixed baseline is now **65/100**.
