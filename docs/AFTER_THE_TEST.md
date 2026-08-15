# What we do after the closed test

**Plain-language plan, written 1 Aug 2026.** The reasoning, evidence and measurements behind
every item are in [`HANDOVER.md`](HANDOVER.md) §10 and §11 — this file is the short version,
in order, for someone who wants to know what happens next without reading two thousand lines.

---

## 15 Aug 2026 — "The times still do not match my mosque", and what I did about it

You brought a four-part plan written with another AI. Here is what happened to each part, in
plain terms. The measurements are all in `HANDOVER.md` §10, "The gap the banner never
reached", and the rule that came out of it is §5.17.

**First, the honest correction to that plan's starting point.** It assumed the app fetches
times from the AlAdhan website. It does not, and it never will — the sums are done on the
phone, so it works with no signal and never sends anyone's location anywhere. That was your
decision and it stands. Everything below fits inside it.

**What I measured before building anything.** Using the app's own engine, not a website:

- **A gap of 15–30 minutes on Asr is not the Hanafi/Shafi'i setting.** That difference is
  never smaller than 26 minutes anywhere on earth and is usually about an hour. So an Asr
  gap of that size is the mosque board showing the *congregation* time, or the mosque adding
  a few minutes of caution. Nothing is wrong. The app now says so, where the user is looking.
- **The 15–30 minute Isha gap is real, and it lives exactly where the app was not asking.**
  On the default, in New York, Chicago, Toronto and Los Angeles the app's Isha is 10–21
  minutes *later* than the ISNA times most mosques there use, and Fajr 14–32 minutes
  *earlier*. In Riyadh, Jeddah and Doha, Isha is 6–21 minutes *earlier* than the official
  times. In Cairo, Jakarta and Singapore, Fajr is 7–10 minutes *late*, which matters for
  fasting. All of those places are nearer the equator than the line where the "Does this match
  your mosque?" card appears, so those users were never offered the choice. Most of the rest of
  the world is within a few minutes of its own convention.

**What the plan asked for, and what I did:**

| The plan said | Verdict |
|---|---|
| Pick the method for the user automatically from their madhab and region | **No.** Madhab is about Asr, not about twilight; tying one to the other would put a Hanafi in Cairo or Chicago on the wrong convention. And the "South Asian Hanafi → Karachi" mapping moves Isha by four to six minutes and Fajr by nothing. Instead, **the app now asks everyone once, at setup**, with "Not sure — keep this" preselected, and a plain line under every option saying who publishes it and where it is used |
| Ask "early or late Isha?" and quietly switch to ISNA behind the scenes | **No.** That is the app choosing a religious ruling for the user behind a preference question — the same thing you and I ruled out earlier — and it calls ISNA "the red twilight", which no authority says. The plain-language descriptions do the honest version of this job |
| A ±60-minute slider per prayer | **Already built on 2 Aug** ("Match your mosque"), at ±30 with plus/minus buttons. Thirty is on purpose: a bigger gap is the wrong method, not a mosque's caution. Sixty would also let someone drag "Isha begins" onto their congregation time and then the app would be stating something false. Reminding at congregation time is a different feature, written down as such |
| Hide the technical words; describe each method in plain English | **Yes, done.** No angles anywhere. Each method now carries one plain line — "Egypt's official method", "Used by many mosques in the United States and Canada", "The official bodies of Indonesia and Singapore", and so on. Every line was hedged where I could not verify it against the authority itself, and I have written the rules for those lines next to them so nobody "improves" them into claims |
| A reassuring "why times differ" card in Settings | **Yes, but on the Times screen.** A user who thinks the app is wrong is looking at the times, not at Settings, and the disclaimer that explains this is read once. There is now a quiet permanent line under the timetable — *"Different from your mosque?"* — that opens a short explanation: the board shows the congregation; Fajr and Isha follow the twilight method; Asr follows the school; a few minutes are left over; where a difference remains, follow your mosque. Each cause has a button that opens the exact setting. The far-north card also now opens the method list directly instead of just the Settings tab |

**Two small things found on the way.** Switching school on the watch used to keep a Sunni
method behind a Shia choice and silently lose the Shia Maghrib; and on the phone the same
thing could happen in the other direction. Both fixed from one definition, with a test.

**What I verified.** Fresh install walked end to end for a Sunni and a Shia user; the new
step; the summary; the door and all three of its buttons; the far-north card; rotation;
large text; dark theme; right-to-left (as a *layout* check — it mirrors correctly); the watch
still installs and the school switch works. Full build: 133 tests, no failures, no lint
errors, both release bundles built. **Not** verified: any physical phone or watch, and the
store screenshots have not been retaken — the Times screen has one new line, so that is owed
with the next upload (P2).

**Where the complaint came from is still not known.** I have no city or mosque for the users
who reported 15–30 minutes. If you can find that out — even one mosque's published timetable
— it can be checked against the app in ten minutes, the way Slough was.

---

## 15 Aug 2026 (later the same day) — your own phone found two bugs the emulators never could

You plugged in your Galaxy S23 Ultra. It was worth doing: within twenty minutes it showed
two things wrong that a whole day of testing on simulated phones had said were fine. The app
was installed *beside* your Play Store copy, not over it, so your testers were never at risk.

