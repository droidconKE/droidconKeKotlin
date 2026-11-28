# AI features plan

What's left before the AI features go on in production. The features themselves are described in
[Architecture](architecture.md#ai-features). They were built live at DevFest Pwani 2026, and this
plan comes from that build and the rehearsals before it. Tick each box as it's done.

## What we learned

- **Most phones get the cloud.** No phone we tested could run Gemini Nano, so the cloud path is
  the one most attendees see. It has to be as good as the on-device one.
- **Every switch defaults to off.** The deploy workflow ships to everyone at once, so each feature
  merges dark and Remote Config turns it on. Debug builds refetch at most every 5 minutes, so flip
  a switch before you need it.
- **A switch can't turn off a dependency.** A2UI is alpha and pulls Compose and Material 3 alphas
  into the whole app, whether Ask the conference is on or not.
- **App Check comes before the cloud.** With enforcement on, every debug install needs its token
  registered, or Gemini calls fail.
- **Library traps cost the most time.** `firebase-ai-ondevice` isn't in the Firebase BoM, a
  `@Generable` class needs mapping to an `@Immutable` model to pass `stabilityCheck`, and Android
  only lists an AppFunction when the manifest declares `android.app.appfunctions.schema`.
- **Latency is the model's thinking.** Ask the conference took minutes with default thinking and
  7 to 80 seconds with `ThinkingLevel.MINIMAL` and short replies.

## Before merging to main

- [ ] Decide what to do about A2UI. Either wait for an A2UI release on stable Compose, or merge
  everything except `:feature:ask` now and Ask the conference later.
- [ ] Run the merged app on a real phone and the `small_phone` emulator with each switch on: the
  summary, Ask the conference, and starring a session through `adb shell cmd app_function`.
- [ ] Build a release APK and check the summary still parses with R8 on. If it doesn't, keep only
  the `@Generable` class.
- [ ] Add a test for `AskViewModel`'s flow from question to answer.

## Cost and abuse

- [ ] Keep the summary until the starred sessions change, in DataStore, so reopening Home doesn't
  call Gemini again.
- [ ] Cap Gemini calls per device per day, for the summary and Ask the conference together.
- [ ] Move the summary's model name to Remote Config, as Ask the conference already does.
- [ ] Trim the schedule Ask the conference sends to what answers need: id, day, time, room, title
  and speakers.
- [ ] Move the Firebase project to the Blaze plan with a budget alert, and check current Gemini
  prices first. Free-tier limits are per project, and free-tier prompts may be used to improve
  Google's products.
- [ ] Turn on App Check enforcement for Firebase AI Logic once release builds pass Play Integrity.

## Store and privacy

- [ ] Update the Play Data safety form: starred session titles and questions go to Google's cloud.
- [ ] Update the privacy policy to say the same.

## AppFunction

- [ ] Add a function that finds sessions by title or time, so an assistant can get a session id
  before starring it.
- [ ] Turn on `star_session_function_enabled` once an assistant outside the preview can call
  AppFunctions.

## Rollout

- [ ] Turn each switch on for 10% of users with a Remote Config condition, and watch Crashlytics,
  latency and spend for a day.
- [ ] Go to 100% when both look right.
- [ ] Turn the switches off after the conference, since the production app reads them all year.
