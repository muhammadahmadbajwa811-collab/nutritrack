# NutriTrack for Android: Product Requirements Document

**Version:** 1.0 (draft for build)  |  **Platform:** Android phones (tablets and foldables adaptive)  |  **Intended builder:** Google Antigravity (agent-first IDE)
**Owner:** Muhammad Ahmad Bajwa  |  **Package name (default):** `com.nutritrack.app`  |  **Brand colour:** Red `#D62839` and white, with a full dark theme

> This document is the single source of truth for building the NutriTrack Android app. It is written to be read by a coding agent: every requirement has an ID, a priority and testable acceptance criteria. Where something is ambiguous, use the stated default instead of asking.

---

## 0. How to use this PRD with Antigravity

1. **Put this file in the repository** as `docs/PRD.md` and add a short rules file for the workspace that says: "Read docs/PRD.md before every task. Do not invent features. Follow the design tokens in section 5 exactly. Never hard-code strings, colours or dimensions."
2. **Work in phases** (section 16). Do one phase per agent task. Do not ask the agent to "build the whole app" in one go.
3. **Use Plan mode** for every phase. Read the generated implementation plan and task list before approving. Reject plans that add libraries or features not in this PRD.
4. **Verification:** Antigravity's built-in browser verifies web apps, so it cannot preview a native Android UI. For this app the agent should verify with: a Gradle build (`./gradlew assembleDebug`), unit tests, instrumented/Compose UI tests, lint, and screenshot tests. You run the app on an emulator or your phone and paste screenshots back so the agent can compare them with section 8.
5. **Commit after every phase** with a message that names the phase and requirement IDs (for example `Phase 3: F-LOG-01..F-LOG-06`).
6. **Priorities:** `P0` = must ship in v1.0, `P1` = v1.1 (build after P0 is stable), `P2` = later. Build P0 first; do not skip ahead.

**Defaults when unsure:** metric units, English UI, light theme following the system setting, no account, all data on the device, no analytics, no ads.

---

## 1. Product overview

**One-line pitch:** NutriTrack is a fast, beautiful, private calorie, macro, water, steps and weight tracker that understands the food people actually eat, including everyday Pakistani dishes.

**Problem:** Most calorie apps are slow to log in, cluttered with ads and paywalls, push accounts and cloud sync, and have poor coverage of South Asian food.

**Solution:** An offline-first Android app where a meal can be logged in under 10 seconds, the whole day is visible on one home screen, and nothing leaves the phone unless the user exports it.

**Product principles**

| Principle | What it means in practice |
|---|---|
| Fast to log | Maximum 3 taps from Home to a logged recent food. Recent and favourite foods are always one tap away. |
| Beautiful and calm | Generous white space, one strong red accent, large friendly numbers, smooth motion. No clutter, no ads. |
| Private by default | No account needed. Data stays on the device. Export and delete are one tap away. |
| Honest numbers | Values are approximate and labelled so. Health disclaimer is shown during onboarding and in Settings. |
| Local food first | 300+ foods at launch including desi dishes, with Urdu and Roman Urdu search aliases. |
| Works offline | Every P0 feature works with no internet. |

---

## 2. Goals, non-goals and success metrics

**Goals (v1.0)**
- Deliver a polished, professional-looking Android app that could be published on Google Play.
- Cover logging, goals, macros, water, steps, workouts, weight, progress, reminders, widgets, backup and settings.
- Feel instant: smooth 60 fps, no loading spinners for local data.

**Non-goals (v1.0)**
- No social features, no leaderboards, no friends, no in-app purchases, no ads.
- No cloud accounts or server (cloud backup is P2).
- No medical advice, diagnosis or meal-plan prescriptions.
- No iOS or web version (the existing web project remains separate).

**Success metrics (measured by tests and manual QA, not tracking)**

| Metric | Target |
|---|---|
| Cold start to Home on a mid-range phone | under 2 seconds |
| Log a recent food from Home | 3 taps or fewer, under 10 seconds |
| Crash-free sessions (Play vitals, if published) | 99.5% or higher |
| Frame rendering | no janky frames on Home scroll (Macrobenchmark) |
| Unit test coverage of domain and calculation code | 90% or higher |
| Release app bundle size | under 25 MB |
| Accessibility | TalkBack usable for all P0 flows; contrast 4.5:1 or higher |

---

## 3. Target users

| Persona | Needs | Notes |
|---|---|---|
| **Ayesha, 22, university student** | Lose 4 kg, wants quick logging between classes, eats desi food | Cares about speed and a clean look |
| **Bilal, 28, office worker** | Maintain weight, walk more, drink more water | Wants reminders and steps, hates clutter |
| **Hamza, 19, gym beginner** | Gain muscle, high protein target, track weight | Wants protein focus and workout log |

---

## 4. Platform and technical decisions

| Area | Decision |
|---|---|
| Language | Kotlin (latest stable), coroutines and Flow |
| UI | Jetpack Compose with Material 3; single-activity app |
| Min / target SDK | Min SDK 26 (Android 8.0); target the latest stable API level |
| Architecture | MVVM with unidirectional data flow, three layers: `ui`, `domain`, `data`; repository pattern |
| Dependency injection | Hilt |
| Database | Room (SQLite) with exported schemas and tested migrations; FTS4 table for food search |
| Settings | Jetpack DataStore (Preferences) |
| Navigation | Navigation Compose (type-safe routes) |
| Background work | WorkManager (reminders, daily rollover, step snapshots, widget refresh) |
| Steps | Health Connect (`StepsRecord`) when available and permitted; otherwise the `TYPE_STEP_COUNTER` sensor with snapshots; reconcile per day |
| Charts | Custom Compose `Canvas` charts (no heavy chart library); a lightweight library is allowed only if a chart cannot be built cleanly |
| Barcode (P1) | CameraX + ML Kit Barcode Scanning; product lookup from Open Food Facts via Retrofit/Ktor, cached in Room |
| Widgets | Jetpack Glance |
| Images | Coil (only for optional product photos); foods use emoji or vector glyphs in v1 |
| Fonts | Bricolage Grotesque (headings) and Figtree (body), **bundled** in `res/font` so they work offline |
| Icons | Material Symbols (Rounded), as vector drawables or Compose icons |
| Localization | English (default) and Urdu (RTL) via string resources; no hard-coded text |
| Build | Gradle Kotlin DSL, version catalog, R8 minification, Baseline Profile |
| Quality | JUnit, MockK, Turbine, Compose UI tests, Room tests, screenshot tests (Roborazzi or Paparazzi), detekt/ktlint, Android Lint, GitHub Actions CI |
| Crash/analytics | None in v1. If added later: opt-in only |

**Permissions (request in context, never all at once)**

| Permission | Why | When requested |
|---|---|---|
| `POST_NOTIFICATIONS` (Android 13+) | Reminders | After the user turns on a reminder |
| `ACTIVITY_RECOGNITION` (Android 10+) | Step sensor | When the user enables step tracking |
| Health Connect read steps (and optionally write weight, P2) | Accurate steps | When the user chooses Health Connect |
| `CAMERA` | Barcode scanning | When the user taps Scan (P1) |
| `INTERNET` | Barcode lookups only | Declared; not used for P0 features |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule reminders and step snapshots | Declared |
| `VIBRATE` | Haptics | Declared |

Do **not** use exact alarms, location, contacts, storage-wide access or advertising ID.

---

## 5. Design system

This section is binding. Implement it as a Compose theme (`NutriTrackTheme`) plus a small design-system module of reusable components, and use only theme tokens in screens.

### 5.1 Visual direction

**"Warm, appetising and confident."** A white canvas with one decisive red accent, large rounded numbers, soft tinted cards and thin borders instead of heavy shadows. Think premium health app, not a spreadsheet. Red is the brand colour, so **warnings use amber and success uses green**; red is never used to mean "error" except for destructive actions and form errors.

### 5.2 Colour tokens

