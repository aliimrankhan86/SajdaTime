# Back-translation of the shipped Turkish (release 1.3.1)

Made 8 Oct 2026 by an independent reviewer (a second AI model, not the one that translated). This is
step 1 of the review: what the Turkish **actually says**, written in plain English **before** the
English source was opened, so the reading is not coloured by what the text was meant to say. The
comparison with the English and the list of fixes are in `review-shipped-tr.md`.

Files read: `app/src/main/res/values-tr/strings.xml`, `core/src/main/res/values-tr/strings.xml`,
`wear/src/main/res/values-tr/strings.xml`.

Short labels (one or two words) are listed only where the wording itself is worth seeing.
Literal glosses are in [square brackets] where the Turkish carries a nuance English loses.

---

## app: onboarding

| Key | What the Turkish says |
|---|---|
| `welcome_tagline` | Your prayer companion, [made] special for you. Always free. |
| `permission_title` | Where do you pray? |
| `permission_body` | SajdaTime needs [lit. "must"] to know roughly where you are to calculate your prayer times and the qibla direction. Tap the info icon to see exactly what this means. |
| `permission_why_title` | Why we need your location |
| `permission_why_body` | To calculate correct prayer times and qibla direction for where you are.<br><br>We look at your approximate location only while the app is open. Your location stays on your phone. We do not send it anywhere. |
| `location_no_fix` | Could not get a location. Check that location is on, or type your city below. |
| `city_fallback_body` | Type your city instead. Your phone finds it itself if it can. If it cannot, only the name you typed is sent, once, to a free search service. After that everything is calculated on your phone. |
| `action_use_makkah` | Skip for now, use Makkah |
| `sect_title` | Which *mezhep* do you follow? [*mezhep* is the same Turkish word used below for the fiqh school] |
| `sect_body` | This choice decides how your prayer times are calculated. You can change it any time. |
| `sect_sunni_desc` | Muslim World League times by default |
| `sect_shia_desc` | Jafari (Ithna Ashari) times |
| `madhab_title` | Choose your fiqh *mezhep* |
| `madhab_body` | Optional. This changes only the Asr time. If you skip it, the standard time is used. |
| `madhab_hanafi_desc` | Later Asr: when the shadow is twice the object's height |
| `madhab_standard_desc` | Standard Asr: when the shadow equals the object's height |
| `method_step_title` | Calculation method |
| `method_step_body` | Mosques measure dawn and evening twilight differently. So Fajr and Isha times may differ from one method to another by a few minutes, and in the far north by more than an hour. If your mosque's times differ from the app's, the solution is usually to choose your mosque's method here. Not sure? **Leave the first option** [*İlk seçeneği bırakın*: most readers will take this as "leave it as it is", but it can also be read as "drop / abandon the first option"]. You can change this any time in Settings. |
| `confirm_title` | Ready |
| `confirm_body` | Is everything right? You can change all of these later in Settings. |
| `label_school` | *Mezhep* |

## app: disclaimer (`disclaimer_body`, seven paragraphs)

1. SajdaTime is a helper, not a religious authority.
2. These times were not given to me by a mosque, a scholar (*âlim*) or any authority. Your phone calculates them from the position of the sun. In doing so it uses calculation methods published by Islamic authorities and scholars. I am not a mufti, an *âlim* or a fiqh expert. This app was made with the help of artificial intelligence. There may be mistakes in the software. The phone may have a wrong location or a wrong time stored. A phone that is saving battery may delay an alert. I may also simply be wrong.
3. So please take what you see here as a calculation, not as a religious ruling. If SajdaTime and your mosque say different things, follow your mosque. If a time or direction looks wrong to you, or you are not sure, ask your mosque or another qualified person who can guide you. The app is free and is offered as it is. There is no warranty and no promise of accuracy. So where exact accuracy matters to you, please do not rely on the app alone.
4. If you use the smartwatch, please treat it as a convenience, not the last word. The smartwatch's compass is smaller than the phone [lit. "smaller than the phone", not "than the phone's"] and sees the sky less well. It may also be working from a location your phone found a while ago. For the qibla especially, your phone is the more accurate of the two. When it matters, check on your phone.
5. Mosques do not always agree on when a prayer begins. The biggest difference is at Isha. In Britain and further north the difference between one method and another can be more than an hour, because scholars measure the disappearance of twilight differently. I cannot settle this, and the app does not try to. If the times here do not match your mosque, ask them which method they use and choose that method in Settings, under Calculation method. If a difference of a few minutes still remains after that, you can correct each time and the date by hand in Match your mosque [*Camiye göre ayarla*, lit. "Adjust to the mosque"]. If a difference still remains, follow your mosque.
6. One more thing. This app shows when a prayer time comes in. Your mosque sets its own time for the congregation, and that is usually a little later. So the timetable at the mosque and the times here will often differ. Neither is wrong.
7. I also have a request, if you think it fitting: please remember me, my family and my parents in your duas.