**1. The place name showed a province, not your town.** Earlier that day I fixed the
"Berkshire instead of Slough" problem and wrote down that it was a British quirk — I had
checked eight other countries and they were all fine. Your phone, in Türkiye, showed
**"Alanya/Antalya"**. Antalya is a province; it is the Turkish version of the exact problem
you asked me to remove. So the fix was right but my explanation of it was too confident, and
Turkey writes its addresses in a way that slipped through. It now reads **"Alanya, Türkiye"**,
and the real address from your phone is saved as a permanent test so it cannot come back.

The lesson, written into the notes for whoever works on this next: eight countries agreeing
is a sample, not a proof, and I had quietly written it up as a proof.

**2. The "Different from your mosque?" button was cut in half by the navigation bar.** You
asked for everything important to fit on one screen. It did — on the simulated phone I
measured, which turns out to give an app *more* room than your real one, even though your
real one is the bigger phone. Yours is about a tenth shorter in the units that matter, and
that tenth was exactly the button. I measured the overflow (37 units), found that much space
in padding that was doing nothing, and it now sits whole and clear above the bar. Checked in
both light and dark.

**What else I checked on your phone.** The full setup walk; that Android's own permission box
says *approximate* location, which is the privacy promise in the platform's words rather than
mine; that the alerts are scheduled as exact ones on your Samsung; that the disclaimer scrolls
all the way to the dua request; that the Qibla dial now shows the needle and the Kaaba as two
separate things, with the maths checked and not just the picture; that everything still works
at large text; and the location panel and city search, which had never once been seen on a
real phone before today.

**What I could not check.** I cannot physically turn your phone round, so I could not watch
the Qibla dial reach the "you are facing it" state on that handset — that part is confirmed
on the simulator and by arithmetic. I did not wait by the phone to watch an alarm actually
ring on it; I only read that it was correctly scheduled. And your watch was not paired with
it, so phone-to-watch syncing is still the one thing this project has never seen work.

**One known limitation showed itself.** I searched Slough from your phone in Türkiye and got
Slough's prayer times displayed in Turkish clock time. That is the app behaving as designed
— it uses your phone's clock — but it reads as nonsense if you are checking a city abroad. It
was already on the list as something to decide about; now it has actually been seen rather
than merely predicted.

**I changed two settings on your phone to test with, and put both back**: the text size
(briefly to large, back to your 0.9) and the app's theme. Nothing else was touched, and I
never had, or asked for, your PIN.

---

## 15 Aug 2026 — the compass, and telling you when you have arrived

You asked for the compass to look better and to say something when you are facing the Kaaba.
Here is what changed, in plain terms.

**It now has N, E, S and W on it.** Before, north was a small filled triangle, which only
helps if you already know that is what it means, and it told you nothing about the other
three directions. Now the four letters sit around the ring like a compass you have actually
held. They turn as you turn, but the letters themselves stay the right way up, because a "W"
that is upside down when you face south is no use to anybody.

**Arriving is now an event.** Before, facing the Kaaba just swapped one line of text for
another, in the same size and colour. You could easily miss it. Now, the moment you line up:
the phone buzzes once, the whole dial fills green, the ring around it thickens, the green
arc showing how far you have to turn disappears, a tick appears, and it says in large green
letters **"You are now facing the Kaaba"**. The degrees reading vanishes at that point too,
because it was only helping while you were still turning. On the watch it buzzes and says
"Facing the Kaaba".

Five separate signals, and only one of them is colour. That is on purpose, so it still works
for someone who cannot easily tell green from grey.

**It stopped leading with the technical bit.** The line under the title used to read "153°
from true north. The Kaaba is about 1858 km away." The number most people cannot use came
first. It now reads "The Kaaba is about 1858 km from here, at 153° from north."

**I also went through the wording everywhere.** The disclaimer I left almost exactly as it
is. It reads as something a person wrote, it says the four things it has to say, and the dua
request at the end is yours. I checked it rather than rewrote it. Elsewhere I fixed two
small things: one long dash inside a sentence, and one in a method name, both replaced with
ordinary punctuation.

**One correction I made to my own work.** I first changed "so we can give you the angle" to
"so the app can give you the angle", then put it back. The app says "we" everywhere else
("We could not get a fix", "We never send it anywhere"), and changing one line would have
made the voice inconsistent for no gain.

**Checked on both simulators, in both the turning state and the arrived state**, with the
compass driven to the exact bearing so I could see the arrival rather than assume it. Full
build clean: 142 tests, no failures. Your real phone was unplugged by then, so the buzz
itself has not been felt on hardware, only the code path that fires it.

---

## 16 Aug 2026 — your phone found the buzz was going off every three seconds

You plugged the S23 Ultra back in and asked me to test it. The short version: **the compass
had a real fault that neither simulator could ever have shown me, and it is fixed.**

**What was wrong.** Your phone was lying still on the desk, facing the Kaaba, and nobody was
touching it. In 76 seconds it buzzed **26 times**. Worse, for one stretch it buzzed 6 times
in 14 seconds while the screen calmly read "You are now facing the Kaaba" the whole time —
so it looked completely settled and was drumming away in your hand.