| Token | Light | Dark | Use |
|---|---|---|---|
| `primary` | `#D62839` | `#FF6B75` | Buttons, active nav, rings, links |
| `onPrimary` | `#FFFFFF` | `#3A0A0F` | Text/icons on primary |
| `primaryContainer` | `#FCEBED` | `#3A1519` | Tinted cards, ring tracks, chips |
| `onPrimaryContainer` | `#7A0F1B` | `#FFD9DC` | Text on primaryContainer |
| `background` | `#FFFFFF` | `#131011` | Screen background |
| `surface` | `#FFFFFF` | `#1B1617` | Cards, sheets |
| `surfaceVariant` | `#FAF6F6` | `#241E1F` | Alternate sections, inputs |
| `outline` | `#EADFE0` | `#3A3133` | 1 dp card borders, dividers |
| `onSurface` | `#1E1819` | `#F6F0F0` | Primary text |
| `onSurfaceVariant` | `#6B6163` | `#A99FA1` | Secondary text |
| `success` | `#1F8A70` | `#3CC4A0` | Goal reached, positive trends |
| `warning` | `#E0861A` | `#F2A14A` | Over goal, caution |
| `info` | `#2F6FDE` | `#6C9BFF` | Hints |
| `protein` | `#2F6FDE` | `#6C9BFF` | Macro: protein |
| `carbs` | `#E0861A` | `#F2A14A` | Macro: carbs |
| `fat` | `#1F8A70` | `#3CC4A0` | Macro: fat |
| `water` | `#2F9BE0` | `#6CBBF2` | Water tracker |
| `scrim` | `#000000` at 40% | `#000000` at 60% | Modals |

Rules: use the **static brand palette by default**. Offer "Material You dynamic colour" as an optional toggle (P1) that is **off** by default so the red brand stays consistent. Text on any background must meet 4.5:1 contrast.

### 5.3 Typography

Headings use **Bricolage Grotesque** (weights 600, 700, 800). Body and labels use **Figtree** (400, 500, 600, 700). Numbers in rings and stat tiles use Bricolage Grotesque with tabular figures.

| Style | Font | Size / line height | Weight | Use |
|---|---|---|---|---|
| `displayLarge` | Bricolage | 56 / 60 sp | 800 | Hero calorie number |
| `displayMedium` | Bricolage | 40 / 44 sp | 800 | Stat numbers |
| `headlineLarge` | Bricolage | 30 / 36 sp | 700 | Screen titles |
| `headlineMedium` | Bricolage | 24 / 30 sp | 700 | Section titles |
| `titleLarge` | Bricolage | 20 / 26 sp | 700 | Card titles |
| `titleMedium` | Figtree | 16 / 22 sp | 600 | List titles |
| `bodyLarge` | Figtree | 16 / 24 sp | 400 | Body |
| `bodyMedium` | Figtree | 14 / 20 sp | 400 | Secondary body |
| `labelLarge` | Figtree | 14 / 20 sp | 600 | Buttons |
| `labelSmall` | Figtree | 12 / 16 sp | 500 | Captions, chart labels |

All text must scale with the system font size up to 200% without clipping.

### 5.4 Shape, spacing, elevation

| Token | Value |
|---|---|
| Corner radius | `xs` 6 dp, `sm` 10 dp, `md` 16 dp, `lg` 20 dp (cards), `xl` 28 dp (bottom sheets, dialogs), `full` pill |
| Spacing scale (4 dp grid) | 4, 8, 12, 16, 20, 24, 32, 48 dp |
| Screen horizontal padding | 20 dp |
| Card | `surface` fill, 1 dp `outline` border, 20 dp radius, 16 to 20 dp inner padding, **no shadow** |
| Elevated card (hero only) | 0 dp border, tonal elevation 2 dp |
| Minimum touch target | 48 x 48 dp |
| Bottom bar height | 80 dp including the floating action button notch |

### 5.5 Motion

| Interaction | Spec |
|---|---|
| Standard transitions | 250 ms, `FastOutSlowIn` |
| Emphasised (screen enter, sheets) | 400 ms, `EaseOutCubic` |
| Rings and bars | Animate from the previous value over 900 ms, `EaseOutCubic`; play once on first display |
| Numbers | Count up/down over 600 ms when the value changes |
| Add food | Item slides into the meal list; the calorie ring animates; light haptic (`CONFIRM`) |
| Delete | Swipe to reveal, item collapses over 200 ms, Snackbar with **Undo** for 5 s |
| Water | Glass fills with a gentle wave animation; haptic tick per +250 ml |
| Goal reached | One-time confetti burst (max 1.2 s) on the ring, only the first time per day; respects "reduce motion" |
| Navigation | Shared-element transition from a food row to Food Detail; fade-through between bottom tabs |
| Reduce motion | If the system animation scale is 0 or the user enables "Reduce motion", replace all of the above with instant or fade-only changes |

### 5.6 Iconography and illustration

- Material Symbols Rounded, 24 dp, weight 400; filled variant for the active tab.
- Foods use emoji in v1 (consistent with the web version) shown in a 48 dp rounded tile on `primaryContainer`.
- Empty states use a simple line illustration in `onSurfaceVariant` with one red accent shape (create these as vector drawables: empty diary, no search results, no workouts, no weight entries, offline barcode, notifications off).
- **App icon:** adaptive icon, `#D62839` background, white flame-leaf glyph centred in the safe zone; also supply a monochrome themed icon (Android 13+). Splash uses the Android 12+ SplashScreen API with the icon on `background`.

### 5.7 Core component library

Build these once in `designsystem/` and reuse everywhere.

| Component | Specification |
|---|---|
| `NtTopAppBar` | Large collapsing title on main screens, small centred title on detail screens; transparent over `background` |
| `NtBottomBar` | 5 slots: **Home, Diary, Add (centre), Activity, Progress**. Add is a 60 dp red circular FAB docked in a notch, with a "+" icon. Active tab: filled icon plus label in `primary`, with a `primaryContainer` pill indicator |
| `CalorieRing` | Diameter 240 dp on Home (180 dp elsewhere), stroke 18 dp, rounded caps, track `primaryContainer`, progress `primary`; turns `warning` when over goal; centre shows remaining kcal in `displayLarge` and a `labelSmall` caption |
| `MacroProgress` | Pill progress bar 10 dp high, macro colour, label row "Protein 72 / 120 g" |
| `MacroDonut` | 120 dp donut for the share of calories from each macro, with legend |
| `StatTile` | Icon, large number, caption; used for Eaten, Burned, Goal, Steps, Distance |
| `MealCard` | Header with meal emoji, name, total kcal and a "+" button; collapsible list of `FoodRow` items |
| `FoodRow` | 48 dp emoji tile, name, "serving x quantity", kcal right-aligned; swipe-to-delete; tap to edit |
| `QuantityStepper` | Pill with minus, value, plus; step 0.5; range 0.5 to 20; long-press repeats |
| `ServingSelector` | Horizontally scrolling chips for serving units (for example 1 piece, 100 g, 1 cup) |
| `WaterGlass` | Rounded glass shape that fills with animated waves in `water` colour |
| `WeekBarChart` | 7 bars, rounded tops, dashed goal line with label, tap a bar for a tooltip, today highlighted in `primary` |
| `LineChart` | Smooth line with gradient-free fill in `primaryContainer`, dots on points, target line, tap to inspect; used for weight |
| `StreakChip` | Flame icon plus day count in a `primaryContainer` pill |
| `BadgeCard` | Circular badge illustration, title, progress ring when locked |
| `NtChip` | 36 dp filter chip, selected = `primary` fill with `onPrimary` text |
| `NtBottomSheet` | 28 dp top radius, drag handle, used for add food, edit entry, water, weight, workout |
| `NtDialog` | 28 dp radius, used for destructive confirmations (red confirm button) |
| `NtTextField` | Outlined, 16 dp radius, inline error text, supports units suffix (kg, cm) |
| `NtButton` | Filled (primary), tonal, outlined, text; 52 dp height; 14 dp radius; full-width variant |
| `EmptyState` | Illustration, title, one-line explanation, primary action |
| `Skeleton` | Shimmer placeholder used only for lists that read from the network (barcode lookups) |
| `Snackbar` | Bottom, above the nav bar, with Undo action |

### 5.8 Accessibility requirements

- Content descriptions for all icons and charts (charts expose a text summary, for example "Calories this week, average 1,950, goal 2,300").
- Every colour-coded state also uses an icon or text (never colour alone).
- Support font scale to 200%, display size changes, TalkBack, Switch Access, and RTL layouts (Urdu).
- Haptics and sounds respect system settings; confetti respects reduce motion.

---

## 6. Information architecture and navigation

