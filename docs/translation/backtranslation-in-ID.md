# Indonesian back-translation (in-ID)

Written 8 Oct 2026 by an independent reviewer model, from the three `values-in/strings.xml` files
**only**, before the English sources were opened. Each line is what the Indonesian actually says,
rendered into plain English as literally as reads sensibly. Single-word labels are omitted unless they
carry a religious or safety term. The comparison with the English is in `review-in-ID.md`.

## app/src/main/res/values-in/strings.xml

### Onboarding, location
- `welcome_tagline`: Your personal prayer companion. Free, forever.
- `permission_title`: Where do you pray?
- `permission_body`: SajdaTime needs a rough estimate of your location to calculate prayer times and the qibla direction. Tap the info icon to see what exactly is meant.
- `permission_why_title`: Why we need your location
- `permission_why_body`: To calculate the correct prayer times and qibla direction for the place where you are. / We only check your approximate location while the app is open. That location stays stored on your phone. We never send it anywhere.
- `location_finding`: Looking for your location…
- `location_looking_up`: Looking for that place…
- `location_no_fix`: We could not determine your position. Make sure location is turned on, or type your city's name below.
- `city_fallback_body`: Just type your city's name. Your phone will look it up itself when it can. If it cannot, only the name you typed is sent, once, to a free search service. After that, everything is calculated on your phone.
- `city_field_helper`: For example: Jakarta, Surabaya, Cairo
- `city_not_found`: We could not find that place. Try adding the name of the country.
- `action_use_makkah`: Skip for now and use Mecca
- `sect_title`: Which stream (aliran) do you follow?
- `sect_body`: This determines how your prayer times are calculated. You can change it at any time.
- `sect_sunni_desc`: Muslim World League times as the default
- `sect_shia`: Shia (Syiah)
- `sect_shia_desc`: Jafari (Ithna Ashari) times
- `madhab_title`: Choose your madhab (mazhab)
- `madhab_body`: Optional. This only changes the Asr time. Skip it and you will get the standard time.
- `madhab_shafii`: Shafi'i (Syafi'i)
- `madhab_hanafi_desc`: Later Asr, when the length of the shadow is twice the length of the object
- `madhab_standard_desc`: Standard Asr, when the length of the shadow equals the length of the object
- `method_step_title`: Calculation method
- `method_step_body`: Mosques measure dawn and dusk in different ways, so Fajr and Isha can differ by a few minutes from one method to another, and by more than an hour in the far north. If your mosque's times differ from the app, choosing your mosque's method here is usually the solution. Not sure? Leave the first option. You can change it any time in Settings.
- `confirm_title`: All set
- `confirm_body`: Is that right? You can change all of this later in Settings.
- `label_school` / `settings_school` / `wear_school`: Stream (aliran)

### Disclaimer
- `disclaimer_title`: Please read this first
- `disclaimer_body`, paragraph 1: SajdaTime is a helping tool, not a religious authority.
- paragraph 2: I do not receive these times from a mosque, from scholars, or from any authority. Your phone calculates them from the position of the sun, with calculation methods that have been published by Islamic institutions and scholars. I am not a mufti, not a scholar (ulama), and not an expert in fiqh, and this app was made with the help of artificial intelligence. Software can contain errors, a phone can hold a wrong location or clock, a phone that is saving battery can delay reminders, and I myself can simply be wrong.
- paragraph 3: Therefore, please regard what you see here as the result of a calculation, not a legal (religious-law) ruling. When SajdaTime and your mosque differ, follow your mosque. If a time or direction seems wrong to you, or you are in doubt, ask the mosque or another person who is competent to give advice. This app is free and is provided as it is, without guarantee and without a promise of accuracy, so if accuracy matters to you, please do not rely on this app alone.
- paragraph 4: If you use a smartwatch, please treat it as a convenience, not as the final word. The watch's compass is smaller and its view of the sky more limited than the phone's, and the watch may be using a location your phone found some time ago. For the qibla in particular, your phone is the more accurate of the two. When that matters, check on the phone.
- paragraph 5: Not all mosques agree about when a prayer begins. Isha is the one that differs most. In Britain and regions further north, the gap between one method and another can be more than an hour, because scholars measure the disappearance of twilight in different ways. I cannot decide it, and this app does not try to either. If the time here does not match your mosque, ask which method they use and then choose it in Settings, under Calculation method. If after that a gap of a few minutes still remains, the Match with mosque feature lets you set each prayer time, and the date too, by hand. If there is still a difference, follow your mosque.
- paragraph 6: One more thing. This app shows when the prayer time comes in. Your mosque then sets its own time for the congregational prayer, usually a little later, so the timetable board at the mosque and the time here often differ. Neither of them is wrong.
- paragraph 7: And one request, if you are willing: please remember me, my family, and both my parents in your prayers (doa).

