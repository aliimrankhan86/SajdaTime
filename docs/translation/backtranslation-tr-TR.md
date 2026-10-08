# Turkish back-translation (tr-TR)

Written 8 Oct 2026 by an independent reviewing model, from the three `values-tr/strings.xml` files
only, **before** the English sources were opened. It says what the Turkish actually says, in plain
English, so it can be compared with the English source. Short labels whose meaning is obvious
(Back, Cancel, Settings, Theme) are left out. The comparison and findings are in
`review-tr-TR.md`.

## app/src/main/res/values-tr/strings.xml

### Welcome, location, onboarding

- `welcome_tagline`: Your prayer helper that respects your privacy. Always free.
- `permission_title`: Where do you pray?
- `permission_body`: SajdaTime must know roughly where you are in order to be able to calculate your prayer times and the Qibla direction. Tap the info icon to see exactly what this means.
- `permission_why_title`: Why we need your location
- `permission_why_body`: To calculate the correct prayer times and Qibla direction for the place you are in. / We check your approximate location only while the app is open. Your location stays on your phone. We do not send it anywhere.
- `location_no_fix`: Could not get a location. Make sure the location service is on, or type your city below.
- `city_fallback_body`: Type your city instead. Your phone finds the city by itself when it can. If it cannot, only the name you typed is sent, once, to a free search service. After that everything is calculated on your phone.
- `city_not_found`: We could not find this place. Try adding the country name too.
- `action_use_makkah`: Skip for now and use Makkah
- `sect_title`: Which *mezhep* (school / sect) do you belong to?
- `sect_body`: This decides how your prayer times are calculated. You can change it any time.
- `sect_sunni_desc`: Muslim World League times by default
- `sect_shia_desc`: Jafari (Ithna Ashari) times
- `madhab_title`: Choose your fiqh *mezhep* (school of jurisprudence)
- `madhab_body`: Optional. This changes only the Asr time. If you skip, you get the standard time.
- `madhab_hanafi_desc`: Later Asr: when the shadow is twice the object's height
- `madhab_standard_desc`: Standard Asr: when the shadow is equal to the object's height
- `method_step_body`: Mosques measure morning and evening twilight differently, so Imsak and Isha can differ by a few minutes from one method to another, and by more than an hour in the far north. If your mosque's times differ from the app, the fix is often to choose your mosque's method here. Not sure? Leave the first option. You can change this any time in the Settings section.
- `confirm_title`: Everything is ready
- `confirm_body`: Does it look right? You can change all of this later in the Settings section.
- `label_school`: *Mezhep* (school / sect)

### Disclaimer (`disclaimer_title`: Please read this first)

`disclaimer_body`, seven paragraphs:

1. SajdaTime is a helper; it is not a religious authority.
2. These times were not given to me by a mosque, a scholar or any authority. Your phone calculates them from the position of the sun, using calculation methods published by Islamic institutions and scholars. I am not a mufti, a scholar (*âlim*) or an expert in fiqh, and this app was made with the help of artificial intelligence. The software may have errors, a phone may be holding the wrong location or the wrong time, a phone trying to save its battery may delay an alert, and I may quite simply be wrong.
3. So please take what you see here as a calculation, not a ruling. When there is a difference between SajdaTime and your mosque, follow your mosque. If a time or a direction looks wrong to you, or you are not sure, ask them or another competent person who can guide you. The app is free and is offered as it is, with no warranty and no promise of accuracy. So where exactness matters to you, please do not rely on it alone.
4. If you use the smartwatch, please treat it as a convenience, not the final word. A watch's compass is smaller than a phone's and sees less of the sky, and it may also be working from a location your phone found some time ago. For the Qibla in particular, the more accurate of the two is your phone. When it matters, check there.
5. Not all mosques agree on when a prayer begins. Isha varies most. In Britain and further north the difference between one method and another can be more than an hour, because scholars measure the disappearance of twilight differently. I cannot settle this, and the app does not try to. If the times here do not agree with your mosque, ask them which method they use and choose it under Calculation method in the Settings section. If a few minutes' difference still remains after that, the "Match your mosque" option lets you adjust each prayer and the date by hand. If a difference still remains, follow your mosque.
6. One more thing. This app shows when a prayer's time comes in. Your mosque, though, sets its own time for the congregation, usually a little later. So the board there and the times here often differ. Neither of them is wrong.
7. I have one request too, if you accept it: do not leave me, my family and my parents out of your duas.

### Home, notices, Match your mosque

