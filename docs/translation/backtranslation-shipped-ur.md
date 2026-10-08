# Back-translation of the shipped Urdu (release 1.3.1)

Written 8 Oct 2026 by an independent reviewer model, **before reading the English source**. Each line
is a plain English rendering of what the Urdu in `values-ur/strings.xml` actually says, as literally as
reads naturally. Where the Urdu is ambiguous, both readings are given. Short labels whose meaning is
obvious (one or two words) are listed briefly at the end of each section. The comparison with the
English and the findings are in `review-shipped-ur.md`.

Files read: `app/src/main/res/values-ur/strings.xml`, `core/src/main/res/values-ur/strings.xml`,
`wear/src/main/res/values-ur/strings.xml`.

---

## App: onboarding and location

| Key | Back-translation of the Urdu |
|---|---|
| `welcome_tagline` | Your personal companion in prayer. Always free. |
| `permission_title` | Where are you praying? |
| `permission_body` | To find out the prayer times and the direction of the Qibla, SajdaTime needs to know roughly where you are. Tap the information icon to learn what this really means. |
| `permission_why_title` | Why we need your location |
| `permission_why_body` | So that correct prayer times and Qibla direction can be found for where you are. / We look at your approximate location only when the app is open. It stays on your phone. We never send it anywhere. |
| `location_finding` | Your location is being searched for… |
| `location_looking_up` | This place is being looked up… |
| `location_no_fix` | We could not find your location. Check that location is on, or write your city's name below. |
| `city_fallback_body` | Instead, write your city's name. Where possible, your phone finds it itself. If it cannot, only the name you wrote is sent, once, to a free search service. After that all the calculation happens on your phone. |
| `city_field_helper` | For example: Manchester, Lahore, Cairo |
| `city_not_found` | We did not find this place. Try writing the country's name with it as well. |
| `action_use_makkah` | Skip for now and use Makkah |
| `sect_title` | Which *maslak* (sect / school) do you follow? |
| `sect_body` | This decides how your prayer times are worked out. You can change it whenever you like. |
| `sect_sunni_desc` | Muslim World League times by default |
| `sect_shia_desc` | Jafari (Twelver) times |
| `madhab_title` | Choose your *fiqh* |
| `madhab_body` | Optional. This changes only the time of Asr. If you skip it you will get the standard time. |
| `madhab_hanafi_desc` | Asr later, when the shadow becomes twice the length of the object |
| `madhab_standard_desc` | Standard Asr, when the shadow becomes equal to the length of the object |
| `method_step_title` | Calculation method |
| `method_step_body` | Mosques measure morning and evening twilight in different ways, so from one method to another Fajr and Isha can differ by a few minutes, and in the far north by more than an hour. If your mosque's times differ from the app, choosing its method here usually solves the problem. Not sure? Leave the first choice. You can change it at any time in Settings. |
| `confirm_title` | Everything is ready |
| `confirm_body` | Does everything look right? You can change any of this later in Settings. |
| `label_school` | *Maslak* (sect) |

Short labels: `nav_times` Times, `nav_qibla` Qibla, `nav_settings` Settings, `action_begin` Begin,
`action_allow_location` Allow location, `action_got_it` OK, `action_understood` I have understood,
`action_continue` Continue, `action_find_city` Find city, `action_back` Back, `action_finish` Complete,
`state_selected` Selected.

## App: disclaimer (`disclaimer_body`, seven paragraphs)

Title (`disclaimer_title`): Please read this first.

1. SajdaTime is a helper, not a religious authority.
2. These times were not given to me by any mosque, any scholar (*alim*) or any authority. Your phone
   works them out from the position of the sun, and for that it uses the calculation methods that
   Islamic institutions and scholars have published. I am neither a Mufti, nor an Aalim, nor an expert
   in fiqh, and this app has been made with the help of artificial intelligence. The software can have
   faults, the phone can have a wrong location or a wrong time, while saving battery the phone can
   delay an alert, and I too can make a plain, simple mistake.
3. So please treat whatever you see here as a calculation, not a sharia ruling. Where SajdaTime and
   your mosque disagree, follow your mosque. If a time or direction seems wrong to you, or you are not
   sure, ask your mosque or some other qualified person who can guide you. This app is free and is
   given as it is, without any guarantee and without any promise of accuracy. So where being exactly
   right matters to you, please do not rely on it alone.
