# Play Console job for the 1.3.0 release (paste into Claude in Chrome or Cowork)

**Run this only after the device check (`docs/ANALYTICS_DEVICE_CHECK.md`) has reported PASS for the
"nothing is sent before consent" proof, the branch has been merged to `main` with the owner's OK
(so `docs/privacy.html` is live), and the signed bundle exists.** The browser agent never uploads
the bundle and never sends anything for review: those two are the owner's. Written 3 Oct 2026.
Source of every answer below: `docs/store/LISTING.md` ("Optional usage counts") and `docs/ANALYTICS_PLAN.md`.

```
I manage the Android app SajdaTime (package com.sajdatime.app) in Google Play Console, signed in as
aikstudies@gmail.com. Prepare the Console for release 1.3.0. Work alone, one screen at a time. Do only
what is listed. Change nothing else.

HARD RULES
- Do NOT click "Send for review", "Publish", "Submit", "Roll out" or any final submit or publish
  button. I do that myself.
- Do NOT upload any bundle (.aab). I do that myself.
- Do NOT accept policies or agreements for me. Stop at any that appear and tell me.
- Do NOT touch pricing, countries, app access, content rating, target audience or anything not
  listed. If a screen is not as described, STOP and describe it.
- Save each page as you go. Do not leave changes unsaved.

PART 1. DATA SAFETY (App content > Data safety > Start / Manage)
Answer, exactly:
- Does your app collect or share any of the required user data types? YES.
- Is all of the user data collected by your app encrypted in transit? YES.
- Do you provide a way for users to request that their data is deleted? NO.
Data types: tick ONLY these three and nothing else.
 1. Location > Approximate location
    Collected: yes. Shared: NO. Processed ephemerally: NO. Required or optional: OPTIONAL (users can
    choose). Purposes: tick BOTH "App functionality" AND "Analytics". Nothing else.
 2. App activity > App interactions
    Collected: yes. Shared: NO. Ephemeral: NO. OPTIONAL. Purpose: "Analytics" only.
 3. Device or other IDs
    Collected: yes. Shared: NO. Ephemeral: NO. OPTIONAL. Purpose: "Analytics" only.
Do NOT tick: App info and performance, Personal info, Financial info, Health, Messages, Photos,
Audio, Files, Calendar, Contacts, Web browsing, Crash logs, Diagnostics, or anything else.
Save, then open the "Preview" of the form and tell me exactly what the public Data safety card would
read.

PART 2. ADVERTISING ID (App content > Advertising ID)
Check the answer. It must be NO (the app has no ads and does not use the advertising ID). If it is
already NO, change nothing. If it is YES, tell me before changing it.

PART 3. STORE LISTING (Grow > Store presence > Main store listing, English (United Kingdom))
 a) Short description: replace the whole text with exactly:
    Offline prayer times (namaz) and Qibla compass for Sunni and Shia. No accounts.
    (79 characters.) Save. If the Console shows a notice about price or promotion keywords for the
    short description, copy the notice text to me and do NOT try other wordings.
 b) Full description: change ONLY these two bullet lines, nothing else in the text.
    Replace the line
      • No analytics, no crash reporting, no tracking of any kind
    with
      • Optional usage counts, off unless you switch them on. No crash reporting
    Replace the line
      • Your coordinates never leave your device — there is no server for them to go to
    with
      • Your coordinates never leave your device, and are never part of the optional usage counts
    (Keep the "—" in the old line only as a way to find it; the new line has no dash.) Save.
 c) Do not change graphics or screenshots.

PART 4. REPORT (stop here, do not send anything for review)
Open Publishing overview and tell me the list of changes waiting to be sent for review. Then report
in one message:
- Data safety: each question and the answer you saved, and the preview text of the public card.
- Advertising ID: the answer found, and whether you changed it.
- Listing: the short description as saved, the two bullet lines as saved, and any notices.
- Publishing overview: the exact list of changes ready to send.
- Anything that did not match this brief, or that you had to decide.
```

## What the owner does after that (about 5 minutes, one screen at a time)
1. Test and release > Production > Create new release (phone / "Phones, Tablets…" form factor only).
2. Upload the signed `app-release.aab` (versionCode 5, 1.3.0).
3. Release notes (en-GB): *New: an optional setting to share usage counts, so we can see how SajdaTime
   is used and improve it. It is off unless you turn it on. Your prayer times and your location are
   not affected.*
4. Check the release summary says versionCode 5 and no errors, then **Send for review** (one submission
   with the Data safety and listing changes). **That is the owner's button.**
5. Do not touch the listing or App content while the review is open. The watch release is not part of this.
