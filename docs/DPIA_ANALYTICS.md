# Data protection impact assessment: optional usage counts

**Status: DRAFT written 3 Oct 2026 by the assistant. The owner must read it and approve it before
release 1.3.0.** The ICO says a DPIA is required when an online service that children are likely to
access is changed significantly (Children's code, standards 2 and 7). SajdaTime says it is suitable
for all ages, and Google Play's audience includes 13 to 17, so this applies even though the app is
not aimed at children. Legal conclusions below are the assistant's reading of ICO pages, not legal
advice. Where a position is not settled it says so.

## 1. What is being done, and why

An opt in setting lets the app send usage counts to Google Analytics for Firebase, so the owner can
see how many people use SajdaTime, how often, for how long, from which countries, which of three
screens they open, and where setup loses people. Purpose: to maintain and improve a free charity
app and plan its next phase. No advertising, no profiling for any other purpose, no selling.

The controller is the developer, Ali Imran Khan, a private individual acting for a charity project. **Settled 3 Oct 2026:** the published policy names him and gives his email address for privacy questions (a public GitHub issues page remains for general questions only).

## 2. What is processed

Only what `UsageCounts.kt` can express, and only after the user chooses Yes: a random Firebase
installation ID; sessions and time the app is open; which main screen; which setup steps were
reached; notifications and location allowed yes or no; exact alarms allowed at the end of setup;
device model, Android and app version, language; and an approximate area (country, often city)
that Google derives from the IP address. Google says IP addresses are not logged or stored in
Analytics. **Recorded on the device before any choice, never sent unless the user says yes:** the SDK stamps the
time of first launch (`first_open`, flagged by Google as "deferred collection"). Seen on a test phone on
4 Oct 2026: stamped at first launch and uploaded only after the user opted in; for decliners nothing left the
device. The policy and consent text now say so. **Never processed:** GPS or coordinates, typed city, sect, madhab, calculation method,
alert or prayer settings, name, email, any account. A test fails the build if the madhab step is
reported, because it is shown to Sunni users only and so would reveal sect.

## 3. Is it necessary and proportionate

- **Alternatives considered:** do nothing (rejected by the owner: he cannot otherwise tell what is
  used or where setup fails); Play Console statistics only (free, no code, but no sessions, screens or
  setup steps); a self hosted tool (needs a server we do not run); a privacy first alternative needing
  event code (rejected as more code and more data). Firebase gives the needed measures with the
  smallest closed set of events.
- **Data minimisation:** closed event set, no free text, no user properties, no timestamps added,
  no BigQuery export, retention set to 14 months, Google signals and ad features off, all console
  data sharing settings off.
- **Lawful basis:** consent (explicit, because using a prayer app can say something about a person's
  faith), obtained through an unticked choice with equal weight buttons, withdrawable in one tap.
  The ICO's "statistical purposes" exception was considered and **not** relied on: it is narrow, and
  Google keeps event level data for months.

## 4. Children (ICO Children's code)

Likely users include 13 to 17 year olds. How each relevant standard is met:

| Standard | How |
|---|---|
| Best interests | Collection is off by default, optional, and saying no changes nothing in the app |
| High privacy by default | Off until chosen; no location, no profiling, no advertising |
| Transparency | The consent text is plain language and states Google receives it, the random ID, what is and is not sent, that it may be processed outside the UK, and the faith point |
| Detrimental use | Nothing is used for advertising, nudging or engagement; no dark patterns |
| Data minimisation | See section 3 |
| Profiling | None; no advertising or personalisation features are enabled |
| Parental controls and nudges | None used. The policy tells under 18s to ask a parent or guardian |

## 5. Risks and how they are handled

| Risk | Likelihood | Handling | Residual |
|---|---|---|---|
| Data revealing sect, madhab or method is sent | Low | Fixed event set, no such field, test enforces path shape | Low |
| Anything is sent before consent | Measured: no | Manifest keeps collection off. Opted out run of 76 min 48 s on 3 to 4 Oct 2026 through a logging proxy that reads the host of every secure connection: no Analytics or Firebase host, no Analytics log lines; the same proxy saw `app-measurement.com` and `firebaseinstallations.googleapis.com` once opted in | Low |
| The SDK notes first launch on the device before consent | Certain | Never sent unless the user says yes (observed); disclosed in the policy and the consent text; switching off resets the data | Low, disclosed |
| A person cannot have their records deleted | Certain | Stated plainly in the policy; ID is random and cannot be matched; resetting destroys the handle | Accepted, disclosed |
| Data leaves the UK | Likely | Disclosed; Google's own safeguards | Accepted, disclosed |
| Identifier survives longer than said | Possible | Console "Reset user data on new activity" set off; 14 month retention | Low |
| Consent screen is unclear or pressured | Low | Equal weight buttons, full text, same text in Settings, review by two independent models | Low |
| Test builds pollute the numbers | Low | Only release builds can send; tests enforce | Low |
| Existing users never see the choice | Was certain | Superseded 3 Oct 2026 (commit 8a39b2b): a one time question for existing users, not dismissible, Yes or No saved, nothing sent until Yes. Checked on an emulator 4 Oct 2026 | Low |

## 6. Outcome

Proceed, provided: the opted out capture shows nothing is sent before consent; the console settings
in the Session 1 list (`docs/ANALYTICS_PLAN.md` section 5) are applied and match the published
policy; the controller identity and contact route are settled; and the owner has read this.

Update 4 Oct 2026: the opted out capture condition is met (see section 5); the first launch note is disclosed. The owner asked on 4 Oct 2026 for the wording to be made as compliant as possible; this is the assistant's reading of ICO guidance, not legal advice. Approved by the owner, Ali Imran Khan, who said in conversation on 3 Oct 2026 that he has read this assessment and wants release to go ahead on these conditions. Recorded by the assistant, not signed.