```
Splash
 └─ First launch: Welcome (3 slides) → Health disclaimer → Profile setup (5 steps) → Plan reveal → Home
 └─ Returning: Home

Bottom navigation
 ├─ Home            (dashboard, date strip)
 ├─ Diary           (meals by date)
 ├─ [+] Add         (opens Add Food sheet)
 ├─ Activity        (tabs: Steps | Workouts | Water)
 └─ Progress        (tabs: Week | Month | 3 Months | Year)

From Home top bar:  Profile (avatar) → Settings → subpages
Modal / pushed screens:
 Add Food sheet → Search → Food Detail → Create Custom Food → Barcode Scanner (P1)
 Create Recipe (P1), Edit Entry sheet, Water sheet, Weight log sheet, Workout log sheet,
 Workout Timer, Achievements, Reminders, Goals & Targets, Units, Language, Theme,
 Data & Privacy (export, import, delete), About, Health disclaimer
```

Back behaviour: system back pops one level; from a bottom tab root, back goes to Home, then exits. Predictive back animation enabled. Deep links: `nutritrack://add-food`, `nutritrack://add-water`, `nutritrack://diary/{yyyy-MM-dd}` (used by widgets, shortcuts and notifications).

---

## 7. Feature requirements

Priority: **P0** v1.0, **P1** v1.1, **P2** later. Every row must be implemented and tested as written.

### 7.1 Onboarding and profile (F-ONB)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-ONB-01 | **Splash** using the Android SplashScreen API | P0 | Brand icon on `background`; transitions into the first screen with no white flash in dark mode |
| F-ONB-02 | **Welcome carousel**, 3 slides (Log fast, See your day, Stay private), Skip and Next | P0 | Swipeable, page dots, shown only on first launch; "Get started" goes to disclaimer |
| F-ONB-03 | **Health disclaimer** screen | P0 | States values are approximate and not medical advice; requires a "I understand" checkbox to continue; also reachable from Settings > About |
| F-ONB-04 | **Profile setup wizard**, 5 steps with progress bar and Back | P0 | Step 1 name and gender; step 2 age (or date of birth); step 3 height and weight (with unit toggle); step 4 activity level; step 5 goal (lose, maintain, gain) with goal weight and pace (0.25, 0.5, 0.75, 1 kg per week). Each step validates before enabling Next |
| F-ONB-05 | **Plan reveal** screen | P0 | Shows animated daily calories, macro split, water goal and step goal calculated per section 10; user can tweak the calorie goal with a slider (limits: safe minimum to 6,000 kcal) before confirming |
| F-ONB-06 | **Units** | P0 | Metric by default; imperial option converts kg/lb, cm/ft-in, ml/fl oz everywhere; stored internally in metric |
| F-ONB-07 | **Permission primers** | P0 | Before any permission dialog, a friendly bottom sheet explains why; refusal never blocks the app and shows a fallback (manual steps, no reminders) |
| F-ONB-08 | **Edit profile** | P0 | All fields editable later in Profile; changing weight, activity or goal offers "Recalculate my targets?" |

### 7.2 Home dashboard (F-HOME)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-HOME-01 | Greeting by time of day with the user's first name, plus today's date | P0 | "Good morning, Ahmad" under 12:00, afternoon under 18:00, evening after |
| F-HOME-02 | **Date strip** showing the current week, swipeable by week | P0 | Selecting a date reloads every card for that day; future dates are disabled; a "Today" shortcut appears when off today |
| F-HOME-03 | **Calorie hero card** with `CalorieRing` and Eaten, Burned, Goal tiles | P0 | Remaining = goal - eaten + burned; ring turns amber when over; message line adapts ("640 kcal left", "You are 120 kcal over") |
| F-HOME-04 | **Macro cards** for protein, carbs, fat | P0 | Each shows grams eaten vs target and a progress bar; tap opens the macro breakdown in Progress |
| F-HOME-05 | **Meal tiles** for Breakfast, Lunch, Dinner, Snacks | P0 | Each shows kcal and a "+" that opens Add Food preselected to that meal |
| F-HOME-06 | **Water card** with `WaterGlass` and quick add buttons | P0 | +250 ml and +500 ml buttons, tap the glass for custom; undo via Snackbar |
| F-HOME-07 | **Steps card** with ring, distance and calories | P0 | Updates live while the screen is open; shows "Connect steps" prompt if not enabled |
| F-HOME-08 | **Weekly chart card** (7-day calories with goal line) | P0 | Tap a bar to open that day in Diary |
| F-HOME-09 | **Insight card** (one rule-based tip per day) | P1 | See F-PRG-06; can be dismissed; never repeats the same tip two days in a row |
| F-HOME-10 | **Weight mini card** with sparkline and "Log weight" | P1 | Shows latest weight and change over 7 days |
| F-HOME-11 | **Streak chip** in the top bar | P1 | See F-GAM-01 |
| F-HOME-12 | Pull to refresh recomputes totals and re-reads steps | P0 | Completes in under 1 s |

### 7.3 Food logging (F-LOG)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-LOG-01 | **Add Food sheet** opened by the centre FAB or a meal "+" | P0 | Full-height bottom sheet with search field focused, meal selector chips (default by time of day: before 11:00 Breakfast, before 16:00 Lunch, before 21:00 Dinner, otherwise Snacks), and tabs **Recent, Favourites, My foods, All** |
| F-LOG-02 | **Search** | P0 | Debounced 200 ms; matches name and aliases (including Urdu and Roman Urdu such as "chawal", "aloo"); ranks favourites and recent first; shows result count; highlights match |
| F-LOG-03 | **One-tap quick add** from a result row | P0 | A "+" button logs 1 default serving immediately, shows a Snackbar "Added to Lunch - Undo" |
| F-LOG-04 | **Food Detail** screen | P0 | Large emoji, name, serving selector, quantity stepper, nutrition facts that update live, macro donut, favourite heart, meal chips, "Add to diary" button showing the kcal total |
| F-LOG-05 | **Serving units** | P0 | Each food offers its base serving plus alternates (for example g, 100 g, piece, cup, tbsp); changing the unit converts values correctly; quantity step 0.5 (0.25 for small units) |
| F-LOG-06 | **Quick add calories** | P0 | Enter a name (optional), calories (required) and optional protein, carbs and fat; logs without creating a food |
| F-LOG-07 | **Custom foods** | P0 | Create, edit, delete; fields: name, emoji, category, serving label, serving grams (optional), calories, protein, carbs, fat, optional fibre, sugar, sodium; validation with inline errors; appears under **My foods** and in search |
| F-LOG-08 | **Favourites** | P0 | Heart toggle anywhere a food appears; the Favourites tab lists them |
| F-LOG-09 | **Recent foods** | P0 | Last 30 distinct foods logged, most recent first |
| F-LOG-10 | **Barcode scanner** | P1 | CameraX viewfinder with a scan frame and torch toggle; on a match shows Food Detail; not found offers "Create this product" with the barcode prefilled; works offline for previously cached products and shows a clear offline message otherwise |
| F-LOG-11 | **Meal templates ("Saved meals")** | P1 | Save the foods of a meal as a named template; one tap logs all items; manage in My foods |
| F-LOG-12 | **Recipes** | P1 | Build a recipe from ingredients and a number of servings; per-serving nutrition computed; logs as a single food; editable; see F-REC |
| F-LOG-13 | **Photo logging with AI** | P2 | Out of scope for v1; keep the data model open to it |
| F-LOG-14 | **Voice search** | P2 | Out of scope for v1 |

### 7.4 Food database (F-DB)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-DB-01 | **Seed database** shipped in the app (JSON asset loaded into Room on first run) | P0 | 300+ foods across categories: Fruits, Vegetables, Grains, Protein, Dairy, Snacks, Drinks, Desi, Fast food, Sweets, Breakfast, Soups. Includes at least 70 desi dishes (see Appendix B) |
| F-DB-02 | **Fields per food** | P0 | id, name, aliases (list), category, emoji, serving label, serving grams (nullable), calories, protein, carbs, fat; fibre, sugar and sodium where known |
| F-DB-03 | **Alternate servings** per food | P0 | List of label and multiplier relative to the base serving |
| F-DB-04 | **Search index** | P0 | FTS4 virtual table over name and aliases; rebuilt on seed update |
| F-DB-05 | **Seed versioning** | P0 | A `seedVersion` constant; on app update, new seed foods are added without deleting user data or edited foods |
| F-DB-06 | **Accuracy label** | P0 | Food Detail shows "Approximate values per serving" |
| F-DB-07 | **Online product data** (Open Food Facts) | P1 | Only for barcode lookups; cached results are marked "Community data" |

