# Back-translation: Urdu (ur-PK) into English

Written 8 Oct 2026 by an independent reviewer model, **from the Urdu files alone, before reading
the English sources or the translator's notes.** The point is to show what an Urdu reader actually
receives, so it is deliberately literal. It is not a proposed English text. Short labels whose
meaning is unambiguous (Back, Cancel, Settings, prayer names) are listed briefly at the end.

Files read: `app/src/main/res/values-ur/strings.xml`, `core/src/main/res/values-ur/strings.xml`,
`wear/src/main/res/values-ur/strings.xml`.

---

## Disclaimer (`disclaimer_title`, `disclaimer_body`)

- `disclaimer_title`: Please read this first.
- `disclaimer_body`, paragraph by paragraph:
  1. SajdaTime is a helper, not any religious authority.
  2. These times were not given to me by any mosque, any scholar (aalim) or any religious
     institution. Your phone works them out itself from the position of the sun, by means of the
     calculation methods that Islamic institutions and respected scholars have published. I am
     neither a Mufti, nor an Aalim, nor an expert in fiqh, and this app was built with the help of
     artificial intelligence. The software can have a fault, the phone can have a wrong location
     or a wrong time, a battery-saving phone can delay an alert, and, to put it plainly, I myself
     can also be wrong.
  3. So please treat whatever appears here as a calculation, not a religious (sharia) ruling.
     Where SajdaTime and your mosque differ, follow your mosque. If a time or direction seems
     wrong to you, or you are not sure, ask them or some other qualified person. This app is free
     and is given just as it is, there is no warranty and no promise of accuracy of any kind, so
     where being exactly right matters to you, please do not rely on it alone.
  4. If you use a watch, please treat it as a convenience, not the final word. The watch's
     compass is smaller than the phone's and it also gets less sky (satellite) signal than the
     phone, and it may be running on a location your phone worked out a while ago. Especially for
     the Qibla, of the two your phone is more accurate. When it matters, look there.
  5. Not all mosques agree on when prayer begins. The biggest difference is in Isha. In Britain
     and further north than it, the difference between one method and another can be more than an
     hour, because scholars measure the disappearance of twilight (shafaq) in different ways. I
     cannot decide this, and this app does not try to either. If the times here do not match your
     mosque, ask them which method they adopt and choose the same under "Calculation method" in
     Settings. If after that a difference of a few minutes still remains, through "Match your
     mosque" you can move each prayer, and the date, forward or back yourself. Where a difference
     remains, follow your mosque.
  6. One more thing. This app shows when the time of a prayer enters. Then your mosque sets its
     own time for the congregation (jama'at), usually a little later, so there will often be a
     difference between the board there and the times here. Neither of the two is wrong.
  7. And one request, if you think it fitting: please remember me, my family and my parents in
     your duas.
- `about_disclaimer`: Important clarification.
- `about_disclaimer_short`: A helper, not a religious authority. Tap to read.
- `wear_disclaimer` (watch): Is a helper, not a religious authority. Times are from calculation.
  Follow your mosque.

## Consent and privacy

- `consent_title`: Will you help make SajdaTime better?
- `consent_body`:
  1. SajdaTime is free and has no adverts in it. If you say yes, it sends a few usage figures
     (statistics) to Google Analytics so the app can be improved. These are linked to a random
     identification number, not to your name, and their processing can also happen outside
     Britain. The detailed record is kept for 14 months.
  2. Using a prayer app can reveal something about your belief, so this is your own decision, and
     if you say no nothing changes in the app. SajdaTime never sends your GPS position or your
     prayer settings.
  3. Until you choose yes, nothing is sent. You can turn it off at any time in Settings. The
     person responsible for this is Ali Imran Khan, who made this app.
- `consent_policy_english`: The full privacy policy is in English.
- `consent_see_sent`: See exactly what is sent. `consent_hide_sent`: Hide details.
- `consent_sent_heading`: What is sent if you say yes.
- `consent_sent_list`:
  - Which main screen you open: Times, Qibla or Settings
  - Which steps of setup you reach
  - Whether you allow notifications, location and exact-time alarms (only yes or no for each,
    never the location itself)
  - How often and for how long the app is open
  - Your phone's model, Android version, the app's version and language
  - When you first opened the app, and how you reached the app (for example from a link on
    Google Play)
  - Your approximate area, which Google works out from your internet connection
- `consent_never_heading`: What is never sent.
- `consent_never_list`:
  - Your GPS position or location (coordinates)
  - The place you type
  - Your school of thought, your prayer-calculation setting, your calculation method and your
    alert settings
  - Anything at all that you have typed
- `consent_yes`: Yes, send usage figures. `consent_no`: No, thank you.
- `consent_read_policy`: Read the privacy policy.
- `about_privacy_desc`: Your location stays on this phone. No account, no adverts. Usage figures
  are optional and stay off until you turn them on. Tap to read the full policy.
- `settings_usage_counts`: Send usage figures.
- `settings_usage_counts_desc`: Sent to Google, linked to a random identification number. Stays
  off until you turn it on. When you turn it on, it is shown exactly what is sent.

## Location and permission

- `permission_title`: Where do you pray?
- `permission_body`: To work out your prayer times and the Qibla direction, SajdaTime only needs an
  estimate of roughly where you are. To know exactly what this means, tap the information sign.
- `permission_why_title`: Why we need your location.
- `permission_why_body`: So that correct prayer times and Qibla direction can be worked out for
  your location. / We only look at your approximate location when the app is open. It stays on
  your phone. We never send it anywhere.
- `location_no_fix`: Location could not be found. Check that the phone's location is on, or write
  your city's name below.
- `city_fallback_body`: Write your city's name instead. Where possible, your phone finds it itself.
  If it cannot, only the name you wrote is sent, once, to a free search service. After that
  everything is worked out on your phone.
- `city_not_found`: This place could not be found. Try writing the country name with it too.
- `action_use_makkah`: Skip for now, use Makkah al-Mukarramah.
- `location_sheet_body`: Both the prayer times and the Qibla depend on where you are. Whenever you
  travel, update it. / To protect your privacy the app always asks only for your approximate
  location, so it may show the name of a nearby town or a wider area instead of your own town.
  This makes a very small difference to the times: a difference of ten miles is about one minute.
  If the name looks wrong to you, write your city's name below.
- `home_default_location_title`: Showing Makkah al-Mukarramah's times.
- `home_default_location_body`: We could not find out where you are.

## Exact alarms, Do Not Disturb, alert styles (the "quiet" vs "silent" family)

- `settings_alerts_help`: Choose this separately for each prayer. **A notification is silent and
  vibrates.** An alarm plays your chosen sound loudly, like a morning wake-up alarm.
- `settings_alarm_sound_desc`: Choose any tone or adhan already on your phone.
- `settings_alarm_respect_silent`: When the phone is on silent, the alarm also stays silent.
- `settings_alarm_respect_silent_desc`: On: the notification still arrives on time, but without
  sound. Off: the alarm rings even when the phone is on silent, like a morning wake-up alarm.
- `settings_dnd_title`: Allow sound during "Do Not Disturb".
- `settings_dnd_desc`: Without this, if the phone is on "Do Not Disturb" your alarm stays silent.
- `settings_ongoing_badge`: Keep showing the next prayer in notifications.
- `settings_ongoing_badge_desc`: A silent notification that tells you which prayer is next and how
  much time is left.
- `settings_exact_alarms_title`: Allow exact-time alarms.
- `settings_exact_alarms_desc`: Without this your phone can hold back prayer alerts by an hour or
  more, and on some phones a night alert, such as Fajr's, may not come at all.
- `confirm_exact_alarm_title`: One last thing.
- `confirm_exact_alarm_body`: Android does not let apps set alerts at the exact minute unless you
  allow it. Without this your phone can hold back prayer alerts by an hour or more, and on some
  phones a night alert, such as Fajr's, may not come at all. You can also do this later from
  Settings.
- `channel_prayers_desc`: A notification at the start of each prayer.
- `channel_alarm_desc`: A loud alert with the sound you chose.
- `channel_ongoing`: Next prayer badge ("بیج", which is also the ordinary Urdu word for "seed").
- `channel_ongoing_desc`: A silent reminder of the next prayer, always present in notifications.

## Polar / approximate times

- `polar_notice_title`: Approximate times.
- `polar_notice_body`: This far from the equator the sun does not always rise or set, so there is
  no real Fajr or Isha to calculate. Instead these times have been worked out from %1$d° latitude.
  Mosques here deal with this in different ways, so ask your mosque which one it follows.
- `polar_notice_alarm`: On these days your alarms come **as silent notifications** instead of
  ringing, because the app cannot guarantee the exact minute. If you still want to be woken, turn
  on "Ring on approximate days" in Settings.
- `pdf_approximate_note`: Some of these times are approximate. This far north the sun does not
  always rise or set, so for those days the times have instead been worked out from %1$d°
  latitude. Ask your mosque which it follows.
- `settings_alarm_on_approximate`: Ring on approximate days.
- `settings_alarm_on_approximate_desc`: When the sun does not rise or set, the times are worked out
  from a lower latitude. On those days alarms stay silent unless you turn this on.
- `notif_approximate`: Approximate time.
- `wear_polar_notice` (watch): Approximate. Here today the sun does not rise or set, so these are
  from %1$d° latitude. Ask your mosque.

## Calculation methods (`core`) and the method step

- `method_auto_desc`: Not sure? Leave it as this. Muslim World League times, or Jafari for Shia.
  Close to local practice in most parts of the world. The far north makes the most difference.
- `method_muslim_world_league_desc`: A common method that is in use in many countries. **The
  standard method here.**
- `method_egyptian_desc`: The official method of Egypt.
- `method_karachi_desc`: Commonly in use in Pakistan and South Asia.
- `method_umm_al_qura_desc`: The official method of Saudi Arabia. Isha is placed at a fixed
  interval after Maghrib.
- `method_dubai_desc`: A method in use in the Gulf.
- `method_moon_sighting_desc`: A seasonal rule made for regions far from the equator. Some mosques
  in Britain follow it.
- `method_north_america_desc`: Islamic Society of North America. Many mosques in America and Canada
  adopt it.
- `method_kuwait_desc`: The method in use in Kuwait. `method_qatar_desc`: The method in use in Qatar.
- `method_singapore_desc`: The official bodies of Indonesia and Singapore.
- `method_turkey_desc`: Turkey's Directorate of Religious Affairs.
- `method_jafari_desc`: The common method of Shia people. Maghrib is a little after sunset, when
  the redness has gone from the sky.
- `method_tehran_desc`: Tehran University, on whose calendar Iran's official times run.
- Method names with Urdu around them: `Umm al-Qura، مکہ مکرمہ` (Umm al-Qura, Makkah),
  `ISNA، شمالی امریکہ` (ISNA, North America), `Kemenag & MUIS، انڈونیشیا اور سنگاپور`
  (… Indonesia and Singapore), `Diyanet، ترکی` (Diyanet, Turkey), `Institute of Geophysics، تہران`
  (…, Tehran). Others left in English.
- `method_step_title`: Calculation method.
- `method_step_body`: Mosques measure morning and evening twilight in different ways, so Fajr and
  Isha can differ by a few minutes from one method to another, and in the far north by more than an
  hour. If your mosque's times differ from the app, usually choosing its method here sorts it out.
  Not sure? Leave the first option. You can change it at any time in Settings.
- `settings_method_help`: Mosques measure twilight in different ways, and this mainly moves Fajr and
  Isha forward or back. The further north you live, the bigger the difference. If these times do
  not match your mosque, ask them which method they adopt and choose the same here.
- `method_notice_title`: Does this match your mosque? `method_notice_body`: Here Fajr and Isha can
  differ by an hour or more. `method_notice_action`: See why.

## Sect and madhab

- `sect_title`: Which school of thought do you belong to?
- `sect_body`: This decides how your prayer times are worked out. You can change it whenever you
  want.
- `sect_sunni_desc`: By default, Muslim World League times. `sect_shia_desc`: Jafari (Ithna Ashari)
  times.
- `madhab_title`: Choose your fiqh.
- `madhab_body`: Optional. This only changes the Asr time. If you skip, you will get the standard
  time.
- `madhab_hanafi_desc`: Asr later, when the shadow becomes twice the object's length.
- `madhab_standard_desc`: Standard Asr, when the shadow becomes equal to the object's length.
- `wear_madhab_note` (watch): Only Asr changes. Hanafi is later.

## Match your mosque / "different from your mosque"

- `mosque_diff_link` / `mosque_diff_title`: Different from your mosque?
- `mosque_diff_congregation`: This app shows when the time of each prayer enters. Your mosque usually
  holds the congregation a little later, so there will often be a few minutes' difference between
  its board and these times. Neither of the two is wrong.
- `mosque_diff_twilight`: Fajr and Isha depend on how twilight is measured, and mosques measure it
  in different ways. If your mosque differs by more than a few minutes, choose the method it
  follows.
- `mosque_diff_asr`: Asr depends on fiqh. Hanafi mosques start Asr later than others, by up to an
  hour or even more.
- `mosque_diff_minutes`: If a few minutes' difference still remains, move each prayer forward or
  back a little and match it to your mosque's printed times.
- `mosque_diff_follow`: Where a difference remains, follow your mosque.
- `settings_adjustments`: Match with your mosque. `settings_adjustments_none`: No change.
- `settings_adjustments_help`: If your mosque prints a different time, move it forward or back a
  little here, or tap a number and choose the time your mosque shows. Try the calculation method
  first. The real difference is usually that, and this is for the few minutes left over.
- `adjustment_limit_note`: A difference of more than %1$d minutes is usually because of a different
  calculation method, or because the mosque board shows the congregation time, which is a little
  later. Try the method first.
- `adjustment_choose_method`: Choose calculation method. `adjustment_now_shows`: Shows: %1$s.
- `adjustment_pick_time_description`: %1$s: set the time your mosque shows.
- `adjustment_pick_time_title`: %1$s: the time on your mosque's board.
- `adjustment_summary_times`: %d prayer adjusted / %d prayers adjusted.
- `adjustment_summary_hijri`: Date %1$s.
- `action_decrease` / `action_increase`: Decrease %1$s / Increase %1$s (with a prayer name this
  reads "reduce Fajr", "increase Fajr").
- `settings_hijri_offset`: Islamic date. `settings_hijri_offset_label`: Move the date forward/back.
- `settings_hijri_offset_help`: Here the Islamic date has been derived by calculation. If your
  mosque sights the moon locally, it can be one day ahead or behind. This also moves Ramadan and
  Eid forward or back.

## Qibla

- `qibla_subtitle`: The Kaaba is about %2$d kilometres from here, at %1$d° from north.
- `qibla_needs_location`: Set your location to find the Qibla.
- `qibla_facing`: Now you are facing towards the Kaaba.
- `qibla_heading_now`: Facing: %1$d°.
- `qibla_legend_kaaba`: Kaaba. `qibla_legend_facing`: Your facing.
- `qibla_turn_left`: Turn left %1$d°. `qibla_turn_right`: Turn right %1$d°. (Physical instruction,
  correct.)
- `qibla_no_compass`: **Facing:** %1$d° from true north.
- `qibla_calibrate`: The compass needs correcting (calibrating). Holding the phone in your hand,
  turn it a few times in the shape of an 8.
- `qibla_sensor_missing`: This phone has no compass, so we can tell you the angle but cannot point
  towards the direction.
- `qibla_accuracy_medium`: The reading may wobble a little. For a steadier result, move away from
  metal, magnets and speakers.
- Watch: `wear_qibla_needs_location`: Set your location to find the Qibla. `wear_qibla_facing`:
  Facing is towards the Kaaba. `wear_qibla_from_true_north`: From true north.

## Home, notifications, export, about

- `welcome_tagline`: Your private companion for prayer. Always free.
- `home_until`: Remaining until it starts.
- `home_now_marker_a11y`: Current prayer.
- `home_countdown_a11y`: %1$s: %2$s remaining.
- `notif_prayer_title`: It is the time of %1$s.
- `notif_prayer_body`: The time of %1$s starts at %2$s.
- `notif_next_title`: %1$s, at %2$s o'clock ("بجے").
- `notif_next_body`: %1$s remaining.
- `export_sheet_body`: We will make a PDF of your prayer times. Location: %1$s. Then you can save
  it, print it or send it to someone.
- `export_week_desc`: Today and the six days after it. `export_month_desc`: Every day from the 1st
  to the end of the month.
- `export_saved`: Saved in Downloads: %1$s.
- `pdf_tagline`: Prayer times and Qibla, free for the Ummah.
- `pdf_footer`: Calculated offline on your phone. SajdaTime is free, forever.
- `about_translation_note`: This translation was made with the help of AI (artificial intelligence)
  and may contain mistakes. If you see one, please tell us through "Send feedback".
- `about_feedback_desc`: Tell the developer what is useful or what is wrong. Your e-mail app will
  open.
- `about_charity_desc`: Made with love, free for the Ummah. A charitable project, for the pleasure
  of Allah.
- `about_credits`: Maker. `about_credits_desc`: Ali Imran Khan, as sadaqah jariyah for the Ummah.
- `about_data_desc`: Prayer times are calculated with adhan-java (MIT) by Batoul Apps. City search
  uses Open-Meteo's geocoding API, which is under the CC BY 4.0 licence.
- Watch: `tile_description`: The next prayer and how much time is left. `tile_needs_setup`: Set up
  on your phone. `wear_no_location_title`: No location yet. `wear_no_location_body`: Open SajdaTime
  on your phone, or use Makkah al-Mukarramah for now. `wear_tile_detail_approx`: About %1$s · %2$s.

## Short labels (unambiguous, one line each)

Times / Qibla / Settings (nav); Begin; Allow location; OK; I have understood; Continue; Your
location; Finding your location…; Looking up this place…; City; e.g. Manchester, Lahore, Cairo;
Find city; Sunni; Shia; Hanafi / Shafi'i / Maliki / Hanbali; Calculation method; Back; All is
ready; "Looks right? You can change all this later in Settings"; Location; School of thought;
Finish; Selected; Next prayer; Today; Next; Now; Remove (dismiss); Change location; Use my current
location; Or write a city name; Save prayer times to PDF; Save to PDF; Save your prayer times; Today
only; One day, six times; Next 7 days; This whole month; PDF could not be made, please try again;
Day; Date; From %1$s onwards; Approximate area; Settings; Appearance; Theme; As the phone; Light
("bright"); Dark ("deep"); Reminders; About the app; Fiqh; Prayer alerts; All five; %1$d of 5;
None; Automatic · %1$s; Off; Notification; Alarm; Mixed; Alarm sound; Allow in settings; Close;
Version; Privacy; Language; Choose the app's language; Not available in this version; Phone's
language; Translation; Permission given; Expanded; Collapsed; Send feedback; Feedback about
SajdaTime; Free, forever; Data and libraries; Prayer times (channel); Prayer alarm; %1$d hours %2$d
minutes; %1$d minutes; %1$d seconds; Set; Cancel; Reset all; 0 minutes; %+d minutes; %+d day(s).

Prayer names: فجر Fajr, طلوعِ آفتاب Sunrise, ظہر Dhuhr, عصر Asr, مغرب Maghrib, عشاء Isha (all
standard). Hijri months: Muharram, Safar, Rabi al-Awwal, Rabi al-Thani, Jumada al-Awwal, Jumada
al-Thani, Rajab, Sha'ban, Ramadan, Shawwal, Dhul-Qa'dah (written ذیقعد), Dhul-Hijjah (in order,
twelve items).
