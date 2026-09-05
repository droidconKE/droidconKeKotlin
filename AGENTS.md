# AGENTS.md

Working notes for AI agents and new contributors on the droidcon Kenya Android app.

`CONTRIBUTING.md` covers the human process — issues, forks, PR expectations. This file
covers what you need to know to change code here without breaking it.

---

## Commands

Run these before opening a PR. CI runs the same set.

```bash
./gradlew spotlessApply ktlintFormat          # format first — the checks below are strict
./gradlew spotlessCheck ktlintCheck detekt    # static analysis
./gradlew lint                                # Android Lint + Slack's Compose rules
./gradlew stabilityCheck                      # Compose recomposition regressions
./gradlew assembleDebug                       # build
./gradlew testDebugUnitTest                   # JVM + Robolectric tests
./gradlew verifyRoborazziDebug                # screenshot goldens
```

Screenshot goldens live in `src/test/screenshots/`, outside Gradle's tracked outputs. After
changing anything visual, record with `--rerun-tasks` — an up-to-date test task will otherwise
leave stale images on disk:

```bash
./gradlew recordRoborazziDebug --rerun-tasks   # regenerate
./gradlew compareRoborazziDebug                # diff images for review
```

Instrumentation tests run on Gradle Managed Devices, so no emulator setup is needed:

```bash
./gradlew :data:supportedApiLevelsGroupDebugAndroidTest    # api30 + api34
```

Single test class:

```bash
./gradlew :presentation:testDebugUnitTest --tests "*SessionsFilterStateTest*"
```

No JDK setup needed — `gradle/gradle-daemon-jvm.properties` pins the daemon to Java 17 and the
foojay resolver provisions it.

---

## Module layout

```
app                  Application, MainActivity host, manifest, DI graph root

core:model           Pure Kotlin data classes. A JVM module — no Android on its classpath.
core:common          Dispatcher qualifiers and other cross-cutting plumbing
core:designsystem    chai: colours, typography, shapes, shared components
core:ui              Presentation models, shared composables, navigation primitives, resources
core:screenshot      Roborazzi harness. Test-only — consumed via testImplementation

feature:speakers     A feature: its screens, view models, tests and goldens

domain               Repository interfaces and sync contracts
data                 Repository implementations, sync, mappers
datasource:local     Room database, DAOs, entities
datasource:remote    Ktor client, DTOs, Remote Config
presentation         The features not yet extracted, plus the composition root
build-logic          Convention plugins
```

**Dependency rules**, in the order they matter:

- **A feature module never depends on another feature module.** Anything two features need
  belongs in `core:ui`. Cross-feature navigation goes through the `NavKey`s in `core:ui`.
- **Nothing depends on `presentation`** except `app`. It is a holding pen for the features not
  yet extracted; it shrinks with every extraction and eventually becomes the composition root
  in `app`.
- `core:model` has no Android dependency and the build enforces it — it is a JVM module, so an
  Android import will not compile.
- `data` depends on `domain`, never the reverse. `presentation` does not reach into
  `datasource:local`.

### Adding a feature module

Apply the convention plugin; do not copy another module's `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.droidconke.android.feature)
}

