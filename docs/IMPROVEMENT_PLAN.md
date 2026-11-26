# droidconKE Android — Improvement Plan

> **Status:** Living roadmap — pending Android-only work (no backend blockers) · **Last audited:** 2026-11-24
> **Repo:** `droidconKeKotlin`
> **Scope:** High-ROI, self-contained Android work verified against the codebase — calendar export, two-pane & screen fixes, internal deep links, Remote Config year-proofing, offline Coil caching, widget battery hygiene, staged Play rollout, and core test gaps.
>
> Finished work is deleted from here; lasting architecture, performance, and workflow facts belong in [`docs/architecture.md`](architecture.md), [`docs/performance.md`](performance.md), or [`AGENTS.md`](../AGENTS.md).

---

## Next up — §11.8, then §3.6 / §9.5 / §11.7

1. **Internal deep links** (§11.8) — extend `DeepLinks` to cover `droidconke://ticket`, `droidconke://sessions`, and `droidconke://speaker/{name}` so launcher shortcuts, widgets, and calendar events share one URI router.
2. **Production hygiene quick wins** (§3.6, §9.5, §11.7, §13.2, §15.4) — Remote Config `event_slug`, Coil offline disk cache, conference-day-only widget refresh, staged Play rollout + native symbols, and Crashlytics custom keys.
3. **Testing safety nets** (§3.9, §5.6, §10) — B7 splash test, automated Compose accessibility checks, edge-case screenshot fixtures, and core `:app` E2E journeys.

---

## 1. Open architecture findings

**A5 — Domain models still carry legacy transport strings.** `Session` carries `startInstant` and `endInstant`, used by `core/domain`, while `startDateTime`, `endDateTime`, `startTime`, and `endTime` remain and are still parsed in `core:ui`'s `SessionMapper.kt`. Migrate presentation mapping to `Instant`, replace the `System.currentTimeMillis()` default in `DateAndTimeUtils.getTimeDifference` with an injected `Clock`, and drop the string fields.

**A6 — No `:app` end-to-end tests.** `pr.yml` runs `:core:data` on Gradle Managed Devices (`api30`, `api34`), but `:app` has no user-journey instrumentation tests yet (§10.3).

---

## 3. Phase 0 — Foundations

### 3.6 One year, one name

`EVENT_SLUG = "droidconke-2026-731"` and `ORG_SLUG = "droidcon-ke-645"` are still compile-time constants in `core/network/.../remote/Constants.kt`. Move both to Remote Config with the current year as the XML default and compose URLs at call time in `UrlProvider` so a new conference year is a config edit rather than an emergency release.

`dcke22-database` and `dcke22-pref` keep their names — renaming either is a migration that risks users' bookmarks for cosmetic gain.

### 3.9 Phase 0 — remaining regression test

| Finding | What the test must assert |
| --- | --- |
| B7 — splash waited on network | `MainViewModel.isInitialising` releases on cached Room data (or timeout) and never waits on `remoteFeatureToggle.syncNowIfEmpty()` (`MainViewModelTest.kt`) |

---

## 5. Phase 2 — Design system & automated accessibility

### 5.2 Loading consolidation & Material 3 pin

- **Consolidate loading skeletons.** Nine separate loading/shimmer files exist (`LoadingBox`, `AnimatedShimmerEffect`, and seven feature-specific loading components). Consolidate to one shared skeleton primitive for content-shaped placeholders and `LoadingIndicator` for indeterminate loads.
- **Drop the `material3` pin** (`1.5.0-rc01` in `gradle/libs.versions.toml`) once a Compose BOM manages stable `1.5.0`.

### 5.5 Optional — Offline session notes

Only build if desired for attendees who prefer in-app talk notes over Keep/Notes. Keep it local-first inside `:feature:sessions` + `:core:database` (no separate `:feature:notes` module, no sign-in): Room `session_notes` table with `CASCADE` delete, debounced autosave (`800ms`), optional elapsed-time stamp during live talks, and plain-text/Markdown share export.

### 5.6 Automated accessibility gate

Enable Compose UI's built-in accessibility checks (`composeTestRule.enableAccessibilityChecks()` + `onRoot().tryPerformAccessibilityChecks()`) in unit/UI tests across top-level screens so touch-target, label, and contrast regressions fail automatically in CI.

---

## 6A. Updates — local sync polish

1. **New speakers on the timeline.** Record a "new speaker" moment during speakers sync the same way sessions sync records new talks.
2. **Unread indicator.** Show a subtle badge dot on the Updates nav item when rows exist newer than the last visit timestamp in DataStore.

---

## 9. Phase 6 — Performance, R8, and app size

Current numbers and measurements live in [`docs/performance.md`](performance.md). CI fails any PR that grows download size by more than 1%.

### 9.3 R8 configuration

- Move release to AGP's `optimization {}` block once `androidx.baselineprofile` supports it cleanly.
- Evaluate `android.r8.strictFullModeForKeepRules=true` so stale keep rules fail loudly instead of rotting silently.

### 9.5 Runtime performance & offline caching

