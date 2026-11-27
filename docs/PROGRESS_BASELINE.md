# Old World Trader — Fixed Progress Baseline v1.0

Date established: 2026-11-25

Progress is calculated from fixed, weighted milestones (100 total points). Work-in-progress is not counted as complete. This baseline corrects prior rough estimates; it is **not evidence of code regression**. A percentage only changes when a milestone's verification status changes or when a documented baseline correction is made.

| Milestone | Weight | Status | Evidence / remaining |
|---|---:|---|---|
| Basic city loop: founding, upgrades, yearly advance, starter recruitment/conquest | 15 | Complete | README implementation snapshot |
| Full city dashboards and services (housing/population, hospital, police, welfare, electricity, prosperity) | 10 | Not complete | Local resource stock/production is implemented; services, resource consumption/trade, and broader dashboard effects remain incomplete |
| Market engine: city-specific prices, stock/cargo, buy/sell validation | 15 | Complete | REQUIREMENTS.md + README implementation snapshot |
| Seasonal supply/demand price events | 5 | Complete | REQUIREMENTS.md + README |
| Trend-risk classification and boundary/extreme tests | 5 | Complete | 8%/20% thresholds; latest documentation commit |
| Separate own-city/country trade and shop ownership/stock management | 10 | Not complete | REQUIREMENTS.md unchecked |
| Ships, cargo upgrades, ports/routes, city/country travel, fleet selection | 15 | Not complete | REQUIREMENTS.md unchecked |
| Advanced army, city defense, kingdom/country and intelligence systems | 10 | Not complete | Starter recruitment/conquest is counted in the basic loop above; advanced systems remain |
| Research/technology and progression gates | 5 | Not complete | REQUIREMENTS.md unchecked |
| Offline persistence and recovery | 5 | Complete | REQUIREMENTS.md marked complete; full device restore still requires QA |
| Tutorial, UX polish, QA, build/release checks | 5 | Not complete | Not yet verified end-to-end |

**Verified milestone total: 45 / 100 = 45%.**

The earlier 28% was an unweighted estimate and is not comparable. No feature is claimed to have been added by this baseline commit. CI/device status must be reported separately; an empty status response is not a green build.


## Implementation update — 2026-11-28

Local food, timber, and iron stocks with annual production from Farm/Sawmill/Iron Mine upgrades, bounded stock, save/load migration, and city/kingdom display have been added and CI-verified. This is partial city-management progress; no weighted milestone is newly complete, so the verified fixed baseline remains **45/100**.
