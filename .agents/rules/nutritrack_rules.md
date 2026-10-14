# NutriTrack Development Rules

- docs/PRD.md is the source of truth. Do not add features, libraries or permissions that it does not list.
- Kotlin + Jetpack Compose + Material 3, MVVM with StateFlow, Hilt, Room, DataStore, WorkManager.
- Brand: red #D62839 and white with full dark theme. Use only tokens from NutriTrackTheme.
- No hard-coded strings (use string resources, English and Urdu), colours or dimensions.
- All calculations live in domain/calc and are unit tested.
- Offline-first. No analytics, ads or network calls except barcode lookups (P1).
- Every screen: empty state, dark mode, 200% font scale, TalkBack labels, 48 dp touch targets.
- Never commit keystores, API keys or local.properties.
