# Performance

How startup is measured, and what the baseline profile is worth.

## Results

What shipping the profiles changed, all measured rather than asserted. Detail and method for
each is further down.

| | Before | After | Change |
| --- | --- | --- | --- |
| **Cold start, first launch** (CS50C, API 34, median of 15) | 1334.9 ms | 1046.7 ms | **−288 ms, −22%** |
| **Primary dex** — what is loaded first at launch | 6,067,100 B | 3,898,064 B | **−2.17 MB, −36%** |
| Shipped ART profile (`assets/dexopt/baseline.prof`) | 12,917 B | 19,892 B | +6,975 B |
| App methods with AOT coverage | 0 | 2,741 | — |
| Total dex across all files | 6,067,100 B | 6,452,464 B | +385 KB |

The two headline rows come from different mechanisms and are not the same win counted twice.
The **baseline profile** gets the startup path AOT-compiled on first launch, which is the
288 ms. The **startup profile** drives dex layout, so R8 puts startup classes in the primary
dex and pushes the rest into `classes2.dex` — that is the 36%, and it costs 385 KB of total
dex, which is the price of splitting.

Also established, as baselines rather than improvements:

- **Scroll jank on the sessions list** (Reno4): `frameDurationCpuMs` P50 8.2 / P99 16.5 ms,
  `frameOverrunMs` P50 −3.6 / P99 +4.1 ms. Median frame finishes early; the worst ~10% miss
  the 60 Hz deadline.
- **R8** was already configured correctly. Migrating to the `optimization {}` DSL produced a
  byte-equivalent artifact, and the coroutines R8 optimizations were already active.

The shipped profile covers 2,741 app methods, 626 of them reached only by scrolling sessions
and visiting the other tabs rather than by launching. Cold start is dominated by the launch
path, which the startup profile already covered, so the extra breadth shows up in the first
interaction after launch rather than in the number above.

## The modules

`:benchmarks` is a `com.android.test` module that does two jobs, the way Now in Android does:

- `BaselineProfileGenerator` records which classes and methods the app touches during startup
  and the first visit to each top-level destination. Its output ships in the APK.
- `StartupBenchmark` measures cold start under four compilation modes, so a claim about the
  profile is a number rather than an assertion.

It applies `com.android.test` by id rather than by catalog alias, because build-logic already
puts AGP on the classpath and a versioned request for a plugin already there fails to resolve.

## Running it

Generate the profile — writes `app/src/release/generated/baselineProfiles/baseline-prof.txt`,
which is committed:

```bash
./gradlew :app:generateBaselineProfile
```

Generation runs on the `benchmarkApi34` Gradle Managed Device, not a connected phone. Two
reasons: profile collection needs API 33+ or root, and a GMD makes the profile reproducible
so CI can rebuild it and a reviewer can diff it.

Measure startup — needs a **physical device**, see the constraint below:

```bash
./gradlew :benchmarks:connectedBenchmarkReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark
```

`enabledRules=Macrobenchmark` keeps the profile generator from running in the measurement
pass. Without it the generator appears as a failure on any device that cannot collect.

If a generation run fails partway, delete `benchmarks/build/outputs` before retrying. The
collect step reads the device output directory, and a stale `connected_android_test_additional_output`
from a failed run shadows a good managed-device result — the symptom is
"No baseline profile rules were generated" despite the generator passing.

## What the four modes mean

| Mode | Represents |
| --- | --- |
| `startupNoCompilation` | JIT only. A fresh install with no profile and no usage history |
| `startupWarmedNoProfile` | The app has been used a few times and ART built its own profile, but no baseline profile shipped |
| `startupBaselineProfile` | What a user gets on first launch with the profile we ship |
| `startupFullCompilation` | Everything AOT-compiled. The upper bound, not shippable |

The number that matters is `startupNoCompilation` against `startupBaselineProfile`: that is
first-launch experience before and after shipping the profile.