- `home_until`: (fragment) "left until it begins"
- `home_now_marker_a11y`: The current prayer
- `method_notice_title`: Does this agree with your mosque?
- `method_notice_body`: Imsak and Isha can differ by an hour or more here.
- `method_notice_action`: See why
- `mosque_diff_link` / `mosque_diff_title`: Different from your mosque?
- `mosque_diff_congregation`: This app shows when each prayer's time comes in. Your mosque usually leads the congregation a little later, so the board there and the times here often differ by a few minutes. Neither is wrong.
- `mosque_diff_twilight`: Imsak and Isha depend on how twilight is measured, and mosques measure it differently. If yours differs by more than a few minutes, choose the method your mosque follows.
- `mosque_diff_asr`: Asr changes according to the fiqh school. In Hanafi mosques Asr starts later than in the others, up to an hour, or even more.
- `mosque_diff_minutes`: If a few minutes' difference still remains, nudge each prayer bit by bit so it matches your mosque's printed times.
- `mosque_diff_follow`: If a difference still remains, follow your mosque.

### Polar notice

- `polar_notice_title`: Approximate times
- `polar_notice_body`: This far from the equator the sun does not always rise and set, so there is no real Imsak or Isha to calculate. These times have instead been calculated from latitude %1$d°. Mosques here handle this in different ways, so ask your mosque what it follows.
- `polar_notice_alarm`: On these days your alarms arrive as a silent notification instead of ringing, because the app cannot vouch for the exact minute. If you still want to be woken, turn on "Ring on approximate days" in the Settings section.
- `pdf_approximate_note`: Some of these times are approximate. This far north the sun does not always rise and set, so those days have instead been calculated from latitude %1$d°. Ask your mosque what it follows.
- `notif_approximate`: Approximate time
- `settings_alarm_on_approximate`: Ring on approximate days
- `settings_alarm_on_approximate_desc`: When the sun does not rise and set, the times are calculated from a lower latitude. Unless you turn this on, alarms stay silent on those days.

### Home misc

- `home_countdown_a11y`: %1$s in %2$s (literally "after %2$s, %1$s")
- `home_default_location_title`: Showing times for Makkah
- `home_default_location_body`: We could not work out where you are.
- `home_default_location_action`: Set my location

### Location sheet

- `location_sheet_body`: Both the prayer times and the Qibla depend on where you are. Update this when you travel. / To protect your privacy, the app asks only for your approximate location, so it may show the name of a nearby town or a wider region instead of your own place. This changes the times very little: 16 kilometres is worth about one minute. If the name looks wrong to you, type your city below.
- `action_use_my_location`: Use my current location
- `location_sheet_search_heading`: Or type a city

### PDF export

- `action_save_timetable`: Save the timetable as PDF
- `export_sheet_title`: Save your timetable
- `export_sheet_body`: We will prepare a PDF of your prayer times for %1$s. Then you can save it, print it or send it to someone.
- `export_today` / `_desc`: Today only / One day, six times
- `export_week` / `_desc`: The next 7 days / Today and the following six days
- `export_month` / `_desc`: All of this month / Every day from the 1st of the month to its end
- `export_saved`: Saved to the Downloads folder under this name: %1$s
- `export_failed`: The PDF could not be created. Please try again.
- `pdf_period_onwards`: %1$s and after
- `pdf_tagline`: Prayer times and Qibla, free for the Ummah
- `pdf_area_label`: Approximate area
- `pdf_footer`: Calculated offline on your device. SajdaTime is free forever.

### Qibla

- `qibla_subtitle`: The Kaaba is about %2$d km away from here, in the direction %1$d° from north.
- `qibla_needs_location`: Set your location to find the Qibla direction.
- `qibla_facing`: You are now facing the Kaaba
- `qibla_heading_now`: The direction you are looking: %1$d°
- `qibla_legend_facing`: The direction you are looking
- `qibla_turn_left` / `qibla_turn_right`: Turn %1$d° to the left / Turn %1$d° to the right
- `qibla_no_compass`: Turn to %1$d° from true north
- `qibla_calibrate`: The compass needs calibrating. Take your phone in your hand and draw a figure eight in the air a few times.
- `qibla_sensor_missing`: This phone has no compass, so we can give the angle but cannot show the direction.
- `qibla_accuracy_medium`: The reading may wobble a little. For a steadier result, move away from metal, magnets and speakers.

### Settings