### 7.5 Diary (F-DIARY)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-DIARY-01 | **Diary screen** for any past or current date | P0 | Date header with previous/next arrows and a calendar picker; future dates disabled |
| F-DIARY-02 | **Four collapsible meal sections** with totals | P0 | Each shows kcal and macros; collapse state remembered per session |
| F-DIARY-03 | **Edit an entry** | P0 | Tap opens the Edit sheet: quantity stepper, serving unit, meal change, delete |
| F-DIARY-04 | **Swipe to delete with Undo** | P0 | Swipe left reveals delete; Snackbar Undo restores the entry in its original position |
| F-DIARY-05 | **Day summary footer** | P0 | Total kcal vs goal progress bar, macros in grams |
| F-DIARY-06 | **Copy from yesterday** and **copy meal to another day** | P0 | Menu action; choose meal and target date; shows how many items were copied |
| F-DIARY-07 | **Multi-select** (long-press) to move, copy or delete several entries | P1 | Contextual top bar with count |
| F-DIARY-08 | **Clear day** | P0 | Destructive confirmation dialog, then Undo Snackbar |
| F-DIARY-09 | **Meal notes** | P2 | Optional note per meal |

### 7.6 Water (F-WAT)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-WAT-01 | **Daily goal** calculated as 35 ml per kg of body weight, rounded to 250 ml, editable (500 to 6,000 ml) | P0 | Shown in ml/L or fl oz per units setting |
| F-WAT-02 | **Quick add** 150, 250, 500 ml and a custom amount; configurable favourite glass size | P0 | Each add is a log entry with a timestamp |
| F-WAT-03 | **Water screen** (tab in Activity) | P0 | Large animated glass, today's total, list of today's entries with swipe to delete, 7-day bar chart |
| F-WAT-04 | **Goal celebration** | P0 | Haptic plus a short message the first time the goal is reached each day |
| F-WAT-05 | **Hydration reminders** | P1 | See F-REM-02 |
| F-WAT-06 | **Quick Settings tile** "Add water" and a launcher shortcut | P1 | Adds the favourite glass size without opening the app |

### 7.7 Steps (F-STP)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-STP-01 | **Step source selection** | P0 | Settings and first-use sheet: Health Connect (preferred if installed and permitted), Phone sensor, or Manual only |
| F-STP-02 | **Sensor tracking** | P0 | Uses `TYPE_STEP_COUNTER`; WorkManager snapshots at least every 15 minutes and on boot; handles the sensor reset after reboot and the day rollover at local midnight without double counting |
| F-STP-03 | **Health Connect** | P0 | Reads `StepsRecord` for the day; shows a permission rationale; if Health Connect is unavailable falls back to the sensor |
| F-STP-04 | **Manual entry** | P0 | Add steps (+500, +1,000, custom) or set the exact total for a day; manual edits override automatic values for that day and are marked "Edited" |
| F-STP-05 | **Steps screen** (tab in Activity) | P0 | Step ring vs goal, distance (stride estimated from height), calories, active minutes estimate, goal streak, 7-day chart with goal line, tap a bar for a day |
| F-STP-06 | **Step goal** | P0 | Default 8,000 (sedentary), 10,000 (light), 10,000 (moderate), 12,000 (active); editable 1,000 to 50,000 |
| F-STP-07 | **Goal reached** | P0 | One-time notification and in-app celebration per day (respecting notification settings) |
| F-STP-08 | **Battery** | P0 | No continuous wake lock; no foreground service in v1; step data comes from the cumulative sensor counter |

### 7.8 Workouts and exercise (F-EXE)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-EXE-01 | **Log a workout** | P0 | Choose an activity (walking, running, cycling, swimming, gym, football, cricket, yoga, hiking, HIIT, badminton, custom), duration in minutes, optional intensity (light, moderate, vigorous), time and note |
| F-EXE-02 | **Calories burned** | P0 | MET x 3.5 x weight kg / 200 x minutes, with intensity adjusting MET (light x0.8, moderate x1.0, vigorous x1.2); editable manually |
| F-EXE-03 | **Workout timer** | P0 | Start, pause, resume, reset; continues with the screen off via a persistent timestamp (no service needed); ongoing notification with elapsed time while running; "Finish and log" saves the workout |
| F-EXE-04 | **Workouts screen** (tab in Activity) | P0 | Today's list with total minutes and kcal, history grouped by day, swipe to delete with Undo |
| F-EXE-05 | **Burned calories** feed Home as the Burned tile | P0 | Burned = workouts + steps estimate (steps estimate excludes minutes already covered by a walking or running workout to avoid double counting) |
| F-EXE-06 | **Favourite activities** and custom MET | P1 | Save a custom activity with its own MET |

### 7.9 Weight and body (F-WGT)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-WGT-01 | **Log weight** (sheet) with date and optional note | P0 | Range 20 to 400 kg; latest weight updates the profile weight and offers target recalculation when it changed by 2 kg or more |
| F-WGT-02 | **Weight chart** | P0 | `LineChart` with 7-day moving average, goal weight line, range tabs (1M, 3M, 6M, 1Y, All) |
| F-WGT-03 | **Progress summary** | P0 | Start weight, current, goal, change, remaining, projected goal date at the chosen pace |
| F-WGT-04 | **BMI** | P0 | Value, category, and a scale with a marker; includes a note that BMI is a rough guide |
| F-WGT-05 | **Weekly weigh-in reminder** | P1 | See F-REM-04 |
| F-WGT-06 | **Body measurements** (waist, chest, hips, arm) | P2 | Optional log and chart |
| F-WGT-07 | **Sync weight with Health Connect** | P2 | Optional write and read |

### 7.10 Progress and insights (F-PRG)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-PRG-01 | **Progress screen** with range tabs Week, Month, 3 Months, Year | P0 | All charts respect the selected range |
| F-PRG-02 | **Calories chart** | P0 | Bars per day (or per week for Year) with goal line and average |
| F-PRG-03 | **Macro distribution** | P0 | Donut for average split and a stacked bar over time; compares with target split |
| F-PRG-04 | **Summary tiles** | P0 | Average calories, days logged, days within goal (within 10%), average steps, average water, best streak |
| F-PRG-05 | **Weight section** | P0 | Mini version of the weight chart with link to the full screen |
| F-PRG-06 | **Rule-based insights** | P1 | Local rules only, for example: protein under 80% of target on 3 of the last 7 days; over calorie goal 3 days in a row; water under 60% of goal on 4 of 7 days; steps trending up or down by 15%; weight trend vs planned pace. Each insight has a title, one sentence and an action button |
| F-PRG-07 | **Weekly report** | P1 | Sunday evening card and notification summarising the week; shareable as an image |
| F-PRG-08 | **Export a day or range as a shareable image** | P2 | Branded card |

### 7.11 Streaks and achievements (F-GAM)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-GAM-01 | **Logging streak** | P1 | Counts consecutive days with at least one food entry; shown as `StreakChip`; breaks at local midnight; best streak stored |
| F-GAM-02 | **Achievements screen** | P1 | Grid of `BadgeCard`: locked badges show a progress ring |
| F-GAM-03 | **Badge set** | P1 | First Bite (first log), 3, 7, 14, 30, 100-day streaks, Hydration Hero (water goal 7 days), Step Master (10,000 steps 5 days), Protein Pro (protein goal 5 days), On Target (within calorie goal 7 days), Weigh-in (first weight), Goal Getter (reach goal weight), Explorer (log 50 different foods), Chef (first recipe) |
| F-GAM-04 | **Unlock moment** | P1 | Bottom sheet with the badge and a small confetti burst (respecting reduce motion) |

### 7.12 Reminders and notifications (F-REM)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-REM-01 | **Meal reminders** | P1 | Per meal toggle and time (defaults 08:00, 13:00, 19:30); skipped if that meal is already logged |
| F-REM-02 | **Hydration reminders** | P1 | Interval (1 to 4 hours) between a start and end time; skipped once the goal is reached; quiet hours respected |
| F-REM-03 | **Step goal and evening nudge** | P1 | Optional 18:00 nudge if steps are under 50% of goal |
| F-REM-04 | **Weigh-in reminder** | P1 | Weekly on a chosen weekday and time |
| F-REM-05 | **Weekly report notification** | P1 | Sunday 19:00, toggleable |
| F-REM-06 | **Notification channels** | P1 | Separate channels: Meal reminders, Hydration, Goals and streaks, Weekly report; each controllable in system settings |
| F-REM-07 | **Reliability** | P1 | Schedules survive reboot and app update (re-scheduled on `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`); no exact alarms; copy is friendly and never shames the user |

