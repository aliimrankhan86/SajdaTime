# 9. iPhone mapping

How the design system carries to an iPhone app so the two look like one product and Apple has nothing
to object to. **No iOS code exists and none is written until Ali decides** (`CLAUDE.md`). This page is
for planning, and for checking every Android decision now keeps that road smooth.

## 9.1 The principle

Same product, same tokens, same words, **native controls**. Users should recognise SajdaTime at once,
and each control should behave the way an iPhone user expects. Where Android and iPhone differ, take
the iPhone's own control and dress it in our tokens; do not port Material components.

App Store review risks for this category, and how the system answers them:

| Guideline | Risk | Answer |
|---|---|---|
| 4.0 Design | Android patterns on iPhone (floating action button, drawer, Material ripples, snackbars as the only feedback) | None of these are in our design; native tab bar, sheets, lists |
| 4.3 Spam, look alike apps | A crowded category of similar prayer apps | Our own look: the skies, the Kaaba dial, the hero gloss, the glyphs, our green and sand |
| 5.1 Privacy | Location, analytics | Location When In Use only, on device; usage counts need a fresh decision and privacy label |
| 2.5.4 Background | Alarms that ring like a clock | iPhone has no exact alarm API; Time Sensitive notifications only |

## 9.2 Tokens

| Token family | iPhone |
|---|---|
| `color.json` → `roles`, `brand` | One Asset Catalog colour set per key, Any and Dark appearances from `light` and `dark` |
| `gradients` | `LinearGradient(stops:, startPoint: .top, endPoint: .bottom)`; sheen as an overlay gradient; rim as a `strokeBorder` gradient |
| `glassBarAlpha` | Not needed: the system tab bar brings its own material |
| `typography.json` | `Font.system(size:weight:)` with `relativeTo:` the `iosRelativeTo` style; `.monospacedDigit()` where tabular. Never a bundled font |
| `spacing.json` | `CGFloat` constants, same names, 1dp = 1pt |
| `shape.json` | `.rect(cornerRadius: 24, style: .continuous)` for cards; capsules for buttons and pills |
| `motion.json` | `Animation.timingCurve(0.4, 0, 0.2, 1, duration: 0.15 / 0.25 / 0.40)`; respect Reduce Motion |
| Haptics | `.sensoryFeedback(.selection)` for ticks and pickers; `.impact` once for the Qibla arrival |

Generate the Swift constants from the JSON; never retype them. Add an XCTest that does what
`DesignTokensTest` does.

## 9.3 Components

