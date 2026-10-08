# Play Store listing drafts: Indonesian, Turkish and Urdu

> **DRAFTS, written 8 Oct 2026 by an assistant. Nothing here is published, and an assistant never
> publishes a store listing** (`docs/translation/BRIEF.md`, safeguard 6). The owner reviews, decides,
> pastes, and presses every Save and Submit himself. **Do not touch the Play Console while a release is
> In review**: edits made during a review are a known way to lose time on this project.

The English listing in [`LISTING.md`](LISTING.md) is the truth these were translated from. On 8 Oct
2026 the public listing read **Updated on 4 Oct 2026**, so release 1.3.0 (optional, opt in usage
counts) is live and every claim below is true of the app a user installs today. The older
[`LISTING-LOCALES.md`](LISTING-LOCALES.md) is stale on two facts (it says "no analytics" and that the
method is "chosen automatically") and nothing was copied from it.

## How these drafts were made, and the rules they follow

- **Terms come from the glossary in `docs/translation/BRIEF.md`** and from the in-app translations on
  branch `claude/consent-buttons-language-1-3-1` (`values-in/`, `values-tr/`, `values-ur/`), so a reader
  who installs sees the same words in the app that the listing used: Subuh / Zuhur / Asar / Magrib / Isya;
  İmsak / Öğle / İkindi / Akşam / Yatsı; فجر / ظہر / عصر / مغرب / عشاء; Kiblat / Kıble / قبلہ.
- **Method names stay in English** (they are organisation names): Muslim World League, ISNA, Umm al-Qura,
  Egyptian General Authority, University of Karachi, Kemenag & MUIS, Diyanet, Moonsighting Committee,
  Jafari (Ithna Ashari). The app itself keeps "Jafari (Ithna Ashari)" in Latin script in all three languages.