4. If you use a watch, please treat it as a convenience, not the final word. The watch's compass is
   smaller than the phone's and it also sees the sky less clearly, and it may be running on a location
   that your phone found a while ago. For the Qibla especially, of the two your phone is the more
   accurate. When it matters, check on the phone.
5. Mosques do not agree on when the prayer time begins. The biggest difference is in Isha. In Britain
   and further north than it, the difference between one method and another can be more than an hour,
   because scholars measure the end of twilight differently. I cannot settle this matter, and the app
   does not try to either. If the times here do not match your mosque, ask them which method they use,
   and choose it in Settings under "Calculation method". If after that a few minutes' difference still
   remains, through "Match your mosque" you can move each prayer, and the date too, forward or back
   yourself. Where a difference still remains, follow your mosque.
6. One more thing. This app tells you when the prayer time begins. Then your mosque sets its own time
   for the congregation, which is usually a little later, so there will often be a difference between
   the board there and the times here. Neither of the two is wrong.
7. And one request, if you can: please remember me, my family and my parents in your duas.

## App: home, mosque differences, polar notice

| Key | Back-translation of the Urdu |
|---|---|
| `home_until` | remaining until it starts |
| `home_now_marker_a11y` | current prayer |
| `method_notice_title` | Does this match your mosque? |
| `method_notice_body` | Here Fajr and Isha can differ by an hour or more. Tap to find out why. |
| `mosque_diff_link` / `_title` | Different from your mosque? |
| `mosque_diff_congregation` | This app shows when each prayer's time begins. Your mosque usually holds the congregation a little later, so there will often be a few minutes' difference between its board and these times. Neither of the two is wrong. |
| `mosque_diff_twilight` | Fajr and Isha depend on how twilight is measured, and mosques measure it differently. If your mosque's times differ by more than a few minutes, choose the method it follows. |
| `mosque_diff_asr` | The Asr time depends on the school of thought. Hanafi mosques begin Asr later than the others, an hour or even more later. |
| `mosque_diff_minutes` | If a few minutes' difference still remains, move each prayer a little forward or back and match it to your mosque's printed times. |
| `mosque_diff_follow` | Where a difference remains, follow your mosque. |
| `polar_notice_title` | Approximate times |
| `polar_notice_body` | This far from the equator the sun does not always rise or set, so the real time of Fajr or Isha cannot be calculated. Instead these times have been taken from latitude %1$d°. Mosques here see this matter in different ways, so ask your mosque which method it follows. |
| `polar_notice_alarm` | On these days your alarms come as a notification with a light (soft) sound instead of ringing, because the app cannot guarantee the exact minute. If you still want to wake up, turn on "Ring alarms on approximate days" in Settings. |
| `home_no_location` | Set your location to see prayer times. |
| `home_countdown_a11y` | %1$s, in %2$s |
| `home_default_location_title` | Makkah's times are being shown |
| `home_default_location_body` | We could not find out where you are. Tap to set your location. |
| `home_default_location_action` | Set my location |

## App: location sheet, export, PDF

| Key | Back-translation of the Urdu |
|---|---|
| `location_sheet_body` | Prayer times and the Qibla both depend on where you are. Whenever you travel, update it. / To protect your privacy the app always asks only for your approximate location, so it may show the name of a nearby town or a whole area instead of your city. This makes very little difference to the times: a distance of ten miles equals about one minute. If the name looks wrong to you, write your city's name below. |
| `action_use_my_location` | Use my current location |
| `location_sheet_search_heading` | Or write a city name |
| `action_save_timetable` | Save timetable as PDF |
| `export_sheet_title` | Save your timetable |
| `export_sheet_body` | We will make a PDF of your prayer times for %1$s. Then you can save it, print it, or send it to someone. |
| `export_today` / `_desc` | Today only / One day, six times |
| `export_week` / `_desc` | Next 7 days / Today and the six days after it |
| `export_month` / `_desc` | This whole month / Every day from the first of the month to the last |
| `export_saved` | Saved in Downloads under the name %1$s |
| `export_failed` | The PDF could not be made. Please try again. |
| `pdf_period_onwards` | onward from %1$s |
| `pdf_tagline` | Prayer times and Qibla, free for the Ummah |
| `pdf_area_label` | Approximate area |
| `pdf_approximate_note` | Some of these times are approximate. This far north the sun does not always rise or set, so the times for those days have been taken from latitude %1$d°. Ask your mosque which method it follows. |
| `pdf_footer` | Calculated offline on your phone. SajdaTime is free forever. |