android {
    namespace = "ke.droidcon.kotlin.feature.<name>"
}
```

That gives you Compose, Hilt, jacoco, the quality checks, the Material 3 opt-in and
`core:ui`. Declare only what is genuinely yours on top — `:feature:speakers` adds
ConstraintLayout and nothing else. Add `droidconke.android.library.roborazzi` if the feature
has screenshot tests, and register it in `settings.gradle.kts`.

### Extracting an existing feature

`:feature:speakers` is the worked example; follow its shape.

1. `git mv` the feature's package out of `presentation`, main and test together.
2. Move its screenshot goldens too. A feature's screens are `internal`, so its tests only
   compile inside the feature module.
3. Add the module to `settings.gradle.kts` and to `presentation`'s dependencies, so
   `DroidconEntryProvider` can still reach the routes.
4. Expect two classes of breakage: `internal` no longer crosses the boundary, and Kotlin will
   not smart-cast a public property declared in another module.
5. `./gradlew stabilityDump` — a new Compose module needs its own stability baseline. That is
   the one kind of baseline this repo keeps, because it records API shape rather than hiding a
   violation. Commit both the debug and release files; both are tracked and both are checked.

---

## Stack

Kotlin 2.4, AGP 9.3 on Gradle 9.7, Compose (BOM 2026.08.00, Material 3), **Navigation 3**,
Hilt + KSP, Ktor 3, Room 2.8, WorkManager, Firebase (Crashlytics, Remote Config, Messaging,
Perf). `compileSdk`/`targetSdk` 37, `minSdk` 26.

**The build uses AGP's built-in Kotlin.** `org.jetbrains.kotlin.android` is not applied
anywhere, and both `android.newDsl` and `android.builtInKotlin` are left at their AGP 9
defaults. Do not add the Kotlin Android plugin back: under the new DSL, applying it
alongside AGP's own Kotlin support is a hard error, not a warning.

Consequences worth knowing before you edit a build file:

- Library modules have no `defaultConfig.targetSdk`. Only the test APK does, via
  `testOptions.targetSdk`.
- The `android { }` block resolves to `com.android.build.api.dsl.*`, not the legacy
  `com.android.build.gradle.*` types.
- Source sets belong to AGP, so a `languageSettings` opt-in no longer reaches the compile
  tasks. Use `kotlin { compilerOptions { optIn.add(...) } }` — see `presentation`.

Navigation 3 is not Navigation 2 with a new name. Destinations are `@Serializable` keys
implementing `NavKey`; there is no `NavHost` or route strings. See
`presentation/.../common/navigation/`.

---

## Conventions

- **Formatting is enforced.** ktlint, spotless, and detekt all fail the build. Run
  `spotlessApply ktlintFormat` before committing.
- **Apache licence header** on every file. Spotless adds it.
- **detekt forbids `TODO` in comments.** Write the note as a plain sentence, or file an
  issue.
- **No baselines — none, anywhere.** There is no Android Lint baseline, no ktlint baseline and
  no detekt baseline in this repo, and adding one is not how a violation gets resolved here.
  The options, in order of preference: fix the code; or, if the rule is genuinely wrong for a
  library idiom, configure the rule in `detekt.yml` with a comment saying why; or, as a last
  resort, `@Suppress` at the site with the reason next to it, where a reviewer sees it. A
  baseline file hides all three from review, which is why there are none. Lint suppressions go
  in `config/lint/lint.xml` with the reason. Rules at `error` are clean and must stay clean;
  rules at `warning` are being burned down, and promoting one to `error` is the last commit of
  the work that clears it. Counts are in
  [`docs/static-analysis.md`](docs/static-analysis.md).
- **Strings live in `strings.xml`.** No user-visible text in Kotlin.
- **Colours come from the theme**, never from the raw palette. Read
  `MaterialTheme.chaiColorsPalette` (semantic) or `MaterialTheme.colorScheme` (Material
  roles). Do not import `ChaiBlue` and friends outside `chai/colors`.
- **Lazy lists need a stable `key`.** Without one, scroll position jumps after a sync
  reorders the list.
- **ViewModels own state; composables derive it.** Do not mirror ViewModel state in a
  `remember` — that is how the UI and the data end up disagreeing after rotation.
- Test naming: backtick-quoted sentences, e.g.
  ``fun `room filter matches a real venue room name`()``.

---

## Gotchas

Each of these has already cost a bug. They are easy to reintroduce.

**`minSdk` is 26, and that is what makes `java.time` safe.** It was 24, where `java.time`
(API 26+) is absent and the app threw `NoClassDefFoundError` during the first sync unless
core library desugaring backported it. Raising the floor removed that class of crash
outright, so the `api24` managed device and its guard test are gone.
`isCoreLibraryDesugaringEnabled` stays on for the newer `java.time` additions, but it is no
longer the only thing standing between the app and a launch crash. **Do not lower `minSdk`
back below 26 without restoring both.**

**No `fallbackToDestructiveMigration()`.** A schema change without a matching migration
must fail loudly, not silently delete the user's bookmarked sessions — their personal
conference agenda. Register new migrations in `Database.ALL_MIGRATIONS`.
`DatabaseMigrationTest` fails if the fallback returns.

**Filter options are derived from session data, not typed by hand.** Hardcoded values
drift from what the API returns, and the mismatch is invisible: the chip highlights and the
list goes empty. `SessionsFilterOptionsTest` asserts that no offered option matches zero
sessions. Keep that test.

**Session times are venue-local.** The API sends them without an offset and they mean
`Africa/Nairobi`. Use `@ConferenceTimeZone` for absolute times; use the device clock only
for relative ones ("in 20 minutes"). No `SimpleDateFormat` — it is not thread-safe and
there are none left in production code.

**`Session.rooms` is comma-joined.** A session can run in two rooms. Compare against
`Session.roomList`, not the joined string.

**Navigation keys carry no display metadata.** They are serialized to `SavedState`, so they
must be immutable and must not hold resource IDs. Icons and labels live in
`TopLevelDestination`.

**`chai` now sits on top of Material 3, not beside it.** `ChaiTheme` provides a real
`colorScheme`, `typography` and `shapes`, so stock Material components are on-brand and you do
**not** need to pass colours explicitly any more. Two things follow:

- Read text styles from `MaterialTheme.typography`. Do not rebuild a `TextStyle` with a
  hardcoded `sp` line height — that is the defect the chai text composables used to have, and
  it is what breaks at 200% font scale.
- `LocalChaiColorsPalette` throws if no `ChaiTheme` wraps the content. A composable under test
  needs `ChaiTheme { }` around it; it will no longer silently render `Color.Unspecified`.

The tier-3 `ChaiColors` tokens are mid-migration: ~38 of them still back call sites that should
read `MaterialTheme.colorScheme`. Prefer the M3 role in new code; see §3.5 for the mapping.

---

## Before you finish

- Tests that fail without your change. A test that passes both ways tests nothing.
- Checked in dark mode, at 200% font scale, and on a tablet or in split-screen.
- No new dependency without a reason in the PR description.
- Verified on a device if you touched date handling, Room, or anything on the sync path.

---

## Where this is going

[`docs/architecture.md`](docs/architecture.md) is the long-form version of this file, with
diagrams. [`docs/static-analysis.md`](docs/static-analysis.md) covers the tooling.

`docs/IMPROVEMENT_PLAN.md` is the roadmap: an audit of the current state, then phased work
covering adaptive/large-screen support, a design-system rebuild onto Material 3 Expressive,
on-device and cloud AI features, ticketing, performance, and testing.

Read §1.3 (known defects) and §16.0 (the P0 list) before starting anything substantial —
some of what looks like a bug is already documented, and some of what looks intentional is
a defect with a fix already specified.
