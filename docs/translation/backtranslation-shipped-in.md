# Back-translation: Indonesian as shipped in 1.3.1

Independent reviewer, 8 Oct 2026. Written from the three `values-in/strings.xml` files only,
**before** reading the English sources. Each line is a plain English rendering of what the
Indonesian actually says, kept deliberately literal so that drift is visible. Short labels whose
meaning is obvious (Cancel, Theme, Dark) are left out. Bracketed words are the reviewer's notes.

## app/src/main/res/values-in/strings.xml

### Onboarding and location

| Key | What the Indonesian says |
|---|---|
| `app_language_tag` | `id` [not `in-ID`] |
| `welcome_tagline` | Your personal prayer companion. Free, forever. |
| `permission_title` | Where do you pray? |
| `permission_body` | SajdaTime needs to know roughly where you are to calculate prayer times and the qibla direction. Tap the info icon to see exactly what this means. |
| `permission_why_title` | Why we need your location |
| `permission_why_body` | To calculate the correct prayer times and qibla direction for your place. / We only check your approximate location while the app is open. That location stays on your phone. We never send it anywhere. |
| `location_no_fix` | Your location could not be found yet. Make sure location is on, or type your city below. |
| `city_fallback_body` | Just type your city. If it can, your phone looks it up by itself. If it cannot, only the name you type is sent once to a free search service. After that, everything is calculated on your phone. |
| `city_field_helper` | Example: Jakarta, Surabaya, Medan |
| `city_not_found` | That place was not found. Try adding its country name. |
| `action_use_makkah` | Skip for now and use Makkah |
| `sect_title` | Which *aliran* [stream / current / sect; also the word in "aliran sesat", deviant sect] do you follow? |
| `sect_body` | This determines how your prayer times are calculated. You can change it at any time. |
| `sect_sunni_desc` | By default it uses Muslim World League times |
| `sect_shia_desc` | Jafari times (Ithna Ashariyah) |
| `madhab_title` | Choose your mazhab |
| `madhab_body` | Optional. This only changes the Asar time. Just skip it and you will get the standard time. |
| `madhab_hanafi_desc` | Later Asar, when the shadow is twice the length of the object |
| `madhab_standard_desc` | Standard Asar, when the shadow is the same length as the object |
| `method_step_body` | Mosques measure dawn and dusk differently, so Subuh and Isya can differ by a few minutes from one method to another, even more than an hour in regions far to the north. If the time at your mosque differs from the app, choosing that mosque's method here is usually the solution. Unsure? Choose the first option. You can change it any time in Settings. |
| `confirm_title` / `confirm_body` | Ready / Is that right? You can change all of this later in Settings. |
| `label_school` / `settings_school` / `wear_school` | *Aliran* [stream / sect] |

### Disclaimer (`disclaimer_body`), paragraph by paragraph

1. SajdaTime is an aid (tool), not a religious authority.
2. These times were not given to me by any mosque, scholar or authority. Your phone calculates them from the position of the sun, with calculation methods published by Islamic authorities and scholars. I am not a mufti, not an alim and not an expert in fiqh, and this app was made with the help of artificial intelligence (AI). Software can contain mistakes, a phone can hold a wrong location or clock, a phone that is saving battery can delay reminders, and I too can simply be mistaken.
3. So please treat what you see here as the result of a calculation, not as a fatwa or a ruling. If SajdaTime and your mosque differ, follow your mosque. If a time or a direction looks not right, or you are unsure, ask them, or someone else who is authorised to give advice. This app is free and is given as it is, without any warranty and without a promise of accuracy. So when accuracy matters to you, please do not rely on this app alone.
4. If you use a smartwatch, please treat it as a convenience only, not the final reference. A watch has a smaller compass and a worse view of the sky than a phone, and may be using a location your phone found some time ago. Especially for the qibla, your phone is the more accurate of the two. When it matters, check on the phone.
5. Not all mosques agree on when prayer time begins. Isya varies the most. In Britain and regions further north, the gap between one method and another can be more than an hour, because scholars measure the fading of twilight light differently. I cannot decide that, and this app does not try to decide it. If the times here do not match your mosque, ask which method they use and choose that method in Settings, under Calculation method. If after that there is still a gap of a few minutes, "Sesuaikan dengan masjid" [Adjust to the mosque] lets you set each prayer time, and also the date, by hand. If there is still a difference, follow your mosque.
6. One more thing. This app shows when the prayer time comes in. Your mosque then decides its own congregational prayer time, usually a little later, so the timetable board there and the time here often differ. Neither is wrong.
7. And one request, if you are willing: please remember me, my family and my parents in your duas.

### Home, notices, Match your mosque