### Home
- `home_next_prayer`: NEXT PRAYER
- `home_until`: until its time comes in
- `home_now_marker_a11y`: Current prayer
- `method_notice_title`: Does this match your mosque?
- `method_notice_body`: Here Fajr and Isha can differ by one hour or more.
- `method_notice_action`: See the reason
- `mosque_diff_link` / `mosque_diff_title`: Different from your mosque?
- `mosque_diff_congregation`: This app shows when the time of each prayer comes in. Your mosque usually holds the congregational prayer a little later, so its timetable board and the time here often differ by a few minutes. Neither is wrong.
- `mosque_diff_twilight`: Fajr and Isha depend on how dawn and dusk are measured, and mosques measure them in different ways. If your mosque differs by more than a few minutes, choose the method it follows.
- `mosque_diff_asr`: Asr depends on the madhab. Hanafi mosques start Asr later than the others, by up to an hour or more.
- `mosque_diff_minutes`: If a gap of a few minutes still remains, shift each prayer time to match your mosque's printed timetable.
- `mosque_diff_follow`: If there is still a difference, follow your mosque.
- `polar_notice_title`: Approximate times
- `polar_notice_body`: This far from the equator, the sun does not always rise or set, so there is no real Fajr or Isha to calculate. Instead, these times are calculated from latitude %1$d°. Mosques here handle it in different ways, so ask your mosque what it follows.
- `polar_notice_alarm`: On these days your alarm comes as a silent notification, not ringing, because the app cannot guarantee the exact minute. Turn on "Ring on approximate days" in Settings if you still want to be woken.
- `home_no_location`: Set your location to see prayer times.
- `home_countdown_a11y`: %1$s in %2$s
- `home_default_location_title`: Showing times for Mecca
- `home_default_location_body`: We could not determine your location.
- `home_default_location_action`: Set my location

### Location sheet
- `location_sheet_body`: Prayer times and the qibla direction both depend on your location. Update this every time you travel. / To protect your privacy, the app only asks for your approximate position, so what is written may be the name of the nearest city or a wider region, not your exact place. The effect on prayer times is very small: 16 kilometres is only about one minute. If the name seems wrong, type your city's name below.
- `action_use_my_location`: Use my current location
- `location_sheet_search_heading`: Or type a city name

### PDF export
- `action_save_timetable`: Save timetable as PDF
- `export_sheet_title`: Save your timetable
- `export_sheet_body`: We will make a PDF of your prayer timetable for %1$s. After that you can save it, print it, or send it to someone else.
- `export_today` / `_desc`: Today only / One day, six times
- `export_week` / `_desc`: Next 7 days / Today and the six days after it
- `export_month` / `_desc`: This whole month / Every day from the 1st to the end of the month
- `export_saved`: Saved in the Downloads folder as %1$s
- `export_failed`: Could not make the PDF. Please try again.
- `pdf_period_onwards`: %1$s and onwards
- `pdf_tagline`: Prayer times and qibla direction, free for the Ummah
- `pdf_area_label`: Approximate area
- `pdf_approximate_note`: Some of these times are approximate. This far north, the sun does not always rise or set, so those days are calculated from latitude %1$d° instead. Ask your mosque what it follows.
- `pdf_footer`: Calculated on your device without internet. SajdaTime free, forever.

