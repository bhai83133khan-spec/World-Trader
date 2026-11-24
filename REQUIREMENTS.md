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
- [ ] City-specific market prices and price trend indicators
- [ ] Inventory/stock quantities and stock-limited buying
- [ ] Buying and selling with quantity, affordability, inventory, and stock validation
- [ ] Separate: own-city trade, city travel/trade, and country-level trade
- [ ] Market price changes driven by supply, demand, and time
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
- [ ] Save/load game state and recovery after app restart

### UI/UX and reliability
- [ ] Main dashboards and distinct sections for all systems
- [ ] Responsive phone layout, clear feedback, confirmations for costly actions
- [ ] Offline-first; no unnecessary network permissions
- [ ] Playable tutorial/onboarding and help text
- [ ] Game-state persistence, data migration and safe defaults
- [ ] QA: Android build, install, smoke tests, edge cases, balance review

## Current implementation snapshot
Implemented in repository at the time this document was written:
- Basic Compose prototype: city list/founding, building upgrades, annual advancement, market buy/sell, recruitment and conquest
- Starter economy and progression data models
- MarketSystem helper: stock-aware quote calculations, supply/demand factors, price trend labels, and buy/sell validation helpers

**Important:** Helper logic is not the same as fully wired gameplay. Items above remain unchecked until implemented in the UI/game state, built, and tested. Do not label the game complete or release-ready until this checklist and on-device QA are complete.
