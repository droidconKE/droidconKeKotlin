# droidconKE Android — Modernization & Product Plan

> **Status:** Draft for review · **Author:** Staff engineering review · **Date:** 2026-08-13
> **Repo:** `droidconKeKotlin` · **Branch:** `main` @ `7a8317c`
> **Scope:** Full-stack app review — tech debt, modern Android practices, intelligent (AI) experiences, adaptive UI, ticketing, performance, testing, notifications, UX, new product surfaces, and store presence.

---

## 0. How to read this document

This is a **working plan**, not a wishlist. Every phase has:

- **Why** — the problem, stated in terms of user or contributor impact.
- **What** — concrete changes, with code that compiles against this repo's actual package names.
- **Definition of done** — how we know it landed.
- **Dependencies** — what must land first.

There are deliberately no time estimates here. This is volunteer work with variable capacity, and a date attached to a feature by someone who isn't building it is a number that gets quoted back later without the caveats. What the plan does commit to is **ordering** — what blocks what — and that's the part that's actually knowable up front.

### A word on scope

This document is deliberately exhaustive because you asked for exhaustive. It is not a commitment to build all of it. Shipping everything here in one conference cycle would leave the app in a half-migrated state, which is worse than where it is now.

**Read §16 first.** It contains the sequencing and a "if you only do five things" list. Phase 0 is non-negotiable groundwork; everything after it is independently shippable and independently cancellable.

### Version numbers

Dependency versions below reflect what was current at authoring time. Before landing any of them, run:

```bash
./gradlew versionCatalogUpdate   # plugin is already declared, just not wired — see §3.2
```

The `toml-checker` / `toml-updater` plugins are already in `libs.versions.toml` but never applied to the root project. §3.2 fixes that.

---

## 1. Where the codebase stands today

### 1.1 Inventory

| Metric | Value |
| --- | --- |
| Kotlin files | 297 |
| Kotlin LOC | ~21,300 |
| Gradle modules | 7 (`app`, `chai`, `data`, `datasource:local`, `datasource:remote`, `domain`, `presentation`) + `build-logic` |
| Convention plugins | 9 |
| First commit | 2023-02-11 |
| Kotlin / AGP | 2.1.21 / 8.10.1 |
| compileSdk / minSdk / targetSdk | 36 / 24 / 36 (app), **34 (libraries)** — target state is **37** (§3.1) |
| Compose BOM | 2025.06.00 |
| Navigation | **Navigation 3** (`navigation3-runtime` 1.0.0) |
| Networking | Ktor 3.1.3 |
| Persistence | Room 2.7.1 (schema v5), DataStore Preferences |
| DI | Hilt 2.56.2 + KSP |
| Sync | WorkManager + Firebase Remote Config feature toggles |
| Quality gates | ktlint, detekt, spotless, Jacoco, Codecov |
| Test files | 33 unit/Robolectric · **2 instrumentation (both scaffolding)** |

### 1.2 What is genuinely good — do not regress this

Being explicit here matters, because a modernization plan can read as "everything is broken." It isn't. This codebase is above the median for a community conference app:

1. **Real module boundaries.** `domain` is pure Kotlin with no Android dependency. `data` depends on `domain`, not the reverse. `datasource:local` and `datasource:remote` are separately consumable. This is the hard part of clean architecture and it's done.
2. **Convention plugins.** `build-logic/` follows the Now in Android pattern. Adding a module is cheap. Most community apps copy-paste 200-line `build.gradle.kts` files into every module; this one doesn't.
3. **Navigation 3, already.** `NavigationState` with per-tab back stacks, `rememberSerializable` for process-death survival, a `NavigationController` with directional transition tracking. This is ahead of most production apps in 2026 and is a genuine differentiator for a conference app that developers will read the source of.
4. **A design system module (`chai`).** Semantic color tokens (38 of them), a typography scale, atoms/components/icons separation. The bones are right even where the implementation needs work (§5).
5. **Feature toggles wired to Remote Config.** `RemoteFeatureToggle` already exists. This is exactly the infrastructure needed to ship AI features safely behind a kill switch (§6.11) — a rare thing to already have.
6. **MVI-ish unidirectional flow in `sessions`.** `SessionsIntentHandler` + a single `SessionsUiState` derived via `combine` + `stateIn`. The pattern is correct.
7. **Offline-first by construction.** Room is the source of truth, `Flow` all the way to the UI, WorkManager reconciles. The app works on Nairobi conference-venue wifi, which is the actual requirement.

### 1.3 Findings — correctness bugs

These are real defects, ordered by user impact. Each has a fix in §3.3.

#### B1 — Selecting any room filter empties the session list

> **Corrected after review.** An earlier draft of this plan named the *topics* filter as the headline bug. That was wrong, and the correction matters because it changes what ships first. Topics is unreachable dead code (see B11). The **room** filter is the one that's broken, and unlike topics it is fully exposed in the UI.

Three facts that only bite in combination:

1. `SessionsFilterPanel.loadFilters()` hardcodes the room options as `"Room A"`, `"Room B"`, `"Room C"`.
2. `data/.../mappers/SessionMapper.kt:70` builds the session's room field from the API: `rooms = this.rooms.joinToString(separator = ",") { it.title }` — real venue titles, and comma-joined when a session spans rooms. `SessionPresentationModel.color`'s `when (venue)` tells us the real names are `"Opal"`, `"Sapphire"`, and others.
3. `SessionsViewModel.filterSessions()` tests `filterState.rooms.contains(it.rooms)` — a `List<String>.contains(String)`, i.e. exact whole-string equality.

So `listOf("Room A").contains("Opal")` is `false` for every session. **Tap any room filter and the list goes empty.**

Even with the names corrected, the comparison is still wrong: a session in `"Opal,Sapphire"` would never match a filter for `"Opal"`, because the filter compares against the joined string rather than the parts.

**User-visible effect:** the room filter appears to work, highlights correctly, and returns nothing. On a conference day, a user trying to find what's on in the room they're sitting in gets an empty screen.

#### B11 — `java.time` on minSdk 24 with core library desugaring never enabled

> Numbered B11 because it was found last, during the review pass in §1.3a. It is placed here because it is the **most severe finding in the document** and priority is not the same thing as discovery order. See §1.3a for the full priority ordering.

It crashes the app on devices the manifest claims to support.

- `minSdk = 24` (`KotlinAndroid.kt`).
- `java.time` was added in **API 26**.
- `isCoreLibraryDesugaringEnabled` appears **nowhere** in the build.
- `desugar_jdk_libs` is declared in `libs.versions.toml` — both a version and a library alias — and **wired to no module**. Someone knew this was needed and never finished.

`java.time` is used in production code in three modules:

| File | Usage |
| --- | --- |
| `data/.../mappers/SessionMapper.kt:26-28` | `LocalDateTime.parse(...).toInstant(ZoneOffset.ofHours(3))` in `fromString()` |
| `datasource/remote/.../feed/model/FeedDTO.kt:21` | `LocalDateTime` field |
| `datasource/remote/.../feed/deserializer/LocalDateTimeSerializer.kt:24` | `LocalDateTime` + `DateTimeFormatter` |

`SessionMapper.fromString()` is called from `SessionDTO.toEntity()`, which runs on **every session sync** — and sync runs at launch. So on Android 7.0 and 7.1 the app throws `NoClassDefFoundError` the first time it syncs.

**Fix:** §3.1's `isCoreLibraryDesugaringEnabled = true` plus the `coreLibraryDesugaring` dependency. That change was already in this plan, but framed as an enabler for a future `kotlinx-datetime` refactor. It is not an enabler — **it is a crash fix, and it is the single highest-priority change in this document.**

**Verify before and after** on an API 24 emulator. And add a CI instrumentation matrix entry at API 24 (§15.1 currently starts at 26, which is exactly why nobody caught this).

#### B2 — Destructive Room migration wipes bookmarks

`datasource/local/.../di/DatabaseModule.kt` calls `.fallbackToDestructiveMigration()` with only `MIGRATION_4_5` registered.

**User-visible effect:** any future schema change without a hand-written migration silently deletes every starred session. For a conference app, the starred-sessions list *is* the user's personal agenda. Losing it the morning of day 1 is a severe failure. Additionally `exportSchema = false` means there is no schema snapshot, so migration tests are impossible and Room can't generate auto-migrations.

#### B3 — `-Xjvm-default=all` is a no-op

`presentation/build.gradle.kts`:

```kotlin
kotlinOptions {
    freeCompilerArgs + "-Xjvm-default=all"   // result discarded
}
```

`+` on a `List` returns a new list; nothing is assigned. The flag has never been applied.

#### B4 — Library modules target SDK 34, app targets 36; both should be 37

`AndroidLibraryConventionPlugin` sets `defaultConfig.targetSdk = 34`; `AndroidApplicationConventionPlugin` sets `36`. Library `targetSdk` only affects instrumentation tests, so the effect is that **library-module instrumented tests run under different platform behaviour than the shipping app** — including the edge-to-edge and predictive-back behaviour changes that land at 35/36. Tests can pass while the app is broken.

Two things to fix, not one:

1. **The drift.** Two hardcoded numbers in two plugins with nothing tying them together. Both must come from a single version-catalog entry so they cannot diverge again.
2. **The level.** Move to `compileSdk = 37` / `targetSdk = 37` across every module.

Bumping to 37 is the part with actual behavioural risk, and it needs its own PR with its own testing pass rather than riding along with the drift fix. Land the shared version ref first (mechanical, zero behaviour change), then bump the shared value to 37 in a second PR where the only thing under review is the platform-behaviour fallout. Review [the behaviour changes for apps targeting 37](https://developer.android.com/about/versions) before that second PR — edge-to-edge enforcement (§3.4) is the one already known to affect this app.

#### B5 — `findActivity()` throws in previews and non-Activity contexts

`chai/src/main/java/com/droidconke/chai/Theme.kt`:

```kotlin
private fun Context.findActivity(): Activity {
    // ...
    throw IllegalStateException("Activity absent")
}
```

Guarded by `if (!view.isInEditMode)`, which covers Studio previews but **not** Robolectric, Roborazzi, or any composition hosted outside an Activity (e.g. a Glance widget host, a `ComposeView` in a Service, or a screenshot test). This will block the screenshot-testing work in §10 until fixed.

#### B6 — Screen-level state that doesn't survive rotation

`SessionsScreen.kt`: `showMySessions` uses `remember { mutableStateOf(false) }` while its siblings use `rememberSaveable`. Rotating the device resets the "My Sessions" toggle but **not** the underlying `filterState` in the ViewModel — so the switch reads OFF while the list is still filtered to bookmarks. Divergent state between UI and ViewModel.

`isFilterDialogOpen` is written in three places and never read. Dead state.

#### B7 — Splash-screen race

`MainActivity.onCreate`:

```kotlin
var keepSplashScreen = true
splashScreen.setKeepOnScreenCondition { keepSplashScreen }
lifecycleScope.launch { /* ... */ keepSplashScreen = false }
```

A plain local `var` mutated from a coroutine and read from a platform callback on the main thread. It happens to work because both run on the main dispatcher, but it's unsynchronized-by-luck, and `setKeepOnScreenCondition` blocks the first frame on a *network-dependent* Remote Config fetch. On a cold start with bad connectivity this holds the splash screen for the full Remote Config timeout.

#### B8 — Theme colours hardcoded in `CText`

`chai/.../components/CText.kt`: `CParagraph` hardcodes `ChaiBlack`, `CPageTitle` hardcodes `ChaiBlue`, `CSubtitle` and `CActionText` hardcode `ChaiRed`. These four are theme-blind — black text on a dark background in dark mode.

#### B9 — `SimpleDateFormat` in a ViewModel

`SessionsViewModel:63` instantiates `SimpleDateFormat("dd", Locale.getDefault())` per emission. `SimpleDateFormat` is not thread-safe, and `kotlinx-datetime` plus core library desugaring are **already dependencies**. Also: the "which day is today" default-selection logic compares a `dd` day-of-month string against event-day strings, which breaks for any event spanning a month boundary.

#### B10 — Mutable state inside serializable navigation keys

```kotlin
@Serializable
sealed class Screens(
    @DrawableRes var icon: Int,
    var title: String,
) : NavKey
```

`var` in a navigation key that gets serialized to `SavedState`. Also: `title` is a hardcoded English string, so **the entire bottom navigation bar is unlocalizable** (§14), and icon resource IDs are not stable across builds — persisting them across process death is unsound in principle.

#### B12 — Session-type filter is broken by letter case

`SessionsFilterPanel.loadFilters()` sets `value = "keynote"` and `value = "codelab"` in lower case. The comparison is `filterState.sessionTypes.contains(it.sessionFormat)` — case-sensitive exact match. `SessionPresentationModel.isKeynote` uses `format.contains("Keynote", ignoreCase = true)`, which tells us the API returns the capitalised form.

So the Keynote and Codelab filters return nothing. `"Session"`, `"Workshop"`, `"Lightning talk"` and `"Panel discussion"` are capitalised and may work, which is why this reads as flaky rather than broken.

Same root cause as B1: **filter option values are hand-typed constants in a composable rather than derived from the data.** Fixing the strings fixes today's bug; deriving the options fixes the class of bug.

#### B13 — `AuthManager`'s network-error branch is unreachable, and a test hides it

```kotlin
// data/.../repos/AuthManager.kt:46
} catch (e: Exception) {
    when (e) {
        is ServerError, is NetworkError ->
            DataResult.Error("Login failed", networkError = true, exc = e)
        else -> DataResult.Error("Login failed", exc = e)
    }
}
```

`ServerError` and `NetworkError` are thrown from exactly one place: `safeApiCall` in `datasource/remote/.../utils/SafeApiCall.kt`, which is `@Deprecated` and has **zero callers**. `AuthApi.googleLogin` doesn't go through it. So the `networkError = true` branch can never execute in production, and login failures never tell the UI that the problem was connectivity.

The reason this survived: `AuthManagerTest.kt:71` constructs `NetworkError()` directly and stubs the API to throw it. The test passes against a path production cannot reach. **A green test asserting unreachable behaviour is worse than no test** — it actively prevents the bug being noticed.

#### B14 — Eight of ten lazy lists have no `key`

Only `VerticalStepComponent` and `SessionStateComponent` pass `key`. Missing at:

| File | Line |
| --- | --- |
| `home/components/HomeSessionSection.kt` | 67 |
| `home/components/HomeSpeakersSection.kt` | 53 |
| `speakers/view/SpeakersScreen.kt` | 142 |
| `common/bottomnav/BottomNavigationBar.kt` | 72, 80 |
| `sessions/components/EventDaySelector.kt` | 42 |
| `feed/view/FeedShareSection.kt` | 103 |
| `feed/view/FeedScreen.kt` | 155 |

Without keys, Compose identifies items positionally: scroll position jumps when a list reorders after sync, item animations attach to the wrong rows, and items re-compose that should have skipped. §9.5 previously listed this as something to "confirm" — it isn't a maybe, it's eight sites.

`HomeSpeakersSection.kt:53` compounds it with `items(speakers.take(8))`, which allocates a fresh list on every recomposition and so defeats skipping by identity. Hoist the slice to the ViewModel or `remember` it.

#### B15 — Session card colours can never respond to dark mode

```kotlin
// presentation/.../models/SessionPresentationModel.kt:26-28, 47
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiTeal
// …
val color = when (venue) {
    "Opal" -> ChaiRed
    "Sapphire" -> ChaiTeal
    else -> ChaiBlue
}
```

A plain data class reaching into the tier-1 brand palette. Because it isn't `@Composable`, it cannot read `MaterialTheme` — so these colours are **structurally incapable** of responding to theme, dark mode, or contrast settings. It also hardcodes venue names, which change every year.

This is the single best argument for §3.5's rule against tier-1 palette references outside `chai/colors`, and the fix is to move the mapping into the composable that draws the card, keyed off a semantic token.

#### B16 — Two more `SimpleDateFormat` sites, and a masked NPE

B9 named `SessionsViewModel`. There are two others:

- `data/.../repos/SessionsManager.kt:88` — `SimpleDateFormat("dd", Locale.getDefault())`, in the data layer.
- `presentation/.../utils/DateAndTimeUtils.kt:24` — `SimpleDateFormat(...).parse(this)` returns a nullable `Date?`, then `timePosted.time` dereferences it. The `catch (e: Exception)` two lines down silently swallows the resulting NPE and returns the raw input string, so a malformed timestamp renders as an ISO-8601 blob in the feed instead of "2 days ago".

Both are covered by the same `kotlinx-datetime` migration, which is only safe **after** B11's desugaring fix.

### 1.3a Priority ordering of the correctness findings

Findings are numbered in discovery order. This is the order to fix them in:

| Priority | Finding | Why this rank |
| --- | --- | --- |
| **P0** | B11 desugaring | Crashes on a supported API level. Nothing else matters if the app doesn't start. |
| **P0** | B2 destructive migration | Silently deletes the user's agenda. Latent, but unbounded damage when it fires. |
| **P0** | B1 room filter | Fully exposed broken feature on a core screen. |
| **P1** | B12 filter case · B6 state divergence · B7 splash race | User-visible, bounded, cheap. |
| **P1** | B13 unreachable branch | Small fix; the test deletion is the real value. |
| **P1** | B4 targetSdk drift | Blocks trustworthy instrumentation tests. |
| **P2** | B14 lazy keys · B15 card colours · B5 `findActivity` | Quality and correctness-of-architecture; B5 blocks §10.2. |
| **P2** | B3 no-op flag · B8 theme-blind text · B9/B16 date handling · B10 nav keys | Cleanup, or prerequisites for later phases. |
| **Deferred** | B11a topics filter (see §1.4) | Dead code. Delete it or spec it with the backend; don't half-build it. |

### 1.4 Findings — deprecated and dead code

| Item | Location | Replacement |
| --- | --- | --- |
| ~~`accompanist-swiperefresh`~~ | `HomeScreen`, `SpeakersScreen`, `SessionStateComponent` | **Done.** M3 `PullToRefreshBox` |
| `GoogleSignIn` / GMS Auth API | `GoogleSignInHandler`, `AuthViewModel`, `AuthDialog`, `GoogleSignInButton` | Credential Manager + Google ID |
| `window.statusBarColor` | `chai/Theme.kt` | `enableEdgeToEdge()` (no-op on API 35+) |
| `packagingOptions {}` | `app`, `presentation`, `chai` | `packaging {}` |
| `kotlinOptions {}` | root, convention plugins, 2 modules | `compilerOptions {}` (`KotlinJvmCompilerOptions`) |
| `project.buildDir` | `AndroidCompose.kt` | `layout.buildDirectory` |
| `detekt { config = files(…) }` | root `build.gradle.kts` | `config.setFrom(…)` |
| `fallbackToDestructiveMigration()` | `DatabaseModule` | `fallbackToDestructiveMigration(dropAllTables = true)` — or better, real migrations |
| ~~`composecompiler = "1.5.15"` + `compose-compiler` in the `compose` bundle~~ | `libs.versions.toml` | **Done.** Kotlin 2.x uses the `org.jetbrains.kotlin.plugin.compose` plugin. Shipping `androidx.compose.compiler:compiler` as an `implementation` dependency is wrong. |
| ~~`gson`~~ | catalog | **Done.** 0 usages. `kotlinx-serialization` is the JSON library. |
| ~~`result-jvm` (kittinunf)~~ | catalog | **Done.** 0 usages. |
| ~~`paging-common`, `paging-compose`, `room-paging`~~ | catalog + `compose` bundle | **Done.** 0 usages. |
| ~~`compose-runtimeLivedata`~~ | `compose` bundle | **Done.** The one usage had already gone; deleted outright. |
| `ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt` | `app` | Delete. |
| `safeApiCall` + `ServerError` + `NetworkError` | `datasource/remote/.../utils/SafeApiCall.kt` | `@Deprecated` with **zero callers**. Deleting it makes B13's unreachable branch obvious rather than subtle. |
| `DateStringConverter` | `datasource/local/.../util/DateStringConverter.kt` | Declared, never referenced, not registered in `@TypeConverters` (only `InstantConverter` is). Dead. |
| `desugar_jdk_libs` catalog entry | `libs.versions.toml:9,111` | Version and library alias both declared, **wired to no module**. See B11 — this is the unfinished fix for a live crash. |
| **The whole topics filter path** | `SessionsFilterCategory.Topic`, `SessionsFilterState.topics`, `SessionsViewModel.updateFilterState`'s `Topic` branch | **Unreachable dead code, not a bug.** `loadFilters()` emits zero `Topic` options and `SessionDTO` has no topic field, so nothing can ever set it. Either delete all three, or spec `topics` with the backend and build it end to end (§3.3). Do not leave it half-wired — that's how it got mistaken for a live bug in the first draft of this plan. |
| `HomeBannerSection` | commented out in `HomeScreen` | Decide: ship or delete. |
| `chai` drawables duplicated in `presentation` | 12 identical files | Single source in `chai`. |

The `compose` bundle is applied to **every** Compose module by `AndroidLibraryComposeConventionPlugin`, so `chai` — a pure design-system module — pulled in `paging-compose`, `runtime-livedata`, `constraintlayout-compose`, and `navigation3`. That's the bundle-as-kitchen-sink antipattern. **Done:** `coil` and `navigation3` are separate bundles now, and they plus `activity-compose`, `constraintlayout-compose` and `lifecycle-runtime-compose` are declared in `presentation`.

### 1.5 Findings — architecture

**A1 — No adaptive layout support at all.** No `androidx.window`, no `material3-adaptive`, no `WindowSizeClass`, no `@PreviewScreenSizes`. The README claims the app "is optimized for phones and tablets of all shapes and sizes." It is not — it renders a phone layout stretched across a tablet, with a bottom bar on a 13" screen. Google Play surfaces large-screen quality in store rankings and on ChromeOS/foldables; this is both a UX and a distribution problem. (§4)

**A2 — No edge-to-edge, no insets handling.** Zero references to `WindowInsets`, `enableEdgeToEdge`, `safeDrawing`, or `systemBarsPadding` in 297 files. Edge-to-edge is enforced for apps targeting SDK 35+; the app targets 36 today and 37 after §3.1. Right now the framework's compatibility path is doing the work, and the manual `statusBarColor` write is a no-op. On API 35+ devices the layout is being saved by luck. **This must be fixed before the targetSdk 37 bump**, not after — bumping the target while insets are unhandled turns a latent problem into a visible one. (§3.4)

**A3 — `chai` bypasses Material 3 entirely.** `ChaiDCKE22Theme` calls `MaterialTheme(content = content)` — default `colorScheme`, default `typography`, default `shapes` — then layers a parallel `ChaiColors` on a `CompositionLocal`. Consequences:

- Every stock M3 component (`ModalBottomSheet`, `Snackbar`, `Slider`, `DatePicker`, `TextField`, `NavigationBar`) renders in **Material's default purple**, not brand colours. You can see this in `SessionsScreen`, which has to manually pass `containerColor = ChaiGrey90.copy(alpha = 0.52f)` to `ModalBottomSheet` to compensate.
- No dynamic colour, no M3 Expressive, no `MotionScheme`.
- Typography is ~20 hand-rolled composable functions (`ChaiBodySmallBold`, `ChaiTextLabelLarge`, …) instead of a `Typography` object. `LocalTextStyle` doesn't work, `TextStyle` can't be overridden at a call site, and every new variant needs a new function. There are already 18 of them.
- `staticCompositionLocalOf { ChaiColors() }` defaults every colour to `Color.Unspecified`. Forget the provider and the UI renders invisible rather than failing loudly.

**A4 — `presentation` is a monolith.** 120 files, one module: home, sessions, speakers, feed, about, feedback, auth, notifications, navigation, and `MainActivity`. Every UI change recompiles everything. Feature-level ownership is impossible. Build times will get materially worse as the features in §11 land. (§2)

**A5 — Domain models carry transport types.** `Session.startDateTime: String`, `endDateTime: String`, `startTime: String`, `endTime: String` — four string fields where two `Instant`s belong. Parsing is duplicated across `SessionMapper` (data), `SessionMapper` (presentation), `DateAndTimeUtils`, and `SessionsViewModel`. Timezone handling is implicit.

**A6 — No screenshot tests, no E2E tests.** The two `androidTest` files are IDE scaffolding. All UI testing is Robolectric in `src/test`. There is no test that launches the app and walks a user journey, and no protection against visual regression in `chai` — which is exactly what a design system needs. (§10)

**A7 — No performance instrumentation.** No baseline profile, no startup profile, no macrobenchmark module, no `profileinstaller`. Cold-start and scroll performance are unmeasured, therefore unmanaged. Firebase Performance is a dependency but no custom traces are defined. (§9)

**A8 — Notifications are receive-only.** `MessagingService` handles FCM; `DroidconNotificationManager` posts. There are no local session reminders, no notification channels per category, no user preference surface, and the permission request fires unconditionally on first launch with no rationale (`MainActivity.askNotificationPermission` logs a rationale to Timber instead of showing one). (§8)

### 1.6 Findings — build & developer experience

**D1 — `repositories {}` in `allprojects`.** `settings.gradle.kts` has no `dependencyResolutionManagement`. The deprecated per-project pattern blocks Gradle **Isolated Projects** and complicates configuration cache. (§3.1)

**D2 — No configuration cache, no build cache, no parallel.** `org.gradle.parallel` is commented out. `org.gradle.jvmargs=-Xmx2048m` is low for a 7-module Compose build with KSP. These are free wins measured in minutes per build. (§3.1)

**D3 — Type-safe project accessors enabled but unused.** `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` is on; every module still writes `project(":domain")` instead of `projects.domain`.

**D4 — Stale 2023 naming.** `rootProject.name = "DroidconKE2023"`, `app` namespace `ke.droidcon.kotlin` (correct) but application class `com.android254.droidconKE2023.app.DroidconKE2023App`, theme `Theme.DroidconKE2023`, DB named `dcke22-database`, design-system theme `ChaiDCKE22Theme`, README says 2024, API event slug says `droidconke-2025-898`. Four different years in one codebase. This is a real contributor-onboarding tax — new contributors cannot tell which parts are current.

**D5 — CI gaps.**
- `branch.yml` only triggers on changes to `build-logic/**`, `build.gradle.kts`, `settings.gradle.kts`. **A PR that changes only Kotlin source runs no CI at all.** This is the single highest-value CI fix in the repo.
- `actions/checkout@v3`, `setup-java@v3` — two majors behind.
- No lint (`./gradlew lint`) in CI, despite a 28 KB `lint-baseline.xml`.
- No dependency review, no APK-size diff, no screenshot diff.
- `fastlane/report.xml` is committed build output.
- Release workflow force-pushes straight to `track: production` with `status: completed` — no staged rollout, no internal track, no gate.

**D6 — 28 KB lint baseline.** Never re-triaged. Whatever it hides is invisible.

**D7 — Two Gradle wrappers, and real AGP 9 exposure.** `gradle/wrapper` pins 8.11.1; `build-logic/gradle/wrapper` pins 8.14.1. The second is stale and unused (included builds run under the root distribution) but it misleads contributors.

Against the [JetBrains AGP 9 migration skill's](https://github.com/Kotlin/kotlin-agent-skills/tree/main/skills/kotlin-tooling-agp9-migration) compatibility tables, this repo is behind on five plugins (KSP, Hilt, Firebase Performance, Firebase Crashlytics, and Gradle itself), and **two of its static-analysis plugins — detekt 1.23.8 and ktlint-gradle 12.3.0 — require AGP 9 opt-out flags that disable AGP 9's two headline features.** There's also a direct conflict between the requested targetSdk 37 and AGP 9.0's API 36.1 cap.

The good news: the two changes that usually make an AGP 9 migration painful are already absent. `Jacoco.kt` uses the new `androidComponents.onVariants` API rather than the removed `applicationVariants`/`libraryVariants`, and coverage uses `enableUnitTestCoverage` rather than the removed `testCoverageEnabled`. Nothing in the build is on the "broken, no workaround" list.

Full assessment, the targetSdk decision, and sequencing in §3.8.

### 1.7 Findings — security & hygiene

**S1 — Debug keystore and its passwords committed.**

```kotlin
signingConfigs {
    getByName("debug") {
        storeFile = file("../keystore/dckedebug.keystore")
        keyAlias = "dcke"
        keyPassword = "droidconkenya"
        storePassword = "droidconkenya"
    }
}
```

A shared debug keystore committed to a public repo is a **deliberate and defensible choice** for an OSS project — it lets contributors install builds over each other and keeps Firebase debug SHA-1 registration stable. Keep it, but document *why* in the README so nobody "fixes" it, and make sure the *release* keystore is nowhere near the repo (it isn't — it comes from `secrets.PLAYSTORE_SIGNING_KEY`, which is correct).

**S2 — `api_key.txt` is tracked, and is not an API key.** Root-level, in git, 15 bytes: the literal string `droidconKe-2020`. It is not a live credential — it reads like a stale keystore alias or password from the 2020 app, orphaned by commit `7975bfb` ("Refactor: Remove 2022 references"). No rotation needed.

But it should not exist. A file named `api_key.txt` in a public repo trains every future contributor to think that's an acceptable place to put a key, and the next one might be real. **Delete the file, and add `*api_key*` and `*.keystore` (except the documented debug keystore) to `.gitignore`** so the pattern can't recur. No history purge required.

**S3 — `app/google-services.json` is tracked.** Standard practice and not a secret (it contains public client identifiers), but it means **anyone can point their own build at the production Firebase project**. Once §6 lands, that Firebase project will be paying for Gemini API calls. Firebase **App Check** becomes mandatory before any AI feature ships. (§6.11)

**S4 — No network security config.** Ktor talks HTTPS to `api.droidcon.co.ke`, but there's no `networkSecurityConfig` pinning cleartext to off. Add `android:usesCleartextTraffic="false"`.

---

## 2. Target architecture

The current 7-module layout has served well, but `presentation` at 120 files is the bottleneck. Target:

```
:app                          — Application, MainActivity, DI graph root, manifest merge
:build-logic                  — convention plugins (existing)

:core:model                   — pure Kotlin data classes (from :domain/models)
:core:domain                  — repository interfaces, use cases (from :domain)
:core:data                    — repository impls, sync, mappers (from :data)
:core:database                — Room (from :datasource:local)
:core:network                 — Ktor, DTOs (from :datasource:remote)
:core:datastore               — preferences / encrypted prefs
:core:designsystem            — chai 2.0: theme, tokens, typography, components
:core:ui                      — shared composables that know about domain models
:core:common                  — Result, dispatchers, extensions
:core:analytics               — analytics + Crashlytics abstraction
:core:ai                      — NEW. inference abstraction (§6)
:core:testing                 — test doubles, rules, fake data
:core:screenshot              — NEW. Roborazzi harness + shared previews

:feature:home
:feature:sessions
:feature:speakers
:feature:feed
:feature:about
:feature:auth
:feature:ticket               — NEW (§7)
:feature:notes                — NEW (§11.5)
:feature:assistant            — NEW (§6.7)
:feature:gamification         — NEW (§6.8)
:feature:jobboard             — NEW (§11.1)
:feature:challenge            — NEW (§11.3)
:feature:networking           — NEW (§11.2)

:widget                       — NEW. Glance "next session" widget (§11.7)
:benchmark                    — NEW. macrobenchmark (§9.1)
:baselineprofile              — NEW. baseline + startup profile generator (§9.2)
```

**Do not big-bang this.** The migration order that minimises risk:

1. Extract `:core:designsystem` (rename `chai`, keep the artifact) — it has the fewest inbound dependencies.
2. Split `:core:model` out of `:domain` — pure data, zero risk.
3. Extract **one** feature (`:feature:speakers`, the smallest at 6 files) end-to-end. Prove the pattern, write it down.
4. Extract the rest one PR per feature, over months, as features get touched anyway.
5. Rename `:datasource:*` and `:data`/`:domain` to `:core:*` **last** — it's a pure rename and it touches every file, so do it when the tree is otherwise stable.

Rule: **a feature module never depends on another feature module.** Cross-feature navigation goes through `NavKey`s owned by `:core:ui` (or a thin `:core:navigation`), which is how the current `DroidconEntryProvider` already works — that pattern survives the split intact.

---

## 3. Phase 0 — Foundations

**Must land before anything else. No dependencies.**

Nothing in Phases 1–10 is safe to build on top of the current build configuration and theme layer. This phase is unglamorous and entirely internal. It is also the phase that makes every subsequent phase cheaper.

### 3.1 Gradle & build modernization

**`settings.gradle.kts`** — move repositories to `dependencyResolutionManagement`, unblocking Isolated Projects:

```kotlin
@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "droidconKE"   // was DroidconKE2023 — see §3.6

include(":app")
include(":benchmark")
include(":baselineprofile")
include(":chai")
include(":data")
include(":domain")
include(":presentation")
include(":datasource:local")
include(":datasource:remote")
```

`FAIL_ON_PROJECT_REPOS` will immediately break the `repositories {}` block in root `build.gradle.kts` — that's the point. Delete it.

**`gradle.properties`** — the free performance wins:

```properties
# 2 GB is not enough for a 7-module Compose + KSP build.
org.gradle.jvmargs=-Xmx6g -XX:+UseParallelGC -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.configuration-cache.parallel=true

# Kotlin
kotlin.code.style=official
kotlin.incremental=true
kotlin.daemon.jvmargs=-Xmx4g

# AndroidX / R8
android.useAndroidX=true
android.nonTransitiveRClass=true
android.nonFinalResIds=false
android.enableR8.fullMode=true

# Only generate the BuildConfig fields we actually use
android.defaults.buildfeatures.buildconfig=false
android.defaults.buildfeatures.aidl=false
android.defaults.buildfeatures.renderscript=false
android.defaults.buildfeatures.shaders=false
```

> Enabling the configuration cache will surface every configuration-time file read and `project` reference inside task actions. Expect one focused PR of fallout — mostly in `Jacoco.kt` and `AndroidCompose.kt`, both of which read `project.buildDir` at configuration time.

**Root `build.gradle.kts`** — remove `allprojects`, fix the deprecated `detekt` config, drop the per-project `kotlinOptions` block (it belongs in the convention plugin):

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.plugin) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.roborazzi) apply false

    alias(libs.plugins.jlleitschuh)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.toml.checker)
    alias(libs.plugins.toml.updater)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        verbose.set(true)
        filter { exclude { it.file.path.contains("generated/") } }
    }

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("detekt.yml"))   // was: config = files(...)
        parallel = true
        buildUponDefaultConfig = true
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt", "${rootProject.rootDir}/build-logic/**/*.kt")
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"), "^(package|object|import|interface)")
        }
        format("kts") {
            target("**/*.kts")
            targetExclude("**/build/**/*.kts")
            licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
        }
    }
}
```

**`build-logic/.../com/android254/KotlinAndroid.kt`** — migrate off deprecated `kotlinOptions`, unify `targetSdk`, add desugaring, and stop leaking blanket opt-ins:

```kotlin
package com.android254

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = libs.findVersion("android-compile-sdk").get().toString().toInt()

        defaultConfig {
            minSdk = libs.findVersion("android-min-sdk").get().toString().toInt()
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            // java.time on API 24 — kills the SimpleDateFormat problem in B9
            isCoreLibraryDesugaringEnabled = true
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)

            val warningsAsErrors = providers.gradleProperty("warningsAsErrors").orNull.toBoolean()
            allWarningsAsErrors.set(warningsAsErrors)

            // Opt-ins are per-module now (see :presentation), not blanket-applied.
            // `-opt-in=kotlin.Experimental` was removed in Kotlin 1.9; keeping it is a no-op at best.
            freeCompilerArgs.addAll(
                "-Xconsistent-data-class-copy-visibility",
                "-Xannotation-default-target=param-property",
            )
        }
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("desugar-jdk-libs").get())
        add("implementation", libs.findLibrary("android.coreKtx").get())
        add("testImplementation", libs.findBundle("test").get())
    }
}
```

Note what left: `androidTestImplementation` of espresso/junit is no longer force-added to every module. Modules that need instrumentation tests declare it. Espresso was being added to `:domain`, a pure Kotlin module.

**`AndroidLibraryConventionPlugin`** — fix B4:

```kotlin
extensions.configure<LibraryExtension> {
    configureKotlinAndroid(this)
    // Was 34 while the app shipped 36 — instrumented tests ran under different platform behaviour.
    defaultConfig.targetSdk = libs.findVersion("android-target-sdk").get().toString().toInt()

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // Only :app and modules that genuinely read BuildConfig need it.
    buildFeatures { buildConfig = false }

    packaging {   // was packagingOptions
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/versions/9/previous-compilation-data.bin"
        }
    }
}
```

Add `android-target-sdk = "37"` to `[versions]` and reference it from both application and library plugins so they can never drift again. `AndroidApplicationConventionPlugin` reads the same ref:

```kotlin
extensions.configure<ApplicationExtension> {
    configureKotlinAndroid(this)
    // Was a hardcoded 36 here and a hardcoded 34 in the library plugin (B4).
    defaultConfig.targetSdk = libs.findVersion("android-target-sdk").get().toString().toInt()
    buildFeatures { buildConfig = true }
}
```

As noted in B4: land the shared-ref refactor at the current value first, then bump the value to 37 in a separate PR so the platform-behaviour review isn't mixed in with a build refactor.

**`build-logic/.../com/android254/AndroidCompose.kt`** — remove the phantom compose-compiler dependency, fix `buildDir`, split the bundle:

```kotlin
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures { compose = true }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))

            // Minimal core set. Feature-specific extras (navigation3, coil, lottie,
            // constraintlayout) are declared per-module — see §1.4.
            add("implementation", libs.findBundle("compose-core").get())
            add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
            add("debugImplementation", libs.findLibrary("compose-ui-test-manifest").get())
            add("implementation", libs.findLibrary("compose-ui-tooling-preview").get())
            add("testImplementation", libs.findLibrary("compose-ui-test-junit").get())
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        // Compose compiler metrics on demand: -PenableComposeCompilerMetrics=true
        fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }
        fun Provider<*>.relativeToRootProject(dir: String) = map {
            isolated.rootProject.projectDirectory.dir("build").dir(projectDir.toRelativeString(rootDir))
        }.map { it.dir(dir) }

        project.providers.gradleProperty("enableComposeCompilerMetrics").onlyIfTrue()
            .relativeToRootProject("compose-metrics")
            .let(metricsDestination::set)

        project.providers.gradleProperty("enableComposeCompilerReports").onlyIfTrue()
            .relativeToRootProject("compose-reports")
            .let(reportsDestination::set)

        // Treat presentation models as stable without annotating them everywhere.
        stabilityConfigurationFiles.add(
            isolated.rootProject.projectDirectory.file("compose_compiler_config.conf")
        )
    }
}
```

`compose_compiler_config.conf` at the repo root:

```
# Types the Compose compiler should treat as stable.
kotlinx.collections.immutable.ImmutableList
kotlinx.collections.immutable.ImmutableSet
kotlinx.datetime.Instant
kotlinx.datetime.LocalDateTime
com.android254.domain.models.*
```

**Definition of done:** `./gradlew build --configuration-cache` passes twice with the second run reporting a configuration-cache hit; `./gradlew :presentation:dependencies --configuration compileClasspath` shows no `paging`, `gson`, `result-jvm`, or `compose-compiler`.

### 3.2 Version catalog cleanup

Wire the version-checker plugins that are already declared but never applied (done in §3.1's root build file), then:

```toml
[versions]
android-compile-sdk = "37"       # was 36 — see §3.8 §0, this conflicts with AGP 9.0's API 36.1 cap
android-min-sdk = "24"
android-target-sdk = "37"        # NEW — single source of truth for app + libraries (fixes B4)

# --- bumped now for AGP 9 readiness (§3.8), independent of the AGP bump itself ---
hilt = "2.59"                    # was 2.56.2 — AGP 9 minimum
firebasePerfPlugin = "2.0.2"     # was 1.4.2 — AGP 9 minimum, and a major version jump
firebaseCrashlyticsPlugin = "3.0.6"  # was 2.9.9 — two majors behind; verify latest
ksp = "2.3.6"                    # was 2.1.21-2.0.2 — AGP 9 needs >= 2.3.1
kotlin = "2.3.20"                # was 2.1.21 — KGP 2.3.0+ recommended for AGP 9
benchmark = "1.5.0-alpha01"      # baselineprofile < 1.5.0-alpha01 needs android.newDsl=false

# --- deleted ---
# composecompiler = "1.5.15"     # Kotlin 2.x uses the compose compiler *plugin*
# gson = "2.13.1"                # 0 usages
# result_jvm = "5.6.0"           # 0 usages
# paging = "3.3.6"               # 0 usages
# swiperefresh = "0.36.0"        # accompanist, deprecated → PullToRefreshBox
# auth = "21.3.0"                # GMS sign-in, deprecated → Credential Manager

# --- added ---
androidx-adaptive = "1.2.0"
androidx-window = "1.5.0"
# Expressive lives in material3 1.4.x. BOM 2025.06.00 resolves material3 to 1.3.2,
# which has none of it — see the prerequisite note in §3.5. Either bump the BOM or
# pin material3 explicitly alongside the platform.
compose-material3-override = "1.4.0"
credentials = "1.6.0"
googleid = "1.2.0"
camerax = "1.5.0"
mlkit-barcode = "17.3.0"
mlkit-genai-summarization = "1.0.0-beta1"
mlkit-genai-image-description = "1.0.0-beta1"
mlkit-translate = "17.0.3"
firebase-ai = "17.4.0"           # ships in firebase-bom, listed for clarity
mediapipe-genai = "0.10.27"
litert = "2.0.1"
localagents-fc = "0.1.0"
zxing-core = "3.5.3"
glance = "1.2.0"
filament = "1.60.0"
immutable-collections = "0.4.0"
benchmark = "1.4.1"
uiautomator = "2.4.0"
profileinstaller = "1.4.1"
roborazzi = "1.53.0"
kotlinx-datetime = "0.6.2"
appcheck = "18.0.0"

[libraries]
# Adaptive / large screen (§4)
androidx-window = { module = "androidx.window:window", version.ref = "androidx-window" }
androidx-window-core = { module = "androidx.window:window-core", version.ref = "androidx-window" }
compose-material3-adaptive = { module = "androidx.compose.material3.adaptive:adaptive", version.ref = "androidx-adaptive" }
compose-material3-adaptive-layout = { module = "androidx.compose.material3.adaptive:adaptive-layout", version.ref = "androidx-adaptive" }
compose-material3-adaptive-navigation = { module = "androidx.compose.material3.adaptive:adaptive-navigation", version.ref = "androidx-adaptive" }
compose-material3-adaptive-navigation-suite = { module = "androidx.compose.material3:material3-adaptive-navigation-suite" }
compose-material3-window-size = { module = "androidx.compose.material3:material3-window-size-class" }

# Auth (§3.7)
androidx-credentials = { module = "androidx.credentials:credentials", version.ref = "credentials" }
androidx-credentials-play-services = { module = "androidx.credentials:credentials-play-services-auth", version.ref = "credentials" }
google-identity-googleid = { module = "com.google.android.libraries.identity.googleid:googleid", version.ref = "googleid" }

# Camera + QR (§7)
camerax-core = { module = "androidx.camera:camera-core", version.ref = "camerax" }
camerax-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
camerax-compose = { module = "androidx.camera:camera-compose", version.ref = "camerax" }
mlkit-barcode-scanning = { module = "com.google.mlkit:barcode-scanning", version.ref = "mlkit-barcode" }
zxing-core = { module = "com.google.zxing:core", version.ref = "zxing-core" }

# AI (§6)
firebase-ai = { module = "com.google.firebase:firebase-ai" }
firebase-appcheck-playintegrity = { module = "com.google.firebase:firebase-appcheck-playintegrity" }
firebase-appcheck-debug = { module = "com.google.firebase:firebase-appcheck-debug" }
mlkit-genai-summarization = { module = "com.google.mlkit:genai-summarization", version.ref = "mlkit-genai-summarization" }
mlkit-genai-image-description = { module = "com.google.mlkit:genai-image-description", version.ref = "mlkit-genai-image-description" }
mlkit-translate = { module = "com.google.mlkit:translate", version.ref = "mlkit-translate" }
mediapipe-tasks-genai = { module = "com.google.mediapipe:tasks-genai", version.ref = "mediapipe-genai" }
litert = { module = "com.google.ai.edge.litert:litert", version.ref = "litert" }
localagents-fc = { module = "com.google.ai.edge.localagents:localagents-fc", version.ref = "localagents-fc" }

# Widget (§11.7)
glance-appwidget = { module = "androidx.glance:glance-appwidget", version.ref = "glance" }
glance-material3 = { module = "androidx.glance:glance-material3", version.ref = "glance" }

# 3D (§12)
filament-android = { module = "com.google.android.filament:filament-android", version.ref = "filament" }
filament-utils = { module = "com.google.android.filament:filament-utils-android", version.ref = "filament" }
filament-gltfio = { module = "com.google.android.filament:gltfio-android", version.ref = "filament" }

# Perf (§9)
androidx-benchmark-macro = { module = "androidx.benchmark:benchmark-macro-junit4", version.ref = "benchmark" }
androidx-benchmark-micro = { module = "androidx.benchmark:benchmark-junit4", version.ref = "benchmark" }
androidx-uiautomator = { module = "androidx.test.uiautomator:uiautomator", version.ref = "uiautomator" }
androidx-profileinstaller = { module = "androidx.profileinstaller:profileinstaller", version.ref = "profileinstaller" }

# Testing (§10)
roborazzi = { module = "io.github.takahirom.roborazzi:roborazzi", version.ref = "roborazzi" }
roborazzi-compose = { module = "io.github.takahirom.roborazzi:roborazzi-compose", version.ref = "roborazzi" }
roborazzi-junit-rule = { module = "io.github.takahirom.roborazzi:roborazzi-junit-rule", version.ref = "roborazzi" }

kotlinx-collections-immutable = { module = "org.jetbrains.kotlinx:kotlinx-collections-immutable", version.ref = "immutable-collections" }

[bundles]
# Split out of the old kitchen-sink `compose` bundle (§1.4)
compose-core = [
    "compose-ui", "compose-ui-util", "compose-ui-tooling-preview",
    "compose-material3", "compose-lifecycle-runtime",
]
compose-adaptive = [
    "compose-material3-adaptive", "compose-material3-adaptive-layout",
    "compose-material3-adaptive-navigation", "compose-material3-adaptive-navigation-suite",
]
navigation3 = [
    "androidx-navigation3-runtime", "androidx-navigation3-ui",
    "androidx-lifecycle-viewmodel-navigation3",
]
camerax = ["camerax-core", "camerax-camera2", "camerax-lifecycle", "camerax-compose"]
roborazzi = ["roborazzi", "roborazzi-compose", "roborazzi-junit-rule"]

[plugins]
android-test = { id = "com.android.test", version.ref = "agp" }
baselineprofile = { id = "androidx.baselineprofile", version.ref = "benchmark" }
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
screenshot = { id = "com.android.compose.screenshot", version.ref = "agp" }
```

Also collapse the duplicate version refs: `gradleplugin` and `agp` are both `8.10.1` and both used; `androidx-activity` and `activity` are both `1.10.1`; `androidx-lifecycle`, `lifecycle`, and `runtime` overlap. One ref each.

Then replace every `project(":x")` with the type-safe accessor:

```kotlin
dependencies {
    implementation(projects.chai)
    implementation(projects.data)
    implementation(projects.datasource.local)
    implementation(projects.datasource.remote)
    implementation(projects.domain)
    implementation(projects.presentation)
}
```

### 3.3 Fix the confirmed bugs

#### B1 / B12 — make the room and session-type filters actually match

Two layers to fix, and doing only the first leaves the bug class in place.

**Layer 1 — derive the filter options from the data, don't hand-type them.** `loadFilters()` hardcoding `"Room A"` is the root cause of B1, and hardcoding `"keynote"` is the root cause of B12. The values must come from the same source as the values they'll be compared against:

```kotlin
// presentation/.../sessions/view/SessionsViewModel.kt

/**
 * Filter options derived from the loaded sessions, so an option can never name a
 * room, level, or format the data doesn't contain. Replaces the hand-typed list in
 * SessionsFilterPanel.loadFilters(), which named rooms ("Room A") that the venue
 * has never had.
 */
private fun buildFilterOptions(sessions: List<Session>): List<SessionsFilterOption> {
    fun options(
        category: SessionsFilterCategory,
        values: Iterable<String>,
    ) = values.asSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinctBy { it.lowercase() }
        .sorted()
        .map { SessionsFilterOption(type = category, label = it, value = it) }
        .toList()

    return options(SessionsFilterCategory.Room, sessions.flatMap { it.roomList }) +
        options(SessionsFilterCategory.Level, sessions.map { it.sessionLevel }) +
        options(SessionsFilterCategory.SessionType, sessions.map { it.sessionFormat })
}
```

Labels now come from the API rather than `strings.xml`. That's a deliberate trade: room and format names are data, not UI copy, and a translated label that no longer matches the value it filters on is exactly the bug we're fixing. Keep the `session_filter_label_*` strings only for the **category headings**.

**Layer 2 — fix the comparison.** `Session.rooms` is a comma-joined string (`SessionMapper.kt:70`), so give the domain model a parsed accessor and compare against the parts:

```kotlin
// domain/src/main/java/com/android254/domain/models/Session.kt

data class Session(
    // …
    /** Comma-joined room titles, as returned by the API. Prefer [roomList]. */
    val rooms: String,
) {
    /**
     * The individual rooms this session runs in. A session can span rooms, in which
     * case [rooms] is "Opal,Sapphire" — filtering against the joined string would
     * never match either one.
     */
    val roomList: List<String>
        get() = rooms.split(',').map(String::trim).filter(String::isNotEmpty)
}
```

Then the predicate:

```kotlin
// presentation/.../sessions/view/SessionsFilterState.kt

data class SessionsFilterState(
    val levels: List<String> = emptyList(),
    val rooms: List<String> = emptyList(),
    val sessionTypes: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
) {
    val isActive: Boolean
        get() = levels.isNotEmpty() || rooms.isNotEmpty() ||
            sessionTypes.isNotEmpty() || isBookmarked

    /** A session matches when it satisfies every *non-empty* facet. */
    fun matches(session: Session): Boolean =
        levels.matchesOrEmpty(session.sessionLevel) &&
            sessionTypes.matchesOrEmpty(session.sessionFormat) &&
            // Multi-room sessions match a filter for any one of their rooms.
            rooms.matchesAnyOrEmpty(session.roomList) &&
            (!isBookmarked || session.isBookmarked)

    // Case-insensitive throughout: the API capitalises "Keynote", the old hardcoded
    // filter said "keynote", and `contains` is case-sensitive (B12).
    private fun List<String>.matchesOrEmpty(value: String) =
        isEmpty() || any { it.equals(value.trim(), ignoreCase = true) }

    private fun List<String>.matchesAnyOrEmpty(values: List<String>) =
        isEmpty() || values.any { value -> any { it.equals(value, ignoreCase = true) } }
}
```

Note `topics` is **gone** from the state class. Per §1.4 it was never reachable; delete it along with `SessionsFilterCategory.Topic` rather than carrying a field nothing can populate.

Then collapse the filter chain — five sequential `.filter {}` calls each re-testing "is this facet empty" become one predicate:

```kotlin
// presentation/src/main/java/com/android254/presentation/sessions/view/SessionsViewModel.kt

private fun filterSessions(
    sessions: List<Session>,
    filterState: SessionsFilterState,
    selectedEventDay: EventDate,
): List<SessionPresentationModel> =
    sessions.asSequence()
        .filter { filterState.matches(it) }
        .distinctBy { it.remoteId }
        .map { it.toPresentationModel() }
        .filter { it.eventDay == selectedEventDay.value }
        .toList()
```

**Tests that must exist before this merges.** These are written to fail against today's code — that's the point:

```kotlin
// presentation/src/test/.../sessions/view/SessionsFilterStateTest.kt

@Test
fun `room filter matches a real venue room name`() {
    // Fails today: loadFilters() would have supplied "Room A".
    val state = SessionsFilterState(rooms = listOf("Opal"))
    assertThat(state.matches(sampleSession(rooms = "Opal"))).isTrue()
    assertThat(state.matches(sampleSession(rooms = "Sapphire"))).isFalse()
}

@Test
fun `room filter matches a session spanning multiple rooms`() {
    // Fails today: the comparison was against the whole joined string.
    val state = SessionsFilterState(rooms = listOf("Opal"))
    assertThat(state.matches(sampleSession(rooms = "Opal,Sapphire"))).isTrue()
}

@Test
fun `session type filter ignores case`() {
    // Fails today: B12 — "keynote" never matched "Keynote".
    val state = SessionsFilterState(sessionTypes = listOf("keynote"))
    assertThat(state.matches(sampleSession(sessionFormat = "Keynote"))).isTrue()
}

@Test
fun `empty filter state matches everything`() {
    assertThat(SessionsFilterState().matches(sampleSession())).isTrue()
}

@Test
fun `filter options never name a value absent from the data`() {
    val sessions = listOf(
        sampleSession(rooms = "Opal", sessionFormat = "Keynote", sessionLevel = "Advanced"),
        sampleSession(rooms = "Sapphire,Opal", sessionFormat = "Workshop", sessionLevel = "Advanced"),
    )
    val options = buildFilterOptions(sessions)

    // Every option must match at least one session, or it's a dead filter (B1).
    options.forEach { option ->
        val state = SessionsFilterState.from(option)
        assertThat(sessions.any { state.matches(it) })
            .withFailMessage("Filter option '%s' matches no session", option.value)
            .isTrue()
    }
    // And levels de-duplicate case-insensitively.
    assertThat(options.count { it.type == SessionsFilterCategory.Level }).isEqualTo(1)
}
```

That last test is the one worth keeping forever — it is a property, not an example, and it makes the whole B1/B12 class of bug impossible to reintroduce.

#### B2 — stop destroying user bookmarks

```kotlin
// datasource/local/.../di/DatabaseModule.kt

@Provides
@Singleton
fun providesDatabase(
    @ApplicationContext context: Context,
): Database =
    Room.databaseBuilder(context, Database::class.java, DATABASE_NAME)
        .addMigrations(*Database.ALL_MIGRATIONS)
        // No fallbackToDestructiveMigration. A missing migration must fail loudly
        // in CI, not silently delete the user's personal agenda on their phone.
        .build()

private const val DATABASE_NAME = "droidconke-database"
```

> Renaming the database from `dcke22-database` is a migration in itself — existing installs would start empty. Either keep the old name (recommended: it's invisible to users and the sync worker would refill it anyway, but a fresh DB on upgrade day means losing bookmarks, which is exactly what we're fixing) **or** ship a one-time `SupportSQLiteOpenHelper` copy. Recommendation: **keep `dcke22-database`.** The cost of the rename is real; the benefit is cosmetic.

Turn on schema export so migrations become testable and auto-migrations become possible:

```kotlin
// datasource/local/build.gradle.kts
android {
    defaultConfig {
        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
}
```

```kotlin
@Database(
    entities = [ /* ... */ ],
    version = 6,
    exportSchema = true,          // was false
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
    ],
)
```

And a migration test that runs in CI:

```kotlin
// datasource/local/src/androidTest/.../MigrationTest.kt
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        Database::class.java,
    )

    @Test
    fun migrate5To6_preservesBookmarks() {
        // Column is `sessionId`, not `session_id` — BookmarkEntity declares
        // `var sessionId: String` with no @ColumnInfo, so Room uses the property name.
        helper.createDatabase(TEST_DB, 5).use { db ->
            db.execSQL("INSERT INTO bookmarks (sessionId) VALUES ('session-42')")
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true)
        db.query("SELECT sessionId FROM bookmarks").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("session-42")
        }
    }

    private companion object { const val TEST_DB = "migration-test" }
}
```

**This test is the whole point of the fix.** It is the difference between "we intend not to lose bookmarks" and "we cannot lose bookmarks."

#### B3 / B4 — handled in §3.1 (`compilerOptions`, shared `targetSdk` version ref)

`-Xjvm-default=all` is the Kotlin 1.x spelling; on Kotlin 2.1 use `-jvm-default=all` (the compiler warns about the old name). Since the flag was never actually applied, verify nothing depended on it, then add it to the convention plugin if interface default methods on the JVM are actually wanted — for this codebase, they aren't. **Recommendation: delete the line rather than fix it.**

#### B5 — make `findActivity` nullable

```kotlin
// chai/src/main/java/com/droidconke/chai/Theme.kt

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
```

Every caller then handles `null`. After §3.4 there is exactly one caller left, and after §3.5 there are none — the whole helper goes away, because edge-to-edge is configured once in `MainActivity` rather than as a `SideEffect` in the theme. **A theme composable should not be reaching for the Activity window.**

#### B6 — hoist screen state into the ViewModel

`showMySessions` is not UI-local state; it is a *filter*, and the filter lives in `SessionsFilterState`. Derive it instead of duplicating it:

```kotlin
// SessionsUiState.kt
data class SessionsUiState(
    val sessions: List<SessionPresentationModel> = emptyList(),
    val eventDays: List<EventDate> = emptyList(),
    val sessionStatus: ResultStatus = ResultStatus.Loading,
    val showMySessionsOnly: Boolean = false,     // NEW — derived from filterState.isBookmarked
    val isFilterActive: Boolean = false,         // NEW — derived from filterState.isActive
    val isListLayout: Boolean = true,
)
```

```kotlin
// SessionsViewModel — inside the combine block
SessionsUiState(
    sessions = filteredSessions,
    eventDays = sessionDays,
    sessionStatus = getResultStatus(filteredSessions),
    showMySessionsOnly = filterState.isBookmarked,
    isFilterActive = filterState.isActive,
    isListLayout = layoutState,
)
```

```kotlin
// SessionsScreen — no local mirror of ViewModel state
CustomSwitch(
    checked = sessionsUiState.showMySessionsOnly,
    onCheckedChange = { onEvent(SessionsIntentHandler.ToggleBookmarkFilter) },
)
```

Delete `isFilterDialogOpen` (dead). Keep the bottom-sheet visibility in `rememberSaveable` — that genuinely *is* UI-local.

Also: `SessionsScreen` currently renders `ModalBottomSheet` inside `if (bottomSheetState.isVisible)`. That's inverted — `ModalBottomSheet` owns its own show/hide animation, and gating it on `isVisible` means the enter animation is skipped and `onDismissRequest` fights the guard. Use a separate boolean:

```kotlin
var showFilterSheet by rememberSaveable { mutableStateOf(false) }

if (showFilterSheet) {
    ModalBottomSheet(
        onDismissRequest = { showFilterSheet = false },
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.chaiColorsPalette.bottomSheetBackgroundColor,
    ) {
        SessionsFilterPanel(
            onDismiss = { showFilterSheet = false },
            // ...
        )
    }
}
```

Note the `containerColor` now comes from the theme instead of the hardcoded `ChaiGrey90.copy(alpha = 0.52f)` — that hardcode existed only because `MaterialTheme` had no brand `colorScheme` (A3). §3.5 removes the need for it entirely.

#### B7 — don't block first frame on the network

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the splash only until the *first cached data* is ready — never on a network call.
        splashScreen.setKeepOnScreenCondition { viewModel.isInitialising.value }

        setContent {
            ChaiTheme {
                DroidconApp(
                    windowSizeClass = calculateWindowSizeClass(this),
                )
            }
        }
    }
}
```

```kotlin
// MainViewModel.kt
@HiltViewModel
class MainViewModel @Inject constructor(
    private val remoteFeatureToggle: RemoteFeatureToggle,
    private val syncDataWorkManager: SyncDataWorkManager,
    sessionsRepo: SessionsRepo,
) : ViewModel() {

    private val _isInitialising = MutableStateFlow(true)
    val isInitialising: StateFlow<Boolean> = _isInitialising.asStateFlow()

    init {
        viewModelScope.launch {
            // Local cache decides how fast we can draw. 700 ms is a hard ceiling —
            // past that we show the UI with skeletons rather than an inert splash.
            withTimeoutOrNull(INITIALISATION_TIMEOUT_MS) {
                sessionsRepo.fetchSessions().first()
            }
            _isInitialising.value = false
        }

        // Sync is fire-and-forget and must never gate the first frame.
        viewModelScope.launch {
            runCatching { remoteFeatureToggle.syncNowIfEmpty() }
                .onSuccess { shouldSync -> if (shouldSync) syncDataWorkManager.startSync() }
                .onFailure { Timber.w(it, "Feature toggle fetch failed; continuing with cached config") }
        }
    }

    private companion object { const val INITIALISATION_TIMEOUT_MS = 700L }
}
```

#### B8 — delete the theme-blind text composables

`CParagraph`, `CPageTitle`, `CSubtitle`, `CActionText` are superseded by the `Chai*` family and hardcode colours. Grep confirms limited usage. **Delete them** rather than fix them; §5 replaces the whole typography layer anyway.

#### B9 / B16 — `kotlinx-datetime` instead of `SimpleDateFormat`

> **Corrected after review.** An earlier draft said `MainViewModel` reads wall-clock time directly and proposed a new `TimeModule`. Both were wrong. `PresentationModule.providesClock()` **already** provides `Clock`, and `MainViewModel` **already** injects it and threads `now` through to `toPresentationModel(now)`. The DI is in place; don't rebuild it.

What's actually left to fix:

| Site | Problem |
| --- | --- |
| `SessionsViewModel:63` | `SimpleDateFormat("dd")`. Does **not** inject the available `Clock`. |
| `SessionsManager:88` | `SimpleDateFormat("dd")` in the data layer. |
| `SessionMapper.kt:31` | `toPresentationModel(now: Instant = Clock.System.now())` — a default argument that silently reaches for the system clock. Callers that forget to pass `now` become untestable. |
| `DateAndTimeUtils.kt:24` | `SimpleDateFormat` plus a nullable `parse()` dereference masked by a broad catch (B16). |

> **Sequencing matters here.** All of these move to `java.time`/`kotlinx-datetime`, and `java.time` on minSdk 24 needs desugaring. **B11 must land first**, or this refactor turns a latent crash into a widespread one.

For `SessionsViewModel`, inject the `Clock` that already exists rather than adding a provider:

```kotlin
@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionsRepo: SessionsRepo,
    private val syncDataWorkManager: SyncDataWorkManager,
    private val clock: Clock,                                 // already provided
    @ConferenceTimeZone private val timeZone: TimeZone,       // new — see below
) : ViewModel()
```

Drop the default argument so the clock is always explicit:

```kotlin
// presentation/.../sessions/mappers/SessionMapper.kt
// Was: fun Session.toPresentationModel(now: Instant = Clock.System.now())
fun Session.toPresentationModel(now: Instant): SessionPresentationModel { … }
```

The "which day should be selected by default" logic is also wrong across a month boundary. Fix both:

```kotlin
// presentation/.../sessions/view/SessionsViewModel.kt

private fun defaultSelectedDay(
    days: List<EventDate>,
    clock: Clock = Clock.System,
    zone: TimeZone = TimeZone.of("Africa/Nairobi"),
): EventDate? {
    if (days.isEmpty()) return null
    val today = clock.now().toLocalDateTime(zone).date
    return days.firstOrNull { it.date == today } ?: days.first()
}
```

`EventDate` gains a real `LocalDate` instead of a `"dd"` string:

```kotlin
data class EventDate(
    val date: LocalDate,
    val label: String,       // "Day 1"
    val displayDate: String, // "6 Nov"
)
```

Add only the missing piece — the timezone qualifier — to the **existing** `PresentationModule`, which already provides `Clock`:

```kotlin
// presentation/src/main/java/com/android254/presentation/di/PresentationModule.kt
// `providesClock()` already exists here. Add the qualifier alongside it; do not
// create a new TimeModule.

@Provides
@ConferenceTimeZone
fun providesConferenceTimeZone(): TimeZone = TimeZone.of("Africa/Nairobi")
```

> **Why a fixed conference timezone and not the device's?** A session is at 14:00 EAT regardless of where the attendee's phone thinks it is. Remote attendees and anyone whose phone clock drifted should see the schedule in venue time. Render *relative* times ("in 20 minutes") from the device clock, and *absolute* times in `Africa/Nairobi`.

#### B10 — mutable state inside serializable navigation keys

```kotlin
// presentation/.../common/navigation/Screens.kt
// Keys are pure, immutable, serializable data. No resources, no display strings.
@Serializable
sealed interface Screens : NavKey {
    @Serializable data object Home : Screens
    @Serializable data object Feed : Screens
    @Serializable data object Sessions : Screens
    @Serializable data object About : Screens
    @Serializable data object Speakers : Screens
    @Serializable data object Feedback : Screens
    @Serializable data object Ticket : Screens
    @Serializable data class SessionDetails(val sessionId: String) : Screens
    @Serializable data class SpeakerDetails(val speakerName: String) : Screens
}
```

```kotlin
// presentation/.../common/navigation/TopLevelDestination.kt
// Display metadata lives here — and the label is a resource, so it localises (§14).
enum class TopLevelDestination(
    val route: Screens,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    @StringRes val labelRes: Int,
    @StringRes val contentDescriptionRes: Int,
) {
    HOME(Screens.Home, R.drawable.home_icon_filled, R.drawable.home_icon, R.string.nav_home, R.string.nav_home_cd),
    FEED(Screens.Feed, R.drawable.feed_icon_filled, R.drawable.feed_icon, R.string.nav_feed, R.string.nav_feed_cd),
    SESSIONS(Screens.Sessions, R.drawable.sessions_icon_filled, R.drawable.sessions_icon, R.string.nav_sessions, R.string.nav_sessions_cd),
    TICKET(Screens.Ticket, R.drawable.ticket_icon_filled, R.drawable.ticket_icon, R.string.nav_ticket, R.string.nav_ticket_cd),
    ABOUT(Screens.About, R.drawable.about_icon_filled, R.drawable.about_icon, R.string.nav_about, R.string.nav_about_cd),
    ;

    companion object {
        val routes: List<Screens> = entries.map { it.route }
        val routeSet: Set<Screens> = routes.toSet()
        fun fromRoute(route: Screens): TopLevelDestination? = entries.firstOrNull { it.route == route }
    }
}
```

This also gives the nav bar filled/outlined icon states (a Material 3 expectation the current single-icon model can't express).

### 3.4 Edge-to-edge and window insets

This is a **correctness** issue, not polish. The app targets SDK 36 today and 37 after §3.1 — edge-to-edge is mandatory at both. Today the app has zero insets handling and one deprecated `statusBarColor` write. **Land this before the targetSdk bump.**

**Step 1 — opt in once, in the Activity:**

```kotlin
// MainActivity.onCreate, before setContent
enableEdgeToEdge()
```

**Step 2 — delete the `SideEffect` from `chai/Theme.kt`.** `window.statusBarColor` and `navigationBarColor` are no-ops on API 35+. The correct control surface is the appearance of the *icons*, and `enableEdgeToEdge()` derives that from the theme automatically. The theme composable becomes purely declarative:

```kotlin
// Sketch only — §3.5 step 3 has the canonical ChaiTheme, including the token
// arguments and the Expressive-readiness pieces. The point here is what's *absent*.
@Composable
fun ChaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // No window reads, no Activity lookup, no SideEffect. Previews and
    // Robolectric now work identically to the real app (fixes B5).
    CompositionLocalProvider(
        LocalChaiColorsPalette provides if (darkTheme) ChaiDarkComponentColors else ChaiLightComponentColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) ChaiDarkColorScheme else ChaiLightColorScheme,
            typography = ChaiTypography,
            shapes = ChaiShapes,
            content = content,
        )
    }
}
```

**Step 3 — consume insets deliberately.** The current `MainScreen` does:

```kotlin
Scaffold(bottomBar = { … }) { padding ->
    Column(Modifier.padding(padding)) { Navigation(…) }   // ← wrong
}
```

Two problems: a `Column` wrapper adds a layout node for nothing, and `Modifier.padding(padding)` insets the *whole* nav host — so a screen that wants to draw its hero image behind the status bar can't. Correct shape:

```kotlin
@Composable
fun DroidconApp(
    windowSizeClass: WindowSizeClass,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val navigationState = rememberNavigationState(
        startRoute = Screens.Home,
        topLevelRoutes = TopLevelDestination.routeSet,
    )
    val navController = remember(navigationState) { NavigationController(navigationState) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DroidconNavigationScaffold(
        windowSizeClass = windowSizeClass,
        navigationState = navigationState,
        onNavigate = navController::navigate,
        showNavigation = uiState.isTopLevelDestination,
        liveSessions = uiState.liveSessions,
    ) { contentPadding ->
        Navigation(
            navController = navController,
            navigationState = navigationState,
            // Screens receive the padding and decide where to apply it —
            // some consume it, some draw behind it.
            contentPadding = contentPadding,
        )
    }
}
```

And per-screen, the two idioms to standardise on:

```kotlin
// (a) Screen with a top bar that should tint under the status bar:
Scaffold(
    topBar = { DroidconAppBar(scrollBehavior = scrollBehavior) },
    contentWindowInsets = WindowInsets.safeDrawing,
) { padding -> /* content */ }

// (b) Screen with a hero that draws edge-to-edge, content that doesn't:
LazyColumn(
    contentPadding = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues(),
) {
    item { HeroImage(Modifier.fillMaxWidth()) }   // behind the status bar, by design
    items(sessions) { SessionCard(it, Modifier.padding(horizontal = 20.dp)) }
}
```

**Step 4 — the keyboard.** `FeedBackScreen` and `DroidConTextField` have text input with no IME handling. Add:

```xml
<!-- app/src/main/AndroidManifest.xml -->
<activity
    android:name="com.android254.presentation.activity.MainActivity"
    android:windowSoftInputMode="adjustResize"
    ... />
```

```kotlin
Column(Modifier.imePadding()) { /* form */ }
// or, for a scrolling form:
LazyColumn(contentPadding = WindowInsets.safeDrawing.union(WindowInsets.ime).asPaddingValues())
```

**Definition of done:**
- `grep -r "statusBarColor\|navigationBarColor" --include=*.kt` returns nothing.
- Every `Scaffold` either sets `contentWindowInsets` explicitly or documents why the default is right.
- Screenshot tests (§10) include a device config with a cutout and a 3-button nav bar.
- Manual check on an API 36 device *and* an API 30 device — edge-to-edge behaviour diverges and both must look correct.

### 3.5 chai and Material 3 — the recommendation

This section answers a direct question: should chai be made to match Material 3's tokens, and if so, how?

**Short answer: keep chai, don't replace it — but chai is missing a layer, and that missing layer is exactly what Material 3's `ColorScheme` / `Typography` / `Shapes` / `MotionScheme` provide. Insert M3 underneath chai as the role layer, and keep chai on top as the brand layer.**

This is not a compromise position. It's the architecture chai was clearly reaching for — `CFonts.kt`'s own doc comment describes a `CTypography` file that was never written, and `CShapes` exists but was never wired up. The design intent is already there; the middle tier just never got built.

> **Hard prerequisite, found during review: the pinned dependency does not contain Expressive.**
>
> Compose BOM `2025.06.00` resolves `androidx.compose.material3:material3` to **1.3.2**. Verify with:
> ```bash
> ./gradlew :chai:dependencies --configuration debugCompileClasspath | grep material3
> ```
> `MaterialExpressiveTheme`, `MotionScheme`, `MaterialShapes`, `ButtonGroup`, `FloatingToolbar`, `LoadingIndicator`, `rememberAnimatedShape` and the `*Emphasized` typography roles all arrived in **1.4.x**. None of the Expressive code in this section or §5.2 compiles against 1.3.2.
>
> So §3.5 needs one of:
> - **Override material3 outside the BOM** — `implementation("androidx.compose.material3:material3:1.4.x")` alongside the BOM platform. Explicit, surgical, and it means accepting whatever stability channel 1.4.x is on.
> - **Move to a newer BOM** that ships material3 1.4.x. Cleaner, but it moves every Compose artifact at once, so it wants its own PR and its own screenshot-diff review.
>
> **Recommendation: the BOM bump, as a standalone PR immediately before §3.5**, with §10.2's screenshot suite already in place so the visual delta across every component is reviewable rather than assumed. Record the resolved material3 version in this document when it lands.
>
> Until that PR merges, §3.5 can still land its *colour* work — `ColorScheme`, `Typography` (base roles), `Shapes` all exist in 1.3.2. Only the Expressive-specific pieces are blocked. Splitting it that way is a reasonable way to get the purple fix out early.

**And the target is Material 3 Expressive, not plain M3.** That's a requirement, and it changes the token design in three specific ways that are cheaper to build in now than to retrofit:

1. **Typography needs the `*Emphasized` roles.** M3 Expressive's `Typography` carries `displayLargeEmphasized`, `headlineMediumEmphasized`, `titleLargeEmphasized`, and so on. Expressive components reach for them. If chai only fills the base roles, every emphasized style silently falls back to the Material default font — Montserrat everywhere except the places Expressive is trying to draw attention to.
2. **chai needs a motion tier, which it does not have at all today.** chai has colour, type, shape, spacing, and alpha. It has zero motion tokens. `MaterialExpressiveTheme` takes a `MotionScheme`, and that's the mechanism behind Expressive's spring-based transitions and shape morphing. This is a genuinely new tier for chai, not a rename.
3. **`CShapes`' corner scale will fight Expressive.** 3/7/9/10 dp is a tight, conservative ramp — Material's own default is 8/12/16/28 dp, and Expressive leans *further* into large and varied radii plus `MaterialShapes` morphing. Wiring `CShapes` in as-is is correct for step 1 (it preserves today's look), but the scale itself is a design decision to revisit in §5.2.

So the sequencing below is deliberate but the *shape* of the tokens is Expressive-ready from the start: §3.5 lands the full token structure under plain `MaterialTheme` so the diff reads as "the purple is gone," and §5.2 flips one call to `MaterialExpressiveTheme` and passes the motion scheme. **The tokens are not rebuilt between those two steps** — that's the whole point of getting the tiering right first.

#### Why this isn't optional: what's broken right now

I want to be concrete, because "adopt design tokens" is the kind of advice that gets deprioritised as architectural taste. These are live defects, today, on `main`:

**1. `CPrimaryButton` — chai's own primary button — renders in Material's default purple.**

```kotlin
// chai/src/main/java/com/droidconke/chai/components/CButtons.kt:86-92
colors = ButtonDefaults.buttonColors(
    contentColor = MaterialTheme.colorScheme.primary,          // ← Material default purple
    disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = AlphaDisabled),
),
```

`containerColor` isn't specified, so it falls back to `ButtonDefaults`' default — which is also `colorScheme.primary`. Since `ChaiDCKE22Theme` never passes a `colorScheme`, that's Material's stock purple. The label is drawn by `CPrimaryButtonText`, whose `TextStyle` colour comes from `chaiColorsPalette.textButtonColor` — so in light mode the app's primary button is **`ChaiBlue` (#000CEB) text on Material default purple**. Two dark, saturated colours with almost no contrast between them. `COutlinedPrimaryButton` has the same problem for its content and border.

**2. Eight call sites already read `MaterialTheme.colorScheme`, which chai has never defined.**

| File | Reads |
| --- | --- |
| `chai/components/CButtons.kt` | `colorScheme.primary` ×3 |
| `presentation/common/stepper/VerticalStepComponent.kt` | `colorScheme.primary`, `colorScheme.outline`, whole `colorScheme` |
| `presentation/feed/view/FeedScreen.kt` | `colorScheme.error`, `colorScheme.surface` |

`VerticalStepComponent` is the stepper down the left of the sessions list — a prominent element, drawn in default Material purple and default Material grey in a blue-and-teal branded app.

**3. `CShapes` is defined and never reaches `MaterialTheme`.**

```kotlin
// chai/utils/Shape.kt — small 3dp, medium 7dp, large 9dp, extraLarge 10dp
```

It's used at exactly two call sites (`CShapes.extraLarge` in `CButtons`), and never passed as `MaterialTheme(shapes = …)`. So chai buttons have 10 dp corners while every stock M3 component uses Material's default scale — a `ModalBottomSheet` with 28 dp top corners, a `Card` with 12 dp. Corner radii are inconsistent across the app by construction.

**4. `primary` means two different things in the two palettes.**

```kotlin
ChaiLightColorPalette: primary = ChaiBlue    // #000CEB — a brand accent
ChaiDarkColorPalette:  primary = ChaiBlack   // #000000 — a background
```

In light mode `primary` is the accent colour. In dark mode it's a surface colour. That's only possible because `primary` has no defined contract — it's whatever each palette's author needed at the time. And nobody noticed, because `chaiColorsPalette.primary` has **exactly one usage in the entire codebase** (`FeedBackScreen.kt:312`). The most important role in any design system is effectively unused, while the undefined Material default with the same name is used four times.

**5. The token surface is accretion, not expressiveness.**

38 tokens. Reference counts:

| | Tokens | References |
| --- | --- | --- |
| Top 7 (`textNormalColor`, `background`, `textBoldColor`, `textWeakColor`, `secondaryButtonColor`, `textTitlePrimaryColor`, `surfaces`) | 7 | **116** |
| Everything else | 31 | 58 |
| …of which have ≤2 references | **23** | 33 |

Two-thirds of all colour usage flows through seven tokens. Twenty-three tokens are effectively single-use — `toggleOffIconBackgroundColor`, `inactiveMultiSelectButtonBorderColor`, `badgeBackgroundColor`, `eventDaySelectorInactiveSurfaceColor`. These aren't design decisions promoted to tokens; they're component-local colour choices that had nowhere else to live. **That's the symptom of a missing semantic tier: with nothing to derive from, every new component must invent new global tokens.**

**6. No foreground/background pairing, so contrast cannot be guaranteed.**

chai has nine-plus text colours (`textNormalColor`, `textBoldColor`, `textWeakColor`, `textTitlePrimaryColor`, `textLabelAndHeadings`, `textButtonColor`, `secondaryButtonTextColor`, `outlinedButtonTextColor`, `eventDaySelector*TextColor`) and none is structurally bound to a background. M3's `primary`/`onPrimary`, `surface`/`onSurface` pairing makes the relationship part of the type. This is why §14's contrast test will fail on first run — nothing in the current model prevents a bad pair.

**7. No tonal elevation ramp — and dark mode is inverted relative to M3.**

Six ad-hoc background tokens (`background`, `surfaces`, `cardsBackground`, `bottomSheetBackgroundColor`, `badgeBackgroundColor`, `textFieldBackgroundColor`) stand in for what M3 models as `surface` plus a five-step `surfaceContainerLowest…Highest` ramp. And in dark mode:

```kotlin
background      = ChaiGrey90  // #20201E
surfaces        = ChaiBlack   // #000000
cardsBackground = ChaiBlack   // #000000
```

Cards are **darker** than the background they sit on. M3's elevation model is the opposite — a raised surface is lighter in dark mode. Either choice can be defended, but it needs to be made deliberately, because every stock M3 component assumes the M3 direction, and right now chai components and M3 components disagree about which way "up" is.

#### The diagnosis: chai has tiers 1 and 3, and no tier 2

Every mature design system converges on three tiers. chai has the outer two:

| Tier | What it is | chai today |
| --- | --- | --- |
| **1. Reference** | Raw palette. No meaning, just values. `ChaiBlue`, `ChaiTeal90`. | ✅ `atoms/Color.kt` — clean, well-documented, 15 colours |
| **2. System / semantic** | Role-based and theme-aware. "What is an accent? What goes on top of a surface? How does elevation read?" | ❌ **Missing.** Skipped entirely. |
| **3. Component** | Per-component decisions, derived from tier 2. `eventDaySelectorActiveSurfaceColor`. | ⚠️ 38 tokens, derived from **tier 1 directly** |

Tier 3 wiring straight to tier 1 is why there are 38 tokens for what should be a dozen, why contrast is unguaranteeable, and why stock M3 components are off-brand: there is no shared vocabulary between chai's components and Material's.

**Material 3's `ColorScheme` is a well-specified tier 2.** It's not a competing design system — it's a role vocabulary with a contrast contract, tested across light/dark, and every Compose component already speaks it. Adopting it as chai's semantic tier costs nothing in brand identity, because tier 1 (the actual brand colours) and tier 3 (the actual brand components) both stay.

#### What I recommend *against*

**Replacing chai with plain Material 3.** You'd lose named brand intent (`eventDaySelectorActiveSurfaceColor` genuinely communicates more than `tertiaryContainer`), you'd lose the module boundary that makes the design system reviewable in isolation, and you'd be forcing brand-specific decisions into roles that don't fit them. chai's naming is a real asset. Keep it.

**Leaving them parallel, as today.** Every stock M3 component is off-brand forever, every new component needs colour overrides at the call site (which `SessionsScreen` already demonstrates — `containerColor = ChaiGrey90.copy(alpha = 0.52f)` passed to `ModalBottomSheet` purely to compensate), and M3 Expressive (§5.2) is unreachable because it's built on `ColorScheme` and `MotionScheme`.

#### The implementation

**Step 1 — define the semantic tier explicitly, from tier 1.** Write the `ColorScheme` by hand from the brand palette. Do *not* derive it from the existing `ChaiColors` — that would propagate the `primary`-means-two-things problem into the new tier.

```kotlin
// chai/src/main/java/com/droidconke/chai/colors/ChaiColorScheme.kt

/**
 * Tier 2: chai's semantic colour roles, expressed as a Material 3 [ColorScheme].
 *
 * Authored directly from the tier-1 brand palette in `atoms/Color.kt`. This is the
 * single source of truth for "what is an accent", "what goes on a surface", and how
 * elevation reads — for chai components *and* for every stock Material component.
 *
 * Note `primary`: in the previous model this was ChaiBlue in light and ChaiBlack in
 * dark, i.e. an accent in one theme and a background in the other. Here it is an
 * accent in both. ChaiTeal is the dark-theme accent, which is what the old
 * `activeBottomNavIconColor` and `textLabelAndHeadings` were already using.
 */
internal val ChaiLightColorScheme: ColorScheme = lightColorScheme(
    primary = ChaiBlue,
    onPrimary = ChaiWhite,
    primaryContainer = ChaiLightGrey90,
    onPrimaryContainer = ChaiBlue,

    secondary = ChaiRed,
    onSecondary = ChaiWhite,
    secondaryContainer = ChaiRed.copy(alpha = 0.12f).compositeOver(ChaiWhite),
    onSecondaryContainer = ChaiCoal,

    tertiary = ChaiTeal,
    onTertiary = ChaiCoal,

    background = ChaiWhite,
    onBackground = ChaiGrey90,

    // The tonal ramp replaces `surfaces` / `cardsBackground` /
    // `bottomSheetBackgroundColor` / `textFieldBackgroundColor` / `badgeBackgroundColor`.
    surface = ChaiWhite,
    onSurface = ChaiGrey90,
    onSurfaceVariant = ChaiSmokeyGrey,            // was `textWeakColor`
    surfaceContainerLowest = ChaiWhite,
    surfaceContainerLow = ChaiLightGrey90,
    surfaceContainer = ChaiLightGrey,
    surfaceContainerHigh = ChaiLightGrey,
    surfaceContainerHighest = ChaiGrey.copy(alpha = 0.24f).compositeOver(ChaiWhite),

    outline = ChaiGrey,
    outlineVariant = ChaiLightGrey,

    error = ChaiRed,
    onError = ChaiWhite,
)

internal val ChaiDarkColorScheme: ColorScheme = darkColorScheme(
    primary = ChaiTeal,                            // was ChaiBlack — see KDoc above
    onPrimary = ChaiCoal,
    primaryContainer = ChaiSubtleGrey,
    onPrimaryContainer = ChaiTeal90,

    secondary = ChaiRed,
    onSecondary = ChaiCoal,

    tertiary = ChaiTeal90,
    onTertiary = ChaiCoal,

    background = ChaiGrey90,
    onBackground = ChaiWhite,

    // Elevation reads *lighter* going up, matching M3 and every stock component.
    // This is a deliberate reversal of the old model, where cards were black on a
    // grey background. Review this with design before merging.
    surface = ChaiGrey90,
    onSurface = ChaiWhite,
    onSurfaceVariant = ChaiGrey,
    surfaceContainerLowest = ChaiBlack,
    surfaceContainerLow = ChaiGrey90,
    surfaceContainer = ChaiSubtleGrey,
    surfaceContainerHigh = ChaiDarkGrey,
    surfaceContainerHighest = ChaiSmokeyGrey,

    outline = ChaiSmokeyGrey,
    outlineVariant = ChaiSubtleGrey,

    error = ChaiRed,
    onError = ChaiCoal,
)
```

> The dark-mode elevation reversal is the one change here with a visible design consequence. Flag it explicitly for whoever owns chai's visual design — it is not a change to make unilaterally in a PR titled "wire up M3 tokens." Screenshot tests (§10.2) make the before/after reviewable, which is a good reason to land §10.2 first.

**Step 2 — shrink `ChaiColors` to the tokens that are genuinely brand-specific.** Of the 38, roughly 28 are restatements of an M3 role. Keep the ~10 that carry real brand meaning:

```kotlin
// chai/src/main/java/com/droidconke/chai/colors/ChaiColors.kt

/**
 * Tier 3: component tokens that carry brand meaning Material's roles don't express.
 *
 * Everything that *is* an M3 role now lives in [ChaiLightColorScheme] /
 * [ChaiDarkColorScheme] and is read via `MaterialTheme.colorScheme`. This class holds
 * only decisions specific to droidcon's visual language.
 *
 * The bar for adding a token here: it must be used by more than one component, and it
 * must not be expressible as an M3 role. Otherwise put it in the component.
 */
@Immutable
data class ChaiColors(
    /** Nav bar label colour, which is deliberately *not* the icon colour. Brand quirk. */
    val activeBottomNavTextColor: Color,
    /** Day chips: red active / teal inactive is a droidcon signature, not a Material pattern. */
    val eventDaySelectorActiveSurfaceColor: Color,
    val eventDaySelectorActiveTextColor: Color,
    val eventDaySelectorInactiveSurfaceColor: Color,
    val eventDaySelectorInactiveTextColor: Color,
    /** Session-card accent spacers (the green/orange dividers). */
    val sessionCardAccentGreen: Color,
    val sessionCardAccentOrange: Color,
    /** Shimmer base for loading skeletons — needs to sit between surface and container. */
    val loadingShimmerColor: Color,
    /** Brand link colour; M3 has no link role. */
    val linkTextColor: Color,
    /** "Live now" pulse. Distinct from `error` even though both are red today. */
    val liveIndicatorColor: Color,
)

// Fail loudly rather than rendering invisible UI. The old default gave every token
// Color.Unspecified, so a missing provider produced a blank screen, not an error.
val LocalChaiColorsPalette = staticCompositionLocalOf<ChaiColors> {
    error("No ChaiColors provided. Wrap content in ChaiTheme { }.")
}
```

The migration mapping, for the 28 that go away:

| Removed chai token | Replacement |
| --- | --- |
| `primary` | `colorScheme.primary` |
| `background` (21 uses) | `colorScheme.background` |
| `surfaces` (10) | `colorScheme.surface` |
| `cardsBackground` (5) | `colorScheme.surfaceContainerLow` |
| `cardsBorderColor`, `bottomNavBorderColor`, `inactiveMultiSelectButtonBorderColor`, `textFieldBorderColor` | `colorScheme.outlineVariant` / `outline` |
| `textNormalColor` (25), `textBoldColor` (18) | `colorScheme.onSurface` / `onBackground` |
| `textWeakColor` (16) | `colorScheme.onSurfaceVariant` |
| `textTitlePrimaryColor` (11), `textLabelAndHeadings` (5) | `colorScheme.primary` (light) / `onBackground` (dark) — see note below |
| `secondaryButtonColor` (15), `secondaryButtonTextColor` | `colorScheme.secondary` / `onSecondary` |
| `outlinedButtonBackgroundColor`, `outlinedButtonTextColor`, `textButtonColor` | `colorScheme.surface` / `primary` |
| `activeBottomNavIconColor`, `inactiveBottomNavIconColor`, `bottomNavBackgroundColor` | `colorScheme.primary` / `onSurfaceVariant` / `surfaceContainer` |
| `bottomSheetBackgroundColor`, `badgeBackgroundColor`, `textFieldBackgroundColor` | the `surfaceContainer*` ramp |
| 6 × `toggle*` | `SwitchDefaults.colors()` — M3's `Switch` already models every one of these states |
| `radioButtonColors` | `RadioButtonDefaults.colors()` |
| `loadingStateOnCardsColor` | kept, renamed `loadingShimmerColor` |

> `textTitlePrimaryColor` is the one genuinely awkward mapping: it's `ChaiBlue` in light (an accent) and `ChaiWhite` in dark (plain foreground). That asymmetry is a design question, not a mechanical one — decide whether headings are accented or neutral, then make both themes agree. Don't preserve the inconsistency just because it's what's there.

**Step 3 — one theme entry point that provides all three tiers.**

```kotlin
// chai/src/main/java/com/droidconke/chai/Theme.kt

@Composable
fun ChaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ChaiDarkColorScheme else ChaiLightColorScheme
    val chaiColors = if (darkTheme) ChaiDarkComponentColors else ChaiLightComponentColors

    // No LocalView, no findActivity, no SideEffect, no window writes (fixes B5).
    // Edge-to-edge is configured once in MainActivity (§3.4) — a theme composable
    // has no business reaching for the Activity window.
    CompositionLocalProvider(
        LocalChaiColorsPalette provides chaiColors,
        LocalChaiMotion provides ChaiMotion.Default,
    ) {
        // Plain MaterialTheme here, MaterialExpressiveTheme in §5.2 — see step 4b.
        // The token arguments do not change between the two.
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChaiTypography,
            shapes = ChaiShapes,          // CShapes, finally connected
            content = content,
        )
    }
}

/** Retained so the ~170 existing `MaterialTheme.chaiColorsPalette` call sites keep compiling. */
val MaterialTheme.chaiColorsPalette: ChaiColors
    @Composable @ReadOnlyComposable
    get() = LocalChaiColorsPalette.current
```

Deliberately plain `MaterialTheme` here, not `MaterialExpressiveTheme`. **Phase 0 fixes correctness without changing how the app looks** — that keeps this PR reviewable as "the purple is gone" rather than "everything moved." §5.2 upgrades to `MaterialExpressiveTheme` and `MotionScheme.expressive()` as a deliberate visual change, on top of a foundation that already works.

**Step 4 — typography, the tier that was documented but never written.** `CFonts.kt` says chai's typography "consists of 2 files that work together: CTypography and CFont". `CTypography` does not exist. Build it:

```kotlin
// chai/src/main/java/com/droidconke/chai/atoms/ChaiTypography.kt

/**
 * Tier 2 typography. One [FontFamily] with weight variants, so `FontWeight.Bold` on
 * any style resolves to montserrat_bold — rather than five separate families that
 * each hardcode one weight, as in the previous `CFonts.kt`.
 */
private val Montserrat = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semi_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

val ChaiTypography: Typography = with(Typography()) {
    copy(
        displayLarge = displayLarge.withChai(FontWeight.Bold),
        displayMedium = displayMedium.withChai(FontWeight.Bold),
        displaySmall = displaySmall.withChai(FontWeight.Bold),
        headlineLarge = headlineLarge.withChai(FontWeight.Bold),
        headlineMedium = headlineMedium.withChai(FontWeight.SemiBold),
        headlineSmall = headlineSmall.withChai(FontWeight.SemiBold),
        titleLarge = titleLarge.withChai(FontWeight.SemiBold),
        titleMedium = titleMedium.withChai(FontWeight.Medium),
        titleSmall = titleSmall.withChai(FontWeight.Medium),
        bodyLarge = bodyLarge.withChai(FontWeight.Normal),
        bodyMedium = bodyMedium.withChai(FontWeight.Normal),
        bodySmall = bodySmall.withChai(FontWeight.Normal),
        labelLarge = labelLarge.withChai(FontWeight.Medium),
        labelMedium = labelMedium.withChai(FontWeight.Medium),
        labelSmall = labelSmall.withChai(FontWeight.Medium),

        // Expressive emphasis roles. Fill these even though §3.5 ships under plain
        // MaterialTheme — otherwise every Expressive component that reaches for an
        // emphasized style in §5.2 silently renders in the Material default font.
        displayLargeEmphasized = displayLargeEmphasized.withChai(FontWeight.Bold),
        displayMediumEmphasized = displayMediumEmphasized.withChai(FontWeight.Bold),
        displaySmallEmphasized = displaySmallEmphasized.withChai(FontWeight.Bold),
        headlineLargeEmphasized = headlineLargeEmphasized.withChai(FontWeight.Bold),
        headlineMediumEmphasized = headlineMediumEmphasized.withChai(FontWeight.Bold),
        headlineSmallEmphasized = headlineSmallEmphasized.withChai(FontWeight.Bold),
        titleLargeEmphasized = titleLargeEmphasized.withChai(FontWeight.Bold),
        titleMediumEmphasized = titleMediumEmphasized.withChai(FontWeight.SemiBold),
        titleSmallEmphasized = titleSmallEmphasized.withChai(FontWeight.SemiBold),
        bodyLargeEmphasized = bodyLargeEmphasized.withChai(FontWeight.Medium),
        bodyMediumEmphasized = bodyMediumEmphasized.withChai(FontWeight.Medium),
        bodySmallEmphasized = bodySmallEmphasized.withChai(FontWeight.Medium),
        labelLargeEmphasized = labelLargeEmphasized.withChai(FontWeight.SemiBold),
        labelMediumEmphasized = labelMediumEmphasized.withChai(FontWeight.SemiBold),
        labelSmallEmphasized = labelSmallEmphasized.withChai(FontWeight.SemiBold),
    )
}

private fun TextStyle.withChai(weight: FontWeight) =
    copy(fontFamily = Montserrat, fontWeight = weight)
```

> Check the exact `*Emphasized` property names against the Material 3 version you land on — this set arrived with Expressive and the surface has been moving. If some don't exist yet, fill what does and leave a TODO rather than dropping the idea; the point is that emphasis styles must carry Montserrat, not that all fifteen exist today.

**Step 4b — add the motion tier chai doesn't have.** This is the piece that makes chai able to drive Expressive rather than merely coexist with it:

```kotlin
// chai/src/main/java/com/droidconke/chai/motion/ChaiMotion.kt

/**
 * Tier 2 motion. chai had no motion tokens at all before this — every animation in
 * the app hardcoded its own `tween`/`spring`, which is why §5.4's transitions and
 * §12's cube each invented their own timing.
 *
 * [ChaiMotionScheme] is what gets handed to `MaterialExpressiveTheme` in §5.2, so
 * stock Expressive components and chai components share one motion language.
 */
val ChaiMotionScheme: MotionScheme = MotionScheme.expressive()

/**
 * Named specs for animations that aren't driven by a Material component, so screens
 * stop hardcoding durations. Read these via `MaterialTheme.motionScheme` where the
 * standard roles fit; use these only for droidcon-specific motion.
 */
@Immutable
data class ChaiMotion(
    /** The bookmark star's bounce. Deliberately springier than the standard scheme. */
    val bookmarkBounce: FiniteAnimationSpec<Float>,
    /** "Live now" pulse. Slow enough not to distract during a talk. */
    val livePulse: DurationBasedAnimationSpec<Float>,
    /** Session-card selection in two-pane mode (§4.4). */
    val paneSelection: FiniteAnimationSpec<Color>,
) {
    companion object {
        val Default = ChaiMotion(
            bookmarkBounce = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            livePulse = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            paneSelection = tween(durationMillis = 200),
        )
    }
}

val LocalChaiMotion = staticCompositionLocalOf { ChaiMotion.Default }
```

Once this exists, §5.2's change to `ChaiTheme` is genuinely one line plus one argument:

```kotlin
// §3.5 ships this:
MaterialTheme(colorScheme = …, typography = ChaiTypography, shapes = ChaiShapes, content = content)

// §5.2 changes it to this. No token rework — that's the payoff for tiering first.
MaterialExpressiveTheme(
    colorScheme = …,
    typography = ChaiTypography,
    shapes = ChaiShapes,
    motionScheme = ChaiMotionScheme,
    content = content,
)
```

chai's existing sizes map onto the M3 scale closely enough that this is mostly a rename:

| chai composable | Size / weight today | M3 role |
| --- | --- | --- |
| `ChaiTitle` | 20sp W700 | `titleLarge` (22sp) |
| `ChaiSubTitle` | 18sp W700 | `titleMedium` |
| `ChaiBodyLarge` / `Bold` | 18sp W400 / W600 | `bodyLarge` |
| `ChaiBodyMedium` / `Bold` | 16sp W400 / W600 | `bodyMedium` |
| `ChaiBodySmall` / `Bold` | 14sp W400 / W700 | `bodySmall` |
| `ChaiBodyXSmall` / `Bold` | 12sp W400 / W500 | `labelMedium` |
| `ChaiTextLabelLarge` | 11sp W400 | `labelSmall` |
| `CPrimaryButtonText` | 18sp W600 | `labelLarge` |

Then turn the 18 text composables into deprecated shims, so this doesn't have to be one 200-file PR:

```kotlin
@Deprecated(
    "Use Text(style = MaterialTheme.typography.bodyMedium). Chai text composables " +
        "cannot participate in LocalTextStyle, cannot be overridden per call site, " +
        "and hardcode lineHeight in sp (which clips at 200% font scale).",
    ReplaceWith("Text(text = bodyText, color = textColor, style = MaterialTheme.typography.bodyMedium)"),
)
@Composable
fun ChaiBodyMedium(
    modifier: Modifier = Modifier,
    bodyText: String,
    textColor: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) = Text(
    text = bodyText,
    modifier = modifier,
    color = textColor,
    style = MaterialTheme.typography.bodyMedium,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
)
```

And delete the four theme-blind ones outright (B8): `CParagraph`, `CPageTitle`, `CSubtitle`, `CActionText` hardcode `ChaiBlack` / `ChaiBlue` / `ChaiRed` and are superseded.

> **Accessibility note.** The current composables pair `fontSize` in `sp` (correct — scales with user preference) with a hardcoded `lineHeight` in `sp` (not correct — doesn't scale proportionally), which clips text at large font scales. `MaterialTheme.typography` carries the framework's tested line-height ratios. This is a real fix, not a cosmetic one. Verify at 200% in §14.

**Step 5 — a lint rule so tier 1 can't be used directly in UI code.** The whole structure decays the first time someone writes `color = ChaiBlue` in a screen:

```kotlin
// chai/src/main/java/com/droidconke/chai/lint/RawColorDetector.kt — or a detekt rule
//
// Flags direct references to tier-1 palette values (ChaiBlue, ChaiTeal90, …) outside
// the chai colors package. Tier 1 is an input to tier 2, not a UI-layer API.
```

A detekt `ForbiddenImport` rule on `com.droidconke.chai.atoms.Chai*` colours outside `com.droidconke.chai.colors` is the cheap version and takes an afternoon.

#### Sequencing

The whole thing is incremental, and every step leaves the app shippable:

1. **Write tier 2** (`ChaiColorScheme`, `ChaiTypography`, wire `ChaiShapes`). Additive — nothing breaks. The purple disappears from `CButtons`, `VerticalStepComponent`, and `FeedScreen` the moment `MaterialTheme` gets a real `colorScheme`. **This alone fixes defects 1–3 above and is worth landing on its own.**
2. **Land Roborazzi (§10.2)** so steps 3–4 are reviewable as image diffs rather than as arguments.
3. **Deprecate, don't delete.** Keep all 38 tokens as `@Deprecated` properties delegating to the new scheme. Zero call-site changes required; the compiler now lists every migration site for you.
4. **Migrate feature by feature**, as features get touched for §4 and §5 anyway. Screenshot tests catch anything that shifts.
5. **Delete the deprecated tokens** once the count reaches zero.

Do **not** do this as one PR. It touches ~170 call sites across 120 files, and a diff that size gets rubber-stamped, which defeats the point.

**Definition of done for §3.5:**
- [ ] `MaterialTheme` receives a real `colorScheme`, `typography`, and `shapes`
- [ ] `grep -rn "MaterialTheme.colorScheme"` returns only on-brand values — verified visually, not just by grep
- [ ] `LocalChaiColorsPalette` errors on missing provider instead of rendering `Color.Unspecified`
- [ ] `ChaiColors` is ≤12 tokens, and each remaining one has a KDoc explaining why it isn't an M3 role
- [ ] Dark-mode elevation direction decided **with design**, and recorded here
- [ ] `CShapes` reaches `MaterialTheme`; corner radii consistent between chai and stock components
- [ ] §14's contrast test passes for both schemes
- [ ] `ChaiTheme` no longer touches `LocalView`, the Activity, or the window (B5)
- [ ] A rule prevents tier-1 palette references outside `chai/colors`

**Expressive-readiness gate** — these are what make §5.2 a one-line change rather than a second token migration:
- [ ] `ChaiTypography` fills the `*Emphasized` roles, not just the base scale
- [ ] `ChaiMotionScheme` and `ChaiMotion` exist and are provided by `ChaiTheme`
- [ ] Swapping `MaterialTheme` → `MaterialExpressiveTheme` + `motionScheme` compiles and renders, verified on a spike branch **before** §3.5 merges — if it doesn't, the tiering is wrong and it's much cheaper to find out now
- [ ] Corner-radius scale question logged for §5.2 (keep 3/7/9/10, or move toward Expressive's larger ramp)

### 3.6 One year, one name

Four different years appear in the codebase. Pick a **year-agnostic** identity so this never recurs:

| From | To |
| --- | --- |
| `rootProject.name = "DroidconKE2023"` | `"droidconKE"` |
| `com.android254.droidconKE2023.app.DroidconKE2023App` | `ke.droidcon.kotlin.DroidconApplication` |
| `com.android254.droidconKE2023.crashlytics.CrashlyticsTree` | `ke.droidcon.kotlin.core.analytics.CrashlyticsTree` |
| `ChaiDCKE22Theme` | `ChaiTheme` |
| `Theme.DroidconKE2023` / `Theme.MySplash` | `Theme.Droidcon` / `Theme.Droidcon.Splash` |
| `com.android254.*` packages | `ke.droidcon.kotlin.*` |
| `dcke22-database` | **unchanged** (see B2 — renaming costs user data) |

The event slug (`droidconke-2025-898`) must move out of a compiled constant and into Remote Config, so a new conference year does not require an app release:

```kotlin
// datasource/remote/.../utils/RemoteConfigConfig.kt
val eventSlug: String get() = remoteConfig.getString(KEY_EVENT_SLUG)
val organizerSlug: String get() = remoteConfig.getString(KEY_ORG_SLUG)
```

```xml
<!-- datasource/remote/src/main/res/xml/remote_config_defaults.xml -->
<entry>
    <key>event_slug</key>
    <value>droidconke-2025-898</value>
</entry>
<entry>
    <key>organizer_slug</key>
    <value>droidcon-ke-645</value>
</entry>
```

`UrlProvider` then composes URLs at call time rather than as compile-time constants. **This one change means the 2027 app is a config edit, not a release.**

Do the package rename as a single mechanical PR with no behaviour change, immediately after §3.1 lands and before feature work starts. Announce it — it invalidates every open PR.

### 3.7 Credential Manager

GMS `GoogleSignIn` is deprecated and being removed. Replace `GoogleSignInHandler`:

```kotlin
// presentation/src/main/java/com/android254/presentation/auth/GoogleSignInHandler.kt

class GoogleSignInHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)

    /**
     * Returns a Google ID token, or null when the user dismisses the sheet or no
     * credential is available. [filterByAuthorizedAccounts] = false on the retry
     * pass so first-time users still see a picker.
     */
    suspend fun signIn(activityContext: Context): Result<String> = runCatching {
        val nonce = generateNonce()

        suspend fun attempt(filterByAuthorizedAccounts: Boolean): GetCredentialResponse {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setAutoSelectEnabled(filterByAuthorizedAccounts)
                .setNonce(nonce)
                .build()

            return credentialManager.getCredential(
                context = activityContext,
                request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build(),
            )
        }

        val response = try {
            attempt(filterByAuthorizedAccounts = true)
        } catch (e: NoCredentialException) {
            Timber.d(e, "No previously authorized account; showing full picker")
            attempt(filterByAuthorizedAccounts = false)
        }

        val credential = response.credential
        require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected credential type: ${credential.type}"
        }
        GoogleIdTokenCredential.createFrom(credential.data).idToken
    }.onFailure { e ->
        when (e) {
            is GetCredentialCancellationException -> Timber.d("User cancelled sign-in")
            else -> Timber.e(e, "Google sign-in failed")
        }
    }

    suspend fun signOut() {
        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
    }

    private fun generateNonce(): String =
        ByteArray(32).also { SecureRandom().nextBytes(it) }
            .let { Base64.encodeToString(it, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING) }
}
```

The nonce should ideally be issued by the backend and verified on token exchange. If `api.droidcon.co.ke` doesn't support that yet, file it as a backend ticket — a client-generated nonce still prevents token replay across *this* app's sessions, which is most of the value.

`AuthViewModel` loses the `ActivityResultLauncher` plumbing entirely; `AuthDialog` calls a suspend function. Net deletion of ~60 lines.

### 3.8 The road to AGP 9

**Most of Phase 0 is an AGP 9 migration wearing a different hat.** That's worth stating plainly, because it changes how you justify this phase: it isn't cleanup for its own sake, it's the prerequisite work for a major-version upgrade that is not optional.

> **Use the official skill.** JetBrains maintains a migration skill at
> [`Kotlin/kotlin-agent-skills` → `skills/kotlin-tooling-agp9-migration`](https://github.com/Kotlin/kotlin-agent-skills/tree/main/skills/kotlin-tooling-agp9-migration).
> Its `references/VERSION-MATRIX.md` and `references/PLUGIN-COMPATIBILITY.md` are the authoritative
> version and plugin-compatibility tables, and the numbers in this section come from them.
>
> **A scope caveat that matters for this repo:** the skill's *migration* procedure is written for
> **Kotlin Multiplatform** modules moving from `com.android.library` to the new
> `com.android.kotlin.multiplatform.library` plugin. droidconKE is not a KMP project, so that plugin
> swap **does not apply** — the library modules keep `com.android.library`. What does apply, in full,
> is the version matrix, the plugin-compatibility tables, and the removed-DSL list. Read the skill for
> those; ignore the `com.android.kotlin.multiplatform.library` sections unless and until the KMP
> question in §17.2 Q10 gets a yes — at which point the skill becomes the primary reference for that
> work too.

#### 0. Decision: both AGP 9 and targetSdk 37 — which pins the AGP version

**The decision is made: the app targets SDK 37 and runs on AGP 9.** Both. This section records what that requires, because the two constraints interact.

The JetBrains version matrix lists **Max API Level: 36.1** for **AGP 9.0** specifically. So `targetSdk = 37` rules out AGP 9.0 — it does not rule out AGP 9. What it means is:

> **The target is the earliest AGP 9.x release whose maximum supported API level is ≥ 37.** Pin that exact version in `libs.versions.toml`; do not pin `9.0.1`.

Verify before starting, because this is the number in this document most likely to have moved:

```bash
# What API levels does a given AGP support? Check the release notes, then confirm
# locally by setting compileSdk = 37 and running:
./gradlew :app:help          # fails fast with a clear message if the AGP is too old
```

Two things follow from committing to both:

1. **Order is forced.** `compileSdk` must be ≥ `targetSdk`, and AGP must support `compileSdk`. So the AGP bump and the SDK bump land **together**, in one PR, once an AGP 9.x supports 37. They are not independent steps.
2. **The prerequisite work is unchanged and can all start now.** Steps 1–5 in §7 below — Gradle 9.1, plugin bumps, Kotlin 2.3.x, `nonFinalResIds` — are required for AGP 9 regardless of which 9.x you land on, and none of them depend on the API cap. Do all of it while waiting.

**If no AGP 9.x supports API 37 when you get there**, the fallback is targetSdk 37 on the latest AGP 8.x, then AGP 9 later — but treat that as a contingency, not the plan. Record which AGP version you landed on here when it happens.

#### 1. Version requirements

From the skill's `VERSION-MATRIX.md`, against this repo's current state:

| Component | AGP 9 minimum | Recommended | Repo today | Gap |
| --- | --- | --- | --- | --- |
| AGP | 9.0.0 | **9.0.1+** | 8.10.1 | Major |
| Gradle | **9.1.0** | 9.1.0+ | **8.11.1** (root) | Major — earlier Gradle simply will not work |
| JDK | 17 | 17+ | 17 | ✅ none |
| Kotlin (KGP) | — | **2.3.0+** | 2.1.21 | Two minors |
| KSP | **2.3.1** | **2.3.6** | 2.1.21-2.0.2 | Major. KSP is no longer version-locked to the Kotlin compiler as of 2.3.0 |
| Max API level | — | **36.1** | compileSdk 36 | See §0 above |
| Android Studio | **Otter 3 (2025.2.3)** | Latest stable | — | Contributor requirement |
| IntelliJ IDEA | **Not supported** | — | — | Contributors on IDEA must switch to Studio |

Two things worth pulling out of that table for a community project:

- **Kotlin 2.1.21 → 2.3.x is its own migration**, not a footnote. Schedule it as a separate PR ahead of the AGP bump.
- **IntelliJ IDEA does not support AGP 9.** Announce this to contributors before it lands, or people will file "project won't sync" issues. Mention it in `CONTRIBUTING.md` (§15.3).

#### 2. Removed API — what §3.1 already handles

AGP 9 removes a large surface of long-deprecated API. Every §3.1 change below is on that removal list:

| Phase 0 change | Why AGP 9 needs it |
| --- | --- |
| `kotlinOptions {}` → `compilerOptions {}` (§3.1) | `kotlinOptions` is gone |
| `packagingOptions {}` → `packaging {}` (§3.1) | `packagingOptions` is gone |
| `project.buildDir` → `layout.buildDirectory` (§3.1) | `Project.buildDir` is removed in Gradle 9, which AGP 9 requires |
| `allprojects { repositories }` → `dependencyResolutionManagement` (§3.1) | Required for Isolated Projects; the old pattern fights Gradle 9 |
| Configuration cache enabled (§3.1) | Effectively mandatory; you want the fallout surfaced now, not during the upgrade |
| `buildConfig` opt-in per module (§3.1) | AGP 9 flips several `buildFeatures` defaults to off |
| `detekt { config = files() }` → `config.setFrom()` (§3.1) | Gradle 9 removes the mutable-property assignment form |

**What this repo already has right.** Two things that are usually the hardest part of an AGP 9 migration are, pleasingly, already done:

1. **`Jacoco.kt` uses the new Variant API** — `androidComponentsExtension.onVariants { }`, not `applicationVariants` / `libraryVariants`. The old Variant API is fully removed in AGP 9, and convention plugins that iterate variants are the classic thing that blocks the upgrade for months. Not an issue here.
2. **Coverage uses `enableUnitTestCoverage` / `enableAndroidTestCoverage`**, not the removed `testCoverageEnabled`.
3. `namespace` is set in every module; Kotlin DSL throughout; `nonTransitiveRClass` already true.

**What remains, specific to this repo.** These are *not* covered by §3.1–3.7 and need their own work:

#### 3. Gradle 9.1+

Two wrappers, two versions:

```
gradle/wrapper/gradle-wrapper.properties              → gradle-8.11.1
build-logic/gradle/wrapper/gradle-wrapper.properties  → gradle-8.14.1
```

`build-logic` is an included build, so it runs under the root's Gradle distribution — its own wrapper is only used if someone runs `./gradlew` from inside that directory, which nobody does. The file is stale and the mismatch is a trap for new contributors.

**Action:** delete `build-logic/gradle/`, and upgrade the root wrapper toward 9.1 incrementally:

```bash
./gradlew wrapper --gradle-version 8.14.3 --distribution-type bin   # land latest 8.x first
./gradlew build --warning-mode all                                  # burn down deprecations
./gradlew wrapper --gradle-version 9.1 --distribution-type bin      # then the major (AGP 9 minimum)
```

Do **not** jump straight to 9.1. Getting to the latest 8.x with `--warning-mode all` clean is how you find the Gradle 9 removals while you still have a working build to compare against.

#### 4. Delete the `kotlinOptions` extension hack

`KotlinAndroid.kt` ends with:

```kotlin
fun CommonExtension<*, *, *, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}
```

This reaches into the Android extension by **string name** to find an extension AGP no longer creates. §3.1 removes every call site; this function must be deleted too, or it becomes a runtime failure the first time someone reuses it. The same applies to the `kotlinOptions {}` blocks in `app/build.gradle.kts`, `presentation/build.gradle.kts`, and `chai/build.gradle.kts`.

With AGP 9's built-in Kotlin support, the modern equivalent is either the `kotlin { compilerOptions { } }` project extension or per-task configuration as shown in §3.1. Prefer the project extension once you're on 9 — it configures test and android source sets consistently, which the per-task loop does not.

#### 5. `android.nonFinalResIds=false` must go

```properties
android.nonFinalResIds=false   # gradle.properties, today
```

AGP 9 makes non-final resource IDs the only behaviour. Setting `false` today means the codebase may contain patterns that only compile with final IDs:

```bash
# Audit before flipping. These are the two patterns that break:
grep -rn "when *( *[a-zA-Z]*\.id *)" --include="*.kt" .   # `when` over R.id — needs constants
grep -rnE "@[A-Za-z]+\(R\.(id|drawable|string)\." --include="*.kt" .  # res IDs as annotation args
```

Flip the flag to `true` in its own PR, fix whatever breaks, then delete the flag entirely. §3.3 B10 (removing `@DrawableRes var icon: Int` from serializable nav keys) already resolves one instance of the underlying smell.

#### 6. Third-party Gradle plugins — the actual blocker

This is where AGP 9 migrations stall. Cross-referencing this repo's plugin list against the skill's `PLUGIN-COMPATIBILITY.md`:

| Plugin | Repo today | AGP 9 requirement | Verdict |
| --- | --- | --- | --- |
| `com.google.devtools.ksp` | 2.1.21-2.0.2 | **2.3.1 min, 2.3.3+ recommended** | Upgrade. May also need `android.disallowKotlinSourceSets=false` |
| `com.google.dagger.hilt.android` | 2.56.2 | **2.59** | Upgrade |
| `com.google.firebase.firebase-perf` | **1.4.2** | **2.0.2** | Major upgrade — as suspected, this was the most-behind plugin in the build |
| `io.gitlab.arturbosch.detekt` | 1.23.8 | < 2.0.0 needs **`android.newDsl=false` + `android.builtInKotlin=false`** | ⚠️ Flags required until detekt 2.0 |
| `org.jlleitschuh.gradle.ktlint` | 12.3.0 | needs **`android.builtInKotlin=false`** | ⚠️ Flag required, no version listed that avoids it |
| `androidx.baselineprofile` (§9.2) | planned 1.4.1 | < 1.5.0-alpha01 needs **`android.newDsl=false`** | Plan for ≥ 1.5.0-alpha01 |
| `com.google.firebase.crashlytics` | 2.9.9 | not listed | Verify — bump regardless, it's two majors behind |
| `com.google.gms.google-services` | 4.4.2 | not listed | Verify |
| `com.diffplug.spotless` | 7.0.4 | not listed | Verify |
| `com.github.ben-manes.versions` / `version-catalog-update` | 0.52.0 / 1.0.0 | not listed | Verify |

**The important finding: this repo needs both opt-out flags.**

detekt below 2.0 requires `android.newDsl=false` **and** `android.builtInKotlin=false`. The ktlint plugin requires `android.builtInKotlin=false`. Both are applied to **every subproject** in this build (§3.1). So:

```properties
# gradle.properties — temporary, remove when detekt 2.0 and a newer ktlint plugin land.
# detekt < 2.0 requires both; jlleitschuh ktlint requires builtInKotlin=false.
android.newDsl=false
android.builtInKotlin=false
```

**Which means AGP 9, for this repo, buys the version bump and very little else** — the new DSL and built-in Kotlin support, the two headline features, both have to be switched off. That is a genuine argument for **not** rushing it, and it reinforecs Option A in §0: take targetSdk 37 on AGP 8.x now, and let AGP 9 wait until detekt 2.0 ships and the flags can come off.

Do not silently carry those flags forever. Put a tracking issue on each with the version that removes it, and re-check at every dependency-update cycle (§15.2).

Three more notes:

- **Nothing in this repo is on the "broken, no workaround" list** (`com.newrelic.agent.android`, `com.huawei.agconnect.agcp`). No hard blocker.
- **§10.2's choice of Roborazzi over Paparazzi pays off here.** `app.cash.paparazzi` requires `android.newDsl=false`; Roborazzi isn't on the workaround list. That recommendation was made for other reasons, but it happens to be the AGP-9-friendlier option too.
- **Firebase Performance 1.4.2 → 2.0.2 is a major version jump.** If that migration is painful, remember §9.5 uses Firebase Performance only for custom traces. It is the most droppable dependency in the build, and dropping it is a legitimate answer.

The Firebase and Hilt plugin bumps are worth doing **now**, independently of AGP 9, as part of §3.2's catalog cleanup. Two-majors-behind build plugins are a latent blocker regardless of when the upgrade happens.

#### 7. Recommended sequencing

AGP 9 is a **separate track after Phase 0**, never bundled with it:

1. **Phase 0 §3.1–3.7** lands on current AGP. Build green, configuration cache on, warnings triaged.
2. **Gradle → latest 8.x.** Green with `--warning-mode all`.
3. **Plugin bumps:** KSP → 2.3.6, Hilt → 2.59, Firebase Perf → 2.0.2, Crashlytics → latest. Green.
4. **Kotlin 2.1.21 → 2.3.x.** Its own PR — this is a compiler upgrade, not a version bump.
5. **`nonFinalResIds` → true**, then remove the flag. Green.
6. **AGP → the earliest 9.x supporting API 37, Gradle → 9.1+, `compileSdk`/`targetSdk` → 37, all in one PR** (§0 explains why these can't be separated), with `newDsl=false` and `builtInKotlin=false`. Should be nearly mechanical, because steps 1–5 removed everything that would have broken. §3.4's insets work must already be in — edge-to-edge is enforced at this target.
7. **Verify on API 24 and on API 37.** Both ends of the supported range, because B11 is exactly what a missing low-end check costs.
8. **Remove the opt-out flags** when detekt 2.0 and a newer ktlint plugin land. This is when the migration is actually finished.

If step 7 is a large diff, one of steps 1–5 was skipped.

Validation commands from the skill, worth putting in the PR description:

```bash
./gradlew --version
./gradlew buildEnvironment | grep -e "com.android.application" -e "com.android.library"
./gradlew buildEnvironment | grep "org.jetbrains.kotlin:kotlin-gradle-plugin"
```

> **On version specifics:** the numbers above come from the JetBrains skill as of this writing. The API-level cap in §0 and the detekt/ktlint flag requirements are the two most likely to change, and both change the recommendation. Re-read `VERSION-MATRIX.md` and `PLUGIN-COMPATIBILITY.md` from the skill before starting, alongside the official [AGP release notes](https://developer.android.com/build/releases/gradle-plugin). Use **AGP Upgrade Assistant** in Android Studio for the mechanical DSL edits — it handles more than people expect.

### 3.9 Phase 0 definition of done

- [ ] `./gradlew build` passes with configuration cache enabled, twice, second run a cache hit
- [ ] `./gradlew build --warning-mode all` produces no Gradle deprecation warnings, or each remaining one has a tracking issue linked in a comment
- [ ] `./gradlew lint` passes; `lint-baseline.xml` regenerated and **shrunk** (triage what's in it)
- [ ] B1–B16 each closed by a **test**, not just a code change (§16.0 lists the test per fix)
- [ ] An API 24 instrumentation run passes — the level B11 crashed on
- [ ] `grep -rn "com.android254.droidconKE2023\|DCKE22\|DroidconKE2023"` returns nothing
- [ ] CI runs on Kotlin-only PRs (§15)
- [ ] Unused deps removed; APK size recorded as a **baseline number** in this document (§9.4)
- [ ] `targetSdk` comes from one version-catalog entry, referenced by both convention plugins (B4)
- [ ] Single Gradle wrapper; `build-logic/gradle/` deleted (§3.8)
- [ ] The `CommonExtension.kotlinOptions()` helper is deleted, not just unused (§3.8)
- [ ] AGP-9-readiness plugin bumps landed: KSP ≥ 2.3.1, Hilt ≥ 2.59, Firebase Perf ≥ 2.0.2 (§3.8 §6)
- [ ] **The §3.8 §0 decision is made and written down**: targetSdk 37 on AGP 8.x, or AGP 9 at API 36
- [ ] `CONTRIBUTING.md` notes the Android Studio requirement — IntelliJ IDEA does not support AGP 9 (§3.8 §1)

---

## 4. Phase 1 — Adaptive & large-screen support

**Depends on: Phase 0 (§3.4 insets, §3.5 M3 bridge, §3.3 B10 nav keys)**

### 4.1 Why this matters more than it looks

A conference app has an unusually strong large-screen case. During a session, the person next to you is on a tablet. Speakers review the agenda on a Chromebook. The organiser team runs the whole conference off two iPads and a Surface. And Google Play ranks large-screen quality directly — apps that pass the large-screen quality tier get promoted in tablet/foldable surfaces, which is free distribution for a community app with no marketing budget.

Right now a 13" tablet gets a phone layout stretched to 1600 dp with a bottom navigation bar at the bottom of a screen nobody's thumb can reach.

### 4.2 The three breakpoints

Standardise on Material 3's window size classes and derive a small app-level abstraction, so screens don't each re-derive layout decisions:

```kotlin
// chai/src/main/java/com/droidconke/chai/adaptive/DroidconWindowSize.kt

/**
 * App-level layout intent, derived from the window size class.
 *
 * Screens branch on this rather than on raw dp, so the breakpoints live in one place
 * and screenshot tests can force a value without a fake window.
 */
enum class DroidconWindowSize {
    /** < 600 dp: phone portrait, small foldable closed. Single pane, bottom bar. */
    Compact,
    /** 600–839 dp: tablet portrait, phone landscape, foldable open. Two panes, nav rail. */
    Medium,
    /** >= 840 dp: tablet landscape, desktop, ChromeOS. Two panes + nav drawer. */
    Expanded,
    ;

    val isSinglePane: Boolean get() = this == Compact
}

val LocalDroidconWindowSize = staticCompositionLocalOf { DroidconWindowSize.Compact }

@Composable
fun rememberDroidconWindowSize(): DroidconWindowSize {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    return remember(adaptiveInfo) {
        when (adaptiveInfo.windowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.COMPACT -> DroidconWindowSize.Compact
            WindowWidthSizeClass.MEDIUM -> DroidconWindowSize.Medium
            else -> DroidconWindowSize.Expanded
        }
    }
}
```

> **Do not** branch on `Configuration.screenWidthDp` or `LocalConfiguration.orientation`. Both are wrong in multi-window, wrong on foldables mid-fold, and wrong in a resizable ChromeOS window. `currentWindowAdaptiveInfo()` also carries posture (`isTableTopPosture`), which §4.5 uses.

### 4.3 Navigation that adapts

Replace the unconditional `BottomAppBar` with `NavigationSuiteScaffold`, which picks bottom bar / nav rail / permanent drawer per size class:

```kotlin
// presentation/src/main/java/com/android254/presentation/common/navigation/DroidconNavigationScaffold.kt

@Composable
fun DroidconNavigationScaffold(
    navigationState: NavigationState,
    onNavigate: (Screens) -> Unit,
    showNavigation: Boolean,
    liveSessions: List<SessionPresentationModel>,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val windowSize = rememberDroidconWindowSize()
    val currentRoute = navigationState.topLevelRoute

    val layoutType = when {
        !showNavigation -> NavigationSuiteType.None
        windowSize == DroidconWindowSize.Expanded -> NavigationSuiteType.NavigationDrawer
        windowSize == DroidconWindowSize.Medium -> NavigationSuiteType.NavigationRail
        else -> NavigationSuiteType.NavigationBar
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = layoutType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.chaiColorsPalette.bottomNavBackgroundColor,
            navigationRailContainerColor = MaterialTheme.chaiColorsPalette.bottomNavBackgroundColor,
            navigationDrawerContainerColor = MaterialTheme.chaiColorsPalette.bottomNavBackgroundColor,
        ),
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val selected = destination.route == currentRoute
                item(
                    selected = selected,
                    onClick = { onNavigate(destination.route) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                if (selected) destination.selectedIcon else destination.unselectedIcon,
                            ),
                            // Label is adjacent, so the icon is decorative for TalkBack.
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.labelRes)) },
                    // Announced by TalkBack instead of the raw label — lets us say
                    // "Sessions tab, 2 of 5" style descriptions (§14).
                    modifier = Modifier.semantics {
                        contentDescription = destination.contentDescriptionRes.let { "" }
                    },
                )
            }
        },
    ) {
        // The "live now / up next" rail is a phone-only affordance: on Medium and
        // Expanded it becomes a persistent supporting pane instead (§4.4).
        Column {
            if (windowSize == DroidconWindowSize.Compact && liveSessions.isNotEmpty()) {
                LiveSessionsRail(sessions = liveSessions, onSessionClick = { /* … */ })
            }
            content(PaddingValues())
        }
    }
}
```

**Two important consequences.** First, `showNavigation` replaces the current `updateBottomBarState: (Boolean) -> Unit` callback that every entry in `DroidconEntryProvider` calls as a side effect during composition — which is a side effect in composition and technically a bug (it works because it's idempotent). Derive it instead:

```kotlin
// MainViewModel / DroidconApp
val isTopLevelDestination: Boolean =
    navigationState.currentEntry in TopLevelDestination.routeSet
```

Then delete the `updateBottomBarState` parameter from `droidconEntryProvider` and all nine entries. Net deletion, and one class of composition side effect gone.

Second, the current `BottomNavigationBar` composable stacks the live-sessions `LazyRow` *above* the bar inside the same `bottomBar` slot. That's why the bar is a `Column`. Splitting them (as above) lets the live-sessions rail become a supporting pane on large screens instead of a horizontal scroller nobody sees.

### 4.4 List-detail for sessions and speakers

The two obvious two-pane candidates. Sessions first:

```kotlin
// presentation/src/main/java/com/android254/presentation/sessions/view/SessionsRoute.kt

@Composable
fun SessionsRoute(
    sessionsViewModel: SessionsViewModel = hiltViewModel(),
    navigateToSessionDetails: (String) -> Unit = {},
) {
    val uiState by sessionsViewModel.sessionsUiState.collectAsStateWithLifecycle()
    val windowSize = rememberDroidconWindowSize()

    if (windowSize.isSinglePane) {
        // Phone: list only; tapping navigates to a full screen (existing behaviour).
        SessionsScreen(
            sessionsUiState = uiState,
            onSessionClick = navigateToSessionDetails,
            onEvent = sessionsViewModel::handleEvent,
        )
    } else {
        SessionsListDetail(
            sessionsUiState = uiState,
            onEvent = sessionsViewModel::handleEvent,
        )
    }
}

@Composable
private fun SessionsListDetail(
    sessionsUiState: SessionsUiState,
    onEvent: (SessionsIntentHandler) -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

    // Predictive back within the scaffold, so back collapses the detail pane
    // before it leaves the Sessions tab.
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                SessionsScreen(
                    sessionsUiState = sessionsUiState,
                    selectedSessionId = navigator.currentDestination
                        ?.takeIf { it.pane == ListDetailPaneScaffoldRole.Detail }
                        ?.contentKey,
                    onSessionClick = { sessionId ->
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, sessionId)
                    },
                    onEvent = onEvent,
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val sessionId = navigator.currentDestination?.contentKey
                if (sessionId == null) {
                    SessionDetailPlaceholder()   // "Select a session" — never an empty grey box
                } else {
                    // key() so switching sessions gets a fresh ViewModel, matching
                    // the phone behaviour where each detail screen is a new entry.
                    key(sessionId) {
                        val viewModel = hiltViewModel<SessionDetailsViewModel, SessionDetailsViewModel.Factory>(
                            key = sessionId,
                            creationCallback = { it.create(Screens.SessionDetails(sessionId)) },
                        )
                        SessionDetailsRoute(
                            sessionId = sessionId,
                            viewModel = viewModel,
                            // No back arrow when the list is visible beside us.
                            showNavigationIcon = false,
                            onNavigationIconClick = { navigator.navigateBack() },
                        )
                    }
                }
            }
        },
    )
}
```

The list pane needs a **selected** visual state it doesn't currently have — on a phone there is no persistent selection, on a tablet there must be:

```kotlin
// SessionsCard.kt
@Composable
fun SessionCard(
    session: SessionPresentationModel,
    isSelected: Boolean = false,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.chaiColorsPalette.cardsBorderColor
        },
        label = "session-card-border",
    )

    Card(
        onClick = onClick,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier.semantics { this.selected = isSelected },
    ) { /* … */ }
}
```

Apply the same pattern to `SpeakersRoute` → `SpeakerDetailsRoute`. Feed stays single-pane (it's a linear stream). About gains a `SupportingPaneScaffold` on Expanded with the organising-team grid as the supporting pane.

**Agenda grid view.** `SessionsScreen` already has a list/agenda toggle (`isSessionLayoutList`). On Medium/Expanded the agenda mode should become what it wants to be — a **room × time grid**, the way conference schedules are actually read:

```kotlin
@Composable
fun AgendaGrid(
    rooms: List<String>,
    slots: List<TimeSlot>,
    sessionsBySlot: Map<Pair<String, TimeSlot>, SessionPresentationModel>,
    onSessionClick: (String) -> Unit,
) {
    // Sticky room headers across the top, sticky times down the left.
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    Row {
        Column(Modifier.width(TimeGutterWidth).verticalScroll(verticalScroll)) {
            Spacer(Modifier.height(RoomHeaderHeight))
            slots.forEach { slot -> TimeLabel(slot, Modifier.height(SlotHeight)) }
        }
        Column(Modifier.horizontalScroll(horizontalScroll)) {
            Row(Modifier.height(RoomHeaderHeight)) {
                rooms.forEach { room -> RoomHeader(room, Modifier.width(RoomColumnWidth)) }
            }
            Column(Modifier.verticalScroll(verticalScroll)) {
                slots.forEach { slot ->
                    Row(Modifier.height(SlotHeight)) {
                        rooms.forEach { room ->
                            AgendaCell(
                                session = sessionsBySlot[room to slot],
                                modifier = Modifier.width(RoomColumnWidth),
                                onClick = onSessionClick,
                            )
                        }
                    }
                }
            }
        }
    }
}
```

This is the single highest-value large-screen feature — it's the view organisers and speakers actually want, and it's impossible on a phone.

### 4.5 Foldables and posture

Two behaviours worth the small effort:

```kotlin
// Table-top posture (half-open, hinge horizontal): put content above the fold,
// controls below. Ideal for watching a session livestream or the agenda grid.
@Composable
fun SessionDetailsScreen(session: SessionDetailsPresentationModel) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val foldingFeature = adaptiveInfo.windowPosture.hingeList.firstOrNull()
    val isTableTop = adaptiveInfo.windowPosture.isTabletop

    if (isTableTop && foldingFeature != null) {
        Column {
            SessionBannerImage(session, Modifier.weight(1f))        // top half
            Spacer(Modifier.height(with(LocalDensity.current) { foldingFeature.bounds.height().toDp() }))
            SessionActionsAndDescription(session, Modifier.weight(1f))  // bottom half
        }
    } else {
        SessionDetailsSinglePane(session)
    }
}
```

Declare resizability so the app isn't letterboxed:

```xml
<!-- app/src/main/AndroidManifest.xml -->
<application ...>
    <activity
        android:name="com.android254.presentation.activity.MainActivity"
        android:resizeableActivity="true"
        android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation|keyboard|keyboardHidden|density|uiMode"
        android:windowSoftInputMode="adjustResize"
        android:exported="true">
        <!-- ... -->
    </activity>
    <!-- Explicitly opt in to large-screen and ChromeOS -->
    <meta-data android:name="WindowManagerPreference:FreeformWindowSize" android:value="maximize" />
    <meta-data android:name="WindowManagerPreference:SuppressWindowControlNavigationButton" android:value="true" />
</application>
```

```xml
<!-- Do not lock orientation. If any screen currently does, remove it. -->
<!-- android:screenOrientation="portrait"  ← must not appear anywhere -->
```

### 4.6 Keyboard, mouse, and stylus

ChromeOS and tablet-with-keyboard users are real. Cheap, high-signal additions:

```kotlin
// Focus traversal order for keyboard nav
Column(Modifier.focusGroup()) {
    sessions.forEach { session ->
        SessionCard(
            session = session,
            modifier = Modifier
                .focusable()
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                        onSessionClick(session.id); true
                    } else false
                },
        )
    }
}
```

```kotlin
// Hover states — invisible on phones, expected on desktop
val interactionSource = remember { MutableInteractionSource() }
val isHovered by interactionSource.collectIsHoveredAsState()
val elevation by animateDpAsState(if (isHovered) 8.dp else 2.dp, label = "card-elevation")

Card(
    onClick = onClick,
    interactionSource = interactionSource,
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
) { /* … */ }
```

Plus: right-click context menu on a session card (bookmark / share / add to calendar), and `Modifier.pointerHoverIcon(PointerIcon.Hand)` on clickables.

### 4.7 Previews and tests

Every screen gets multi-size previews, so regressions are visible in the IDE:

```kotlin
// presentation/src/main/java/com/android254/presentation/utils/Previews.kt

@Preview(name = "phone", device = Devices.PHONE, group = "size")
@Preview(name = "foldable", device = Devices.FOLDABLE, group = "size")
@Preview(name = "tablet", device = Devices.TABLET, group = "size")
@Preview(name = "desktop", device = Devices.DESKTOP, group = "size")
annotation class ChaiScreenSizePreview

@Preview(name = "light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ChaiLightAndDarkComposePreview   // already exists — keep

@ChaiLightAndDarkComposePreview
@Preview(name = "font 200%", fontScale = 2.0f)
@Preview(name = "rtl", locale = "ar")
annotation class ChaiA11yPreview
```

**Definition of done:**
- [ ] `NavigationSuiteScaffold` in place; no unconditional `BottomAppBar`
- [ ] Sessions and Speakers use `NavigableListDetailPaneScaffold` on Medium/Expanded
- [ ] Agenda grid ships on Medium/Expanded
- [ ] No `screenOrientation` lock anywhere
- [ ] Roborazzi screenshot suite (§10) covers phone/foldable/tablet/desktop × light/dark
- [ ] Manually verified on: Pixel Tablet, Pixel Fold (folded and unfolded, table-top), a resizable ChromeOS window, and split-screen on a phone
- [ ] Passes [Play's large-screen quality checklist](https://developer.android.com/docs/quality-guidelines/large-screen-app-quality)

---

## 5. Phase 2 — Design system 2.0 and world-class UX

**Depends on: §3.5 (the token bridge). Parallelisable across contributors once that lands.**

### 5.1 What "world class" actually means here

Not more animation. The conference apps people remember are the ones that answer, instantly and without being asked:

1. **Where do I need to be right now, and where next?**
2. **What did I star, and is any of it clashing?**
3. **Who is this speaker and what else are they doing?**
4. **Where's my ticket?**

Everything in this phase serves one of those four. Anything that doesn't is decoration, and decoration is what makes conference apps feel like brochures.

The single biggest UX gap today: **the app never tells you where to be.** There's a `CurrentSessionComponent` in a horizontal scroller above the bottom bar — easy to miss, and it competes with navigation. That should be the most prominent thing on the home screen during conference hours.

### 5.2 Adopt M3 Expressive

Material 3 Expressive brings shape morphing, spring-based motion schemes, button groups, and loading indicators that read as *deliberate* rather than default. Combined with §3.5's `ColorScheme` bridge:

```kotlin
// chai/src/main/java/com/droidconke/chai/Theme.kt

@Composable
fun ChaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ChaiDarkColorScheme else ChaiLightColorScheme
    val chaiColors = if (darkTheme) ChaiDarkComponentColors else ChaiLightComponentColors

    CompositionLocalProvider(
        LocalChaiColorsPalette provides chaiColors,
        LocalChaiMotion provides ChaiMotion.Default,
        LocalChaiSpacing provides ChaiSpacing,
    ) {
        // The *only* delta from §3.5's version: MaterialTheme → MaterialExpressiveTheme,
        // plus the motionScheme argument. Every token is unchanged, because §3.5 built
        // them Expressive-ready. If this is a bigger diff than that, §3.5 was skipped.
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = ChaiTypography,        // already carries the *Emphasized roles
            shapes = ChaiShapes,
            motionScheme = ChaiMotionScheme,
            content = content,
        )
    }
}
```

**Revisit the corner-radius scale here.** `CShapes` is 3/7/9/10 dp against Material's default 8/12/16/28 dp, and Expressive pushes further toward large, varied radii. Wiring `CShapes` through unchanged in §3.5 was deliberate — it preserved the existing look while the plumbing changed. Now is the point to decide whether that tight scale is a brand signature worth keeping or an artefact of an older visual language. Screenshot tests (§10.2) make the comparison concrete; take both versions to whoever owns chai's design rather than deciding it in a PR.

Concrete places Expressive pays off:

**Event-day selector → `ButtonGroup`.** `EventDaySelector` + `EventDaySelectorButton` hand-roll a segmented control. Replace with the real thing, which gets keyboard nav, correct semantics, and the expressive press-morph for free:

```kotlin
@Composable
fun EventDaySelector(
    eventDays: List<EventDate>,
    selectedDate: EventDate,
    onDaySelected: (EventDate) -> Unit,
) {
    ButtonGroup(
        overflowIndicator = { menuState ->
            FilledIconButton(onClick = { menuState.show() }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.more_event_days))
            }
        },
    ) {
        eventDays.forEach { day ->
            toggleableItem(
                checked = day == selectedDate,
                onCheckedChange = { onDaySelected(day) },
                label = day.label,
            )
        }
    }
}
```

The `overflowIndicator` matters: a three-day conference fits, a five-day one does not, and the hand-rolled `Row` currently just clips.

**Loading states → `LoadingIndicator` / `ContainedLoadingIndicator`.** There are currently four bespoke loading implementations: `AnimatedShimmerEffect`, `Loader`, `LoadingBox`, plus a Lottie `loading.json`, and per-screen skeletons (`HomeSessionLoadingComponent`, `HomeSpeakersLoadingComponent`, `SessionLoadingComponent`, `SessionLoadingCard`, `FeedLoadingComponent`). Consolidate to **two**: a shimmer skeleton for content-shaped loading, and `LoadingIndicator` for indeterminate actions. **Delete Lottie** — it's a 1.5 MB dependency with one usage (§9.4).

**Pull-to-refresh → `PullToRefreshBox`.** Kills the deprecated accompanist dep:

```kotlin
@Composable
fun HomeScreen(isSyncing: Boolean, onRefresh: () -> Unit, /* … */) {
    val pullState = rememberPullToRefreshState()

    Scaffold(topBar = { /* … */ }) { padding ->
        PullToRefreshBox(
            isRefreshing = isSyncing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier.padding(padding),
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullState,
                    isRefreshing = isSyncing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) { /* content */ }
    }
}
```

**Shape morphing on the bookmark toggle.** Starring a session is the app's most emotionally significant interaction — it's the user building their day. Make it feel like something:

```kotlin
@Composable
fun BookmarkButton(isBookmarked: Boolean, onToggle: () -> Unit) {
    val shape = rememberAnimatedShape(
        if (isBookmarked) MaterialShapes.Cookie9Sided else MaterialShapes.Circle,
    )
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isBookmarked) {
        if (isBookmarked) {
            scale.animateTo(1.25f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        }
    }

    FilledIconToggleButton(
        checked = isBookmarked,
        onCheckedChange = { onToggle() },
        shape = shape,
        modifier = Modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .semantics {
                stateDescription = if (isBookmarked) {
                    stringResource(R.string.session_starred)
                } else {
                    stringResource(R.string.session_not_starred)
                }
            },
    ) {
        Icon(
            imageVector = if (isBookmarked) ChaiIcons.StarFilled else ChaiIcons.StarOutline,
            contentDescription = null,
        )
    }
}
```

### 5.3 The home screen, rethought

Current home: header → (commented-out banner) → sessions section → speakers section → sponsors. It's a directory. During the conference it should be a **dashboard**.

Proposed structure, with content that changes by conference phase:

```kotlin
sealed interface ConferencePhase {
    data class BeforeEvent(val daysUntil: Int) : ConferencePhase
    data class DuringEvent(val day: Int, val isSessionHours: Boolean) : ConferencePhase
    data object AfterEvent : ConferencePhase
}
```

| Phase | Home screen leads with |
| --- | --- |
| **Before** | Countdown, "build your agenda" CTA, speaker highlights, ticket status |
| **During, session hours** | **Now / Next card** (biggest element on screen), then *your* starred day, then live feed |
| **During, off hours** | Tomorrow's starred sessions, "rate today's sessions" prompt, social feed |
| **After** | Personal recap (§6.10), session recordings, "what you missed", feedback prompt |

```kotlin
@Composable
fun HomeScreen(viewState: HomeViewState, /* … */) {
    LazyColumn(
        contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        when (val phase = viewState.conferencePhase) {
            is ConferencePhase.DuringEvent -> {
                // The single most important element in the app during the conference.
                item(key = "now-next") {
                    NowAndNextCard(
                        current = viewState.currentSession,
                        next = viewState.nextSession,
                        onSessionClick = onSessionClicked,
                        onDirectionsClick = onDirectionsClicked,
                    )
                }
                item(key = "my-day") {
                    MyDayTimeline(
                        sessions = viewState.myStarredSessionsToday,
                        conflicts = viewState.scheduleConflicts,
                        onSessionClick = onSessionClicked,
                    )
                }
            }
            is ConferencePhase.BeforeEvent -> {
                item(key = "countdown") { CountdownHero(daysUntil = phase.daysUntil) }
                item(key = "build-agenda") { BuildYourAgendaCta(starredCount = viewState.starredCount) }
            }
            ConferencePhase.AfterEvent -> {
                item(key = "recap") { PersonalRecapCard(recap = viewState.recap) }
            }
        }

        item(key = "speakers") { HomeSpeakersSection(/* … */) }
        item(key = "sponsors") { SponsorsSection(/* … */) }
    }
}
```

**Schedule-conflict detection** is a small change with outsized value — nobody else's conference app does it well:

```kotlin
// domain/src/main/java/com/android254/domain/usecase/DetectScheduleConflictsUseCase.kt

class DetectScheduleConflictsUseCase @Inject constructor(
    private val sessionsRepo: SessionsRepo,
) {
    /**
     * Two starred sessions conflict when their time ranges overlap by more than
     * [minOverlap]. A 1-minute overlap from back-to-back rounding isn't a conflict.
     */
    operator fun invoke(minOverlap: Duration = 5.minutes): Flow<List<ScheduleConflict>> =
        sessionsRepo.fetchSessions()
            .map { sessions -> sessions.filter { it.isBookmarked } }
            .map { starred ->
                starred
                    .sortedBy { it.startInstant }
                    .zipWithNext()
                    .mapNotNull { (a, b) ->
                        val overlap = minOf(a.endInstant, b.endInstant) - b.startInstant
                        if (overlap >= minOverlap) ScheduleConflict(a, b, overlap) else null
                    }
            }
            .distinctUntilChanged()
}

data class ScheduleConflict(
    val first: Session,
    val second: Session,
    val overlap: Duration,
)
```

Surface it as a dismissible banner on the sessions screen and in the AI summary (§6.6): *"Heads up — 'Compose Multiplatform in Production' and 'Scaling Kotlin Backends' overlap by 35 minutes."*

### 5.4 Motion that carries meaning

The app already has directional navigation transitions (`NavigationAnimation.kt`, `horizontalSlideIn` / `zoomInTransition`) — good instinct, keep it. Add:

**Shared element transitions** for session and speaker cards → detail. This is *the* transition that makes an app feel native, and Compose supports it in stable now:

```kotlin
// In the NavDisplay wrapper
SharedTransitionLayout {
    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        transitionSpec = { transitionSpec },
    ) { /* … */ }
}
```

```kotlin
// SessionCard, in the list
Image(
    // `sessionImage`, not `sessionImageUrl` — SessionPresentationModel declares
    // `val sessionImage: String = ""` (non-null, empty when absent).
    painter = rememberAsyncImagePainter(session.sessionImage),
    contentDescription = null,
    modifier = Modifier.sharedElement(
        rememberSharedContentState(key = "session-image-${session.id}"),
        animatedVisibilityScope = animatedVisibilityScope,
    ),
)
Text(
    text = session.title,
    modifier = Modifier.sharedBounds(
        rememberSharedContentState(key = "session-title-${session.id}"),
        animatedVisibilityScope = animatedVisibilityScope,
    ),
)
```

Threading `SharedTransitionScope` and `AnimatedVisibilityScope` through Navigation 3 entries needs a small `CompositionLocal`:

```kotlin
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("Not in a SharedTransitionLayout")
}
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {
    error("Not in a NavEntry animation scope")
}
```

**Live-session pulse.** The one place a subtle continuous animation earns its keep — an ongoing session should feel alive:

```kotlin
@Composable
fun LiveIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "live-pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "live-alpha",
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer { this.alpha = alpha }
                .background(MaterialTheme.colorScheme.error, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.session_live_now), style = MaterialTheme.typography.labelSmall)
    }
}
```

> **Respect the accessibility setting.** Wrap continuous animation in a check so users who disable animations aren't subjected to it:
> ```kotlin
> val animationsEnabled = LocalAccessibilityManager.current?.let { true } ?: true
> // Better: read Settings.Global.ANIMATOR_DURATION_SCALE via a small provider
> ```
> Compose respects `ANIMATOR_DURATION_SCALE` for `animate*AsState` but **not** for `rememberInfiniteTransition`. Gate it explicitly.

### 5.5 Session notes

Requested feature, and a natural fit — attendees take notes and currently switch to Keep, losing the session context.

```kotlin
// datasource/local/.../model/SessionNoteEntity.kt
@Entity(
    tableName = "session_notes",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("session_id")],
)
data class SessionNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    /** Set when the note was captured at a specific point in the talk. */
    @ColumnInfo(name = "timestamp_in_session") val timestampInSession: Duration? = null,
)
```

Design decisions worth stating:

- **Local-first, no account required.** Notes work offline and without sign-in. Optional sync to the backend later, behind a toggle. Never make note-taking depend on auth — people take notes in a basement with no signal.
- **Autosave, no save button.** Debounced write on every keystroke pause.
- **Timestamped notes.** If the session is live, stamp the offset from session start. Later, when recordings publish, deep-link the note to that moment in the video. This is the feature that makes notes worth keeping.
- **Markdown-lite.** Bullets and bold only. Not a rich text editor.
- **Export.** Share as text, or all notes for a day as one document. And feed them to §6.10's recap generator.

```kotlin
@Composable
fun SessionNotesSheet(
    sessionId: String,
    viewModel: SessionNotesViewModel = hiltViewModel(),
) {
    val note by viewModel.note.collectAsStateWithLifecycle()
    var draft by rememberSaveable(sessionId) { mutableStateOf(note?.body.orEmpty()) }

    // Debounced autosave — no save button, no lost notes.
    LaunchedEffect(draft) {
        delay(AUTOSAVE_DEBOUNCE)
        viewModel.save(sessionId, draft)
    }

    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        modifier = Modifier.fillMaxWidth().imePadding(),
        placeholder = { Text(stringResource(R.string.notes_placeholder)) },
        minLines = 6,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        supportingText = {
            AnimatedVisibility(viewModel.isSaving.collectAsStateWithLifecycle().value) {
                Text(stringResource(R.string.notes_saving), style = MaterialTheme.typography.labelSmall)
            }
        },
    )
}

private val AUTOSAVE_DEBOUNCE = 800.milliseconds
```

### 5.6 Accessibility as a first-class requirement

Currently: `contentDescription = destination.title` on nav icons that sit next to a text label (TalkBack reads the label twice), no `stateDescription` on any toggle, no `heading()` semantics, no `testTag` discipline, and no verification at large font scales.

A conference for developers, in a country with a strong accessibility community, running an app that fails TalkBack is a bad look. Concretely:

```kotlin
// 1. Decorative icons get null, not a duplicate of adjacent text
Icon(painter = …, contentDescription = null)

// 2. Toggles announce state, not just label
Modifier.semantics {
    stateDescription = if (isBookmarked) starred else notStarred
    role = Role.Switch
}

// 3. Section headers are headings, so TalkBack users can jump between them
Text(
    text = stringResource(R.string.sessions_title),
    style = MaterialTheme.typography.headlineSmall,
    modifier = Modifier.semantics { heading() },
)

// 4. Merge card contents into one focusable node — not 7 separate stops
Card(Modifier.semantics(mergeDescendants = true) {
    contentDescription = buildString {
        append(session.title); append(", ")
        append(session.speakerName); append(", ")
        append(session.timeRange); append(", ")
        append(session.room)
        if (session.isLive) append(", live now")
    }
}) { /* … */ }

// 5. Live regions for content that changes without user action
Modifier.semantics { liveRegion = LiveRegionMode.Polite }   // on the Now/Next card

// 6. Minimum touch targets — audit every IconButton
Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
```

And an automated gate, so it doesn't rot:

```kotlin
// presentation/src/androidTest/.../AccessibilityTest.kt
@Test
fun sessionsScreen_hasNoAccessibilityViolations() {
    composeTestRule.enableAccessibilityChecks()
    composeTestRule.setContent { ChaiTheme { SessionsScreen(/* … */) } }
    composeTestRule.onRoot().tryPerformAccessibilityChecks()
}
```

**Definition of done:**
- [ ] `MaterialExpressiveTheme` with brand `ColorScheme`, `Typography` (base **and** `*Emphasized`), `Shapes`, and `MotionScheme` — all four sourced from chai, none defaulted
- [ ] The diff from §3.5's `ChaiTheme` is the theme-function swap plus `motionScheme`, nothing more
- [ ] Corner-radius scale decision made with design and recorded
- [ ] Zero hardcoded `Color` literals inside composables (lint rule to enforce)
- [ ] Zero hardcoded animation durations or springs in feature code — motion comes from `MaterialTheme.motionScheme` or `LocalChaiMotion`
- [ ] Loading states consolidated from 9 implementations to 2
- [ ] Accompanist and Lottie removed
- [ ] Shared element transitions on session and speaker cards
- [ ] Notes ship, offline, no auth
- [ ] Full TalkBack pass on every screen, recorded as a checklist in the PR
- [ ] Renders correctly at 200% font scale and in RTL (`ar` pseudo-locale)

---

## 6. Phase 3 — Intelligent experiences

**Depends on: Phase 0, and `:core:testing` from §10.4 · Highest product upside, highest risk**

### 6.1 Principles before APIs

This is the section most likely to produce a demo that impresses at a talk and annoys users in the field. Four rules:

1. **Every AI feature must degrade to a non-AI feature.** No device support, no network, no quota, model refuses — the screen still works. If a feature can't degrade, it doesn't ship.
2. **On-device first, cloud when it earns it.** Free, private, offline, no quota. A conference venue's wifi is the worst network you will test on; the sessions where AI is most useful are exactly the ones where the network is most congested.
3. **Never block the UI on inference.** Streaming, cancellable, with a visible non-blocking state. A `CircularProgressIndicator` over a full screen for 8 seconds is not acceptable.
4. **Every feature behind a Remote Config flag.** `RemoteFeatureToggle` already exists. Use it as a kill switch, a per-feature rollout percentage, and a cost cap.

And one product rule: **AI must answer a question the user actually has.** "Summarise this session description" is not a real need — the description is three paragraphs and they can read it. "Which of my 14 starred sessions should I actually go to, given they clash and I care about Compose?" *is* a real need, and it's not answerable without a model.

### 6.2 The `:core:ai` abstraction

Three inference backends with different availability, latency, cost, and capability. Features must not know which one served them.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/InferenceEngine.kt

/**
 * A single text-or-multimodal inference call.
 *
 * Implementations: [OnDeviceMlKitEngine] (Gemini Nano via AICore),
 * [OnDeviceGemmaEngine] (open-weights Gemma via MediaPipe/LiteRT),
 * [CloudGeminiEngine] (Firebase AI Logic).
 *
 * Callers never construct these directly — they inject [InferenceRouter].
 */
interface InferenceEngine {
    val id: EngineId

    /** Cheap, cached, non-suspending-if-possible check. Must never throw. */
    suspend fun availability(): Availability

    /** One-shot generation. Cancellable via the calling coroutine. */
    suspend fun generate(request: InferenceRequest): Result<InferenceResponse>

    /** Token-by-token generation. Emits partial text; completes with the full response. */
    fun generateStreaming(request: InferenceRequest): Flow<InferenceChunk>

    sealed interface Availability {
        data object Available : Availability
        /** Model needs downloading. [sizeBytes] is null when unknown. */
        data class Downloadable(val sizeBytes: Long?) : Availability
        data class Downloading(val progress: Float) : Availability
        data class Unavailable(val reason: Reason) : Availability

        enum class Reason { DeviceNotSupported, NoNetwork, QuotaExceeded, FeatureDisabled, Unknown }
    }
}

enum class EngineId { MlKitOnDevice, GemmaOnDevice, CloudGemini }

data class InferenceRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val images: List<Bitmap> = emptyList(),
    /** When set, the engine must return JSON conforming to this schema. */
    val jsonSchema: JsonSchemaSpec? = null,
    val maxOutputTokens: Int = 1024,
    val temperature: Float = 0.4f,
    /** Hard requirements that disqualify engines that can't satisfy them. */
    val capabilities: Set<Capability> = emptySet(),
) {
    enum class Capability { Multimodal, StructuredOutput, FunctionCalling, LongContext }
}

data class InferenceResponse(
    val text: String,
    val servedBy: EngineId,
    val latency: Duration,
    /** Null for on-device engines. */
    val tokensUsed: Int? = null,
)

sealed interface InferenceChunk {
    data class Partial(val text: String) : InferenceChunk
    data class Complete(val response: InferenceResponse) : InferenceChunk
    data class Failed(val error: InferenceError) : InferenceChunk
}

sealed class InferenceError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoEngineAvailable(val attempted: List<EngineId>) :
        InferenceError("No inference engine available. Tried: $attempted")
    class Blocked(val reason: String) : InferenceError("Response blocked: $reason")
    class QuotaExceeded : InferenceError("Inference quota exceeded")
    class Timeout(val after: Duration) : InferenceError("Inference timed out after $after")
    class Unknown(cause: Throwable) : InferenceError(cause.message ?: "Unknown inference error", cause)
}
```

### 6.3 On-device via ML Kit GenAI (Gemini Nano)

The easiest on-device path: no model to bundle, no API key, no cost, runs through AICore on supported devices. Purpose-built APIs for summarisation, image description, rewriting, and proofreading.

**Availability is the catch** — it needs a device with AICore and Gemini Nano (Pixel 9+, Galaxy S25+, and a growing list). Plan for it being unavailable on most Kenyan-market devices; treat it as a bonus tier, not the baseline.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/engines/OnDeviceMlKitEngine.kt

@Singleton
class OnDeviceMlKitEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : InferenceEngine {

    override val id = EngineId.MlKitOnDevice

    private val summarizer: Summarizer by lazy {
        Summarization.getClient(
            SummarizerOptions.builder(context)
                .setInputType(SummarizerOptions.InputType.ARTICLE)
                .setOutputType(SummarizerOptions.OutputType.THREE_BULLETS)
                .setLanguage(SummarizerOptions.Language.ENGLISH)
                .build(),
        )
    }

    private val imageDescriber: ImageDescriber by lazy {
        ImageDescription.getClient(ImageDescriberOptions.builder(context).build())
    }

    private val availabilityCache = MutableStateFlow<Availability?>(null)

    override suspend fun availability(): Availability =
        availabilityCache.value ?: computeAvailability().also { availabilityCache.value = it }

    private suspend fun computeAvailability(): Availability = runCatching {
        when (summarizer.checkFeatureStatus().await()) {
            FeatureStatus.AVAILABLE -> Availability.Available
            FeatureStatus.DOWNLOADABLE -> Availability.Downloadable(sizeBytes = null)
            FeatureStatus.DOWNLOADING -> Availability.Downloading(progress = 0f)
            else -> Availability.Unavailable(Availability.Reason.DeviceNotSupported)
        }
    }.getOrElse { e ->
        Timber.d(e, "ML Kit GenAI unavailable on this device")
        Availability.Unavailable(Availability.Reason.DeviceNotSupported)
    }

    /**
     * Triggers the on-device model download. Safe to call repeatedly.
     * Progress is surfaced so the UI can show it rather than appearing frozen.
     */
    fun downloadModel(): Flow<Availability> = callbackFlow {
        summarizer.downloadFeature(object : DownloadCallback {
            override fun onDownloadStarted(bytesToDownload: Long) {
                trySend(Availability.Downloading(0f))
            }
            override fun onDownloadProgress(totalBytesDownloaded: Long) {
                trySend(Availability.Downloading(progress = -1f))  // indeterminate; total unknown
            }
            override fun onDownloadCompleted() {
                availabilityCache.value = Availability.Available
                trySend(Availability.Available)
                close()
            }
            override fun onDownloadFailed(e: GenAiException) {
                Timber.w(e, "GenAI feature download failed")
                trySend(Availability.Unavailable(Availability.Reason.Unknown))
                close()
            }
        })
        awaitClose { }
    }

    override suspend fun generate(request: InferenceRequest): Result<InferenceResponse> = runCatching {
        val start = TimeSource.Monotonic.markNow()

        val text = if (request.images.isNotEmpty()) {
            imageDescriber.runInference(
                ImageDescriptionRequest.builder(request.images.first()).build(),
            ).await().description
        } else {
            summarizer.runInference(
                SummarizationRequest.builder(request.prompt).build(),
            ).await().summary
        }

        InferenceResponse(text = text, servedBy = id, latency = start.elapsedNow())
    }.mapError()

    override fun generateStreaming(request: InferenceRequest): Flow<InferenceChunk> = callbackFlow {
        val start = TimeSource.Monotonic.markNow()
        val accumulated = StringBuilder()

        val result = summarizer.runInference(
            SummarizationRequest.builder(request.prompt).build(),
        ) { partial ->
            accumulated.append(partial)
            trySend(InferenceChunk.Partial(partial))
        }

        result.await()
        trySend(
            InferenceChunk.Complete(
                InferenceResponse(accumulated.toString(), id, start.elapsedNow()),
            ),
        )
        close()
        awaitClose { }
    }
}
```

**Hard limits to design around:** ML Kit GenAI does *not* support arbitrary prompts, structured JSON output, or function calling. It does summarisation, image description, rewriting, and proofreading — and nothing else. So it serves:

- ✅ Session-description summaries (§6.6, partially)
- ✅ Photo captions for the gamified check-in (§6.8)
- ✅ Proofreading user feedback and notes before submission
- ❌ The conference assistant (needs function calling → §6.7)
- ❌ Structured agenda recommendations (needs JSON schema)

### 6.4 On-device via Gemma + MediaPipe / LiteRT

This is the **open-source** answer, and the interesting one for a droidcon audience. Gemma 3 / Gemma 3n are open-weights models that run on any sufficiently capable device — no AICore dependency, no vendor gate. Gemma 3n is multimodal (text, image, audio).

It is also the option with the highest engineering cost: you own model distribution, storage, memory pressure, and a wildly variable latency profile.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/engines/OnDeviceGemmaEngine.kt

@Singleton
class OnDeviceGemmaEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelStore: GemmaModelStore,
    @IoDispatcher private val io: CoroutineDispatcher,
) : InferenceEngine {

    override val id = EngineId.GemmaOnDevice

    // A single LlmInference instance is expensive (hundreds of MB resident).
    // Created lazily, released on memory pressure — see releaseIfIdle().
    private val mutex = Mutex()
    private var engine: LlmInference? = null

    override suspend fun availability(): Availability = when {
        !deviceCanRun() -> Availability.Unavailable(Availability.Reason.DeviceNotSupported)
        modelStore.isDownloaded() -> Availability.Available
        else -> Availability.Downloadable(sizeBytes = modelStore.modelSizeBytes)
    }

    /**
     * Gemma 3 1B in int4 needs roughly 1.5 GB of headroom during inference.
     * Below 4 GB total RAM we don't offer it at all — the OOM killer will take
     * the app mid-session, which is worse than not having the feature.
     */
    private fun deviceCanRun(): Boolean {
        val am = context.getSystemService<ActivityManager>() ?: return false
        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        return memInfo.totalMem >= MIN_TOTAL_RAM_BYTES && !am.isLowRamDevice
    }

    private suspend fun obtainEngine(): LlmInference = mutex.withLock {
        engine ?: withContext(io) {
            LlmInference.createFromOptions(
                context,
                LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelStore.modelFile().absolutePath)
                    .setMaxTokens(MAX_TOKENS)
                    .setPreferredBackend(LlmInference.Backend.GPU)
                    .build(),
            ).also { engine = it }
        }
    }

    override fun generateStreaming(request: InferenceRequest): Flow<InferenceChunk> = channelFlow {
        val start = TimeSource.Monotonic.markNow()
        val llm = obtainEngine()

        val session = LlmInferenceSession.createFromOptions(
            llm,
            LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTopK(TOP_K)
                .setTemperature(request.temperature)
                .setGraphOptions(
                    GraphOptions.builder()
                        .setEnableVisionModality(request.images.isNotEmpty())
                        .build(),
                )
                .build(),
        )

        try {
            request.systemInstruction?.let { session.addQueryChunk(it) }
            session.addQueryChunk(request.prompt)
            request.images.forEach { session.addImage(BitmapImageBuilder(it).build()) }

            val accumulated = StringBuilder()
            session.generateResponseAsync { partial, done ->
                accumulated.append(partial)
                trySend(InferenceChunk.Partial(partial))
                if (done) {
                    trySend(
                        InferenceChunk.Complete(
                            InferenceResponse(accumulated.toString(), id, start.elapsedNow()),
                        ),
                    )
                    close()
                }
            }
            awaitClose { session.close() }
        } catch (e: Throwable) {
            session.close()
            trySend(InferenceChunk.Failed(InferenceError.Unknown(e)))
            close()
        }
    }.flowOn(io)

    override suspend fun generate(request: InferenceRequest): Result<InferenceResponse> = runCatching {
        generateStreaming(request)
            .filterIsInstance<InferenceChunk.Complete>()
            .first()
            .response
    }

    /** Called from onTrimMemory — a 1.5 GB resident model is not worth keeping warm. */
    suspend fun releaseIfIdle() = mutex.withLock {
        engine?.close()
        engine = null
    }

    private companion object {
        const val MIN_TOTAL_RAM_BYTES = 4L * 1024 * 1024 * 1024
        const val MAX_TOKENS = 2048
        const val TOP_K = 40
    }
}
```

**Model distribution is the real problem.** Do **not** bundle a 500 MB–1 GB model in the APK. Options, ranked:

| Option | Size cost | Notes |
| --- | --- | --- |
| **Play Asset Delivery, on-demand** | 0 in base APK | **Recommended.** Google-hosted, resumable, no auth, works with the existing AAB pipeline. |
| Direct download from Hugging Face / Kaggle | 0 | Needs auth tokens for gated models, and you own retry/resume/integrity. |
| Bundled in the APK | +500 MB | Non-starter. |

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/GemmaModelStore.kt

@Singleton
class GemmaModelStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val assetPackManager = AssetPackManagerFactory.getInstance(context)

    val modelSizeBytes: Long get() = APPROX_MODEL_BYTES

    fun isDownloaded(): Boolean = modelFileOrNull()?.exists() == true

    /**
     * Requests the on-demand asset pack containing the Gemma `.task` bundle.
     * Emits download progress so the UI can show a real progress bar with a
     * cancel button — a 500 MB download on Kenyan mobile data needs explicit,
     * informed consent and an unmetered-network default.
     */
    fun download(): Flow<DownloadState> = callbackFlow {
        val listener = AssetPackStateUpdateListener { state ->
            when (state.status()) {
                AssetPackStatus.DOWNLOADING -> trySend(
                    DownloadState.InProgress(
                        bytesDownloaded = state.bytesDownloaded(),
                        totalBytes = state.totalBytesToDownload(),
                    ),
                )
                AssetPackStatus.COMPLETED -> { trySend(DownloadState.Complete); close() }
                AssetPackStatus.FAILED -> {
                    trySend(DownloadState.Failed(state.errorCode())); close()
                }
                AssetPackStatus.WAITING_FOR_WIFI -> trySend(DownloadState.WaitingForWifi)
                AssetPackStatus.REQUIRES_USER_CONFIRMATION -> trySend(DownloadState.NeedsConfirmation)
                else -> Unit
            }
        }
        assetPackManager.registerListener(listener)
        assetPackManager.fetch(listOf(ASSET_PACK_NAME))
        awaitClose { assetPackManager.unregisterListener(listener) }
    }

    fun modelFile(): File = requireNotNull(modelFileOrNull()) { "Gemma model not downloaded" }

    private fun modelFileOrNull(): File? =
        assetPackManager.getPackLocation(ASSET_PACK_NAME)
            ?.assetsPath()
            ?.let { File(it, MODEL_FILE_NAME) }

    sealed interface DownloadState {
        data class InProgress(val bytesDownloaded: Long, val totalBytes: Long) : DownloadState {
            val fraction: Float get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
        }
        data object WaitingForWifi : DownloadState
        data object NeedsConfirmation : DownloadState
        data object Complete : DownloadState
        data class Failed(val errorCode: Int) : DownloadState
    }

    private companion object {
        const val ASSET_PACK_NAME = "gemma_model"
        const val MODEL_FILE_NAME = "gemma3-1b-it-int4.task"
        const val APPROX_MODEL_BYTES = 555L * 1024 * 1024
    }
}
```

**Honest recommendation:** ship Gemma as an **opt-in "offline AI" power-user feature**, gated behind an explicit settings toggle with the download size stated in the UI, defaulting to unmetered networks only. Do not put it in the critical path of any feature. It's a fantastic demo, a great talk, and a genuinely useful offline fallback — but it is not the primary engine.

### 6.5 Cloud via Firebase AI Logic

The workhorse. Multimodal, structured output, function calling, streaming, generous free tier through the Gemini Developer API backend.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/engines/CloudGeminiEngine.kt

@Singleton
class CloudGeminiEngine @Inject constructor(
    private val connectivity: ConnectivityMonitor,
    private val quotaGuard: InferenceQuotaGuard,
    private val featureToggle: RemoteFeatureToggle,
) : InferenceEngine {

    override val id = EngineId.CloudGemini

    private val ai by lazy { Firebase.ai(backend = GenerativeBackend.googleAI()) }

    override suspend fun availability(): Availability = when {
        !featureToggle.cloudInferenceEnabled -> Availability.Unavailable(Availability.Reason.FeatureDisabled)
        !connectivity.isOnline() -> Availability.Unavailable(Availability.Reason.NoNetwork)
        !quotaGuard.hasBudget() -> Availability.Unavailable(Availability.Reason.QuotaExceeded)
        else -> Availability.Available
    }

    private fun model(request: InferenceRequest): GenerativeModel = ai.generativeModel(
        modelName = featureToggle.cloudModelName,   // Remote Config, e.g. "gemini-2.5-flash"
        systemInstruction = request.systemInstruction?.let { content { text(it) } },
        generationConfig = generationConfig {
            temperature = request.temperature
            maxOutputTokens = request.maxOutputTokens
            request.jsonSchema?.let {
                responseMimeType = "application/json"
                responseSchema = it.toFirebaseSchema()
            }
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, HarmBlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, HarmBlockThreshold.MEDIUM_AND_ABOVE),
        ),
    )

    override suspend fun generate(request: InferenceRequest): Result<InferenceResponse> = runCatching {
        val start = TimeSource.Monotonic.markNow()

        val response = withTimeout(CLOUD_TIMEOUT) {
            model(request).generateContent(
                content {
                    request.images.forEach { image(it) }
                    text(request.prompt)
                },
            )
        }

        val text = response.text ?: throw InferenceError.Blocked(
            response.candidates.firstOrNull()?.finishReason?.name ?: "empty response",
        )

        quotaGuard.record(response.usageMetadata?.totalTokenCount ?: 0)

        InferenceResponse(
            text = text,
            servedBy = id,
            latency = start.elapsedNow(),
            tokensUsed = response.usageMetadata?.totalTokenCount,
        )
    }.mapError()

    override fun generateStreaming(request: InferenceRequest): Flow<InferenceChunk> = flow {
        val start = TimeSource.Monotonic.markNow()
        val accumulated = StringBuilder()
        var tokens: Int? = null

        model(request)
            .generateContentStream(
                content {
                    request.images.forEach { image(it) }
                    text(request.prompt)
                },
            )
            .collect { chunk ->
                chunk.text?.let {
                    accumulated.append(it)
                    emit(InferenceChunk.Partial(it))
                }
                chunk.usageMetadata?.totalTokenCount?.let { tokens = it }
            }

        tokens?.let(quotaGuard::record)
        emit(
            InferenceChunk.Complete(
                InferenceResponse(accumulated.toString(), id, start.elapsedNow(), tokens),
            ),
        )
    }.catch { e ->
        emit(InferenceChunk.Failed(e.toInferenceError()))
    }

    private companion object { val CLOUD_TIMEOUT = 30.seconds }
}
```

**App Check is mandatory before this ships.** `google-services.json` is public in this repo, so without App Check anyone can burn the project's Gemini quota:

```kotlin
// app/src/main/java/.../DroidconApplication.kt
override fun onCreate() {
    super.onCreate()
    Firebase.initialize(this)
    Firebase.appCheck.installAppCheckProviderFactory(
        if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        },
    )
}
```

Then enforce App Check on the Firebase AI Logic API in the Firebase console. Without the console-side enforcement, the client-side install does nothing.

### 6.6 The hybrid router

Where the three engines become one capability. The routing policy is a **product** decision, so keep it in one readable place:

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/InferenceRouter.kt

/**
 * Picks an engine per request and falls through on failure.
 *
 * Policy, in order:
 *   1. Drop engines that cannot satisfy the request's required [Capability] set.
 *   2. Prefer on-device when the task is small and privacy-sensitive.
 *   3. Prefer cloud when the task needs structured output, function calling,
 *      or long context — on-device engines are worse at all three.
 *   4. On failure, fall through to the next candidate. Never surface a raw
 *      backend error to the UI; surface [InferenceError.NoEngineAvailable].
 */
@Singleton
class InferenceRouter @Inject constructor(
    private val mlKit: OnDeviceMlKitEngine,
    private val gemma: OnDeviceGemmaEngine,
    private val cloud: CloudGeminiEngine,
    private val analytics: AnalyticsHelper,
) {
    suspend fun generate(
        request: InferenceRequest,
        policy: RoutingPolicy = RoutingPolicy.Balanced,
    ): Result<InferenceResponse> {
        val attempted = mutableListOf<EngineId>()

        for (engine in candidates(request, policy)) {
            if (engine.availability() !is InferenceEngine.Availability.Available) continue

            attempted += engine.id
            val result = engine.generate(request)

            result.onSuccess { response ->
                analytics.logInference(
                    engine = response.servedBy,
                    latencyMs = response.latency.inWholeMilliseconds,
                    tokens = response.tokensUsed,
                    fellBackFrom = attempted.dropLast(1),
                )
                return result
            }.onFailure { e ->
                Timber.w(e, "Engine ${engine.id} failed; trying next candidate")
                // Quota and safety blocks are terminal — don't retry elsewhere.
                if (e is InferenceError.QuotaExceeded || e is InferenceError.Blocked) return result
            }
        }

        return Result.failure(InferenceError.NoEngineAvailable(attempted))
    }

    fun generateStreaming(
        request: InferenceRequest,
        policy: RoutingPolicy = RoutingPolicy.Balanced,
    ): Flow<InferenceChunk> = flow {
        val candidates = candidates(request, policy)
        for (engine in candidates) {
            if (engine.availability() !is InferenceEngine.Availability.Available) continue

            var failed = false
            engine.generateStreaming(request).collect { chunk ->
                if (chunk is InferenceChunk.Failed) failed = true else emit(chunk)
            }
            if (!failed) return@flow
        }
        emit(InferenceChunk.Failed(InferenceError.NoEngineAvailable(candidates.map { it.id })))
    }

    private fun candidates(
        request: InferenceRequest,
        policy: RoutingPolicy,
    ): List<InferenceEngine> {
        val needsCloud = request.capabilities.any {
            it == Capability.StructuredOutput ||
                it == Capability.FunctionCalling ||
                it == Capability.LongContext
        }

        return when {
            needsCloud -> listOf(cloud)
            policy == RoutingPolicy.PrivacyFirst -> listOf(mlKit, gemma)
            policy == RoutingPolicy.QualityFirst -> listOf(cloud, gemma, mlKit)
            else -> listOf(mlKit, gemma, cloud)   // Balanced: cheapest and most private first
        }
    }

    enum class RoutingPolicy {
        /** Cheapest and most private that can do the job. Default. */
        Balanced,
        /** Never leaves the device. Used for notes and anything user-authored. */
        PrivacyFirst,
        /** Best output regardless of cost. Used for the recap and the assistant. */
        QualityFirst,
    }
}
```

The routing decision must be **visible to the user** — not as a technical detail, but as a trust signal:

```kotlin
@Composable
fun InferenceProvenanceChip(servedBy: EngineId) {
    val (label, icon) = when (servedBy) {
        EngineId.MlKitOnDevice, EngineId.GemmaOnDevice ->
            stringResource(R.string.ai_on_device) to ChaiIcons.Smartphone
        EngineId.CloudGemini ->
            stringResource(R.string.ai_cloud) to ChaiIcons.Cloud
    }

    AssistChip(
        onClick = { /* opens an explainer sheet */ },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = { Icon(icon, contentDescription = null, Modifier.size(16.dp)) },
    )
}
```

### 6.7 Feature: summary of starred sessions

The flagship. A user with 14 starred sessions across three days wants one thing: *"tell me what my conference looks like."*

This needs **structured output**, so it routes to cloud — and degrades to a deterministic, non-AI summary when cloud is unavailable.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/features/AgendaSummarizer.kt

@Serializable
data class AgendaSummary(
    /** Two sentences, max. What kind of conference this person has built. */
    val overview: String,
    /** The 3–5 themes their picks cluster around, most represented first. */
    val themes: List<String>,
    /** Human-readable clash warnings, from DetectScheduleConflictsUseCase. */
    val conflicts: List<ConflictNote>,
    /** Sessions they haven't starred that fit their themes. Max 3. */
    val suggestions: List<Suggestion>,
    /** One practical logistics note: long gaps, room-hopping, back-to-backs. */
    val logisticsTip: String?,
) {
    @Serializable
    data class ConflictNote(val sessionATitle: String, val sessionBTitle: String, val advice: String)

    @Serializable
    data class Suggestion(val sessionId: String, val title: String, val why: String)
}

@Singleton
class AgendaSummarizer @Inject constructor(
    private val router: InferenceRouter,
    private val sessionsRepo: SessionsRepo,
    private val detectConflicts: DetectScheduleConflictsUseCase,
    private val json: Json,
) {
    suspend fun summarize(): Result<AgendaSummary> {
        val all = sessionsRepo.fetchSessions().first()
        val starred = all.filter { it.isBookmarked }

        if (starred.isEmpty()) return Result.failure(EmptyAgendaException())

        val conflicts = detectConflicts().first()

        return router.generate(
            InferenceRequest(
                systemInstruction = SYSTEM_INSTRUCTION,
                prompt = buildPrompt(starred, all, conflicts),
                jsonSchema = AGENDA_SCHEMA,
                capabilities = setOf(Capability.StructuredOutput),
                temperature = 0.3f,           // low: this is analysis, not creative writing
                maxOutputTokens = 1200,
            ),
            policy = RoutingPolicy.QualityFirst,
        ).mapCatching { json.decodeFromString<AgendaSummary>(it.text) }
    }

    /**
     * The prompt sends only titles, times, rooms, levels, and speaker names —
     * never the user's notes, name, or email. Session data is already public.
     */
    private fun buildPrompt(
        starred: List<Session>,
        all: List<Session>,
        conflicts: List<ScheduleConflict>,
    ): String = buildString {
        appendLine("## The attendee's starred sessions")
        starred.sortedBy { it.startInstant }.forEach { s ->
            appendLine(
                "- id=${s.remoteId} | ${s.title} | ${s.formattedSlot()} | ${s.rooms} | " +
                    "level=${s.sessionLevel} | speakers=${s.speakers.joinToString { it.name }}",
            )
        }

        if (conflicts.isNotEmpty()) {
            appendLine()
            appendLine("## Detected clashes (already computed — explain, don't recompute)")
            conflicts.forEach { c ->
                appendLine("- \"${c.first.title}\" overlaps \"${c.second.title}\" by ${c.overlap.inWholeMinutes} min")
            }
        }

        appendLine()
        appendLine("## Sessions they have NOT starred (candidates for suggestions)")
        all.filterNot { it.isBookmarked }.take(MAX_CANDIDATES).forEach { s ->
            appendLine("- id=${s.remoteId} | ${s.title} | ${s.formattedSlot()} | level=${s.sessionLevel}")
        }
    }

    private companion object {
        const val MAX_CANDIDATES = 60

        val SYSTEM_INSTRUCTION = """
            You are a helpful conference companion for droidcon Kenya, an Android
            developer conference in Nairobi.

            Analyse the attendee's starred sessions and produce a summary.

            Rules:
            - Be specific and concrete. Reference actual session titles.
            - Never invent a session. Only use sessions from the provided lists.
            - Suggestions must use the exact `id` given, and must not clash with
              anything already starred.
            - For each clash, give one clear, actionable recommendation. It is fine
              to say "catch the recording of X and attend Y live".
            - Warm, brief, practical. No hype, no emoji, no exclamation marks.
            - If their picks are all one theme, say so plainly — that is useful signal,
              not a problem to fix.
        """.trimIndent()

        val AGENDA_SCHEMA = JsonSchemaSpec.obj(
            "overview" to JsonSchemaSpec.string("Two sentences describing the shape of their conference"),
            "themes" to JsonSchemaSpec.array(JsonSchemaSpec.string(), description = "3-5 themes, most represented first"),
            "conflicts" to JsonSchemaSpec.array(
                JsonSchemaSpec.obj(
                    "sessionATitle" to JsonSchemaSpec.string(),
                    "sessionBTitle" to JsonSchemaSpec.string(),
                    "advice" to JsonSchemaSpec.string("One actionable recommendation"),
                ),
            ),
            "suggestions" to JsonSchemaSpec.array(
                JsonSchemaSpec.obj(
                    "sessionId" to JsonSchemaSpec.string("Exact id from the candidate list"),
                    "title" to JsonSchemaSpec.string(),
                    "why" to JsonSchemaSpec.string("One sentence tied to their existing picks"),
                ),
                maxItems = 3,
            ),
            "logisticsTip" to JsonSchemaSpec.string(nullable = true),
        )
    }
}

class EmptyAgendaException : Exception("No starred sessions to summarise")
```

**The non-AI fallback, which is not a consolation prize.** Most of this is computable without a model, and the deterministic version is *more* trustworthy:

```kotlin
// domain/src/main/java/com/android254/domain/usecase/BuildDeterministicAgendaSummaryUseCase.kt

/**
 * The always-available agenda summary. No model, no network, no device requirements.
 *
 * This exists so the summary feature is never unavailable — and because for many
 * users it is genuinely the better answer: it cannot hallucinate.
 */
class BuildDeterministicAgendaSummaryUseCase @Inject constructor(
    private val sessionsRepo: SessionsRepo,
    private val detectConflicts: DetectScheduleConflictsUseCase,
) {
    operator fun invoke(): Flow<DeterministicAgendaSummary> = combine(
        sessionsRepo.fetchSessions().map { list -> list.filter { it.isBookmarked } },
        detectConflicts(),
    ) { starred, conflicts ->
        DeterministicAgendaSummary(
            sessionCount = starred.size,
            dayBreakdown = starred.groupingBy { it.eventDay }.eachCount(),
            topRooms = starred.groupingBy { it.rooms }.eachCount()
                .entries.sortedByDescending { it.value }.take(3).map { it.key },
            topicHistogram = starred.flatMap { it.topics }.groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }.associate { it.key to it.value },
            levelSpread = starred.groupingBy { it.sessionLevel }.eachCount(),
            totalListeningTime = starred.sumOf { (it.endInstant - it.startInstant).inWholeMinutes }.minutes,
            longestGap = starred.sortedBy { it.startInstant }.zipWithNext()
                .maxOfOrNull { (a, b) -> b.startInstant - a.endInstant },
            conflicts = conflicts,
        )
    }
}
```

Render the deterministic summary **always**, and layer the AI narrative on top when available:

```kotlin
@Composable
fun AgendaSummarySheet(viewModel: AgendaSummaryViewModel = hiltViewModel()) {
    val deterministic by viewModel.deterministicSummary.collectAsStateWithLifecycle()
    val aiState by viewModel.aiSummary.collectAsStateWithLifecycle()

    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Always present. Instant. Never wrong.
        AgendaStatsRow(deterministic)
        ConflictWarnings(deterministic.conflicts)

        // Additive. Never blocks. Never required.
        when (val state = aiState) {
            AiSummaryState.Idle ->
                TextButton(onClick = viewModel::generateAiSummary) {
                    Icon(ChaiIcons.Sparkle, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.agenda_ai_explain))
                }

            is AiSummaryState.Streaming -> Column {
                Text(state.partialText, style = MaterialTheme.typography.bodyMedium)
                LoadingIndicator(Modifier.padding(top = 8.dp))
            }

            is AiSummaryState.Ready -> AgendaAiNarrative(
                summary = state.summary,
                servedBy = state.servedBy,
                onSuggestionClick = viewModel::onSuggestionClicked,
                onFeedback = viewModel::onFeedback,     // thumbs up/down — see §6.11
            )

            is AiSummaryState.Unavailable -> {
                // Not an error. The stats above already answered the question.
                Text(
                    stringResource(R.string.agenda_ai_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.chaiColorsPalette.textWeakColor,
                )
            }
        }
    }
}
```

### 6.8 Feature: the conference assistant (agentic)

A conversational surface that can actually *do* things, via function calling. This is the "agents in the cloud" item, and function calling is what separates an agent from a chatbot.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/features/ConferenceAssistant.kt

@Singleton
class ConferenceAssistant @Inject constructor(
    private val sessionsRepo: SessionsRepo,
    private val speakersRepo: SpeakersRepo,
    private val featureToggle: RemoteFeatureToggle,
) {
    private val ai by lazy { Firebase.ai(backend = GenerativeBackend.googleAI()) }

    private val tools = listOf(
        FunctionDeclaration(
            name = "search_sessions",
            description = "Search conference sessions by free-text query, day, room, level, or speaker.",
            parameters = mapOf(
                "query" to Schema.string("Free-text search over titles, descriptions, and topics"),
                "day" to Schema.string("Event day, e.g. '6'. Omit for all days", nullable = true),
                "level" to Schema.enumeration(listOf("Beginner", "Intermediate", "Advanced"), nullable = true),
            ),
        ),
        FunctionDeclaration(
            name = "get_my_agenda",
            description = "Get the sessions the user has starred, in chronological order.",
            parameters = emptyMap(),
        ),
        FunctionDeclaration(
            name = "star_session",
            description = "Add a session to the user's personal agenda. Confirm with the user first.",
            parameters = mapOf("session_id" to Schema.string("The session's remote id")),
        ),
        FunctionDeclaration(
            name = "get_current_and_next",
            description = "What is happening right now and what is on next.",
            parameters = emptyMap(),
        ),
        FunctionDeclaration(
            name = "get_speaker",
            description = "Look up a speaker's bio and their sessions.",
            parameters = mapOf("name" to Schema.string("Speaker name, full or partial")),
        ),
    )

    private fun startChat() = ai.generativeModel(
        modelName = featureToggle.cloudModelName,
        systemInstruction = content { text(SYSTEM_INSTRUCTION) },
        tools = listOf(Tool.functionDeclarations(tools)),
    ).startChat()

    /**
     * Sends [message] and runs the tool loop until the model produces text.
     *
     * Bounded at [MAX_TOOL_ROUNDS] — a model that keeps calling tools without
     * concluding is a bug, and an unbounded loop is a bill.
     */
    fun send(chat: Chat, message: String): Flow<AssistantEvent> = flow {
        var response = chat.sendMessage(message)
        var rounds = 0

        while (response.functionCalls.isNotEmpty() && rounds++ < MAX_TOOL_ROUNDS) {
            val results = response.functionCalls.map { call ->
                emit(AssistantEvent.ToolCall(call.name))
                FunctionResponsePart(call.name, execute(call))
            }
            response = chat.sendMessage(content { parts.addAll(results) })
        }

        response.text?.let { emit(AssistantEvent.Message(it)) }
            ?: emit(AssistantEvent.Error("The assistant could not answer that."))
    }.catch { emit(AssistantEvent.Error(it.toUserMessage())) }

    private suspend fun execute(call: FunctionCallPart): JsonObject = when (call.name) {
        "search_sessions" -> {
            val query = call.args["query"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val day = call.args["day"]?.jsonPrimitive?.contentOrNull
            val level = call.args["level"]?.jsonPrimitive?.contentOrNull

            val results = sessionsRepo.fetchSessions().first()
                .asSequence()
                .filter { day == null || it.eventDay == day }
                .filter { level == null || it.sessionLevel.equals(level, ignoreCase = true) }
                .filter { session ->
                    query.isBlank() ||
                        session.title.contains(query, ignoreCase = true) ||
                        session.description.contains(query, ignoreCase = true) ||
                        session.topics.any { it.contains(query, ignoreCase = true) }
                }
                .take(MAX_TOOL_RESULTS)
                .map { it.toToolJson() }
                .toList()

            buildJsonObject {
                put("count", results.size)
                put("sessions", JsonArray(results))
            }
        }

        "get_my_agenda" -> buildJsonObject {
            val starred = sessionsRepo.fetchSessions().first()
                .filter { it.isBookmarked }
                .sortedBy { it.startInstant }
            put("sessions", JsonArray(starred.map { it.toToolJson() }))
        }

        "star_session" -> {
            val id = call.args["session_id"]!!.jsonPrimitive.content
            sessionsRepo.bookmarkSession(id)
            buildJsonObject { put("ok", true); put("session_id", id) }
        }

        "get_current_and_next" -> {
            val now = Clock.System.now().toEpochMilliseconds()
            buildJsonObject {
                put("current", JsonArray(sessionsRepo.fetchCurrentSessions(now).first().map { it.toToolJson() }))
                put("next", JsonArray(sessionsRepo.fetchUpNextSessions(now).first().map { it.toToolJson() }))
            }
        }

        "get_speaker" -> {
            val name = call.args["name"]!!.jsonPrimitive.content
            val speaker = speakersRepo.fetchSpeakers().first()
                .firstOrNull { it.name.contains(name, ignoreCase = true) }
            speaker?.toToolJson() ?: buildJsonObject { put("error", "No speaker matching '$name'") }
        }

        else -> buildJsonObject { put("error", "Unknown tool: ${call.name}") }
    }

    private companion object {
        const val MAX_TOOL_ROUNDS = 5
        const val MAX_TOOL_RESULTS = 20

        val SYSTEM_INSTRUCTION = """
            You are the droidcon Kenya conference assistant, inside the official
            Android app. droidcon Kenya is an Android developer conference in Nairobi.

            You have tools to search sessions, read the user's starred agenda, star
            sessions, and look up speakers. Use them — never guess at schedule facts.

            Rules:
            - All times are East Africa Time (UTC+3).
            - Before starring a session, state which session and ask the user to confirm.
            - If a tool returns nothing, say so plainly. Do not invent sessions,
              speakers, rooms, or times.
            - Keep answers under four sentences unless asked for detail.
            - You know nothing about topics outside this conference. If asked, say so
              and offer to help with the schedule instead.
        """.trimIndent()
    }
}

sealed interface AssistantEvent {
    data class ToolCall(val name: String) : AssistantEvent
    data class Message(val text: String) : AssistantEvent
    data class Error(val userMessage: String) : AssistantEvent
}
```

**On-device function calling** is possible via the AI Edge Function Calling SDK (`com.google.ai.edge.localagents:localagents-fc`) paired with the Gemma engine — a genuinely impressive fully-offline agent. Treat it as a stretch goal and a great conference talk, not a shipping requirement.

**Surface the tool calls in the UI.** Showing "Searching sessions…" while it works is both better UX and better trust than a spinner:

```kotlin
when (event) {
    is AssistantEvent.ToolCall -> AssistantToolChip(
        label = when (event.name) {
            "search_sessions" -> stringResource(R.string.assistant_searching)
            "get_my_agenda" -> stringResource(R.string.assistant_reading_agenda)
            "star_session" -> stringResource(R.string.assistant_starring)
            else -> stringResource(R.string.assistant_working)
        },
    )
    is AssistantEvent.Message -> AssistantBubble(event.text)
    is AssistantEvent.Error -> AssistantErrorBubble(event.userMessage)
}
```

### 6.9 Feature: gamified session check-in by photo

"Take a picture of the current session" → verify it plausibly matches, award points. Multimodal inference doing something a QR code can't: proving you were *in the room*.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/features/SessionPhotoVerifier.kt

@Serializable
data class PhotoVerification(
    /** Does the image plausibly show a conference talk in progress? */
    val looksLikeConferenceSession: Boolean,
    /** 0.0–1.0. Below 0.5 we do not award points. */
    val confidence: Float,
    /** Any readable text — slide titles, room signage — that supports the claim. */
    val visibleText: List<String>,
    /** One-line caption for the user's photo journal. Never mentions verification. */
    val caption: String,
    /** Why the model reached its conclusion. Shown only on failure. */
    val reasoning: String,
)

@Singleton
class SessionPhotoVerifier @Inject constructor(
    private val router: InferenceRouter,
    private val json: Json,
) {
    suspend fun verify(photo: Bitmap, session: Session): Result<PhotoVerification> =
        router.generate(
            InferenceRequest(
                systemInstruction = SYSTEM_INSTRUCTION,
                prompt = """
                    Session title: ${session.title}
                    Room: ${session.rooms}
                    Speakers: ${session.speakers.joinToString { it.name }}

                    Does this photo plausibly show this session in progress?
                """.trimIndent(),
                images = listOf(photo.downscaledForInference()),
                jsonSchema = VERIFICATION_SCHEMA,
                capabilities = setOf(Capability.Multimodal, Capability.StructuredOutput),
                temperature = 0.1f,
            ),
            policy = RoutingPolicy.QualityFirst,
        ).mapCatching { json.decodeFromString<PhotoVerification>(it.text) }

    private companion object {
        val SYSTEM_INSTRUCTION = """
            You verify photos taken at an Android developer conference.

            Say `looksLikeConferenceSession` is true when the image shows any of:
            a presentation slide or projection, a speaker addressing an audience,
            an audience seated facing a stage, or conference room signage.

            Be generous about photo quality — these are phone photos taken from the
            back of a dim room. Be strict about the subject: a selfie in a corridor,
            a plate of food, or a screenshot is not a session.

            You are NOT confirming the specific session, only that this is plausibly
            a conference talk. Do not accuse the user of anything. The caption must
            be a warm, factual one-liner about the photo.
        """.trimIndent()

        val VERIFICATION_SCHEMA = JsonSchemaSpec.obj(
            "looksLikeConferenceSession" to JsonSchemaSpec.boolean(),
            "confidence" to JsonSchemaSpec.number("0.0 to 1.0"),
            "visibleText" to JsonSchemaSpec.array(JsonSchemaSpec.string()),
            "caption" to JsonSchemaSpec.string("One warm, factual sentence"),
            "reasoning" to JsonSchemaSpec.string(),
        )
    }
}

/**
 * Inference cost scales with pixels. 768 px on the long edge is plenty for
 * "is this a conference talk", and cuts upload size by ~90% on a 12 MP photo —
 * which matters a lot on venue wifi.
 */
private fun Bitmap.downscaledForInference(maxEdge: Int = 768): Bitmap {
    val scale = maxEdge.toFloat() / maxOf(width, height)
    if (scale >= 1f) return this
    return scale(width = (width * scale).toInt(), height = (height * scale).toInt())
}
```

**The gamification design matters more than the model.** Points for attendance alone produce photo-farming. Structure it so the incentive is the behaviour you actually want:

| Action | Points | Guard |
| --- | --- | --- |
| Check in to a session you starred | 10 | Only during the session's time window, once per session |
| Check in to a session you didn't star | 15 | Rewards exploring outside your bubble |
| Leave a rating + one-line note | 20 | Only after check-in — the note is the real goal |
| Visit a sponsor booth (QR, §7) | 10 | Once per sponsor |
| Complete a track (all sessions in a room, one day) | 50 | Bonus |
| Meet another attendee (badge scan, §11.2) | 5 | Max 20 per day; anti-farming |

Points live on the device, sync to Firestore when signed in. The leaderboard is **opt-in** — plenty of people don't want to be on it, and an involuntary leaderboard is a reason to uninstall.

> **Privacy: photos never leave the device unless the user says so.** The verification image goes to the model and is discarded; it is not uploaded to any storage bucket. The photo journal is local. State this in the UI at capture time, not buried in a policy.

### 6.10 Feature: live translation and captions

The highest-impact accessibility feature available, and it runs entirely on-device for free via ML Kit Translation.

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/features/SessionTranslator.kt

@Singleton
class SessionTranslator @Inject constructor() {

    private val translators = ConcurrentHashMap<String, Translator>()

    /**
     * Translates session titles and descriptions on-device. Model packs are ~30 MB
     * per language pair, downloaded once, on unmetered networks only.
     */
    suspend fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String = TranslateLanguage.ENGLISH,
    ): Result<String> = runCatching {
        val translator = translators.getOrPut("$sourceLanguage-$targetLanguage") {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build(),
            )
        }

        translator.downloadModelIfNeeded(
            DownloadConditions.Builder().requireWifi().build(),
        ).await()

        translator.translate(text).await()
    }
}
```

Priority language pairs for droidcon Kenya: **English → Swahili** (and back), then French and Portuguese for pan-African attendees.

Wire it as a per-screen toggle, not a global setting — someone reading English session titles may still want a Swahili description.

**Live audio captioning** (transcribe a talk in real time) is technically reachable via Gemma 3n's audio modality or a cloud speech API, but: it needs the microphone during a talk, it drains battery, it's ethically fraught to record speakers without consent, and quality in a large room with poor acoustics is bad. **Recommendation: don't build it.** Instead, lobby the organisers for official captions and surface *those*. That is the version that actually helps.

### 6.11 Feature: post-conference recap

The feature nobody builds and everybody would love — turn what you did into something you can share and act on.

```kotlin
@Serializable
data class ConferenceRecap(
    val headline: String,                    // "Your droidcon was about Compose and CI"
    val narrative: String,                   // 3-4 sentences
    val standoutSessions: List<String>,      // titles, from their highest-rated
    val themesExplored: List<String>,
    val followUps: List<FollowUp>,           // concrete next actions from their notes
    val shareableStat: String,               // "You attended 11 talks across 3 days"
) {
    @Serializable
    data class FollowUp(val action: String, val context: String)
}
```

Inputs: check-ins, starred sessions, ratings, and — with explicit consent — their notes. Because notes are personal, this routes `PrivacyFirst` when the user opts out of cloud, and the deterministic stats version is always available.

Deliver it as a **notification 2 days after the conference** linking to a shareable card. That notification is also the strongest possible re-engagement hook for next year.

### 6.12 Cost, privacy, safety, and kill switches

**Quota guard** — a client-side circuit breaker so no single device (or a bug) can drain the project's quota:

```kotlin
// core/ai/src/main/kotlin/ke/droidcon/kotlin/core/ai/InferenceQuotaGuard.kt

/**
 * Per-device, per-day inference budget. Not a security boundary — App Check and
 * server-side quotas are that — but it stops a retry loop from costing money and
 * gives us a graceful "come back tomorrow" instead of a hard API error.
 */
@Singleton
class InferenceQuotaGuard @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val featureToggle: RemoteFeatureToggle,
    private val clock: Clock,
) {
    suspend fun hasBudget(): Boolean {
        val today = clock.now().toLocalDateTime(TimeZone.UTC).date.toString()
        val prefs = dataStore.data.first()
        if (prefs[KEY_DAY] != today) return true
        return (prefs[KEY_CALLS] ?: 0) < featureToggle.maxDailyInferenceCalls &&
            (prefs[KEY_TOKENS] ?: 0) < featureToggle.maxDailyInferenceTokens
    }

    suspend fun record(tokens: Int) {
        val today = clock.now().toLocalDateTime(TimeZone.UTC).date.toString()
        dataStore.edit { prefs ->
            if (prefs[KEY_DAY] != today) {
                prefs[KEY_DAY] = today; prefs[KEY_CALLS] = 0; prefs[KEY_TOKENS] = 0
            }
            prefs[KEY_CALLS] = (prefs[KEY_CALLS] ?: 0) + 1
            prefs[KEY_TOKENS] = (prefs[KEY_TOKENS] ?: 0) + tokens
        }
    }

    private companion object {
        val KEY_DAY = stringPreferencesKey("inference_quota_day")
        val KEY_CALLS = intPreferencesKey("inference_quota_calls")
        val KEY_TOKENS = intPreferencesKey("inference_quota_tokens")
    }
}
```

**First, `RemoteFeatureToggle` needs typed accessors — it has none.** Found during review: the class exposes only `sync()`, `syncNowIfEmpty()` and `getString(key)`. Every `featureToggle.cloudInferenceEnabled` / `maxDailyInferenceCalls` read in §6.5 and §6.12 above is against API that **does not exist yet**. The entire AI safety story depends on adding it, so add it first:

```kotlin
// datasource/remote/.../utils/RemoteFeatureToggle.kt

class RemoteFeatureToggle(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    // … existing sync() / syncNowIfEmpty() / getString() unchanged …

    fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)

    fun getLong(key: String): Long = remoteConfig.getLong(key)

    /**
     * Percentage rollout, evaluated against a stable per-install hash so a device
     * stays on the same side of the split across launches. Without the stable hash a
     * user flickers in and out of a feature between sessions, which is worse than
     * either state.
     */
    fun isInRollout(key: String, installId: String): Boolean {
        val percentage = getLong(key).coerceIn(0, 100)
        if (percentage <= 0) return false
        if (percentage >= 100) return true
        return (installId.hashCode().toLong().absoluteValue % 100) < percentage
    }

    // --- Typed AI feature accessors. One property per Remote Config key, so a
    // --- typo is a compile error rather than a silently-false flag.
    val aiEnabled: Boolean get() = getBoolean(KEY_AI_ENABLED)
    val cloudInferenceEnabled: Boolean get() = aiEnabled && getBoolean(KEY_AI_CLOUD_ENABLED)
    val onDeviceInferenceEnabled: Boolean get() = aiEnabled && getBoolean(KEY_AI_ON_DEVICE_ENABLED)
    val cloudModelName: String get() = getString(KEY_AI_CLOUD_MODEL)
    val maxDailyInferenceCalls: Long get() = getLong(KEY_AI_MAX_DAILY_CALLS)
    val maxDailyInferenceTokens: Long get() = getLong(KEY_AI_MAX_DAILY_TOKENS)

    private companion object {
        const val KEY_AI_ENABLED = "ai_enabled"
        const val KEY_AI_CLOUD_ENABLED = "ai_cloud_inference_enabled"
        const val KEY_AI_ON_DEVICE_ENABLED = "ai_on_device_enabled"
        const val KEY_AI_CLOUD_MODEL = "ai_cloud_model_name"
        const val KEY_AI_MAX_DAILY_CALLS = "ai_max_daily_calls"
        const val KEY_AI_MAX_DAILY_TOKENS = "ai_max_daily_tokens"
    }
}
```

Note `cloudInferenceEnabled` and `onDeviceInferenceEnabled` both `&&` with `aiEnabled`, so `ai_enabled = false` is a true master switch — you cannot leave a sub-flag on by accident.

`RemoteFeatureToggleTest` already exists in `datasource/remote/src/test`; extend it to cover the master-switch behaviour and the rollout hash stability.

**Remote Config keys** — add to `remote_config_defaults.xml` so every AI feature has an independent off switch and a rollout dial:

```xml
<entry><key>ai_enabled</key><value>false</value></entry>
<entry><key>ai_cloud_inference_enabled</key><value>false</value></entry>
<entry><key>ai_on_device_enabled</key><value>true</value></entry>
<entry><key>ai_cloud_model_name</key><value>gemini-2.5-flash</value></entry>
<entry><key>ai_agenda_summary_enabled</key><value>false</value></entry>
<entry><key>ai_assistant_enabled</key><value>false</value></entry>
<entry><key>ai_photo_checkin_enabled</key><value>false</value></entry>
<entry><key>ai_recap_enabled</key><value>false</value></entry>
<entry><key>ai_max_daily_calls</key><value>30</value></entry>
<entry><key>ai_max_daily_tokens</key><value>60000</value></entry>
<entry><key>ai_rollout_percentage</key><value>0</value></entry>
```

Every default is **off**. Ship the code dark, enable for the team, then 5%, then 50%, then all. If quota spikes during the conference, one console toggle stops it — no release required. This is the whole reason the existing `RemoteFeatureToggle` is valuable.

**Privacy rules, non-negotiable:**

| Data | Leaves device? |
| --- | --- |
| Session/speaker data (already public) | Yes |
| Which sessions the user starred | Yes, as titles only — never with an identifier |
| User's name, email, Google ID | **Never** |
| Session notes | Only with explicit per-use consent; `PrivacyFirst` routing by default |
| Check-in photos | Sent to the model for verification, never stored server-side |
| Location | Not collected |

Add a plain-language AI disclosure screen — reachable from every AI surface, and from Settings — stating what runs where, what's sent, and how to turn it off. Then mirror it in the Play Data Safety form (§13.4). If the app declares AI features, the Data Safety declaration must match what the code does.

**Safety and feedback.** Every AI output gets a thumbs-up/down and a "report" path:

```kotlin
@Composable
fun AiFeedbackRow(onFeedback: (helpful: Boolean) -> Unit, onReport: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.ai_generated_disclaimer), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onFeedback(true) }) { Icon(ChaiIcons.ThumbUp, stringResource(R.string.ai_helpful)) }
        IconButton(onClick = { onFeedback(false) }) { Icon(ChaiIcons.ThumbDown, stringResource(R.string.ai_not_helpful)) }
        TextButton(onClick = onReport) { Text(stringResource(R.string.ai_report)) }
    }
}
```

Log the thumbs-down rate per feature to analytics. **If a feature's thumbs-down rate exceeds 25%, turn it off.** Write that threshold down now, before anyone is emotionally invested in a feature.

**Definition of done for Phase 3:**
- [ ] `:core:ai` module with all three engines and a tested router
- [ ] Router unit-tested with fake engines: fallthrough, capability filtering, terminal errors
- [ ] Every AI feature has a working non-AI path, verified by a test with all engines unavailable
- [ ] App Check enforced in the Firebase console, verified by an unsigned build being rejected
- [ ] Every feature independently flagged, defaulting off
- [ ] Quota guard tested at the boundary
- [ ] AI disclosure screen written and reviewed by a non-engineer
- [ ] Play Data Safety form updated
- [ ] Cost projection recorded here: `expected DAU × calls/user × tokens/call × price/token`

---

## 7. Phase 4 — Ticketing and QR

**Depends on: Phase 0 · Requires backend coordination — start that conversation before the code**

### 7.1 What this replaces

Today attendees screenshot a confirmation email, then hunt for it at the door with 300 people behind them. Registration desk throughput is the single worst experience at most conferences. The app can fix it — but only if it works with no network, because the venue wifi will not survive 500 simultaneous check-ins.

### 7.2 Design: offline-verifiable tickets

The critical constraint: **the scanner must verify a ticket without a network call.** So the ticket must carry its own proof.

```
Ticket payload (compact, signed, offline-verifiable)

  v1.<base64url(payload)>.<base64url(signature)>

  payload = {
    "t": "<ticket id>",         // opaque, server-issued
    "e": "<event slug>",
    "n": "<attendee name>",
    "y": "<ticket type>",       // "regular" | "student" | "speaker" | "sponsor" | "organiser"
    "x": <expiry epoch seconds>
  }

  signature = Ed25519(payload, conference private key)
```

The **public** key ships in the app (and in Remote Config, so it can be rotated). The private key never leaves the backend. A scanner verifies the signature locally, offline, in microseconds — and cannot forge a ticket even with the app's full source, which matters because the source is public.

```kotlin
// core/model/src/main/kotlin/ke/droidcon/kotlin/core/model/Ticket.kt

data class Ticket(
    val ticketId: String,
    val eventSlug: String,
    val attendeeName: String,
    val type: TicketType,
    val expiresAt: Instant,
    /** The full signed string, exactly as it goes into the QR code. */
    val signedPayload: String,
) {
    fun isExpired(now: Instant): Boolean = now > expiresAt
}

enum class TicketType(val displayNameRes: Int, val badgeColor: Long) {
    Regular(R.string.ticket_type_regular, 0xFF1D6FB8),
    Student(R.string.ticket_type_student, 0xFF00A5A5),
    Speaker(R.string.ticket_type_speaker, 0xFFE8544E),
    Sponsor(R.string.ticket_type_sponsor, 0xFFF5A623),
    Organiser(R.string.ticket_type_organiser, 0xFF2E2E2E),
}
```

```kotlin
// feature/ticket/src/main/kotlin/.../TicketVerifier.kt

/**
 * Verifies a scanned ticket entirely offline.
 *
 * The signing key is asymmetric on purpose: this app's source is public, so a
 * shared secret would let anyone mint tickets. With Ed25519, a compromised client
 * can verify but never forge.
 */
@Singleton
class TicketVerifier @Inject constructor(
    private val keyProvider: TicketPublicKeyProvider,
    private val scanLog: ScanLogRepository,
    private val clock: Clock,
) {
    suspend fun verify(scanned: String): VerificationResult {
        val parts = scanned.split('.')
        if (parts.size != 3 || parts[0] != FORMAT_VERSION) {
            return VerificationResult.Invalid(Reason.MalformedPayload)
        }

        val (_, payloadB64, signatureB64) = parts
        val payloadBytes = runCatching { Base64.UrlSafe.decode(payloadB64) }
            .getOrElse { return VerificationResult.Invalid(Reason.MalformedPayload) }
        val signature = runCatching { Base64.UrlSafe.decode(signatureB64) }
            .getOrElse { return VerificationResult.Invalid(Reason.MalformedPayload) }

        // 1. Signature — is this ticket genuine?
        if (!keyProvider.verify(payloadBytes, signature)) {
            return VerificationResult.Invalid(Reason.BadSignature)
        }

        val ticket = Json.decodeFromString<TicketPayload>(payloadBytes.decodeToString())

        // 2. Event — is it for *this* conference?
        if (ticket.eventSlug != keyProvider.currentEventSlug) {
            return VerificationResult.Invalid(Reason.WrongEvent)
        }

        // 3. Expiry
        if (clock.now() > Instant.fromEpochSeconds(ticket.expiresAt)) {
            return VerificationResult.Invalid(Reason.Expired)
        }

        // 4. Replay — has this ticket already been scanned on this device?
        //    Devices reconcile their scan logs when a network is available, so a
        //    duplicate across two gates is caught after sync, not at the gate.
        //    That is the right trade: never block a legitimate attendee offline.
        val previousScan = scanLog.findScan(ticket.ticketId)
        if (previousScan != null) {
            return VerificationResult.AlreadyScanned(ticket.toTicket(scanned), previousScan.scannedAt)
        }

        scanLog.record(ticket.ticketId, clock.now())
        return VerificationResult.Valid(ticket.toTicket(scanned))
    }

    sealed interface VerificationResult {
        data class Valid(val ticket: Ticket) : VerificationResult
        data class AlreadyScanned(val ticket: Ticket, val previouslyAt: Instant) : VerificationResult
        data class Invalid(val reason: Reason) : VerificationResult
    }

    enum class Reason { MalformedPayload, BadSignature, WrongEvent, Expired }

    private companion object { const val FORMAT_VERSION = "v1" }
}
```

> **Backend dependency.** This design needs `api.droidcon.co.ke` to issue signed tickets. If that isn't available for this cycle, ship a **degraded v0**: the QR carries only the ticket id, the scanner verifies against a downloaded attendee list (refreshed hourly, cached), and un-listed ids are flagged rather than rejected. Note it as v0 in the code so nobody mistakes it for the real thing.

### 7.3 Displaying the ticket

```kotlin
// feature/ticket/src/main/kotlin/.../QrCodeImage.kt

/**
 * Renders [content] as a QR code. Generation is off the main thread and cached
 * by (content, size) — regenerating a QR on every recomposition drops frames.
 */
@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    foreground: Color = Color.Black,
    background: Color = Color.White,
) {
    val density = LocalDensity.current
    val sizePx = with(density) { QrSize.roundToPx() }

    val bitmap by produceState<ImageBitmap?>(initialValue = null, content, sizePx) {
        value = withContext(Dispatchers.Default) {
            generateQrBitmap(content, sizePx, foreground.toArgb(), background.toArgb())
        }
    }

    Box(modifier.size(QrSize), contentAlignment = Alignment.Center) {
        when (val bmp = bitmap) {
            null -> LoadingIndicator()
            else -> Image(
                bitmap = bmp,
                // Announce what it is, not what it looks like.
                contentDescription = stringResource(R.string.ticket_qr_content_description),
                modifier = Modifier.fillMaxSize(),
                filterQuality = FilterQuality.None,   // crisp module edges, no blur
            )
        }
    }
}

private fun generateQrBitmap(content: String, size: Int, fg: Int, bg: Int): ImageBitmap {
    val matrix = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size,
        mapOf(
            EncodeHintType.MARGIN to 1,
            // High correction: the code will be scanned off a scratched, glare-lit
            // screen in a dim room, sometimes with a cracked protector.
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        ),
    )

    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        val offset = y * size
        for (x in 0 until size) {
            pixels[offset + x] = if (matrix[x, y]) fg else bg
        }
    }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
}

private val QrSize = 280.dp
```

The ticket screen has to work at a gate, which means solving physical problems:

```kotlin
@Composable
fun TicketScreen(viewModel: TicketViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    // A dim screen is the #1 reason QR scans fail. Force max brightness while
    // the ticket is visible, and restore it on the way out.
    DisposableEffect(activity) {
        val window = activity?.window
        val previous = window?.attributes?.screenBrightness
        window?.attributes = window.attributes?.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        // Also keep the screen awake — queues are slow.
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window?.attributes = window.attributes?.apply {
                screenBrightness = previous ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    when (val s = state) {
        TicketUiState.NoTicket -> AddTicketPrompt(onAddTicket = viewModel::startTicketEntry)
        is TicketUiState.HasTicket -> TicketCard(
            ticket = s.ticket,
            // A light QR on white scans best regardless of app theme — do not
            // theme the code itself.
            onAddToCalendar = viewModel::addEventToCalendar,
            onShowBadge = viewModel::showBadge,
        )
    }
}
```

Also: **the ticket must be reachable in one tap from cold start.** Add it as a top-level destination *and* an app shortcut:

```xml
<!-- app/src/main/res/xml/shortcuts.xml -->
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <shortcut
        android:shortcutId="ticket"
        android:enabled="true"
        android:icon="@drawable/ic_shortcut_ticket"
        android:shortcutShortLabel="@string/shortcut_ticket_short"
        android:shortcutLongLabel="@string/shortcut_ticket_long">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="ke.droidcon.kotlin"
            android:targetClass="com.android254.presentation.activity.MainActivity"
            android:data="droidconke://ticket" />
        <categories android:name="android.shortcut.conversation" />
    </shortcut>
    <shortcut android:shortcutId="agenda" ... android:data="droidconke://sessions?filter=starred" />
    <shortcut android:shortcutId="now" ... android:data="droidconke://now" />
</shortcuts>
```

### 7.4 Scanning

Two paths, and the choice matters:

**Path A — Google code scanner (recommended for attendee-to-attendee badge scanning).** No camera permission needed, Google-provided UI, and the scanning module is delivered by Play services rather than bundled:

```kotlin
@Singleton
class CodeScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scanner = GmsBarcodeScanning.getClient(
        context,
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build(),
    )

    /** No CAMERA permission required — the scanning UI is hosted by Play services. */
    suspend fun scan(): Result<String> = suspendCancellableCoroutine { cont ->
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                barcode.rawValue
                    ?.let { cont.resume(Result.success(it)) }
                    ?: cont.resume(Result.failure(IllegalStateException("Empty barcode")))
            }
            .addOnCanceledListener { cont.cancel() }
            .addOnFailureListener { cont.resume(Result.failure(it)) }
    }
}
```

**Path B — custom CameraX scanner (for the registration desk).** Organisers scan hundreds of tickets in a row; they need continuous scanning with no per-scan UI dismissal, plus a running count and a torch:

```kotlin
// feature/ticket/src/main/kotlin/.../ContinuousScannerScreen.kt

@Composable
fun ContinuousScannerScreen(viewModel: ScannerViewModel = hiltViewModel()) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!cameraPermission.status.isGranted) {
        CameraPermissionRationale(onRequest = cameraPermission::launchPermissionRequest)
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    Box(Modifier.fillMaxSize()) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(surfaceRequest = request, modifier = Modifier.fillMaxSize())
        }

        ScannerOverlay(
            scanCount = state.scanCount,
            lastResult = state.lastResult,
            isTorchOn = state.isTorchOn,
            onToggleTorch = viewModel::toggleTorch,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
```

```kotlin
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val verifier: TicketVerifier,
    private val haptics: HapticsPlayer,
) : ViewModel() {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
    )

    /** De-dupes the same code across consecutive frames — one scan, one result. */
    private var lastScanned: String? = null
    private var lastScannedAt: Instant = Instant.DISTANT_PAST

    private val preview = Preview.Builder().build().apply {
        setSurfaceProvider { _surfaceRequest.value = it }
    }

    private val analysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .build(),
        )
        .build()
        .apply { setAnalyzer(Dispatchers.Default.asExecutor(), ::analyze) }

    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val provider = ProcessCameraProvider.awaitInstance(appContext)
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis,
        )
        try { awaitCancellation() } finally { provider.unbindAll() }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return imageProxy.close()
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstNotNullOfOrNull { it.rawValue }?.let(::onCodeScanned)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun onCodeScanned(raw: String) {
        val now = Clock.System.now()
        if (raw == lastScanned && now - lastScannedAt < DEDUPE_WINDOW) return
        lastScanned = raw
        lastScannedAt = now

        viewModelScope.launch {
            val result = verifier.verify(raw)
            // Haptics + distinct tones: at a busy gate, staff scan by feel, not by
            // reading the screen.
            when (result) {
                is VerificationResult.Valid -> haptics.success()
                is VerificationResult.AlreadyScanned -> haptics.warning()
                is VerificationResult.Invalid -> haptics.error()
            }
            _uiState.update { it.copy(lastResult = result, scanCount = it.scanCount + 1) }
        }
    }

    private companion object { val DEDUPE_WINDOW = 2.seconds }
}
```

### 7.5 Attendee-to-attendee: the digital badge

Scanning another person's QR to exchange details is a networking feature, not a ticketing one, and the privacy model must be different: **a badge is not a ticket.** Two separate QR payloads:

```kotlin
sealed interface QrPayload {
    /** Gate entry. Signed. Contains the ticket id. Only organisers scan these. */
    data class TicketQr(val signed: String) : QrPayload

    /**
     * A networking badge. Unsigned, revocable, and contains only what the user
     * explicitly chose to share. Rotating the token invalidates every previously
     * shared badge — which is the whole point.
     */
    data class BadgeQr(val token: String) : QrPayload
}
```

The badge QR encodes a short opaque token; scanning it fetches the shareable profile from the backend (or, offline, exchanges a compact vCard directly in the payload). Rules:

- The user picks what's on their badge: name always, then any of company, role, socials, email.
- **Email is off by default.** Nobody should hand out their email by accident.
- A "rotate my badge" button invalidates every prior share, in one tap.
- Scanning shows a confirmation sheet before saving — no silent contact capture.

See §11.2 for the connections surface this feeds.

**Definition of done:**
- [ ] Ticket displays offline, from cold start, in under one second
- [ ] Screen brightness and keep-awake handled, and correctly restored
- [ ] Signature verification unit-tested with valid, tampered, wrong-event, and expired fixtures
- [ ] Continuous scanner sustains ≥ 20 scans/minute on a mid-range device
- [ ] Scanner works fully offline; scan log reconciles on reconnect
- [ ] Badge QR is separate from ticket QR, with rotation
- [ ] Tested at a real gate with real staff before conference day — **not** on conference day

---

## 8. Phase 5 — Notifications

**Depends on: Phase 0 (§3.3 B10 for localizable strings)**

### 8.1 Current state

`MessagingService` receives FCM; `DroidconNotificationManager` posts. There are no channels, no local reminders, no user preferences, and the permission prompt fires on first launch with a rationale that is written to Timber instead of shown to the user.

The result: users deny the permission on launch (because they have no idea what they'd be getting) and then never get session reminders — which is the one notification a conference app genuinely needs.

### 8.2 Ask at the right moment

Never on first launch. Ask when the user does something that *implies* they want a reminder:

```kotlin
// The first time a user stars a session, that is the moment to ask.
@Composable
fun BookmarkButton(/* … */) {
    val notificationPermission = rememberNotificationPermissionState()
    var showRationale by remember { mutableStateOf(false) }

    FilledIconToggleButton(
        checked = isBookmarked,
        onCheckedChange = { checked ->
            onToggle()
            if (checked && notificationPermission.shouldAsk) showRationale = true
        },
    ) { /* … */ }

    if (showRationale) {
        NotificationRationaleDialog(
            // "Get a nudge 10 minutes before the sessions you've starred."
            // Concrete benefit, tied to the action they just took.
            onAllow = { showRationale = false; notificationPermission.request() },
            onDismiss = { showRationale = false },
        )
    }
}
```

Delete the unconditional `askNotificationPermission()` from `MainActivity`.

### 8.3 Channels, so users can tune rather than mute

One channel means one choice: all or nothing. Users choose nothing.

```kotlin
// presentation/src/main/java/com/android254/presentation/notifications/NotificationChannels.kt

enum class NotificationChannelSpec(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val importance: Int,
) {
    /** "Your talk starts in 10 minutes." The one people actually want. */
    SessionReminders(
        "session_reminders", R.string.channel_session_reminders,
        R.string.channel_session_reminders_desc, NotificationManager.IMPORTANCE_HIGH,
    ),

    /** Room changes, delays, cancellations. Must be high importance. */
    ScheduleChanges(
        "schedule_changes", R.string.channel_schedule_changes,
        R.string.channel_schedule_changes_desc, NotificationManager.IMPORTANCE_HIGH,
    ),

    /** "Lunch is served", "keynote starting in the main hall". */
    Announcements(
        "announcements", R.string.channel_announcements,
        R.string.channel_announcements_desc, NotificationManager.IMPORTANCE_DEFAULT,
    ),

    /** Feed posts, new speaker announcements. Low — this is browsable content. */
    SocialFeed(
        "social_feed", R.string.channel_social_feed,
        R.string.channel_social_feed_desc, NotificationManager.IMPORTANCE_LOW,
    ),

    /** Post-conference recap, next year's CFP. */
    Milestones(
        "milestones", R.string.channel_milestones,
        R.string.channel_milestones_desc, NotificationManager.IMPORTANCE_DEFAULT,
    ),

    /** Sync progress. MIN so it never interrupts. */
    Sync(
        "sync", R.string.channel_sync,
        R.string.channel_sync_desc, NotificationManager.IMPORTANCE_MIN,
    ),
    ;

    companion object {
        fun createAll(context: Context) {
            val manager = context.getSystemService<NotificationManager>() ?: return
            val group = NotificationChannelGroup(GROUP_CONFERENCE, context.getString(R.string.channel_group_conference))
            manager.createNotificationChannelGroup(group)

            manager.createNotificationChannels(
                entries.map { spec ->
                    NotificationChannel(spec.id, context.getString(spec.nameRes), spec.importance).apply {
                        description = context.getString(spec.descriptionRes)
                        this.group = GROUP_CONFERENCE
                    }
                },
            )
        }

        const val GROUP_CONFERENCE = "conference"
    }
}
```

Note `WorkConstants.NOTIFICATION_CHANNEL` currently used by `SyncDataWorker` — point it at `Sync.id` and fix the placeholder icon (`androidx.core.R.drawable.notification_bg_low` is not an icon; it's a nine-patch background, and it renders as a grey blob).

### 8.4 Local session reminders

The core feature, and it must work offline — a push-based reminder is useless when the venue wifi dies.

```kotlin
// data/src/main/java/com/android254/data/notifications/SessionReminderScheduler.kt

/**
 * Schedules a local notification before each starred session.
 *
 * WorkManager, not AlarmManager: reminders are not exact-alarm-worthy (a 10-minute
 * warning arriving at 9 or 11 minutes is fine), and `SCHEDULE_EXACT_ALARM` is a
 * restricted permission we should not be asking a conference app to hold.
 */
@Singleton
class SessionReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val preferences: NotificationPreferences,
    private val clock: Clock,
) {
    suspend fun rescheduleAll(starredSessions: List<Session>) {
        workManager.cancelAllWorkByTag(TAG_SESSION_REMINDER)
        if (!preferences.sessionRemindersEnabled()) return

        val lead = preferences.reminderLeadTime()      // user-configurable: 5/10/15/30 min
        val now = clock.now()

        starredSessions
            .mapNotNull { session ->
                val fireAt = session.startInstant - lead
                if (fireAt <= now) return@mapNotNull null   // already started
                session to (fireAt - now)
            }
            .forEach { (session, delay) ->
                workManager.enqueueUniqueWork(
                    "$WORK_PREFIX${session.remoteId}",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<SessionReminderWorker>()
                        .setInitialDelay(delay.toJavaDuration())
                        .addTag(TAG_SESSION_REMINDER)
                        .setInputData(
                            workDataOf(
                                SessionReminderWorker.KEY_SESSION_ID to session.remoteId,
                                SessionReminderWorker.KEY_TITLE to session.title,
                                SessionReminderWorker.KEY_ROOM to session.rooms,
                                SessionReminderWorker.KEY_SPEAKERS to session.speakers.joinToString { it.name },
                            ),
                        )
                        .build(),
                )
            }
    }

    private companion object {
        const val TAG_SESSION_REMINDER = "session_reminder"
        const val WORK_PREFIX = "session_reminder_"
    }
}
```

```kotlin
@HiltWorker
class SessionReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(KEY_SESSION_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val room = inputData.getString(KEY_ROOM).orEmpty()
        val speakers = inputData.getString(KEY_SPEAKERS).orEmpty()

        if (!applicationContext.canPostNotifications()) return Result.success()

        val deepLink = "droidconke://session/$sessionId".toUri()
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            sessionId.hashCode(),
            Intent(Intent.ACTION_VIEW, deepLink).setPackage(applicationContext.packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannelSpec.SessionReminders.id,
        )
            .setSmallIcon(R.drawable.ic_notification_droidcon)
            .setContentTitle(applicationContext.getString(R.string.reminder_title, title))
            .setContentText(applicationContext.getString(R.string.reminder_body, room, speakers))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                applicationContext.getString(R.string.reminder_big_text, title, room, speakers),
            ))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .addAction(
                R.drawable.ic_directions,
                applicationContext.getString(R.string.reminder_action_directions),
                directionsIntent(room),
            )
            .addAction(
                R.drawable.ic_notes,
                applicationContext.getString(R.string.reminder_action_notes),
                notesIntent(sessionId),
            )
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(sessionId.hashCode(), notification)

        return Result.success()
    }

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val KEY_TITLE = "title"
        const val KEY_ROOM = "room"
        const val KEY_SPEAKERS = "speakers"
    }
}
```

Hook the scheduler to bookmark changes so it's never stale:

```kotlin
// In the app-level coordinator, observing bookmarks
sessionsRepo.fetchSessions()
    .map { sessions -> sessions.filter { it.isBookmarked } }
    .distinctUntilChanged()
    .onEach { starred -> reminderScheduler.rescheduleAll(starred) }
    .launchIn(applicationScope)
```

And reschedule after a reboot and after each sync (a room change moves a session's start time):

```xml
<receiver android:name=".notifications.BootReceiver" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
    </intent-filter>
</receiver>
```

### 8.5 Push, with a payload contract

Untyped FCM data maps are how notification bugs happen. Define a contract:

```kotlin
@Serializable
sealed interface PushPayload {
    @Serializable @SerialName("schedule_change")
    data class ScheduleChange(
        val sessionId: String,
        val changeType: ChangeType,
        val message: String,
    ) : PushPayload {
        enum class ChangeType { RoomChanged, TimeChanged, Cancelled, SpeakerChanged }
    }

    @Serializable @SerialName("announcement")
    data class Announcement(val title: String, val body: String, val deepLink: String? = null) : PushPayload

    /** Silent — triggers a sync, posts nothing. */
    @Serializable @SerialName("sync")
    data object SyncRequest : PushPayload

    @Serializable @SerialName("feed")
    data class NewFeedPost(val postId: String, val preview: String) : PushPayload
}
```

```kotlin
class MessagingService : FirebaseMessagingService() {

    @Inject lateinit var handler: PushPayloadHandler

    override fun onMessageReceived(message: RemoteMessage) {
        val payload = runCatching {
            Json.decodeFromString<PushPayload>(message.data["payload"] ?: return)
        }.getOrElse {
            Timber.w(it, "Unrecognised push payload: ${message.data}")
            return
        }

        // A schedule change is the one push that must reach a user who has muted
        // everything else — it is why they are in the wrong room.
        handler.handle(payload)
    }

    override fun onNewToken(token: String) {
        // Topic subscriptions, so the backend can target by day/track/room.
        Firebase.messaging.subscribeToTopic(TOPIC_ALL_ATTENDEES)
    }
}
```

**Topic strategy:** subscribe everyone to `all_attendees`; subscribe to `session_<id>` on star, so schedule changes for *your* sessions are targeted rather than broadcast. This keeps the "cancelled talk" notification from going to 3,000 people who weren't going anyway.

### 8.6 A preferences screen

There isn't one. Users need it, and Play policy expects it:

```kotlin
@Composable
fun NotificationSettingsScreen(viewModel: NotificationSettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    Column {
        SwitchPreference(
            title = stringResource(R.string.pref_session_reminders),
            summary = stringResource(R.string.pref_session_reminders_summary),
            checked = prefs.sessionRemindersEnabled,
            onCheckedChange = viewModel::setSessionRemindersEnabled,
        )

        AnimatedVisibility(prefs.sessionRemindersEnabled) {
            SingleChoicePreference(
                title = stringResource(R.string.pref_reminder_lead_time),
                options = ReminderLeadTime.entries,
                selected = prefs.reminderLeadTime,
                onSelect = viewModel::setReminderLeadTime,
                optionLabel = { stringResource(it.labelRes) },
            )
        }

        SwitchPreference(/* announcements */)
        SwitchPreference(/* feed */)

        // Deep-link into system settings for per-channel control.
        ListItem(
            headlineContent = { Text(stringResource(R.string.pref_system_notification_settings)) },
            trailingContent = { Icon(ChaiIcons.OpenInNew, null) },
            modifier = Modifier.clickable { viewModel.openSystemNotificationSettings() },
        )
    }
}
```

**Definition of done:**
- [ ] Six channels created, in a group; sync channel is `IMPORTANCE_MIN` with a real icon
- [ ] Permission requested contextually on first star, never on launch
- [ ] Local reminders fire offline; verified in airplane mode
- [ ] Reminders survive reboot and app update
- [ ] Reminders reschedule when a sync changes a session time
- [ ] Deep links from every notification land on the right screen from a cold start
- [ ] Preferences screen ships
- [ ] Typed push payload with a `PushPayload` contract documented for the backend team

---

## 9. Phase 6 — Performance, R8, and app size

**Depends on: Phase 0 · §9.1 must land before §9.2–9.5 — everything else here is unverifiable without it**

### 9.1 Measure before optimising

Nothing here is currently measured. First step is a macrobenchmark module, so every later claim is a number.

```kotlin
// benchmark/build.gradle.kts
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "ke.droidcon.kotlin.benchmark"
    compileSdk = 37
    defaultConfig {
        minSdk = 28                 // macrobenchmark requires 28+
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    targetProjectPath = ":app"
    // Benchmarks must run against a release-shaped build.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.uiautomator)
    implementation(libs.junit4)
    implementation(libs.androidx.test.junit4)
}
```

```kotlin
// benchmark/src/main/kotlin/.../StartupBenchmark.kt

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test fun startupNoCompilation() = startup(CompilationMode.None())

    @Test fun startupBaselineProfile() = startup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
    )

    @Test fun startupFullCompilation() = startup(CompilationMode.Full())

    private fun startup(mode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.COLD,
        compilationMode = mode,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        // Wait for real content, not just the first frame — otherwise we are
        // measuring how fast we can draw a splash screen.
        device.wait(Until.hasObject(By.res("home_sessions_section")), 10_000)
    }

    private companion object { const val PACKAGE = "ke.droidcon.kotlin" }
}
```

```kotlin
// benchmark/src/main/kotlin/.../ScrollBenchmark.kt

@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test
    fun scrollSessionsList() = rule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(
            FrameTimingMetric(),
            // Which composables are recomposing, and how much they cost.
            TraceSectionMetric("SessionCard", TraceSectionMetric.Mode.Sum),
        ),
        iterations = 10,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(),
        setupBlock = {
            startActivityAndWait()
            device.findObject(By.res("nav_sessions")).click()
            device.wait(Until.hasObject(By.res("sessions_list")), 5_000)
        },
    ) {
        val list = device.findObject(By.res("sessions_list"))
        list.setGestureMargin(device.displayWidth / 5)
        repeat(4) {
            list.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }

    private companion object { const val PACKAGE = "ke.droidcon.kotlin" }
}
```

This requires `testTag`s that survive R8 — enable them in the benchmark build type:

```kotlin
// app/build.gradle.kts
buildTypes {
    create("benchmark") {
        initWith(getByName("release"))
        signingConfig = signingConfigs.getByName("debug")
        matchingFallbacks += listOf("release")
        // testTag → resource-id for UiAutomator
        buildConfigField("boolean", "USE_SEMANTIC_TEST_TAGS", "true")
        isProfileable = true
        isMinifyEnabled = true
        isShrinkResources = true
    }
}
```

```kotlin
// In the app's root composable — testTags become UiAutomator-visible only in
// benchmark builds, so production apps don't ship semantics overhead.
Modifier.semantics { if (BuildConfig.USE_SEMANTIC_TEST_TAGS) testTagsAsResourceId = true }
```

### 9.2 Baseline and startup profiles

The single highest-ROI performance change available — typically 20–30% faster cold start for the cost of a Gradle plugin.

```kotlin
// baselineprofile/src/main/kotlin/.../BaselineProfileGenerator.kt

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "ke.droidcon.kotlin",
        // Include the code paths users actually hit in the first 30 seconds.
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()

        // Home: wait for content, scroll it.
        device.wait(Until.hasObject(By.res("home_sessions_section")), 10_000)
        device.findObject(By.res("home_scroll"))?.fling(Direction.DOWN)

        // Sessions: the heaviest list in the app.
        device.findObject(By.res("nav_sessions")).click()
        device.wait(Until.hasObject(By.res("sessions_list")), 5_000)
        device.findObject(By.res("sessions_list")).fling(Direction.DOWN)

        // Session details: the most common navigation.
        device.findObject(By.res("sessions_list")).children.first().click()
        device.wait(Until.hasObject(By.res("session_details_title")), 5_000)
        device.pressBack()

        // Ticket: must be instant (§7.3).
        device.findObject(By.res("nav_ticket")).click()
        device.wait(Until.hasObject(By.res("ticket_qr")), 5_000)

        device.findObject(By.res("nav_feed")).click()
        device.wait(Until.hasObject(By.res("feed_list")), 5_000)
    }
}
```

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.baselineprofile)
}

dependencies {
    implementation(libs.androidx.profileinstaller)   // required at runtime
    baselineProfile(projects.baselineprofile)
}

baselineProfile {
    // Regenerate manually, commit the result. Generating in CI on every PR is slow
    // and flaky; a committed profile is reviewable and deterministic.
    automaticGenerationDuringBuild = false
    saveInSrc = true
    dexLayoutOptimization = true       // R8 uses the startup profile to order DEX
}
```

Commit `app/src/main/baseline-prof.txt` and `app/src/main/startup-prof.txt`, and **record the before/after numbers in the PR description.** Regenerate whenever navigation or startup changes materially.

### 9.3 R8 configuration

Current state: `isMinifyEnabled = true` on release, no resource shrinking, R8 full mode not explicitly declared, `proguardFiles` also applied to the debug build (harmless, but it means debug builds carry rules they never use).

```kotlin
// app/build.gradle.kts
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"          // install alongside release
        versionNameSuffix = "-debug"
        signingConfig = signingConfigs.getByName("debug")
        // No proguardFiles on debug — minification is off, so they do nothing.
    }

    release {
        isMinifyEnabled = true
        isShrinkResources = true                // NEW — was missing
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
        // Ship mapping + a readable configuration for debugging R8 output.
        ndk { debugSymbolLevel = "FULL" }
    }
}
```

```properties
# gradle.properties
android.enableR8.fullMode=true

# Fail the build when a keep rule references a class that no longer exists —
# catches stale keep rules instead of silently keeping nothing.
android.r8.strictFullModeForKeepRules=true
```

Keep rules to add, with reasons — an unexplained keep rule is technical debt:

```proguard
# app/proguard-rules.pro

# --- Crash reports we can actually read -------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization ---------------------------------------------------
# (existing rules are correct; keeping them)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Navigation 3 keys are serialized to SavedState by fully-qualified name.
# Renaming them breaks process-death restore in a way that only shows up on
# a real device with "don't keep activities" on.
-keep,includedescriptorclasses class com.android254.presentation.common.navigation.Screens { *; }
-keep,includedescriptorclasses class com.android254.presentation.common.navigation.Screens$* { *; }

# --- Room -------------------------------------------------------------------
# Entities are reflected over by generated code in some configurations.
-keep class ke.droidcon.kotlin.datasource.local.model.** { *; }

# --- Ktor -------------------------------------------------------------------
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer { *; }

# --- Firebase AI Logic / ML Kit (§6) ---------------------------------------
# Response models are deserialized reflectively.
-keep class com.google.firebase.ai.type.** { *; }
-keep class com.google.mlkit.genai.** { *; }

# --- Enum valueOf is used by DataStore and Remote Config mapping ------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Diagnostics (uncomment when investigating size) ------------------------
# -printusage build/outputs/mapping/release/usage.txt
# -printconfiguration build/outputs/mapping/release/full-config.txt
# -whyareyoukeeping class com.example.SomeClass
```

**Newer R8 capabilities worth evaluating** (verify against the AGP version you land on — these move):

- **Full mode is default** from AGP 8.0. It enables aggressive interface-method optimisation, enum unboxing, class merging, and argument propagation. Explicitly declaring it documents intent.
- **DEX layout optimisation** from the startup profile — enabled above via `dexLayoutOptimization = true`. Orders methods in the DEX by startup order, reducing page faults on cold start.
- **Partial shrinking** (`android.r8.partialShrinking`, experimental) — shrinks app code while skipping library code, trading size for build speed. Worth measuring on this codebase; the win is build time, not APK size.
- **`strictFullModeForKeepRules`** — makes stale keep rules an error. Turn it on; keep rules rot silently otherwise.
- **Resource shrinking with the new shrinker** — enabled by default in recent AGP. Verify with `resources.txt` in the mapping output that nothing needed was removed.

### 9.4 App size

Record a baseline *before* changing anything, so wins are provable:

```bash
./gradlew :app:bundleRelease
# Then, per-device-config download size:
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app.apks --mode=default
bundletool get-size total --apks=app.apks --dimensions=SDK,ABI,SCREEN_DENSITY
```

"Pixel-class" means this fixed device spec, so numbers stay comparable run to run:

```json
{ "supportedAbis": ["arm64-v8a"], "supportedLocales": ["en"],
  "screenDensity": 420, "sdkVersion": 34 }
```

```bash
bundletool get-size total --apks=app.apks --device-spec=pixel.json   # download
bundletool extract-apks --apks=app.apks --device-spec=pixel.json \
  --output-dir=out && du -cb out/*.apk                               # install
```

Write the number here as a **tracked baseline**:

| Date | Version | Download size (Pixel-class) | Install size | Notes |
| --- | --- | --- | --- | --- |
| 2026-08-13 | 1.0.0 (vc 1) | 6,420,105 B — 6.12 MiB | 10,596,026 B — 10.11 MiB | Pre-Phase-0 baseline |
| 2026-08-13 | 1.0.0 (vc 1) | 6,416,765 B — 6.12 MiB | 10,587,545 B — 10.10 MiB | After removing `accompanist-swiperefresh`, `gson`, `result-jvm`, `paging-*`, `runtime-livedata`, `compose-compiler`, and splitting the `compose` bundle |

**−3,340 B download: 0.05%.** Deleting unused dependencies does not shrink the APK — R8 was
already stripping them. The win was a smaller dependency graph and one fewer frozen
artifact pinned into the build, not bytes. The real size wins below are the ones R8 cannot
prove are unused: `material-icons-extended`, the Montserrat files, Lottie, and
`play-services-auth`. Measure each against the baseline rather than assuming.

Wins, ordered by return on effort:

**1. Drop `material-icons-extended`.** It's in the global `compose` bundle, so *every* Compose module pulls it. R8 does shrink unused icons, but the build-time cost is significant (thousands of generated classes to process) and the risk of an accidental full-keep is real.

The stronger argument, found during review: it's a **frozen artifact**. It resolves to `1.7.8` while `compose-ui` resolves to `1.9.5` — Google stopped publishing new versions. Depending on a frozen artifact for the app's entire icon set is a slow-moving liability regardless of bytes.

**One blocker to clear first:** `SessionPresentationModel` uses `Icons.Default.CoPresent` and `Icons.Default.MicExternalOn`, both extended-only. Vector those two into `ChaiIcons` before removing the dependency, or the build breaks. (`Icons.Default.Build` and `Icons.Default.Coffee` are in `material-icons-core` and are fine.)

Replace with `material-icons-core` plus a curated set:

```kotlin
// chai/src/main/java/com/droidconke/chai/icons/ChaiIcons.kt

/**
 * The app's icon set. Adding an icon here is a deliberate act, which keeps the
 * set small and visually consistent — and avoids depending on the 5,000-icon
 * material-icons-extended artifact for the dozen icons we actually use.
 */
object ChaiIcons {
    val StarFilled: ImageVector get() = Icons.Filled.Star
    val StarOutline: ImageVector get() = Icons.Outlined.StarOutline
    val Share: ImageVector get() = Icons.Filled.Share
    val Filter: ImageVector get() = filterVector          // local, hand-authored
    val Sparkle: ImageVector get() = sparkleVector
    val Cloud: ImageVector get() = cloudVector
    val Smartphone: ImageVector get() = smartphoneVector
    // …
}
```

**2. Fonts.** Five separate static Montserrat files are shipped. A single variable font, or downloadable fonts, replaces them:

```kotlin
// Downloadable fonts — zero bytes in the APK, cached by Play services.
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val Montserrat = FontFamily(
    Font(GoogleFont("Montserrat"), provider, FontWeight.Light),
    Font(GoogleFont("Montserrat"), provider, FontWeight.Normal),
    Font(GoogleFont("Montserrat"), provider, FontWeight.Medium),
    Font(GoogleFont("Montserrat"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Montserrat"), provider, FontWeight.Bold),
)
```

> **Trade-off, stated plainly:** downloadable fonts add a first-launch fetch and a fallback-font flash on devices without Play services. For a conference app whose users are on Play-enabled Android phones, the size win is worth it — but if the brand cares about a pixel-perfect first frame, bundle **one variable font file** instead. Either beats five static files.

**3. Remove unused dependencies.** ~~`gson`, `result-jvm`, `paging-*`, `accompanist-swiperefresh`, `runtime-livedata`~~ — done, and worth 0.05% (see the baseline table above). Still open: `lottie-compose`, `gms-play-services-auth`, `constraintlayout-compose` (3 usages — check whether each is necessary). These three are actually *used*, so unlike the ones above they will move the number. Measure each removal; Lottie and play-services-auth are the biggest.

**4. Per-app language + locale filtering.** Once §14's translations land:

```kotlin
android {
    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf("en", "sw", "fr", "pt")
    }
}
```

**5. Deduplicate drawables.** Twelve identical XML drawables exist in both `chai` and `presentation`. Single source in `chai`.

**6. Add a CI size gate,** so size regressions are caught in review rather than at release (§15).

### 9.5 Runtime performance

**Compose stability.** With Kotlin 2.x, strong skipping is on by default, but unstable parameters still break skipping. Run:

```bash
./gradlew :presentation:assembleRelease -PenableComposeCompilerReports=true
```

Then read `build/compose-reports/*-composables.txt` for `skippable=false` / `restartable=false`. Expected offenders in this codebase:

- `SessionsUiState` holds `List<SessionPresentationModel>` — `List` is an interface, so it's unstable. Fix with `kotlinx.collections.immutable`:

```kotlin
data class SessionsUiState(
    val sessions: ImmutableList<SessionPresentationModel> = persistentListOf(),
    val eventDays: ImmutableList<EventDate> = persistentListOf(),
    // …
)
```

Or, cheaper and nearly as effective, add the types to `compose_compiler_config.conf` (§3.1) — which is why that file is there.

- Lambdas captured in `LazyColumn` item content: hoist to stable references so items skip.

**`LazyColumn` keys.** Not a "confirm" — **8 of 10 call sites are missing them** (B14). Full list in §1.3. Fix every one:

```kotlin
LazyColumn {
    items(
        items = sessions,
        key = { it.id },                     // required for correct reuse + animation
        contentType = { it.sessionStatus },  // improves item reuse across types
    ) { session -> SessionCard(session) }
}
```

**Coil configuration.** Currently default. Sessions and speakers screens load many remote images:

```kotlin
// app/src/main/java/.../DroidconApplication.kt
class DroidconApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder().maxSizePercent(context, 0.25).build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            // Speaker avatars and session banners barely change; a long cache
            // means the app is fully populated offline after one sync.
            .crossfade(true)
            .build()
}
```

Consider upgrading Coil 2.7 → Coil 3 while here (multiplatform-ready, and the API surface is close enough that the migration is mostly imports).

**Firebase Performance custom traces** on the paths that matter:

```kotlin
suspend fun <T> traced(name: String, block: suspend () -> T): T {
    val trace = Firebase.performance.newTrace(name)
    trace.start()
    return try { block() } finally { trace.stop() }
}

// Usage
traced("sync_all_data") { syncAll() }
traced("agenda_ai_summary") { agendaSummarizer.summarize() }
traced("ticket_render") { generateQrBitmap(...) }
```

**Definition of done:**
- [ ] Macrobenchmark module with startup + scroll benchmarks
- [ ] Baseline and startup profiles generated, committed, and their effect measured
- [ ] `isShrinkResources = true`, R8 full mode explicit
- [ ] Every keep rule has a comment explaining why
- [ ] APK size baseline and post-optimisation numbers in the table above
- [ ] Compose stability report clean, or each remaining unstable type justified
- [ ] Cold start on a low-end device (2 GB RAM, API 26) under 2 seconds to first content

---

## 10. Phase 7 — Testing

**Depends on: Phase 0 — §3.3 B5 (`findActivity` throws) hard-blocks screenshot tests, §3.5 blocks meaningful goldens**

### 10.1 Where the gaps are

33 unit/Robolectric test files is respectable. But:

- **Zero E2E tests.** The two `androidTest` files are IDE scaffolding.
- **Zero screenshot tests.** For a design-system module, this is the most valuable test type available and it's absent.
- **Zero migration tests** (§3.3 B2 adds the first).
- **Zero accessibility assertions.**
- **Coverage is measured but not gated.** Jacoco + Codecov are configured; nothing fails a PR.

### 10.2 Screenshot testing with Roborazzi

Roborazzi over the official Compose Preview Screenshot Testing plugin, for one decisive reason: the project already uses Robolectric, so Roborazzi runs on the JVM in the existing `test` source set with no emulator, and it can capture *interaction* states — a pressed button, a bottom sheet mid-open, a scrolled list — which preview-based testing cannot.

```kotlin
// build-logic/convention/src/main/kotlin/AndroidLibraryRoborazziConventionPlugin.kt

class AndroidLibraryRoborazziConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.github.takahirom.roborazzi")

        extensions.configure<LibraryExtension> {
            testOptions.unitTests.isIncludeAndroidResources = true
        }

        dependencies {
            add("testImplementation", libs.findBundle("roborazzi").get())
            add("testImplementation", libs.findLibrary("test-robolectric").get())
            add("testImplementation", libs.findLibrary("compose-ui-test-junit").get())
        }
    }
}
```

```kotlin
// core/screenshot/src/main/kotlin/.../ChaiScreenshotTest.kt

/**
 * Base for Chai screenshot tests.
 *
 * Every component is captured across the matrix that actually breaks things:
 * light/dark, phone/tablet, and default/200% font scale. Those three axes catch
 * the overwhelming majority of real visual regressions.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
abstract class ChaiScreenshotTest {

    @get:Rule val composeRule = createComposeRule()

    protected fun captureMatrix(
        name: String,
        content: @Composable () -> Unit,
    ) {
        ScreenshotVariant.entries.forEach { variant ->
            composeRule.setContent {
                TestHarness(
                    darkMode = variant.isDark,
                    fontScale = variant.fontScale,
                    size = variant.size,
                ) {
                    ChaiTheme(darkTheme = variant.isDark) { content() }
                }
            }
            composeRule.onRoot().captureRoboImage(
                "src/test/screenshots/$name/${variant.id}.png",
                roborazziOptions = RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(
                        // 0.1% tolerance absorbs font-rendering noise across JDKs
                        // without hiding real layout changes.
                        changeThreshold = 0.001f,
                    ),
                ),
            )
        }
    }

    enum class ScreenshotVariant(
        val id: String,
        val isDark: Boolean,
        val fontScale: Float,
        val size: DpSize,
    ) {
        PhoneLight("phone_light", false, 1f, DpSize(412.dp, 915.dp)),
        PhoneDark("phone_dark", true, 1f, DpSize(412.dp, 915.dp)),
        PhoneLargeFont("phone_font_200", false, 2f, DpSize(412.dp, 915.dp)),
        TabletLight("tablet_light", false, 1f, DpSize(1280.dp, 800.dp)),
        TabletDark("tablet_dark", true, 1f, DpSize(1280.dp, 800.dp)),
    }
}
```

```kotlin
// chai/src/test/kotlin/.../SessionCardScreenshotTest.kt

class SessionCardScreenshotTest : ChaiScreenshotTest() {

    @Test fun default() = captureMatrix("session_card/default") {
        SessionCard(session = FakeSessions.regular, onClick = {})
    }

    @Test fun bookmarked() = captureMatrix("session_card/bookmarked") {
        SessionCard(session = FakeSessions.regular.copy(isStarred = true), onClick = {})
    }

    @Test fun live() = captureMatrix("session_card/live") {
        SessionCard(session = FakeSessions.regular.copy(sessionStatus = SessionStatus.Ongoing), onClick = {})
    }

    @Test fun selectedForTwoPane() = captureMatrix("session_card/selected") {
        SessionCard(session = FakeSessions.regular, isSelected = true, onClick = {})
    }

    /** Long titles and long speaker lists are where cards actually break. */
    @Test fun longContent() = captureMatrix("session_card/long_content") {
        SessionCard(session = FakeSessions.pathologicallyLong, onClick = {})
    }

    @Test fun missingImage() = captureMatrix("session_card/no_image") {
        // Empty string, not null — the field is a non-null String with a "" default.
        SessionCard(session = FakeSessions.regular.copy(sessionImage = ""), onClick = {})
    }
}
```

Note `FakeSessions.pathologicallyLong` — the fake data in `presentation/common/fakedata/FakeSessions.kt` currently only has happy-path values. Add deliberate edge cases: 200-character titles, eight speakers, missing images, empty descriptions, non-Latin characters. **Those are the cases that break layouts, so those are the ones worth screenshotting.**

Commands:

```bash
./gradlew recordRoborazziDebug     # regenerate goldens
./gradlew verifyRoborazziDebug     # verify (CI)
./gradlew compareRoborazziDebug    # produce diff images for review
```

### 10.3 End-to-end tests

Real instrumentation tests against real user journeys. Start with the five that matter most:

```kotlin
// app/src/androidTest/kotlin/.../journeys/AgendaJourneyTest.kt

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AgendaJourneyTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var sessionsRepo: SessionsRepo

    @Before fun setUp() = hiltRule.inject()

    /**
     * The core journey: find a session, star it, confirm it appears in My Sessions,
     * unstar it, confirm it's gone. If this breaks, the app has no purpose.
     */
    @Test
    fun starASession_appearsInMySessions_thenUnstar() = with(composeRule) {
        onNodeWithTag("nav_sessions").performClick()
        waitUntilExactlyOneExists(hasTestTag("sessions_list"), timeoutMillis = 5_000)

        val firstCard = onAllNodesWithTag("session_card")[0]
        val title = firstCard.fetchSemanticsNode().config[SemanticsProperties.Text].first().text

        firstCard.onChildWithTag("bookmark_button").performClick()

        onNodeWithTag("my_sessions_switch").performClick()
        waitForIdle()
        onNodeWithText(title, substring = true).assertIsDisplayed()

        // And back off again.
        onAllNodesWithTag("session_card")[0].onChildWithTag("bookmark_button").performClick()
        waitForIdle()
        onNodeWithTag("sessions_empty_state").assertIsDisplayed()
    }

    @Test
    fun starredSessionSurvivesProcessDeath() = with(composeRule) {
        onNodeWithTag("nav_sessions").performClick()
        waitUntilExactlyOneExists(hasTestTag("sessions_list"))
        onAllNodesWithTag("session_card")[0].onChildWithTag("bookmark_button").performClick()

        // Simulate process death and restore.
        activityRule.scenario.recreate()

        onNodeWithTag("my_sessions_switch").performClick()
        onAllNodesWithTag("session_card").assertCountEquals(1)
    }
}
```

```kotlin
// app/src/androidTest/kotlin/.../journeys/OfflineJourneyTest.kt

/**
 * The journey that matters most in practice: the venue wifi is gone and the
 * attendee still needs their agenda and their ticket.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OfflineJourneyTest {

    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @BindValue @JvmField
    val networkMonitor: ConnectivityMonitor = AlwaysOfflineConnectivityMonitor()

    @Test
    fun offline_showsCachedSessionsAndTicket() = with(composeRule) {
        onNodeWithTag("nav_sessions").performClick()
        waitUntilExactlyOneExists(hasTestTag("sessions_list"), timeoutMillis = 5_000)
        onAllNodesWithTag("session_card").assertCountIsAtLeast(1)

        onNodeWithTag("nav_ticket").performClick()
        onNodeWithTag("ticket_qr").assertIsDisplayed()

        // And the AI surface degrades rather than erroring.
        onNodeWithTag("nav_home").performClick()
        onNodeWithTag("agenda_summary_button").performClick()
        onNodeWithTag("agenda_stats").assertIsDisplayed()          // deterministic path
        onNodeWithTag("agenda_ai_narrative").assertDoesNotExist()  // AI path absent, not broken
    }
}
```

Journeys to cover:

| # | Journey | Why |
| --- | --- | --- |
| 1 | Star → My Sessions → unstar | The app's core purpose |
| 2 | Starred session survives process death | Users lose agendas to this class of bug |
| 3 | Offline: sessions + ticket + degraded AI | The actual conference network |
| 4 | Ticket displays and scans | Gate failure is the worst failure |
| 5 | Session details → notes → autosave → reopen | Data loss path |
| 6 | Filter by topic, level, room, and combinations | Where B1 lived |
| 7 | Deep link from notification, cold start | Notifications are useless if this breaks |
| 8 | Sign in → sign out → data intact | Auth regressions eat local data |
| 9 | Rotation on every screen | State-loss regressions |
| 10 | Two-pane on tablet: select, rotate, back | New surface, new bugs |

### 10.4 Fix the test infrastructure

**`:core:testing`** so fakes stop being duplicated. Today `FakeSyncWorkManager` lives in `presentation/src/test`, `SampleData` in `data/src/test`, `MockTokenProvider` and `SamplePaginationMetaData` in `datasource/remote/src/test`, and `FakeEntryProvider` in `presentation/src/test`. None are reusable.

```kotlin
// core/testing/src/main/kotlin/.../repository/FakeSessionsRepo.kt

/**
 * A hand-written fake, not a mock.
 *
 * Fakes over mockk for repositories: a fake with a real in-memory Flow catches
 * bugs that a stubbed `every { … } returns` cannot — ordering, emission counts,
 * and the state-after-write behaviour that MVI actually depends on.
 */
class FakeSessionsRepo : SessionsRepo {
    private val sessions = MutableStateFlow<List<Session>>(emptyList())

    fun seed(vararg session: Session) { sessions.value = session.toList() }

    override fun fetchSessions(): Flow<List<Session>> = sessions

    override suspend fun bookmarkSession(id: String) {
        sessions.update { list -> list.map { if (it.remoteId == id) it.copy(isBookmarked = true) else it } }
    }

    override suspend fun unBookmarkSession(id: String) {
        sessions.update { list -> list.map { if (it.remoteId == id) it.copy(isBookmarked = false) else it } }
    }

    // …
}
```

```kotlin
// core/testing/src/main/kotlin/.../MainDispatcherRule.kt
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

Also: **replace `Clock.System` with an injected `Clock` everywhere**, so time-dependent tests aren't flaky. `SessionsManager`, `SessionsViewModel`, and `MainViewModel` all read wall-clock time directly. `core/testing` provides:

```kotlin
class TestClock(private var current: Instant = Instant.parse("2026-11-06T09:00:00Z")) : Clock {
    override fun now(): Instant = current
    fun advanceBy(duration: Duration) { current += duration }
    fun setTo(instant: Instant) { current = instant }
}
```

This unlocks the tests nobody can write today: "does the live-session indicator appear exactly when the session starts", "does the Now/Next card roll over correctly at a session boundary", "does the reminder fire at the right offset".

### 10.5 Gate coverage

Jacoco exists; nothing enforces it. Set floors that ratchet up rather than a single unreachable number:

```kotlin
// build-logic/convention/src/main/kotlin/com/android254/Jacoco.kt
tasks.withType<JacocoCoverageVerification>().configureEach {
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                // Start at the current level, raise by 2% per quarter.
                minimum = "0.55".toBigDecimal()
            }
        }
        rule {
            // New code is held to a higher bar than the legacy average.
            element = "CLASS"
            includes = listOf("*.usecase.*", "*.viewmodel.*", "*ViewModel")
            limit { counter = "LINE"; value = "COVEREDRATIO"; minimum = "0.80".toBigDecimal() }
        }
    }
}
```

And exclude what shouldn't count — generated code, DI modules, previews, `@Composable` preview functions — otherwise the number is noise.

**Definition of done:**
- [ ] `:core:testing` module; every duplicated fake consolidated
- [ ] `Clock` injected everywhere; no direct `Clock.System` / `System.currentTimeMillis()` in production code
- [ ] Roborazzi covering every `chai` component and every screen, across 5 variants
- [ ] `verifyRoborazziDebug` in CI, with diff images posted to the PR
- [ ] 10 E2E journeys passing on an emulator in CI
- [ ] Room migration tests
- [ ] Accessibility checks enabled in Compose tests
- [ ] Coverage gate enforced, with an agreed ratchet schedule
- [ ] `ExampleUnitTest` / `ExampleInstrumentedTest` deleted

---

## 11. Phase 8 — New product surfaces

**Mostly product and editorial work rather than engineering work. Most items here also depend on backend capacity.**

### 11.0 Read this before building any of it

Each feature below is individually buildable. Collectively they would turn a conference app into a platform, and platforms need owners. Before writing code for any of them, three questions need answers **from the organising team, not from engineering**:

1. **Who maintains the content?** A job board with no jobs is worse than no job board. A code challenge with no challenges is a broken screen. Every feature here has an editorial cost that recurs annually.
2. **What happens between conferences?** The app is used intensely for three days and then not at all for 51 weeks. Features that only work during the conference are fine; features that need year-round moderation are not, unless someone signs up for that.
3. **Does it need a backend?** Most of these do. `api.droidcon.co.ke` is the constraint, not the Android app.

You asked for a dedicated planning meeting for this. **Hold it before Phase 3, not after** — because the answers change what `:core:ai` needs to support, and it's cheaper to know that up front.

My recommendation, stated plainly: **pick two.** The strongest candidates, in order:

1. **Ticketing + badge scanning (§7)** — solves a real, painful, universal problem, low editorial cost, high visible impact.
2. **Session notes + recap (§5.5, §6.11)** — pure retention value, zero editorial cost, no backend required for v1.
3. **Connections / networking (§11.2)** — high value, moderate backend cost, and it composes with ticketing.

The job board, code challenge, and calendar booking are each good ideas that need a committed owner. Without one they'll ship empty and become the thing people point at when they say the app is stale.

### 11.1 Digital job board

**The proposition:** droidcon Kenya is where Kenyan Android talent and Kenyan Android employers are in the same building. Right now that matchmaking happens by luck at the sponsor booths.

```kotlin
// core/model/src/main/kotlin/.../JobPosting.kt

data class JobPosting(
    val id: String,
    val title: String,
    val company: Company,
    val locationType: LocationType,
    val location: String?,
    val seniority: Seniority,
    val skills: List<String>,
    val description: String,
    val salaryRange: SalaryRange?,     // optional but strongly encouraged
    val applyUrl: String,
    val postedAt: Instant,
    val expiresAt: Instant,
    /** True when the posting company is a conference sponsor. */
    val isSponsorPosting: Boolean,
    /** Set when a sponsor has a physical booth attendees can visit. */
    val boothLocation: String?,
) {
    enum class LocationType { OnSite, Hybrid, Remote, RemoteWithinAfrica }
    enum class Seniority { Intern, Junior, Mid, Senior, Staff, Lead, Manager }
}

data class SalaryRange(
    val min: Int,
    val max: Int,
    val currency: String,
    val period: Period,
) { enum class Period { Month, Year } }
```

Design decisions that make this work rather than rot:

- **Postings expire.** Hard `expiresAt`, default 60 days, no exceptions. A stale job board is worse than none.
- **Salary ranges strongly encouraged, and surfaced.** Sort and filter by "has salary range". This is the single feature that would make it genuinely useful in the Kenyan market, and it's a policy choice, not an engineering one.
- **No in-app applications for v1.** Deep link out to the employer's process. Building an ATS is not the job.
- **Sponsors get placement, and it's labelled.** Sponsor postings appear first with a visible "Sponsor" badge. Honest and monetisable.
- **Booth cross-link.** A sponsor posting links to their booth location, which ties into the gamification passport (§6.9).
- **AI match, carefully.** With the user's consent and a locally-held skill profile, rank postings by fit. Route `PrivacyFirst` — a skills profile is sensitive. Never send it to the cloud.

```kotlin
// The AI angle worth building: not a recommender, a *summariser*.
// "12 of the 40 postings match your interests: Compose, KMP, and CI."
suspend fun summariseRelevantJobs(
    postings: List<JobPosting>,
    interests: List<String>,   // derived from starred sessions — no separate profile needed
): Result<JobDigest>
```

Deriving interests from **starred sessions** rather than asking the user to fill in a profile is the key insight: it's zero-friction, it's already local, and it's genuinely predictive.

**Backend requirement:** `GET /events/{slug}/jobs`, plus an admin surface for sponsors to post, plus a moderator. **Non-trivial, and the Android work is the small half.** Do not start the client until the endpoint and the moderation owner both exist.

### 11.2 Connections and networking

Builds directly on §7.5's badge QR. The single highest-value networking feature is not a chat system — it's **remembering who you met and why**.

```kotlin
data class Connection(
    val id: String,
    val name: String,
    val company: String?,
    val role: String?,
    val socials: List<SocialLink>,
    val email: String?,               // only if they chose to share it
    val metAt: Instant,
    /** The session or location where the scan happened — the memory hook. */
    val metContext: String?,
    /** User's own note. This is the feature. */
    val note: String,
    val avatarUrl: String?,
)
```

Why the note is the feature: three days later nobody remembers which of 14 scanned badges was "the person building the offline-first payments SDK." Prompt for a one-line note immediately after the scan, while the memory is fresh:

```kotlin
@Composable
fun ConnectionCapturedSheet(
    profile: SharedProfile,
    currentSession: Session?,
    onSave: (note: String) -> Unit,
) {
    var note by remember { mutableStateOf("") }

    Column(Modifier.padding(24.dp)) {
        ProfileHeader(profile)

        // Context is auto-filled from where they are right now — free metadata.
        currentSession?.let {
            Text(
                stringResource(R.string.connection_met_at, it.title),
                style = MaterialTheme.typography.labelMedium,
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.connection_note_label)) },
            placeholder = { Text(stringResource(R.string.connection_note_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Row {
            TextButton(onClick = { onSave("") }) { Text(stringResource(R.string.skip)) }
            Spacer(Modifier.weight(1f))
            Button(onClick = { onSave(note) }) { Text(stringResource(R.string.save)) }
        }
    }
}
```

Then export: system contacts, vCard, or a shareable list. And an AI-assisted follow-up digest post-conference — *"You met 9 people. Three mentioned hiring; two work on Compose Multiplatform."*

**Privacy is the whole design.** Everything opt-in, badge tokens rotatable, no directory of attendees, no "who's nearby", no location. A conference app that leaks attendee data once will never be trusted again.

### 11.3 Code challenge platform

**Honest assessment: this is the weakest item on the list for an app, and the strongest as a web property.**

A code editor on a phone is a bad experience. Attendees are in talks. The people who want to solve challenges want a keyboard.

**The version that works in the app:** the app is the *companion*, not the platform. Challenges are hosted on the web; the app does discovery, notifications, leaderboard, and submission verification via QR at a sponsor booth.

```kotlin
data class CodeChallenge(
    val id: String,
    val title: String,
    val difficulty: Difficulty,
    /** Ties the challenge to a talk — solving it reinforces what was taught. */
    val relatedSessionId: String?,
    val theme: String,
    val briefMarkdown: String,
    val webUrl: String,
    val points: Int,
    val opensAt: Instant,
    val closesAt: Instant,
) { enum class Difficulty { Warmup, Intermediate, Hard, Fiendish } }
```

The one genuinely app-native version worth considering: **micro-quizzes tied to a session, delivered 20 minutes after it ends.** Three multiple-choice questions on what was just taught. Under a minute, no keyboard, high completion, and it doubles as session feedback. That is a good app feature; a code editor is not.

If AI is in play, generating those quizzes from the session description and the speaker's slides — reviewed by a human before publishing — makes the editorial cost near-zero. **Never auto-publish model-generated quiz content.**

### 11.4 Calendar booking

Two distinct features, often conflated:

**(a) Add sessions to your calendar.** Trivial, high value, no backend. Ship it in Phase 0's slipstream:

```kotlin
fun Context.addSessionToCalendar(session: Session) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, session.title)
        putExtra(CalendarContract.Events.EVENT_LOCATION, "${session.rooms}, ${VENUE_NAME}")
        putExtra(
            CalendarContract.Events.DESCRIPTION,
            buildString {
                appendLine(session.description.take(500))
                appendLine()
                appendLine("Speakers: ${session.speakers.joinToString { it.name }}")
                appendLine("droidconke://session/${session.remoteId}")
            },
        )
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, session.startInstant.toEpochMilliseconds())
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, session.endInstant.toEpochMilliseconds())
        putExtra(CalendarContract.Events.EVENT_TIMEZONE, "Africa/Nairobi")
        putExtra(CalendarContract.Reminders.MINUTES, 10)
    }
    startActivity(intent)
}
```

Plus a **bulk export** of the whole starred agenda as an `.ics` file — one tap, entire conference in the user's calendar. This is the highest value-to-effort ratio feature in this entire document.

```kotlin
/**
 * Emits the starred agenda as an RFC 5545 calendar. Shared via FileProvider, so
 * it opens in whatever calendar app the user actually uses.
 */
fun buildIcs(sessions: List<Session>): String = buildString {
    appendLine("BEGIN:VCALENDAR")
    appendLine("VERSION:2.0")
    appendLine("PRODID:-//droidcon Kenya//Android App//EN")
    appendLine("X-WR-CALNAME:droidcon Kenya — My Agenda")
    appendLine("X-WR-TIMEZONE:Africa/Nairobi")

    sessions.forEach { session ->
        appendLine("BEGIN:VEVENT")
        appendLine("UID:${session.remoteId}@droidcon.co.ke")
        appendLine("DTSTAMP:${Clock.System.now().toIcsUtc()}")
        appendLine("DTSTART:${session.startInstant.toIcsUtc()}")
        appendLine("DTEND:${session.endInstant.toIcsUtc()}")
        appendLine("SUMMARY:${session.title.escapeIcs()}")
        appendLine("LOCATION:${session.rooms.escapeIcs()}")
        appendLine("DESCRIPTION:${session.description.take(500).escapeIcs()}")
        appendLine("BEGIN:VALARM")
        appendLine("TRIGGER:-PT10M")
        appendLine("ACTION:DISPLAY")
        appendLine("DESCRIPTION:${session.title.escapeIcs()} starts in 10 minutes")
        appendLine("END:VALARM")
        appendLine("END:VEVENT")
    }
    appendLine("END:VCALENDAR")
}
```

**(b) Book a slot with a person** — office hours with a speaker, a sponsor demo, a mentor session. Genuinely useful, and a substantial backend feature: availability windows, double-booking prevention, cancellation, no-show handling, and timezone correctness. Worth doing only if the organisers commit to running speaker office hours, because the feature is worthless without supply.

### 11.5 Session ratings and feedback

Currently there is a single global `FeedBackScreen`. Per-session feedback is far more valuable — to speakers, who currently get nothing, and to organisers choosing next year's programme.

```kotlin
data class SessionRating(
    val sessionId: String,
    val rating: Rating,
    val comment: String?,
    val submittedAt: Instant,
    val isAnonymous: Boolean = true,
) {
    /** Three options, not five stars. Higher completion, clearer signal. */
    enum class Rating { Poor, Good, Excellent }
}
```

Prompt at the right moment: a notification when the session ends, and an inline card on the session detail screen. Three taps, no typing required, comment optional. Then aggregate for speakers — *"38 ratings, 84% Excellent"* — which is more feedback than most conference speakers have ever received.

### 11.6 Venue map and wayfinding

The most-asked question at any conference is "where is Hall B?" — currently unanswerable in the app.

Full indoor positioning is overkill. What works:

- A static venue map (SVG, zoomable, pinch-to-zoom via `Modifier.graphicsLayer` + transformable).
- Rooms highlighted and tappable → filtered session list for that room.
- "Take me there" from a session card → highlights the room on the map with a route hint.
- Floor switcher if the venue is multi-level.

No backend, no beacons, no ML. All it needs is one SVG from the organisers. **The best value-to-effort ratio of anything in §11.**

### 11.7 Glance widget: what's on now

A conference app's ideal surface is the home screen: glance, know where to be, done.

```kotlin
// widget/src/main/kotlin/.../NextSessionWidget.kt

class NextSessionWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(SmallWidget, MediumWidget, LargeWidget),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication<WidgetEntryPoint>(context)
        val repo = entryPoint.sessionsRepo()

        provideContent {
            val now = Clock.System.now().toEpochMilliseconds()
            val current by repo.fetchCurrentSessions(now).collectAsState(emptyList())
            val next by repo.fetchUpNextSessions(now).collectAsState(emptyList())

            GlanceTheme {
                WidgetContent(
                    current = current.firstOrNull(),
                    next = next.firstOrNull(),
                    size = LocalSize.current,
                )
            }
        }
    }

    private companion object {
        val SmallWidget = DpSize(140.dp, 100.dp)    // next session title only
        val MediumWidget = DpSize(250.dp, 100.dp)   // + room and time
        val LargeWidget = DpSize(250.dp, 200.dp)    // + the next three
    }
}
```

```kotlin
@Composable
private fun WidgetContent(current: Session?, next: Session?, size: DpSize) {
    Column(
        GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        when {
            current != null -> {
                Text(
                    LocalContext.current.getString(R.string.widget_happening_now),
                    style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.error),
                )
                Text(current.title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold), maxLines = 2)
                Text(current.rooms, style = TextStyle(fontSize = 12.sp))
            }
            next != null -> {
                Text(
                    LocalContext.current.getString(R.string.widget_up_next),
                    style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.primary),
                )
                Text(next.title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold), maxLines = 2)
                Text("${next.startTime} · ${next.rooms}", style = TextStyle(fontSize = 12.sp))
            }
            else -> Text(LocalContext.current.getString(R.string.widget_no_sessions))
        }
    }
}
```

Update it from the sync worker and on a periodic 15-minute cadence during conference days only — a widget that polls year-round is a battery complaint:

```kotlin
NextSessionWidget().updateAll(context)
```

Consider also a **Wear OS tile** with the same content. Small effort, disproportionate delight, and a good talk.

### 11.8 Deep links and App Links

None exist. Session links shared in the conference WhatsApp group open a browser, not the app.

```xml
<activity android:name="…MainActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <!-- Custom scheme: internal navigation, notifications, shortcuts -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:scheme="droidconke" />
    </intent-filter>

    <!-- App Links: verified https links open the app directly, no chooser -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="droidcon.co.ke" android:pathPrefix="/sessions" />
        <data android:scheme="https" android:host="droidcon.co.ke" android:pathPrefix="/speakers" />
    </intent-filter>
</activity>
```

Requires `https://droidcon.co.ke/.well-known/assetlinks.json` with the release signing certificate fingerprint — a website change, so coordinate early.

Map URIs to `NavKey`s in one place:

```kotlin
// presentation/.../navigation/DeepLinkResolver.kt

object DeepLinkResolver {
    /**
     * Resolves an incoming URI to a navigation key.
     * Returns null for anything unrecognised — the app opens on Home rather than
     * crashing on a malformed link from a WhatsApp forward.
     */
    fun resolve(uri: Uri): Screens? = when {
        uri.scheme == "droidconke" -> resolveCustomScheme(uri)
        uri.host == "droidcon.co.ke" -> resolveWebLink(uri)
        else -> null
    }

    private fun resolveCustomScheme(uri: Uri): Screens? = when (uri.host) {
        "session" -> uri.lastPathSegment?.let { Screens.SessionDetails(it) }
        "speaker" -> uri.lastPathSegment?.let { Screens.SpeakerDetails(it) }
        "sessions" -> Screens.Sessions
        "ticket" -> Screens.Ticket
        "now" -> Screens.Home
        else -> null
    }

    private fun resolveWebLink(uri: Uri): Screens? {
        val segments = uri.pathSegments
        return when {
            segments.size >= 2 && segments[0] == "sessions" -> Screens.SessionDetails(segments[1])
            segments.size >= 2 && segments[0] == "speakers" -> Screens.SpeakerDetails(segments[1])
            segments.firstOrNull() == "sessions" -> Screens.Sessions
            else -> null
        }
    }
}
```

And make sharing produce those links. `FeedShareSection` exists for the feed; session and speaker cards need share actions using `https://droidcon.co.ke/sessions/{slug}`, so a shared link works for people without the app *and* deep-links for people with it.

---

## 12. Phase 9 — Filament 3D hero animation

**No dependencies. Pure delight, zero functional value, and that's fine.**

### 12.1 What and why

A 3D Rubik's-cube animation — faces carrying the conference identity, solving itself on the home screen or the about screen. It has no functional purpose. Its purpose is that developers screenshot it and post it, and it says "this app was made by people who care."

Two ways to build it. **Try the second one first.**

### 12.2 Option A: Compose-only (recommended first attempt)

A Rubik's cube is 27 axis-aligned boxes. That's tractable with `Canvas` and hand-rolled projection, at zero dependency cost, and it'll run on every device the app supports.

```kotlin
// chai/src/main/java/com/droidconke/chai/hero/CubeHero.kt

/**
 * A 3D Rubik's cube, rendered in Compose with a hand-rolled projection.
 *
 * No Filament, no glTF, no GPU pipeline: 27 cubelets × 6 faces, back-face culled,
 * painter-sorted. On a mid-range device this holds 60fps comfortably, and it adds
 * zero bytes of dependency.
 *
 * Try this before reaching for Filament (§12.3).
 */
@Composable
fun CubeHero(
    modifier: Modifier = Modifier,
    faceColors: List<Color> = ChaiCubeFaces,
    rotationsPerMinute: Float = 6f,
) {
    val reduceMotion = LocalReduceMotion.current
    val infinite = rememberInfiniteTransition(label = "cube")

    val yaw by if (reduceMotion) {
        remember { mutableFloatStateOf(0.6f) }
    } else {
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                tween((60_000 / rotationsPerMinute).toInt(), easing = LinearEasing),
            ),
            label = "cube-yaw",
        )
    }

    // A gentle pitch oscillation reads as "alive" without being distracting.
    val pitch by infinite.animateFloat(
        initialValue = -0.25f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(7_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cube-pitch",
    )

    Canvas(
        modifier
            .aspectRatio(1f)
            .semantics {
                // Decorative. Do not make TalkBack users listen to a description
                // of a spinning cube.
                hideFromAccessibility()
            },
    ) {
        val quads = buildCubeQuads(faceColors)
            .map { it.rotated(yaw = yaw, pitch = pitch) }
            .filter { it.facesViewer() }                 // back-face culling
            .sortedBy { it.averageDepth }                // painter's algorithm

        val scale = size.minDimension * 0.28f
        val centre = Offset(size.width / 2f, size.height / 2f)

        quads.forEach { quad ->
            val path = Path().apply {
                val projected = quad.vertices.map { it.project(scale, centre) }
                moveTo(projected[0].x, projected[0].y)
                projected.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            // Simple Lambertian shade so the faces read as 3D.
            drawPath(path, quad.color.shadedBy(quad.normal))
            drawPath(path, Color.Black.copy(alpha = 0.35f), style = Stroke(width = 2f))
        }
    }
}

private data class Vec3(val x: Float, val y: Float, val z: Float)

private fun Vec3.project(scale: Float, centre: Offset): Offset {
    // Weak perspective — enough depth cue without a full projection matrix.
    val perspective = 1f / (1f + z * 0.18f)
    return Offset(
        x = centre.x + x * scale * perspective,
        y = centre.y + y * scale * perspective,
    )
}
```

Complete this with the "solving" choreography — sequenced layer rotations following a real solve, so it looks intentional rather than random. Model it as a list of moves and animate through them:

```kotlin
/** Standard Rubik's notation. A short scripted solve reads better than randomness. */
private val SolveSequence = listOf(
    Move.R, Move.U, Move.RPrime, Move.UPrime,
    Move.F, Move.RPrime, Move.FPrime, Move.R,
    // …
)
```

### 12.3 Option B: Filament

Reach for Filament only if Option A can't deliver the visual you want — realistic materials, image-based lighting, reflections, a glTF asset authored by a designer.

```kotlin
// chai/src/main/java/com/droidconke/chai/hero/FilamentCubeHero.kt

@Composable
fun FilamentCubeHero(modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSupported by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) { isSupported = FilamentSupport.isDeviceSupported() }

    when (isSupported) {
        // Never leave a hole in the layout while probing.
        null -> Box(modifier.aspectRatio(1f))
        // Filament requires OpenGL ES 3.0+ / Vulkan. Fall back, don't crash.
        false -> CubeHero(modifier)
        true -> AndroidView(
            modifier = modifier.aspectRatio(1f),
            factory = { context ->
                SurfaceView(context).also { surfaceView ->
                    val renderer = CubeRenderer(context, surfaceView)
                    lifecycleOwner.lifecycle.addObserver(renderer)
                }
            },
        )
    }
}
```

```kotlin
/**
 * Filament renderer for the cube hero.
 *
 * Filament is a manual-resource-management API: every Engine, Scene, View, Renderer,
 * SwapChain, and entity must be explicitly destroyed, in order. Leaking any of them
 * leaks native memory that the GC will never reclaim. Hence the LifecycleObserver.
 */
private class CubeRenderer(
    private val context: Context,
    private val surfaceView: SurfaceView,
) : DefaultLifecycleObserver {

    private lateinit var engine: Engine
    private lateinit var renderer: Renderer
    private lateinit var scene: Scene
    private lateinit var view: View
    private lateinit var camera: Camera
    private lateinit var displayHelper: DisplayHelper
    private lateinit var uiHelper: UiHelper
    private lateinit var modelViewer: ModelViewer

    private var swapChain: SwapChain? = null

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            modelViewer.render(frameTimeNanos)
        }
    }
    private val choreographer = Choreographer.getInstance()

    override fun onCreate(owner: LifecycleOwner) {
        Utils.init()
        modelViewer = ModelViewer(surfaceView).apply {
            scene.skybox = null                       // transparent, blends with the app
            view.blendMode = View.BlendMode.TRANSLUCENT
            renderer.clearOptions = renderer.clearOptions.apply { clear = true }
        }

        // A designer-authored .glb keeps the visual out of Kotlin.
        context.assets.open("models/rubiks_cube.glb").use { input ->
            val bytes = ByteBuffer.wrap(input.readBytes())
            modelViewer.loadModelGlb(bytes)
            modelViewer.transformToUnitCube()
        }

        // Image-based lighting is what makes Filament worth the cost.
        loadIndirectLight("environments/studio_ibl.ktx")
    }

    override fun onResume(owner: LifecycleOwner) = choreographer.postFrameCallback(frameCallback)

    override fun onPause(owner: LifecycleOwner) = choreographer.removeFrameCallback(frameCallback)

    override fun onDestroy(owner: LifecycleOwner) {
        choreographer.removeFrameCallback(frameCallback)
        // Order matters — Filament will assert if you destroy the Engine first.
        modelViewer.destroyModel()
        // ModelViewer owns engine/view/scene teardown internally.
    }
}
```

**Costs to weigh honestly:**

| Concern | Impact |
| --- | --- |
| APK size | Filament native libs: ~3–5 MB per ABI. Real. |
| Device support | Needs GLES 3.0+. Fine on the target install base, but needs a fallback path. |
| Battery | Continuous 3D on a home screen drains battery. Pause when off-screen; do not animate forever. |
| Maintenance | Native lifecycle management. A leak here is a native OOM, which is much harder to debug than a Kotlin one. |
| Reviewability | Very few contributors will be able to review Filament code. Bus factor of one. |

**Recommendation: build Option A.** It gets 80% of the delight for 5% of the cost and the whole team can maintain it. Keep Filament as a stretch goal for a contributor who specifically wants to build it — and if they do, gate it behind Remote Config so it can be turned off if it burns battery in the field.

**Non-negotiables either way:**
- Pause when off-screen (`LifecycleEventEffect` / `Lifecycle.State.RESUMED` gating).
- Honour reduced-motion: static pose, no animation.
- `hideFromAccessibility()` — it's decoration.
- Never on the critical startup path. Load after first content is on screen.

---

## 13. Phase 10 — Play Store presence

**Mostly design and copy work. Screenshot automation depends on §10.2.**

### 13.1 Current state

`fastlane/` has an `Appfile`, a `Fastfile` with lint/test/build lanes, and `whatsnew/whatsnew-en-US`. There is **no `fastlane/metadata/android/` directory**, which means the entire store listing — title, descriptions, screenshots, feature graphic — is managed by hand in the Play Console. It is not versioned, not reviewable, and not reproducible.

Also: `fastlane/report.xml` is committed build output. Delete it and gitignore it.

### 13.2 Version the listing

```
fastlane/metadata/android/
├── en-US/
│   ├── title.txt                     (max 30 chars)
│   ├── short_description.txt          (max 80 chars)
│   ├── full_description.txt           (max 4000 chars)
│   ├── video.txt
│   ├── changelogs/
│   │   └── default.txt
│   └── images/
│       ├── icon.png                   512×512
│       ├── featureGraphic.png         1024×500
│       ├── phoneScreenshots/          1–8, 16:9 or 9:16
│       ├── sevenInchScreenshots/
│       ├── tenInchScreenshots/
│       └── tvScreenshots/
└── sw-KE/
    └── … (see §14)
```

```ruby
# fastlane/Fastfile — add
desc "Upload store metadata and screenshots without shipping a binary"
lane :update_listing do
  upload_to_play_store(
    skip_upload_apk: true,
    skip_upload_aab: true,
    skip_upload_changelogs: false,
    skip_upload_images: false,
    skip_upload_screenshots: false,
    track: 'production',
  )
end

desc "Ship to internal testing"
lane :internal do
  gradle(task: 'bundleRelease')
  upload_to_play_store(
    track: 'internal',
    aab: 'app/build/outputs/bundle/release/app-release.aab',
    skip_upload_metadata: true,
    skip_upload_images: true,
  )
end

desc "Promote internal → production with a staged rollout"
lane :promote_to_production do |options|
  upload_to_play_store(
    track: 'internal',
    track_promote_to: 'production',
    rollout: options[:rollout] || '0.1',   # start at 10%
    skip_upload_aab: true,
    skip_upload_metadata: true,
  )
end
```

**Fix the release workflow while here.** `deploy-to-playstore.yml` currently pushes straight to `track: production` with `status: completed` — no staged rollout, no gate. One bad build reaches 100% of users with no brake:

```yaml
- name: Deploy to Production (staged)
  uses: r0adkll/upload-google-play@v1
  with:
    serviceAccountJsonPlainText: ${{ secrets.GOOGLE_SERVICES_JSON }}
    packageName: ke.droidcon.kotlin
    releaseFiles: app/build/outputs/bundle/release/app-release.aab
    track: production
    status: inProgress          # was: completed
    userFraction: 0.1           # 10%, then promote manually after monitoring
    whatsNewDirectory: whatsnew/
    mappingFile: app/build/outputs/mapping/release/mapping.txt
    debugSymbols: app/build/intermediates/merged_native_libs/release/out/lib
```

Note `mappingFile` and `debugSymbols` — currently not uploaded, which means **production crash reports are unreadable**. That's a one-line fix with immediate value.

### 13.3 Automate screenshots

Hand-taken screenshots go stale the moment the UI changes. Generate them from the screenshot-test harness (§10.2), so they're always current:

```kotlin
// core/screenshot/src/test/kotlin/.../StoreScreenshotGenerator.kt

/**
 * Generates Play Store screenshots from the real UI with curated demo data.
 *
 * Run with: ./gradlew :core:screenshot:recordRoborazziDebug -Pstore-screenshots=true
 * Output:   fastlane/metadata/android/en-US/images/phoneScreenshots/
 *
 * These are the same components users see — they can never drift from the app.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StoreScreenshotGenerator {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun `01 - your personal agenda`() = captureStoreShot(
        fileName = "1_agenda",
        device = StoreDevice.Phone,
        headline = "Build your conference",
        subhead = "Star the talks you care about",
    ) {
        SessionsScreen(sessionsUiState = DemoData.curatedSessions, /* … */)
    }

    @Test
    fun `02 - never miss a talk`() = captureStoreShot(
        fileName = "2_now_next",
        device = StoreDevice.Phone,
        headline = "Never miss a talk",
        subhead = "Reminders that work offline",
    ) {
        HomeScreen(viewState = DemoData.duringConference, /* … */)
    }

    @Test
    fun `03 - ai agenda summary`() = captureStoreShot(
        fileName = "3_ai_summary",
        device = StoreDevice.Phone,
        headline = "Understand your schedule",
        subhead = "AI-powered agenda insights",
    ) {
        AgendaSummarySheet(state = DemoData.agendaSummary)
    }

    @Test
    fun `04 - ticket`() = captureStoreShot(
        fileName = "4_ticket",
        device = StoreDevice.Phone,
        headline = "Your ticket, offline",
        subhead = "Straight through the gate",
    ) {
        TicketScreen(state = DemoData.ticket)
    }

    @Test
    fun `05 - tablet two pane`() = captureStoreShot(
        fileName = "1_tablet_agenda",
        device = StoreDevice.TenInchTablet,
        headline = "Built for every screen",
        subhead = "Phones, tablets, and foldables",
    ) {
        SessionsListDetail(sessionsUiState = DemoData.curatedSessions, /* … */)
    }
}
```

`captureStoreShot` wraps the screen in a store frame — device bezel, headline, brand background — so the output is upload-ready rather than a raw screenshot:

```kotlin
private fun captureStoreShot(
    fileName: String,
    device: StoreDevice,
    headline: String,
    subhead: String,
    content: @Composable () -> Unit,
) {
    composeRule.setContent {
        StoreFrame(device = device, headline = headline, subhead = subhead) {
            ChaiTheme { content() }
        }
    }
    composeRule.onRoot().captureRoboImage(
        "${device.outputDir}/$fileName.png",
        roborazziOptions = RoborazziOptions(
            recordOptions = RoborazziOptions.RecordOptions(resizeScale = 1.0),
        ),
    )
}
```

**Do generate tablet screenshots.** Play requires 7" and 10" screenshots to be eligible for large-screen promotion, and after §4 the app will actually deserve them.

### 13.4 Listing copy

The current listing (whatever it says) almost certainly describes the app as an agenda viewer. That undersells it and buries the differentiators.

**Title** (30 chars): `droidcon Kenya` — leave room; don't pad with keywords.

**Short description** (80 chars):
> Your droidcon Kenya companion — agenda, tickets, and talks that fit your day.

**Full description** — lead with the user's problem, not the feature list:

```
Three days. Five tracks. Sixty talks. One question: where should you be right now?

droidcon Kenya's official app answers it.

★ BUILD YOUR CONFERENCE
Star the talks you care about and get a personal agenda that warns you when your
picks clash. Bulk-export the whole thing to your calendar in one tap.

★ NEVER MISS A TALK
Reminders before every session you starred — and they work when the venue wifi
doesn't. Everything is cached on your device.

★ UNDERSTAND YOUR SCHEDULE
See what your conference is actually about, which themes you've clustered around,
and which talks you've overlooked. Powered by Google's Gemini models, with
on-device processing where your device supports it.

★ YOUR TICKET, ALWAYS READY
Your QR ticket, offline, one tap from the home screen. No hunting for an email
at the gate.

★ REMEMBER WHO YOU MET
Scan a badge, jot a note. Three days later you'll still know who was building
what.

★ TAKE NOTES THAT KEEP
Notes attached to the talk they came from, timestamped, and yours — no account
required.

★ BUILT FOR EVERY SCREEN
Phones, tablets, foldables, and Chromebooks. On a big screen you get the full
room-by-time schedule grid.

★ OPEN SOURCE
Every line of this app is public, built by the Kenyan Android community.
Read it, learn from it, contribute:
https://github.com/droidconKE/droidconKeKotlin

Built with Kotlin, Jetpack Compose, and Navigation 3.
```

That last section matters more than it looks: the app's audience is Android developers, so **"read the source" is a feature**, and it's the reason people who aren't attending will still install it.

### 13.5 The rest of the Play Console

- **Data safety form** — must be updated once §6 ships. Declare exactly what the AI features send. A mismatch between the form and the code is a policy violation and can pull the listing.
- **In-app review** — prompt after a good moment (a session rated, a day completed), never on launch:

```kotlin
suspend fun requestReviewIfEarned(activity: Activity) {
    if (!reviewPreferences.hasEarnedPrompt()) return   // e.g. 3+ sessions checked in
    val manager = ReviewManagerFactory.create(activity)
    val info = manager.requestReviewFlow().await()
    manager.launchReviewFlow(activity, info).await()
    reviewPreferences.markPrompted()
}
```

- **In-app updates** — a conference app ships fixes mid-conference. An immediate-update flow for critical fixes and a flexible one otherwise:

```kotlin
// Flexible by default; immediate only when Remote Config says the build is broken.
val updateType = if (featureToggle.forceUpdate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
```

- **Pre-launch report** — free automated testing across a device farm on every internal-track upload. Read it; it catches crashes and accessibility issues you won't.
- **Store listing experiments** — A/B the feature graphic and short description. Free conversion data.
- **Custom store listing for Kenya** with Swahili copy (§14).

---

## 14. Accessibility and localization

**Depends on: §3.3 B10 — nav labels are hardcoded Kotlin strings today, so the navigation bar literally cannot be translated until that lands.**

### 14.1 Swahili

The app is for a Kenyan conference and ships English only. Swahili is the national language, spoken by the majority of the country. Adding it is:

- The right thing to do.
- A visible statement about who the app is for.
- A real accessibility win for attendees who aren't comfortable in technical English.
- A strong story for the Play listing and a good conference talk.

Practical scope: **UI chrome in Swahili, content in its original language.** Session titles and descriptions are written by speakers in English and shouldn't be machine-translated in place — but the *app* around them can be Swahili, and §6.10's on-device translation offers per-item translation on demand.

```xml
<!-- presentation/src/main/res/values-sw/strings.xml -->
<resources>
    <string name="nav_home">Nyumbani</string>
    <string name="nav_sessions">Vipindi</string>
    <string name="nav_feed">Habari</string>
    <string name="nav_ticket">Tikiti</string>
    <string name="nav_about">Kuhusu</string>

    <string name="session_live_now">Inaendelea sasa</string>
    <string name="session_starred">Umehifadhi kipindi hiki</string>
    <string name="sessions_my_sessions">Vipindi vyangu</string>

    <!-- Plurals work differently per language — never build these by string concat -->
    <plurals name="sessions_count">
        <item quantity="one">Kipindi %d</item>
        <item quantity="other">Vipindi %d</item>
    </plurals>
</resources>
```

This requires the string-resource work from §3.3 B10 to land first: today the bottom nav labels are hardcoded Kotlin strings in `Screens`, so **the navigation bar cannot be translated at all.**

Have a native Swahili speaker from the community review the translations. Machine-translated UI strings in a language you don't speak is how you end up with something unintentionally funny on the biggest screen at the conference.

Add per-app language support so users can pick independently of the system:

```xml
<!-- app/src/main/res/xml/locales_config.xml -->
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en" />
    <locale android:name="sw" />
</locale-config>
```

```xml
<application android:localeConfig="@xml/locales_config" ...>
```

```kotlin
// In-app language picker
AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("sw"))
```

### 14.2 Accessibility checklist

Beyond §5.6's semantics work, the things that need explicit verification:

| Area | Check |
| --- | --- |
| **TalkBack** | Every screen navigable; every action reachable; no unlabelled controls; reading order logical |
| **Font scale** | Legible and unclipped at 200%; no fixed-height text containers |
| **Display size** | Largest display size setting doesn't break layouts |
| **Contrast** | 4.5:1 for body text, 3:1 for large text — **audit `ChaiColors` pairs**; `textWeakColor` on `background` is the likely failure |
| **Touch targets** | 48×48 dp minimum; audit every `IconButton` and the bottom nav items |
| **Colour independence** | Session status (live/upcoming/past) must not be conveyed by colour alone — add an icon or text |
| **Reduced motion** | Continuous animations respect the setting (§5.4, §12) |
| **Switch Access / keyboard** | Full traversal without touch |
| **Screen reader + two-pane** | Pane changes announced on tablet (§4.4) |

The contrast audit is the one most likely to find real problems. Write it as a test so it can't regress:

```kotlin
// chai/src/test/kotlin/.../ColorContrastTest.kt

/**
 * WCAG AA contrast for every text-on-background pair Chai defines.
 *
 * Colour-contrast failures are invisible to people with normal vision and
 * completely blocking for people without it. A test is the only reliable guard.
 */
class ColorContrastTest {

    private data class Pair(val name: String, val foreground: Color, val background: Color, val isLargeText: Boolean = false)

    /**
     * The M3 role pairs. After §3.5 these are the ones that matter, because the
     * on/background relationship is part of the type — which is exactly why the
     * bridge makes this test possible to write exhaustively.
     */
    private fun schemePairs(scheme: ColorScheme, label: String) = listOf(
        Pair("$label on-background", scheme.onBackground, scheme.background),
        Pair("$label on-surface", scheme.onSurface, scheme.surface),
        Pair("$label on-surface-variant", scheme.onSurfaceVariant, scheme.surface),
        Pair("$label on-surface-on-container", scheme.onSurface, scheme.surfaceContainer),
        Pair("$label on-surface-on-container-high", scheme.onSurface, scheme.surfaceContainerHigh),
        Pair("$label on-primary", scheme.onPrimary, scheme.primary),
        Pair("$label on-primary-container", scheme.onPrimaryContainer, scheme.primaryContainer),
        Pair("$label on-secondary", scheme.onSecondary, scheme.secondary),
        Pair("$label on-tertiary", scheme.onTertiary, scheme.tertiary),
        Pair("$label on-error", scheme.onError, scheme.error),
        Pair("$label primary-on-background", scheme.primary, scheme.background),
        // Outlines are non-text UI: WCAG asks 3:1, same as large text.
        Pair("$label outline-on-surface", scheme.outline, scheme.surface, isLargeText = true),
    )

    /** The surviving brand component tokens, which have no structural pairing. */
    private fun componentPairs(
        chai: ChaiColors,
        scheme: ColorScheme,
        label: String,
    ) = listOf(
        Pair("$label nav-active-label", chai.activeBottomNavTextColor, scheme.surfaceContainer),
        Pair("$label day-chip-active", chai.eventDaySelectorActiveTextColor, chai.eventDaySelectorActiveSurfaceColor),
        Pair("$label day-chip-inactive", chai.eventDaySelectorInactiveTextColor, chai.eventDaySelectorInactiveSurfaceColor),
        Pair("$label link-on-background", chai.linkTextColor, scheme.background),
        Pair("$label live-indicator", chai.liveIndicatorColor, scheme.surface, isLargeText = true),
    )

    @Test
    fun lightScheme_meetsWcagAa() = assertAllPairsPass(
        schemePairs(ChaiLightColorScheme, "light") +
            componentPairs(ChaiLightComponentColors, ChaiLightColorScheme, "light"),
    )

    @Test
    fun darkScheme_meetsWcagAa() = assertAllPairsPass(
        schemePairs(ChaiDarkColorScheme, "dark") +
            componentPairs(ChaiDarkComponentColors, ChaiDarkColorScheme, "dark"),
    )

    private fun assertAllPairsPass(pairs: List<Pair>) {
        val failures = pairs.mapNotNull { pair ->
            val ratio = contrastRatio(pair.foreground, pair.background)
            val required = if (pair.isLargeText) 3.0 else 4.5
            if (ratio < required) "${pair.name}: %.2f:1 (needs %.1f:1)".format(ratio, required) else null
        }
        assertThat(failures).isEmpty()
    }

    /** WCAG 2.1 relative luminance and contrast ratio. */
    private fun contrastRatio(a: Color, b: Color): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun channel(c: Float): Double {
            val v = c.toDouble()
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }
}
```

> Expect this test to **fail on first run**. That's the point — it's finding real problems. Fix the palette, don't relax the threshold.

---

## 15. CI/CD and developer experience

**No dependencies. Highest leverage per hour spent in this entire document — do §15.1 first, before anything else in the plan.**

### 15.1 The single most important fix

**Done** — `.github/workflows/pr.yml`. It differs from the sketch below: instrumentation
uses the Gradle Managed Devices from `build-logic/.../ManagedDevices.kt` rather than
`android-emulator-runner`, and the screenshot-test and APK-size-diff jobs are omitted
because Roborazzi and `.github/actions/apk-size-diff` do not exist yet. `branch.yml` is now
redundant with `pr.yml`; deleting it is a follow-up.

`branch.yml` triggers only on:

```yaml
paths:
  - 'build-logic/**'
  - 'build.gradle.kts'
  - 'settings.gradle.kts'
```

**A pull request that changes only Kotlin source code runs no CI.** No lint, no detekt, no tests, no build. For an open-source project taking community contributions, this is the highest-severity issue in the repository — higher than any bug in §1.3, because it's the reason bugs get in.

```yaml
# .github/workflows/pr.yml
name: PR

on:
  pull_request:
  merge_group:

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

permissions:
  contents: read
  pull-requests: write

jobs:
  static-analysis:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'zulu', java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4
        with: { cache-read-only: ${{ github.ref != 'refs/heads/main' }} }

      - run: ./gradlew spotlessCheck ktlintCheck detekt --continue
      - run: ./gradlew lint
      - uses: github/codeql-action/upload-sarif@v3
        if: always()
        with: { sarif_file: app/build/reports/lint-results-debug.sarif }

  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'zulu', java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4

      - run: ./gradlew testDebugUnitTest
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/build/reports/tests/'

      - run: ./gradlew createDebugCombinedCoverageReport
      - uses: codecov/codecov-action@v5
        with: { token: '${{ secrets.CODECOV_TOKEN }}' }

  screenshot-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'zulu', java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4

      - run: ./gradlew verifyRoborazziDebug
      - name: Upload screenshot diffs
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: screenshot-diffs
          path: '**/build/outputs/roborazzi/**/*_compare.png'
      - name: Comment diffs on PR
        if: failure()
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: '📸 Screenshot tests failed. Diff images are in the workflow artifacts. If the change is intentional, run `./gradlew recordRoborazziDebug` and commit the updated goldens.'
            })

  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'zulu', java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4

      - run: ./gradlew assembleDebug

      # Fail the PR if the app grows more than 2% without an explicit override.
      - name: APK size check
        uses: ./.github/actions/apk-size-diff
        with:
          base-ref: ${{ github.base_ref }}
          threshold-percent: 2

  instrumentation-tests:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        # 24 is not optional: it is the declared minSdk, and starting this matrix at
        # 26 is precisely why B11 (java.time without desugaring) shipped undetected.
        # If a device level is supported, CI runs on it.
        api-level: [24, 26, 30, 35]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'zulu', java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4

      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' \
            | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules && sudo udevadm trigger --name-match=kvm

      - uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          arch: x86_64
          disable-animations: true
          emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim
          script: ./gradlew connectedDebugAndroidTest

  dependency-review:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/dependency-review-action@v4
        with: { fail-on-severity: moderate }
```

Also: bump `actions/checkout@v3` → `v4` and `setup-java@v3` → `v4` in the existing workflows, and replace the deprecated `gradle/gradle-build-action@v2` with `gradle/actions/setup-gradle@v4`.

### 15.2 Dependabot

**Done** — `.github/dependabot.yml`, weekly on Monday, grouped.

`toml-checker` and `toml-updater` were declared in the catalog but never applied, so nothing tracked dependency drift.

**Dependabot rather than Renovate**, reversing this section's earlier recommendation. The reason Renovate was preferred here was grouping — without it a repo this size gets forty PRs a week. Dependabot has supported `groups:` since 2023, so that advantage is gone, and what remains is a deployment difference: Renovate's hosted app must be installed at the `droidconKE` organisation level, which needs org-admin rights and a separate approval loop, while Dependabot is a file in the repo and nothing else. For a community project where the maintainer may not be an org admin, that matters more than any remaining feature gap. Renovate stays a reasonable swap later if the dependency dashboard becomes worth the install.

The config groups AndroidX, Compose, Kotlin + KSP, Firebase, Ktor, Room, Hilt, test libraries and static-analysis plugins, and covers both the `gradle` ecosystem — which reads `gradle/libs.versions.toml` directly, so catalog entries are bumped in place — and `github-actions`.

Two deliberate choices:

- **Nothing is auto-merged.** Dependabot does not auto-merge without an explicit workflow, so the earlier "AGP and Kotlin must never auto-merge" rule needs no special case. The earlier draft also auto-merged patch bumps of test dependencies; that is dropped, because a test-library patch that silently changes assertion behaviour is exactly the kind of thing this project has no screenshot coverage to catch (§10.2).
- **AGP is left ungrouped on purpose.** `com.android.tools.build:gradle` matches no group pattern, so it arrives as its own PR. Given §3.8, an AGP bump is a migration rather than a version change and deserves to be reviewed alone.

### 15.3 Contributor experience

The repo has `CONTRIBUTING.md` and a 400-line README. Improve targeted things:

**A module map in the README.** New contributors currently have to reverse-engineer where things live. A short table plus a Mermaid dependency graph pays for itself immediately.

**Document the debug-keystore decision** (§1.7 S1) so nobody "fixes" it.

**Add `.editorconfig` rules that match ktlint,** so the IDE and CI agree. Currently `.editorconfig` exists — verify it encodes the same rules ktlint enforces, otherwise contributors get formatted-then-rejected.

**A PR template with a real checklist:**

```markdown
<!-- .github/pull_request_template.md -->
## What
<!-- One paragraph. What changes, and why. -->

## How to verify
<!-- Steps a reviewer can follow. "Ran the app" is not steps. -->

## Checklist
- [ ] `./gradlew spotlessApply ktlintFormat` run
- [ ] Tests added or updated (and they fail without the change)
- [ ] Screenshot goldens updated if UI changed (`./gradlew recordRoborazziDebug`)
- [ ] Strings in `strings.xml`, not hardcoded
- [ ] Colours from `MaterialTheme` / `chaiColorsPalette`, not literals
- [ ] Content descriptions on non-decorative images/icons
- [ ] Checked in dark mode
- [ ] Checked at 200% font scale
- [ ] Checked on a tablet or in split-screen
- [ ] No new dependency, or the new dependency is justified below

## Screenshots
| Before | After |
| --- | --- |
|  |  |
```

**A `/run` path that just works.** Document one command that builds and installs a debug build, and make sure it works on a clean clone with no local setup beyond the JDK. Verify `local.properties` isn't required.

### 15.4 Observability

Crashlytics is wired but under-used. Cheap additions with high debugging value:

```kotlin
// Custom keys so a crash report tells you the state, not just the stack
Firebase.crashlytics.apply {
    setCustomKey("window_size_class", windowSize.name)
    setCustomKey("is_signed_in", isSignedIn)
    setCustomKey("starred_session_count", starredCount)
    setCustomKey("last_sync_ago_minutes", lastSyncAgo.inWholeMinutes)
    setCustomKey("ai_engine_available", availableEngines.joinToString())
    setCustomKey("conference_phase", conferencePhase::class.simpleName ?: "unknown")
}
```

Non-fatal reporting for the failures that currently vanish into Timber:

```kotlin
// SyncDataWorker: a sync that always fails is invisible today
if (!syncedSuccessfully) {
    Firebase.crashlytics.recordException(SyncFailedException(failedRepos))
    Result.retry()
}
```

And upload mapping files (§13.2), without which every release crash report is unreadable obfuscated garbage.

---

## 16. Roadmap and sequencing

### 16.0 P0 — ships alongside this plan document

These are the small, self-contained fixes that should land **with** the PR that adds this plan, not after it. Every one is a real defect with a bounded diff, needs no product decision, and no design review. Ordered by severity.

| # | Fix | Finding | Files | Test that proves it |
| --- | --- | --- | --- | --- |
| 1 | Enable core library desugaring | **B11** | `build-logic/.../KotlinAndroid.kt` (+`isCoreLibraryDesugaringEnabled`, +`coreLibraryDesugaring` dep) | Instrumentation test on an **API 24** emulator that triggers a session sync |
| 2 | Remove `fallbackToDestructiveMigration` | **B2** | `datasource/local/.../di/DatabaseModule.kt`, `Database.kt` (`exportSchema = true`) | `MigrationTest.migrate5To6_preservesBookmarks` (note: column is `sessionId`) |
| 3 | Derive filter options from data; fix the room + case comparison | **B1, B12** | `SessionsFilterPanel.kt`, `SessionsFilterState.kt`, `SessionsViewModel.kt`, `domain/models/Session.kt` (+`roomList`) | `SessionsFilterStateTest` — 5 tests in §3.3, incl. the "no dead option" property |
| 4 | Delete the topics filter path | §1.4 | `SessionsFilterCategory.kt`, `SessionsFilterState.kt`, `SessionsViewModel.updateFilterState` | Compiles; no behaviour to test — it was unreachable |
| 5 | Unify `targetSdk` behind one catalog entry | **B4** | `libs.versions.toml`, both convention plugins | `./gradlew :presentation:dependencies` — or just read the merged manifest |
| 6 | Add `key` to 8 lazy lists | **B14** | 8 files listed in §1.3 | Existing screenshot/UI tests; scroll-position assertion on `SpeakersScreen` |
| 7 | Delete `safeApiCall`, `ServerError`, `NetworkError`, `DateStringConverter` | §1.4 | `SafeApiCall.kt`, `DateStringConverter.kt` | Deleting `AuthManagerTest`'s `NetworkError` case is part of the fix (**B13**) |
| 8 | Fix `AuthManager`'s error branch | **B13** | `AuthManager.kt` | Test that a real Ktor `IOException` yields `networkError = true` |
| 9 | Hoist `speakers.take(8)` out of composition | **B14** | `HomeSpeakersSection.kt`, `HomeViewModel.kt` | Compose compiler report shows the composable skippable |
| 10 | Delete `-Xjvm-default=all` no-op; `packagingOptions` → `packaging` | **B3**, §1.4 | `presentation/build.gradle.kts`, `app`, `chai` | `./gradlew build --warning-mode all` |
| 11 | Fix `getTimeDifference` nullable deref | **B16** | `DateAndTimeUtils.kt` | Test with a malformed timestamp asserting a sensible fallback, not the raw input |
| 12 | Delete `ExampleUnitTest` / `ExampleInstrumentedTest`; untrack `api_key.txt`, `fastlane/report.xml`, `build-logic/gradle/` | §1.4, S2 | as listed | `git ls-files` is clean |

**Sequencing within P0:** #1 first and on its own, because it's a crash fix and it should be reviewable in isolation. #2 next. #3 and #4 belong in one PR — deleting topics while fixing rooms keeps the filter code coherent. #5–#12 can go in any order, and several are one-liners that can be batched.

**Deliberately not P0**, despite being cheap: B5 (`findActivity`), B6 (state divergence), B7 (splash race), B8 (theme-blind text), B9/B16's `kotlinx-datetime` migration, B10 (nav keys), B15 (card colours). Each is either a prerequisite for a later phase and better done with it, or needs a decision. B9's date migration in particular **must not** be attempted before #1 lands.

### 16.1 If you only do five things

In priority order. Each is independently valuable and none depends on the others landing first (beyond Phase 0):

| # | Thing | Why it's top-five |
| --- | --- | --- |
| **1** | **CI on Kotlin PRs** (§15.1) | Every future bug becomes cheaper to catch. Smallest change in the document, permanent compounding value. |
| **2** | **The P0 list** (§16.0) | Twelve real defects, including a crash on the declared minSdk and one that deletes user agendas. |
| **3** | **Edge-to-edge + insets** (§3.4) | Correctness, not polish — and a hard prerequisite for the targetSdk 37 bump. |
| **4** | **Baseline profile** (§9.2) | Best startup improvement available per unit of work, and it needs no product decisions. |
| **5** | **Calendar export + venue map** (§11.4a, §11.6) | The two highest value-to-effort features in the document. Neither needs a backend. |

Notice what's *not* in the top five: the AI work. It's the most exciting section and it should not be first. Build it on a codebase that has CI, tests, and no data-loss bugs — otherwise the AI features become the thing that gets blamed when something unrelated breaks.

### 16.2 Stages, in dependency order

Ordered, not scheduled. Each stage is a coherent unit that leaves the app shippable when it completes. Move to the next when the previous one's milestone is met — not on a date.

**Stage 1 — Make the codebase safe to change**
- §15.1 CI on all PRs — do this first, before touching any source
- §3 Phase 0 in full, including the shared `targetSdk` ref
- §3.6 year-agnostic rename (one mechanical PR, announced in advance)
- §10.4 test infrastructure: `:core:testing`, injected `Clock`, consolidated fakes
- §9.1–9.2 benchmark module + baseline profile
- **§16.0's P0 list in full** — this is the stage's actual headline
- §3.8 §6 AGP-9-readiness plugin bumps (KSP, Hilt, Firebase Perf) — independent of AGP 9, do them now
- §3.8 steps 1–5: Gradle 9.1, Kotlin 2.3.x, `nonFinalResIds`
- **Milestone:** green CI on every PR **including API 24** · no crash on any supported API level · zero known data-loss bugs · startup measured with a number written down

**Stage 2 — Make it a 2026 app**
- §3.5 → §5 design system: token restructure, then M3 Expressive
- §10.2 Roborazzi screenshot suite — build this *alongside* §5, because it's how §5 gets reviewed
- §4 adaptive & large screen
- Compose BOM / material3 1.4.x bump — **hard prerequisite for any Expressive work** (§3.5)
- §3.4 insets, then **AGP 9.x + targetSdk 37 together** (§3.8 §0 explains why one PR)
- §2 extract `:core:designsystem` plus one feature module, to prove the pattern
- §14 Swahili + accessibility audit
- **Milestone:** correct on every form factor · visual regressions caught in CI · contrast test green

**Stage 3 — Make it worth installing**
- §6 intelligent experiences, every flag default-off
- §7 ticketing + QR — **the backend conversation must have started during Stage 2**
- §8 notifications
- §5.5 notes · §11.4a calendar export · §11.6 venue map · §11.7 widget
- **Milestone:** the app does things no other conference app does, all behind flags that have been tested in the off position

**Stage 4 — Ship it**
- §9.3–9.5 perf & size pass, with before/after numbers
- §10.3 E2E journeys green in CI
- §13 store listing, generated screenshots, staged rollout
- Internal track → 10% → 100%, each step gated on Crashlytics being clean
- **Feature freeze well before the conference.** Fixes only. Pick the date early and hold it.
- Gate rehearsal with the actual registration staff, on their actual devices, with time left to fix what it finds
- **Milestone:** shipped, staged, monitored

**Stage 4.5 — AGP 9, when the ecosystem is ready**
- Gate: detekt 2.0 released, and a ktlint-gradle release that doesn't need `android.builtInKotlin=false`
- Gate: an AGP release whose API cap covers the targetSdk the app ships (§3.8 §0)
- Then §3.8 §7 steps 6–8, in order
- **Milestone:** AGP 9 with **no** opt-out flags in `gradle.properties`. Landing it with the flags on is a half-migration, not a milestone.

**Stage 5 — After the conference**
- §6.11 recap notification, a couple of days out
- Retrospective driven by analytics: which features got used? Instrument for this during Stage 3, not after.
- Prune. Anything with negligible engagement is a candidate for deletion, not iteration.

**Deliberately deferred:** job board (§11.1), code challenge (§11.3), booking (§11.4b), Filament (§12), connections (§11.2). Each needs either a backend commitment or a named editorial owner. Revisit at the §11.0 planning meeting — and if the answer is "nobody owns the content," the answer is no.

### 16.3 Parallelisation

The natural split for a small contributor pool, chosen so people don't collide:

| Track | Owns | Sections |
| --- | --- | --- |
| **Foundations** | Build, CI, perf, testing infra | §3.1–3.2, §9, §10.4, §15 |
| **Design system** | `chai`, theme, motion, a11y | §3.5, §5, §10.2, §14 |
| **Adaptive** | Navigation, layouts, large screen | §4, §11.8 |
| **AI** | `:core:ai` and its features | §6 |
| **Conference ops** | Ticketing, notifications, widget, map | §7, §8, §11.6–11.7 |

Cross-track dependencies to watch:
- Design system must land §3.5 before Adaptive can use `MaterialExpressiveTheme`.
- Foundations must land §3.3 B5 before Design System can write screenshot tests.
- Adaptive must land §3.3 B10 before Conference Ops can add a Ticket tab.
- AI needs `:core:testing` from Foundations to test its router.

### 16.4 Definition of done, per PR

Non-negotiable for every PR in every phase:

1. Compiles, and `./gradlew spotlessCheck ktlintCheck detekt lint` passes
2. Tests added that **fail without the change** — a test that passes before and after tests nothing
3. Screenshot goldens updated if UI changed
4. No hardcoded strings, colours, or dimensions
5. Verified in dark mode, at 200% font scale, and on a tablet
6. No new dependency without a one-line justification in the PR
7. Public APIs have KDoc, and the KDoc says *why*, not *what*

---

## 17. Risks and open questions

### 17.1 Risks

| Risk | Likelihood | Impact | Mitigation |
| --- | --- | --- | --- |
| **Half-migrated state.** Volunteer capacity evaporates mid-phase, leaving two design systems and three navigation patterns coexisting. | **High** | **High** | Every phase must be independently completable and shippable. Never start Phase N+1 with Phase N half-done. Prefer deprecated shims (§3.5) over big-bang rewrites. |
| **AI cost blowout.** A retry bug or a bot burns the Gemini quota during the conference. | Medium | High | App Check enforced (§6.5), client quota guard (§6.12), Remote Config kill switch, budget alerts in GCP. Test the kill switch before the conference. |
| **Gate failure on day 1.** Ticketing ships untested at scale and the queue backs up. | Medium | **Severe** | Offline-first by design (§7.2). Rehearse with real staff a week out. Keep the paper/email fallback for year one — do not make the app the only path. |
| **Model hallucination in a user-visible place.** The agenda summary invents a session. | Medium | Medium | Structured output constrained to provided ids, deterministic path always shown alongside (§6.7), thumbs-down monitoring with a 25% kill threshold. |
| **Package rename invalidates every open PR.** | High | Medium | Announce two weeks ahead, do it in one mechanical PR, merge everything outstanding first, do it during a quiet period. |
| **Gemma model download over mobile data.** A user pays for 500 MB by accident. | Medium | High | Unmetered-only default, explicit size in the UI, opt-in with a confirmation, cancellable (§6.4). |
| **Large-screen work regresses phone UX.** | Medium | Medium | Screenshot matrix includes phone variants (§10.2). Phone is the primary form factor and stays the default code path. |
| **AGP 9 lands as a half-migration.** detekt < 2.0 and the ktlint plugin both require opt-out flags, so AGP 9 ships with `newDsl=false` + `builtInKotlin=false` and the flags never come off. | **High** | Medium | Treat flags-removed as the definition of done (§3.8 §7 step 8, Stage 4.5). Tracking issue per flag with the version that removes it. Don't schedule AGP 9 before detekt 2.0 — the version bump alone buys little (§3.8 §6). |
| **No AGP 9.x supports API 37 when the migration is ready.** AGP 9.0 caps at 36.1, and both targetSdk 37 and AGP 9 are committed. | Medium | Medium | Prerequisite work (§3.8 steps 1–5) is version-independent, so it proceeds regardless. Contingency: targetSdk 37 on latest AGP 8.x first, AGP 9 after. Check the API cap before pinning any AGP version. |
| **Expressive work starts before the material3 bump** and burns a sprint on code that cannot compile. | Medium | Low | The BOM/material3 1.4.x bump is a named prerequisite in §3.5 and a Stage 2 line item. §3.5's colour work can land on 1.3.2; only the Expressive pieces are blocked. |
| **Kotlin 2.1 → 2.3 breaks something subtle.** Bundled into the AGP bump, it becomes impossible to bisect. | Medium | Medium | Its own PR, ahead of the AGP work (§3.8 §7 step 4). Screenshot tests and E2E journeys are the safety net. |
| **Backend isn't ready** for signed tickets, jobs, or bookings. | **High** | Medium | Start the backend conversation during Stage 2, not Stage 3. Every backend-dependent feature has a documented degraded v0. |
| **Editorial features ship empty.** Job board with no jobs; challenges with no challenges. | High | Medium | Do not build without a named owner who has committed to content (§11.0). |
| **Filament battery drain** in the field. | Low | Medium | Remote Config gate, pause off-screen, Compose fallback is the default (§12.3). |
| **New contributors bounce** off a 400-line README and an unfamiliar architecture. | Medium | Medium | Module map, good-first-issue labels, a `/run` command that works on a clean clone (§15.3). |

### 17.2 Open questions — need answers from outside engineering

**For the organising team:**

1. Will there be a **backend** for signed tickets this cycle? This determines whether §7 ships properly or as the degraded v0.
2. Is there a named **owner for job board content**? If not, §11.1 is out.
3. Will there be **speaker office hours**? Without them, §11.4b has no supply and shouldn't be built.
4. Can we get a **venue floor plan as SVG**? Unlocks §11.6, which is one of the cheapest high-value features.
5. Who **moderates** the social feed during the conference? Currently read-only, and it should probably stay that way unless someone owns moderation.
6. Are session **recordings** published, and where? Determines whether timestamped notes (§5.5) can deep-link into video.
7. Is there budget for **Gemini API usage** beyond the free tier, and what's the ceiling? Sets `ai_max_daily_tokens`.

**For engineering to decide:**

8. **Coil 2 → 3** now or later? Recommendation: during Phase 2, while touching the image-loading surface anyway.
9. **Kover instead of Jacoco**? Kover is Kotlin-native and handles inline functions better. Low priority; Jacoco works.
10. **KMP?** `:domain` and most of `:data` are already pure Kotlin. Making them multiplatform would enable an iOS app and a web agenda from the same models. **Genuinely worth considering** — but only if someone wants to build the iOS app. Note that this interacts with §3.8: AGP 9 introduces `com.android.kotlin.multiplatform.library` for KMP modules, and the [JetBrains AGP 9 migration skill](https://github.com/Kotlin/kotlin-agent-skills/tree/main/skills/kotlin-tooling-agp9-migration) is written primarily for exactly that migration. If KMP is on the table, doing it *after* AGP 9 rather than before avoids migrating the same modules twice.
11. **Module split depth** (§2) — is the full `:core:*` / `:feature:*` split worth it at 21k LOC? Recommendation: split `:core:designsystem` and the largest features; don't split for the sake of symmetry. Seven well-bounded modules beat twenty-five badly-bounded ones.
12. **How much analytics?** Deciding what to prune post-conference requires knowing what got used. Instrument in Q3, and be explicit in the Data Safety form.

---

## 18. Appendix A — file-by-file change index

Quick reference for where each finding lives.

### Correctness

| Finding | File | Section |
| --- | --- | --- |
| B1 topics filter dead | `presentation/.../sessions/view/SessionsViewModel.kt`, `SessionsFilterState.kt`, `domain/.../models/Session.kt` | §3.3 |
| B2 destructive migration | `datasource/local/.../di/DatabaseModule.kt`, `Database.kt` | §3.3 |
| B3 no-op compiler flag | `presentation/build.gradle.kts` | §3.3 |
| B4 targetSdk mismatch (34/36 → shared ref, then 37) | `build-logic/.../AndroidLibraryConventionPlugin.kt`, `AndroidApplicationConventionPlugin.kt` | §3.1 |
| B5 `findActivity` throws | `chai/.../Theme.kt` | §3.3 |
| B6 state not saveable | `presentation/.../sessions/view/SessionsScreen.kt` | §3.3 |
| B7 splash blocks on network | `presentation/.../activity/MainActivity.kt`, `MainViewModel.kt` | §3.3 |
| B8 hardcoded colours | `chai/.../components/CText.kt` | §3.3 |
| B9 `SimpleDateFormat` | `presentation/.../sessions/view/SessionsViewModel.kt` | §3.3 |
| B10 mutable nav keys | `presentation/.../common/navigation/Screens.kt` | §3.3 |

### Build

| Change | File | Section |
| --- | --- | --- |
| `dependencyResolutionManagement` | `settings.gradle.kts` | §3.1 |
| Config cache, parallel, R8 full mode | `gradle.properties` | §3.1 |
| Remove `allprojects`, fix detekt | `build.gradle.kts` | §3.1 |
| `compilerOptions`, desugaring | `build-logic/.../KotlinAndroid.kt` | §3.1 |
| Remove compose-compiler dep, fix `buildDir` | `build-logic/.../AndroidCompose.kt` | §3.1 |
| Shared `targetSdk`, `packaging {}` | `build-logic/.../AndroidLibraryConventionPlugin.kt` | §3.1 |
| Catalog cleanup | `gradle/libs.versions.toml` | §3.2 |
| `isShrinkResources`, benchmark buildType | `app/build.gradle.kts` | §9.3 |
| Keep rules with reasons | `app/proguard-rules.pro` | §9.3 |
| Stability config | `compose_compiler_config.conf` (new) | §3.1 |
| Delete `kotlinOptions()` extension helper | `build-logic/.../KotlinAndroid.kt` | §3.8 |
| Gradle 8.11.1 → 9.x; delete stale second wrapper | `gradle/wrapper/`, `build-logic/gradle/` | §3.8 |
| `nonFinalResIds=false` → removed | `gradle.properties` | §3.8 |
| Firebase Crashlytics 2.9.9 / Perf 1.4.2 plugin bumps | `gradle/libs.versions.toml` | §3.2, §3.8 |

### Deletions

```
app/src/test/java/com/android254/droidconKE2023/ExampleUnitTest.kt
app/src/androidTest/java/com/android254/droidconKE2023/ExampleInstrumentedTest.kt
fastlane/report.xml
build-logic/gradle/                                  # stale second Gradle wrapper (§3.8)
api_key.txt                                          # stale 15-byte string, not a live key (§1.7 S2)
chai/.../components/CText.kt: CParagraph, CPageTitle, CSubtitle, CActionText
presentation/src/main/res/drawable/*                 # 12 duplicated from chai
presentation/.../common/components/{Loader,LoadingBox,AnimatedShimmerEffect}.kt  # consolidate to 2
presentation/src/main/res/raw/loading.json           # with the Lottie dependency
```

### New modules

```
benchmark/                    §9.1
baselineprofile/              §9.2
core/ai/                      §6.2
core/testing/                 §10.4
core/screenshot/              §10.2
widget/                       §11.7
feature/ticket/               §7
```

---

## 19. Appendix B — reference material

The apps and docs this plan draws on, and what specifically to take from each.

### Reference apps

| App | Take from it |
| --- | --- |
| [Now in Android](https://github.com/android/nowinandroid) | Convention plugin structure (already partially adopted — go further), `:core`/`:feature` module conventions, `:core:testing` patterns, Roborazzi setup, baseline profile module layout |
| [jetpacker (ai-samples)](https://github.com/android/ai-samples/tree/main/jetpacker) | Firebase AI Logic + ML Kit GenAI wiring, on-device/cloud fallback patterns, structured output usage |
| [Adaptive JetStream](https://github.com/android/adaptive-apps-samples/tree/main/AdaptiveJetStream) | `NavigationSuiteScaffold`, `ListDetailPaneScaffold`, posture handling, the adaptive navigation patterns in §4 |
| [Socialite](https://github.com/android/socialite) | CameraX + Compose integration, media handling, `camera-compose` viewfinder usage |
| [Compose Samples](https://github.com/android/compose-samples) | Jetsnack for design-system layering; Jetcaster for adaptive + media; Reply for two-pane navigation |
| [Google I/O app](https://github.com/google/iosched) | Conference-app domain modelling, schedule conflict handling, agenda UX prior art |
| [DroidKaigi conference app](https://github.com/DroidKaigi/conference-app-2024) | The other serious OSS conference app. Compare notes on schedule grid, KMP approach, and screenshot testing at scale |

### Documentation

**AI**
- [developer.android.com/ai](https://developer.android.com/ai) — the umbrella; start here
- [Firebase AI Logic](https://firebase.google.com/docs/ai-logic) — cloud Gemini, structured output, function calling, App Check
- [ML Kit GenAI APIs](https://developers.google.com/ml-kit/genai) — on-device summarisation, image description, rewriting, proofreading
- [LiteRT for Android](https://developers.google.com/edge/litert/android) — on-device open models
- [MediaPipe LLM Inference](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android) — the Gemma runtime used in §6.4
- [AI Edge Function Calling SDK](https://ai.google.dev/edge/mediapipe/solutions/genai/function_calling) — on-device agents
- [Play Asset Delivery](https://developer.android.com/guide/playcore/asset-delivery) — model distribution

**Adaptive**
- [Adaptive layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive)
- [Large screen app quality](https://developer.android.com/docs/quality-guidelines/large-screen-app-quality) — the checklist §4 targets
- [Window size classes](https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes)

**Performance**
- [Baseline profiles](https://developer.android.com/topic/performance/baselineprofiles/overview)
- [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)
- [R8 / shrinking](https://developer.android.com/build/shrink-code)
- [Compose performance](https://developer.android.com/develop/ui/compose/performance)

**Build & AGP 9**
- [JetBrains AGP 9 migration skill](https://github.com/Kotlin/kotlin-agent-skills/tree/main/skills/kotlin-tooling-agp9-migration) — the source for §3.8's version and plugin tables. `VERSION-MATRIX.md` and `PLUGIN-COMPATIBILITY.md` are the two files to read; the migration procedure itself is KMP-scoped and doesn't apply to this repo yet (see §3.8's scope caveat)
- [AGP release notes & upgrade guide](https://developer.android.com/build/releases/gradle-plugin)
- [Gradle 9 upgrade guide](https://docs.gradle.org/current/userguide/upgrading_version_8.html)
- [Now in Android's `build-logic`](https://github.com/android/nowinandroid/tree/main/build-logic) — the reference for convention-plugin structure this repo already follows

**Other**
- [Edge to edge](https://developer.android.com/develop/ui/compose/layouts/insets)
- [Material 3 Expressive](https://m3.material.io/) and [Compose Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg)
- [Roborazzi](https://github.com/takahirom/roborazzi)
- [Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility)

---

## Changelog

| Date | Change |
| --- | --- |
| 2026-08-13 | Initial plan. Audit of `main` @ `7a8317c`. |
| 2026-08-13 | targetSdk target raised to 37 (B4, §3.1). Time estimates removed throughout — the plan commits to ordering, not dates. §3.5 rewritten as an explicit chai/Material 3 recommendation with the evidence behind it. §3.8 added: AGP 9 migration, grounded in the JetBrains AGP 9 migration skill's version and plugin-compatibility tables. |
| 2026-08-13 | Reviewed the plan against the codebase and re-audited the codebase for gaps. Corrections and additions: **B11** added — `java.time` at minSdk 24 with desugaring never enabled, a crash on a supported API level and now the highest-priority item. **B1 corrected** — the broken filter is rooms, not topics; topics is unreachable dead code (§1.4). **B12–B16** added. **B9 corrected** — `Clock` is already provided and injected; the proposed `TimeModule` was a duplicate. **§3.5** gained a hard material3 1.4.x prerequisite: BOM 2025.06.00 resolves material3 to 1.3.2, which contains no Expressive API. **§6.12** gained the `RemoteFeatureToggle` typed accessors the AI kill switches depend on and that did not exist. Fixed wrong column name in the migration test and two `sessionImageUrl` compile errors. CI matrix extended down to API 24. **§16.0** added: the P0 list. AGP 9 + targetSdk 37 both committed, which pins the AGP to the earliest 9.x supporting API 37. |