### Qibla
- `qibla_subtitle`: The Kaaba is about %2$d km from here, at %1$d° from north.
- `qibla_needs_location`: Set your location to find the qibla direction.
- `qibla_facing`: You are now facing the Kaaba
- `qibla_heading_now`: Facing %1$d°
- `qibla_legend_kaaba`: Kaaba (Kakbah)
- `qibla_legend_facing`: Your facing direction
- `qibla_turn_left`: Turn to the left %1$d°
- `qibla_turn_right`: Turn to the right %1$d°
- `qibla_no_compass`: Face %1$d° from true north
- `qibla_calibrate`: The compass needs calibrating. Hold your phone and move it in a figure of eight a few times.
- `qibla_sensor_missing`: This phone has no compass, so we can tell you the angle but cannot show you the direction.
- `qibla_accuracy_medium`: The reading may be slightly off. Move away from metal, magnets and loudspeakers so the result is steadier.

### Settings
- `settings_alerts`: Prayer reminders
- `settings_which_prayers_all`: All five
- `settings_which_prayers_some`: %1$d of 5
- `settings_method_auto`: Automatic · %1$s
- `settings_method_help`: Mosques measure dawn and dusk in different ways, and that mainly shifts Fajr and Isha. The further north you live, the bigger the gap. If the time here does not match your mosque, ask which method they use and choose it here.
- `settings_alerts_help`: Choose this for each prayer. Notifications are silent and vibrate. Alarm plays the sound you chose, loudly, like a wake-up alarm.
- `alert_style_mixed`: Mixed
- `settings_alarm_sound_desc`: Choose any tone or adhan (azan) already on your phone
- `settings_alarm_respect_silent`: Stay silent when the phone is in silent mode
- `settings_alarm_respect_silent_desc`: On: you still get a notification on time, without sound. Off: the alarm still sounds even when the phone is silenced, like a wake-up alarm.
- `settings_dnd_title`: Allow sound during Do Not Disturb mode
- `settings_dnd_desc`: Without this, your alarm stays silent when the phone is in Do Not Disturb mode.
- `settings_ongoing_badge`: Show the next prayer in the notification panel
- `settings_ongoing_badge_desc`: A silent notification that shows the next prayer and your remaining time
- `settings_exact_alarms_title`: Allow on-time alarms
- `settings_exact_alarms_desc`: Without this, your phone can hold back prayer reminders for an hour or more, and on some phones night-time reminders such as Fajr may not come at all.
- `confirm_exact_alarm_title`: One last thing
- `confirm_exact_alarm_body`: Android does not let apps set reminders at the exact minute unless you allow it. Without that permission, your phone can hold back prayer reminders for an hour or more, and on some phones night-time reminders such as Fajr may not come at all. You can also do it later from Settings.

