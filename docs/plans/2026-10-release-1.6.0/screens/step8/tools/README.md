# Step 8 picture tool (never compiled into the app)

Draws the strip, the Next prayer widget and the Today widget through the real `WidgetRenderer` into
`AppWidgetHostView`s of exact dp sizes (the host picks the `SizeF` entry that fits, as a launcher does), on a neutral
background, with captions. Nothing on the phone is changed: the instant (London, 26 Oct 2026 unless a scene says
otherwise, Ramadan scene 22 Feb 2027), the text size (fontScale on a configuration context), the language (`ur`) and
the Theme row (Light or Dark, so the fixed layouts) are all set in code. It sets and resets the TEMPORARY
`WidgetRenderer.stripRamadanWords` and `stripGlyphs` switches around each cell. It does save London as the app's
location (the app needs one to compute times).

To use, copy `WidgetPicturesTmp.kt.txt` to `app/src/androidTest/java/com/sajdatime/app/WidgetPicturesTmp.kt`, then:

    export ANDROID_SERIAL=emulator-5554
    ./gradlew :app:installDebug :app:installDebugAndroidTest
    adb shell am instrument -w -e class com.sajdatime.app.WidgetPicturesTmp [-e pic 07] \
      com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner
    adb pull /sdcard/Android/data/com.sajdatime.app/files/step8 OUTDIR

`pic` is a filename prefix filter (omit for all). Scenes are the `scenes()` list: edit sizes, times or captions there.
Delete the copy afterwards and never commit it. Picture 13 (the widget picker) is not drawn by this tool.
Not a launcher: no launcher corner clipping, inset or wallpaper blur (see the report in the commit message).