## app: home, notices, location

| Key | What the Turkish says |
|---|---|
| `home_next_prayer` | NEXT [PRAYER] TIME |
| `home_until` | remaining until it starts |
| `home_now_marker_a11y` | The current [prayer] time [*Şu anki vakit*; could be heard as "the current time" of day] |
| `method_notice_title` | Does it match your mosque? |
| `method_notice_body` | Here Fajr and Isha may differ by an hour or more. Tap to see why. |
| `mosque_diff_congregation` | This app shows when each prayer time comes in. Your mosque usually holds the congregation a little later. So the timetable at the mosque and the times here often differ by a few minutes. Neither is wrong. |
| `mosque_diff_twilight` | Fajr and Isha times depend on how twilight is measured, and mosques measure it differently. If the difference from your mosque is more than a few minutes, choose the method your mosque follows. |
| `mosque_diff_asr` | Asr time depends on the *mezhep*. In Hanafi mosques Asr begins later than in the others. The difference can reach an hour, or even more. |
| `mosque_diff_minutes` | If there is still a few minutes' difference, shift each time slightly to match the times your mosque publishes. |
| `mosque_diff_follow` | If a difference remains, follow your mosque. |
| `polar_notice_title` | Approximate times |
| `polar_notice_body` | This far from the equator the sun does not always rise or set. So there is no real Fajr or Isha time that can be calculated. These times are instead calculated on the basis of latitude %1$d°. Mosques here handle this in different ways. Ask your mosque which way it follows. |
| `polar_notice_alarm` | On these days your alarms come as a quieter notification instead of ringing, because the app cannot guarantee the exact minute of the time. If you still want to be woken, turn on "Ring on days with approximate times" in Settings. |
| `home_no_location` | Set your location to see prayer times. |
| `home_countdown_a11y` | %1$s, in [lit. "after"] %2$s |
| `home_default_location_title` | Showing Makkah times |
| `home_default_location_body` | We could not find where you are. Tap to set your location. |
| `location_sheet_body` | Prayer times and the qibla both depend on where you are. Update this when you travel.<br><br>To protect your privacy the app asks only for your approximate location. So it may show the name of a nearby town or a wider area instead of your own place. This changes the times very little: 16 km comes to about one minute. If the name looks wrong to you, type your city below. |
| `action_use_my_location` | Use my current location |
| `location_sheet_search_heading` | Or type a city |

## app: export and PDF

| Key | What the Turkish says |
|---|---|
| `action_save_timetable` | Save the timetable as PDF |
| `export_sheet_title` | Save your timetable |
| `export_sheet_body` | We will prepare a PDF file of your prayer times. Location: %1$s. Then you can save the file, print it or send it to someone. |
| `export_today` / `_desc` | Today only / One day, six times |
| `export_week` / `_desc` | The next 7 days / Today and the six days after |
| `export_month` / `_desc` | The whole of this month / Every day from the 1st of the month to the end |
| `export_saved` | Saved to the Downloads folder: %1$s |
| `export_failed` | The PDF could not be created. Please try again. |
| `pdf_period_onwards` | as of %1$s |
| `pdf_tagline` | Prayer times and qibla, free for the Ummah |
| `pdf_area_label` | Approximate area |
| `pdf_approximate_note` | Some of these times are approximate. This far north the sun does not always rise or set. So the times on those days are calculated on the basis of latitude %1$d°. Ask your mosque which way it follows. |
| `pdf_footer` | Calculated on your device without internet. SajdaTime is always free. |

