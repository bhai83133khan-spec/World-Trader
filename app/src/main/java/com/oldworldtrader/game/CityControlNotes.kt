package com.oldworldtrader.game

// Player settlement ownership is distinct from the original geographic country.
// This optional identifier also records which AI faction currently controls a city.
// A null value retains legacy-save behavior: country ownership is inferred from country.
// Added with a default so existing constructors and saved games remain compatible.