| Key | What the Indonesian says |
|---|---|
| `home_until` | "more [time] until the time comes in" [a fragment that relies on surrounding layout] |
| `home_now_marker_a11y` | The current prayer |
| `method_notice_title` | Does this match your mosque? |
| `method_notice_body` | Subuh and Isya can differ by an hour or more here. Tap to find out why. |
| `method_notice_action` | See the reason |
| `mosque_diff_title` | Different from your mosque? |
| `mosque_diff_congregation` | This app shows when each prayer time comes in. Your mosque usually holds the congregational prayer a little later, so its timetable board and the times here often differ by a few minutes. Neither is wrong. |
| `mosque_diff_twilight` | Subuh and Isya depend on how dawn and dusk light is measured, and mosques measure it differently. If your mosque differs by more than a few minutes, choose the method it follows. |
| `mosque_diff_asr` | Asar depends on the mazhab. Hanafi mosques begin Asar later than the others, by up to an hour or more. |
| `mosque_diff_minutes` | If there is still a gap of a few minutes, shift each prayer time so it equals the printed timetable at your mosque. |
| `mosque_diff_follow` | If there is still a difference, follow your mosque. |
| `settings_adjustments` | Adjust to the mosque |
| `settings_adjustments_none` | No changes |
| `settings_adjustments_help` | If your mosque prints different times, shift them here. Try changing the calculation method first. Usually that is the real difference, and this setting is for the remaining gap of a few minutes. |
| `settings_hijri_offset_help` | The Hijri date here is a calculation result. If your mosque does its own crescent sighting (rukyat hilal), its date can be a day ahead or behind. This also shifts Ramadan and the Eid days (hari raya). |
| `adjustment_summary_times` | %d prayer time(s) adjusted |
| `adjustment_summary_hijri` | Date %1$s |
| `adjustment_choose_method` | Choose calculation method |
| `adjustment_limit_note` | A difference of more than %1$d minutes is usually caused by a different calculation method, or by a mosque timetable board that shows the congregational prayer time, which is a little later. Try the method first. |
| `adjustment_now_shows` | Showing %1$s |
| `adjustment_pick_time_description` | Set %1$s to the time your mosque shows |
| `adjustment_pick_time_title` | %1$s: the time on your mosque's timetable board |
| `action_set` | Set |
| `action_allow_in_settings` | Allow in Settings |
| `home_default_location_title` / `_body` / `_action` | Showing times for Makkah / Your location could not be determined. Tap to set your location. / Set my location |

### Polar / approximate days

| Key | What the Indonesian says |
|---|---|
| `polar_notice_title` | Approximate times |
| `polar_notice_body` | This far from the equator, the sun does not always rise or set, so there is no real Subuh or Isya to calculate. These times are calculated from latitude %1$d° instead. Mosques here handle it in different ways, so ask your mosque what it follows. |
| `polar_notice_alarm` | On these days your alarm arrives as an ordinary notification, instead of ringing, because the app cannot guarantee the exact minute. Turn on "Bunyikan pada hari perkiraan" [Ring on approximate days] in Settings if you still want to be woken. |
| `pdf_approximate_note` | Some of these times are approximations. This far north, the sun does not always rise or set, so those days are calculated from latitude %1$d° instead. Ask your mosque what it follows. |
| `notif_approximate` | Approximate time |
| `settings_alarm_on_approximate` | Ring on approximate days |
| `settings_alarm_on_approximate_desc` | When the sun does not rise or set, times are calculated from a lower latitude. Alarms do not ring on those days unless you turn this on. |

### Location sheet, export, PDF

| Key | What the Indonesian says |
|---|---|
| `location_sheet_body` | Prayer times and the qibla direction depend on where you are. Update this every time you travel. / To protect your privacy, the app asks only for your approximate position, so what appears may be the name of a nearby town or a wider area, not your town. That changes the times only slightly: a distance of about 16 km (ten miles) is roughly one minute. If that name looks wrong, type your city below. |
| `export_sheet_body` | We will make a PDF of your prayer times for %1$s. After that you can save, print or send it to someone. |
| `export_today_desc` | One day, six times |
| `export_week_desc` | Today and the six days after it |
| `export_month` / `_desc` | This whole month / Every day from the 1st to the end of the month |
| `export_saved` | Saved in Download as %1$s |
| `export_failed` | The PDF could not be made. Please try again. |
| `pdf_tagline` | Prayer times and qibla, free for the ummah |
| `pdf_footer` | Calculated offline on your device. SajdaTime is free, forever. |

### Qibla