## app: Qibla

| Key | What the Turkish says |
|---|---|
| `qibla_subtitle` | The Kaaba is about %2$d km from here. Its direction is %1$d° from north. |
| `qibla_needs_location` | Set your location to find the qibla. |
| `qibla_facing` | You are now facing the Kaaba |
| `qibla_heading_now` | Direction you are facing: %1$d° |
| `qibla_turn_left` / `_right` | Turn left: %1$d° / Turn right: %1$d° |
| `qibla_no_compass` | Turn to %1$d° from true north |
| `qibla_calibrate` | The compass needs calibrating. Hold your phone and move it a few times as if drawing the number 8 in the air. |
| `qibla_sensor_missing` | This phone has no compass. So we can give you the angle but cannot show the direction. |
| `qibla_accuracy_medium` | The reading may wobble a little. For a steadier result, move away from metal, magnets and speakers. |

## app: settings, alerts and permissions

| Key | What the Turkish says |
|---|---|
| `theme_system` | Follow the phone |
| `settings_madhab` | Fiqh *mezhep* |
| `settings_alerts` | Prayer alerts |
| `settings_which_prayers_all` | All five [prayer] times |
| `settings_method_auto` | Automatic · %1$s |
| `settings_method_help` | Mosques measure twilight differently. This mostly affects the Fajr and Isha times. The further north you live, the bigger the difference. If these times do not match your mosque, ask them which method they use and choose that method here. |
| `settings_alerts_help` | Choose this separately for each prayer. A notification is quieter and vibrates. An alarm plays the sound you choose loudly, like a wake-up alarm. |
| `alert_style_mixed` | Mixed |
| `settings_alarm_sound_desc` | Choose any ringtone on your phone, or the adhan [*ezanı*, definite: "the adhan", as if the app supplied one] |
| `settings_alarm_respect_silent` | Stay silent when the phone is on silent |
| `settings_alarm_respect_silent_desc` | On: you still get the notification on time, but no sound plays. Off: even when the phone is on silent, the alarm rings like a wake-up alarm. |
| `settings_dnd_title` | Allow sound in Do Not Disturb mode |
| `settings_dnd_desc` | Without this permission, your alarm stays silent when the phone is in Do Not Disturb mode. Tap to allow. |
| `settings_ongoing_badge` | Keep the next prayer time in the notification panel |
| `settings_ongoing_badge_desc` | A silent notification showing the next prayer time and how long is left |
| `settings_exact_alarms_title` | Allow exact-time alarms |
| `settings_exact_alarms_desc` | Without this permission your phone may delay prayer alerts by an hour or more. On some phones an alert that comes at night, such as Fajr, may not come at all. Tap to allow. |
| `confirm_exact_alarm_title` | One last thing |
| `confirm_exact_alarm_body` | Unless you allow it, Android does not let apps set alerts for the exact minute. Without this permission your phone may delay prayer alerts by an hour or more. On some phones an alert that comes at night, such as Fajr, may not come at all. You can also do this later in Settings. |
| `about_disclaimer` | Disclaimer [*Sorumluluk reddi*, the legal term] |
| `about_disclaimer_short` | A helper, not a religious authority. Tap to read. |
| `about_privacy_desc` | Your location stays on this phone. No account, no ads. Usage counts are optional and are off unless you turn them on. Tap to read the full policy. |

## app: usage counts and consent

