# Turkish translation review (tr-TR)

Independent review, 8 Oct 2026, by a second model that did not make the translation. Method: the
three Turkish files were back-translated into English first, without looking at the English
(`backtranslation-tr-TR.md`). Then that was compared with the English sources, their translator
comments, and the translator's notes. `tools/check-translation.py tr tr-TR` prints `0 error(s), 0
warning(s)`. Nothing was built or run on a phone or watch.

## 1. Verdict

The translation is careful and faithful. The disclaimer keeps all four points, the humble
first-person voice, "follow your mosque", "no warranty", and the dua in the last paragraph only. The
consent text says exactly what the English says, no more and no less. There are **two blockers**,
both small wording fixes: Turkish calls ordinary notifications "silent" when the phone actually
makes a sound, and the "Not sure? Keep this" advice can be read as "drop this".

## 2. Blockers

### B1. "Quiet" became "silent", which is not what the phone does

Keys: `settings_alerts_help`, `polar_notice_alarm`, `settings_alarm_on_approximate_desc`.

The English says a notification is *quiet* (quieter than an alarm). The Turkish says it is
**sessiz**, and on Android in Turkish "sessiz bildirim" means a notification that makes **no sound
at all**. That is false. The prayer notification channel is high-importance with the phone's normal
notification sound (`Notifications.kt` line 61). On approximate days an alarm becomes this same
notification (`Notifications.kt` line 330), so it still makes a sound. A user who picks
"Notification" so their phone stays silent in a meeting or at the mosque will hear it go off.

- `settings_alerts_help` now says: "A notification is **silent** and vibrates."
  Should say: "A notification is quieter and vibrates."
  Replacement: `Bunu her namaz için ayrı seçin. Bildirim daha sessizdir ve titreşir. Alarm ise seçtiğiniz sesi, bir uyandırma alarmı gibi yüksek sesle çalar.`
- `polar_notice_alarm` now says: "your alarms arrive as a **silent** notification".
  Replacement: `Bu günlerde alarmlarınız çalmak yerine daha sessiz bir bildirim olarak gelir, çünkü uygulama tam dakikaya kefil olamaz. Yine de uyandırılmak isterseniz Ayarlar bölümünde “Yaklaşık günlerde çal” seçeneğini açın.`
- `settings_alarm_on_approximate_desc` now says: "alarms stay **silent** on those days".
  Replacement: `Güneş doğup batmadığında vakitler daha düşük bir enlemden hesaplanır. Bunu açmadıkça o günlerde alarm çalmaz, yalnızca bildirim gelir.`

The places that really are silent are correct as they are and should stay: `settings_dnd_desc`,
`settings_alarm_respect_silent`, `settings_ongoing_badge_desc`, `channel_ongoing_desc`.

### B2. "Keep the first option" can be read as "drop it"

Keys: `method_step_body`, `method_auto_desc`.

"İlk seçeneği bırakın" and "Bunu bırakın" were meant as "leave it as it is". But **bırakmak**
with an object also commonly means "give it up / drop it". This is the one piece of advice the
method step gives an unsure user, and the source comment warns that a wrong pick moves Isha by an
hour in the north. If the advice reads the wrong way round, it sends the unsure user to pick
something at random.

- `method_step_body`: replace `İlk seçeneği bırakın.` with `İlk seçeneği seçili bırakın.` ("leave the first option selected"). Rest unchanged.
- `method_auto_desc`, full replacement (this also softens "most of the world" back to the English "much of the world"):
  `Emin değil misiniz? Bunu seçili bırakın. Muslim World League vakitleri, Şiiler için ise Jafari. Dünyanın birçok yerinde yerel uygulamaya yakındır. En çok uzak kuzey farklılık gösterir.`

## 3. Should fix

### S1. Disclaimer, watch paragraph: the compass "sees less of the sky"

Key: `disclaimer_body`, paragraph 4. The Turkish makes the watch's *compass* the thing that "sees
less of the sky". The English says the *watch* has a smaller compass and a poorer view of the sky
(the sky matters for the location fix, not for the compass). Replace the sentence
`Bir saatin pusulası telefona göre daha küçüktür ve gökyüzünü daha az görür, ayrıca telefonunuzun bir süre önce bulduğu bir konumla çalışıyor olabilir.`
with
`Saatin pusulası telefonunkinden daha küçüktür ve saat gökyüzünü telefon kadar iyi göremez. Ayrıca saat, telefonunuzun bir süre önce bulduğu bir konumla çalışıyor olabilir.`

### S2. Muslim World League: "the standard here" reads as "the standard in this country"

Key: `method_muslim_world_league_desc`. "Buradaki standart" reads in Turkish as "the standard in
this place". In Turkey that is a false claim, because Diyanet is the standard there, and Diyanet is
listed just below it. (In this app the two give identical times, both 18°/17°, HANDOVER §10, so
nobody's times are affected. The wrong part is what it says about who has authority.)
Replacement: `Birçok ülkede kullanılan genel bir yöntem. Bu uygulamada varsayılan.` ("The default in
this app.") **The English "The standard here" has the same ambiguity.** That is reported to you
here, not fixed.

### S3. Watch tile lines are longer than the space the source allows

Keys: `wear_in_h_m`, `wear_in_m`, `wear_tile_detail_approx`, and `wear_disclaimer`. The source
comment gives a tile line about 8 characters for the countdown. "2sa 10dk sonra" is 14 (English "in
2h 10m" is 9), and the approximate line "Yaklaşık 05:14 · 2sa 10dk sonra" is 31 against the
English 25. The watch disclaimer has a hard three-line limit, and Turkish words are longer, so a
character count does not prove it fits. Before release, run `./tools/wear-verify.sh` on these
screens. If the tile clips, use `%1$dsa %2$ddk` and `%1$ddk` (drop "sonra") in `wear_in_*`.
Keep "Yaklaşık" spelled out, because a screen reader has to say it.

## 4. Nits

- `home_until`: "başlamasına kaldı" under a countdown is understood, but plain `kaldı` ("left") reads more naturally under "2 sa 10 dk".
- `notif_prayer_body`: "itibarıyla" ("as of") is office language in a prayer notification. Suggest `%1$s vakti başlıyor: saat %2$s`.
- Dua paragraph (`disclaimer_body`, last line): the English "please" was dropped, and "kabul ederseniz" ("if you accept") reads a little like a bargain. Suggest `Bir de ricam olacak, mümkünse: lütfen beni, ailemi ve anne babamı dualarınızdan eksik etmeyin.` "Dualarınızdan eksik etmeyin" itself is the right, humble idiom. Keep it.
- "Kullanım sayımları" (`consent_body`, `consent_yes`, `settings_usage_counts`, `about_privacy_desc`, `settings_usage_counts_desc`): "sayım" suggests a census or a stock-take. `kullanım sayıları` ("usage numbers") is more natural and still says no more than "counts". Do not move to "istatistik".

## 5. My verdict on the translator's doubts

1. **İmsak or Sabah for Fajr: keep İmsak. About 75% sure.** Diyanet's timetable, and as far as I
   know every mainstream Turkish prayer-times app, labels the dawn column "İmsak". "İmsak vakti" is
   also the normal Turkish phrase for the moment Fajr comes in and the fast begins. Where Turkish
   calendars print "Sabah", it usually means a *different, later* time (a recommended time to pray
   Sabah, close to sunrise). So putting "Sabah" on the dawn time would put a familiar word on the
   wrong number, which is worse than the small oddity of "SIRADAKİ NAMAZ: İmsak" (the prayer itself
   is *sabah namazı*). A Turkish reader should confirm, but I would not change it on doubt alone.
   **On the translator's temkin explanation, I disagree with the detail.** The app's Diyanet
   preset applies no offsets at all (`PrayerEngine.kt` line 398, plain 18°/17°). My recollection is
   that Diyanet's published safety margins (temkin) fall on Güneş, Öğle, İkindi and Akşam, by about
   4 to 7 minutes, and not on İmsak. I could not check this, because Aladhan is blocked from this
   container. If I am right, Turkish complaints will be about those four times, not İmsak. That is a
   calculation question for a separate check against Aladhan's Diyanet method. The translation is
   not the place to fix it.
2. **"Mezhep" for Sunni/Shia and "Fıkıh mezhebi" for Hanafi etc.: agree.** Departing from the
   glossary here was right, because the two settings need different names. "Amelî mezhep" is more
   scholarly and less familiar. One caution: to most Turkish Sunnis, "Hangi mezhebe bağlısınız?"
   (`sect_title`) first suggests "Hanefi". The Sünni/Şii options underneath make it clear, so this
   is a native-speaker check, not a fault.
3. **Watch lengths: agree, and it matters more than the note suggests.** See S3.
4. **"Kullanım sayımları": I would change it.** See the nits.
5. **Bismillahirrahmanirrahim:** fine as one word. That is the normal Turkish spelling, and Turkish TTS reads it.
6. **"Sorumluluk reddi":** fine. It is the standard term, and it is only the About row label.
7. **"Düpedüz yanılıyor olabilirim":** fine. It keeps the humble, plain voice. "Pekâlâ" would also work.
8. **"Kıldırır" in `mosque_diff_congregation`:** fine. "Usually" is kept, and the mosque/imam shift harms nothing.
9. **Others (section 3 of the notes):** keeping "Kuwait", "Qatar" and "Jafari (Ithna Ashari)" in English follows the brief. The localised city examples and the conversion of 10 miles to 16 km are both correct choices.

## 6. What I could not judge (needs a native Turkish speaker or a device)

- Whether the dua line, as it stands or with the nit applied, sounds natural and humble to a Turkish Muslim. It is your personal request, so it deserves a native ear.
- İmsak against Sabah (point 1), and whether "Mezhep" alone reads as Sunni/Shia.
- Overall naturalness of the long texts (disclaimer, consent, `location_sheet_body`). I found no errors in grammar, suffixes or `siz` register, but I am not a native speaker.
- Whether Turkish TalkBack reads "sa", "dk" and "sn" sensibly in the countdowns.
- Watch and button fit (S3, `consent_yes`): this needs screenshots, not a reader.