## App: Qibla

| Key | Back-translation of the Urdu |
|---|---|
| `qibla_subtitle` | The Kaaba (*Khana Kaaba*) is about %2$d km from here, at %1$d° from north. |
| `qibla_needs_location` | Set your location to find the Qibla. |
| `qibla_facing` | You are now facing the Kaaba |
| `qibla_heading_now` | Facing %1$d° |
| `qibla_legend_kaaba` / `_facing` | Khana Kaaba / Your facing |
| `qibla_turn_left` | Turn %1$d° to the left (an instruction to the body) |
| `qibla_turn_right` | Turn %1$d° to the right (an instruction to the body) |
| `qibla_no_compass` | Face towards %1$d° from true north |
| `qibla_calibrate` | The compass must be calibrated. Hold the phone in your hand and move it a few times in the shape of an 8. |
| `qibla_sensor_missing` | This phone has no compass, so we can tell you the angle but cannot point towards the direction. |
| `qibla_accuracy_medium` | The reading may wander here and there a little. For a steadier result move away from metal, magnets and speakers. |

## App: settings, alerts and permissions

| Key | Back-translation of the Urdu |
|---|---|
| `settings_group_appearance` | Appearance |
| `theme_system` / `_light` / `_dark` | As the phone / Light / Dark |
| `settings_group_reminders` | Reminders |
| `settings_school` | School of thought (*maktab-e-fikr*) |
| `settings_madhab` | Fiqh |
| `settings_alerts` | Prayer alerts |
| `settings_which_prayers_all` / `_some` / `_none` | All five / %1$d of 5 / None |
| `settings_method_auto` | Automatic · %1$s |
| `settings_method_help` | Mosques measure twilight differently, and this mostly affects Fajr and Isha. The further north you live, the bigger the difference. If these times don't match your mosque, ask them which method they use and choose it here. |
| `settings_alerts_help` | Choose this separately for each prayer. A notification comes with a light (soft) sound and vibrates. An alarm plays your chosen sound loudly, just like a wake-up alarm. |
| `alert_style_off` / `_notification` / `_alarm` / `_mixed` | Off / Notification / Alarm / Mixed |
| `settings_alarm_sound_desc` | Choose any tone or adhan already on your phone |
| `settings_alarm_respect_silent` | Stay silent when the phone is on silent |
| `settings_alarm_respect_silent_desc` | On: you get the notification on time, but without sound. Off: the alarm rings even when the phone is on silent, just like a wake-up alarm. |
| `settings_dnd_title` | Allow sound in Do Not Disturb |
| `settings_dnd_desc` | Without this, when the phone is on Do Not Disturb your alarm stays silent. Tap to allow. |
| `settings_ongoing_badge` | Keep the next prayer in the notification bar |
| `settings_ongoing_badge_desc` | A silent notification that tells which prayer is next and how much time you have |
| `settings_exact_alarms_title` | Allow exact-time alarms |
| `settings_exact_alarms_desc` | Without this your phone can hold back prayer alerts by an hour or more, and on some phones a night alert like Fajr may not come at all. Tap to allow. |
| `confirm_exact_alarm_title` | One last thing |
| `confirm_exact_alarm_body` | Until you allow it, Android does not let apps set alerts at the exact minute. Without this your phone can hold back prayer alerts by an hour or more, and on some phones a night alert like Fajr may not come at all. You can also do this later from Settings. |
| `about_disclaimer_short` | A helper, not a religious authority. Tap to read. |
| `about_privacy_desc` | Your location stays on this phone. No account, no adverts. Usage counts are optional and stay off until you turn them on. Tap to read the full policy. |
| `settings_language_desc` | Choose the app's language |
| `language_unavailable` | The translation is waiting for review by native speakers |
| `permission_allowed` | Allowed |
| `state_expanded` / `_collapsed` | Open / Closed |
| `about_feedback_desc` | Tell the developer what is helpful or what is wrong. Your email app will open. |
| `settings_usage_counts` | Share usage counts |
| `settings_usage_counts_desc` | Sent to Google, linked to a random identification number (ID). Stays off until you turn it on. On turning it on, exactly what is sent is shown. |
| `about_charity` / `_desc` | Free forever / Made with love, free for the Ummah. A charitable project for the pleasure of Allah. |
| `about_credits` / `_desc` | Prepared by / Ali Imran Khan, as a *sadaqah jariyah* for the Ummah |
| `about_data_desc` | Prayer times are calculated with adhan-java (MIT) by Batoul Apps. City search uses the Open-Meteo geocoding API, licensed under CC BY 4.0. |