### 7.13 Widgets and shortcuts (F-WID)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-WID-01 | **Calories widget** 2x2 | P1 | Ring with remaining kcal; tap opens Home |
| F-WID-02 | **Daily summary widget** 4x2 | P1 | Ring, protein, carbs, fat bars, water, and two buttons: Log food and +250 ml |
| F-WID-03 | **Water widget** 2x1 | P1 | Total vs goal and a +250 ml button that works without opening the app |
| F-WID-04 | **Widget theming** | P1 | Follows light/dark and matches brand colours; refreshes on data change and at least every 30 minutes |
| F-WID-05 | **Launcher shortcuts** | P1 | Long-press the app icon: Log food, Add water, Log weight |

### 7.14 Settings, data and privacy (F-SET)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-SET-01 | **Profile and goals** | P0 | Edit profile, calorie goal, macro split preset (Balanced 50/25/25, High protein 40/30/30, Low carb 30/35/35, Custom), water goal, step goal |
| F-SET-02 | **Appearance** | P0 | Theme: System, Light, Dark; "Reduce motion" toggle; dynamic colour toggle (P1, off by default) |
| F-SET-03 | **Units** | P0 | Weight, height, water, energy (kcal or kJ) |
| F-SET-04 | **Language** | P1 | English and Urdu; Urdu mirrors layout (RTL) and uses Urdu numerals only if the user selects them |
| F-SET-05 | **Notifications** | P1 | Entry to all reminder settings |
| F-SET-06 | **Step source** | P0 | Choose Health Connect, Sensor or Manual |
| F-SET-07 | **Export data** | P0 | Export everything as JSON and diary as CSV via the system share/save dialog (Storage Access Framework) |
| F-SET-08 | **Import / restore** | P0 | Pick a previously exported JSON; validates schema version; offers Merge or Replace; shows a summary before applying |
| F-SET-09 | **Delete all data** | P0 | Two-step confirmation (dialog then type DELETE); wipes Room, DataStore and scheduled work, then returns to onboarding |
| F-SET-10 | **App lock** | P2 | Biometric or device credential on app open |
| F-SET-11 | **About** | P0 | Version, health disclaimer, privacy policy, open-source licences, rate the app, send feedback (email intent) |
| F-SET-12 | **Backup** | P0 | Auto Backup enabled with data extraction rules that include the database and settings; manual file backup via F-SET-07 |
| F-SET-13 | **Cloud sync** (Google sign-in) | P2 | Out of scope for v1 |

### 7.15 Cross-cutting requirements (F-X)

| ID | Requirement | Pri | Acceptance criteria |
|---|---|---|---|
| F-X-01 | **Offline-first** | P0 | Every P0 feature works in airplane mode |
| F-X-02 | **Day rollover** | P0 | At local midnight (and on timezone change) all "today" values reset correctly; stored dates are ISO local dates |
| F-X-03 | **Error handling** | P0 | No crashes on bad input; database errors show a friendly Snackbar and are logged locally; destructive actions always confirm or offer Undo |
| F-X-04 | **Empty states** | P0 | Every list has an `EmptyState` with a helpful action |
| F-X-05 | **Adaptive layout** | P1 | Compact width: bottom bar. Medium/expanded: navigation rail, Home in two columns, Diary list plus detail pane; support foldables and landscape |
| F-X-06 | **Accessibility** | P0 | See section 5.8 |
| F-X-07 | **Localization-ready** | P0 | All text in string resources, plurals used, no concatenated sentences, dates and numbers formatted by locale |
| F-X-08 | **Haptics** | P0 | Light haptics on add, undo, goal reached; user can disable |
| F-X-09 | **Input safety** | P0 | Numeric fields use the right keyboard, reject impossible values, and never accept negative amounts |

---

## 8. Screen-by-screen UI specification

All dimensions in dp unless stated. All screens use `background`, 20 dp horizontal padding, and support light, dark, RTL and font scale 200%. Each screen must have loading-free local data, an empty state, and an error state where relevant.

### 8.1 Splash and Welcome

- **Splash:** brand icon centred on `background`, 400 ms scale-in, then fade to the next screen.
- **Welcome carousel (3 slides):** each slide has a large vector illustration (top 45% of screen), a `headlineLarge` title, two lines of `bodyLarge` text, page dots and a full-width **Next** button (**Get started** on the last slide). **Skip** is a text button at the top end.
  1. "Log meals in seconds": illustration of a plate and a "+" button.
  2. "See your whole day": illustration of rings.
  3. "Private by design": illustration of a phone with a lock.

### 8.2 Disclaimer and profile setup

- **Disclaimer:** shield illustration, title "A quick note", short text, checkbox "I understand NutriTrack is not medical advice", **Continue** (disabled until checked).
- **Wizard layout:** top row with Back arrow and a 5-segment progress bar (active segments in `primary`); big question in `headlineLarge`; content; bottom-pinned **Next** button (52 dp, full width). Keyboard avoids the button.
  - Step 1: name field; gender as two large selectable cards (Male, Female), each with an icon.
  - Step 2: age with a wheel picker (14 to 100).
  - Step 3: height and weight with two wheel pickers and a kg/lb, cm/ft toggle.
  - Step 4: four activity cards stacked, each with an icon, title and one line (Sedentary, Light, Moderate, Active).
  - Step 5: goal cards (Lose, Maintain, Gain); when Lose or Gain is chosen, show goal weight field and a pace selector of four chips (0.25, 0.5, 0.75, 1 kg/week) with a recommended badge on 0.5.
- **Plan reveal:** full-screen `primaryContainer` top area with confetti-free, calm count-up of the daily calories in `displayLarge`; below, three `StatTile`s (Protein, Carbs, Fat in grams), a water goal tile and a step goal tile; a slider to fine-tune calories; **Start tracking** button.

### 8.3 Home

```
┌──────────────────────────────────────┐
│ Good evening, Ahmad        🔥12  (A) │  top bar: greeting, streak chip, avatar
│ Mon 12 Oct                           │
│ ‹  M  T  W  T  F  S  S  ›            │  date strip (selected = red pill)
│                                      │
│ ┌──────────────────────────────────┐ │
│ │            ◜‾‾‾‾‾◝               │ │
│ │          ◜   1,642   ◝           │ │  CalorieRing 240 dp
│ │          ◟  kcal left ◞          │ │  (number: displayLarge)
│ │            ◟_____◞               │ │
│ │  Eaten 840   Burned 152   Goal 2,330 │  3 StatTiles
│ └──────────────────────────────────┘ │
│  Protein      Carbs        Fat       │  3 macro cards in a row
│  42/146 g     96/292 g     31/65 g   │
│  ▓▓░░░░       ▓▓▓░░░       ▓▓░░░░     │
│                                      │
│  Meals                               │
│  ┌────────────┐ ┌────────────┐       │
│  │🍳 Breakfast│ │🥗 Lunch    │       │  2 x 2 meal tiles with "+"
│  │ 350 kcal + │ │ 490 kcal + │       │
│  └────────────┘ └────────────┘       │
│  ┌────────────┐ ┌────────────┐       │
│  │🍽️ Dinner  +│ │🍿 Snacks  +│       │
│  └────────────┘ └────────────┘       │
│  ┌────────────────┐ ┌─────────────┐  │
│  │ 💧 Water 1.5 L │ │ 🚶 3,800    │  │  water + steps cards side by side
│  │ [+250] [+500]  │ │  steps ring │  │
│  └────────────────┘ └─────────────┘  │
│  Last 7 days  (bar chart + goal line)│
│  Insight card · Weight card          │
│                                      │
│ Home  Diary   (+)  Activity Progress │  bottom bar with docked FAB
└──────────────────────────────────────┘
```

- Top bar is not sticky; the bottom bar always is. The date strip is sticky under the top bar when scrolling.
- The hero card has `primaryContainer` at 40% as an elevated tonal surface, a subtle radial glow is **not** allowed (keep it flat and clean).
- Meal tiles show a thin progress arc of that meal's share of the day's calories.
- Over goal: ring and remaining number switch to `warning`, caption changes to "kcal over".
- First-time empty state: ring at 0 with the caption "Log your first meal" and a pulsing "+" FAB hint (one time).

### 8.4 Add Food sheet and Search

