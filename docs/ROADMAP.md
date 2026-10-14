# NutriTrack for Android: Master Application Roadmap

This document outlines the master engineering roadmap for NutriTrack Android based on [`docs/PRD.md`](PRD.md).

---

## 1. Executive Summary

NutriTrack is an offline-first native Android app (Kotlin, Jetpack Compose, Material 3) engineered for high performance (sub-2s cold start, sub-10s logging loop, 60 fps rendering). It tracks calories, macronutrients, water, steps, workouts, and weight with first-class support for South Asian and Pakistani cuisine (300+ seed foods with Urdu and Roman Urdu search aliases). It enforces strict privacy by default: all data is stored locally in Room SQLite with zero external analytics, accounts, or tracking.

---

## 2. Technical Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin (latest stable), Coroutines, Flow, StateFlow |
| **UI** | Jetpack Compose, Material 3, Navigation Compose (type-safe routes) |
| **Architecture** | MVVM / Clean Architecture with Unidirectional Data Flow (`UiState`, `UiEvent`) |
| **Dependency Injection** | Hilt |
| **Database** | Room (SQLite) with FTS4 full-text search, exported schemas & migration tests |
| **Key-Value Storage** | Jetpack DataStore (Preferences) |
| **Background Work** | WorkManager (periodic reminders, step snapshots, midnight rollover) |
| **Steps** | Dual-source: Health Connect (`StepsRecord`) with fallback to hardware `TYPE_STEP_COUNTER` + manual entry |
| **Charts** | Custom Compose `Canvas` charts (zero external chart dependencies) |
| **Design System** | Brand Red `#D62839`, Bricolage Grotesque (headings), Figtree (body), 4dp grid |
| **Testing** | JUnit 5 / 4, MockK, Turbine, Compose UI tests, Room tests, Roborazzi / Paparazzi |

---

## 3. Execution Phases (0 to 9)

```mermaid
gantt
    title NutriTrack Android Execution Timeline
    dateFormat  YYYY-MM-DD
    section Core Foundation
    Phase 0: Setup & Architecture       :p0, 2026-10-15, 2d
    Phase 1: Design System & Components :p1, after p0, 3d
    Phase 2: Data & Domain Calculators  :p2, after p1, 3d
    section Core Features (P0)
    Phase 3: Onboarding & Profile Setup :p3, after p2, 2d
    Phase 4: Core Food Logging & Diary  :p4, after p3, 4d
    Phase 5: Steps, Workouts & Water    :p5, after p4, 4d
    Phase 6: Weight, Progress & Backup  :p6, after p5, 3d
    Phase 7: Polish, Motion & A11y      :p7, after p6, 3d
    section Enhancements & Release
    Phase 8: P1 Features & Widgets      :p8, after p7, 5d
    Phase 9: Store Release Hardening    :p9, after p8, 2d
```

### Phase 0: Project Setup & Baseline Architecture
- Package `com.nutritrack.app`, `minSdk = 26`, `targetSdk = 34+`.
- Gradle Kotlin DSL with Version Catalog (`gradle/libs.versions.toml`).
- Configure Hilt, Room, Compose compiler, Navigation Compose, WorkManager, DataStore.
- Modular package layout (`designsystem`, `core`, `domain`, `data`, `feature`, `widget`).
- Linting & quality: `detekt`, `ktlint`, GitHub Actions CI workflow.

### Phase 1: Design System & Core Component Library
- Static brand palette (`primary` = `#D62839`, `primaryContainer` = `#FCEBED`, dark mode `primary` = `#FF6B75`).
- Bundled offline fonts in `res/font/`: Bricolage Grotesque & Figtree.
- Core components: `NtTopAppBar`, `NtBottomBar` (with 60dp red FAB notch), `CalorieRing` (240dp canvas), `MacroProgress`, `MacroDonut`, `StatTile`, `MealCard`, `FoodRow`, `QuantityStepper`, `ServingSelector`, `WaterGlass`, `WeekBarChart`, `LineChart`, `StreakChip`, `BadgeCard`, `NtChip`, `NtBottomSheet`, `NtDialog`, `NtTextField`, `NtButton`, `EmptyState`, `Skeleton`, `Snackbar`.
- Debug-only Component Gallery screen.

