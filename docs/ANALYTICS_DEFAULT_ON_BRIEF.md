# Usage counts on by default: brief for a data protection adviser

Prepared 3 Oct 2026 for the owner (Ali Imran Khan). Not legal advice. Purpose: get a clear answer
before anyone builds "on unless the user turns it off".

## Facts
- SajdaTime: free Android and Wear OS prayer times and Qibla app, UK based developer, no ads, no accounts.
  Users include children and people worldwide. Using a prayer app can reveal religion.
- Proposed: Google Analytics for Firebase (SDK 23.2.0) collecting fixed events only (screen name for 3
  tabs, setup step reached, notification/location/exact alarm yes or no). Tied to a random app instance
  ID. Google derives approximate area from the IP address. Retention 14 months. Google signals and all
  data sharing off. Data Processing Terms accepted. Data may leave the UK.
- Never sent: sect, madhab, calculation method, settings, city or coordinates.
- Currently: off until the user taps Yes (setup question, or Settings). Release builds only.
- Wanted: collect from first launch unless the user switches off, to improve the app.

## Questions
1. Does PECR reg 6 (as amended by the Data (Use and Access) Act 2025) allow this without prior consent?
   Does the new statistical purposes exception cover Firebase, given Google is a third party?
2. If consent is required, is "notice then collect, with an off switch" ever acceptable? If not, is a
   first run question with equal Yes and No buttons enough?
3. Does the faith link make this special category data under UK GDPR art 9, and what condition applies?
4. Children: what does the ICO Children's code require (default settings, nudges, DPIA)?
5. EU users: ePrivacy and GDPR consent position for users in the EU.
6. Google Play: Data safety wording if collection is by default, and the User Data policy on
   prominent disclosure and consent.
7. Is the existing DPIA (docs/DPIA_ANALYTICS.md) adequate for a default on design, or does it need redoing?
8. International transfer to Google (IDTA or UK addendum) and the controller or processor roles.

## Material to give the adviser
docs/privacy.html, docs/DPIA_ANALYTICS.md, docs/ANALYTICS_PLAN.md, and the consent strings in
app/src/main/res/values/strings.xml (names starting consent_).

## Where to start
ICO helpline for small organisations (free), or a data protection solicitor for a short written opinion.

## If the answer is "default on is acceptable"
Needed before release: flip the stored default, a first run notice, rewritten privacy policy, consent
text, DPIA and Data safety answers, new tests, and a new capture proving what is sent. Owner signs off.