```
┌──────────────────────────────────────┐
│               ───                    │  drag handle
│ 🔍 Search foods, e.g. biryani    ⌁📷 │  search field + barcode button (P1)
│ ( Breakfast )( Lunch )( Dinner )( Snacks )   meal chips
│ Recent │ Favourites │ My foods │ All  │  tabs
│ ┌──────────────────────────────────┐ │
│ │🍛 Chicken biryani    490 kcal  ＋│ │  FoodRow with quick add
│ │   1 plate                        │ │
│ ├──────────────────────────────────┤ │
│ │🥚 Boiled egg          78 kcal  ＋│ │
│ └──────────────────────────────────┘ │
│  Quick add calories    Create food   │  bottom actions
└──────────────────────────────────────┘
```

- Sheet opens to 92% height with a 400 ms emphasised slide-up; search auto-focuses.
- No-results state: illustration, "No match for 'xyz'", buttons **Create custom food** and **Quick add calories**.
- Tapping a row opens **Food Detail** with a shared-element transition from the emoji tile.

### 8.5 Food Detail

- Top: back arrow, favourite heart, overflow menu (edit if custom).
- Hero: 96 dp emoji tile on `primaryContainer`, food name in `headlineLarge`, category chip.
- `ServingSelector` chips, then `QuantityStepper` (centred, large).
- **Calories** in `displayMedium` with "kcal", then a `MacroDonut` and three macro rows (grams and % of calories).
- Collapsible **More nutrition** (fibre, sugar, sodium) when available.
- Footer note "Approximate values per serving".
- Sticky bottom bar: meal chips and a full-width **Add to diary (490 kcal)** button; success shows a Snackbar and returns to the previous screen.

### 8.6 Diary

- Top: title "Diary", date with arrows, calendar icon, overflow (Copy from yesterday, Clear day).
- Summary card: kcal eaten vs goal progress bar and macro grams.
- Four `MealCard`s (collapsible, chevron). Empty meal shows a dashed outline row "Add food" with "+".
- Swipe left on a row: red delete background with trash icon; on release the row collapses and Undo appears.
- Long-press (P1): multi-select mode with a contextual top bar (count, copy, move, delete).

### 8.7 Activity (tabs: Steps, Workouts, Water)

- Top: title and a segmented tab row.
- **Steps tab:** step ring (200 dp), distance and calories tiles, a source chip ("Health Connect", "Phone sensor" or "Manual"), quick buttons +500, +1,000, "Set total", 7-day bar chart with goal line, streak tile.
- **Workouts tab:** today's total card (minutes and kcal), **Start timer** primary button and **Log workout** tonal button, list of workouts.
- **Water tab:** large `WaterGlass` (220 dp), "1.5 / 2.5 L", row of quick add chips (150, 250, 500, custom), list of today's entries, 7-day chart.
- **Workout timer screen:** full screen, huge time in `displayLarge` (tabular), activity picker chip, Pause/Resume and Finish buttons, ongoing notification while running.

### 8.8 Progress

- Segmented range tabs: Week, Month, 3M, Year.
- Cards in order: summary tiles (2 x 3 grid), calories chart, macro distribution, weight, steps, water, adherence ring ("Days within goal 5 of 7"), insights list (P1), weekly report (P1).
- Charts are tappable; a tooltip shows the date and value; long-press scrubs across points.

### 8.9 Weight

- Opened from Progress or the Home weight card.
- Latest weight in `displayMedium`, change chip (green when moving toward goal, amber otherwise), `LineChart`, range chips, history list with swipe to delete, FAB "Log weight" opens a sheet with a number pad style field, date and note.

### 8.10 Profile and Settings

- **Profile:** large avatar with initials on `primaryContainer`, name, summary line (goal, current weight), a row of three stat tiles (Streak, Days logged, Badges), then list sections.
- **Settings list** (grouped cards with 1 dp border): Profile and goals, Appearance, Units, Language, Notifications, Step source, Data and privacy, Achievements, About. Each row has a leading icon in a `primaryContainer` circle, title, optional value, and chevron.
- **Data and privacy:** Export JSON, Export CSV, Import, Delete all data (red text), plus a short privacy statement ("Your data never leaves this phone").

### 8.11 Reminders, Achievements, Goals

- **Reminders:** grouped switches with time pickers, a master toggle, quiet hours selector, and a banner if notification permission is off ("Turn on notifications").
- **Achievements:** 3-column grid of `BadgeCard`s with a header showing unlocked count; tap shows details.
- **Goals and targets:** calorie goal slider and input, macro preset chips with a live donut preview, water and step goals, "Recalculate from my body data" button with a before/after summary.

### 8.12 Widgets

- **2x2 Calories:** ring with remaining kcal and "left" caption on `surface`, 20 dp radius.
- **4x2 Summary:** ring on the left; protein, carbs, fat mini bars and water on the right; buttons **Log food** and **+250 ml** along the bottom.
- **2x1 Water:** drop icon, "1.5 / 2.5 L", **+250** button.

### 8.13 Required screen states

| State | Behaviour |
|---|---|
| Loading | Local data shows instantly; use `Skeleton` only for barcode network lookups |
| Empty | Illustration plus action (every list) |
| Error | Snackbar or inline message with a Retry action; never a blank screen |
| Offline | Barcode lookup shows "You are offline. Cached products still work." |
| Permission denied | Inline card explaining what is unavailable with an **Open settings** button |
| Dark mode | Every screen verified with screenshot tests |
| Urdu / RTL | Mirrored layout, charts keep time left-to-right unless the locale requires otherwise |

---

## 9. Data model (Room)

All dates are ISO local dates (`yyyy-MM-dd`) stored as text; timestamps are epoch milliseconds (UTC). Energy and mass are stored in metric (kcal, g, ml, kg, cm); conversion happens only in the UI layer.

| Entity | Fields | Notes |
|---|---|---|
| `UserProfile` (single row, id = 1) | name, gender, dateOfBirth or birthYear, heightCm, weightKg, goalWeightKg, activityLevel, goalType (LOSE, MAINTAIN, GAIN), paceKgPerWeek, calorieGoal, macroPreset, proteinGoalG, carbsGoalG, fatGoalG, waterGoalMl, stepGoal, createdAt, updatedAt | Updated whenever the user edits goals |
| `Food` | id (UUID string, seed ids prefixed `seed_`), name, aliases (JSON list), category, emoji, servingLabel, servingGrams?, calories, protein, carbs, fat, fiber?, sugar?, sodiumMg?, barcode?, source (SEED, USER, ONLINE), isFavorite, lastUsedAt?, isDeleted, createdAt | Index on name, category, barcode, isFavorite |
| `FoodFts` (FTS4) | name, aliases | Content table = `Food` |
| `ServingOption` | id, foodId, label, multiplier | Multiplier relative to the base serving |
| `DiaryEntry` | id, date, meal (BREAKFAST, LUNCH, DINNER, SNACKS), foodId?, nameSnapshot, emojiSnapshot, servingLabelSnapshot, quantity, calories, protein, carbs, fat, fiber?, sugar?, sodiumMg?, isQuickAdd, createdAt | Nutrients are **snapshots of the entry total**, so editing a food later never rewrites history. Index on date |
| `Recipe` | id, name, emoji, servings, createdAt | P1 |
| `RecipeIngredient` | id, recipeId, foodId, quantity, servingOptionId? | P1 |
| `MealTemplate`, `MealTemplateItem` | name, items (foodId, quantity, meal) | P1 |
| `WaterLog` | id, date, timestamp, amountMl | Index on date |
| `StepDay` | date (PK), sensorSteps, healthConnectSteps, manualSteps?, source, updatedAt | Effective steps = manual if present, else the selected source |
| `StepSnapshot` | id, timestamp, counterValue | Raw cumulative sensor values for delta calculation |
| `Workout` | id, date, startTime?, activityKey, name, minutes, intensity, met, calories, note, createdAt | Index on date |
| `WeightLog` | id, date, weightKg, note | One entry per day (latest wins) |
| `Achievement` | key, unlockedAt?, progress | P1 |
| `AppMeta` | seedVersionApplied, lastRolloverDate, schemaExportVersion | Housekeeping |

**DataStore (Preferences):** onboardingDone, themeMode, dynamicColor, reduceMotion, haptics, units (weight, height, volume, energy), language, stepSource, favouriteGlassMl, reminder settings (per type: enabled, times, interval, quiet hours), lastInsightId.

**Export format (JSON):** `{ "schemaVersion": 1, "exportedAt": "...", "profile": {...}, "foods": [user foods only], "diary": [...], "water": [...], "steps": [...], "workouts": [...], "weight": [...], "achievements": [...], "settings": {...} }`. The importer must reject unknown major versions.

---

## 10. Business rules and formulas