## App: consent (usage counts)

**`consent_title`**: Help make SajdaTime better?

**`consent_body`**:

> SajdaTime is free and has no advert in it. If you say yes, it sends a few usage counts to Google
> Analytics so that the app can be improved. They are linked not to your name but to a random
> identification number (ID), and they may be processed outside Britain. Detailed records are kept
> for 14 months.
>
> Using a prayer app can reveal something about your faith, so this decision is yours, and if you
> say no [literally *"if you don't say"*, see review] nothing changes in the app. SajdaTime never
> sends your GPS position or your prayer settings.
>
> Until you choose yes, nothing is sent. You can turn it off at any time in Settings. Responsible for
> it is Ali Imran Khan, who made this app.

| Key | Back-translation of the Urdu |
|---|---|
| `consent_see_sent` / `_hide_sent` | See exactly what is sent / Hide details |
| `consent_sent_heading` | What is sent if you say yes |
| `consent_sent_list` | • Which main screen you open: Times, Qibla or Settings • Which steps of setup you reach • Whether you allow notifications, location and exact-time alarms (only yes or no for each, never the location itself) • How often and for how long the app is open • Your phone's model, Android version, app version and language • When you first opened the app, and how you found this app (for example a link on Google Play) • Your approximate area, which Google works out from your internet connection |
| `consent_never_heading` | What is never sent |
| `consent_never_list` | • Your GPS position or coordinates • The place you write • Your *maslak*, your setting for the calculation of prayer, your calculation method and your alert settings • Anything you have written |
| `consent_yes` / `consent_no` | Yes, share the counts / No, thanks |
| `consent_read_policy` | Read the privacy policy |
| `consent_policy_english` | The full privacy (*raazdari*) policy is in English. |

## App: notifications, Hijri, durations

| Key | Back-translation of the Urdu |
|---|---|
| `channel_prayers_desc` | A notification when each prayer begins |
| `channel_alarm_desc` | A louder alert with your chosen sound |
| `channel_ongoing` | Next prayer badge |
| `channel_ongoing_desc` | A silent reminder of the next prayer, always in the notification bar |
| `notif_prayer_title` | Time of %1$s |
| `notif_prayer_body` | The time of %1$s begins at %2$s |
| `notif_next_title` | %1$s, at %2$s |
| `notif_next_body` | in %1$s |
| `hijri_months` | Muharram, Safar, Rabi al-Awwal, Rabi al-Thani, Jumada al-Awwal, Jumada al-Thani, Rajab, Shaban, Ramadan, Shawwal, Dhul-Qadah (written ذوالقعدہ), Dhul-Hijjah |
| `duration_h_m` / `_m` / `_s` | %1$d hours %2$d minutes / %1$d minutes / %1$d seconds |

## App: Match your mosque, approximate days, late block

| Key | Back-translation of the Urdu |
|---|---|
| `settings_adjustments` | Match your mosque |
| `settings_adjustments_none` | No change |
| `settings_adjustments_help` | If your mosque's printed time is different, move it a little forward or back here. First try changing the calculation method. Usually that is the real difference, and this is for the few minutes left over. |
| `settings_adjustments_reset` | Reset all |
| `settings_hijri_offset` / `_label` | Islamic date / Move the date forward or back |
| `settings_hijri_offset_help` | Here the Islamic date has been worked out by calculation. If your mosque sights the moon locally, there can be a difference of one day ahead or behind. This also moves Ramadan and Eid forward or back. |
| `adjustment_days` | %+d day(s) |
| `adjustment_summary_times` | change in %d prayer / change in %d prayers |
| `adjustment_summary_hijri` | Date %1$s |
| `action_decrease` / `_increase` | Decrease %1$s / Increase %1$s |
| `notif_approximate` | Approximate time |
| `settings_alarm_on_approximate` | Ring alarms on approximate days |
| `settings_alarm_on_approximate_desc` | When the sun does not rise or set, the times are taken from some lower latitude. On those days alarms do not ring, only a notification with a light (soft) sound comes, unless you turn this on. |
| `about_translation` | Translation |
| `about_translation_note` | This translation has been made with the help of AI (artificial intelligence) and may contain mistakes. If you see one, please tell us through "Send feedback". |
| `action_allow_in_settings` | Allow in Settings |
| `action_cancel` / `action_set` | Cancel / Set |
| `adjustment_choose_method` | Choose calculation method |
| `adjustment_limit_note` | A difference of more than %1$d minutes is usually because of a different calculation method, or because the mosque's board shows the congregation time, which is a little later. Try the method first. |
| `adjustment_now_shows` | Shows: %1$s |
| `adjustment_pick_time_description` | %1$s: set the time that your mosque shows |
| `adjustment_pick_time_title` | %1$s: the time on your mosque's board |
| `adjustment_zero_minutes` | 0 minutes |
| `language_phone` | Phone's language |
| `method_notice_action` | See why |