**Why it happened.** A real compass never sits perfectly still; it drifts a degree or two
even when the phone does not. The app had one line drawn at 5 degrees: inside it you have
arrived, outside it you have not. When the reading wobbles right on that line, it crosses
back and forth, and the app read every crossing as "you have just arrived" and buzzed again.
The wobble was too quick to see on screen, which is exactly why this survived being tested
on the simulators — a simulated compass is perfectly steady, so it never wobbles at all.

**What I changed.** Three things, each fixing a different way it went wrong:

1. **Two lines instead of one.** You arrive at 5 degrees, but you do not stop having arrived
   until you have turned 8 degrees away. The gap in between is the wobble, absorbed silently.
2. **No two buzzes within five seconds.** When you first open the screen the compass genuinely
   takes a moment to settle, and that settling swings wider than any sensible allowance.
3. **Opening the screen while already facing the Kaaba no longer buzzes.** It was doing that
   every single time. Opening a screen is not arriving anywhere.

**The result, measured the same way as before**: three minutes, phone untouched, **not a
single buzz** — and the message stayed put the whole time.

**Then I checked the opposite thing, which mattered more.** Making it silent is easy; making
it silent by accident would have been a worse bug than the one I started with, and you would
have found it in a car park somewhere. Since I cannot turn your phone from here, I drove a
simulator's compass around instead: turning onto the Qibla buzzes once, wobbling about while
facing it buzzes not at all, and turning away and coming back buzzes again. It still does
the thing it is for.

**Two things I could finally prove on your phone that I never could before.**

- **An alarm was watched going off.** Prayer times move with where you are, so I briefly set
  the location to Dhaka, which put Fajr 90 seconds away instead of waiting until dawn. The
  alert arrived **37 thousandths of a second** after the minute it was due. I set your
  location back afterwards.
- **The buzz genuinely reaches the motor.** Android keeps a log of every vibration and which
  app asked for it, and yours are in there. **I still have not felt it** — I am reading a
  log, not holding the phone — and I would rather say that plainly than claim more.

**What I could not do.** The watch still cannot be tested against a real phone. Pairing one
needs the "Wear OS by Google" app installed on your handset and signed in with your Google
account, and I will not sign into your account. That is the one job on this list that has to
be yours — and you have said you will do it, so it is written down as yours rather than left
looking undone.

### The one-minute check, for when you have paired a watch

You do not need to test the watch thoroughly. **One comparison answers it**, and it is the
same one that caught the worst bug this project has ever had.

1. On the **phone**, make sure the school is set to **Hanafi** (Settings → School of thought).
2. Open SajdaTime on the **watch**.
3. Compare **Asr** on the two.

**Same time → the sync works**, and I can finally cross this off. **Different → the phone's
settings are not reaching the watch.** Nothing is broken on your end if that happens; it is
exactly the thing we have never been able to test, and it is what I need to know.

Why Asr and not one of the others: Asr is the only prayer the Hanafi/Shafi'i choice moves,
and it moves it by at least 26 minutes and usually about an hour. Every other time would look
identical whether the sync worked or not, so they tell you nothing. If the two disagree, look
at the button at the bottom of the watch's list of times — it names the school the watch
actually used, which tells us straight away whether the setting arrived.

**A second thing worth trying while you are paired**, if you have the patience: change one
prayer by a few minutes on the phone under *Match your mosque*, then look at the watch. That
correction is meant to travel too — the whole point being that the app must never give you
one answer in your pocket and a different one on your wrist.

Just tell me "same" or "different" and I will take it from there.

**I put your phone back as I found it**: text size, dark mode, and your real location. The
Play Store copy of the app was never touched — I install a separate test copy alongside it,
exactly so your closed test is never disturbed.

### I also redid the Play Store screenshots

They were showing an app that no longer exists. The compass picture was **two versions out
of date** — it still had the needle sitting on top of the Kaaba, which is the very thing you
told me looked wrong and which I fixed. The Times picture was from before the "Different
from your mosque?" line existed, and the Settings one said "Greater Manchester" instead of
naming the town.

All seven are retaken and ready. They live in `docs/store/upload/`, one folder per box in
the Play Console, so there is no guessing which file goes where:

| Folder | Where it goes in Play |
|---|---|
| `phone/` | Phone screenshots — all 5, in filename order |
| `wear-os/` | Wear OS screenshots — both |
| `app-icon/`, `feature-graphic/` | Unchanged, but they are there if Play asks |

**Uploading them is yours**, because it happens in the Play Console under your account —
do it in the same sitting as the app update, not before it, so the pictures never describe
a version your testers do not have yet.

Full build clean: **145 tests, no failures** (three new ones, which fail if anyone ever
quietly collapses those two lines back into one).

---

## ⚠️ One thing was not left until later: the app crashed in the far north

**Found on 1 Aug 2026, fixed the same day, and the reason it was found is worth saying.**

You said: *don't just use Slough as an example, we are catering for a global audience.* Every
test this project had ever run was at Slough's latitude. Widening it to the whole planet found
a crash that had been in **every version shipped so far**.

**What was wrong.** Past about 65½° north — Tromsø, Kiruna, Rovaniemi, Murmansk, Norilsk,
Longyearbyen — the sun does not properly rise or set. The prayer-time library returns
"no answer" for every single time, and the app was not expecting that, so it crashed. Not just
one screen: **the home screen, the watch face, the notification and the alarm scheduler all
died together**, in summer and in winter. Anyone living up there installed the app and it never
opened. Those cities have mosques.