## What the baseline profile is worth

Measured on a Ciontek CS50C (Android 14, API 34, `armeabi-v7a`), 15 iterations per mode,
cold start, `timeToInitialDisplayMs`:

| Mode | Median | Min | Max |
| --- | --- | --- | --- |
| `startupNoCompilation` | 1334.9 ms | 1233.8 | 2057.7 |
| `startupWarmedNoProfile` | 953.8 ms | 906.4 | 1214.4 |
| **`startupBaselineProfile`** | **1046.7 ms** | 910.1 | 1222.9 |
| `startupFullCompilation` | 1228.2 ms | 1139.3 | 1476.9 |

**The shipped profile takes first-launch cold start from 1335 ms to 1047 ms — 288 ms, or
22%**, inside the 20–30% the guide advertises.

Two results look wrong and are not:

- **The profile does not beat `startupWarmedNoProfile`** (1047 vs 954 ms, and their ranges
  overlap almost entirely). Both end up profile-guided: one from the profile we ship, one from
  ART's own after three warmup runs. That is the point — the profile hands a *first-time* user
  the startup a repeat user would otherwise earn over several launches. It is not beating
  warmed ART, it is arriving before it.
- **Full AOT is slower than profile-guided** (1228 vs 1047 ms). Compiling everything bloats
  the code footprint and costs I/O and page-cache locality at launch. Compiling only the
  startup path wins, which is why `CompilationMode.Full()` is a reference bound rather than
  something to ship.

### How much to trust these numbers

An earlier run of the same four modes on the same device reported 1573.5 / 1017.6 / 1016.6 /
1260.3, and a headline of 35%. That run was confounded: `StartupBenchmark` did not grant
`POST_NOTIFICATIONS`, so the permission dialog appeared on every cold start. It cost the
uncompiled case most — 239 ms — because everything is slower there, which inflated the
apparent gain. The table above is from a run that grants the permission first.

Between those two runs the medians moved by 30–240 ms. Treat differences under about 100 ms
as noise, and re-run rather than reading a single number precisely. Anything quoted here
should come from a run where every mode was measured in the same session on the same device.

### Which device measures what

Two physical devices, and they are not interchangeable:

| | OPPO Reno4 (API 31) | Ciontek CS50C (API 34) |
| --- | --- | --- |
| Force a compilation mode | ✗ ColorOS stubs `cmd package compile` entirely — even `--help` returns `Error: Failed to cpmpile !` | ✓ `speed`, `speed-profile`, `verify` all succeed |
| Generate a baseline profile | ✗ below the API 33 floor, and `adb root` is refused on a `user` build | ✓ API 34 |
| Startup comparison | ✗ | ✓ the table above |
| Jank / `FrameTimingMetric` | ✓ needs no forced compile | ✓ |

Numbers are only comparable within one device. The Reno4's 772 ms unprofiled cold start is
its own data point, not the "before" for the CS50C's 1047 ms.

Two operational notes that cost a run each:

- **Android Studio device mirroring fails the benchmark**, correctly — mirroring makes the
  device push frames to an extra display. Killing `screen-sharing-agent` over adb is not
  enough because Studio re-establishes the session; close the device's tab in Running
  Devices. Do not suppress the `DEVICE-MIRRORING` error, for the same reason as `EMULATOR`.
- **Pin the device** with `ANDROID_SERIAL` when more than one is attached, or
  `connectedAndroidTest` runs on all of them.

## What the profile demonstrably changed

Independent of timing, the shipped artifact changed in a way that can be verified from the
APK. Building `:app:assembleRelease` with and without the generated profile:

| `assets/dexopt/` | Libraries only | Libraries + app profile |
| --- | --- | --- |
| `baseline.prof` | 12,917 bytes | 19,892 bytes |
| `baseline.profm` | 1,555 bytes | 819 bytes |

