# Play store listing

The listing in Google Play, kept here so it changes with the app. No workflow uploads it: copy each
file into Play Console by hand (Grow users → Store presence → Main store listing). The full
description claims only what the app does, so update it when a feature ships or goes.

| File | Play field | Limit |
| --- | --- | --- |
| `en-US/short_description.txt` | Short description | 80 characters |
| `en-US/full_description.txt` | Full description | 4,000 characters |
| `en-US/images/icon.png` | App icon | 512 × 512 PNG |
| `en-US/images/featureGraphic.jpg` | Feature graphic | 1024 × 500, no alpha |
| `en-US/images/phoneScreenshots/*.png` | Phone screenshots, in file order | 2–8, at most twice as tall as wide |

Release notes are not here: the deploy workflow ships `whatsnew/whatsnew-en-US` with each release.

## Icon and feature graphic

Both are drawn from the app's own resources by `StoreListingGraphicsTest` in `:app`: the icon from
the launcher icon's layers, the feature graphic from the hero banner, the logo and the About strings.
After a rebrand, a new theme or a cube change, regenerate them:

```bash
STORE_GRAPHICS=1 ./gradlew :app:testDebugUnitTest --tests '*StoreListingGraphicsTest*'
```

Without `STORE_GRAPHICS=1` the test is skipped, so ordinary test runs never rewrite the files.

## Phone screenshots

Taken on the `droidcon_demo` emulator with real conference data, online, and no ticket saved:
someone's ticket and QR must never reach the listing. Play refuses a screenshot more than twice as
tall as it is wide, so the screen is set to 1080 × 2160 for the capture, with a clean status bar:

```bash
adb shell wm size 1080x2160
adb shell settings put global sysui_demo_allowed 1
demo() { adb shell am broadcast -a com.android.systemui.demo -e command "$@"; }
demo enter; demo clock -e hhmm 0941; demo battery -e level 100 -e plugged false
demo network -e wifi show -e level 4 -e fully true; demo network -e mobile hide
demo notifications -e visible false
```

Capture each screen with `adb exec-out screencap -p > name.png`. The current set is Home, Sessions,
a session's details, Speakers, a speaker, Add your ticket, Notification settings (every switch on,
10 minutes) and About; on Home and About, take a few frames and keep one where the cube rests on its
droidcon face. Play wants 24-bit PNGs, so drop the alpha channel:

```bash
magick name.png -background white -alpha remove -alpha off PNG24:01-home.png
```

Then put the emulator back:

```bash
demo exit
adb shell settings put global sysui_demo_allowed 0
adb shell wm size reset
```
