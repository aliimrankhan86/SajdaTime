# Step 6 picture tool (never compiled into the app)

The emulator's console sensor commands cannot turn its compass (HANDOVER §15 lesson 182), so the Qibla pictures are
rendered by a temporary instrumentation test. To use: copy `QiblaPicturesTmp.kt.txt` to
`app/src/androidTest/java/com/sajdatime/app/QiblaPicturesTmp.kt`, install the debug app and test APK, then per picture:

    adb shell am instrument -w -e class com.sajdatime.app.QiblaPicturesTmp -e pic NAME [-e heading 60] [-e dark 1] \
      [-e scale 2.0 -e narrow 1] [-e noloc 1] [-e open 1] [-e acc low|none] [-e centre 1] \
      com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner
    adb pull /sdcard/Android/data/com.sajdatime.app/files/NAME.png

Before reuse after `5f56fba`: the `flat` flag's switch (`qiblaFlatDial`) is gone; replace it with flags for
`qiblaSandBezel`, `qiblaShowArc` and `qiblaDarkBezelBright`. Urdu or Arabic: `adb shell cmd locale set-app-locales
com.sajdatime.app --locales ur`, then back to `en`. Delete the copied test afterwards and never commit it: it sets the
TEMPORARY switches, which are process globals, and would leak into other device tests.
