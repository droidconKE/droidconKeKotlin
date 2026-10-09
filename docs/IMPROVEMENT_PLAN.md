# droidconKE Android — Modernization & Product Plan

> **Status:** Living roadmap — pending work only · **Last pruned:** 2026-10-05
> **Repo:** `droidconKeKotlin`
> **Scope:** What is left to build — the design system, adaptive follow-ups, intelligent (AI) experiences, ticketing, notifications, performance, testing, new product surfaces, and store presence.
>
> Finished work is not recorded here. How the app works today is in [`docs/architecture.md`](architecture.md), [`docs/performance.md`](performance.md) and [`AGENTS.md`](../AGENTS.md). The history is in git.

---

## Next up — the feed 404 (§3.10 item 1), then §14

The sync follow-ups that needed no backend work have landed: duplicates are gone (schema 6),
each table syncs in one transaction, an empty sessions response keeps the cached schedule, and
`isSyncing` covers the daily job. Checking that on a device showed the feed endpoint answering
404 for the configured event, which fails every sync. That is §3.10 item 1.

The 2026 rebrand and Material 3 Expressive landed on 2026-10-05; `docs/architecture.md` ("Design
system") describes the result. §14 is next. It depends on nothing, and the role-by-role contrast
it starts from is in `docs/architecture.md`.

§14 can run alongside the rest by a separate owner (§16.1 #8). In ranked order the others are the
Compose stability config (§3.1), the size wins (§9.4), the phase-aware home and schedule
conflicts (§5.3), then the 3D cube (§12).

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

**Read §16 first.** It holds the ranked priority order and the stages. What is left of Phase 0 is small (§3); everything after it is independently shippable and independently cancellable.

### Version numbers

Dependency versions below reflect what was current when each section was written. Before landing any of them, check what is current:

```bash
./gradlew dependencyUpdates
```

---

## 1. Where the codebase stands today

The stack and module layout are in [`AGENTS.md`](../AGENTS.md), and how the pieces fit is in [`docs/architecture.md`](architecture.md). This section lists only the findings from the original audit that are still open. Every correctness bug it found (B1–B16) is fixed or withdrawn; the fixes that still lack a regression test are in §3.9.

### 1.5 Findings — architecture

**A5 — Domain models carry transport types.** `Session.startDateTime: String`, `endDateTime: String`, `startTime: String`, `endTime: String` — four string fields where two `Instant`s belong. Parsing is spread across the data-layer `SessionMapper`, `SessionsManager`, the presentation `SessionMapper` in `core:ui`, `DateAndTimeUtils` and `SessionsViewModel`. Timezone handling is implicit.

**A6 — No end-to-end tests.** The only instrumented test is `SessionMapperInstrumentedTest`. Nothing launches the app and walks a user journey. (§10.3)

**A8 — Notifications are receive-only.** `MessagingService` handles FCM; `DroidconNotificationManager` posts. There are no local session reminders, no notification channels per category, no user preference surface, and the permission request fires unconditionally on first launch with no rationale (`MainActivity.askNotificationPermission` logs a rationale to Timber instead of showing one). (§8)

### 1.6 Findings — build & developer experience

**D5 — CI and release gaps.** CI runs on every PR, including Android Lint and screenshot verification. Still missing: a dependency-review job and an APK-size diff, which needs a size-diff action (§15.1). The release workflow also pushes straight to the production track with `status: completed` — no internal track, no staged rollout, no gate. (§13.2)

### 1.7 Findings — security & hygiene

**S3 — `app/google-services.json` is tracked.** Standard practice and not a secret (it contains public client identifiers), but it means **anyone can point their own build at the production Firebase project**. Once §6 lands, that Firebase project will be paying for Gemini API calls. Firebase **App Check** becomes mandatory before any AI feature ships. (§6.5, §6.12)

---

## 2. Modules still to come

The module split is done; [`docs/architecture.md`](architecture.md#modules) has the layout. These modules are planned and do not exist yet. Each arrives with the phase that needs it — an empty shell created ahead of the work is scaffolding nobody maintains.

| Module | For | Section |
| --- | --- | --- |
| `:core:ai` | The inference abstraction | §6.2 |
| `:core:analytics` | Analytics and Crashlytics behind one interface | §15.4 |
| `:feature:ticket` | Ticketing and QR | §7 |
| `:feature:notes` | Session notes | §5.5 |
| `:feature:assistant` | The conference assistant | §6.8 |
| `:feature:gamification` | Session check-in by photo | §6.9 |
| `:feature:jobboard` | Job board | §11.1 |
| `:feature:networking` | Connections | §11.2 |
| `:feature:challenge` | Code challenges | §11.3 |
| `:widget` | Glance "what's on now" widget | §11.7 |

`:core:datastore` is not planned. The preferences code is two files inside `:core:data`, and splitting them out would be symmetry for its own sake.

---

## 3. Phase 0 — Foundations

Most of Phase 0 has landed: the build configuration, the bug fixes (§3.3), edge-to-edge (§3.4), Credential Manager (§3.7) and AGP 9 (§3.8). Those subsections are deleted and their numbers are not reused. What is left is below.

### 3.1 Compose compiler configuration

The Compose compiler plugin's own extension is not configured. `AndroidCompose.kt` still drives
metrics through `freeCompilerArgs` strings, which is the pre-plugin way of doing it and gives no
stability configuration at all:

```kotlin
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
```

`compose_compiler_config.conf` at the repo root:

```
# Types the Compose compiler should treat as stable.
kotlinx.collections.immutable.ImmutableList
kotlinx.collections.immutable.ImmutableSet
kotlin.time.Instant
kotlinx.datetime.LocalDateTime
com.android254.domain.models.*
```

This is the cheapest available fix for the 20 `ComposeUnstableCollections` findings and a chunk
of the 47 non-skippable composables recorded in `docs/static-analysis.md` — the stability file
moves the needle without touching a call site. The committed `.stability` baselines make the
before/after measurable.

**Definition of done:** `stabilityDump` shows fewer non-skippable composables than the committed
baseline, and the diff is reviewed rather than rubber-stamped.

### 3.2 Version catalog additions for later phases

The entries the later phases assume. Add each one with the phase that needs it, not up front, and
check the version first with `./gradlew dependencyUpdates`.

```toml
[libraries]
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

[bundles]
camerax = ["camerax-core", "camerax-camera2", "camerax-lifecycle", "camerax-compose"]
```

### 3.5 chai and Material 3 — done

The token restructure landed on 2026-10-04 and the 2026 rebrand on 2026-10-05.
`docs/architecture.md` ("Design system") describes the result: Material roles first, three
`ChaiColors` tokens for the brand-blue hero panel, and a detekt rule against raw palette imports
outside `chai/colors`. One item moves to §14: a test that checks contrast for every role pair
the app draws.

### 3.6 One year, one name

The year names are gone from the project, class and theme names. Two things still tie the app to
one conference year.

**The event slug is a compiled constant.** `EVENT_SLUG = "droidconke-2025-898"` lives in
`core/network/.../remote/Constants.kt`. Move it into Remote Config, so a new conference year does
not need an app release:

```kotlin
// core/network/.../utils/RemoteConfigConfig.kt
val eventSlug: String get() = remoteConfig.getString(KEY_EVENT_SLUG)
val organizerSlug: String get() = remoteConfig.getString(KEY_ORG_SLUG)
```

```xml
<!-- core/network/src/main/res/xml/remote_config_defaults.xml -->
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

**The About copy names 2023.** `about_droidcon` in `core/ui`'s `strings.xml` says the conference
"will be held in Nairobi, Kenya on November 8th to 10th 2023". Rewrite it without a date, or fill
the dates from the same Remote Config values.

**Packages are still split** between `com.android254.*` and `ke.droidcon.kotlin.*`. Renaming them
is one mechanical PR that invalidates every open PR (§17.1). Do it in a quiet period, or decide
not to.

`dcke22-database` and `dcke22-pref` keep their names. Renaming either is a migration that costs
users their bookmarks, for a cosmetic gain.

### 3.9 Phase 0 — what is left

Every B finding from the original audit is fixed in code. Four of the fixes are not held by a
test, so nothing stops them coming back:

| Finding | What a test would assert |
| --- | --- |
| B5 — the theme threw outside an Activity | `ChaiTheme` renders with no Activity behind it. It no longer touches `LocalView`, so this is a small Robolectric test |
| B6 — the "My sessions" switch reset on rotation while the list stayed filtered | The switch survives recreation. Needs a `SavedStateHandle` / process-death test, which the suite has no harness for yet |
| B7 — the splash screen waited on a network call | The splash releases on cached data, never on Remote Config. Needs a startup test |
| B10 — mutable state and resource IDs in navigation keys | Every `NavKey` is immutable and holds no resource ID. A reflection test over the implementations would close it |

B6 and B10 protect user data, so do those first. B3 and B4 were build-configuration fixes; a test
would only assert the build script.

---

### 3.10 Sync follow-ups

Compared with `android/nowinandroid` (`a49ed25`, 2026-09-22), our sync now matches it where the
API allows: server-keyed tables, `@Upsert`, one transaction per table, and one expedited request
builder. What is left, in order:

1. **The feed endpoint returns 404, so every sync fails.** `GET /events/droidconke-2025-898/feeds`
   answers `404 {"message":"requested item not found"}` (seen 2026-10-09). The other four lists
   sync, but the worker treats one failure as the whole job failing and retries with backoff
   for ever. Ask the backend whether the event has a feed; until it does, treat a 404 from feed
   as an empty list rather than a failure.
2. **The sync notification channel is loud and badly named.** `DroidconApp` creates it with
   `IMPORTANCE_HIGH` and the raw id `sync_data` as its user-visible name. On API 30 and below an
   expedited sync posts a heads-up. Importance cannot be lowered for existing installs, so this
   needs a new channel id at `IMPORTANCE_LOW` with a string-resource name, and the old channel
   deleted.
3. **Push-triggered sync (needs backend).** NiA subscribes to an FCM `sync` topic and enqueues the
   worker when content changes. Ours syncs on start and every 24 hours, so a room change on
   conference day can take a day to arrive. A topic message that calls `startSync()` fixes that.
4. **Delta sync (needs backend).** NiA's `changeListSync` reads versioned change lists
   (`getTopicChangeList(after = version)`) and fetches only what changed. The droidcon API returns
   full lists and has no change-list endpoint, so this does not port. If the backend adds `ETag`
   or an `updated_since` parameter, adopt it then.
5. **A worker test.** NiA tests `SyncWorker` with WorkManager's `TestDriver`. `SyncDataWorker` has
   no test; the sync helper and `isSyncing` do.

## 4. Phase 1 — Adaptive follow-ups

Adaptive and large-screen support has landed. How it works — the navigation suite, the Navigation 3
scenes and their guards, the measured pane directive — is in
[`docs/architecture.md`](architecture.md#navigation). Three small items were left out of it.

### 4.4 List-detail: a selected state for the list pane

On a phone there is no persistent selection. With the detail beside the list there must be, and
neither the session card nor the speaker card shows which one is open:

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
            MaterialTheme.colorScheme.outlineVariant
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

Apply the same pattern to the speaker cards. **About**'s grids already add columns as the window
widens; it could also take a supporting pane for the organising team at expanded widths — through `SupportingPaneSceneStrategy` metadata, never a
`SupportingPaneScaffold`.

### 4.6 Keyboard and mouse: a context menu

Session and speaker cards take the hand cursor, a session card lifts on hover, and
`Modifier.clickable` already handles Enter and Space. Still missing: a right-click context menu on
a session card — bookmark, share, add to calendar.

---

## 5. Phase 2 — Design system 2.0 and world-class UX

**No remaining dependencies: §3.5 landed. Parallelisable across contributors.**

### 5.1 What "world class" actually means here

Not more animation. The conference apps people remember are the ones that answer, instantly and without being asked:

1. **Where do I need to be right now, and where next?**
2. **What did I star, and is any of it clashing?**
3. **Who is this speaker and what else are they doing?**
4. **Where's my ticket?**

Everything in this phase serves one of those four. Anything that doesn't is decoration, and decoration is what makes conference apps feel like brochures.

**Question 1 is answered (2026-10-05).** While a session is live, Home's hero shows it, its room
and end time, and what's next. With nothing live it welcomes, because up-next has no upper
bound and could be weeks away. On a phone the live-sessions rail is hidden on Home only when a
single session is live, the case the hero covers; parallel live sessions keep the rail. Questions 2 to 4 are
still open: conflicts (§5.3), speaker cross-links, and the ticket (§7).

### 5.2 Adopt M3 Expressive — mostly done

Landed on 2026-10-05, on material3 1.5.0-alpha29:
- the theme, Emphasized type and the Expressive corner scale;
- connected `ToggleButton` groups (`ButtonGroupDefaults` shapes) for the day selector and feedback rating, shared as `ConnectedToggleGroup`;
- the bookmark shape morph and cookie-shaped speaker avatars;
- `FilterChip` filters, the M3 switch and `LiveBadge`;
- the short navigation bar and wide navigation rail;
- the pull-to-refresh `LoadingIndicator`.
- a polish pass: one Share button on feed posts (the system share sheet), a plain sign-in
  dialog, sponsor and organiser logo tiles with grids that add columns on wider windows, and a
  filter bottom sheet.

Still open:
- **Loading consolidation.** The per-screen skeletons, `LoadingBox` and `AnimatedShimmerEffect`
  remain. Consolidate to one skeleton primitive for content-shaped loading, and use
  `LoadingIndicator` for full-screen and indeterminate loads.
- **Day-selector overflow.** The group fits three days and doesn't scroll. A longer event
  needs a scrolling row, or a move to `ButtonGroup` and its `overflowIndicator`.
- **The material3 pin.** Remove the `version.ref` once a BOM manages a stable 1.5.0 (see
  `AGENTS.md`).

### 5.3 The home screen, rethought

> **Status 2026-10-05:** the Now/Next card landed as home's hero. The phase model below (before,
> during and after the event) and `DetectScheduleConflictsUseCase` are still open.

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
| **After** | Personal recap (§6.11), session recordings, "what you missed", feedback prompt |

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

Surface it as a dismissible banner on the sessions screen and in the AI summary (§6.7): *"Heads up — 'Compose Multiplatform in Production' and 'Scaling Kotlin Backends' overlap by 35 minutes."*

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
- [ ] Zero hardcoded `Color` literals inside composables (lint rule to enforce)
- [ ] Zero hardcoded animation durations or springs in feature code — motion comes from `MaterialTheme.motionScheme`
- [ ] Loading states consolidated from 9 implementations to 2
- [ ] Shared element transitions on session and speaker cards
- [ ] Notes ship, offline, no auth
- [ ] Full TalkBack pass on every screen, recorded as a checklist in the PR
- [ ] Renders correctly at 200% font scale and in RTL (`ar` pseudo-locale)

---

## 6. Phase 3 — Intelligent experiences

**Depends on: Phase 0 · Highest product upside, highest risk**

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
// app/src/main/java/com/android254/droidcon/app/DroidconApp.kt
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
// core/domain/src/main/java/com/android254/domain/usecase/BuildDeterministicAgendaSummaryUseCase.kt

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

Offer the attendee's device language as the target rather than a fixed list — the audience is global, and ML Kit downloads each model on demand.

Wire it as a per-screen toggle, not a global setting — someone reading English session titles may still want a description in their own language.

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
// core/network/.../utils/RemoteFeatureToggle.kt

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

`RemoteFeatureToggleTest` already exists in `core/network/src/test`; extend it to cover the master-switch behaviour and the rollout hash stability.

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

**Depends on: Phase 0**

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
// app/src/main/java/com/android254/presentation/notifications/NotificationChannels.kt

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

The benchmark module, the baseline and startup profiles, an R8 health check and a −28 % APK pass
have landed. Numbers, method and the open questions are in [`docs/performance.md`](performance.md).
What is left is below.

### 9.3 R8 configuration

R8 is healthy: full mode, resource shrinking inside R8, and keep rules in
`app/src/main/keepRules/` (see [`docs/performance.md`](performance.md#r8)). Three things remain:

- **Move release to the `optimization {}` block** once `androidx.baselineprofile` understands it.
  Today the new block breaks baseline profile generation.
- **Evaluate `android.r8.strictFullModeForKeepRules=true`**, which makes a keep rule that names a
  missing class an error. Keep rules rot silently otherwise. Check first whether AGP 9.3 already
  defaults it.
- **Measure partial shrinking** (`android.r8.partialShrinking`, experimental), which skips library
  code. The win is build time, not APK size.

### 9.4 App size

The release APK is 4.62 MB after the 2026-09-11 pass
([`docs/performance.md`](performance.md#app-size)). Measure each change below against a fresh
number rather than assuming a win:

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
| 2026-09-03 | 1.0.0 (vc 1) | 5,205,416 B — 4.96 MiB | 8,553,954 B — 8.16 MiB | End of Phase 0. Includes §3.7 swapping `play-services-auth` for `androidx.credentials` + `googleid` |

That row predates the APK pass, so record a fresh one before the first change.

R8 already strips unused code, so deleting an unused dependency barely moves the number. The wins
left are the ones R8 cannot prove are unused. In order of return on effort:

**1. Drop `material-icons-extended`.** It's in the global `compose` bundle, so *every* Compose module pulls it. R8 does shrink unused icons, but the build-time cost is significant (thousands of generated classes to process) and the risk of an accidental full-keep is real.

The stronger argument, found during review: it's a **frozen artifact**. Google stopped publishing it, so it stays on 1.7.x while the rest of Compose moves on. Depending on a frozen artifact for the app's entire icon set is a slow-moving liability regardless of bytes.

**One blocker to clear first:** `SessionPresentationModel` uses `Icons.Default.CoPresent` and `Icons.Default.MicExternalOn`, both extended-only. Vector those two into `ChaiIcons` before removing the dependency, or the build breaks. (`Icons.Default.Build` and `Icons.Default.Coffee` are in `material-icons-core` and are fine.)

Replace with `material-icons-core` plus a curated set:

```kotlin
// core/designsystem/src/main/java/com/droidconke/chai/icons/ChaiIcons.kt

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

**2. Fonts.** Seven static Montserrat files ship, already subset to Latin. A single variable font, or downloadable fonts, replaces them:

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

**3. Remove `constraintlayout-compose`.** `app` and `feature:home` still use it. Check whether each
use is necessary, and measure the removal.

**4. Add a CI size gate,** so size regressions are caught in review rather than at release (§15.1).

### 9.5 Runtime performance

**Compose stability.** With Kotlin 2.x, strong skipping is on by default, but unstable parameters still break skipping. Run:

```bash
./gradlew assembleRelease -PenableComposeCompilerReports=true
```

Then read each module's `build/compose-reports/*-composables.txt` for `skippable=false` /
`restartable=false`. The cheapest fix for most offenders is the stability configuration file in
§3.1. Also hoist lambdas captured in lazy item content to stable references, so items skip.

**Coil configuration.** Currently default. Sessions and speakers screens load many remote images:

```kotlin
// app/src/main/java/com/android254/droidcon/app/DroidconApp.kt
class DroidconApp : Application(), SingletonImageLoader.Factory {
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

**Firebase Performance custom traces.** First settle the open question in
[`docs/performance.md`](performance.md#still-open): whether Performance Monitoring earns its ~30 ms
of main thread at startup at all. If it stays, trace the paths that matter:

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
- [ ] Compose stability report clean, or each remaining unstable type justified
- [ ] Coil memory and disk caches configured
- [ ] A decision on Firebase Performance Monitoring, with custom traces if it stays
- [ ] Each §9.4 size win measured against a recorded number
- [ ] Time to full display under 2 seconds on a low-end device — 2.7 s on the CS50C today, with the profile, and network-bound

---

## 10. Phase 7 — Testing

**Depends on: nothing.** What this phase needed from Phase 0 has landed.

### 10.1 Where the gaps are

Screenshot tests (Roborazzi: light, dark and 200 % font at phone size, plus four form factors),
Room migration tests and a shared `:core:testing` module are in place. The gaps:

- **No end-to-end tests.** The only instrumented test is `SessionMapperInstrumentedTest`.
- **No accessibility assertions.**
- **Coverage is measured but not gated.** Jacoco + Codecov are configured; nothing fails a PR.
- **Four fixed defects have no regression test** (§3.9).

### 10.2 Screenshot coverage gaps

The Roborazzi harness lives in `:core:screenshot`; [`AGENTS.md`](../AGENTS.md#commands) has the
record and verify commands. Two gaps:

- **The fake data is all happy path.** `FakeSessions` in `core/ui/.../common/fakedata/` has no edge
  cases. Add deliberate ones — 200-character titles, eight speakers, missing images, empty
  descriptions, non-Latin characters — and capture them. **Those are the cases that break layouts,
  so those are the ones worth screenshotting.**
- **Diffs are an artifact, not a comment.** CI uploads the Roborazzi output; posting the diff
  images to the PR would put them where reviewers look.

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
| 6 | Filter by level, room, session type, and combinations | Where the room-filter bug lived |
| 7 | Deep link from notification, cold start | Notifications are useless if this breaks |
| 8 | Sign in → sign out → data intact | Auth regressions eat local data |
| 9 | Rotation on every screen | State-loss regressions |
| 10 | Two-pane on tablet: select, rotate, back | New surface, new bugs |

### 10.4 Finish the test infrastructure

`:core:testing` exists but holds one fake, `FakeSyncWorkManager`. The rest are still private to
their modules: `SampleData` in `core/data`'s tests, and `MockTokenProvider` and
`SamplePaginationMetaData` in `core/network`'s. Move a fake when a second module needs it, and add
hand-written repository fakes as the features that need them arrive:

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

`Clock` is injected — `TimeModule` in `:core:common` provides it. One default argument still
reaches for the system clock: `getTimeDifference(nowMillis = System.currentTimeMillis())` in
`DateAndTimeUtils`. Pass the time in, and add a `TestClock` to `:core:testing`:

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
- [ ] Shared fakes in `:core:testing` once a second module needs them
- [ ] No `System.currentTimeMillis()` default in production code
- [ ] Edge-case fake data captured in screenshots
- [ ] 10 E2E journeys passing on an emulator in CI
- [ ] Regression tests for B5, B6, B7 and B10 (§3.9)
- [ ] Accessibility checks enabled in Compose tests
- [ ] Coverage gate enforced, with an agreed ratchet schedule

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
// app/.../common/navigation/DeepLinkResolver.kt

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

## 12. Phase 9 — 3D cube hero

**No dependencies. Decided 2026-10-05: draw it with Compose Canvas, not a 3D engine.**

### 12.1 What and why

A 3D Rubik's cube with faces carrying the conference identity, solving itself on screen. It has
no functional purpose. Its purpose is that developers screenshot it and post it, and it says
"this app was made by people who care."

### 12.2 What the website does, and why the app doesn't copy it

droidcon.co.ke's hero (`components/home/Banner.tsx` in droidconKE2022Web) is a Spline scene: 27
cubies, a choreographed solve authored to run once over 30 seconds, then replayed every 30
seconds in alternating directions. It is skipped for reduced-motion users and where WebGL is
missing, and its replay pauses off screen.

The app does not embed it:
- the scene is downloaded from `prod.spline.design` at runtime, so it needs a network
  connection, fails offline, and sends a request to a third party on every view;
- the scene file is opaque, so a pull request cannot review a change to it;
- a native 3D runtime costs megabytes per ABI, needs a fallback where GLES is missing, and very
  few contributors could maintain it.

### 12.3 The build: Compose Canvas

A cube is 27 axis-aligned cubelets. With back faces culled, at most three faces of each are
visible, so every frame is a few hundred projected quads, which `Canvas` draws easily on every
supported device. No dependencies.

- **Faces** use the brand palette: blue 700, neon green 500, ink, white, blue 300 and green 300.
  The centre cubelet of the neon face carries the "con" mark from the wordmark.
- **Motion** follows the website: a scripted 12-move solve in standard notation, each quarter
  turn on `MaterialTheme.motionScheme.defaultSpatialSpec()`, then a slow idle yaw. It replays
  every 30 seconds, alternating direction, as the website does.
- **Placement:** at the top of the About screen at every window size, and in the trailing half of
  Home's hero panel on medium and wider windows. Never on a phone's Home, which needs the space
  for now and next.
- **Structure:** a pure `CubeState` (positions and orientations) with `apply(move)`, kept apart
  from the renderer, so the maths is tested without a device.

```kotlin
// core/designsystem/.../chai/hero/CubeHero.kt
@Composable
fun CubeHero(modifier: Modifier = Modifier, moves: List<CubeMove> = SignatureSolve)

data class CubeState(val cubelets: List<Cubelet>) {
    fun apply(move: CubeMove): CubeState
}
```

**Non-negotiables:**
- Pause when off screen or when the app is not resumed (`LifecycleResumeEffect` plus visibility).
- Respect reduced motion: show the solved cube at a three-quarter angle, with no animation.
- `hideFromAccessibility()`, because it is decoration.
- Never on the startup path. About is never the start destination, and Home only draws it after
  first content.
- A Remote Config kill switch, `cube_hero_enabled`, in case it costs battery in the field.

**Tests:** unit tests on `CubeState` (any move applied four times is the identity; a scramble
followed by its inverse is solved), a Roborazzi golden of the static pose with the clock paused,
and a macrobenchmark on About showing p90 frame time under 16 ms.

## 13. Phase 10 — Play Store presence

**Mostly design and copy work. Screenshot automation depends on §10.2.**

### 13.1 Current state

`fastlane/` has an `Appfile`, a `Fastfile` with lint/test/build lanes, and `whatsnew/whatsnew-en-US`. There is **no `fastlane/metadata/android/` directory**, which means the entire store listing — title, descriptions, screenshots, feature graphic — is managed by hand in the Play Console. It is not versioned, not reviewable, and not reproducible.

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

`mappingFile` is already uploaded. `debugSymbols` is not, so add it in the same change as the staged rollout.

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

**Do generate tablet screenshots.** Play requires 7" and 10" screenshots to be eligible for large-screen promotion, and the app now deserves them.

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

---

## 14. Accessibility

**Swahili was dropped on 2026-10-04.** The app is built for a global audience, not only for
attendees in Kenya, so it ships in English and this section is the accessibility audit alone.
Nothing blocks it: the navigation labels that used to be hardcoded Kotlin strings are
`@StringRes` values on `TopLevelDestination`, which is also what TalkBack reads.

### 14.1 Accessibility checklist

Beyond §5.6's semantics work, the things that need explicit verification:

| Area | Check |
| --- | --- |
| **TalkBack** | Every screen navigable; every action reachable; no unlabelled controls; reading order logical |
| **Font scale** | Legible and unclipped at 200%; no fixed-height text containers |
| **Display size** | Largest display size setting doesn't break layouts |
| **Contrast** | 4.5:1 for body text, 3:1 for large text — **audit every role pair**; the tightest today is `onSurfaceVariant` on `surfaceContainer` at 4.54:1 in light, and neon on the hero blue (4.1:1) is display-size only |
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

CI runs on every pull request — read `.github/workflows/pr.yml` rather than a copy here — and
Dependabot opens weekly grouped updates. What is left:

### 15.1 CI gaps

- **An APK-size gate.** It needs a `.github/actions/apk-size-diff` action (§9.4).
- **A dependency-review job.**

### 15.3 Contributor experience

**Add `.editorconfig` rules that match ktlint,** so the IDE and CI agree. Currently `.editorconfig` exists — verify it encodes the same rules ktlint enforces, otherwise contributors get formatted-then-rejected.

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

---

## 16. Roadmap and sequencing

### 16.1 Priority order — what to do next

Ranked. Higher items are either prerequisites for lower ones, or buy more per unit of work. A
struck row has landed.

| # | Do this | Section | Why here |
| --- | --- | --- | --- |
| ~~**1**~~ | ~~Fix duplicated sessions after sync~~ — **done** | — | Synced tables are keyed on what sync matches on (schema 6). |
| ~~**2**~~ | ~~Sync follow-ups that need no backend~~ — **done** | — | Transactions, empty-response guard, `isSyncing`, one request builder, stable notification. |
| **2a** | Feed 404 fails every sync | §3.10 #1 | Every sync retries for ever until it is handled. |
| ~~**3**~~ | ~~Roborazzi screenshot suite~~ — **done** | §10.2 | The only mechanism that makes design-system work reviewable. Build it before §5, not after. |
| **5** | Compose compiler stability config | §3.1 | The cheapest fix for the 20 unstable-collection findings and a chunk of the 47 non-skippable composables. No call sites change. Previously struck as done; it never landed. |
| ~~**6**~~ | ~~Design-system token restructure, then M3 Expressive~~ — **done 2026-10-05**, with the 2026 rebrand, on a pinned material3 alpha | §3.5 → §5 | The single biggest visible change available. What is left is listed in §5.2. |
| **8** | Accessibility audit (Swahili dropped 2026-10-04) | §14 | Independent of the above; can run in parallel by a separate owner. |
| **9** | Real size wins: `material-icons-extended`, fonts, `constraintlayout-compose` | §9.4 | Measurable against a recorded number. Unlike dead catalog entries, R8 cannot strip these. |
| **10** | Phase-aware home and schedule conflicts | §5.3 | Answers question 2 of §5.1, and the hero already has the slot. |
| **11** | 3D cube hero | §12 | Delight with no dependencies; build it once the app is accessible and fast. |

Then the product surfaces — ticketing (§7), notifications (§8), calendar export (§11.4a), venue map (§11.6) — and only after those, the AI work (§6).

The AI section is still deliberately last. It is the most interesting part of this document and it should be built on a codebase that has screenshot tests and a measured startup time, otherwise it becomes the thing blamed when something unrelated regresses.

### 16.2 Stages, in dependency order

Ordered, not scheduled. Each stage is a coherent unit that leaves the app shippable when it completes. Move to the next when the previous one's milestone is met — not on a date.

Stage 1, making the codebase safe to change, is complete.

**Stage 2 — Make it a 2026 app**
- ~~§3.5 → §5 design system: token restructure, rebrand, then M3 Expressive~~ — **landed 2026-10-05**
- §14 accessibility audit
- **Milestone:** contrast test green

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

**Stage 5 — After the conference**
- §6.11 recap notification, a couple of days out
- Retrospective driven by analytics: which features got used? Instrument for this during Stage 3, not after.
- Prune. Anything with negligible engagement is a candidate for deletion, not iteration.

**Deliberately deferred:** job board (§11.1), code challenge (§11.3), booking (§11.4b), connections (§11.2). Each needs either a backend commitment or a named editorial owner. Revisit at the §11.0 planning meeting — and if the answer is "nobody owns the content," the answer is no.

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
- AI (§6) needs `:core:testing` to test its router.
- The cube (§12) needs nothing, but lands after §14 so its reduced-motion path is audited with the rest.

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
| **Backend isn't ready** for signed tickets, jobs, or bookings. | **High** | Medium | Start the backend conversation during Stage 2, not Stage 3. Every backend-dependent feature has a documented degraded v0. |
| **Editorial features ship empty.** Job board with no jobs; challenges with no challenges. | High | Medium | Do not build without a named owner who has committed to content (§11.0). |
| **Cube battery drain** in the field. | Low | Medium | Pause off screen, reduced-motion static pose, and the `cube_hero_enabled` Remote Config switch (§12.3). |

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
10. **KMP?** `:core:model` is a JVM module and most of `:core:data` is plain Kotlin. Making them multiplatform would enable an iOS app and a web agenda from the same models. **Genuinely worth considering** — but only if someone wants to build the iOS app. AGP 9 is in, so KMP modules would use `com.android.kotlin.multiplatform.library`.
11. **How much analytics?** Deciding what to prune post-conference requires knowing what got used. Instrument in Q3, and be explicit in the Data Safety form.

---

## 18. Appendix A — file-by-file change index

Quick reference for where the pending changes land.

### Build

| Change | File | Section |
| --- | --- | --- |
| Stability config | `compose_compiler_config.conf` (new), `build-logic/.../AndroidCompose.kt` | §3.1 |
| Event slug from Remote Config | `core/network/.../remote/Constants.kt`, `RemoteConfigConfig.kt`, `UrlProvider.kt` | §3.6 |

### Consolidations

```
core/ui/.../common/components/{LoadingBox,AnimatedShimmerEffect}.kt  # one loading treatment (§5.2)
```

### New modules

```
core/ai/                      §6.2
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
| [Adaptive JetStream](https://github.com/android/adaptive-apps-samples/tree/main/AdaptiveJetStream) | `NavigationSuiteScaffold` and posture handling. It uses `ListDetailPaneScaffold`, which this app forbids — see [`docs/architecture.md`](architecture.md#navigation) |
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

**Build**
- [AGP release notes & upgrade guide](https://developer.android.com/build/releases/gradle-plugin)
- [Gradle 9 upgrade guide](https://docs.gradle.org/current/userguide/upgrading_version_8.html)
- [Now in Android's `build-logic`](https://github.com/android/nowinandroid/tree/main/build-logic) — the reference for convention-plugin structure this repo already follows

**Other**
- [Edge to edge](https://developer.android.com/develop/ui/compose/layouts/insets)
- [Material 3 Expressive](https://m3.material.io/) and [Compose Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Roborazzi](https://github.com/takahirom/roborazzi)
- [Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility)

---

## Changelog

| Date | Change |
| --- | --- |
| 2026-10-05 | Pruned to pending work only. Deleted the landed-work notes at the top, §1.1–1.4, §3.3, §3.4, §3.7, §3.8, most of §4, §9.1–9.2, §16.0 and the struck roadmap rows; git keeps them. Durable facts moved to `docs/architecture.md`, `docs/performance.md`, `AGENTS.md` and `README.md`. §16.1 #5 is no longer struck: the Compose stability configuration (§3.1) never landed. |
| 2026-10-05 | Recorded the 2026 rebrand and Expressive components: §3.5 and the landed-work block move to `docs/architecture.md`, §5.2 lists only what is left, §5.1 and §5.3 note the Now/Next hero, and §12 is decided on a Compose Canvas cube after reviewing the website's Spline scene. |