**What it does now.** When the sun genuinely never rises or sets, the app borrows the day from
a lower latitude — the nearest place where night and day still separate. That is a recognised
classical position (*aqrab al-bilad*, "the nearest locality"), not something we invented. Your
longitude and hemisphere are kept, so midday stays honest, and the screen **says so** in a
notice that cannot be dismissed.

**Which latitude it borrows from was got wrong the first time, and is now right.** The first
version used 60° for everyone. 60° is the Moonsighting Committee's published figure — but it
is *their* rule for *their* method, and the app was quietly applying it to Muslim World League,
Egyptian, Karachi and every other option too. That is precisely the "deciding a religious
question on your behalf" failure this app exists to avoid, and a reviewer was right to press it.

Checked properly, there are two published answers and the app now follows whichever body you
have already chosen:

| If your method is | It borrows from | Whose rule that is |
|---|---|---|
| Moonsighting Committee | 60° | Moonsighting's own published rule |
| Anything else | **45°** | Islamic Fiqh Council of the Muslim World League, 1406 AH (1986), endorsed by the European Council for Fatwa and Research |

The screen prints the actual number, so you can check it against your mosque rather than take
the app's word for it.

**This matters, it is not a technicality.** At Tromsø on the longest day, borrowing from 60°
gave a fasting day of **20 hours 25 minutes** — longer than the eighteen-hour limit of the
fatwa Moonsighting themselves cite. From 45° it is 18 hours 9 minutes. In December the 60°
version left only **51 minutes** between Dhuhr and Asr; from 45° it is a normal two hours.

**And a second, separate bug was found underneath it — worse than the crash.** Widening the
testing to every latitude and every day of the year (7.3 million checks, which took seconds)
found that the library does not only fail by returning "no answer". Sometimes it returns a
confident wrong answer. **At 71½° north on 27 January it gave the afternoon prayer as
13 March — forty-five days out.** No crash, no warning; the wrong time was simply shown,
written into the PDF and used to set an alarm. This was in the very first release and is in the
build being tested now. It affected 2,239 days a year across the far north *and* far south.
The app now checks that the answer is physically possible before believing it — the six times
must be in order, and midday must actually fall in the middle of the day — and where it is not,
it falls back to the borrowed times and says so.

**How confident I am.** High, for the calculation. Tests now cover all fourteen methods, both
hemispheres, both solstices, and a sweep of every latitude on Earth for every day of a year;
they also check the app does *not* start approximating anywhere normal — Reykjavík and Luleå,
the closest real towns to the line, keep their own times. Sunrise and sunset were cross-checked
against Aladhan and agree **to the minute**. Full build passes: 129 tests, no failures, no lint
errors.

**And I have now seen it on a screen**, which I could not manage in two earlier attempts. The
mistake before was trying to fake an Arctic GPS position. The app has a city search built in,
so I simply typed **Longyearbyen** — which is still in permanent daylight in August — and the
notice appeared. On the default method it reads *"worked out from latitude 45° instead"*; after
switching to Moonsighting in Settings it changes to *"latitude 60°"*, exactly as intended. It
also survives being run entirely right-to-left, which is how it will look once someone
translates the app into Arabic or Urdu.

**What is still not verified:** the same situation on the **watch**, which does not show this
notice at all. That is a design question — the screen is tiny — rather than something that is
broken, and it is written down.

**Should this be uploaded now, or wait?** I said last time I had not checked this properly.
I have now. **Google's own documentation makes the requirement about your testers, not about
your builds:** *"a minimum of 12 testers who have been opted-in for at least the last 14 days
continuously"*, and it warns that testers who opt out and back in do not count — *"these 14
days must be consecutive"*. There is **no statement anywhere in Google's documentation that
publishing an update restarts the count.** The clock is attached to people staying opted in.

So uploading a new build should not cost you the fortnight. What genuinely can cost it is a
tester uninstalling. Two honest caveats: Google does not say this in so many words, so it is an
absence of a rule rather than a stated permission; and every new build triggers a fresh review,
which is a small risk of a different kind. Your call — but the far-north bugs are real, they
have shipped, and they are now fixed and tested.

**A second, smaller thing was fixed at the same time**, because it costs one line of text and
helps more people than anything else in this document. See "The method nobody could find"
below.

---

## ⚠️ Your tester sent five more notes, and one of them uncovered something worse

**Reported and fixed on 1 Aug 2026.** Four were about alerts. The fifth was *"user typing
city name but its not selecting it"*, with a screenshot. Chasing that one down found a fault
nobody had reported.

### The one nobody reported: a new user could get stuck on the very first screen

If someone declines the location permission and then mistypes their city — "Manchestr", or a
place the lookup does not know — **everything on that screen disappears.** The box they typed
into, the Find city button, the message telling them it was not found, and even the "skip and
use Makkah" way out. What is left is a button they already said no to, and a Continue button
that is greyed out. There is no way forward at all except reinstalling the app.

This has been in every version, including the one your testers have right now. It was not
found by reading the code — it was found by sitting on the first-run screen and typing
nonsense into the box on purpose.

**Fixed** by showing the "or type a city" box from the start, instead of only after something
has gone wrong. That also quietly closes a complaint from the earlier round: this tester could
not find the manual city entry, because until now it only appeared once the app had failed.