### Phase 2: Data Layer & Domain Pure Calculators
- Pure calculators in `domain/calc/` (BMR Mifflin-St Jeor, TDEE, Deficit/Surplus pace, Macro grams, Water goal, Step calories & distance, MET workout calories, BMI, Streak calculation) with 100% unit test coverage.
- Room database with entities: `UserProfile`, `Food`, `FoodFts`, `ServingOption`, `DiaryEntry`, `WaterLog`, `StepDay`, `StepSnapshot`, `Workout`, `WeightLog`, `Achievement`, `AppMeta`.
- FTS4 Virtual Table for instant food searching with Urdu and Roman Urdu aliases.
- Seed database loader: populates `assets/foods_seed.json` (300+ foods, 70+ desi items) on first launch.
- DataStore preferences for theme, units, reduce motion, reminder settings.

### Phase 3: Onboarding & Profile Setup Wizard
- Android 12+ SplashScreen API.
- 3-slide welcome carousel ("Log fast", "See your day", "Stay private").
- Health disclaimer screen with mandatory agreement.
- 5-step wizard (Name/Gender, Age, Height/Weight, Activity Level, Goal/Pace).
- Plan reveal screen with animated targets and calorie fine-tuning slider.

### Phase 4: Core Food Logging & Diary
- Home Dashboard: greeting, streak chip, sticky date strip, 240dp `CalorieRing`, macro bars, 2x2 meal tiles, 7-day intake chart.
- Add Food Bottom Sheet: search field with 200ms debounce, FTS4 matching, tabs (Recent, Favorites, My Foods, All), one-tap quick add with Undo Snackbar.
- Food Detail screen: emoji tile, serving selector, quantity stepper, live macro donut, micronutrients.
- Custom food creator and Quick Add calories dialog.
- Diary screen: collapsible meal sections, swipe-to-delete with Undo, copy from yesterday, clear day.

### Phase 5: Activity Tracking (Steps, Workouts, Water, Timer)
- Water Tracker: animated `WaterGlass`, quick-add buttons (+150ml, +250ml, +500ml), daily goal celebration, 7-day chart.
- Step Counter: Health Connect `StepsRecord` + hardware `TYPE_STEP_COUNTER` fallback with WorkManager snapshots; reboot & midnight rollover handling; manual override.
- Workouts: 12 presets + custom activity, MET calorie calculation, persistent workout timer with foreground notification, deduplicated step calories on Home.

### Phase 6: Weight, Progress Analytics, Settings & Data Management
- Weight log sheet, 7-day moving average `LineChart`, BMI scale, projected goal arrival date.
- Progress screen: range tabs (Week, Month, 3M, Year), intake vs goal chart, macro split donut, adherence rate.
- Settings: profile/goals adjustment, metric/imperial toggle, theme mode.
- Data & Privacy: JSON full backup & CSV diary export via Storage Access Framework, schema-validated JSON import (Merge/Replace), secure two-step delete all data.

### Phase 7: Polish, Transitions & Accessibility Pass
- Motion: Shared-element transitions, 900ms calorie ring animation, 600ms number count-up, system `reduce_motion` support.
- Haptics: light feedback on add, undo, stepper increments, and goal completion.
- Accessibility: TalkBack content descriptions on all charts/icons, 48x48dp touch targets, 4.5:1 contrast, 200% font scaling.
- Adaptive layouts: Navigation Rail and two-column dashboard on tablets and foldables.

### Phase 8: P1 Features (Barcodes, Recipes, Reminders, Widgets)
- Barcode Scanner: CameraX + ML Kit + Open Food Facts API with Room caching and offline mode.
- Meal templates ("Saved Meals") and Recipe creator with computed per-serving nutrition.
- WorkManager reminders (meals, hydration with quiet hours, weigh-in, weekly summary).
- Jetpack Glance widgets (2x2 Calories, 4x2 Summary with quick actions, 2x1 Water) and launcher dynamic shortcuts.
- Gamification: streaks and 12 achievement badges.
- Urdu localization (`res/values-ur/strings.xml`) with full RTL layout mirroring.

### Phase 9: Release Hardening & Store Readiness
- Baseline Profile generation for sub-2s startup and zero-jank scrolling.
- R8 minification and bundle size optimization (< 25MB).
- Room migration test suite.
- Macrobenchmark performance validation.
- Google Play Store assets, adaptive icons, and privacy policy declaration.

---

## 4. Verification & Testing Strategy

```bash
# Lint & Static Analysis
./gradlew detekt ktlintCheck lintDebug

# Unit Tests (Domain Calculators & Use Cases)
./gradlew testDebugUnitTest

# Database & Migration Tests
./gradlew connectedDebugAndroidTest

# Compose UI & Screenshot Tests
./gradlew verifyRoborazziDebug

# Build Debug APK
./gradlew assembleDebug
```