### About, privacy, consent
- `about_disclaimer`: Disclaimer
- `about_disclaimer_short`: A helping tool, not a religious authority. Tap to read.
- `about_privacy_desc`: Your location stays stored on this phone. No account, no ads. Usage statistics are optional and off unless you turn them on. Tap to read the full policy.
- `consent_title`: Help improve SajdaTime?
- `consent_body`, paragraph 1: SajdaTime is free and ad-free. If you agree, the app sends a small amount of usage statistics to Google Analytics so the app can be improved. Those statistics are linked to a random ID, not your name, and may be processed outside the United Kingdom. Detailed records are kept for 14 months.
- paragraph 2: Using a prayer app can imply something about your belief, so this is your choice, and nothing in the app changes if you decline. SajdaTime never sends your GPS position or your prayer settings.
- paragraph 3: Before you choose to agree, nothing is sent. You can turn it off in Settings at any time. The person responsible is Ali Imran Khan, the maker of this app.
- `consent_policy_english`: The full privacy policy is available in English.
- `consent_see_sent`: See exactly what is sent
- `consent_hide_sent`: Hide details
- `consent_sent_heading`: What is sent if you agree
- `consent_sent_list`: • Which main screen you open: Times, Qibla, or Settings • Which setup step you reach • Whether you allow notifications, location and on-time alarms (each only yes or no, never the location itself) • How often and for how long the app is opened • Phone model, Android version, app version and your language • When you first opened the app, and how you found it (for example through a link on Google Play) • Your approximate area, which Google estimates from your internet connection
- `consent_never_heading`: What is never sent
- `consent_never_list`: • Your GPS position or coordinates • Places you type • Your stream (aliran), your prayer calculation settings, your calculation method, and your reminder settings • Anything you type
- `consent_yes`: Yes, share usage statistics
- `consent_no`: No, thank you
- `settings_language_desc`: Choose the app language
- `language_unavailable`: Not available in this version
- `about_translation_note`: This translation was made with AI help and may contain mistakes. If you find one, please let us know through Send feedback.
- `about_feedback_desc`: Tell the developer what helps or what is wrong. Opens your email app.
- `settings_usage_counts`: Share usage statistics
- `settings_usage_counts_desc`: Sent to Google, linked to a random ID. Off unless you turn it on. When turned on, exactly what is sent will be shown.
- `about_charity`: Free, forever
- `about_charity_desc`: Made with love, free for the Ummah. A charity project, for the sake of Allah.
- `about_credits` / `_desc`: Made by / Ali Imran Khan, as a sadaqah jariyah (sedekah jariah) for the Ummah
- `about_data_desc`: Prayer times are calculated with adhan-java by Batoul Apps (MIT). City search uses the Open-Meteo geocoding API, licensed CC BY 4.0.