| Ours | iPhone |
|---|---|
| `CtaButton` | `Button` `.buttonStyle(.borderedProminent)` `.buttonBorderShape(.capsule)` `.controlSize(.large)`, tint `cta`, white bold label |
| Compact `CtaButton` | Same at `.controlSize(.regular)` |
| Amber notice button | Same with tint `accent` |
| Text link (Close, Got it) | Toolbar button in a sheet (`Done` placement), our words |
| Close X | `xmark` in the sheet's top trailing toolbar |
| `Notice` | Custom card in our tokens (no system equivalent); "Details" as a `DisclosureGroup` style toggle |
| Card | Rounded rectangle at 24, light shadow, dark hairline |
| Settings group and rows | Inset grouped `List` with `Section`; `Label` with a tinted SF Symbol tile; `NavigationLink`; `Toggle`; `DisclosureGroup` |
| Radio rows | `List` rows with a trailing `checkmark` (Apple's pattern) |
| Choice chips | Segmented `Picker` for up to three short options, else chips |
| Theme selector | Segmented `Picker` |
| Stepper | Two filled circular buttons around the value (to match), or `Stepper` |
| Switch | `Toggle`, tint `primary` |
| Time picker | `DatePicker(displayedComponents: .hourAndMinute)` |
| Dialogs | `.sheet` with `NavigationStack`; must answer questions as `.alert` with two buttons |
| Bottom sheets | `.sheet` with `.presentationDetents([.large])` |
| Glass tab bar | System `TabView` with four tabs |
| Snackbar with Undo | A small overlay above the tab bar with "Undo" |
| Toast | The same overlay, no action |
| `Tick`, `Bell`, `DayRing`, sky mark, Qibla dial | Custom views drawn from the same geometry in `03-components.md` |
| Widgets, tile | WidgetKit (see `08-surfaces.md`) |

## 9.4 Icons: Material to SF Symbol

Every icon in 1.6, with its SF Symbol. A new Material icon is not finished until a row is added here.

| Material | Where | SF Symbol |
|---|---|---|
| `Schedule` | Times tab, School row, board button | `clock` / `clock.fill` |
| `Explore` | Qibla tab | `location.north.circle` / `.fill` |
| `CheckCircle` | My prayers tab, aligned Qibla, tracker row | `checkmark.circle` / `.fill` |
| `Settings` | Settings tab | `gearshape` / `.fill` |
| `Close` | Close X | `xmark` |
| `ArrowForwardIos` (auto mirrored) | Row chevron | `chevron.forward` |
| `ArrowForward` (auto mirrored) | Notice action arrow | `arrow.forward` |
| `ExpandMore` | Details, folds | `chevron.down` |
| `ArrowDropDown` | City caret | `chevron.down` (or `arrowtriangle.down.fill`) |
| `WarningAmber` | Warning notice mark | `exclamationmark.triangle` |
| `NotificationsOff` | Bell off, notifications card | `bell.slash` |
| `Notifications` | Bell on | `bell.fill` |
| `Alarm` | Bell alarm, Suhoor row | `alarm.fill` / `alarm` |
| `NotificationsActive` | Prayer alerts row | `bell.badge` |
| `Mosque` | Mosque button | No SF Symbol: use our own drawing in the same weight, licensed for both stores |
| `Brightness3` | Fajr row, Eid, Ramadan notice | `moon` |
| `WbTwilight` | Sunrise row | `sunrise` |
| `WbSunny` | Dhuhr row | `sun.max` |
| `Brightness5` | Asr row | `sun.min` |
| `Brightness6` | Maghrib row | `sunset` |
| `NightsStay` | Isha row, Ramadan card row | `moon.stars` |
| `CalendarMonth` | Timetable, Last 30 days, streak row | `calendar` |
| `PictureAsPdf` | Save as PDF | `doc.richtext` |
| `Pause`, `PlayArrow` | Pause, Resume | `pause.fill`, `play.fill` |
| `PauseCircleOutline`, `PlayCircleOutline` | Settings pause rows | `pause.circle`, `play.circle` |
| `EventAvailable` | Made up one, Fasts row | `calendar.badge.checkmark` |
| `Event` | Islamic dates row | `calendar.circle` |
| `LocationOn` | Location, place row | `mappin.and.ellipse` |
| `MyLocation` | Use my current location | `location` |
| `Search` | Find place | `magnifyingglass` |
| `Language` | Language | `globe` |
| `Public` | Jama'ah in another time zone | `globe` |
| `Info` | Info buttons, disclaimer row | `info.circle` |
| `Tune` | Calculation method | `slider.horizontal.3` |
| `Groups` | Jama'ah times | `person.3` |
| `MoreTime` | Match your mosque | `clock.badge.plus` (closest; check at build time) |
| `MusicNote` | Sounds, adhan credits | `music.note` |
| `Timer` | Reminder before prayer | `timer` |
| `PushPin` | Next prayer badge | `pin` |
| `Contrast` | Theme | `circle.lefthalf.filled` |
| `PhoneAndroid` | Version | `iphone` |
| `Lock` | Privacy | `lock` |
| `MailOutline` | Feedback | `envelope` |
| `StarOutline` | Rate | `star` |
| `Share` | Share | `square.and.arrow.up` |
| `BarChart` | Usage counts | `chart.bar` |
| `FavoriteBorder` | Free, forever | `heart` |
| `Person` | Made by | `person` |
| `Code` | Data and libraries | `chevron.left.forwardslash.chevron.right` |
| `FileDownload`, `FileUpload` | Save and load the record | `square.and.arrow.down`, `square.and.arrow.up` |
| `DeleteOutline` | Delete record | `trash` |
| `Add`, `Remove` | Stepper | `plus`, `minus` |
| `Check` | Switch thumb, granted, selected card | `checkmark` |
| Kaaba, sky glyphs, prayer glyphs, the mark | Our drawings | Our drawings (vectors in `core` and `app` resources); export as SVG to an Asset Catalog |

## 9.5 Patterns that change on iPhone

| Pattern | Android | iPhone |
|---|---|---|
| Back | System back gesture and button | Swipe from the edge in a navigation stack; sheets swipe down |
| Permissions | Exact alarms, Do Not Disturb access cards | Not available; drop those cards, keep notifications and location |
| Next prayer badge | Ongoing notification | Live Activity on the lock screen and Dynamic Island |
| Widgets | Three widgets, five layouts | WidgetKit families; lock screen accessories |
| Language picker | In app list | iOS Settings per app language (link to it); keep the in app list only if Apple allows the same behaviour |
| Files | Create and Open Document | `fileExporter` and `fileImporter` |
| Review card | Play In App Review | `requestReview` with the same rare rules |
| Haptics | `LocalHapticFeedback`, `Vibrator` | `sensoryFeedback` |

## 9.6 Code that carries over

From `CLAUDE.md`: new rules and calculations go in `:core` as plain Kotlin with no `android.*` import;
stored data in plain values or JSON. Known gaps today: `AppLocale.kt` and `PrayerLabels.kt` use Android
`Context`; `:core` uses `java.time` (including `HijrahDate`) and the Java `adhan` library. An iPhone
version swaps these (Foundation calendars, an adhan port). The tracker's JSON file format is portable,
so a user could move their record between phones.

## 9.7 Rule for every Android decision now

Each plan states its iPhone effect in one line: **none**, **carries over**, or **Android only,
because**. `plan-verifier` and `reviewer` check it. When two options cost about the same, take the one
that carries over.