### What he actually saw with "slough"

The search was working. Every time. It found Slough, saved it, and the Settings screen behind
the panel changed to say so — **but the panel stayed open with his typing still in it, and
said nothing.** From where he sat, nothing had happened. He was right to report it.

Three things were wrong and all three are fixed:

- The panel opened at half height, so the **Find city button was off the bottom of the
  screen** before the keyboard was even involved. It now opens full height.
- Nothing confirmed success. It now closes when it finds your city, so you see the new place
  name straight away.
- The little "Finding your location…" message appeared next to the wrong button. It now says
  "Looking up that place…" and sits under the box you typed in.

The search key on your keyboard now works too, so you do not have to reach for the button.

### "Notifications arrive late" — this one had a number

Android will not let an app set an alert for an exact minute unless the user allows it, and
from Android 13 that permission is **switched off by default**. Nobody had been asked for it
— there was only a small banner most people scroll past.

Measured on a test phone with the permission off: Android was giving itself a **one-hour
window** to deliver each prayer alert. The app's own banner said "a few minutes late". That
was wrong, and it now says the phone "may hold prayer alerts back and deliver them late",
because Android publishes no limit at all.

Two fixes. Setup now asks for the permission on the last screen, in plain English, with
Finish still right underneath so nobody is trapped. And every prayer alert now uses the one
kind of alarm Android promises never to move — previously only the loud "alarm" mode did, and
ordinary notifications used a weaker one that the system is explicitly allowed to delay.

The one visible cost: a small alarm-clock icon in your status bar. It is honest — an alarm
genuinely is set — and a prayer alert arriving after the prayer has started is worse.

### "If the phone is silent the alarm should not ring"

He is right, and it was doing the opposite on purpose — Android deliberately lets alarms
sound through a silenced phone, because that is what you want from a wake-up alarm you set
last night. It is not what you want from something that goes off five times a day.

Now, if your phone is on silent or vibrate, the alarm **stays quiet and the notification still
arrives on time**. There is a switch for anyone who wants the old behaviour, because someone
relying on this to wake for Fajr may well want it to ring regardless. The switch says exactly
what each choice means.

### "Let the user configure the alarm"

The old settings had two separate rows — *which prayers* and *how you are told* — and between
them they could not say the obvious thing: **a loud alarm for Fajr, a quiet notification for
the rest.** They are now one row. Each prayer gets Off, Notification or Alarm.

Nothing changes for anyone who never opens Settings: all five prayers, quiet notification,
exactly as before.

### What is still not fixed, and I am not going to pretend otherwise

Android puts apps you rarely open into a restricted category, and an app in the worst of
those is allowed **one alarm a day**. This app is exactly the kind that gets put there,
because the notification *is* the point — you do not need to open it.

There is one official way out, a permission reserved for alarm-clock and calendar apps. A
prayer app arguably qualifies. **Arguably is not good enough**: if Google disagrees at review,
the release is blocked, and we have already waited a fortnight. So it was checked, written
down, and left alone.

---

## First, what we are waiting for

The app is **live on the closed testing track** and approved. It is not in the store.

**The twelve-tester bar is cleared and the fourteen days are running.** Confirmed in the
Console on 1 Aug 2026 — the production-access checklist now shows:

| Criterion | State |
|---|---|
| Publish a closed testing release | ✓ done |
| **Have at least 12 testers opted-in** | **✓ done** |
| Run with at least 12 testers for at least 14 days | ○ in progress |

**Apply for production** stays greyed out until the fortnight completes.

**One thing to know, because it is a trap.** The email list holds **24 addresses** — that is
not the opt-in figure, and it never was. Google used to print the real count as a line of
italic text; the moment you pass twelve it replaces that line with a tick and **stops showing
the number entirely**. So for the whole fourteen days — the only period when dropping below
twelve would matter — there is no counter to watch and no warning if it slips. Nothing can be
done about that from inside the Console; the only defence is keeping your own list of who
confirmed they installed and left it installed.

Nothing in the code is blocking. See [`RELEASING.md`](RELEASING.md) Step 8.

**Why we are not shipping fixes now.** Every new build triggers another Google review and
another chance to disturb a run that has not begun. None of the three fixes below is urgent
enough to risk that. They are written up in full so the work can start the day the gate
clears, not so it can start early.

> **This was reversed on 2 Aug 2026, and only for one build.** The live version turns out to
> skip Dhuhr's alert entirely — a defect that costs a user a prayer notification every single
> day, which is itself a reason to uninstall, and uninstalling is the one thing that actually
> resets a tester's fourteen days. Google's rule counts **tester opt-in days** and says nothing
> about uploads, so a new build does not restart the clock. The repo is therefore at
> `versionCode` 3 waiting to go up mid-window, and the reasoning is set out in full in
> [`PRODUCTION_READINESS.md`](PRODUCTION_READINESS.md) §3 and §7. The paragraph above still
> holds for everything that is merely an improvement.

---

## The three fixes, in the order users feel them

### 1. Isha does not match UK mosques

**The problem.** For a user in Slough, the app says Isha begins at **23:17**. All three of
his local mosques say about **22:00**. The app is 78 minutes late.

