# SajdaTime 1.7.0 picture and device test report

4 November 2026 · `claude/1-7-0` · tested build from `077bdc0`

## Store pictures

I opened the new [Arabic Times picture](upload/phone/ar/01-times-light.png) and [Urdu Times picture](upload/phone/ur/01-times-light.png). Both show London in light mode. Dhuhr is the highlighted Now row, and its thin green bar is on the right edge in both pictures.

I used fresh onboarding with Automatic calculation, then set London using the project's capture helper. I took the pictures at 1080 × 1920 on the store emulator, with the app set to each language in turn. I restored the app to its original language setting and switched automatic date and time back on. The store asset script copied both raw captures into the upload folders byte for byte. These were the only two pictures recaptured.

## Device suite

I installed the debug app and test app on `emulator-5554`, granted widget binding and ran the step 6b device suite once.

| Runner total | Passed | Failures | Errors | Skipped |
|---:|---:|---:|---:|---:|
| 49 | 38 | 0 | 0 | 11 |

The runner printed `OK (49 tests)`. Its individual results include 11 skips. Ten were optional widget render checks, and one was the alarm channel language check, which applies to older Android versions. The corrected Today widget check passed. No app or test code changed.

## Outside this run

The separate Ramadan Suhoor reminder test and the two capture helper classes were excluded from the suite as specified. I did not test on a real phone or check the Play listing preview. Nothing was uploaded to Google Play.
