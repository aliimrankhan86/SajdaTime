# 1. Principles

Why SajdaTime looks the way it does. When a rule elsewhere seems odd, the reason is usually here.

## 1.1 What the app is

A free, ad free, privacy first Muslim prayer times and Qibla app for Android phones, home screen
widgets and Wear OS, made as ongoing charity (*sadaqah jariyah*). No accounts, no own server, no
revenue, ever. Prayer times are calculated on the device and need no network.

Design consequence: nothing in the interface sells, nags, upsells, gamifies or asks for attention.
The app is opened several times a day for a few seconds. It should answer and get out of the way.

## 1.2 Who it is for

Design for the hardest user, not the average one:

| User | What that means for design |
|---|---|
| An old, small, slow phone (Android 7, 320dp wide) | Every screen works at 320dp and 2.0 text; no effect that needs Android 12 without a fallback |
| No signal | Nothing visible depends on the network |
| Never changes a setting | Defaults must be right; following the phone's theme and language is the default |
| Cannot read English | Pictures carry meaning where they can (the Kaaba, prayer glyphs, the sky); the app is translated |
| Reads right to left | Urdu and Arabic are first class: mirrored layout, checked on every screen |
| Poor eyesight or large text | Text scales; nothing clips; WCAG AA contrast is enforced by test |
| Colour blind or greyscale | No meaning carried by colour alone: always a second signal (shape, position, words) |

**Dependable beats elegant.** When the two pull apart, the dependable choice ships.

## 1.3 The visual direction: Quiet Glass

Chosen by the owner on 17 Oct 2026 from three researched directions. In one sentence: calm, solid,
readable content surfaces, with glass only for what floats.

- **Glass is for what floats, never for content.** The tab bar floats as a pill at 97% opacity.
  Prayer times, cards and text always sit on solid surfaces. (Apple's own rule for Liquid Glass.)
- **Calm.** Warm paper neutrals in light, near neutral darks, one green identity, one amber for
  warnings. No decorative colour.
- **Gloss where it marks the hero.** The next prayer card and the warning notice carry a fixed
  gradient with a soft sheen and rim (the owner's "Apple style premium look", 18 Oct 2026). Nothing
  else is glossy.
- **Depictive colour is allowed, sparingly.** Two families only: the Kaaba's gold (what the object
  is) and the four skies (what time of day it is). Neither ever carries meaning or sits behind a
  prayer time.
- **Every tab opens the same way**: a title at the top start, content below, the floating bar at the
  foot.

## 1.4 Green means something

Green is the colour of Paradise in the Qur'an (18:31, 76:21). In SajdaTime it marks **the next
prayer, the Qibla, and anything you can tap**. It is never decoration, never a background wash, never
used to make a screen "feel brand". If something green cannot be tapped and is not the next prayer
or the Qibla, it is probably wrong.

Amber means **the system is withholding something** (exact alarms, notifications blocked, a time
outside its window). It is not red: alerts still arrive, and an error colour would say the app is
broken when it is not. Red is not used for messages or actions. Its one use today is the setup location error line (`OnboardingScreen.kt`, `error` role), recorded as a deviation in [`03-components.md`](03-components.md) §3.20; do not add more.

## 1.5 The owner's fixed rules for anything visual

These come from `CLAUDE.md` and `docs/DESIGN_SYSTEM.md`. Each was learnt the hard way.

1. **Every action is a solid button with white bold text**, in light and dark. The only text links
   are "Close" and "Got it" at a dialog's foot, plus the usage question's two bold links (his one
   exception). Hierarchy comes from order, position and wording, never from fading a button.
2. **Gaps come from the spacing scale, chosen by relationship.** Never by eye.
3. **No two icon buttons side by side.** A close X stands alone at the top end.
4. **Every dismissable dialog has a close X** as well as its text button.
5. **No hyphens or em dashes in app text**, except inside names that need them (Eid al-Fitr,
   ir-Rahman, Rabi al-Awwal, Open-Meteo).
6. **The religious disclaimer is never removed, softened or buried.** Its dua request is the only
   place the app asks the user for anything.
7. **Rating and sharing are doors the user opens.** Google's own review card, rarely, and passive
   rows in About. The app never asks in its own words.
8. **Nothing is "done" until checked**: emulator in light, dark, 2x text and Urdu RTL, the tests
   green, an independent review, and every gap written down.
9. **When unsure, ask him**, with screenshots and a recommendation first. Do not guess and report
   done.

## 1.6 What the app deliberately does not do

Recorded so nobody "improves" it back. Reasons in `docs/DESIGN_SYSTEM.md` §9 and `docs/HANDOVER.md` §2.

| Not used | Why |
|---|---|
| Material You dynamic colour | Breaks the verified contrast and the green identity |
| A bundled web font | About 400KB, and overrides the user's accessibility font |
| Outlined or tonal buttons | Too easy to miss (owner, 18 Oct 2026) |
| Red or the error role for messages and actions | Says broken when it is not; destructive actions stay green, the words warn (one legacy use, see 03 §3.20) |
| A floating action button or navigation drawer | Not shared with iPhone; Apple rejects look alike Android patterns |
| A gradient that changes with time or data behind text | Its contrast cannot be asserted (the old time of day card) |
| Entrance animations, loops, springs | Calm; motion only answers a touch, a sensor or a real event |
| Uppercasing in code | Breaks Turkish and caseless scripts |
| English laid out right to left | Genuinely wrong; the app goes RTL only when it has the words |
| Gamification, streak pressure, badges | A prayer record is private worship, not a game (tracker rules in §5) |
| Generative AI images, other apps' artwork | Play declaration and licence; distinct look for App Store 4.3 |