**Why.** Nothing is broken. Isha begins when the twilight goes, and there are two ways to
define that — a fixed sun angle, or the observed fading of the redness (*shafaq*). The app
defaults to Muslim World League's 17° angle. Every mosque measured uses the *shafaq*-based
Moonsighting convention. At Britain's latitude those two answers are over an hour apart.

**Measured, not assumed.** Both mosques were identified exactly: Reading Mosque matches
Moonsighting with Hanafi Asr, JMIC Slough matches Moonsighting with standard Asr — Asr agreed
**to the exact minute** in both cases, on a value that differs by 67 minutes between the two
madhabs. Full tables in `HANDOVER.md` §10, "Isha in the UK".

**What already works today, with no update at all:** Settings → Calculation method →
**Moonsighting Committee**, then set the madhab the mosque follows. That reproduces both
mosques to within a minute. The option ships already. It is simply unreachable for anyone who
never opens Settings, because the first-run flow asks for sect and madhab and never mentions
method.

**But this affects far fewer people than it first looked, and that was checked before
anything was planned.** The default was tested against the locally-dominant method in fifteen
major cities. **It is within about five minutes across South Asia, South-East Asia, the
Middle East and Africa** — which is where most of the world's Muslims live. It only fails in
the West, and only badly above about **45° of latitude**:

| Where | How far out the default is |
|---|---|
| Jakarta, Karachi, Dhaka, Delhi, Cairo, Kuala Lumpur | 3–6 minutes |
| Istanbul, Casablanca, Kano, Tashkent | exact |
| Riyadh | 7–15 minutes |
| Chicago | 11–18 minutes |
| **London, Berlin** | **20–47 minutes** |

**So we are not changing the default and not adding any new methods.** That table was written
before the label was fixed; Indonesia's method was in the app all along, just under the wrong
name — see "The method nobody could find" below. Malaysia's 18° is the same angle the default
already uses.

**And no per-prayer adjustment setting is needed either** — which reverses what this file
said earlier today. When I ran the app's own engine rather than trusting the reference API,
SajdaTime on Moonsighting reproduces JMIC Slough **to within one minute on all six prayers,
matching Dhuhr and Maghrib exactly**. The small offsets I had put down to mosques adding a
safety margin turn out to be part of the method itself. There is nothing left for an
adjustment feature to fix.

**⚠️ What remains is one decision, and it is yours because it is religious, not technical:**

| Option | What it does | Cost |
|---|---|---|
| Leave it; explain it in Settings | Keeps a convention that is right for most of the world | Free, but northern users stay mismatched |
| **A one-time note for users above ~45° latitude**, offering the method choice | Reaches exactly the people affected, changes no time silently, adds nothing for everyone else | Small: one card that only appears when relevant |
| Ask everyone the method during first-run | Makes the existing option reachable | Small, but lengthens the flow for people who do not need it |
| Switch method automatically by latitude | Fixes it for everyone silently | **Rejected.** The app would adopt a religious position for you without saying so |

The one-time note is now the strongest. The objection to the rejected option was never that
a smart default is wrong — it is that a **silent** one is. Showing you the choice is not the
same as making it for you.

### 2. Automatic location shows the county, not the town

**The problem.** It shows "Berkshire" instead of "Slough, Berkshire, UK".

**What it is not.** It is **not** making the times wrong, and this was measured before
anything was planned, because the obvious fix would have bought nothing:

| Where the app thinks you are | Isha |
|---|---|
| Slough, exact | 21:59 |
| 3 km away | **21:59 — identical** |
| Newbury, 60 km, the far side of the county | 22:01 |

A kilometre is worth about **five seconds** of prayer time here. Being wrong by the whole
width of Berkshire costs two minutes. The hour-plus error the tester was feeling is fix 1.

**What is actually wrong.** The code takes the first name it finds and stops, so it can only
ever show one word — never "Slough, Berkshire, UK". And when it asks for the town and gets
nothing back (common for unitary authorities like Slough) it silently falls back to the
county with no sign that it has. Two fields that usually survive when the town does not are
not being checked at all.

**The harm is trust, not accuracy.** A user shown his county assumes the app cannot find him
and stops believing times that are correct to the minute.

**The work, smallest first:**

1. Check the two missing name fields before falling back to the county.
2. Build the label from what survives — "Slough, United Kingdom" rather than one word.
3. Add one honest line underneath: *"Approximate location is enough — prayer times change by
   under a minute within a few kilometres."*
4. Make typing your own town obvious. **It already exists and already works** in Settings —
   it is just invisible to anyone who has stopped trusting the automatic name.

**Ruled out: precise GPS location.** It would cost privacy and buy zero minutes.

### 3. The first-run screens do not say which button to press

**The problem, as reported by a tester:** you cannot tell whether to tap a choice or press
Next.

**Confirmed in the code**, and it is worse than reported. Of the five first-run steps:

| Step | Behaviour |
|---|---|
| Welcome | Has a **Begin** button ✅ |
| Location | Has a **Continue** button ✅ |
| Sect | **No button.** Tapping a choice jumps you forward |
| Madhab | Tapping a choice jumps you forward — **while also showing Back and Skip** |
| Confirm | Has a finish button ✅ |

Madhab is the worst case: the screen visibly has buttons, so people reasonably wait for a
Next that does not exist, then tap a card and are thrown forward before they have decided.

**The fix is consistency, not cleverness.** Selecting only selects. Every step gets an
explicit forward button.