| Key | What the Turkish says |
|---|---|
| `consent_title` | Will you help improve SajdaTime? |
| `consent_body` | SajdaTime is free and has no ads. If you say yes, it sends a few usage counts to Google Analytics so that the app can be improved. These counts are tied to a random ID, not to your name, and may be processed outside the United Kingdom. Detailed records are kept for 14 months.<br><br>Using a prayer app can say something about your faith. So the decision is yours, and if you say no nothing changes in the app. SajdaTime never sends your GPS location or your prayer settings.<br><br>Nothing is sent until you say yes. You can turn this off any time in Settings. The person responsible is Ali Imran Khan, who made the app. |
| `consent_see_sent` | See exactly what is sent |
| `consent_hide_sent` | Hide details |
| `consent_sent_heading` | What is sent if you say yes |
| `consent_sent_list` | • Which main screen you open: Times, Qibla or Settings<br>• Which steps of the setup you reached<br>• Whether you allowed notifications, location and exact-time alarms (only yes or no for each, never the location itself)<br>• How often and for how long the app is open<br>• Your phone model, your Android version, the app version and your language<br>• When you first opened the app and how you found it (for example a link on Google Play)<br>• Your approximate area, which Google works out from your internet connection |
| `consent_never_heading` | What is never sent |
| `consent_never_list` | • Your GPS location or coordinates<br>• The place name you type<br>• Your *mezhep* [one word that covers both Sunni/Shia and the fiqh school], your prayer calculation setting, your calculation method and your alert settings<br>• Anything you type |
| `consent_yes` | Yes, share usage counts |
| `consent_no` | No, thanks |
| `consent_read_policy` | Read the privacy policy |
| `settings_usage_counts` | Share usage counts |
| `settings_usage_counts_desc` | Sent to Google and tied to a random ID. Off unless you turn it on. When you turn it on, exactly what is sent is shown. |
| `consent_policy_english` | The full text of the privacy policy is in English. |

## app: about

