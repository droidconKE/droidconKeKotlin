# Next-Session Glance Widget Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a home-screen Glance widget that shows the current or next conference session, refreshed by the existing sync worker.

**Architecture:** A new top-level `:widget` Gradle module holds a `GlanceAppWidget` (`NextSessionWidget`) and its `GlanceAppWidgetReceiver` (`NextSessionWidgetReceiver`, `@AndroidEntryPoint`), reading `SessionsRepo` from `:core:domain` via constructor injection. A new `WidgetRefresher` interface in `:core:domain`, implemented and Hilt-bound inside `:widget`, lets `SyncDataWorker` (in `:core:data`) trigger a refresh without `:core:data` depending on `:widget` directly — the same interface-inversion shape this codebase already uses for `Syncable`/`Synchronizer`.

**Tech Stack:** Kotlin, Jetpack Glance (`androidx.glance:glance-appwidget` / `glance-material3`, `1.2.0`), Hilt, Gradle convention plugins (`droidconke.android.library`, `droidconke.android.hilt`), Robolectric + `glance-testing`/`glance-appwidget-testing` for unit tests.

**Spec:** `docs/superpowers/specs/2026-10-08-next-session-widget-design.md`

## Global Constraints

- `minSdk = 26`, `compileSdk`/`targetSdk = 37` (from `gradle/libs.versions.toml`) — no version-gating needed for Glance.
- Glance artifact versions are pinned at `1.2.0` across `glance-appwidget`, `glance-material3`, `glance-testing`, `glance-appwidget-testing` (confirmed latest stable against Google's Maven metadata; do not use `1.3.0-alpha02`).
- `:widget` must NOT apply the `droidconke.android.library.compose` convention plugin — it pulls the full Jetpack Compose UI/Material3 bundle, which Glance does not use (Glance only needs the Compose runtime/graphics/unit layers). Apply `org.jetbrains.kotlin.plugin.compose` (`libs.plugins.compose.compiler`) directly and set `buildFeatures.compose = true` in the module's own `android {}` block instead.
- `:widget` must not depend on `:app` (circular — `:app` depends on `:widget`). The widget's "open app" action resolves the launcher intent at runtime via `packageManager.getLaunchIntentForPackage(context.packageName)`, never by referencing `MainActivity` as a class.
- `:core:data` must not gain a dependency on `:widget` (inverts existing layering — `:core:data` only depends on `:core:*` modules today). The refresh hook goes through the new `WidgetRefresher` interface in `:core:domain` instead.
- Package/namespace convention for this module: `ke.droidcon.kotlin.widget`, sources under `widget/src/main/java/...` (this repo uses a `java/` source dir for Kotlin files throughout, not `kotlin/`).
- `WidgetRefresher` package/location: `com.android254.domain.widget`, mirroring the existing `com.android254.domain.sync.Syncable`/`Synchronizer` pair.

## Review Focus

- **Widget pinned before first sync completes.** `SessionsRepo.fetchCurrentSessions`/`fetchUpNextSessions` return empty lists, not an error — `WidgetContent` must render the empty-state string, not crash or show a blank box. Covered in Task 2's empty-state test.
- **A session is current AND another is "up next" at the same moment.** `WidgetContent`'s `when` must prefer "happening now" and never show both labels at once. Covered in Task 2's current-session test: it passes both a non-null `current` and a non-null `next`, and asserts `statusLabel`'s text is exactly "Happening now" — `onNode` requires exactly one match, so the assertion fails outright if both branches rendered their own `statusLabel`-tagged text.
- **Device has no default launcher resolvable for the package (edge case on some test/CI environments).** `getLaunchIntentForPackage` can return `null`; `NextSessionWidget.provideGlance` must not force-unwrap it into a crash. Covered in Task 2 by typing the action parameter as nullable and only attaching `.clickable` when non-null.
- **Widget added, then sync fails repeatedly (`Result.retry()` path in `SyncDataWorker`).** `WidgetRefresher.refresh()` must only be called on the `syncedSuccessfully == true` branch, never on retry, so the widget doesn't appear to refresh (and reset any Glance-side error state) on a failed sync. Covered in Task 4's manual verification step (trigger a sync with network off, confirm widget content is untouched).
- **Smallest breakpoint (140dp x 100dp) is too narrow for a two-line title plus room text.** `WidgetContent` must cap title to `maxLines = 2` (per spec) and the room/time line must not wrap unbounded and push content off the small widget. Covered in Task 2's small-size test asserting both text nodes are present and in Task 5's on-device resize check.

---

## Task 1: `:widget` module skeleton, wired into the build, shows static content

**Files:**
- Modify: `gradle/libs.versions.toml` — add a `glance = "1.2.0"` entry to `[versions]`, and to `[libraries]`: `glance-appwidget = { module = "androidx.glance:glance-appwidget", version.ref = "glance" }`, `glance-material3 = { module = "androidx.glance:glance-material3", version.ref = "glance" }`, `glance-testing = { module = "androidx.glance:glance-testing", version.ref = "glance" }`, `glance-appwidget-testing = { module = "androidx.glance:glance-appwidget-testing", version.ref = "glance" }`.
- Modify: `settings.gradle.kts` — add `include(":widget")` after the existing `include(...)` lines.
- Modify: `app/build.gradle.kts` — add `implementation(projects.widget)` in the `dependencies {}` block, alongside the other `implementation(projects.feature.*)` lines.
- Create: `widget/build.gradle.kts`
- Create: `widget/src/main/AndroidManifest.xml`
- Create: `widget/src/main/res/xml/next_session_widget_info.xml`
- Create: `widget/src/main/res/values/strings.xml`
- Create: `widget/src/main/java/ke/droidcon/kotlin/widget/NextSessionWidget.kt`
- Create: `widget/src/main/java/ke/droidcon/kotlin/widget/NextSessionWidgetReceiver.kt`

**Interfaces:**
- Produces: `NextSessionWidget` (a `GlanceAppWidget` subclass, no-arg constructor at this stage), `NextSessionWidgetReceiver` (a `GlanceAppWidgetReceiver` subclass). Later tasks replace the no-arg constructor with `SessionsRepo` injection — this task's shape is intentionally temporary scaffolding, not a frozen interface.

- [ ] **Step 1: Add the version-catalog entries listed above to `gradle/libs.versions.toml`.**

- [ ] **Step 2: Create `widget/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.droidconke.quality)
    alias(libs.plugins.droidconke.android.library)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "ke.droidcon.kotlin.widget"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
}
```

(Catalog aliases confirmed against `gradle/libs.versions.toml`'s `[plugins]` section: `droidconke-quality` and `droidconke-android-library`, which Gradle's type-safe accessors expose as `libs.plugins.droidconke.quality` / `libs.plugins.droidconke.android.library` above.)

- [ ] **Step 3: Add `include(":widget")` to `settings.gradle.kts`.**

- [ ] **Step 4: Add `implementation(projects.widget)` to `app/build.gradle.kts`'s `dependencies {}` block.**

- [ ] **Step 5: Create `widget/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <receiver
            android:name=".NextSessionWidgetReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/next_session_widget_info" />
        </receiver>
    </application>
</manifest>
```

- [ ] **Step 6: Create `widget/src/main/res/xml/next_session_widget_info.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="140dp"
    android:minHeight="100dp"
    android:maxResizeWidth="250dp"
    android:maxResizeHeight="200dp"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:updatePeriodMillis="0"
    android:initialLayout="@layout/glance_default_loading_layout">
</appwidget-provider>
```

- [ ] **Step 7: Create `widget/src/main/res/values/strings.xml`** with a single placeholder string `widget_placeholder_text` = `"Next session widget"`.

- [ ] **Step 8: Create `NextSessionWidget.kt` in `widget/src/main/java/ke/droidcon/kotlin/widget/`**

```kotlin
class NextSessionWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(SmallWidget, MediumWidget, LargeWidget),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Text(LocalContext.current.getString(R.string.widget_placeholder_text))
        }
    }

    private companion object {
        val SmallWidget = DpSize(140.dp, 100.dp)
        val MediumWidget = DpSize(250.dp, 100.dp)
        val LargeWidget = DpSize(250.dp, 200.dp)
    }
}
```

- [ ] **Step 9: Create `NextSessionWidgetReceiver.kt`**

```kotlin
class NextSessionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextSessionWidget()
}
```

- [ ] **Step 10: Build and verify**

Run: `./gradlew :widget:assembleDebug :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 11: Manual check** — install the debug build on an emulator, long-press the home screen, add the widget, confirm "Next session widget" text appears.

- [ ] **Step 12: Commit**

```bash
git add gradle/libs.versions.toml settings.gradle.kts app/build.gradle.kts widget/
git commit -m "feat(widget): scaffold :widget module with a static Glance widget"
```

---

## Task 2: `WidgetContent` composable — the three states, testable in isolation

**Files:**
- Modify: `widget/build.gradle.kts` — add `testImplementation(libs.glance.testing)`, `testImplementation(libs.glance.appwidget.testing)`, `testImplementation(libs.test.robolectric)` (confirmed against `gradle/libs.versions.toml`: catalog key `test-robolectric`, version `4.16.1`).
- Modify: `widget/src/main/res/values/strings.xml` — replace the Task 1 placeholder with `widget_happening_now` = `"Happening now"`, `widget_up_next` = `"Up next"`, `widget_no_sessions` = `"Nothing scheduled right now"`.
- Create: `widget/src/main/java/ke/droidcon/kotlin/widget/WidgetContent.kt`
- Create: `widget/src/test/java/ke/droidcon/kotlin/widget/WidgetContentTest.kt`
- Modify: `widget/src/main/java/ke/droidcon/kotlin/widget/NextSessionWidget.kt` — call `WidgetContent` instead of the placeholder `Text`; data is still hardcoded `null`/`null` here (Task 3 wires the real repo).

**Interfaces:**
- Consumes: `com.android254.domain.models.Session` (already available transitively — `:widget` will depend on `:core:domain` starting in Task 3; for this task's test, construct `Session` directly without needing that dependency yet is not possible since the type lives in `:core:model`/`:core:domain` — pull `implementation(projects.core.domain)` into `widget/build.gradle.kts` now, one task early, since `WidgetContent`'s signature needs the `Session` type immediately).
- Produces: `WidgetContent(current: Session?, next: Session?, size: DpSize, launchIntent: Intent?)` — a `@Composable` function. Later tasks (3+) call this from `NextSessionWidget.provideGlance`; the signature above is what they must match.

- [ ] **Step 1: Add `implementation(projects.core.domain)` to `widget/build.gradle.kts`'s `dependencies {}` block, and the three test dependencies listed above.**

- [ ] **Step 2: Update `widget/src/main/res/values/strings.xml`** with the three real strings listed above (remove the Task 1 placeholder).

- [ ] **Step 3: Write the failing tests in `WidgetContentTest.kt`**

```kotlin
@RunWith(RobolectricTestRunner::class)
class WidgetContentTest {

    private fun fakeSession(
        title: String = "Test Session",
        rooms: String = "Hall A",
        startTime: String = "10:00 AM",
    ) = Session(
        id = "1", endDateTime = "", endTime = "", isBookmarked = false,
        isKeynote = false, isServiceSession = false, sessionImage = null,
        startDateTime = "", startTime = startTime, rooms = rooms,
        speakers = emptyList(), remoteId = "1", description = "",
        sessionFormat = "", sessionLevel = "", slug = "", title = title,
        eventDay = "",
    )

    @Test
    fun widgetContent_currentSessionPresent_showsHappeningNowNotUpNext() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        provideComposable {
            WidgetContent(
                current = fakeSession(title = "Keynote"),
                next = fakeSession(title = "Should not show"),
                size = DpSize(140.dp, 100.dp),
                launchIntent = null,
            )
        }
        onNode(hasTestTag("statusLabel")).assertHasText("Happening now")
        onNode(hasTestTag("sessionTitle")).assertHasText("Keynote")
    }

    @Test
    fun widgetContent_onlyNextSession_showsUpNext() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        provideComposable {
            WidgetContent(
                current = null,
                next = fakeSession(title = "Workshop"),
                size = DpSize(250.dp, 100.dp),
                launchIntent = null,
            )
        }
        onNode(hasTestTag("statusLabel")).assertHasText("Up next")
        onNode(hasTestTag("sessionTitle")).assertHasText("Workshop")
    }

    @Test
    fun widgetContent_noSessions_showsEmptyState() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        provideComposable {
            WidgetContent(current = null, next = null, size = DpSize(140.dp, 100.dp), launchIntent = null)
        }
        onNode(hasTestTag("emptyState")).assertExists()
    }
}
```

- [ ] **Step 4: Run tests to verify they fail**

Run: `./gradlew :widget:testDebugUnitTest --tests "*.WidgetContentTest"`
Expected: FAIL — `WidgetContent` is not yet defined.

- [ ] **Step 5: Implement `WidgetContent` in `WidgetContent.kt`**

Three-branch `when` on `(current, next)`, matching the spec's states: current session running → `statusLabel` = "Happening now" (`GlanceTheme.colors.error`), `sessionTitle` = title (`maxLines = 2`), a room line; else if a next session exists → `statusLabel` = "Up next" (`GlanceTheme.colors.primary`), `sessionTitle`, a time+room line; else → `emptyState` text. Tag each `Text` with `GlanceModifier.semantics { testTag = "..." }` matching the test tags above. Wrap the whole column in `GlanceModifier.fillMaxSize().background(GlanceTheme.colors.widgetBackground).padding(12.dp)`, and append `.clickable(actionStartActivity(launchIntent))` only when `launchIntent != null` (per the Review Focus item on a null launch intent).

- [ ] **Step 6: Run tests to verify they pass**

Run: `./gradlew :widget:testDebugUnitTest --tests "*.WidgetContentTest"`
Expected: PASS, 3 tests green.

- [ ] **Step 7: Update `NextSessionWidget.provideGlance`** to call `WidgetContent(current = null, next = null, size = LocalSize.current, launchIntent = null)` inside `GlanceTheme { ... }`, replacing the Task 1 placeholder `Text`. (Still no real data — that's Task 3.)

- [ ] **Step 8: Rebuild and confirm the app still assembles**

Run: `./gradlew :widget:assembleDebug :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add widget/
git commit -m "feat(widget): add WidgetContent with current/next/empty states"
```

---

## Task 3: Wire real session data via Hilt

**Files:**
- Modify: `widget/build.gradle.kts` — add `alias(libs.plugins.droidconke.android.hilt)` to `plugins {}`.
- Modify: `widget/src/main/java/ke/droidcon/kotlin/widget/NextSessionWidget.kt` — take `sessionsRepo: SessionsRepo` as a constructor parameter; `provideGlance` collects `fetchCurrentSessions`/`fetchUpNextSessions` and resolves the launch intent.
- Modify: `widget/src/main/java/ke/droidcon/kotlin/widget/NextSessionWidgetReceiver.kt` — `@AndroidEntryPoint`, `@Inject lateinit var sessionsRepo: SessionsRepo`, `glanceAppWidget` getter constructs `NextSessionWidget(sessionsRepo)`.

**Interfaces:**
- Consumes: `SessionsRepo.fetchCurrentSessions(currentTime: Long): Flow<List<Session>>`, `SessionsRepo.fetchUpNextSessions(currentTime: Long): Flow<List<Session>>` (`core/domain/src/main/java/com/android254/domain/repos/SessionsRepo.kt` — already exists, unchanged). `WidgetContent(current, next, size, launchIntent)` from Task 2 — unchanged signature.
- Produces: `NextSessionWidget(sessionsRepo: SessionsRepo)` — Task 4 constructs this same way from `NextSessionWidgetRefresher`.

- [ ] **Step 1: Add the Hilt plugin alias to `widget/build.gradle.kts`'s `plugins {}`, and `implementation(libs.kotlin.coroutines.datetime)` to its `dependencies {}` (for `kotlinx.datetime.Clock`, used below — catalog module `org.jetbrains.kotlinx:kotlinx-datetime`).**

- [ ] **Step 2: Change `NextSessionWidget` to a primary constructor `class NextSessionWidget(private val sessionsRepo: SessionsRepo) : GlanceAppWidget()`.**

- [ ] **Step 3: Implement `provideGlance`**

```kotlin
override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
        val now = Clock.System.now().toEpochMilliseconds()
        val current by sessionsRepo.fetchCurrentSessions(now).collectAsState(emptyList())
        val next by sessionsRepo.fetchUpNextSessions(now).collectAsState(emptyList())
        val launchIntent = remember { context.packageManager.getLaunchIntentForPackage(context.packageName) }

        GlanceTheme {
            WidgetContent(
                current = current.firstOrNull(),
                next = next.firstOrNull(),
                size = LocalSize.current,
                launchIntent = launchIntent,
            )
        }
    }
}
```

- [ ] **Step 4: Rewrite `NextSessionWidgetReceiver`**

```kotlin
@AndroidEntryPoint
class NextSessionWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject lateinit var sessionsRepo: SessionsRepo
    override val glanceAppWidget: GlanceAppWidget
        get() = NextSessionWidget(sessionsRepo)
}
```

- [ ] **Step 5: Build**

Run: `./gradlew :widget:assembleDebug :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Manual check** — install on an emulator/device with seeded session data (or against whatever the app's current data source is), pin the widget, confirm it shows a real current/next/empty state matching the app's own session list for "now."

- [ ] **Step 7: Commit**

```bash
git add widget/
git commit -m "feat(widget): wire SessionsRepo via Hilt AndroidEntryPoint"
```

---

## Task 4: Refresh on sync

**Files:**
- Create: `core/domain/src/main/java/com/android254/domain/widget/WidgetRefresher.kt`
- Create: `widget/src/main/java/ke/droidcon/kotlin/widget/di/NextSessionWidgetRefresher.kt`
- Create: `widget/src/main/java/ke/droidcon/kotlin/widget/di/WidgetRefresherModule.kt`
- Modify: `core/data/src/main/java/com/android254/data/work/SyncDataWorker.kt`

**Interfaces:**
- Consumes: `NextSessionWidget(sessionsRepo: SessionsRepo)` from Task 3, `SessionsRepo` from `:core:domain`.
- Produces: `WidgetRefresher.refresh(): suspend () -> Unit` — the only thing `SyncDataWorker` needs to know about.

- [ ] **Step 1: Create `WidgetRefresher.kt` in `:core:domain`**

```kotlin
package com.android254.domain.widget

interface WidgetRefresher {
    suspend fun refresh()
}
```

- [ ] **Step 2: Create `NextSessionWidgetRefresher.kt` in `widget/.../di/`**

```kotlin
class NextSessionWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionsRepo: SessionsRepo,
) : WidgetRefresher {
    override suspend fun refresh() {
        NextSessionWidget(sessionsRepo).updateAll(context)
    }
}
```

- [ ] **Step 3: Create `WidgetRefresherModule.kt`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetRefresherModule {
    @Binds
    abstract fun bindWidgetRefresher(impl: NextSessionWidgetRefresher): WidgetRefresher
}
```

- [ ] **Step 4: Modify `SyncDataWorker`** — add `private val widgetRefresher: WidgetRefresher` to the `@AssistedInject constructor(...)` parameter list (import `com.android254.domain.widget.WidgetRefresher`), and in `doWork()`, after `if (syncedSuccessfully) { ... }`'s success branch is determined but before returning, call `widgetRefresher.refresh()` only on the success path:

```kotlin
if (syncedSuccessfully) {
    widgetRefresher.refresh()
    Result.success()
} else {
    Result.retry()
}
```

- [ ] **Step 5: Build the whole app**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL` — this is the step that proves Hilt resolved `WidgetRefresher` across the `:core:data`/`:widget` module boundary correctly.

- [ ] **Step 6: Manual check (Review Focus: retry path must not refresh)** — trigger a sync with the network disabled, confirm `Result.retry()` fires and the widget content does not change; then re-enable network, confirm a successful sync updates the widget without reopening the app.

- [ ] **Step 7: Commit**

```bash
git add core/domain/ widget/ core/data/
git commit -m "feat(widget): refresh widget from SyncDataWorker via WidgetRefresher"
```

---

## Task 5: On-device acceptance pass

**Files:** none (verification only).

- [ ] **Step 1: Studio preview pass** — add a temporary `@Preview`-annotated function using `androidx.glance.appwidget.preview` (dev-only; do not commit) to check `WidgetContent` across the three `DpSize` breakpoints and three states quickly, per the spec's Acceptance section.
- [ ] **Step 2: On-device resize check** — pin the widget on an emulator/device, drag-resize through small/medium/large, confirm layout holds (Review Focus: no text overflow at the 140x100 breakpoint).
- [ ] **Step 3: Tap-to-open check** — tap the widget, confirm the app's launcher activity opens.
- [ ] **Step 4: Sync-refresh check** — repeat Task 4 Step 6's manual check once more end-to-end as a final confirmation.
- [ ] **Step 5: Remove any temporary preview code added in Step 1 if it was left in a source file; confirm `git status` is clean before the final commit of this task (if Steps 1-4 required no code changes, this task produces no commit).**
