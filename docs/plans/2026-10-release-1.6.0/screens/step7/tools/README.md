# Step 7 picture tool (never compiled into the app)

Seeds jama'ah rules and opens the real app for Ali's step 7 pictures. To use, copy `JamaahPicturesTmp.kt.txt` to
`app/src/androidTest/java/com/sajdatime/app/JamaahPicturesTmp.kt`, install the debug app and test APK, grant
notifications (`pm grant ... POST_NOTIFICATIONS`) and exact alarms (`appops set ... SCHEDULE_EXACT_ALARM allow`) so
their cards do not cover the rows, then (as root, auto time off) `adb shell date 102711002026.00` and:

    adb shell am instrument -w -e class com.sajdatime.app.JamaahPicturesTmp [-e seed 1] [-e pic NAME] [-e dark 1] \
      [-e method AUTO|MOON_SIGHTING] [-e dialog 1] [-e scroll N] \
      com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner
    adb pull /sdcard/Android/data/com.sajdatime.app/files/NAME.png

Seed first (`-e seed 1`, no pic). 823dp: `wm size 1440x3088`, `wm density 600`. 320dp: `720x1280`, `360`. Away:
`adb shell cmd alarm set-timezone Asia/Dubai`, back with `Europe/London`. Delete the copied test afterwards and never
commit it. Put `auto_time` back to 1 and `wm size reset` / `wm density reset` when done.
The `nodoor` flag was removed with the switch (25 Oct 2026, Q15 decided).