| Key | What the Indonesian says |
|---|---|
| `qibla_subtitle` | The Ka'bah is about %2$d km from here, at %1$d° from north. |
| `qibla_facing` | You are now facing the Ka'bah |
| `qibla_heading_now` | Facing %1$d° |
| `qibla_legend_facing` | Your facing direction |
| `qibla_turn_left` / `_right` | Turn to the left %1$d° / Turn to the right %1$d° ["putar": rotate, turn something round] |
| `qibla_no_compass` | Face %1$d° from true north |
| `qibla_calibrate` | The compass needs calibrating. Hold your phone and move it in a figure of eight a few times. |
| `qibla_sensor_missing` | This phone has no compass, so we can give the angle but cannot show the direction. |
| `qibla_accuracy_medium` | The result may be slightly off. Move away from metal, magnets and loudspeakers so it is steadier. |

### Alerts, alarms and permissions

| Key | What the Indonesian says |
|---|---|
| `settings_which_prayers_all` / `_some` / `_none` | All five times / %1$d of 5 / None |
| `settings_method_help` | Mosques measure dawn and dusk light differently, and that mainly shifts Subuh and Isya. The further north you live, the larger the gap. If these times do not match your mosque, ask which method they use and choose it here. |
| `settings_alerts_help` | Choose this for each prayer time. A notification **sounds softly** and vibrates. An alarm plays the sound you chose loudly, like a wake-up alarm. |
| `alert_style_mixed` | Mixed |
| `settings_alarm_sound_desc` | Choose any tone or adhan that is already on your phone |
| `settings_alarm_respect_silent` | Stay silent when the phone is in silent mode |
| `settings_alarm_respect_silent_desc` | On: you still get a notification on time, without sound. Off: the alarm still rings even when the phone is silenced, like a wake-up alarm. |
| `settings_dnd_title` | Allow sound during Do Not Disturb |
| `settings_dnd_desc` | Without this, your alarm stays silent when the phone is in Do Not Disturb mode. Tap to allow. |
| `settings_ongoing_badge` | Show the next prayer in the notification panel |
| `settings_ongoing_badge_desc` | A silent notification showing the next prayer and the time left |
| `settings_exact_alarms_title` | Allow on-time alarms |
| `settings_exact_alarms_desc` | Without this, your phone can delay prayer reminders by up to an hour or more, and on some phones night reminders such as Subuh can fail to appear at all. Tap to allow. |
| `confirm_exact_alarm_title` | One last thing |
| `confirm_exact_alarm_body` | Android does not let apps set a reminder at the exact minute unless you allow it. Without that permission, your phone can delay prayer reminders by up to an hour or more, and on some phones night reminders such as Subuh can fail to appear at all. You can also do this later from Settings. |
| `permission_allowed` | Allowed |
| `channel_prayers_desc` | A notification when each prayer time comes in |
| `channel_alarm_desc` | A louder reminder with the sound you chose |
| `channel_ongoing` | Next-prayer marker |
| `channel_ongoing_desc` | A silent reminder for the next prayer, always in the notification panel |
| `notif_prayer_title` | It is time for %1$s |
| `notif_prayer_body` | %1$s comes in at %2$s |
| `notif_next_title` / `_body` | %1$s at %2$s / In %1$s |

### About, privacy, consent, language

| Key | What the Indonesian says |
|---|---|
| `about_disclaimer_short` | An aid, not a religious authority. Tap to read. |
| `about_privacy_desc` | Your location stays on this phone. No account, no ads. Usage count data is optional and off unless you turn it on. Tap to read the full policy. |
| `consent_title` | Help improve SajdaTime? |
| `consent_body` | SajdaTime is free and ad-free. If you agree, this app sends a small amount of usage count data to Google Analytics so that the app can be improved. That data is linked to a random ID, not your name, and may be processed outside the United Kingdom. Detailed records are kept for 14 months. / Using a prayer app can reveal something about your beliefs, so this is entirely your choice, and nothing in the app changes if you say no. SajdaTime never sends your GPS position or your prayer settings. / Until you choose yes, nothing is sent. You can turn it off in Settings at any time. The person responsible is Ali Imran Khan, the maker of this app. |
| `consent_see_sent` / `consent_hide_sent` | See exactly what is sent / Hide details |
| `consent_sent_heading` | What is sent if you agree |
| `consent_sent_list` | • The main screen you open: Times, Qibla or Settings • The setup steps you reach • Whether you allowed notifications, location and on-time alarms (each only yes or no, never the location itself) • How often and how long the app is open • Phone model, Android version, app version and your language • When you first opened the app, and how you found this app (for example a link on Google Play) • Your approximate area, which Google works out from your internet connection |
| `consent_never_heading` | What is never sent |
| `consent_never_list` | • Your GPS position or coordinates • Places you type • Your *aliran* [sect], prayer calculation settings, calculation method, and your reminder settings • Anything you type |
| `consent_yes` / `consent_no` | Yes, share usage data / No, thank you |
| `consent_policy_english` | The full privacy policy is written in English. |
| `settings_usage_counts` | Share usage data |
| `settings_usage_counts_desc` | Sent to Google, linked to a random ID. Off unless you turn it on. When it is turned on, you see exactly what is sent. |
| `about_feedback_desc` | Tell the developer what helps or what is wrong. Opens your email app. |
| `about_charity_desc` | Made with love, free for the ummah. A charity project, purely for the sake of Allah. |
| `about_credits_desc` | Ali Imran Khan, as sadaqah jariyah for the ummah |
| `about_data_desc` | Prayer times are calculated with adhan-java from Batoul Apps (MIT). City search uses the Open-Meteo geocoding API, licensed CC BY 4.0. |
| `settings_language_desc` | Choose the app's language |
| `language_unavailable` | Translation waiting to be reviewed by a native speaker |
| `language_phone` | Phone language |
| `about_translation_note` | This translation was made with AI help and may contain mistakes. If you find one, please tell us through Send feedback. |