AndroidX ships profiles inside its own AARs, so the app was already shipping library rules.
What is new is the app's own startup path: 2,741 of the 43,987 rules are `ke.droidcon` or
`com.android254` classes and methods, which previously had no AOT coverage on first launch.

The startup profile does something different and more visible — it drives dex layout, so R8
puts startup classes in the primary dex and everything else behind it:

| | Baseline profile only | + startup profile |
| --- | --- | --- |
| `classes.dex` | 6,067,100 bytes | 3,898,064 bytes |
| `classes2.dex` | absent | 2,554,400 bytes |

The primary dex is loaded first at launch, so a smaller one means fewer page faults before
the app draws. Total dex grew ~385 KB, the cost of splitting — a fair trade for startup
locality, but worth revisiting if APK size becomes the binding constraint (§9.4).

## Jank on the sessions list

`ScrollBenchmark` measures frame timing while flinging the sessions list. Unlike startup
compilation modes, this needs no forced compile (`CompilationMode.Ignore`), so it runs on the
OPPO Reno4 we have. 15 iterations, ~146 frames each:

| Metric | P50 | P90 | P95 | P99 |
| --- | --- | --- | --- | --- |
| `frameDurationCpuMs` | 8.2 | 10.4 | 11.4 | 16.5 |
| `frameOverrunMs` | −3.6 | +2.7 | +3.0 | +4.1 |

`frameOverrunMs` is how far past its deadline a frame finished, so negative is good. The
median frame finishes 3.6 ms early, but from P90 up frames run over by 2.7–4.1 ms, and the
P99 frame duration of 16.5 ms sits right on the 60 Hz budget. Scrolling is broadly smooth
with the worst ~10% of frames missing. That is the baseline to improve against.

## UiAutomator and test tags

`testTagsAsResourceId` is enabled once, on the root `Scaffold` in `:app`. Verified on device:
`nav_home`, `nav_sessions`, `nav_feed`, `nav_about`, `home_header` and the rest show up in
`uiautomator dump`.

One trap worth knowing. The exposed resource-id is the **bare tag** — `sessions_list`, not
`ke.droidcon.kotlin:id/sessions_list`. So use `By.res("sessions_list")`, never the
two-argument `By.res(pkg, tag)`. The two-argument form silently matches nothing, and because
UiAutomator returns null rather than throwing, a journey built on it runs to completion having
done nothing at all. `Benchmarks.kt` has a `requireObject` helper that fails loudly instead.

## Startup, from the traces

The benchmark already records a Perfetto trace per iteration. Reading the CS50C traces with
the `perfetto` Python API (15 iterations per mode, process `ke.droidcon.kotlin`, main thread):

| Slice | No compilation | Baseline profile |
|---|---|---|
| `bindApplication` | 199 ms | 233 ms |
| `activityStart` | 52 ms | 50 ms |
| `activityResume` | 53 ms | 51 ms |

Medians. `bindApplication` is *longer* with the profile because ART loads the pre-compiled
oat and app image there (`OpenDexFilesFromOat` 90 ms, `madvising` 44 ms, `AppImage:Loading`
39 ms in the profiled run); it comes back many times over in the JIT-free activity start and
first frame, which is the 22% in the table at the top.

Inside `bindApplication`, one profiled iteration splits as:

- ART opening the oat file and app image: ~93 ms — the price of having compiled code to load.
- `makeApplication` — `DroidconApp`, the Hilt component, `WorkManager.initialize`, the two
  `enqueue` calls, Timber, the notification channel: **9 ms**.
- Firebase's content-provider initialisation: **82 ms** — `fire-cls` 38, `fire-sessions` 25,
  `Firebase` 8, `fire-perf-early` 6, `fire-fcm` 3, `fire-rc` 2.