| Key | What the Turkish says |
|---|---|
| `settings_language_desc` | Choose the app's language |
| `language_unavailable` | The translation is waiting to be reviewed by someone whose native language this is |
| `about_feedback_desc` | Tell the developer what works or what is wrong. Opens your email app. |
| `about_charity` | Always free |
| `about_charity_desc` | Made with love, free for the Ummah. A charity project for the sake of Allah [*Allah rızası için*: for Allah's pleasure]. |
| `about_credits` | Made by |
| `about_credits_desc` | Ali Imran Khan, as a *sadaka-i câriye* (sadaqah jariyah) for the Ummah |
| `about_data_desc` | Prayer times are calculated with adhan-java (MIT), developed by Batoul Apps. City search uses the Open-Meteo geocoding API, licensed CC BY 4.0. |

## app: notifications

| Key | What the Turkish says |
|---|---|
| `channel_prayers_desc` | A notification as each prayer time comes in |
| `channel_alarm_desc` | A louder alert with the sound you choose |
| `channel_ongoing` | Next prayer time badge |
| `channel_ongoing_desc` | A silent reminder that always stays in the notification panel, showing the next prayer time |
| `notif_prayer_title` | %1$s time [e.g. "Öğle vakti" = "Dhuhr time"] |
| `notif_prayer_body` | %1$s time is coming in as of %2$s [*itibarıyla giriyor*: "as of 12:30 it is entering"; stilted] |
| `notif_next_body` | Time left: %1$s |

## app: Hijri months

Muharrem, Safer, Rebiülevvel, Rebiülahir, Cemaziyelevvel, Cemaziyelahir, Recep, Şaban, Ramazan,
Şevval, Zilkade, Zilhicce (the Diyanet spellings of the twelve months, in order).

## app: Match your mosque (`settings_adjustments` family) and the late block

| Key | What the Turkish says |
|---|---|
| `settings_adjustments` | Adjust to the mosque [lit. "Set according to the mosque"] |
| `settings_adjustments_none` | No change |
| `settings_adjustments_help` | If your mosque publishes a different time, shift it a little here. Try the calculation method first. The main difference is usually there. This section is for the few minutes that are left. |
| `settings_hijri_offset_label` | Shift the date |
| `settings_hijri_offset_help` | The Hijri date here is calculated. If your mosque sights the crescent locally, the date may be a day ahead or behind. This setting also shifts Ramadan and the Eids [*bayramları*]. |
| `adjustment_summary_times` | Adjusted [prayer] time: %d [reads like "Adjusted time: 3", not "3 times adjusted"] |
| `adjustment_summary_hijri` | Date %1$s |
| `action_decrease` / `action_increase` | %1$s: decrease / %1$s: increase |
| `notif_approximate` | Approximate time |
| `settings_alarm_on_approximate` | Ring on days with approximate times |
| `settings_alarm_on_approximate_desc` | When the sun does not rise or does not set, the times are calculated on the basis of a lower latitude. Unless you turn this on, no alarm rings on those days; only a notification comes. |
| `about_translation_note` | This translation was made with the help of artificial intelligence and may contain mistakes. If you see a mistake, please tell us using Send feedback. |
| `action_allow_in_settings` | Allow in settings |
| `action_set` | Set |
| `adjustment_choose_method` | Choose a calculation method |
| `adjustment_limit_note` | A difference larger than %1$d minutes usually comes from a different calculation method, or from a mosque board that shows the congregation time, which is a little later. Try the method first. |
| `adjustment_now_shows` | Shown: %1$s |
| `adjustment_pick_time_description` | Set the time your mosque shows for %1$s |
| `adjustment_pick_time_title` | %1$s: the time on your mosque's board |
| `home_default_location_action` | Set my location |
| `language_phone` | The phone's language |
| `method_notice_action` | See why |

## core: prayer names

Fajr = *Sabah* (lit. "Morning"; in Turkish the dawn prayer is *sabah namazı*), Sunrise = *Güneş* ("Sun"),
Dhuhr = *Öğle*, Asr = *İkindi*, Maghrib = *Akşam*, Isha = *Yatsı*.

## core: method names and descriptions

| Key | What the Turkish says |
|---|---|
| `method_auto` | Automatic |
| `method_muslim_world_league` | Muslim World League (MWL) |
| `method_egyptian` | Egyptian General Research Authority [*Araştırma* = "Research"; "Survey" would be *Ölçme*] |
| `method_karachi` | University of Karachi |
| `method_umm_al_qura` | Umm al-Qura, Makkah |
| `method_moon_sighting` | Moonsighting Committee (Crescent Sighting Committee) |
| `method_north_america` | ISNA, North America |
| `method_singapore` | Kemenag and MUIS, Indonesia and Singapore |
| `method_turkey` | Diyanet, Türkiye |
| `method_jafari` | Jafari (Ithna Ashari) |
| `method_tehran` | Tehran Institute of Geophysics |
| `method_auto_desc` | Not sure? Leave this selected. Muslim World League times, Jafari times for Shia. Close to local practice in many parts of the world. The biggest difference is in the far north. |
| `method_muslim_world_league_desc` | A general method used in many countries. The default method here. |
| `method_egyptian_desc` | Egypt's official method. |
| `method_karachi_desc` | Common in Pakistan and South Asia. |
| `method_umm_al_qura_desc` | Saudi Arabia's official method. Isha begins a fixed time after Maghrib. |
| `method_dubai_desc` | A method used in the Gulf region. |
| `method_moon_sighting_desc` | A rule that changes with the season, designed for places far from the equator. Some mosques in Britain follow it. |
| `method_north_america_desc` | Islamic Society of North America (ISNA). Many mosques in the US and Canada use it. |
| `method_kuwait_desc` / `method_qatar_desc` | The method used in Kuwait / in Qatar. |
| `method_singapore_desc` | The official bodies of Indonesia and Singapore. |
| `method_turkey_desc` | The Presidency of Religious Affairs of the Republic of Türkiye. |
| `method_jafari_desc` | The method Shia commonly use. Maghrib begins a little after sunset, when the redness in the sky has gone. |
| `method_tehran_desc` | University of Tehran. Iran's official times are based on this institution's calendar. |

## wear

| Key | What the Turkish says |
|---|---|
| `tile_label` | Next prayer |
| `tile_description` | The next prayer and how much time you have left |
| `tile_needs_setup` | Set up from your phone |
| `wear_locating` | Finding your location |
| `wear_no_location_title` | No location yet |
| `wear_no_location_body` | Open SajdaTime on your phone, or use Makkah for now. |
| `wear_disclaimer` | A helper, not a religious authority. The times are calculated. Follow your mosque. |
| `wear_polar_notice` | Approximate. Today the sun does not rise and set here [stated as a flat fact, no "fully" or "always"], so these are from latitude %1$d°. Ask your mosque. |
| `wear_madhab_note` | Only Asr changes. Hanafi is later. |
| `wear_qibla_needs_location` | Set your location to find the qibla direction |
| `wear_qibla_facing` | You are facing the Kaaba |
| `wear_qibla_from_true_north` | from true north |
| `wear_in_h_m` / `wear_in_m` | %1$dh %2$dm / %1$dm [same as the plain duration: no word for "in"] |
| `wear_tile_detail_approx` | Approx. %1$s · %2$s |
