# Declarative layout v3

## Delivered contract

- Rail `position`: `LEFT` (legacy default), `RIGHT`, or `BOTTOM` (horizontal, scrollable dock).
  Existing `width` controls side-rail width or dock-item width; the dock reserves 112 reference dp vertically.
- Top bar `position`: `TOP` (legacy default) or `BOTTOM`.
- Rail and top bar `glass`: optional boolean, default `false`.
- Composition `widgets`: optional list of up to four internal widgets, default empty.
  - `type`: `CLOCK` or `WEATHER` only.
  - `anchor`: `TOP_START`, `TOP_END`, `BOTTOM_START`, `BOTTOM_END`; unique within a composition.
  - `width`: 100–400 reference dp (default 320).
  - `height`: 60–240 reference dp (default 200).
  - `showDate`, `use24Hour`: booleans (default true); used by the clock.
  - `overlay`: default false. When true the widget is anchored to the whole canvas, not a reserved row.
  - `offsetX`, `offsetY`: 0–360 reference dp from the selected edge (default 0).
  - `glass`: default true; false allows an unboxed clock. Overlay clocks stack date beneath time.
- Renderer clamps widget width to half the available content width, and height to 24% of
  the canvas height. Top/bottom widget rows reserve space instead of covering app cards.
  Weather content can scroll to expose actions in a short region.
- Navigation insets apply to home blocks and embedded section content, including overlay rails.
- Side rails also accept `topInset` (0–360, default 0) and optional `height` (120–600).
  Long rail content scrolls without overlapping the clock.
- `bgStyle: LANDSCAPE` selects original, bundled blue/coral mountain-lake artwork. Its glass
  surfaces draw correctly aligned crops of an offline pre-blurred copy, then violet-gray tint,
  directional sheen and a fine rim. Other backgrounds use a tinted fallback. This is a static
  approximation, not dynamic backdrop sampling or optical refraction. Children stay sharp.
  No API31-only rendering effect, network request or additional dependency is required.
- `macOS Liquid Glass` is a composition preset, not a new built-in layout ID.

## Data and compatibility

The app host supplies existing current-weather state and actions through
`LocalCanvasWeatherContent`; the canvas performs no weather network requests. Disabled or
unconfigured weather does not mount a widget. Weather settings remain available from the
launcher settings. Legacy compositions without a weather widget retain the original weather
card; compositions with one do not mount the duplicate overlay.

Archives with these new semantics export as format 3. Existing compositions still export as
format 2, and appearance-only packages remain format 1. Readers retain formats 1 and 2;
extended layout semantics in a format-2 manifest are rejected rather than silently downgraded.
Unknown types, properties, invalid primitive values, unsafe sizes and duplicate widget anchors
are rejected on import.

## Explicitly deferred

Android external widgets and host binding/consent, live media-session controls (notification
listener access), system telemetry widgets, calendar integration and photo libraries remain
out of scope. The internal clock already provides a functional date display. Weather remains
current conditions only; no forecast data is fabricated. No new permissions are requested.

## Validation

Unit tests cover schema round trips, rejected input and archive version selection. Compose
instrumentation tests exercise D-pad dock traversal/activation/exit, overlay section bounds
for all rail positions, vertical-rail D-pad exit, bottom-bar bounds, and a single host weather
slot with no empty region when disabled. Previews cover the glass preset and right rail with
bottom bar. Text rails use the existing localized device-settings/personalization labels.
The existing settings regression now activates TV surfaces using D-pad center rather than
injecting touch input, which did not invoke the action in this emulator run.

Executed gates:

```sh
./gradlew :core:model:test :core:data:testDebugUnitTest :core:ui:testDebugUnitTest :feature:editor:testDebugUnitTest :feature:home:testDebugUnitTest :app:assembleDebug :app:lintDebug
env ANDROID_SERIAL=emulator-5554 ./gradlew :core:theme:connectedDebugAndroidTest
```

The unit suites contain 103 passing tests (model 5, data 46, UI 24, editor 8, home 20).
The Android TV Android-16 emulator suite contains eight passing tests. Debug assembly and
lint pass. Runtime behavior on API 26, a live weather-service E2E run, and the complete editor
save/import UI journey were not exercised. Figma identity was checked, but no target design
file was supplied and no editable Figma deliverable was produced.

## Reference-composition integration

The updated preset and `demo-themes/macos-liquid-glass.zip` use the left sidebar, large
unboxed clock/date, a glass `FEATURED_APP` hero, a right weather overlay and square installed
app rows. No desktop menu bar or bottom dock. Home, Apps, Search, device settings and
personalization are actual launcher destinations; unsupported reference movie/live-TV/game
categories and HDMI controls are intentionally omitted, not replaced with invented behavior.
Weather is host-supplied existing state, absent when disabled/unconfigured. The compact card
opens weather settings and retains source attribution; it does not fetch weather in the renderer.

The clock/rail/hero positions use a 960-dp canvas width and scale to the TV viewport. At the
reference aspect ratio the next apps heading is partly below the fold; Home scrolls vertically
and app rows horizontally. The same renderer remains responsible for other themes and sections.
An empty custom-theme selection falls back to the existing layout's declarative composition,
rather than rendering an empty Home.

Artwork is original procedural Core Graphics output (no screenshot or third-party imagery).
Regenerate it with `swift tools/generate-glass-landscape.swift`. Rebuild the ZIP with the real
writer, explicitly opting into the maintenance test:

```sh
UPDATE_LIQUID_GLASS_ARCHIVE=1 ./gradlew :app:testDebugUnitTest --tests '*LiquidGlassThemeArchiveTest.rebuild*'
```

Normal tests never rewrite assets. Archive/preset/install parity, PNG signature, geometry
codec bounds, old defaults and standalone v3 version selection have regression tests.
`LiquidGlassCompositionTest` additionally checks separated sidebar/hero/weather bounds and
D-pad featured-app activation. Do not interpret its presence as a successful device run without
checking the latest connected-test result.

Existing installed themes, including customized copies of the previous Liquid Glass ZIP,
are not automatically overwritten. Reimport the bundled ZIP or create a new theme from the
updated **macOS Liquid Glass** preset to adopt the new composition.

### Integration validation (2026-10-27)

- RED: the new reference-layout test failed against the original bottom-dock preset.
- GREEN: 117 unit tests pass across model/data/UI/editor/home/app; one opt-in archive-generation
  maintenance test is skipped in the normal suite. Debug assembly and `:app:lintDebug` pass.
- Android TV Android-16 emulator: a complete 10-test `:core:theme` run passed after restart.
  A subsequent final recheck was interrupted when the emulator shut down after four completed
  tests (Gradle reported an incomplete run); that latest run is not a full PASS. The earlier
  successful run and manual APK inspection provide device evidence, but emulator stability is
  a remaining validation limitation.
- Actual APK inspected at 3840×2160 (screenshot scaled to 1218×685), including theme creation,
  save, apply, bundled ZIP import/reimport and D-pad navigation. The imported theme uses real
  installed app artwork; disabled weather leaves its reserved right overlay absent. The taller
  16:9 viewport reveals more apps than the 1218×536 reference. Artwork remains an original
  procedural illustration, not a copy of the reference photograph.
- No API-26 device run or live-weather-service E2E validation. API-26-compatible drawing is used;
  no live optical refraction, forecast, fake app catalog, HDMI switching or external widgets.