- `theme_system`: Same as the phone
- `settings_school`: *Mezhep* (school / sect)
- `settings_madhab`: Fiqh *mezhep*
- `settings_alerts`: Prayer alerts
- `settings_which_prayers_all`: All five
- `settings_which_prayers_some`: %1$d of the 5 times
- `settings_method_auto`: Automatic · %1$s
- `settings_method_help`: Mosques measure twilight differently, and this mainly shifts the Imsak and Isha times. The further north you live, the bigger the difference. If these times do not agree with your mosque, ask them which method they use and choose it here.
- `settings_alerts_help`: Choose this separately for each prayer. A notification is silent and vibrates. An alarm plays the sound you chose, loudly, like a wake-up alarm.
- `alert_style_mixed`: Mixed
- `settings_alarm_sound_desc`: Choose any ringtone or adhan that is already on your phone
- `settings_alarm_respect_silent`: Stay silent when the phone is on silent
- `settings_alarm_respect_silent_desc`: On: you still get the notification on time, but no sound plays. Off: the alarm sounds even when the phone has been silenced, like a wake-up alarm.
- `settings_dnd_title`: Allow sound during Do Not Disturb
- `settings_dnd_desc`: Without this, your alarm stays silent while the phone is in Do Not Disturb mode.
- `settings_ongoing_badge`: Keep the next prayer in the notification panel
- `settings_ongoing_badge_desc`: A silent notification showing the next prayer and how much time you have left
- `settings_exact_alarms_title`: Allow on-time alarms
- `settings_exact_alarms_desc`: Without this your phone may delay prayer alerts by an hour or more, and on some phones an alert that waits through the night, such as Imsak, may never arrive at all.
- `action_allow_in_settings`: Allow in Settings
- `confirm_exact_alarm_title`: One last thing
- `confirm_exact_alarm_body`: Android does not let apps set an alert for the exact minute unless you allow it. Without this your phone may delay prayer alerts by an hour or more, and on some phones an alert that waits through the night, such as Imsak, may never arrive at all. You can also do this later in the Settings section.

### About, privacy, consent

- `about_disclaimer`: Disclaimer (literally "disclaimer of responsibility")
- `about_disclaimer_short`: A helper, not a religious authority. Tap to read.
- `about_privacy_desc`: Your location stays on this phone. No account, no ads. Usage counts are optional and off unless you turn them on. Tap to read the whole policy.
- `consent_title`: Will you help improve the SajdaTime app?
- `consent_body`:
  1. SajdaTime is free and has no ads. If you say yes, it sends a few usage counts to Google Analytics so that the app can be improved. These are linked to a random ID, not to your name, and may be processed outside the United Kingdom. Detailed records are kept for 14 months.
  2. Using a prayer app can say something about your faith, so the choice is yours, and if you say no nothing in the app changes. SajdaTime never sends your GPS location or your prayer settings.
  3. Nothing is sent unless you say yes. You can turn this off any time in the Settings section. The person responsible is Ali Imran Khan, who makes the app.
- `settings_language_desc`: Choose the app's language
- `language_unavailable`: Not available in this version
- `language_phone`: The phone's language
- `about_translation_note`: This translation was made with the help of AI and may contain mistakes. If you see a mistake, please tell us with Send feedback.
- `consent_policy_english`: The full text of the privacy policy is in English.
- `consent_see_sent`: See exactly what is sent
- `consent_hide_sent`: Hide details
- `consent_sent_heading`: What is sent if you say yes
- `consent_sent_list`:
  - Which main screen you open: Times, Qibla or Settings
  - Which steps you reach in setup
  - Whether you allow notifications, location and on-time alarms (a yes or no for each, never the location itself)
  - How often and for how long the app is open
  - Your phone model, your Android version, your app version and your language
  - When you first opened the app and how you found the app (for example a link on Google Play)
  - Your approximate region, which Google infers from your internet connection
- `consent_never_heading`: What is never sent
- `consent_never_list`:
  - Your GPS location or your coordinates
  - The place you typed
  - Your *mezhep* (school / sect), your prayer calculation setting, your calculation method and your alert settings
  - Anything you type
- `consent_yes`: Yes, share usage counts
- `consent_no`: No, thanks
- `consent_read_policy`: Read the privacy policy
- `about_feedback_desc`: Tell the developer what works or what is wrong. Opens your e-mail app.
- `settings_usage_counts`: Share usage counts
- `settings_usage_counts_desc`: Sent to Google, linked to a random ID. Off unless you turn it on. When you turn it on you are shown exactly what is sent.
- `about_charity`: Free forever
- `about_charity_desc`: Made with love, free for the Ummah. A charity project for the sake of Allah.
- `about_credits`: Made by
- `about_credits_desc`: Ali Imran Khan, as a *sadaqa-i jariya* (ongoing charity) for the Ummah
- `about_data`: Data and libraries
- `about_data_desc`: Prayer times are calculated with the adhan-java library written by Batoul Apps (MIT). City search uses the Open-Meteo geocoding API, licensed CC BY 4.0.

### Notification channels and notifications

- `channel_prayers_desc`: A notification at the start of each prayer time
- `channel_alarm` / `_desc`: Prayer alarm / A louder alert with the sound you chose
- `channel_ongoing` / `_desc`: Next prayer badge / A silent reminder of the next prayer that always sits in the notification panel
- `notif_prayer_title`: Time for %1$s (literally "%1$s time")
- `notif_prayer_body`: The %1$s time begins as of %2$s o'clock
- `notif_next_title`: %1$s, at %2$s
- `notif_next_body`: in %1$s (literally "after %1$s")