So the "heavy `Application.onCreate`" item on the startup checklist does not apply here;
deferring the WorkManager enqueue would buy single-digit milliseconds and was not done. The
one app-controlled cost of note is Firebase, and that is a product decision rather than a
code fix: Crashlytics and Sessions install early to catch startup crashes, and Performance
Monitoring's early hook is what produces its app-start trace. Dropping Performance Monitoring
would remove roughly 30 ms of main-thread time and two busy background threads
(`Measurement Worker`, `ScionFrontendApi`: ~400 ms of CPU in the first 1.5 s on this device).
Worth deciding deliberately if nobody reads its dashboard.

Two things the traces surfaced were fixed:

**ProfileInstaller was never running.** `AndroidManifest.xml` removed the whole
`androidx.startup.InitializationProvider` — the blunt way to stop WorkManager's auto-init —
which also removed `ProfileInstallerInitializer`. The merged release manifest carried only
profileinstaller's broadcast receiver, which is enough for macrobenchmark to force-install the
profile (so the numbers above are real) but installs nothing on a device that did not get the
profile from Play: sideloads, internal APKs, older Play clients. The provider is now merged
and only `androidx.work.WorkManagerInitializer` is removed, so `ProfileInstallerInitializer`
(and `EmojiCompatInitializer`, which Compose text wants anyway) run again.

**No time to full display.** Nothing called `reportFullyDrawn`, so the benchmark could only
report TTID and Play Vitals has no TTFD for this app. `HomeScreen` now reports fully drawn from the
same two conditions that decide whether the sessions and speakers skeletons show — no sync
running and both lists non-empty — so it fires exactly when the last skeleton leaves the screen.
`StartupBenchmark`'s measure block waits for both sections after the first frame so the trace
covers the report, and `StartupTimingMetric` reports it as `timeToFullDisplayMs`. Read it
knowing what it contains: `DroidconApp.onCreate` starts a sync on
every cold start and the home screen hides content behind skeletons while one runs, so every
cold iteration's TTFD includes the network round trip; offline, or between conferences when
there is no data and the skeletons stay, the reporter never fires. It is the honest number for what a user waits for, not a rendering figure. Not yet measured; needs
the CS50C.

Worth knowing when reading TTFD: `HomeScreen` shows the loading skeletons whenever
`isSyncing` is true, even when Room already holds yesterday's data, so on every cold start with
network the content is hidden for the whole sync while the pull-to-refresh indicator says the
same thing. Showing cached content during sync is the biggest perceived-startup lever left,
and it is a UX call, not a performance one.

A footnote for anyone reading these traces: the ~45 ms of `ImageDecoder` work on an
`AsyncTask #1` thread right after `makeApplication` is `com.mediatek.res.AsyncDrawableCache`,
the CS50C's MediaTek framework pre-decoding the app's two largest bitmaps. Not app code.

## App size

Release APK (universal, unsigned): 6,417,593 → **4,618,830 bytes, −28%**.

| Entry | Before | After | What changed |
|---|---|---|---|
| `resources.arsc` | 763,956 | 338,508 | Material Components + AppCompat gone |
| `res/*` | 435 files, 2,860,260 | 66 files, 1,258,103 | same, plus the two rows below |
| Fonts (7 × Montserrat) | 1,389,344 | 763,244 | subset to Latin + Latin Extended |
| PNG / WebP | 180 files, 1,256,636 | 30 files, 458,287 | four large PNGs → WebP; ~150 AppCompat/Material PNGs gone |
| `*.proto` | 64 files, 331,775 | 0 | packaging exclude |
| `*.kotlin_builtins` | 8 files, 53,396 | 0 | packaging exclude |
| `classes2.dex` | 2,585,364 | 2,493,036 | Material/AppCompat classes gone |

- **`com.google.android.material:material` and `androidx.appcompat` were direct dependencies
  of `:app`, `:core:data` and `:core:designsystem`** in an app with no Views. The only use was
  the theme parent `Theme.MaterialComponents.DayNight.DarkActionBar`, whose colour attributes
  nothing read. `Theme.Droidcon` now extends `android:Theme.Material.Light.NoActionBar`
  (`android:Theme.Material.NoActionBar` at night), as NIA does; Compose paints everything after
  the splash. `core-splashscreen` needs only `appcompat-resources`, which stays.