- **No claim is strengthened, softened or added.** The hedges in the disclaimer paragraph ("a helper, not a
  religious authority", "can get things wrong", "follow your mosque", "no warranty", "do not rely on it
  alone") are carried as hedges. Nothing is said about features the app does not have: no prayer tracker,
  no streaks, no Quran, no bundled adhan recitation, no Imsak time.
- **The usage counts are described as the English describes them:** optional, controlled in Settings,
  never containing coordinates, no crash reporting. Each draft adds the words "off until you turn it on",
  which is true (the Settings description in the app says the same) and is what the owner asked these
  listings to say. Nowhere does any draft say "no analytics" or "no tracking".
- **No price words in the short description.** The Console's promotability check flagged `Free` and
  `No ads` in English (`LISTING.md`). No short description below contains *gratis*, *ücretsiz*, *مفت*,
  *iklan*, *reklam* or *اشتہار*. Google does not publish the list, so after pasting, use the method
  in `LISTING.md`: save, reload, and search the page for "may not be promoted".
- **No Unicode direction marks or invisible characters** anywhere (checked by script). Urdu uses Urdu
  punctuation (`۔` `،` `؟`) and Latin digits.
- **The dua request appears once per description, as the last line, exactly as in the English.** It is a
  translation of the English listing's own last paragraph, not an addition.
- **Character counts are in brackets after every field** and were produced by a script
  (`len()` of the text, i.e. Unicode code points), not by hand. `LISTING.md` records why a hand count is
  decoration.

## One deliberate difference from the English, and one line held back

**The calculation method line.** The live English description still says *"The right method is chosen
automatically for your region, and you can override it"*. Rule 5.17 in `docs/HANDOVER.md` says the app
**never** chooses a method for the user: it asks once at setup (with "Automatic" preselected, which is
Muslim World League, or Jafari for Shia) and offers the chooser again in Settings and behind "Different
from your mosque?". So all three drafts say, in their own words: *you choose the method yourself, at setup
and any time afterwards in Settings; the app never picks one for you.* That is what the app does. **The
English line itself is not edited here** (this task may not touch `LISTING.md`); it is flagged for the
owner below.

**The "Match your mosque" bullet is not included.** `LISTING.md` holds a pending bullet (*move any prayer
by up to 30 minutes to the time on its board*) that must not go live until the build with the tappable
number ships. The three drafts match the live English, so they leave it out. Translations are ready at
the end of this file for the day the English bullet is applied.

---

## Indonesian (Play Console language: Indonesian, `id-ID`)

Android resources use the qualifier `in`, but the Play Console lists the language as **Indonesian (id-ID)**.
Pick it from the Console's own list; the code is not typed anywhere.

**Title** [30 / 30]

```
SajdaTime: Waktu Salat, Kiblat
```

Why *Waktu Salat* and not *Jadwal Salat*: "SajdaTime: Jadwal Salat, Kiblat" is 31 characters. *Jadwal*
is carried in the short description instead, so Play indexes both phrases.

**Short description** [80 / 80]

```
Jadwal salat (sholat) offline & kompas kiblat untuk Sunni dan Syiah. Tanpa akun.
```

This is exactly at the limit. If the Console rejects it for any reason, this 79-character version drops
the final full stop and nothing else:
`Jadwal salat (sholat) offline & kompas kiblat untuk Sunni dan Syiah. Tanpa akun`

The gloss *(sholat)* is the Indonesian counterpart of the English *(namaz)* gloss, and for the same reason
(`LISTING.md`, "Why *namaz* went in after all"): *sholat* is the spelling most Indonesians type into
search, *salat* is the KBBI standard the app uses. The full description's fourth line names all three
spellings, mirroring the English "namaz, salah or salat" line.

**Full description** [3977 / 4000]

```
SajdaTime memberi tahu kapan waktu salat tiba dan ke arah mana menghadap.
Hanya itu, dan semuanya tanpa iklan, tanpa akun, dan tanpa mengirim lokasi Anda ke mana pun.

Waktu salat dihitung di ponsel Anda sendiri, jadi aplikasi tetap bekerja di pesawat, di ruang bawah tanah, atau tanpa sinyal sama sekali.

Baik Anda menyebutnya salat, sholat, atau shalat, yang dimaksud adalah lima salat harian yang sama.

YANG ANDA DAPATKAN

• Lima salat harian ditambah terbit matahari, dihitung untuk tempat Anda berada
• Tanggal Hijriah di samping tanggal Masehi, dengan nama bulan Islam
• Hitung mundur langsung ke salat berikutnya
• Kompas kiblat yang dikoreksi ke utara sejati, bukan utara magnetis
• Notifikasi di setiap waktu salat, bahkan saat aplikasi ditutup
• Mode alarm opsional dengan nada atau azan apa pun yang sudah ada di ponsel, cukup keras untuk Subuh
• Lencana senyap opsional di bilah notifikasi, menampilkan salat berikutnya
• Jadwal PDF siap cetak untuk hari ini, tujuh hari ke depan, atau satu bulan
• Saat Ramadan menjadi jadwal sahur dan berbuka: Subuh mengakhiri sahur, Magrib waktunya berbuka
• Aplikasi Wear OS dan tile jam tangan yang bekerja mandiri, dengan atau tanpa ponsel
• Tema terang dan gelap, mengikuti ponsel atau dipilih sendiri, keduanya teruji keterbacaannya

DIBUAT UNTUK MAZHAB ANDA

Ketentuan Sunni dan Syiah sama-sama didukung semestinya, bukan sekadar pelengkap.

• Sunni: Hanafi, Syafi'i, Maliki, dan Hanbali, dengan aturan Asar yang tepat untuk masing-masing
• Syiah: Jafari (Ithna Ashari), termasuk aturan Magrib yang tepat
• Metode perhitungan: Kemenag & MUIS, Muslim World League, ISNA, Umm al-Qura, Egyptian General Authority, University of Karachi, Dubai, Kuwait, Qatar, Diyanet, Moonsighting Committee, dan lainnya
• Metode perhitungan Anda pilih sendiri, saat penyiapan dan kapan saja di Pengaturan; aplikasi tidak pernah memilih untuk Anda
• Aturan lintang tinggi untuk waktu Subuh dan Isya yang wajar di negara utara, tempat matahari tak benar-benar terbenam di musim panas
• Penyesuaian Ramadan Umm al-Qura diterapkan otomatis selama Ramadan

PRIVASI ANDA BUKAN HARGANYA

Kebanyakan aplikasi salat gratis dibiayai iklan, artinya Anda membayar dengan data Anda.
Aplikasi ini tidak dibayar dengan apa pun.

• Tanpa iklan, selamanya
• Tanpa akun, tanpa masuk, tanpa alamat email
• Statistik penggunaan opsional, diatur di Pengaturan, nonaktif sampai Anda mengaktifkannya. Tanpa laporan crash
• Hanya lokasi perkiraan, dibaca saat aplikasi terbuka, tidak pernah di latar belakang
• Koordinat Anda tidak pernah meninggalkan ponsel, dan tidak pernah masuk statistik penggunaan opsional
• Pencadangan cloud sengaja dimatikan agar tidak ada yang bisa disalin dari ponsel

Jika Anda lebih suka tidak membagikan lokasi sama sekali, ketik saja nama kota; aplikasi bekerja seperti biasa.

JUJUR TENTANG APA ADANYA

SajdaTime adalah alat bantu, bukan otoritas agama. Ponsel Anda menghitungnya dari posisi matahari dengan metode perhitungan yang telah diterbitkan; waktu itu tidak berasal dari masjid, ulama, atau otoritas mana pun. Aplikasi ini ditulis satu orang, dengan bantuan kecerdasan buatan, dan bisa saja keliru. Bila SajdaTime dan masjid Anda berbeda, ikutilah masjid Anda. Jika suatu waktu atau arah tampak keliru, atau Anda ragu, tanyakan kepada mereka atau orang lain yang berkompeten.

Waktu salat dihitung dengan adhan-java dari Batoul Apps, pustaka sumber terbuka yang banyak dipakai, dan dicek terhadap jadwal rujukan independen sebelum tiap rilis. Meski begitu, aplikasi ini diberikan gratis dan apa adanya, tanpa jaminan dan tanpa janji ketepatan, jadi bila ketepatan penting bagi Anda, mohon jangan mengandalkannya saja.

GRATIS, DAN TETAP GRATIS

Tidak ada versi berbayar, tidak ada langganan, tidak ada yang dikunci. SajdaTime diberikan cuma-cuma sebagai sedekah jariyah untuk umat.

Hanya satu hal yang diminta sebagai balasan: mohon ingat saya, keluarga saya, dan kedua orang tua saya dalam doa Anda. Jazakallah khairan.
```

**Search phrases an Indonesian Muslim actually types**

| Phrase | Why |
|---|---|
| `jadwal sholat` | The dominant query in Indonesia, in the informal *sholat* spelling; it is in the short description and the fourth line |
| `arah kiblat` | "Qibla direction", the way the compass is searched for; *kiblat* is in the title and short description |
| `waktu sholat` / `jadwal imsakiyah` | The second phrasing, and the Ramadan phrasing; *waktu salat* is in the title, *imsakiyah* is deliberately **not** used (see "For the owner") |

---

## Turkish (Play Console language: Turkish, `tr-TR`)

**Title** [29 / 30]

```
SajdaTime: Namaz Vakti, Kıble
```

"SajdaTime: Namaz Vakitleri, Kıble" is 33 characters, so the title uses the singular *Namaz Vakti*, which
is also a common search phrase. The plural *namaz vakitleri* opens the short description.

**Short description** [76 / 80]

```
Çevrimdışı namaz vakitleri ve Kıble pusulası, Sünni ve Şii için. Üyelik yok.
```

*Üyelik yok* ("no membership") is how Turkish apps say "no accounts"; *Üyelik gerekmez* was 81 characters.

**Full description** [3953 / 4000]

```
SajdaTime size ne zaman namaz kılacağınızı ve hangi yöne döneceğinizi söyler.
Yaptığı tek şey budur ve bunu reklamsız, hesapsız ve konumunuzu hiçbir yere göndermeden yapar.

Namaz vakitleri kendi telefonunuzda hesaplanır; bu yüzden uygulama uçakta, bodrumda ya da hiç sinyal yokken bile çalışmaya devam eder.

İster namaz ister salât deyin, bunlar aynı beş vakit namazdır.

NELER SUNAR

• Beş vakit namaz ve güneşin doğuşu, bulunduğunuz yer için hesaplanmış
• Miladi tarihin yanında Hicri tarih, Hicri ay adları dahil
• Bir sonraki namaza canlı geri sayım
• Manyetik kuzeye değil gerçek kuzeye göre düzeltilmiş bir Kıble pusulası
• Uygulama kapalıyken bile her namaz vaktinde bildirim
• Telefonunuzda zaten bulunan herhangi bir zil sesini ya da ezanı kullanan, İmsak için yeterince yüksek sesli, isteğe bağlı alarm modu
• Bildirim alanında bir sonraki namazı gösteren isteğe bağlı sessiz rozet
• Bugün, önümüzdeki yedi gün ya da tüm ay için yazdırılabilir PDF vakit çizelgesi
• Ramazan'da sahur ve iftar çizelgesi olarak da iş görür: İmsak sahuru bitirir, Akşam orucu açar
• Telefonunuz olsun ya da olmasın kendi başına çalışan Wear OS uygulaması ve saat kutucuğu
• Telefonunuzu izleyen ya da elle seçilen açık ve koyu tema, ikisi de okunabilirlik için denetlenmiş

MEZHEBİNİZE GÖRE

Sünni ve Şii uygulamalarının ikisi de üstünkörü değil, gerektiği gibi desteklenir.

• Sünni: Hanefi, Şafii, Maliki ve Hanbeli, her biri için doğru İkindi kuralıyla
• Şii: Jafari (Ithna Ashari), doğru Akşam kuralı dahil
• Hesaplama yöntemleri: Diyanet (Türkiye), Muslim World League, ISNA, Umm al-Qura, Egyptian General Authority, University of Karachi, Dubai, Kuwait, Qatar, Kemenag & MUIS (Endonezya ve Singapur), Moonsighting Committee ve daha fazlası
• Hesaplama yöntemini kurulumda ve sonrasında istediğiniz zaman Ayarlar'dan siz seçersiniz; uygulama sizin yerinize asla yöntem seçmez
• Yazın güneşin hiç tam batmadığı kuzey ülkelerinde makul İmsak ve Yatsı vakitleri veren bir yüksek enlem kuralı
• Umm al-Qura Ramazan düzeltmesi Ramazan boyunca otomatik uygulanır

GİZLİLİĞİNİZ BEDEL DEĞİLDİR

Ücretsiz namaz uygulamalarının çoğu reklamla finanse edilir; bu da bedelini verilerinizle ödediğiniz anlamına gelir.
Bu uygulamanın bedeli hiçbir şekilde ödenmez.

• Reklam yok, asla
• Hesap yok, oturum açma yok, e-posta adresi yok
• Ayarlar'dan sizin denetlediğiniz, siz açmadıkça kapalı kalan isteğe bağlı kullanım sayıları. Çökme raporu yok
• Yalnızca yaklaşık konum, yalnızca uygulama açıkken okunur, arka planda asla
• Koordinatlarınız cihazınızdan asla çıkmaz ve isteğe bağlı kullanım sayılarının asla parçası olmaz
• Telefonunuzdan hiçbir şey kopyalanamasın diye bulut yedekleme bilerek kapalıdır

Konumunuzu hiç paylaşmak istemiyorsanız bunun yerine bir şehir adı yazabilirsiniz; uygulama aynı şekilde çalışır.

NE OLDUĞU KONUSUNDA DÜRÜST

SajdaTime bir yardımcıdır, dinî bir otorite değildir. Telefonunuz bu vakitleri yayımlanmış hesaplama yöntemleriyle güneşin konumundan hesaplar; vakitler herhangi bir cami, âlim ya da makam tarafından verilmiş değildir. Uygulama tek bir kişi tarafından, yapay zekâ yardımıyla yazıldı ve hata yapabilir. SajdaTime ile caminiz arasında fark olduğunda caminize uyun. Bir vakit ya da yön size yanlış görünürse veya emin değilseniz, lütfen onlara ya da size yol gösterebilecek ehil başka birine sorun.

Namaz vakitleri, Batoul Apps'in yaygın kullanılan açık kaynaklı adhan-java kütüphanesiyle hesaplanır ve her sürümden önce bağımsız referans çizelgelerle karşılaştırılır. Yine de uygulama ücretsiz ve olduğu gibi sunulur; hiçbir garantisi ve doğruluk vaadi yoktur. Bu yüzden kesinliğin sizin için önemli olduğu durumlarda lütfen yalnızca buna güvenmeyin.

ÜCRETSİZ, VE ÖYLE KALACAK

Ücretli bir sürüm, abonelik ya da kilitli hiçbir şey yok. SajdaTime ümmet için sadaka-i cariye olarak karşılıksız sunulur.

Karşılığında istenen tek bir şey var: lütfen beni, ailemi ve anne babamı dualarınızdan eksik etmeyin. Allah razı olsun.
```

**Search phrases a Turkish Muslim actually types**

| Phrase | Why |
|---|---|
| `namaz vakitleri` | The standard query, and the phrase Diyanet itself uses; it opens the short description and appears throughout |
| `kıble pusulası` / `kıble bulucu` | "Qibla compass" / "Qibla finder"; *Kıble pusulası* is in the short description and the feature list |
| `imsakiye` | The Ramadan timetable everyone looks for in Ramazan; the word is **not** used, see "For the owner" |

*Ezan vakti* was considered and rejected for the reason `LISTING.md` rejected *Adhan*: the app plays a
sound the user already has, it does not ship an ezan, so the keyword would over-promise.

---

## Urdu (Play Console language: Urdu, `ur`)

**Title** [30 / 30]

```
SajdaTime: نماز کے اوقات، قبلہ
```

Mixed script: the app name stays in Latin letters (`BRIEF.md` rule 9), the rest is Urdu, exactly the phrase
the app's own PDF tagline uses (نماز کے اوقات). Check how the Console preview renders the colon between the
two scripts before saving; no direction mark has been added, because the brief forbids them.

**Short description** [74 / 80]

```
آف لائن نماز کے اوقات اور قبلہ نما، سنی اور شیعہ کے لیے۔ کوئی اکاؤنٹ نہیں۔
```

*قبلہ نما* is the Urdu word for a Qibla compass. The in-app text says *قطب نما* for the compass itself; both
are standard.

**Full description** [3801 / 4000]

```
SajdaTime آپ کو بتاتی ہے کہ نماز کب پڑھنی ہے اور رخ کس طرف کرنا ہے۔
یہ بس اتنا ہی کرتی ہے، اور یہ کام اشتہار کے بغیر، اکاؤنٹ کے بغیر، اور آپ کا مقام کہیں بھیجے بغیر کرتی ہے۔

نماز کے اوقات آپ کے اپنے فون پر معلوم کیے جاتے ہیں، اس لیے ایپ جہاز میں، تہہ خانے میں، یا بالکل سگنل نہ ہونے پر بھی کام کرتی رہتی ہے۔

آپ انہیں نماز کہیں یا صلاۃ، یہ وہی پانچ وقت کی نمازیں ہیں۔

آپ کو کیا ملتا ہے

• پانچ وقت کی نمازیں اور طلوعِ آفتاب، آپ جہاں بھی ہوں وہاں کے لیے معلوم کیے ہوئے
• عام تاریخ کے ساتھ ہجری تاریخ، اسلامی مہینوں کے ناموں سمیت
• اگلی نماز تک چلتی ہوئی الٹی گنتی
• قبلہ نما جو مقناطیسی شمال نہیں بلکہ حقیقی شمال کے مطابق درست کیا گیا ہے
• ہر نماز کے وقت پر اطلاع، ایپ بند ہونے پر بھی
• اختیاری الارم موڈ، آپ کے فون میں پہلے سے موجود کسی بھی ٹون یا اذان کے ساتھ، فجر کے لیے کافی اونچا
• نوٹیفکیشن شیڈ میں اگلی نماز دکھانے والا اختیاری خاموش بیج
• آج، اگلے سات دن، یا پورے مہینے کا پرنٹ ہونے والا PDF ٹائم ٹیبل
• رمضان میں یہ سحری اور افطار کا ٹائم ٹیبل بھی بن جاتی ہے: فجر پر سحری ختم، مغرب پر افطار
• Wear OS ایپ اور گھڑی کی ٹائل جو آپ کے فون کے ساتھ یا اس کے بغیر خود کام کرتی ہیں
• ہلکی اور گہری تھیم، فون کے مطابق یا خود منتخب کی ہوئی، دونوں پڑھنے میں آسانی کے لیے جانچی ہوئی

آپ کے مکتبِ فکر کے مطابق

سنی اور شیعہ دونوں کے طریقے باقاعدہ طور پر شامل ہیں، محض خانہ پری کے لیے نہیں۔

• سنی: حنفی، شافعی، مالکی اور حنبلی، ہر ایک کے لیے عصر کے درست اصول کے ساتھ
• شیعہ: Jafari (Ithna Ashari)، مغرب کے درست اصول سمیت
• حساب کے طریقے: University of Karachi, Muslim World League, ISNA, Umm al-Qura, Egyptian General Authority, Dubai, Kuwait, Qatar, Kemenag & MUIS, Diyanet, Moonsighting Committee اور دیگر
• حساب کا طریقہ آپ خود چنتے ہیں، سیٹ اپ کے وقت اور اس کے بعد کسی بھی وقت ترتیبات میں؛ ایپ کبھی آپ کے لیے طریقہ نہیں چنتی
• بلند عرضِ بلد کا اصول جو ان شمالی ملکوں میں فجر اور عشاء کے معقول اوقات دیتا ہے جہاں گرمیوں میں سورج کبھی پوری طرح غروب نہیں ہوتا
• Umm al-Qura کی رمضان کی ترمیم رمضان کے دوران خود بخود لاگو ہوتی ہے

آپ کی رازداری اس کی قیمت نہیں

زیادہ تر مفت نماز ایپس اشتہاروں سے چلتی ہیں، یعنی آپ اپنے ڈیٹا سے قیمت ادا کرتے ہیں۔
اس ایپ کی قیمت کسی بھی طرح ادا نہیں کی جاتی۔

• کوئی اشتہار نہیں، کبھی نہیں
• کوئی اکاؤنٹ نہیں، کوئی سائن اِن نہیں، کوئی ای میل پتہ نہیں
• استعمال کے اختیاری اعداد و شمار، جو ترتیبات میں آپ کے اختیار میں ہیں اور جب تک آپ آن نہ کریں بند رہتے ہیں۔ کریش رپورٹنگ نہیں
• صرف تخمینی مقام، صرف ایپ کھلی ہونے پر پڑھا جاتا ہے، پس منظر میں کبھی نہیں
• آپ کے کوآرڈینیٹس کبھی آپ کے فون سے باہر نہیں جاتے، اور کبھی اختیاری اعداد و شمار کا حصہ نہیں بنتے
• کلاؤڈ بیک اپ جان بوجھ کر بند رکھا گیا ہے تاکہ آپ کے فون سے کچھ بھی نقل نہ ہو سکے

اگر آپ اپنا مقام بالکل بھی نہ بتانا چاہیں تو اس کے بجائے شہر کا نام لکھ سکتے ہیں، اور ایپ ویسے ہی کام کرتی ہے۔

یہ کیا ہے، اس بارے میں صاف بات

SajdaTime ایک مددگار ہے، کوئی دینی اتھارٹی نہیں۔ آپ کا فون یہ اوقات شائع شدہ حسابی طریقوں کے ذریعے سورج کی پوزیشن سے معلوم کرتا ہے؛ یہ کسی مسجد، عالم یا ادارے کے دیے ہوئے نہیں ہیں۔ یہ ایپ ایک شخص نے مصنوعی ذہانت کی مدد سے بنائی ہے، اور اس میں غلطی ہو سکتی ہے۔ جہاں SajdaTime اور آپ کی مسجد میں فرق ہو، اپنی مسجد کی پیروی کریں۔ اگر کوئی وقت یا سمت کبھی غلط لگے، یا آپ کو یقین نہ ہو، تو براہِ کرم ان سے یا کسی اور اہل شخص سے پوچھ لیں۔

نماز کے اوقات Batoul Apps کی adhan-java لائبریری سے معلوم کیے جاتے ہیں، جو ایک وسیع پیمانے پر استعمال ہونے والی اوپن سورس لائبریری ہے، اور ہر ریلیز سے پہلے آزاد حوالہ جاتی ٹائم ٹیبلز سے ان کا موازنہ کیا جاتا ہے۔ اس کے باوجود یہ ایپ مفت اور جیسی ہے ویسی دی گئی ہے، نہ کوئی ضمانت ہے نہ درستی کا کوئی وعدہ، اس لیے جہاں آپ کے لیے عین درست ہونا اہم ہو، وہاں براہِ کرم صرف اسی پر بھروسا نہ کریں۔

مفت ہے، اور مفت رہے گی

نہ کوئی ادائیگی والا درجہ، نہ کوئی سبسکرپشن، نہ کچھ بند۔ SajdaTime امت کے لیے صدقۂ جاریہ کے طور پر مفت دی گئی ہے۔

بدلے میں بس ایک چیز مانگی جاتی ہے: براہِ کرم مجھے، میرے اہلِ خانہ اور میرے والدین کو اپنی دعاؤں میں یاد رکھیں۔ جزاک اللہ خیر۔
```

**Search phrases a Pakistani Muslim actually types**

| Phrase | Why |
|---|---|
| `namaz timings` / `namaz time` (Latin script) | Most Pakistanis search Play in Latin letters; the English listing's *(namaz)* already catches this, and this Urdu listing adds the Urdu-script searchers |
| `نماز کے اوقات` / `اوقات نماز` | The Urdu-script query; the first form is in the title and short description |
| `qibla direction` / `قبلہ کی سمت` | The compass query in both scripts; *قبلہ* is in the title and short description |

Seasonal: `sehri iftar time` / `سحری افطار کے اوقات`, covered by the Ramadan bullet.

---

## Held back: the "Match your mosque" bullet, translated and waiting

Apply only when the English bullet in `LISTING.md` is applied, which is only after the build with the
tappable number is released. Insert each straight after the light-and-dark-themes bullet.

| Language | Bullet | Characters incl. the line break |
|---|---|---|
| Indonesian | `• Sesuaikan dengan masjid: geser waktu salat mana pun hingga 30 menit ke waktu di papan jadwalnya` | 98 |
| Turkish | `• Caminizle eşleştirin: herhangi bir namazı, panosundaki vakte kadar 30 dakikaya dek kaydırın` | 94 |
| Urdu | `• اپنی مسجد سے ملائیں: کسی بھی نماز کو 30 منٹ تک اس کے بورڈ کے وقت پر لے جائیں` | 79 |

**Watch the limit when that day comes.** With the bullet added the descriptions become
4075 (Indonesian), 4047 (Turkish) and 3880 (Urdu) characters. **Urdu fits; Indonesian and Turkish do not** and would need
about 75 and 47 characters trimmed elsewhere first, which is a job for a fresh draft against the English of that day,
not a silent cut now. The feature names match the in-app disclaimer translations (*Sesuaikan dengan masjid*,
*Caminizle eşleştir*, *اپنی مسجد سے ملائیں*).

---

## For the owner

Things I was not sure about, in the order I would want them decided. A flagged doubt is useful; a
confident guess is a trap.

1. **The English listing contradicts rule 5.17.** `LISTING.md` line *"The right method is chosen
   automatically for your region, and you can override it"* is live on Play and is not what the app does.
   The three drafts say the user chooses. When the English is next edited in the Console (after the current
   review, never during), that line should change too, or the four listings will disagree.
2. **Timing: the live app is English-only.** The translations exist on branch
   `claude/consent-buttons-language-1-3-1` and are listed but disabled until shipped. A localised listing
   published before the translated app ships sends an Indonesian reader into an English app. The old
   LOCALES file called that acceptable; it is your call, and it is a reason to publish these together with
   the release that carries the languages.
3. **The language is the owner's to check, not mine.** You read Urdu, so the Urdu draft can be checked
   by you; nobody on the project reads Indonesian or Turkish, and these were written by an assistant. The
   app's About screen says *translated with AI help, may contain mistakes*; the listings do not say so,
   because the English listing has no such line. Say if you want it added.
4. **Shia wording.** All three drafts write *Jafari (Ithna Ashari)* in Latin letters because the app does
   (`sect_shia_desc` in all three languages). A Turkish reader would normally see *Caferi*, an Urdu reader
   *جعفری*; the Urdu Sunni madhab names are in Urdu script. Consistency with the app won here. If you prefer
   the native spelling, change it in the app strings and the listing together.
5. **Imsak.** Indonesian and Turkish timetables print an *Imsak* time. In Turkey, Diyanet's İmsak **is**
   the Fajr time, so the Turkish label is right. In Indonesia *imsak* is a few minutes before Subuh and the
   app does not show it, so the Indonesian draft says *Subuh mengakhiri sahur* (Fajr ends sahur), exactly
   what the English says, and does not use the search word *imsakiyah*, which would promise an Imsak time.
   Same for Turkish *imsakiye*. If you want those words for Ramadan reach, that is a feature question first.
6. **"JazakAllah Kher" rendered idiomatically**: Indonesian *Jazakallah khairan*, Turkish *Allah razı
   olsun* (what Turks actually say; *Cezakallahu hayran* exists but is rare), Urdu *جزاک اللہ خیر*. Each is
   the same thanks, not a new religious claim. Tell me if you want the Arabic kept untranslated everywhere.
7. **"Off until you turn it on."** Each usage-counts bullet adds this; the English bullet does not say it.
   It is true and it is what the app's Settings row says. You may want the English bullet to say it too.
8. **Urdu bidi rendering.** The method list in the Urdu description is a run of Latin names separated by
   Latin commas inside Urdu text, which keeps it one left-to-right run. Check it in the Console preview on
   a phone before saving; if it looks scrambled, tell me what you see rather than adding direction marks.
9. **Indonesian short description is exactly 80.** A 79-character fallback is given above.
10. **Promotability check in other languages.** The English list of flagged words was found by
    elimination. No price or ad words are in any short description, but I could not run the Console's check
    from here. Save, reload, read.
11. **Console language codes** (`id-ID`, `tr-TR`, `ur`) are from memory of the Console's picker and were not
    verified from here; choose by the language name in the picker.
12. **Not tested:** nothing here has been pasted into the Console, previewed, or seen on a phone.

**Reminder: nothing may be edited in the Play Console while a review is in progress, and the owner presses
Save and Submit, never an assistant.**