- **Configure Coil caching (`SingletonImageLoader`).** `DroidconApp` currently uses default Coil settings. Configure a 25% memory cache, a 50 MB disk cache (`cacheDir/image_cache`), and crossfade so speaker avatars and session banners render instantaneously offline after the first sync. Consider migrating Coil 2.7 → Coil 3 at the same time.
- **Remove or use Firebase Performance Monitoring.** `firebase-perf` currently costs ~30 ms on the main thread at cold start (`docs/performance.md#still-open`) with zero custom traces. Remove it unless the Firebase Perf dashboard is actively monitored.

---

## 10. Phase 7 — Testing

### 10.2 Screenshot edge cases

- Add edge-case fixtures to `FakeSessions` (long titles, 8 speakers, missing images, empty descriptions) and capture them in Roborazzi — edge cases are what break real conference layouts.

### 10.3 Core `:app` E2E journeys

Add Hilt instrumentation tests on the existing Gradle Managed Devices (`api30`, `api34`) for the five journeys that protect core attendee trust:

| # | Journey | Why |
| --- | --- | --- |
| 1 | Star session → My Sessions → unstar | Core value of the app |
| 2 | Starred session survives Activity recreation | Prevents agenda loss |
| 3 | Offline launch: cached sessions + saved ticket visible | Real conference Wi-Fi |
| 4 | Filter by level, room, and session type | Protects schedule filtering |
| 5 | Deep link from cold start (`session`, `feed`, `ticket`) | Keeps notifications & shortcuts reliable |

### 10.4 Test infrastructure

- Promote shared fakes (`SampleData`, `FakeSessionsRepo`, `MainDispatcherRule`, `TestClock`) into `:core:testing` whenever a second module needs them.
- Remove the `System.currentTimeMillis()` default in `DateAndTimeUtils.getTimeDifference` so time-boundary tests are deterministic.

---

## 11. Phase 8 — High-value standalone features

### 11.7 Widget battery hygiene

`widget/src/main/res/xml/next_session_widget_info.xml` sets `android:updatePeriodMillis="1800000"`, waking devices every 30 minutes all year. Set `updatePeriodMillis="0"` and refresh only after syncs and during conference days.

### 11.8 Internal deep links

`DeepLinks.kt` and `AndroidManifest.xml` currently route `droidconke://session/{id}` and `droidconke://feed`. Extend `DeepLinks` to cover `droidconke://ticket` (replacing custom `ACTION_SHOW_TICKET`), `droidconke://sessions`, and `droidconke://speaker/{name}` so launcher shortcuts, widgets, and calendar events share one URI router.

---

## 12. Phase 9 — 3D cube hero performance (optional)

- **Fix 60 Hz RenderThread jank on mid-range hardware.** Pre-render each face's 9 stickers into a bitmap and draw visible stickers as textured quads (~27–45 GPU draws vs ~130 anti-aliased path fills/frame on OPPO Reno4). Verify on physical hardware with `NavigationBenchmark`, never emulator software rendering.

---

## 13. Phase 10 — Play Store release safety

### 13.2 Staged rollout & native debug symbols

`deploy-to-playstore.yml` currently publishes immediately to 100% of production (`status: completed`) without native debug symbols. Update to:
- `status: inProgress` with `userFraction: 0.1` (10% staged rollout, promote manually after monitoring Crashlytics)
- `debugSymbols: app/build/intermediates/merged_native_libs/release/out/lib`

---

## 15. Observability

### 15.4 Crashlytics custom keys

Set lightweight state keys directly in `:app` (`window_size_class`, `is_signed_in`, `starred_session_count`, `last_sync_ago_minutes`) without adding a new module so crash reports carry layout and sync context.

---

## 16. Roadmap and sequencing

### 16.1 Priority order — what to do next

| # | Work item | Section | Why here |
| --- | --- | --- | --- |
| **1** | Internal deep links (`ticket`, `sessions`, `speaker`) | §11.8 | Unifies URI routing across launcher shortcuts, widgets, and calendar events |
| **2** | Production hygiene batch (Remote Config `event_slug`, Coil 50 MB offline cache, widget `updatePeriodMillis="0"`, staged Play rollout + `debugSymbols`, Crashlytics custom keys, Firebase Perf removal decision) | §3.6, §9.5, §11.7, §13.2, §15.4 | High reliability, battery, offline UX, and release safety in small clean PRs |
| **3** | Testing safety nets (B7 splash test, automated Compose accessibility checks, edge-case screenshot fixtures, core `:app` E2E journeys) | §3.9, §5.6, §10 | Prevents regressions before conference day |
| **4** | Tech debt & local polish (A5 `Session` `Instant` cleanup, loading skeleton consolidation, Updates unread dot + new speakers, optional offline session notes, optional Reno4 cube 60 Hz fix) | §1.5, §5.2, §5.5, §6A, §12 | Clean codebase & nice-to-have attendee polish |

### 16.4 Definition of done, per PR

1. Compiles, and `./gradlew spotlessCheck ktlintCheck detekt lint stabilityCheck` passes
2. Tests added that **fail without the change**
3. Screenshot goldens updated if UI changed
4. No hardcoded strings, colours, or dimensions
5. Verified in dark mode, at 200% font scale, and at foldable/tablet widths
6. No new dependency without a one-line justification in the PR
7. Public APIs have KDoc explaining *why*, not *what*