- **Images.** `team.png` was an 896 KB PNG photograph — 14% of the APK by itself. It is now a
  141 KB lossy WebP (quality 90, PSNR 35 dB). `all`, `droidcon_event_banner` and `smiling` are
  lossless WebP, verified pixel-identical to the PNGs (`magick compare -metric AE` = 0), so the
  screenshot goldens that include them do not move.
- **Fonts.** Each Montserrat weight carried 969 codepoints including full Cyrillic.
  `pyftsubset` (fontTools) cut them to Google Fonts' `latin` + `latin-ext` ranges — 552
  codepoints — with `--layout-features='*'` so kerning survives. Verified per weight by
  decomposing every kept glyph in both files: outlines, advance widths and vertical metrics are
  identical, so text renders the same and the goldens hold. The seven weights now exist once, in
  `core:designsystem` where `ChaiTypography` references them; `core:ui` carried a second copy of
  all 18 Montserrat files, 11 of them referenced by nothing, which shadowed the real one in the
  resource merge and is gone.
- **Packaging.** `.proto` schema sources ride along inside protobuf-javalite and firebase-perf
  and are never read at runtime; `.kotlin_builtins` is only read by `kotlin-reflect`, which is
  not on the release classpath (`dependencyInsight` confirms). Both are excluded in
  `app/build.gradle.kts`.
- **About screen** decoded the full-size team photo on the main thread as its own Coil
  placeholder, then Coil decoded it again off the main thread. The placeholder is gone and the
  slot reserves its aspect ratio instead, so nothing jumps.

Left alone on purpose: `androidx.window` and `androidx.biometric` are transitive (Compose UI,
Credential Manager) and R8 already strips what is unused; the x86 `.so` files (33 KB) only
exist in the universal APK, bundles split them out; `localeFilters` would trim library strings
further but would hand Swahili users English in Compose's own accessibility strings; the
`META-INF/**/LICENSE.txt` copies are a legal question rather than a size one. The Compose
stability analyzer's runtime (`com.github.skydoves:compose-stability-runtime`) is on the
release classpath because its plugin adds it to every variant; its consumer rules keep six
small classes, the instrumentation itself is debug-only by the plugin's default, and the few KB
were accepted.

## Still open

- ~~The generator journey is launch-only~~ — **fixed.** The cause was not the build variant
  and not the emulator: `MainActivity` requests `POST_NOTIFICATIONS` on every cold start, and
  macrobenchmark reinstalls each iteration, so the permission dialog sat on top of the app and
  the journey drove the permission controller instead. It only looked variant-specific because
  the permission had already been granted by hand on the debug device. `grantNotificationPermission()`
  in the benchmark setup fixes it. Worth noting the product side too: every user who has not
  granted it sees that dialog on a cold start with no context, which `MainActivity` already
  carries a comment about.
- **Generating on a connected device is worse, not better.** Tried on the CS50C: it produced
  16,479 rules against the emulator's 40,380, so the emulator stays the generator. A low-end
  32-bit device simply executes less of the app.
- **TTFD is wired but not measured.** `ReportDrawnWhen` landed after the last device run;
  `StartupBenchmark` on the CS50C will add `timeToFullDisplayMs` to the four modes.
- **ProfileInstaller needs a sideload check.** Install a release build over adb, launch, and
  `adb logcat -s ProfileInstaller` should log the profile write — it could not before the
  manifest fix. `benchmarkRelease` is the signed variant to use.
- **Two decisions, not bugs:** whether the home screen should keep hiding cached content behind
  skeletons while a sync runs, and whether Firebase Performance Monitoring earns its ~30 ms of
  main thread and two background threads at startup. Both are described under
  "Startup, from the traces".

## R8

