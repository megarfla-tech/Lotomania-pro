# Changelog - Lotomania AI PRO

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-06-21

### Added
- **Core Architecture**: Implemented modern MVVM clean architecture using standard Android design directives (SOLID, Repository Pattern).
- **Room Local Database**: Integrated Room Database, custom TypeConverters for List<Int> representation, custom seeds for seeding of initial offline historical draws.
- **OkHttp/Retrofit Integrations**: Integrated direct okhttp REST clients to support background online updates syncing from official endpoints.
- **Intellectual Core (Gemini AI)**: Configured OkHttpClient with direct endpoint access and context prompting mapping quents/frios/delay data back to `gemini-3.5-flash` Coprocessor.
- **Stats Engine & ML**: Configured multi-metric statistical engine measuring parities, quadrants, decades, standard line/column deviations, with heuristics simulating Ensemble Random Forests and Recurrent LSTMs.
- **Smart Game Generator**: Integrated 11 custom generation algorithms (Random, Statistical, IA Smart, Balanced, Anti-repetition, etc.) supporting quantities from 1 to 100.
- **Backtest Investment Simulator**: Developed real-time investment simulator testing combos against past draws and estimating costs, payouts, wins (15..20 and 0 points) and net balances.
- **Interactive Bloomberg Graphics**: Programmed high-fidelity interactive Canvas rendering custom Heatmaps, Decade Bars, and Normal line distribution algorithms.
- **GitHub Workflow CI**: Programmed `.github/workflows/android.yml` to support automated push-to-main compile tasks.