---

### The method nobody could find — fixed on 1 Aug, one line of text

Not a tester report. It came out of checking the whole world instead of one town.

**The problem.** Indonesia and Singapore use the same convention for Fajr and Isha — their
religious authorities are Kemenag and MUIS. **The app has always calculated that convention
perfectly.** It was in the list. It was just called *"Singapore"*.

So around **240 million Muslims** had no reason to ever select it, and stayed on the default.

> **I got part of this wrong this morning and it went out.** The first version of the label
> also said Malaysia. I had checked it against a well-known prayer-times website, which agreed
> exactly — but that website is out of date: Malaysia changed its official Fajr from 20° to 18°
> back in **November 2019**, and the website never updated. A reviewer caught it. I have since
> checked Singapore against **MUIS's own printed 2024 timetable** rather than a website, and it
> holds up on all four dates I tested. Malaysia has been removed from the label. Malaysian
> users are correctly served by the default, which already uses 18°.
>
> The lesson is written down: checking against another calculator only proves the *sums* match.
> It proves nothing about what a country actually does today. That needs the country's own
> published timetable.

**Why that mattered, and it is not cosmetic.** On the default, Fajr in Jakarta comes out
**eight minutes later** than the Indonesian national timetable. During Ramadan that is eight
minutes in which someone believes they may still eat, and their own country's timetable says
the fast has already begun. Being *late* on Fajr is the dangerous direction. Every earlier
check had looked at Isha, so nobody had noticed.

**The fix.** The entry is now called **"Kemenag & MUIS — Indonesia, Singapore"**. No new
calculation, no new code. Just a name people recognise. (Malaysia is deliberately absent —
see the correction above; Malaysian users are already served correctly by the default.)

**Checked properly, the second time:** for Singapore, against **MUIS's own printed 2024
timetable** on 1 January, 21 March, 21 June and 23 September — Subuh matches to within a
minute on all four, and Isha matches exactly. For Indonesia, against Kemenag's method at
Jakarta, Medan and Surabaya, plus Kemenag's own public statement that it still uses 20°.
There is now a test that fails the build if a library update moves those times and makes the
label untrue, and a second test pinning Malaysia to the default instead.

**Worth knowing about the default generally.** Compared against each region's own official
method, the app's default is 0–10 minutes *later* on Fajr almost everywhere in the world, and
earlier essentially nowhere. It is still a sound default — for most of the Ummah it is within
about five minutes and it only goes badly wrong above 45° latitude — but that consistent
lateness is the argument for making the method easier to find, which is what fix 1 is about.

---

## The order we actually build them

Different from the order above, on purpose:

1. **Fix 3 first** — the first-run navigation.
2. **Then fix 1** — if a method question is added, it lands on those same screens. Fixing the
   navigation first means not doing it twice.
3. **Then fix 2** — independent of both, can slot in anywhere.

The far-north work is already done and in the repository — it is not waiting on anything, and
neither are the five fixes from the second round of tester feedback above.

**A correction to something this file used to imply.** It read as though no work should happen
until the fortnight was over. That was muddled. Nothing reaches Google until a build is
*uploaded* — writing the code, testing it and saving it to the repository costs the closed
test nothing at all. So the fixes are done and waiting; only the upload is on hold.

Then: one update to the closed track, one Google review, and **Wear OS after the phone
reaches production** — not before, because it triggers a second review for no gain.

---

## The checklist for the day the fortnight ends

In order. Nothing here needs a decision from you except where it says so.

**Before touching the Console**

1. Run the full gate: `./gradlew clean test lint :app:bundleRelease :wear:bundleRelease`.
2. Bump `versionCode` and `versionName` — see [`RELEASING.md`](RELEASING.md). The phone and
   watch bundles share an application ID, so their version codes must not collide.
3. Run both emulators and compare them. Run `./gradlew installRtl` for anything visual.
4. Confirm the release is signed with **your** key. No agent has it and none ever will.

**In the Console**

5. Check the production-access checklist actually shows the fourteen days complete. If it
   does not, nothing below matters yet.
6. **Apply for production.** The form asks about the closed test, the app, and readiness.
   Google say the review *"usually takes 7 days or less"*.
7. While waiting, do **not** push another build unless something is broken.

**Decisions that were yours — settled on 2 Aug 2026**

You said *"lets go with your recommendation"*, so these are now decided and written down. If a
later session proposes reopening one, the reasoning is in `HANDOVER.md` §11.

8. ~~The one-time note above ~45° latitude (A2).~~ **Built and shipped.**
9. **Alarms on far-north days (A10) — decided and built.** On days the app has to work the
   times out from a lower latitude, an alarm now arrives as a quiet notification instead of
   ringing. It still comes on time and still says the time is approximate; it just does not
   wake anyone for a minute the app cannot vouch for.

   **There is a switch, and the switch matters more than the rule.** In a place like Tromsø
   *every* day from late May to late July is worked out that way, so without a way to turn
   ringing back on, someone's Fajr alarm would quietly stop working for two months a year —
   and they would only find out by not waking up. So: quiet by default, one switch to turn it
   back on, and the switch is only shown to people whose own town actually has such days.
   Nobody in Britain, Pakistan, Indonesia or anywhere near the equator ever sees it.