Release deliberately uses the **legacy** `isMinifyEnabled` / `isShrinkResources` /
`proguardFiles` DSL, even though AGP 9.3's `optimization { enable = true }` is what the guide
now recommends. Keep rules do live in the new `app/src/main/keepRules/*.keep` source set,
which works with either DSL.

**The new block is incompatible with baseline profile generation in this toolchain.** The
baseline profile plugin builds a `nonMinifiedRelease` variant and turns minification off there
via `isMinifyEnabled`. It does not know about `optimization {}`, so the variant inherited
`enable = true`, was minified, and generation produced a profile of repackaged names —
`La0;`, `La00;` — 1,615 of them, which cannot match a shipped build. App-specific rules
collapsed from 2,136 to 49. Revisit when `androidx.baselineprofile` understands the new DSL.

Nothing is lost by staying on the legacy DSL. Migrating produced a functionally identical
artifact — same entry count, same `resources.arsc`, same 2,931 kept roots, +3,986 bytes of
10.8 MB — because AGP 9 already makes full mode, integrated resource shrinking and precise
resource shrinking the defaults and rejects the old opt-out flags outright.

What is already on for free, verified in the build:

- R8 9.4.14, full mode (no `-dontoptimize`, `enableR8.fullMode` not disabled anywhere)
- Resource shrinking runs inside R8 — there is no separate `shrinkReleaseRes` task
- Coroutines 1.11.0 ships its R8-specific rules (`r8-from-1.6.0/coroutines.pro`), so the
  `-assumenosideeffects` entries for `MainDispatcherLoader`, `FastServiceLoaderKt`,
  `MainDispatchersKt` and `DebugKt` are all in the merged config. That is what lets R8 strip
  the main-dispatcher service lookup and compile out debug probes.

`core:designsystem` used to declare a `consumerProguardFiles` pointing at an empty file, plus
a `proguardFiles` block on a library that sets `isMinifyEnabled = false`. Both were dead and
have been removed.

### Configuration analyzer

`./gradlew :app:analyzeReleaseR8Config` writes `app/build/reports/r8/r8-config-analyzer-release.{html,pb}`
(AGP 9.4). The `.html` is the interactive report; the `.pb` is what the
[android/skills r8-analyzer](https://github.com/android/skills/tree/main/performance/r8-analyzer)
scripts read — protobuf bindings plus `convert.py`, `analyze.py` and `report.py` from its
reference doc, which need a Python venv with `protobuf`. Run over this branch's release build:

| | Before | After |
|---|---|---|
| Optimization score | 98.09% | 98.17% |
| Obfuscation score | 98.41% | 98.48% |
| Shrinking score | 98.33% | 98.40% |
| Keep rules with impact | 144 | 137 |
| Global rules (`-dontobfuscate`, `-keep class ** { *; }` …) | 0 | 0 |
| Live classes / methods | 17,593 / 92,923 | 17,344 / 91,284 |

What it found: the five most expensive rules are all AGP defaults or library consumer rules —
enum `values()`/`valueOf`, Play Services `zzadu` fields, `@Keep`, protobuf
`GeneratedMessageLite` fields, `WindowInsetsCompat` member names — and each blocks at most
0.33% of members. Nothing to act on there. `app.keep`'s three kotlinx-serialization rules
were exact duplicates of what `kotlinx-serialization-core` now ships as consumer rules (the
analyzer marks each pair as subsuming the other), and its `-keepattributes` line duplicated
`proguard-android-optimize.txt`. `app.keep` is down to its single `-dontwarn`; the after-report
confirms every `@Serializable` `Companion` and `INSTANCE` in the app is still kept, by the
library's rules. The ten subsumed pairs that remain are library-versus-AGP-default duplicates
and not ours to edit.

## CI

Generation is deliberately **not** wired into the PR gate. It boots an emulator and takes
minutes, and the profile only needs regenerating when startup code changes. Regenerate it
deliberately, review the diff, and commit it.
