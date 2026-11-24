# Old World Trader — Project Requirements and Completion Plan

**Game title:** Old World Trader  
**Platform:** Android, Kotlin + Jetpack Compose  
**Goal:** Offline-capable strategy, city management, trading, ships, and empire progression.  
**APK policy:** Do not download/install until the user explicitly requests it.

## Required gameplay systems

### City management
- [ ] Owned-city dashboard: population, treasury/value, defense, housing, welfare, electricity
- [ ] Housing construction and population growth with progression
- [ ] Building upgrades with costs, requirements, effects, and level display
- [ ] Hospital, Police Station, Barracks, Temple, Park, Power Plant
- [ ] Power plant unlocks electricity and powered infrastructure
- [ ] City prosperity and annual/passive income with transparent calculations

### Trade and markets
- [x] City-specific market prices and price trend indicators
- [x] Inventory/stock quantities and stock-limited buying
- [x] Buying and selling with quantity, affordability, inventory, and stock validation
- [ ] Separate: own-city trade, city travel/trade, and country-level trade
- [x] Market price changes driven by deterministic seasonal supply/demand events and year-based pricing (CI verified in run #56)
- [x] Yearly port-stock replenishment refills depleted traded goods up to a cap without reducing surplus stock (unit tests)
- [ ] Shop ownership and inventory management

### Travel and maritime systems
- [ ] City travel and country travel as separate navigation systems
- [ ] Ships, purchase, cargo capacity, upgrade progression
- [ ] Port routes and travel costs/duration
- [ ] Fleet ownership and ship selection

### Army and kingdoms
- [ ] Recruit and maintain army with costs
- [ ] City defense and conquest calculations
- [ ] Kingdom/country systems and strategic progression
- [ ] Intelligence reports and spy/information mechanics

### Progression
- [ ] Research/technology tree
- [ ] Unlock gates tied to population, buildings, wealth, or tech
- [ ] Health, education, safety, morale, and city development modifiers
- [x] Offline save/load game state and recovery after app restart (gold, year, army, country, cities, building levels/unlocks, market stocks and cargo; stored in app-private preferences)

### UI/UX and reliability
- [ ] Main dashboards and distinct sections for all systems
- [ ] Responsive phone layout, clear feedback, confirmations for costly actions
- [ ] Offline-first; no unnecessary network permissions
- [ ] Playable tutorial/onboarding and help text
- [ ] Game-state persistence, data migration and safe defaults
- [ ] QA: Android build, install, smoke tests, edge cases, balance review

## Current implementation snapshot
Implemented in repository:
- Basic Compose prototype: city list/founding, building upgrades, annual advancement, recruitment and conquest
- Starter economy and progression data models
- City-specific market transaction engine for market quotes, price trends, stock and cargo validation
- **Market tab integration:** select a trading port; show city/year/stock-aware prices and trend; track port stock independently and shared cargo; buy/sell through the transaction engine with quantity, affordability, inventory and overflow checks
- **Seasonal market events:** repeatable commodity/city/year supply-surplus, shortage, high-demand, high-supply, or balanced factors are integrated into the port UI; trends compare current and prior-year event factors
- JUnit test added for city-stock isolation and separate port quotes
- GitHub Actions runs unit tests before the debug APK build

**Not yet implemented:** full city management dashboards and services, manual supply/demand controls, separate country trade, shop ownership/stock management, ship/fleet and route systems, kingdom/intelligence/research, comprehensive save/load, tutorial, and on-device QA.

**Important:** Helper logic is not the same as fully wired gameplay. Items above remain unchecked until implemented in the UI/game state, built, and tested. Do not label the game complete or release-ready until this checklist and on-device QA are complete.