10. **Sect and calculation method (A1), the method on the home screen (A3), the high-latitude
    rule as a setting (A4), Morocco and Wifaqul Ulama (A6) — all deferred, on purpose.** The
    problem each was raised to solve is now handled by the far-north note and by "Match your
    mosque". What is left of them would add settings and questions for everyone in order to
    tidy something most users will never notice. Morocco can be added any time someone wants
    it; Wifaqul cannot, because we would be copying a number from a review rather than from
    the authority's own document, and this project does not do that.
11. **Ramadan wording for Fajr (A11) — deferred, in your words**: we are aiming at salah for
    now, and suhoor is a fasting question. Worth picking up before the first Ramadan the app
    is live for.
12. **The exact-alarm permission (A14) — not before production.** Google names exactly two
    kinds of app that may use it, an alarm/timer app and a calendar app, and says everything
    else *"will be disallowed from publishing on Google Play"*. Muslim Pro getting away with
    it proves it can pass, not that we would. Once the app is live, a rejection costs a
    version instead of the launch, and it can be tried properly then.
13. **The disclaimer now says four things instead of two (A15) — your instruction, 2 Aug.**
    You said the app should tell people the times come from a calculation, that they should
    follow their mosque if there is a contradiction, and that you do not want to be held
    accountable. It now says all three, plus one more that was missing and matters:

    - **Where the times come from.** The app never actually said this before. Someone could
      reasonably have assumed the times were supplied by a mosque or a scholar. They are not —
      your phone works them out from the position of the sun. Now it says so.
    - **How it can go wrong.** Not just "it may get things wrong", which is the line every app
      carries and nobody reads, but the actual ways: a fault in the software, a wrong location,
      a wrong clock, or me being wrong.
    - **Follow your mosque.** It used to end that paragraph with *"It is your choice to make"*,
      which is true but unhelpful — it hands someone a decision without helping them make it.
      It now says plainly that where the app and the mosque disagree, the mosque wins. That is
      not the app taking sides in fiqh; deferring to a real scholar over a piece of software is
      the humble answer, and it is the safest one for you.
    - **No warranty.** That it is free, given as it is, with no promise of accuracy. Said once,
      in plain English, in the same voice as the rest of the screen — not in legal small print,
      which would be read even less than the rest.

    The same wording is now in four places, so someone who never installs the app can still
    read it: the first-run screen, the watch, the privacy page on the website (under its own
    "Disclaimer" heading, which the front page links to), and the Play Store description. The
    dua request stays where it always was — the first-run screen only, asked once, never
    repeated.

14. **Both of these are now closed** (2 August), and neither needed a decision from you in the
    end.

    - *The watch and the far-north notice.* It turned out the watch was not saying anything at
      all about approximate times, which was simply a gap rather than a design question. It now
      carries one line under the countdown: *"Approximate. Here the sun does not rise or set
      today, so these come from 45°. Ask your mosque."*
    - *The Moonsighting question.* This had been waiting for a published timetable that nobody
      could get hold of. It turned out their website does publish one — it just builds the page
      in a way the earlier tool could not read. **Their times match SajdaTime to the minute**,
      so the app has been right all along, and the "fix" that was built and thrown away would
      have made it worse by up to an hour and a half. Their figures are now locked into the
      tests so nobody rebuilds it by mistake.

**After production is granted**

15. Then, and only then, the Wear OS release.

---

## What we check before saying it is done

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

Then, because compiling is not working:

- Run it on the phone **and** watch emulators and compare them.
- `./gradlew installRtl` — check every changed screen right-to-left before it ships.
- Re-verify times against Aladhan, and **on two dates, December and June**, not one. See the
  open question below.
- Retake `docs/store/screenshots/` if anything visual changed.

## The honest gaps

- **The seasonal question is now closed — this no longer gates fix 1.** The worry was that
  UK mosques often change rule for the ten weeks around midsummer, when the normal Isha
  calculation stops working at this latitude. If ours did, "set it to Moonsighting" would
  have been right in August and wrong in June. Reading Mosque's own monthly timetable was
  read for June and July: on **21 June, the solstice and the hardest date of the year, Fajr,
  Asr and Isha all match to the exact minute**, and across five dates spanning ten weeks the
  largest disagreement is one minute. They do not switch. Both sides are now verified — the
  app reproduces the convention correctly all year, and the mosque genuinely uses it all
  year. **Still inferred rather than measured:** the same check for JMIC Slough, whose
  timetable is a PDF behind bot protection. It matched exactly on 1 August, and the mosque
  26 km away holds the convention through the solstice.
- **Diamond Road publishes no start times** anywhere found, so only its congregation column
  could be read. Its apparent 47-minute disagreement with JMIC is almost certainly this and
  nothing more.
- **Three mosques in two towns is a small sample** for a decision about a default. It is
  enough to prove the current default is wrong for this user; it is not enough to prove which
  one is right for Britain.

## What we are deliberately not doing

- Not machine-translating anything. Prayer and madhab names are religious content.
- Not adding precise location. Zero minutes of benefit, real privacy cost.
- Not calling any API at runtime to fetch times. The app works with no signal, permanently.
- Not silently shifting times to close the gap between the app and a mosque noticeboard —
  that gap is usually the difference between when a prayer becomes due and when the
  congregation is held, and it is supposed to be there.

---

*Made with love, free for the Ummah.*
