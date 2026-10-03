# How to read your SajdaTime numbers

For the owner. Written 3 Oct 2026, **before any real data exists, so screen names come from Google's
documentation and may have moved: if one is missing, search the console for the word in bold.**

## Two places, two jobs

| Where | What it is for |
|---|---|
| **Play Console** (play.google.com/console) | The only **true totals**: installs, active devices, country of installs, ratings, crashes. Not a sample. |
| **Firebase console** (console.firebase.google.com) then your project then **Analytics** | **How people behave**, but only people who said Yes. A sample. |

Never quote a Firebase number as "how many users I have". Say "of the people who opted in".

## Firebase: what to open and what it tells you

- **Dashboard** (or Reports, then App developer, then Firebase): active users over 1, 7 and 30 days
  and **average engagement time**. Engagement counts only time the app is on screen. A prayer app is
  glance and go, so a short time is normal, not a failure.
- **Realtime**: who is using it in the last 5 and 30 minutes. Use it to check the first data arrives
  after release. Everything else lags by up to 24 to 48 hours.
- **Retention** (Reports, Life cycle): of people who opened it on day 0, how many came back on day 1,
  7, 28. **This is the best sign of usefulness.** Low time with good retention is a healthy prayer app.
- **Events**: `screen_view` (which of Times, Qibla, Settings), `setup_step` (how far through setup
  people get), `permission_result` (allowed or not). Your own parameters (`step`, `permission`,
  `granted`) only appear in reports after you register them under **Custom definitions**, then 24 to
  48 hours (the assistant sets this up in the Firebase session).
- **Demographics, Geo**: countries, and city when Google provides it. Small groups are hidden.

## What each number can and cannot tell you

| You see | You can say | You cannot say |
|---|---|---|
| Qibla screen has few views | Fewer opted in users open Qibla | That nobody wants Qibla. It might be used only now and then |
| Many people stop at the permission step | Setup friction at location is a candidate | Why. Ask for reviews or feedback |
| Few people allow exact alarms | Reliability is at risk for them | That they refused. Many Android versions grant it by default, so this is a **state, not a decision** |
| Retention at day 7 is low | People do not come back in the first week | That the app is bad. Notifications may serve them without opening it |
| A country has many users | Where opted in users are | Where all users are. Use Play Console for that |

## Rules of thumb

- **Sample bias.** Expect only a minority to opt in, and not a typical minority. Compare *ratios*,
  never raw counts, and never compare Firebase to Play Console counts.
- **Small numbers lie.** With a few dozen opted in users, one person moves a percentage a lot. Wait
  for weeks of data before acting, and treat anything under about 50 users as a hint.
- **Do not chase engagement time.** It is not the goal of this app.
- **Demand for a new feature is not in this data.** These numbers show how people use what exists.
  For what to build next, read Play reviews and ask people directly.
- **The data cannot say who anyone is, their sect or their settings. That is by design and a rule
  of the project.** If you want "which madhab is most popular", it must go through a privacy policy
  change and your explicit decision first. Do not ask an assistant to add it quietly.

## A simple routine

1. Weekly, five minutes: Play Console installs and ratings; Firebase retention and active users.
2. After any release: Realtime that day; Events after two days.
3. Before deciding anything: write the question down and check this table for whether the data can
   answer it.