Implement as pure Kotlin functions in `domain/calc` with unit tests for every rule.

| Rule | Definition |
|---|---|
| **BMR** (Mifflin-St Jeor) | `10 x kg + 6.25 x cm - 5 x age + 5` (male) or `- 161` (female) |
| **Activity factor** | Sedentary 1.2, Light 1.375, Moderate 1.55, Active 1.725 |
| **TDEE** | BMR x activity factor |
| **Pace adjustment** | `dailyDelta = paceKgPerWeek x 7,700 / 7` kcal. LOSE: TDEE - dailyDelta. GAIN: TDEE + dailyDelta. MAINTAIN: TDEE |
| **Safety limits** | Deficit capped at 25% of TDEE; floor 1,200 kcal (female) or 1,500 kcal (male); ceiling 6,000 kcal; if a limit changes the result, show an explanation on the plan screen |
| **Rounding** | Goal rounded to the nearest 10 kcal; displayed calories rounded to whole numbers; macros shown to 1 decimal in detail and whole grams on cards |
| **Macro presets** | Balanced 50/25/25, High protein 40/30/30, Low carb 30/35/35 (carbs/protein/fat shares of calories); grams = calories x share / 4 (carbs, protein) or / 9 (fat). Custom lets the user set percentages that must total 100 |
| **Water goal** | `weightKg x 35 ml`, rounded to the nearest 250 ml, minimum 1,500 ml |
| **Step goal default** | By activity: Sedentary 8,000, Light 10,000, Moderate 10,000, Active 12,000 |
| **Calories remaining** | `goal - eaten + burned` |
| **Burned** | `workoutKcal + stepKcal`; `stepKcal = steps x weightKg x 0.00057`, excluding steps from minutes already covered by walking or running workouts (estimate 100 steps per minute) |
| **Exercise kcal** | `MET x 3.5 x weightKg / 200 x minutes`; intensity multiplier light 0.8, moderate 1.0, vigorous 1.2 |
| **Distance** | `steps x (heightCm x 0.415) / 100,000` km |
| **BMI** | `kg / (m x m)`; categories under 18.5 Underweight, under 25 Healthy, under 30 Overweight, 30 and over Obese |
| **Within goal** | A day counts as "within goal" when eaten is between 90% and 110% of the goal |
| **Streak** | Consecutive local days with at least one diary entry; today does not break the streak until midnight passes |
| **Weight trend** | 7-day moving average over available entries |
| **Projected goal date** | `remainingKg / paceKgPerWeek` weeks from today; hidden if the trend is moving away from the goal |
| **Day rollover** | Recompute "today" on `ACTION_DATE_CHANGED`, `ACTION_TIMEZONE_CHANGED` and app resume; never rely on a cached date |
| **Serving conversion** | Entry nutrients = food nutrients x serving multiplier x quantity |

---

## 11. Architecture and project structure

Single Gradle module `app` (split into modules later if needed), packages by feature with layered folders inside.

```
com.nutritrack.app
 ├─ NutriTrackApp.kt              Hilt application, WorkManager init
 ├─ MainActivity.kt               Single activity, edge-to-edge, NavHost
 ├─ designsystem/                 Theme, colours, typography, shapes, all Nt* components, charts
 ├─ core/
 │   ├─ common/                   Result types, dispatchers, date/time utils, units
 │   ├─ database/                 Room DB, DAOs, entities, migrations, FTS, seed loader
 │   ├─ datastore/                Preferences repositories
 │   ├─ health/                   Health Connect + sensor step sources
 │   └─ notifications/            Channels, schedulers, workers
 ├─ domain/
 │   ├─ model/                    Plain Kotlin models
 │   ├─ calc/                     Pure calculators (BMR, TDEE, macros, BMI, MET, streaks)
 │   ├─ repository/               Interfaces
 │   └─ usecase/                  LogFoodUseCase, GetDaySummaryUseCase, etc.
 ├─ data/                         Repository implementations and mappers
 ├─ feature/
 │   ├─ onboarding/  home/  diary/  addfood/  fooddetail/  customfood/  scanner/
 │   ├─ activity/ (steps, workouts, water, timer)  progress/  weight/
 │   ├─ profile/  settings/  reminders/  achievements/  recipes/
 └─ widget/                       Glance widgets and receivers
```

Rules: ViewModels expose a single immutable `UiState` via `StateFlow` and take `UiEvent`s; side effects use one-shot channels; composables are stateless where possible and previewable; no business logic in composables; repositories return `Flow`; all database work off the main thread; no `GlobalScope`; dependency versions live in `libs.versions.toml`.

---

## 12. Non-functional requirements

| Area | Requirement |
|---|---|
| Performance | Cold start under 2 s; Home first frame under 500 ms after Room is ready; search results within 100 ms for 1,000+ foods; no jank in Home or Diary scroll; Baseline Profile and R8 enabled |
| Reliability | No data loss on process death; entries are written immediately (no "save" step); database migrations tested from every released schema |
| Offline | All P0 features offline; barcode lookup the only network call |
| Privacy | No account, no analytics, no ads, no third-party SDK that collects data; Play Data Safety: "No data collected or shared"; privacy policy page included |
| Security | Release builds not debuggable; `allowBackup` governed by data extraction rules; exported files are written only to a location the user picks; no sensitive data in logs |
| Battery | No foreground service or wake locks in v1; WorkManager only; sensor snapshots are cheap |
| Compatibility | Android 8.0 to latest; phones, tablets, foldables; portrait first, landscape supported; dark and light; RTL |
| Size | App bundle under 25 MB; seed database under 1 MB |
| Accessibility | See 5.8; verified with TalkBack on the P0 flows |
| Localization | English and Urdu; strings externalised from day one |

---

## 13. Testing and quality plan

| Level | What | Tooling |
|---|---|---|
| Unit | Every calculator in section 10 (including edge cases: minimum calories, imperial conversion, midnight rollover, streak breaks), use cases, mappers, ViewModels | JUnit, MockK, Turbine |
| Database | DAO queries, FTS search ranking, migrations, seed update without data loss | Room testing, instrumented tests |
| UI | Onboarding, log a food (search and quick add), edit and undo delete, add water, set steps manually, log workout, log weight, export and import, change theme, delete all data | Compose UI tests |
| Screenshot | Every screen in light, dark, Urdu RTL and 200% font scale | Roborazzi or Paparazzi |
| Performance | Startup and Home scroll | Macrobenchmark |
| Static analysis | Formatting and code smells | ktlint, detekt, Android Lint (warnings as errors for new code) |
| CI | Build, lint, unit and screenshot tests on every push | GitHub Actions |
| Manual QA | Pixel-class phone, a low-end phone, a tablet; Android 8, 10, 13, 14 and latest; airplane mode; low storage; changing system time zone and date; TalkBack | Checklist in `docs/QA.md` |

**Definition of done for any task:** builds with no warnings from new code, tests added and passing, matches the screen spec in light and dark, works with 200% font scale, no hard-coded strings or colours, requirement IDs referenced in the commit message.

---

## 14. Release and Google Play checklist

- App name NutriTrack; unique package name; versionCode and versionName scheme (`1.0.0`); signed release bundle with a securely stored keystore (never commit it).
- Store listing: short and full description, 8 phone screenshots (light and dark), a 1024 x 500 feature graphic, 512 x 512 icon, category Health & Fitness.
- Policies: public privacy policy URL, Data Safety form (no data collected), content rating questionnaire, health disclaimer in the description, Health Connect permission declaration (only if Health Connect is included in the release), notification permission rationale.
- Pre-launch: internal testing track, pre-launch report review, crash-free check on at least 10 devices or emulators.
- If the app is only for personal use, a signed APK installed via adb or a file is enough and the Play steps can be skipped.

---

## 15. Assumptions and open decisions

| Topic | Default used in this PRD | Change if |
|---|---|---|
| Technology | Native Kotlin and Jetpack Compose for the best Android experience, Health Connect and widgets | You would rather use Flutter or React Native; the requirements, design system and flows still apply |
| Accounts and cloud sync | None in v1 | You want multi-device sync (P2) |
| Languages | English v1.0, Urdu in v1.1 | Urdu is needed at launch |
| Monetisation | None | You plan Pro features later |
| Food data accuracy | Approximate seed values; Open Food Facts for barcodes | You want a licensed nutrition database |
| Publishing | Possible Google Play release | Personal use only |

---

## 16. Build plan for Antigravity (phases and prompts)

Run each phase as a separate Plan-mode task. After each phase: build, run tests, install on an emulator or phone, compare with section 8, then commit.

