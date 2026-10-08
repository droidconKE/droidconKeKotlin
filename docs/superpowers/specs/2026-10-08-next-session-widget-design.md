# Next-Session Glance Widget — Design Spec

Date: 2026-10-08
Status: Approved for planning
Source idea: `docs/IMPROVEMENT_PLAN.md` §11.7 "Glance widget: what's on now"

## Purpose

Add a home-screen widget showing the conference attendee's current or
next session at a glance, so they don't have to open the app to know
where to be. This is also a learning vehicle for Jetpack Glance and for
how Hilt reaches non-`Activity` Android components (`BroadcastReceiver`)
in a multi-module project — the build is deliberately explained
end-to-end rather than dropped in as a finished diff.

## Scope

In scope:
- One widget kind ("next session"), three responsive sizes.
- Hilt wiring via `@AndroidEntryPoint` on the widget's receiver.
- Refresh hooked into the existing sync worker.
- A Robolectric/composition test for the widget's content states.

Out of scope (explicitly deferred, not forgotten):
- "Only poll every 15 minutes during conference days" smart scheduling —
  requires a conference-dates concept that doesn't exist anywhere in the
  domain model yet. Revisit if the sync-worker-triggered refresh proves
  too stale during a live event.
- Wear OS tile.
- Custom Glance theming/color providers — v1 uses default
  `GlanceTheme.colors` (dynamic/system colors).
- A second widget kind (e.g. full schedule) — the module and DI pattern
  here are written so that's additive later, not precluded, but nothing
  about this spec builds toward it speculatively.

## Why this diverges from the improvement plan's snippet

`IMPROVEMENT_PLAN.md`'s §11.7 code sample uses
`EntryPointAccessors.fromApplication<WidgetEntryPoint>(context)` inside
`provideGlance`. This repo doesn't use that pattern anywhere — every
other non-`Activity` Hilt consumer (`SyncDataWorker` via `@HiltWorker`)
uses Hilt's native support for that component type. `GlanceAppWidgetReceiver`
is a `BroadcastReceiver`, and Hilt supports `BroadcastReceiver` injection
natively via `@AndroidEntryPoint`. This spec uses that instead — it's more
idiomatic to the existing codebase and a better worked example of
"how Hilt reaches a non-Activity component."

The plan's snippet also references `MainActivity` by a placeholder
package; the real class is `com.android254.presentation.activity.MainActivity`.

## Architecture

### New module: `:widget`

A new top-level Gradle module (sibling to `:app`, `:benchmarks`), not
nested under `:core` or `:feature` — matching the module list already
named in the improvement plan.

- `widget/build.gradle.kts`
  - namespace `ke.droidcon.kotlin.widget`
  - plugins: `droidconke.quality`, `droidconke.android.library`,
    `droidconke.android.hilt`, `org.jetbrains.kotlin.plugin.compose`
    (Glance's `@Composable` requires the Compose compiler plugin even
    though it is not Jetpack Compose UI)
  - dependencies: `implementation(projects.core.domain)` (for
    `SessionsRepo` / `Session`), new version-catalog entries for
    `androidx.glance:glance-appwidget` and `androidx.glance:glance-material3`
- `settings.gradle.kts`: add `include(":widget")`
- `app/build.gradle.kts`: add `implementation(projects.widget)`

### Manifest & widget metadata

- `widget/src/main/AndroidManifest.xml` declares the `<receiver>` with
  an `ACTION_APPWIDGET_UPDATE` intent-filter and `meta-data` pointing at
  the provider-info XML below. This merges into the app's final manifest
  at build time — no changes needed to `app/src/main/AndroidManifest.xml`.
- `widget/src/main/res/xml/next_session_widget_info.xml`: the
  `AppWidgetProviderInfo` — min size, `resizeMode`,
  `android:widgetCategory="home_screen"`, `updatePeriodMillis="0"` (we
  drive updates ourselves rather than relying on the OS's coarse native
  periodic mechanism), and an `initialLayout` placeholder (Glance still
  requires one static layout for the instant before first composition).

### DI wiring