### Hijri months, durations

- `hijri_months`: Muharram, Safar, Rabi al-Awwal, Rabi al-Akhir, Jumada al-Ula, Jumada al-Akhira, Rajab, Sha'ban, Ramadan, Shawwal, Dhu al-Qa'da, Dhu al-Hijja (Diyanet spellings)
- `duration_h_m` / `_m` / `_s`: %1$d h %2$d min / %1$d min / %1$d s

### Match your mosque (adjustments)

- `settings_adjustments`: Match your mosque
- `settings_adjustments_none`: No change
- `settings_adjustments_help`: If your mosque writes a different time, nudge it here bit by bit, or tap a number and pick the time your mosque shows. Try the calculation method first. The real difference is usually that; this is for the few minutes that are left over.
- `adjustment_limit_note`: A difference bigger than %1$d minutes usually comes from a different calculation method, or from a mosque board that shows the congregation time, which is a little later. Try the method first.
- `adjustment_choose_method`: Choose a calculation method
- `adjustment_now_shows`: Shown: %1$s
- `adjustment_pick_time_description`: Set the time your mosque shows for %1$s
- `adjustment_pick_time_title`: %1$s: the time on your mosque's board
- `settings_adjustments_reset`: Reset all
- `settings_hijri_offset`: Hijri date
- `settings_hijri_offset_label`: Shift the date
- `settings_hijri_offset_help`: The Hijri date here is calculated. If your mosque sights the new crescent locally, the date may be a day ahead or behind. This also shifts the Ramadan and Bayram (Eid) dates.
- `adjustment_summary_times`: %d time(s) adjusted
- `adjustment_summary_hijri`: Date %1$s
- `action_decrease` / `action_increase`: Decrease for %1$s / Increase for %1$s

## core/src/main/res/values-tr/strings.xml

- Prayer names: Fajr = **İmsak** (the Diyanet timetable word for dawn / start of the fast, not the name of the prayer itself, which is *Sabah namazı*), Sunrise = Güneş ("Sun"), Dhuhr = Öğle, Asr = İkindi, Maghrib = Akşam, Isha = Yatsı.
- Method names: kept in English, with "Makkah", "North America", "Indonesia and Singapore", "Turkey", "Tehran" rendered in Turkish. `method_auto`: Automatic.
- `method_auto_desc`: Not sure? Leave this. Muslim World League times, and Jafari for Shia. Close to local practice in most of the world. The far north differs most.
- `method_muslim_world_league_desc`: A general method used in many countries. The standard here.
- `method_egyptian_desc`: The official method of Egypt.
- `method_karachi_desc`: Common in Pakistan and South Asia.
- `method_umm_al_qura_desc`: The official method of Saudi Arabia. Isha is set at a fixed interval after Maghrib.
- `method_dubai_desc`: A method used in the Gulf.
- `method_moon_sighting_desc`: A seasonal rule made for places far from the equator. Followed by some mosques in Britain.
- `method_north_america_desc`: Islamic Society of North America. Used by many mosques in the United States and Canada.
- `method_kuwait_desc`: The method used in Kuwait.
- `method_qatar_desc`: The method used in Qatar.
- `method_singapore_desc`: The official bodies of Indonesia and Singapore.
- `method_turkey_desc`: The Presidency of Religious Affairs of the Republic of Turkey.
- `method_jafari_desc`: The usual Shia method. Maghrib begins a little after sunset, when the redness has withdrawn from the sky.
- `method_tehran_desc`: University of Tehran. Iran's official times follow its timetable.

## wear/src/main/res/values-tr/strings.xml

- `tile_label`: Next prayer
- `tile_description`: The next prayer and how much time you have left
- `tile_needs_setup`: Set up from your phone
- `wear_locating`: Finding your location
- `wear_no_location_title`: No location yet
- `wear_no_location_body`: Open SajdaTime on your phone, or use Makkah for now.
- `wear_use_makkah`: Use Makkah
- `wear_disclaimer`: A helper, not a religious authority. The times are calculated. Follow your mosque.
- `wear_polar_notice`: Approximate. Today the sun does not rise and set here, so these are from latitude %1$d°. Ask your mosque.
- `wear_school`: *Mezhep*
- `wear_madhab_note`: Only Asr changes. Hanafi is later.
- `wear_qibla_needs_location`: Set your location to find the Qibla direction
- `wear_qibla_facing`: You are facing the Kaaba
- `wear_qibla_from_true_north`: from true north
- `wear_duration_*` / `wear_in_*`: %1$dh %2$dmin / %1$dmin / in %1$dh %2$dmin / in %1$dmin
- `wear_tile_detail_approx`: Approximately %1$s · %2$s