| Phase | Scope | Done when |
|---|---|---|
| **0. Setup** | Project, Gradle Kotlin DSL, version catalog, Hilt, Compose, Navigation, detekt/ktlint, CI, folder structure (section 11) | App launches to a blank themed screen; CI green |
| **1. Design system** | `NutriTrackTheme` (light, dark, typography, shapes), bundled fonts, all `Nt*` components (5.7), a debug-only **Component Gallery** screen | Gallery matches section 5 in light and dark; screenshot tests added |
| **2. Data and domain** | Room entities, DAOs, FTS, seed JSON loader (start with 70 foods), DataStore, repositories, all calculators with unit tests | All section 10 rules covered by passing tests; seed loads once; search works |
| **3. Onboarding and profile** | F-ONB-01..08 | A new user completes setup and lands on an empty Home with correct targets |
| **4. Core loop** | Home (F-HOME-01..08, 12), Add Food and search (F-LOG-01..09), Food Detail, Diary (F-DIARY-01..06, 08), custom foods, quick add | Log, edit, undo and delete foods; Home and Diary update live; matches wireframes |
| **5. Water, steps, workouts** | F-WAT-01..04, F-STP-01..08, F-EXE-01..05 incl. timer | Water and workouts logged; steps via sensor, Health Connect and manual; correct rollover at midnight |
| **6. Weight, progress, settings, data** | F-WGT-01..04, F-PRG-01..05, F-SET-01..03, 06..09, 11, 12 | Charts correct for all ranges; export then import restores everything; delete all works |
| **7. Polish** | Motion (5.5), haptics, empty states, accessibility pass, adaptive layouts (F-X-05), screenshot tests for dark, RTL and 200% font | All screens pass the checklist; no jank in Macrobenchmark |
| **8. P1 features** | Barcode (F-LOG-10), saved meals and recipes, reminders, widgets and shortcuts, achievements and streaks, insights and weekly report, Urdu, dynamic colour | Each P1 requirement ID passes its criteria |
| **9. Release hardening** | Baseline Profile, R8 check, migration tests, final QA, store assets, signed bundle | Section 14 complete |

### Kickoff prompt for Phase 0 (paste into Antigravity, Plan mode)

```
Read docs/PRD.md fully. This is the source of truth for the NutriTrack Android app.
Task: Phase 0 (Setup) only.
- Create a native Android project (Kotlin, Jetpack Compose, Material 3), package com.nutritrack.app, minSdk 26, target latest stable.
- Use Gradle Kotlin DSL with a version catalog, Hilt, Navigation Compose, Room, DataStore, WorkManager, coroutines.
- Create the package structure from section 11 with empty placeholder files and a single-activity NavHost.
- Add ktlint, detekt, a GitHub Actions workflow that builds and runs lint and unit tests.
- Do not implement features or add libraries that are not listed in the PRD.
First produce an implementation plan and task list for my approval. After implementing, run ./gradlew assembleDebug and the tests and report the results.
```

### Template prompt for later phases

```
Read docs/PRD.md. Task: Phase <N> (<name>) only, covering requirement IDs <list>.
Follow the design tokens in section 5 and the screen specs in section 8 exactly.
No hard-coded strings, colours or dimensions; use theme tokens and string resources.
Add unit tests for all logic and Compose/screenshot tests for new screens (light, dark).
Plan first. After implementing, run build, lint and tests, then list which acceptance criteria are met and any that are not.
Do not start the next phase.
```

### Rules file text (add to the Antigravity workspace rules)

```
- docs/PRD.md is the source of truth. Do not add features, libraries or permissions that it does not list.
- Kotlin + Jetpack Compose + Material 3, MVVM with StateFlow, Hilt, Room, DataStore, WorkManager.
- Brand: red #D62839 and white with full dark theme. Use only tokens from NutriTrackTheme.
- No hard-coded strings (use string resources, English and Urdu), colours or dimensions.
- All calculations live in domain/calc and are unit tested.
- Offline-first. No analytics, ads or network calls except barcode lookups (P1).
- Every screen: empty state, dark mode, 200% font scale, TalkBack labels, 48 dp touch targets.
- Never commit keystores, API keys or local.properties.
```

---

## 17. Risks

| Risk | Mitigation |
|---|---|
| Step counting accuracy and OEM battery restrictions | Prefer Health Connect; sensor snapshots; manual override; clear "Edited" label |
| Reminder reliability on aggressive OEM battery savers | WorkManager, re-schedule on boot and update, in-app help text for battery settings |
| Seed food values are approximate | Label as approximate; verify desi dishes against a trusted composition table before release; allow users to edit foods |
| Scope is large for one builder | Strict P0 then P1 phases; do not start Phase 8 until Phase 7 is stable |
| Agent adds unwanted libraries or features | Plan-mode review, rules file, per-phase scope |
| Health claims and policy compliance | Disclaimer, no medical advice language, Data Safety honesty |

---

## Appendix A. Notification copy (friendly, never shaming)

| Type | Example text |
|---|---|
| Breakfast | "Good morning! Ready to log breakfast?" |
| Lunch | "Lunch time. A quick log keeps your day on track." |
| Dinner | "Dinner logged yet? It takes about 10 seconds." |
| Hydration | "Time for a glass of water. 💧" |
| Step nudge | "A short walk could get you closer to your step goal." |
| Goal reached | "You hit your step goal today. Nice work!" |
| Weigh-in | "Weekly weigh-in time. Log your weight to see your trend." |
| Weekly report | "Your week is ready: you logged 6 of 7 days." |

## Appendix B. Seed food list (to be generated as JSON, then verified)

The seed JSON (`assets/foods_seed.json`) must contain 300+ foods. Include the 70 foods from the web version and add at least the following **desi dishes** (values approximate, per typical serving, with Roman Urdu and Urdu aliases where natural):

- **Rice and biryani:** chicken biryani, beef biryani, mutton biryani, chicken pulao, yakhni pulao, daal chawal, white rice, zarda, matar pulao
- **Curries:** chicken karahi, mutton karahi, beef karahi, chicken handi, chicken korma, mutton korma, butter chicken, nihari, haleem, paya, aloo gosht, keema matar, chana masala, daal mash, daal chana, masoor daal, moong daal, kadhi pakora, palak paneer, saag, sarson ka saag, bhindi masala, aloo gobi, baingan bharta, karela, mixed sabzi
- **Breads:** roti, naan, garlic naan, kulcha, paratha, aloo paratha, keema paratha, puri, halwa puri, makki ki roti, tandoori roti
- **Grill and snacks:** seekh kabab, chapli kabab, shami kabab, reshmi kabab, chicken tikka, chicken malai boti, fish fry, Lahori fish, samosa, pakora, aloo tikki, dahi bhalla, chana chaat, fruit chaat, papri chaat, gol gappay, chicken shawarma, chicken roll paratha, shami burger
- **Dairy and drinks:** plain yogurt, raita, sweet lassi, salty lassi, chaas, doodh patti chai, chai without sugar, qehwa, Rooh Afza sharbat, falooda, sugarcane juice
- **Sweets:** gulab jamun, rasmalai, kheer, gajar ka halwa, suji halwa, jalebi, barfi, ladoo, kulfi
- **Breakfast:** anda paratha, omelette, halwa puri plate, nihari kulcha, daal paratha, desi ghee toast

Other categories to fill: fruits, vegetables, grains, proteins, dairy, snacks, drinks, fast food, soups, breakfast cereals, nuts and seeds. **The agent must not present invented numbers as precise:** mark every seed food approximate, and generate the file in a way that makes values easy to review and correct.

## Appendix C. Achievement details

| Badge | Condition |
|---|---|
| First Bite | Log your first food |
| Streak 3 / 7 / 14 / 30 / 100 | Log food for that many consecutive days |
| Hydration Hero | Reach the water goal 7 days in a row |
| Step Master | 10,000 steps on 5 different days |
| Protein Pro | Reach the protein target 5 days in a row |
| On Target | Stay within the calorie goal 7 days in a row |
| Weigh-in | Log your first weight |
| Goal Getter | Reach your goal weight |
| Explorer | Log 50 different foods |
| Chef | Create your first recipe |

## Appendix D. Out of scope for v1 (do not build)

Social feed or friends, meal plan generator, AI photo recognition, voice logging, wearable apps, cloud accounts, subscriptions, ads, in-app chat, supplements and micronutrient tracking beyond fibre, sugar and sodium.