## core/src/main/res/values-in/strings.xml

| Key | What the Indonesian says |
|---|---|
| `app_language_tag` | `id` [not `in-ID`] |
| Prayer names | Subuh, Terbit, Zuhur, Asar, Magrib, Isya |
| `method_muslim_world_league` | Muslim World League [translated: Liga Muslim Dunia] |
| `method_egyptian` | Egyptian General Authority [translated] |
| `method_karachi` | University of Karachi [translated] |
| `method_umm_al_qura` | Umm al-Qura, Makkah |
| `method_moon_sighting` | Moonsighting Committee [kept in English] |
| `method_singapore` | Kemenag & MUIS, Indonesia and Singapore |
| `method_jafari` | Jafari (Ithna Ashariyah) |
| `method_tehran` | Institute of Geophysics, Tehran [translated] |
| `method_auto_desc` | Unsure? Just pick this. Uses Muslim World League times, or Jafari for Shia. Close to local practice in many parts of the world. The biggest differences are in regions far to the north. |
| `method_muslim_world_league_desc` | A general rule used in many countries. This is the default in this app. |
| `method_egyptian_desc` | The official method of Egypt. |
| `method_karachi_desc` | Common in Pakistan and South Asia. |
| `method_umm_al_qura_desc` | The official method in Saudi Arabia. Isya is set at a fixed interval after Magrib. |
| `method_dubai_desc` | The rule used in the Gulf region. |
| `method_moon_sighting_desc` | A seasonal rule for places far from the equator. Followed by some mosques in Britain. |
| `method_north_america_desc` | Islamic Society of North America. Used by many mosques in the United States and Canada. |
| `method_kuwait_desc` / `method_qatar_desc` | The rule used in Kuwait / in Qatar. |
| `method_singapore_desc` | The official bodies of Indonesia and Singapore. |
| `method_turkey_desc` | The Presidency of Religious Affairs of Turkey. |
| `method_jafari_desc` | The rule commonly used among Shia. Magrib is a little after sunset, when the red colour has gone from the sky. |
| `method_tehran_desc` | The University of Tehran, whose calendar is the reference for Iran's official times. |

## wear/src/main/res/values-in/strings.xml

| Key | What the Indonesian says |
|---|---|
| `tile_label` / `tile_description` | Next prayer / The next prayer and your remaining time |
| `tile_needs_setup` | Set up on your phone |
| `wear_no_location_title` / `_body` | No location yet / Open SajdaTime on your phone, or use Mecca [Mekah] for now. |
| `wear_use_makkah` | Use Mecca [Mekah] |
| `wear_disclaimer` | An aid, not a religious authority. Times are calculated. Follow your mosque. |
| `wear_polar_notice` | Approximate. Today the sun does not rise or set here, so the times are taken from %1$d°. Ask your mosque. |
| `wear_madhab_note` | Only Asar changes. Hanafi is later. |
| `wear_qibla_needs_location` | Set your location to find the qibla direction |
| `wear_qibla_facing` | Facing the Kaaba [Kakbah] |
| `wear_qibla_from_true_north` | from true north |
| `wear_in_h_m` / `wear_in_m` | %1$dh %2$dm to go / %1$dm to go |
| `wear_tile_detail_approx` | About %1$s · %2$s |

## Things already visible from the Indonesian alone

- The phone app spells the Kaaba `Ka'bah` (four strings) and Makkah `Makkah`; the watch spells
  them `Kakbah` and `Mekah`. The glossary says `Kakbah` and `Mekah`.
- `aliran` is used for "school / sect" in five places; the glossary has no entry for it.
- `app_language_tag` is `id`, not the `in-ID` the brief asks for. (Checked afterwards: `id` is
  what `AppLanguage.INDONESIAN` and `TranslationIntegrityTest` require, so this is correct.)
- `settings_alerts_help` says a notification "sounds softly"; needs checking against the English
  quiet/silent distinction.