## Core: prayer names and methods

Prayer names: فجر Fajr, طلوع آفتاب Sunrise (written without the *izafat* zer), ظہر Dhuhr, عصر Asr,
مغرب Maghrib, عشاء Isha. `app_language_tag` is `ur`.

| Key | Back-translation of the Urdu |
|---|---|
| `method_auto` | Automatic |
| `method_muslim_world_league` … `method_tehran` | Muslim World League; Egyptian General Authority; Karachi University; Umm al-Qura, Makkah; Dubai; Moon Sighting Committee; ISNA, North America; Kuwait; Qatar; Kemenag & MUIS, Indonesia and Singapore; Diyanet, Turkey; Jafari (Twelver); Institute of Geophysics, Tehran (all in Urdu script except ISNA, Kemenag, MUIS) |
| `method_auto_desc` | Not sure? Leave it. Muslim World League times, or Jafari for Shia. Close to local practice in a large part of the world. The biggest difference is in the far north. |
| `method_muslim_world_league_desc` | A general method used in many countries. Here this is the standard one. |
| `method_egyptian_desc` | Egypt's official method. |
| `method_karachi_desc` | Common in Pakistan and South Asia. |
| `method_umm_al_qura_desc` | Saudi Arabia's official method. The Isha time is set at a fixed interval after Maghrib. |
| `method_dubai_desc` | A method used in the Gulf. |
| `method_moon_sighting_desc` | A seasonal rule made for regions far from the equator. Some mosques in Britain follow it. |
| `method_north_america_desc` | Islamic Society of North America. Many mosques in America and Canada use it. |
| `method_kuwait_desc` / `method_qatar_desc` | The method used in Kuwait / in Qatar. |
| `method_singapore_desc` | The official bodies of Indonesia and Singapore. |
| `method_turkey_desc` | Turkey's Presidency of Religious Affairs (Diyanet). |
| `method_jafari_desc` | The common Shia method. Maghrib is some while after sunset, when the redness has gone from the sky. |
| `method_tehran_desc` | Tehran University, by whose calendar Iran's official times are set. |

## Wear

| Key | Back-translation of the Urdu |
|---|---|
| `tile_label` | Next prayer |
| `tile_description` | The next prayer and how much time is left |
| `tile_needs_setup` | Set up on your phone |
| `wear_locating` | Your location is being searched for |
| `wear_no_location_title` | No location yet |
| `wear_no_location_body` | Open SajdaTime on your phone, or use Makkah al-Mukarramah for now. |
| `wear_use_makkah` | Use Makkah al-Mukarramah |
| `wear_disclaimer` | It is a helper, not a religious authority. The times are from calculation. Follow your mosque. |
| `wear_polar_notice` | Approximate. Here today the sun does not rise or set, so these are from %1$d° latitude. Ask your mosque. |
| `wear_school` | School of thought |
| `wear_madhab_note` | Only Asr changes. Hanafi is late. |
| `wear_qibla_needs_location` | Set your location to find the Qibla |
| `wear_qibla_facing` | Facing is towards the Kaaba |
| `wear_qibla_from_true_north` | from true north |
| `wear_duration_h_m` / `wear_in_h_m` | %1$d hours %2$d minutes (both identical; no word for "in") |
| `wear_duration_m` / `wear_in_m` | %1$d minutes (both identical) |
| `wear_tile_detail_approx` | approximately %1$s · %2$s |