### Notifications
- `channel_prayers_desc`: Notification at the start of each prayer time
- `channel_alarm_desc`: A louder reminder with the sound you chose
- `channel_ongoing`: Next-prayer badge
- `channel_ongoing_desc`: A silent reminder for the next prayer, always in the notification panel
- `notif_prayer_title`: Time for %1$s (literally "Its time, %1$s")
- `notif_prayer_body`: %1$s starts at (o'clock) %2$s
- `notif_next_title`: %1$s at %2$s
- `notif_next_body`: %1$s more / %1$s to go

### Durations
- `duration_h_m`: %1$dj %2$dm ("j" = jam/hour, "m" = minute, abbreviation not standard Indonesian)
- `duration_m`: %1$dm
- `duration_s`: %1$d sec (dtk)

### Match your mosque, Hijri
- `settings_adjustments`: Match with mosque
- `settings_adjustments_none`: No changes
- `settings_adjustments_help`: If your mosque prints different times, shift them here, or tap the number and choose the time your mosque shows. Try the calculation method first. That is usually the real difference, and this feature is for the leftover gap of a few minutes.
- `adjustment_limit_note`: A gap of more than %1$d minutes is usually caused by a different calculation method, or by a mosque timetable board that shows the congregational prayer time, which is a little later. Try the method first.
- `adjustment_choose_method`: Choose calculation method
- `adjustment_now_shows`: Showing %1$s
- `adjustment_pick_time_description`: Set %1$s to the time your mosque shows
- `adjustment_pick_time_title`: %1$s: the time on your mosque's timetable board
- `settings_adjustments_reset`: Reset all
- `settings_hijri_offset`: Hijri date
- `settings_hijri_offset_label`: Shift date
- `settings_hijri_offset_help`: The Hijri date here is a calculated result. If your mosque does local crescent sighting (rukyat hilal), the date may be one day earlier or later. This also shifts Ramadan and the Eid holiday (hari raya Id).
- `adjustment_minutes`: %+d min
- `adjustment_days`: %+d day(s)
- `adjustment_summary_times`: %d prayer(s) adjusted
- `adjustment_summary_hijri`: Date %1$s
- `action_decrease` / `action_increase`: Decrease %1$s / Increase %1$s
- `notif_approximate`: Approximate time
- `settings_alarm_on_approximate`: Ring on approximate days
- `settings_alarm_on_approximate_desc`: When the sun does not rise or set, times are calculated from a lower latitude. Alarms stay silent on those days unless you turn this on.

### Hijri months (`hijri_months`)
Muharram, Safar, Rabi al-Awwal, Rabi al-Akhir, Jumada al-Ula (awal), Jumada al-Akhirah, Rajab, Sha'ban, Ramadan, Shawwal, Dhu al-Qa'dah, Dhu al-Hijjah.

## core/src/main/res/values-in/strings.xml

- Prayer names: Fajr = Subuh, Sunrise = Terbit ("rises"), Dhuhr = Zuhur, Asr = Asar, Maghrib = Magrib, Isha = Isya.
- `method_auto`: Automatic
- `method_umm_al_qura`: Umm al-Qura, Mecca
- `method_north_america`: ISNA, North America
- `method_singapore`: Kemenag & MUIS, Indonesia and Singapore
- `method_turkey`: Diyanet, Turkey
- `method_tehran`: Institute of Geophysics, Tehran
- `method_auto_desc`: Not sure? Leave this option. Muslim World League times, or Jafari for Shia. Close to local practice in most of the world. The biggest differences are in the far north.
- `method_muslim_world_league_desc`: A common convention used in many countries. The standard choice here.
- `method_egyptian_desc`: The official method of Egypt.
- `method_karachi_desc`: Commonly used in Pakistan and South Asia.
- `method_umm_al_qura_desc`: The official method in Saudi Arabia. Isha is set at a fixed interval after Maghrib.
- `method_dubai_desc`: The convention used in the Gulf region.
- `method_moon_sighting_desc`: A seasonal rule made for places far from the equator. Followed by some mosques in Britain.
- `method_north_america_desc`: Islamic Society of North America. Used by many mosques in the United States and Canada.
- `method_kuwait_desc` / `method_qatar_desc`: The convention used in Kuwait / in Qatar.
- `method_singapore_desc`: The official bodies of Indonesia and Singapore.
- `method_turkey_desc`: Turkey's Religious Affairs Agency.
- `method_jafari_desc`: The convention customarily used by Shia (Muslims). Maghrib falls a little after sunset, when the red twilight has gone from the sky.
- `method_tehran_desc`: University of Tehran, whose calendar is the reference for Iran's official times.

## wear/src/main/res/values-in/strings.xml

- `tile_label`: Next prayer
- `tile_description`: The next prayer and your remaining time
- `tile_needs_setup`: Set up on your phone
- `wear_locating`: Finding your location
- `wear_no_location_title`: No location yet
- `wear_no_location_body`: Open SajdaTime on your phone, or use Mecca for now.
- `wear_use_makkah`: Use Mecca
- `wear_disclaimer`: A helping tool, not a religious authority. Times are calculated. Follow your mosque.
- `wear_polar_notice`: Approximate. Here the sun does not rise or set today, so these times are taken from latitude %1$d°. Ask your mosque.
- `wear_madhab_note`: Only Asr changes. Hanafi is later.
- `wear_qibla_needs_location`: Set your location to find the qibla direction
- `wear_qibla_facing`: Facing the Kaaba
- `wear_qibla_from_true_north`: from true north
- `wear_in_h_m` / `wear_in_m`: %1$dh %2$dm more / %1$dm more
- `wear_tile_detail_approx`: About %1$s · %2$s