```kotlin
@AndroidEntryPoint
class NextSessionWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject lateinit var sessionsRepo: SessionsRepo
    override val glanceAppWidget: GlanceAppWidget
        get() = NextSessionWidget(sessionsRepo)
}

class NextSessionWidget(
    private val sessionsRepo: SessionsRepo,
) : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(SmallWidget, MediumWidget, LargeWidget),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val now = Clock.System.now().toEpochMilliseconds()
            val current by sessionsRepo.fetchCurrentSessions(now).collectAsState(emptyList())
            val next by sessionsRepo.fetchUpNextSessions(now).collectAsState(emptyList())

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
        val SmallWidget = DpSize(140.dp, 100.dp)
        val MediumWidget = DpSize(250.dp, 100.dp)
        val LargeWidget = DpSize(250.dp, 200.dp)
    }
}
```

Hilt re-runs field injection on every `onReceive` call (the receiver
instance is ephemeral per the OS's widget-broadcast model), so
`sessionsRepo` is never stale despite the receiver having no persistent
lifecycle of its own. `SessionsRepo` already exposes
`fetchCurrentSessions(currentTime)` and `fetchUpNextSessions(currentTime)`
(`core/domain/.../SessionsRepo.kt`) — no domain-layer changes needed.

### UI content

`WidgetContent` composable, three states:
- **Current session running** — "Happening now" label, title, room.
- **No current session, one upcoming** — "Up next" label, title, time + room.
- **Nothing scheduled** — empty-state string.

Tap target on the whole widget → `actionStartActivity<MainActivity>()`
pointed at `com.android254.presentation.activity.MainActivity`.

Strings live in `widget/src/main/res/values/strings.xml`
(`widget_happening_now`, `widget_up_next`, `widget_no_sessions`),
following the same per-module strings convention as `core:ui`/`core:data`.

Colors: default `GlanceTheme.colors` for v1 — no custom `ColorProviders`.

### Refresh trigger

In `core/data/.../SyncDataWorker.doWork()`, after `syncedSuccessfully`
is confirmed `true`, call `NextSessionWidget(...).updateAll(appContext)`
(or the Glance-generated equivalent) before returning `Result.success()`.
This rides the sync worker's existing periodic schedule
(`syncDataWorkManager.setupPeriodicSync()` in `DroidconApp`) — no new
WorkManager job, no new battery cost, no "conference days" concept
needed for v1.

Note: `:core:data` does not currently depend on `:widget`, and adding
that dependency edge needs checking against the module graph during
implementation (data modules are typically lower in the dependency
chain than feature-ish modules). If a direct dependency would invert
the graph, the fallback is broadcasting a local intent/signal that
`:widget` listens for instead of `:core:data` depending on `:widget`
directly — this choice is left to the implementation plan to resolve
once the real dependency graph is in front of us.

## Error handling / edge states

- No sessions at all (e.g. before the conference, or sync never ran):
  empty-state string, no crash.
- `SessionsRepo` flow emission failure: Glance's `collectAsState` simply
  keeps the last good value; no special handling needed beyond what the
  repo already guarantees.
- Widget added before first sync completes: shows empty state until the
  next successful sync calls `updateAll`.

## Testing

- A Robolectric/composition test for `WidgetContent` covering the three
  states (current / next / empty), using the existing `core:testing`
  fakes for `SessionsRepo` — following the same test-fake pattern already
  used by feature modules.
- No attempt to test the receiver or AppWidget-host integration itself
  (requires an emulator/device and isn't worth automating for v1).
- Manual verification (see Acceptance below) substitutes for host-level
  integration testing.

## Acceptance / manual verification

1. `androidx.glance:glance-appwidget-preview` + `@Preview` used during
   development for fast iteration on the three breakpoints and three
   states — approximate, not the real `RemoteViews` rendering path.
2. Ground truth: pin the widget on an emulator/device, drag-resize
   through small/medium/large, confirm against the real host rendering
   (this is where OEM launcher quirks — padding, corner radius, scrims —
   would surface, not in the Studio preview).
3. Confirm tapping the widget opens `MainActivity`.
4. Confirm the widget updates after a sync completes (trigger a manual
   sync and observe the widget content change without reopening the app).

## Open question carried into the implementation plan

Whether `:core:data` can depend on `:widget` directly for the
`updateAll()` call, or whether that violates the module dependency
graph and needs the local-broadcast fallback described above. Resolve
by inspecting the actual module graph (e.g.
`./gradlew :core:data:dependencies` or reading existing
`build.gradle.kts` dependency directions) during planning, before
writing the refresh-trigger step.
