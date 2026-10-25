# Design tokens

Machine readable values for SajdaTime, release 1.6. Read by people, AI sessions, and later the iPhone
build. Four of the five files are held to the Android code by `DesignTokensTest`
(`app/src/test/java/com/sajdatime/app/DesignTokensTest.kt`).

| File | Holds | Source in code | Tested |
|---|---|---|---|
| `color.json` | 32 Material roles in light and dark, the call to action fill, the Kaaba gold, the hero, warning and sky gradients, the glass bar opacity | `ui/theme/Color.kt`, `ui/theme/Theme.kt` | Yes: every value, every Material role, both directions |
| `typography.json` | 10 type styles plus the prayer time and button label styles | `ui/theme/Type.kt`, `ui/components/CtaButton.kt` | Yes: size, line height, weight, letter spacing, tabular figures |
| `spacing.json` | The five gap tokens and fixed layout metrics | `ui/theme/Spacing.kt` | The five gaps, both directions; layout metrics by review |
| `motion.json` | Three durations, the easing, two exceptions, the rules | `ui/theme/Motion.kt` | Durations and easing |
| `shape.json` | Corner radii, borders, elevation, component sizes | Spread across `ui/` (no single file) | No: a census taken on 25 Oct 2026. Review |

## Format

Close to the W3C Design Tokens Community Group format, without its tooling:

- A token is an object with `$value` and `$type`. Extra keys explain it: `kotlin` (the symbol it is
  held equal to), `m3Role` (the Material 3 colour slot), `usage` (where it lands), `ios` or
  `iosRelativeTo` (the Apple equivalent).
- A colour that differs by theme has `light` and `dark`, each with its own `$value` and `kotlin`.
- Colours are `#RRGGBB`, or `#RRGGBBAA` when translucent. **Note the order:** Kotlin writes
  `0xAARRGGBB`, so `Color(0x99FFFFFF)` is `#FFFFFF99` here.
- `{"ref": "background.dark"}` points at another token instead of repeating its value.
- Sizes are unitless numbers meaning dp on Android, pt on iOS (and sp for text, which scales with the
  user's setting on both). Durations are milliseconds.

## Rules for using them

1. **Use roles, not hex.** A composable reads `MaterialTheme.colorScheme.primary`, never a hex value
   (no hex outside `ui/theme`). On iPhone, read the named colour set, never a literal.
2. **Use a role for what it means, not because the colour looks right.** `primary` means the next
   prayer, the Qibla or something tappable. `accent` means withheld. `outlineVariant` is decorative and
   below 3:1, so never for an icon.
3. **Depictive colours** (`brand.kiswah`, `gradients.sky`) are pictures, not states. Never reuse them for
   meaning, never alias them to a role.
4. **A new token** is a design decision. Add it to the Kotlin and here in one commit, add its contrast
   pairs to `ColorContrastTest`, and log it in `../CHANGELOG.md`.

## Consuming them on iPhone (later)

- One Asset Catalog colour set per entry in `roles` and `brand`, with Any and Dark appearances from
  `light` and `dark`. Name it after the key (`primary`, `onSurface`, `cta`).
- Gradients become `LinearGradient` with the listed stops, top to bottom.
- Each type style becomes `Font.system(size:weight:)` with `relativeTo:` its `iosRelativeTo` style, and
  `.monospacedDigit()` where `tabular` is true, so Dynamic Type scales it.
- Spacing becomes `CGFloat` constants with the same names.
- Write a generator from these files rather than retyping, and an XCTest twin of `DesignTokensTest`.
