# Localised Play Store listings

The English listing is in [`LISTING.md`](LISTING.md). This file holds the eight additional
languages, so the app is findable by Muslims searching in their own language rather than in
English.

**No code change is involved.** A localised store listing is purely a Play Console entry:
Play Console → *Grow* → *Store presence* → *Store listings* → **Add translations**. Each
language gets its own title, short description and full description. Screenshots and the
feature graphic are reused automatically unless you upload language-specific ones.

> **The app is in English, Urdu, Turkish and Indonesian since 1.3.1.** The five other listings below
> (Malay, Bengali, Arabic, French, Hindi) are store text only, for discoverability: a user in Dhaka
> will find SajdaTime in Bengali and then open an app in English, or in one of its other three languages.
> That is a normal and accepted trade-off, since the numbers, the prayer names and the compass are
> understood regardless.

> **Corrected 9 Oct 2026 (drafts only, none of these is in Play).** The privacy bullets and short descriptions used to say "no analytics, no tracking, no crash reporting". That stopped being true when optional usage counts shipped in 1.3.0, so each language now follows the English listing: optional usage counts the user controls in Settings, no crash reporting, coordinates never part of the counts. The calculation method line used to say it was chosen for "your region"; it is chosen from the Sunni or Shia setting (Muslim World League or Jafari), so it now says that. The "app stays in English" note is also corrected: 1.3.1 ships Urdu, Turkish and Indonesian in the app.

> **Updated 16 Oct 2026 for release 1.4.0 (drafts only, none of these is in Play).** All eight now carry the
> two widgets, the Ramadan line, the languages line and the Umm al-Qura line, and five also carry the Hijri
> date bullet, from the English listing (`LISTING.md`, "Release A"), and the HONEST section now makes all four points the English
> one makes: not supplied by any mosque, scholar or authority, follow your mosque, written by one person
> with AI help, and no warranty (`docs/HANDOVER.md` §5.15 requires the Play listing to carry all four).
> **The Ramadan bullet belongs to the January version of the English listing.** If you publish any of these
> before then, delete that one bullet first, or publish them together with the January English text.
> Measured on 16 Oct 2026 (characters, newlines counted): Indonesian 3,951, Malay 3,947, French 3,976 (these three
> dropped the Hijri date bullet and some wording to fit), Urdu 3,383, Bengali 3,506, Arabic 3,021, Turkish 3,675,
> Hindi 3,571. Check each against Play's 4,000 limit again when pasting, because the text is wrapped here.
>
> **Translation status.** Written by Claude with no native speaker review, by the owner's decision of
> 9 Oct 2026. Correct any mistake a user reports. Do not tell users a translation awaits review.

---

## Why these eight

| Language | Locale | Reaches | Search term that matters |
|---|---|---|---|
| Indonesian | `id-ID` | Indonesia — the largest Muslim population on earth | *Jadwal Sholat* |
| Malay | `ms-MY` | Malaysia, Brunei, Singapore | *Waktu Solat* |
| Urdu | `ur` | Pakistan, and Urdu readers across India and the diaspora | *نماز* |
| Bengali | `bn-BD` | Bangladesh and West Bengal | *নামাজ* |
| Arabic | `ar` | The Arab world, and the liturgical language everywhere | *أوقات الصلاة* |
| Turkish | `tr-TR` | Turkey, Cyprus, the large German-Turkish diaspora | *Namaz Vakitleri* |
| French | `fr-FR` | North and West Africa, and the European diaspora | *heures de prière* |
| Hindi | `hi-IN` | Indian users who do not read Urdu script | *नमाज़ का समय* |

Between them, these eight plus English cover the first language of the large majority of
Muslims worldwide.

---

## Indonesian — `id-ID`

**Title** (24 / 30)

```
SajdaTime: Jadwal Sholat
```

**Short description** (66 / 80)

```
Jadwal sholat offline dan arah kiblat. Tanpa iklan dan tanpa akun.
```

**Full description**

```
SajdaTime memberi tahu kapan waktu sholat tiba dan ke arah mana Anda menghadap. Hanya itu
yang dilakukannya, tanpa iklan, tanpa akun, dan tanpa mengirim lokasi Anda ke mana pun.

Jadwal sholat dihitung langsung di ponsel Anda, sehingga aplikasi tetap berfungsi di dalam
pesawat, di ruang bawah tanah, atau tanpa sinyal sama sekali.


YANG ANDA DAPATKAN

• Lima waktu sholat wajib beserta terbit matahari, dihitung untuk lokasi Anda
• Hitung mundur langsung menuju waktu sholat berikutnya
• Kompas kiblat yang dikoreksi ke utara sejati, bukan utara magnetis
• Notifikasi pada setiap waktu sholat, bahkan saat aplikasi ditutup
• Mode alarm opsional dengan suara pilihan Anda, cukup keras untuk membangunkan Subuh
• Lencana tenang opsional di panel notifikasi yang menampilkan sholat berikutnya
• Jadwal PDF yang dapat dicetak untuk hari ini, tujuh hari ke depan, atau sebulan penuh
• Dua widget layar utama: sholat berikutnya dengan hitung mundur, dan waktu hari ini
• Saat Ramadan: akhir sahur dan buka puasa untuk lokasi Anda, hitung mundur, lama puasa hari ini, waktu besok, jadwal lengkap (dilihat atau disimpan sebagai PDF), dan pengingat sahur opsional sebelum Subuh
• Aplikasi Wear OS dan tile jam tangan yang bekerja mandiri
• Tema terang dan gelap, keduanya diperiksa keterbacaannya
• Dalam bahasa Inggris, Urdu, Turki, dan Indonesia, dipilih di Pengaturan


SESUAI MAZHAB ANDA

Ketentuan Sunni dan Syiah keduanya didukung dengan benar, bukan sekadar tambahan.

• Sunni: Hanafi, Syafi'i, Maliki dan Hanbali, dengan aturan Ashar yang tepat untuk masing-masing
• Syiah: Jafari / Itsna Asyariyah, termasuk aturan Maghrib yang benar
• Metode perhitungan: Muslim World League, ISNA, Umm al-Qura, Mesir, Karachi, Dubai,
  Kuwait, Qatar, Singapura, Turki, Moonsighting Committee dan lainnya
• Metode dipilih otomatis sesuai pilihan Anda, Sunni atau Syiah (Muslim World League untuk Sunni, Jafari untuk Syiah), dan dapat Anda ubah sendiri
• Aturan lintang tinggi untuk waktu Subuh dan Isya yang masuk akal di negara utara yang
  matahari tidak benar-benar terbenam pada musim panas
• Dengan metode Umm al-Qura, penyesuaian Isya Ramadannya diterapkan otomatis


PRIVASI ANDA BUKAN HARGANYA

Kebanyakan aplikasi sholat gratis dibiayai iklan, artinya dibiayai data Anda. Yang ini tidak.

• Tidak ada iklan, selamanya
• Tidak ada akun, tidak ada masuk, tidak ada alamat email
• Penghitungan penggunaan bersifat opsional dan Anda atur di Pengaturan. Tanpa pelaporan kerusakan
• Hanya lokasi perkiraan, dibaca saat aplikasi terbuka, tidak pernah di latar belakang
• Koordinat Anda tidak pernah meninggalkan perangkat, dan tidak pernah menjadi bagian dari penghitungan penggunaan opsional
• Pencadangan awan sengaja dimatikan agar tidak ada yang dapat disalin dari ponsel Anda

Jika Anda lebih memilih tidak membagikan lokasi sama sekali, Anda dapat mengetikkan nama
kota dan aplikasi bekerja persis sama.


JUJUR TENTANG APA ADANYA

SajdaTime adalah alat bantu, bukan otoritas keagamaan. Ponsel Anda menghitung waktu-waktu ini dari posisi matahari dengan metode perhitungan yang telah dipublikasikan. Waktu-waktu ini tidak diberikan oleh masjid, ulama, atau otoritas mana pun. Aplikasi ini ditulis oleh satu orang dengan bantuan kecerdasan buatan dan bisa saja keliru. Jika SajdaTime dan masjid Anda berbeda, ikuti masjid Anda. Jika suatu waktu atau arah terlihat tidak tepat, atau Anda ragu, mohon tanyakan kepada mereka atau kepada orang lain yang berkompeten.

Waktu sholat dihitung dengan adhan-java dari Batoul Apps, pustaka sumber terbuka, dan diperiksa terhadap jadwal rujukan independen sebelum setiap rilis. Meski begitu, aplikasi ini diberikan gratis apa adanya, tanpa jaminan dan tanpa janji ketepatan, jadi jika ketepatan penting bagi Anda, mohon jangan mengandalkannya semata.


GRATIS, DAN TETAP GRATIS

Tidak ada versi berbayar, tidak ada langganan, dan tidak ada yang dikunci. SajdaTime
diberikan cuma-cuma sebagai sedekah jariyah untuk umat.
```

---

## Malay — `ms-MY`

**Title** (30 / 30)

```
SajdaTime: Waktu Solat, Kiblat
```

**Short description** (64 / 80)

```
Waktu solat luar talian dan arah kiblat. Tiada iklan atau akaun.
```

**Full description**

```
SajdaTime memberitahu anda bila masuk waktu solat dan ke arah mana untuk mengadap. Itu
sahaja yang dilakukannya, tanpa iklan, tanpa akaun, dan tanpa menghantar lokasi anda ke
mana-mana.

Waktu solat dikira terus di telefon anda, jadi aplikasi ini terus berfungsi di dalam
kapal terbang, di tingkat bawah tanah, atau tanpa isyarat langsung.


APA YANG ANDA DAPAT

• Lima waktu solat fardhu serta syuruk, dikira untuk lokasi anda
• Kiraan detik secara langsung menuju waktu solat berikutnya
• Kompas kiblat yang diperbetulkan kepada utara benar, bukan utara magnet
• Pemberitahuan pada setiap waktu solat, walaupun aplikasi ditutup
• Mod penggera pilihan dengan bunyi pilihan anda, cukup kuat untuk mengejutkan Subuh
• Lencana senyap pilihan di panel pemberitahuan yang menunjukkan solat berikutnya
• Jadual PDF boleh cetak untuk hari ini, tujuh hari akan datang, atau sebulan penuh
• Dua widget skrin utama: solat berikutnya dengan kiraan detik, dan waktu hari ini
• Semasa Ramadan: akhir sahur dan waktu berbuka untuk lokasi anda, kiraan detik, tempoh puasa hari ini, waktu esok, jadual penuh (dilihat atau disimpan sebagai PDF), dan peringatan sahur pilihan sebelum Subuh
• Aplikasi Wear OS dan tile jam tangan yang berfungsi sendiri
• Tema cerah dan gelap, kedua-duanya diperiksa kebolehbacaannya
• Dalam bahasa Inggeris, Urdu, Turki dan Indonesia, dipilih dalam Tetapan


MENGIKUT MAZHAB ANDA

Kaedah Sunni dan Syiah kedua-duanya disokong dengan betul, bukan sekadar tambahan.

• Sunni: Hanafi, Syafi'i, Maliki dan Hanbali, dengan kaedah Asar yang tepat bagi setiap satu
• Syiah: Jafari / Ithna Asyari, termasuk kaedah Maghrib yang betul
• Kaedah pengiraan: Muslim World League, ISNA, Umm al-Qura, Mesir, Karachi, Dubai,
  Kuwait, Qatar, Singapura, Turki, Moonsighting Committee dan banyak lagi
• Kaedah dipilih secara automatik mengikut pilihan anda, Sunni atau Syiah (Muslim World League untuk Sunni, Jafari untuk Syiah), dan boleh anda ubah
• Peraturan latitud tinggi untuk waktu Subuh dan Isyak yang munasabah di negara utara
  yang matahari tidak terbenam sepenuhnya pada musim panas
• Dengan kaedah Umm al-Qura, pelarasan Isyak Ramadannya digunakan secara automatik


PRIVASI ANDA BUKAN HARGANYA

Kebanyakan aplikasi solat percuma dibiayai iklan, bermakna dibiayai data anda. Yang ini tidak.

• Tiada iklan, selamanya
• Tiada akaun, tiada log masuk, tiada alamat e-mel
• Kiraan penggunaan pilihan yang anda kawal dalam Tetapan. Tiada laporan ranap
• Lokasi anggaran sahaja, dibaca semasa aplikasi dibuka, tidak pernah di latar belakang
• Koordinat anda tidak pernah meninggalkan peranti, dan tidak pernah menjadi sebahagian daripada kiraan penggunaan pilihan
• Sandaran awan sengaja dimatikan supaya tiada apa boleh disalin dari telefon anda

Jika anda lebih suka tidak berkongsi lokasi langsung, anda boleh menaip nama bandar dan
aplikasi berfungsi dengan sama.


JUJUR TENTANG APA ADANYA

SajdaTime ialah pembantu, bukan pihak berkuasa agama. Telefon anda mengira waktu-waktu ini daripada kedudukan matahari menggunakan kaedah pengiraan yang diterbitkan. Waktu-waktu ini tidak dibekalkan oleh mana-mana masjid, ulama atau pihak berkuasa. Aplikasi ini ditulis oleh seorang dengan bantuan kecerdasan buatan dan boleh tersilap. Jika SajdaTime dan masjid anda berbeza, ikutlah masjid anda. Jika sesuatu waktu atau arah kelihatan tidak tepat, atau anda ragu-ragu, sila tanya mereka atau orang lain yang berkelayakan.

Waktu solat dikira menggunakan adhan-java oleh Batoul Apps, pustaka sumber terbuka yang digunakan secara meluas, dan disemak terhadap jadual rujukan bebas sebelum setiap keluaran. Aplikasi ini diberikan percuma sebagaimana adanya, tanpa waranti dan tanpa janji ketepatan, jadi jika ketepatan penting bagi anda, jangan bergantung padanya semata-mata.


PERCUMA, DAN KEKAL PERCUMA

Tiada versi berbayar, tiada langganan dan tiada apa-apa yang dikunci. SajdaTime diberikan
secara percuma sebagai sedekah berterusan untuk umat.
```

---

## Urdu — `ur`

**Title** (28 / 30)

```
SajdaTime: اوقات نماز و قبلہ
```

**Short description** (57 / 80)

```
آف لائن نماز کے اوقات اور قبلہ نما۔ نہ اشتہار، نہ اکاؤنٹ۔
```

**Full description**

```
SajdaTime آپ کو بتاتا ہے کہ نماز کا وقت کب ہے اور کس سمت رخ کرنا ہے۔ بس یہی اس کا کام ہے،
بغیر اشتہارات کے، بغیر اکاؤنٹ کے، اور آپ کا مقام کہیں بھیجے بغیر۔

نماز کے اوقات آپ کے اپنے فون پر ہی شمار کیے جاتے ہیں، اس لیے یہ ایپ جہاز میں، تہہ خانے
میں، یا بالکل سگنل کے بغیر بھی کام کرتی رہتی ہے۔


آپ کو کیا ملتا ہے

• پانچوں فرض نمازیں اور طلوعِ آفتاب، آپ کے مقام کے مطابق
• عام تاریخ کے ساتھ ہجری تاریخ
• اگلی نماز تک زندہ گنتی
• قبلہ نما جو مقناطیسی نہیں بلکہ حقیقی شمال کے مطابق درست کیا گیا ہے
• ہر نماز کے وقت اطلاع، چاہے ایپ بند ہو
• اختیاری الارم موڈ، آپ کی پسند کی آواز کے ساتھ، فجر کے لیے جگانے کو کافی بلند
• اطلاعات میں اختیاری خاموش نشان جو اگلی نماز دکھاتا ہے
• آج، اگلے سات دن، یا پورے مہینے کا قابلِ طباعت PDF نقشہ اوقات
• ہوم اسکرین کے دو ویجٹ: اگلی نماز الٹی گنتی کے ساتھ، اور آج کے تمام اوقات
• رمضان میں: آپ کے مقام کے لیے سحری کا اختتام اور افطار، الٹی گنتی، آج کے روزے کا دورانیہ، کل کے اوقات، دیکھنے یا PDF میں محفوظ کرنے کے لیے پورا نقشہ، اور فجر سے پہلے سحری کی اختیاری یاددہانی
• Wear OS ایپ اور واچ ٹائل جو فون کے ساتھ یا بغیر، خود مختار کام کرتی ہے
• روشن اور تاریک تھیم، دونوں پڑھنے کی صلاحیت کے لیے جانچے گئے
• انگریزی، اردو، ترکی اور انڈونیشی میں، ترتیبات سے منتخب کریں


آپ کے مسلک کے مطابق

سنی اور شیعہ دونوں کے اصول درست طور پر شامل ہیں، محض ضمنی طور پر نہیں۔

• سنی: حنفی، شافعی، مالکی اور حنبلی، ہر ایک کے لیے عصر کا درست اصول
• شیعہ: جعفری / اثنا عشری، بشمول مغرب کا درست اصول
• حسابی طریقے: مسلم ورلڈ لیگ، ISNA، ام القریٰ، مصری، کراچی، دبئی، کویت، قطر، سنگاپور،
  ترکی، مون سائٹنگ کمیٹی اور مزید
• طریقہ آپ کے انتخاب، سنی یا شیعہ، کے مطابق خود بخود منتخب ہوتا ہے (سنی کے لیے مسلم ورلڈ لیگ، شیعہ کے لیے جعفری)، اور آپ اسے بدل سکتے ہیں
• بلند عرض بلد کا اصول جو شمالی ممالک میں معقول فجر اور عشاء کے اوقات دیتا ہے جہاں گرمیوں
  میں سورج پوری طرح غروب نہیں ہوتا
• ام القریٰ طریقے کے ساتھ، رمضان میں اس کی عشاء کی ترمیم خود بخود لاگو ہوتی ہے


آپ کی رازداری اس کی قیمت نہیں

زیادہ تر مفت نماز ایپس اشتہارات سے چلتی ہیں، یعنی آپ کے ڈیٹا سے چلتی ہیں۔ یہ ایپ کسی چیز
سے نہیں چلتی۔

• کبھی کوئی اشتہار نہیں
• نہ اکاؤنٹ، نہ سائن اِن، نہ ای میل پتہ
• استعمال کی اختیاری گنتی، جس پر آپ کا اختیار سیٹنگز میں ہے۔ کوئی کریش رپورٹنگ نہیں
• صرف تخمینی مقام، وہ بھی جب ایپ کھلی ہو، پس منظر میں کبھی نہیں
• آپ کے جغرافیائی نقاط کبھی آپ کے آلے سے باہر نہیں جاتے، اور اختیاری گنتی میں بھی شامل نہیں ہوتے
• کلاؤڈ بیک اپ جان بوجھ کر بند ہے تاکہ آپ کے فون سے کچھ نقل نہ ہو سکے

اگر آپ اپنا مقام بالکل بھی نہیں بتانا چاہتے تو شہر کا نام لکھ دیں، ایپ بالکل ویسے ہی کام
کرے گی۔


جو ہے، سچ ہے

SajdaTime ایک مددگار ہے، دینی مرجع نہیں۔ آپ کا فون یہ اوقات سورج کی پوزیشن سے، شائع شدہ حسابی طریقوں کے ذریعے نکالتا ہے۔ یہ کسی مسجد، عالم یا اتھارٹی کی طرف سے فراہم نہیں کیے گئے۔ یہ ایپ ایک شخص نے مصنوعی ذہانت کی مدد سے بنائی ہے اور اس سے غلطی ہو سکتی ہے۔ جہاں SajdaTime اور آپ کی مسجد میں اختلاف ہو، وہاں اپنی مسجد کی پیروی کریں۔ اگر کوئی وقت یا سمت درست نہ لگے، یا آپ کو یقین نہ ہو، تو براہِ کرم ان سے یا کسی اور اہل شخص سے پوچھ لیں۔

نماز کے اوقات Batoul Apps کی اوپن سورس لائبریری adhan-java سے شمار ہوتے ہیں اور ہر اجرا سے پہلے آزاد حوالہ جاتی نقشوں سے جانچے جاتے ہیں۔ پھر بھی یہ ایپ مفت ہے اور جیسی ہے ویسی ہی دی جا رہی ہے، کسی ضمانت اور درستگی کے کسی وعدے کے بغیر، اس لیے جہاں بالکل درست ہونا آپ کے لیے اہم ہو، وہاں صرف اسی پر بھروسہ نہ کریں۔


مفت، اور مفت ہی رہے گی

نہ کوئی ادائیگی، نہ سبسکرپشن، نہ کچھ بند۔ SajdaTime امت کے لیے صدقہ جاریہ کے طور پر مفت
پیش کی گئی ہے۔
```

---

## Bengali — `bn-BD`

**Title** (30 / 30)

```
SajdaTime: নামাজের সময়, কিবলা
```

**Short description** (65 / 80)

```
অফলাইন নামাজের সময় ও কিবলা কম্পাস। বিজ্ঞাপন নেই, অ্যাকাউন্ট নেই।
```

**Full description**

```
SajdaTime আপনাকে জানায় কখন নামাজের সময় এবং কোন দিকে মুখ করতে হবে। এটুকুই এর কাজ — কোনও
বিজ্ঞাপন ছাড়া, কোনও অ্যাকাউন্ট ছাড়া, এবং আপনার অবস্থান কোথাও না পাঠিয়ে।

নামাজের সময় আপনার নিজের ফোনেই গণনা করা হয়, তাই অ্যাপটি বিমানে, বেসমেন্টে, বা একেবারে
নেটওয়ার্ক ছাড়াও কাজ করে।


আপনি যা পাবেন

• পাঁচ ওয়াক্ত নামাজ ও সূর্যোদয়, আপনার অবস্থান অনুযায়ী গণনা করা
• সাধারণ তারিখের পাশে হিজরি তারিখ
• পরবর্তী নামাজ পর্যন্ত সরাসরি ক্ষণগণনা
• কিবলা কম্পাস, চৌম্বক উত্তর নয় — প্রকৃত উত্তর অনুযায়ী সংশোধিত
• প্রতি ওয়াক্তে বিজ্ঞপ্তি, অ্যাপ বন্ধ থাকলেও
• ঐচ্ছিক অ্যালার্ম মোড, আপনার পছন্দের শব্দে, ফজরে জাগানোর মতো জোরালো
• ঐচ্ছিক নীরব ব্যাজ যা বিজ্ঞপ্তি প্যানেলে পরবর্তী নামাজ দেখায়
• আজ, আগামী সাত দিন বা পুরো মাসের মুদ্রণযোগ্য PDF সময়সূচি
• হোম স্ক্রিনের দুটি উইজেট: ক্ষণগণনাসহ পরবর্তী নামাজ, আর আজকের সব সময়
• রমজানে: আপনার অবস্থানের জন্য সাহরির শেষ সময় ও ইফতার, ক্ষণগণনা, আজকের রোজার দৈর্ঘ্য, আগামীকালের সময়, দেখার বা PDF হিসেবে সংরক্ষণের জন্য পূর্ণ সময়সূচি, এবং ফজরের আগে ঐচ্ছিক সাহরির অনুস্মারক
• Wear OS অ্যাপ ও ঘড়ির টাইল, ফোন থাকুক বা না থাকুক নিজেই চলে
• উজ্জ্বল ও অন্ধকার থিম, দুটোই পঠনযোগ্যতার জন্য যাচাই করা
• ইংরেজি, উর্দু, তুর্কি ও ইন্দোনেশীয় ভাষায়, সেটিংস থেকে বেছে নেওয়া যায়


আপনার মাযহাব অনুযায়ী

সুন্নি ও শিয়া দুই রীতিই সঠিকভাবে সমর্থিত, নামমাত্র নয়।

• সুন্নি: হানাফি, শাফিয়ি, মালিকি ও হাম্বলি, প্রত্যেকের জন্য সঠিক আসরের নিয়ম
• শিয়া: জাফরি / ইসনা আশারি, সঠিক মাগরিবের নিয়মসহ
• গণনা পদ্ধতি: মুসলিম ওয়ার্ল্ড লিগ, ISNA, উম্মুল কুরা, মিশরীয়, করাচি, দুবাই, কুয়েত,
  কাতার, সিঙ্গাপুর, তুরস্ক, মুনসাইটিং কমিটি এবং আরও
• পদ্ধতি আপনার পছন্দ, সুন্নি বা শিয়া, অনুযায়ী স্বয়ংক্রিয়ভাবে নির্বাচিত হয় (সুন্নির জন্য মুসলিম ওয়ার্ল্ড লিগ, শিয়ার জন্য জাফরি), এবং আপনি বদলাতে পারেন
• উচ্চ অক্ষাংশের নিয়ম, যা উত্তরের দেশগুলিতে যুক্তিসঙ্গত ফজর ও ইশার সময় দেয় যেখানে
  গ্রীষ্মে সূর্য পুরোপুরি অস্ত যায় না
• উম্মুল কুরা পদ্ধতিতে এর রমজানের ইশার সমন্বয় স্বয়ংক্রিয়ভাবে প্রযোজ্য হয়


আপনার গোপনীয়তাই এর মূল্য নয়

বেশিরভাগ বিনামূল্যের নামাজ অ্যাপ বিজ্ঞাপনে চলে, অর্থাৎ আপনার তথ্যে চলে। এটি কিছুতেই চলে
না।

• কখনও কোনও বিজ্ঞাপন নয়
• কোনও অ্যাকাউন্ট নেই, সাইন-ইন নেই, ইমেল ঠিকানা নেই
• ঐচ্ছিক ব্যবহারের গণনা, যা আপনি সেটিংসে নিয়ন্ত্রণ করেন। ক্র্যাশ রিপোর্ট নেই
• কেবল আনুমানিক অবস্থান, অ্যাপ খোলা থাকলে পড়া হয়, কখনও পটভূমিতে নয়
• আপনার স্থানাঙ্ক কখনও ডিভাইস ছাড়ে না, এবং ঐচ্ছিক ব্যবহারের গণনার অংশও হয় না
• ক্লাউড ব্যাকআপ ইচ্ছাকৃতভাবে বন্ধ, যাতে ফোন থেকে কিছু অনুলিপি করা না যায়

আপনি যদি অবস্থান একেবারেই ভাগ করতে না চান, শহরের নাম লিখে দিন — অ্যাপ ঠিক একইভাবে কাজ
করবে।


যা, তা নিয়ে সৎ

SajdaTime একটি সহায়ক, ধর্মীয় কর্তৃপক্ষ নয়। আপনার ফোন এই সময়গুলো সূর্যের অবস্থান থেকে, প্রকাশিত গণনা পদ্ধতি ব্যবহার করে হিসাব করে। এগুলো কোনও মসজিদ, আলেম বা কর্তৃপক্ষের দেওয়া নয়। অ্যাপটি একজন মানুষ কৃত্রিম বুদ্ধিমত্তার সহায়তায় লিখেছেন এবং এতে ভুল হতে পারে। SajdaTime ও আপনার মসজিদের মধ্যে অমিল হলে আপনার মসজিদের অনুসরণ করুন। কোনও সময় বা দিক ভুল মনে হলে, বা আপনি নিশ্চিত না হলে, অনুগ্রহ করে তাঁদের কাছে অথবা অন্য কোনও যোগ্য ব্যক্তির কাছে জেনে নিন।

নামাজের সময় Batoul Apps-এর adhan-java দিয়ে গণনা করা হয়, যা একটি বহুল ব্যবহৃত ওপেন সোর্স লাইব্রেরি, এবং প্রতিটি প্রকাশের আগে স্বাধীন রেফারেন্স সময়সূচির সঙ্গে মিলিয়ে দেখা হয়। তবুও অ্যাপটি বিনামূল্যে ও যেমন আছে তেমনভাবে দেওয়া, কোনও ওয়ারেন্টি বা নির্ভুলতার প্রতিশ্রুতি ছাড়া, তাই নির্ভুলতা যেখানে আপনার কাছে গুরুত্বপূর্ণ, সেখানে শুধু এর ওপর নির্ভর করবেন না।


বিনামূল্যে, এবং বিনামূল্যেই থাকবে

কোনও অর্থপ্রদান নেই, সাবস্ক্রিপশন নেই, কিছুই আটকানো নেই। SajdaTime উম্মাহর জন্য সদকায়ে
জারিয়া হিসেবে বিনামূল্যে দেওয়া হয়েছে।
```

---

## Arabic — `ar`

**Title** (29 / 30)

```
SajdaTime: أوقات الصلاة، قبلة
```

**Short description** (62 / 80)

```
أوقات الصلاة والقبلة دون إنترنت. بلا إعلانات، بلا حساب. مجاني.
```

**Full description**

```
يخبرك SajdaTime بموعد الصلاة وباتجاه القبلة. هذا كل ما يفعله، بلا إعلانات، وبلا حساب،
ودون إرسال موقعك إلى أي جهة.

تُحسب أوقات الصلاة على هاتفك نفسه، لذا يظل التطبيق يعمل في الطائرة، أو في قبو، أو دون أي
تغطية إطلاقًا.


ما الذي تحصل عليه

• الصلوات الخمس مع الشروق، محسوبة لمكانك أنت
• التاريخ الهجري إلى جانب التاريخ العادي
• عدّ تنازلي مباشر حتى الصلاة التالية
• بوصلة قبلة مصحّحة إلى الشمال الحقيقي لا الشمال المغناطيسي
• إشعارات عند كل صلاة، حتى والتطبيق مغلق
• وضع منبّه اختياري بصوت تختاره أنت، عالٍ بما يكفي لإيقاظك للفجر
• شارة هادئة اختيارية في لوحة الإشعارات تُظهر الصلاة التالية
• جدول PDF قابل للطباعة لليوم، أو للأيام السبعة القادمة، أو للشهر كاملًا
• ويدجتان على الشاشة الرئيسية: الصلاة التالية مع عدّ تنازلي، وجميع أوقات اليوم
• في رمضان: انتهاء السحور والإفطار لموقعك، وعدّ تنازلي، ومدة صيام اليوم، وأوقات الغد، وجدول كامل للعرض أو الحفظ بصيغة PDF، وتذكير اختياري بالسحور قبل الفجر
• تطبيق Wear OS وبطاقة للساعة يعملان بشكل مستقل، مع الهاتف أو بدونه
• مظهر فاتح وداكن، وقد رُوجع كلاهما من حيث وضوح القراءة
• بالإنجليزية والأردية والتركية والإندونيسية، وتختار منها من الإعدادات


وفق مذهبك

يُدعم المذهبان السنّي والشيعي دعمًا صحيحًا، لا على سبيل الإضافة.

• السنّة: الحنفي والشافعي والمالكي والحنبلي، مع قاعدة العصر الصحيحة لكل منها
• الشيعة: الجعفري / الإثنا عشري، بما في ذلك قاعدة المغرب الصحيحة
• طرق الحساب: رابطة العالم الإسلامي، ISNA، أم القرى، الهيئة المصرية، كراتشي، دبي،
  الكويت، قطر، سنغافورة، تركيا، لجنة رؤية الهلال وغيرها
• تُختار الطريقة تلقائيًا بحسب اختيارك، سنّي أو شيعي (رابطة العالم الإسلامي للسنّة، الجعفري للشيعة)، ويمكنك تغييرها
• قاعدة خطوط العرض العالية تعطي أوقاتًا معقولة للفجر والعشاء في البلدان الشمالية حيث لا
  تغيب الشمس تمامًا في الصيف
• مع طريقة أم القرى، يُطبَّق تعديل العشاء في رمضان تلقائيًا


خصوصيتك ليست هي الثمن

معظم تطبيقات الصلاة المجانية تموّلها الإعلانات، أي تموّلها بياناتك. هذا التطبيق لا يموّله
شيء أصلًا.

• لا إعلانات، أبدًا
• لا حسابات، ولا تسجيل دخول، ولا عنوان بريد
• عدّ اختياري للاستخدام تتحكم به من الإعدادات. لا تقارير أعطال
• موقع تقريبي فقط، يُقرأ والتطبيق مفتوح، ولا يُقرأ في الخلفية أبدًا
• إحداثياتك لا تغادر جهازك، ولا تدخل أبدًا ضمن العدّ الاختياري للاستخدام
• النسخ الاحتياطي السحابي معطّل عمدًا حتى لا يُنسخ شيء من هاتفك

وإن كنت تفضّل ألا تشارك موقعك إطلاقًا، فاكتب اسم مدينتك ويعمل التطبيق بالطريقة نفسها.


صريح فيما هو عليه

SajdaTime أداة مساعدة، وليس مرجعًا شرعيًا. يحسب هاتفك هذه الأوقات من موضع الشمس باستخدام طرق حساب منشورة، وهي ليست صادرة عن أي مسجد أو عالم أو جهة. كتب التطبيق شخص واحد بمساعدة الذكاء الاصطناعي، وقد يخطئ. إذا اختلف SajdaTime مع مسجدك فاتبع مسجدك. وإن بدا لك وقت أو اتجاه غير صحيح، أو لم تكن متأكدًا، فالرجاء سؤال مسجدك أو أي شخص مؤهل.

تُحسب أوقات الصلاة باستخدام مكتبة adhan-java من Batoul Apps، وهي مكتبة مفتوحة المصدر واسعة الاستخدام، وتُراجَع مقابل جداول مرجعية مستقلة قبل كل إصدار. ومع ذلك فالتطبيق مقدَّم مجانًا وكما هو، دون أي ضمان ودون وعد بالدقة، فحيث تهمّك الدقة، فلا تعتمد عليه وحده.


مجاني، وسيبقى مجانيًا

لا نسخة مدفوعة، ولا اشتراك، ولا شيء محجوب. يُقدَّم SajdaTime مجانًا صدقةً جاريةً للأمة.
```

---

## Turkish — `tr-TR`

**Title** (26 / 30)

```
SajdaTime: Namaz Vakitleri
```

**Short description** (68 / 80)

```
Çevrimdışı namaz vakitleri ve kıble pusulası. Reklam yok, hesap yok.
```

**Full description**

```
SajdaTime size namaz vaktinin ne zaman girdiğini ve hangi yöne döneceğinizi söyler. Yaptığı
tek şey budur. Reklamsız, hesapsız ve konumunuzu hiçbir yere göndermeden.

Namaz vakitleri doğrudan telefonunuzda hesaplanır, bu yüzden uygulama uçakta, bodrumda ya
da hiç sinyal yokken bile çalışmaya devam eder.


NELER SUNAR

• Beş vakit namaz ve güneşin doğuşu, bulunduğunuz yere göre hesaplanır
• Miladi tarihin yanında hicri tarih
• Bir sonraki namaza canlı geri sayım
• Manyetik kuzeye değil, gerçek kuzeye göre düzeltilmiş kıble pusulası
• Her namaz vaktinde bildirim, uygulama kapalıyken bile
• Seçtiğiniz sesle isteğe bağlı alarm modu, sabah namazına kaldıracak kadar yüksek
• Bildirim panelinde bir sonraki namazı gösteren isteğe bağlı sessiz rozet
• Bugün, önümüzdeki yedi gün veya tüm ay için yazdırılabilir PDF vakit cetveli
• İki ana ekran widget'ı: geri sayımlı sonraki namaz ve bugünün tüm vakitleri
• Ramazan'da: bulunduğunuz yer için imsak ve iftar, geri sayım, bugünkü orucun süresi, yarının vakitleri, görüntülenecek veya PDF olarak kaydedilecek tam imsakiye ve sabah namazından önce isteğe bağlı sahur hatırlatıcısı
• Telefonla ya da telefonsuz kendi başına çalışan Wear OS uygulaması ve saat kartı
• Açık ve koyu tema, ikisi de okunabilirlik açısından kontrol edilmiştir
• İngilizce, Urduca, Türkçe ve Endonezce, Ayarlar'dan seçilir


MEZHEBİNİZE GÖRE

Sünni ve Şii usulleri sonradan eklenmiş gibi değil, gerçekten doğru şekilde desteklenir.

• Sünni: Hanefi, Şafii, Maliki ve Hanbeli, her biri için doğru ikindi kuralıyla
• Şii: Caferi / İsna Aşeri, doğru akşam kuralı dahil
• Hesaplama yöntemleri: Müslüman Dünya Birliği, ISNA, Ümmü'l-Kura, Mısır, Karaçi, Dubai,
  Kuveyt, Katar, Singapur, Türkiye, Moonsighting Committee ve daha fazlası
• Yöntem, seçtiğiniz mezhebe göre otomatik belirlenir (Sünni için Müslüman Dünya Birliği, Şii için Caferi) ve dilerseniz değiştirebilirsiniz
• Yazın güneşin tam batmadığı kuzey ülkelerinde makul sabah ve yatsı vakitleri veren yüksek
  enlem kuralı
• Ümmü'l-Kura yöntemiyle Ramazan yatsı düzeltmesi otomatik uygulanır


BEDELİ MAHREMİYETİNİZ DEĞİL

Ücretsiz namaz uygulamalarının çoğu reklamla, yani sizin verinizle finanse edilir. Bu
uygulama hiçbir şeyle finanse edilmiyor.

• Asla reklam yok
• Hesap yok, giriş yok, e-posta adresi yok
• Ayarlardan kontrol ettiğiniz isteğe bağlı kullanım sayıları. Çökme raporu yok
• Yalnızca yaklaşık konum, uygulama açıkken okunur, arka planda asla
• Koordinatlarınız cihazınızdan hiç çıkmaz ve isteğe bağlı kullanım sayılarına da girmez
• Bulut yedeklemesi bilerek kapatılmıştır, telefonunuzdan hiçbir şey kopyalanamaz

Konumunuzu hiç paylaşmak istemiyorsanız şehir adını yazmanız yeterli. Uygulama tamamen aynı
şekilde çalışır.


NE OLDUĞU KONUSUNDA DÜRÜST

SajdaTime bir yardımcıdır, dinî bir merci değildir. Telefonunuz bu vakitleri güneşin konumundan, yayımlanmış hesaplama yöntemleriyle hesaplar. Hiçbir cami, âlim ya da makam tarafından verilmiş değildir. Uygulama tek kişi tarafından yapay zekâ desteğiyle yazılmıştır ve hata yapabilir. SajdaTime ile caminiz arasında fark varsa caminize uyun. Bir vakit veya yön size yanlış görünürse ya da emin değilseniz lütfen onlara veya ehil birine danışın.

Namaz vakitleri Batoul Apps'in açık kaynaklı adhan-java kütüphanesiyle hesaplanır ve her sürümden önce bağımsız referans cetvelleriyle karşılaştırılır. Yine de uygulama ücretsiz ve olduğu gibi sunulur, hiçbir garanti ve doğruluk vaadi yoktur. Kesinliğin sizin için önemli olduğu yerde lütfen yalnızca buna güvenmeyin.


ÜCRETSİZ VE ÜCRETSİZ KALACAK

Ücretli sürüm, abonelik ya da kilitli özellik yoktur. SajdaTime, ümmet için sürekli bir
sadaka olarak karşılıksız sunulmaktadır.
```

---

## French — `fr-FR`

**Title** (27 / 30)

```
SajdaTime: Heures de prière
```

**Short description** (66 / 80)

```
Heures de prière hors ligne et boussole Qibla. Sans pub ni compte.
```

**Full description**

```
SajdaTime vous indique quand prier et dans quelle direction vous tourner. C'est tout ce
qu'il fait, sans publicité, sans compte, et sans envoyer votre position où que ce soit.

Les heures de prière sont calculées sur votre propre téléphone : l'application continue
donc de fonctionner en avion, dans un sous-sol, ou sans aucun réseau.


CE QUE VOUS OBTENEZ

• Les cinq prières quotidiennes ainsi que le lever du soleil, calculés pour votre position
• Un compte à rebours en direct jusqu'à la prochaine prière
• Une boussole Qibla corrigée vers le nord géographique, et non le nord magnétique
• Des notifications à chaque heure de prière, même application fermée
• Un mode réveil facultatif avec le son de votre choix, assez fort pour le Fajr
• Un badge discret facultatif dans les notifications indiquant la prochaine prière
• Un horaire PDF imprimable pour aujourd'hui, les sept prochains jours ou le mois entier
• Deux widgets d'accueil : prochaine prière avec compte à rebours, et heures du jour
• Pendant le Ramadan : fin du suhoor et iftar pour votre position, compte à rebours, durée du jeûne, horaire complet en PDF, rappel facultatif du suhoor avant le Fajr
• Une appli Wear OS et une tuile de montre autonomes
• Thèmes clair et sombre, vérifiés pour la lisibilité
• En anglais, ourdou, turc et indonésien, au choix dans les Réglages


SELON VOTRE ÉCOLE JURIDIQUE

Les conventions sunnites et chiites sont toutes deux correctement prises en charge.

• Sunnite : hanafite, chaféite, malikite et hanbalite, avec la règle du Asr propre à chacune
• Chiite : jafarite / duodécimain, y compris la règle correcte du Maghrib
• Méthodes de calcul : Ligue islamique mondiale, ISNA, Oumm al-Qura, Karachi,
  Moonsighting Committee et bien d'autres
• La méthode est choisie automatiquement selon votre choix, sunnite ou chiite (Ligue islamique mondiale pour les sunnites, Jafari pour les chiites), et reste modifiable
• Règle des hautes latitudes : heures de Fajr et d'Isha raisonnables dans les pays du Nord où le soleil ne se couche pas complètement en été
• Avec Oumm al-Qura, l'ajustement de l'Isha au Ramadan est automatique


VOTRE VIE PRIVÉE N'EST PAS LE PRIX À PAYER

La plupart des applications de prière gratuites vivent de la publicité, donc de vos données.
Pas celle-ci.

• Aucune publicité, jamais
• Aucun compte, aucune connexion, aucune adresse e-mail
• Compteurs d'usage facultatifs que vous contrôlez dans les Réglages. Aucun rapport d'erreur
• Position approximative uniquement, lue application ouverte, jamais en arrière-plan
• Vos coordonnées ne quittent jamais votre appareil et ne font jamais partie des compteurs
  d'usage facultatifs
• Sauvegarde cloud volontairement désactivée : rien ne peut être copié depuis votre téléphone

Si vous préférez ne pas partager votre position, saisissez un nom de ville :
l'application fonctionne pareil.


HONNÊTE SUR CE QU'IL EST

SajdaTime est une aide, pas une autorité religieuse. Votre téléphone calcule ces heures à partir de la position du soleil, selon des méthodes de calcul publiées. Elles ne sont fournies par aucune mosquée, aucun savant ni aucune autorité. L'application a été écrite par une seule personne, avec l'aide de l'intelligence artificielle, et elle peut se tromper. Quand SajdaTime et votre mosquée ne sont pas d'accord, suivez votre mosquée. Si une heure ou une direction vous semble inexacte, ou si vous avez un doute, demandez à votre mosquée ou à une personne qualifiée.

Les heures de prière sont calculées avec adhan-java de Batoul Apps, une bibliothèque open source largement utilisée, et comparées à des horaires de référence indépendants avant chaque version. L'application est offerte gratuitement en l'état, sans garantie ni promesse d'exactitude. Là où l'exactitude compte pour vous, ne vous y fiez pas seul.


GRATUIT, ET IL LE RESTERA

Il n'y a pas de version payante, pas d'abonnement et rien de verrouillé. SajdaTime est
offert gratuitement comme une aumône continue pour la Oumma.
```

---

## Hindi — `hi-IN`

**Title** (23 / 30)

```
SajdaTime: नमाज़ का समय
```

**Short description** (65 / 80)

```
ऑफ़लाइन नमाज़ का समय और क़िबला कम्पास। कोई विज्ञापन या खाता नहीं।
```

**Full description**

```
SajdaTime आपको बताता है कि नमाज़ का वक़्त कब है और किस दिशा में मुँह करना है। बस इतना ही
इसका काम है — बिना विज्ञापन, बिना खाते, और आपकी लोकेशन कहीं भेजे बिना।

नमाज़ के समय आपके अपने फ़ोन पर ही निकाले जाते हैं, इसलिए यह ऐप हवाई जहाज़ में, तहख़ाने में,
या बिल्कुल सिग्नल के बिना भी काम करता रहता है।


आपको क्या मिलता है

• पाँचों वक़्त की नमाज़ें और सूर्योदय, आपकी जगह के हिसाब से
• सामान्य तारीख़ के साथ हिजरी तारीख़
• अगली नमाज़ तक चलती हुई उलटी गिनती
• क़िबला कम्पास, जो चुम्बकीय नहीं बल्कि वास्तविक उत्तर के अनुसार सही किया गया है
• हर नमाज़ के समय सूचना, ऐप बंद होने पर भी
• वैकल्पिक अलार्म मोड, आपकी पसंद की आवाज़ के साथ, फ़ज्र के लिए जगाने भर तेज़
• सूचना पटल पर वैकल्पिक शांत बैज जो अगली नमाज़ दिखाता है
• आज, अगले सात दिन या पूरे महीने की छपने योग्य PDF समय-सारणी
• होम स्क्रीन के दो विजेट: उलटी गिनती के साथ अगली नमाज़, और आज के सभी समय
• रमज़ान में: आपकी जगह के लिए सहरी का अंत और इफ़्तार, उलटी गिनती, आज के रोज़े की अवधि, कल के समय, देखने या PDF में सहेजने के लिए पूरी समय-सारणी, और फ़ज्र से पहले सहरी की वैकल्पिक याद दिलाने वाली सूचना
• Wear OS ऐप और वॉच टाइल, जो फ़ोन के साथ या बिना, अपने आप काम करती है
• उजाला और अँधेरा थीम, दोनों पठनीयता के लिए जाँचे गए
• अंग्रेज़ी, उर्दू, तुर्की और इंडोनेशियाई में, सेटिंग्स से चुनें


आपके मसलक के अनुसार

सुन्नी और शिया दोनों की रीतियाँ ठीक तरह से समर्थित हैं, ऊपरी तौर पर नहीं।

• सुन्नी: हनफ़ी, शाफ़ई, मालिकी और हंबली, हर एक के लिए सही अस्र नियम
• शिया: जाफ़री / इसना अशरी, सही मग़रिब नियम सहित
• गणना विधियाँ: मुस्लिम वर्ल्ड लीग, ISNA, उम्म अल-क़ुरा, मिस्री, कराची, दुबई, कुवैत,
  क़तर, सिंगापुर, तुर्की, मूनसाइटिंग कमेटी और अन्य
• विधि आपकी पसंद, सुन्नी या शिया, के अनुसार अपने आप चुनी जाती है (सुन्नी के लिए मुस्लिम वर्ल्ड लीग, शिया के लिए जाफ़री), और आप उसे बदल सकते हैं
• उच्च अक्षांश नियम, जो उत्तरी देशों में उचित फ़ज्र और इशा समय देता है जहाँ गर्मियों में
  सूरज पूरी तरह नहीं डूबता
• उम्म अल-क़ुरा विधि के साथ, रमज़ान में उसका इशा समायोजन अपने आप लागू होता है


आपकी निजता इसकी क़ीमत नहीं है

ज़्यादातर मुफ़्त नमाज़ ऐप विज्ञापन से चलते हैं, यानी आपके डेटा से चलते हैं। यह किसी चीज़ से
नहीं चलता।

• कभी कोई विज्ञापन नहीं
• कोई खाता नहीं, कोई साइन-इन नहीं, कोई ईमेल पता नहीं
• वैकल्पिक उपयोग गणना, जिसे आप सेटिंग्स में नियंत्रित करते हैं। कोई क्रैश रिपोर्ट नहीं
• केवल अनुमानित लोकेशन, वह भी ऐप खुले होने पर, पृष्ठभूमि में कभी नहीं
• आपके निर्देशांक कभी आपके डिवाइस से बाहर नहीं जाते, और वैकल्पिक उपयोग गणना में भी शामिल नहीं होते
• क्लाउड बैकअप जानबूझकर बंद है, ताकि आपके फ़ोन से कुछ भी कॉपी न हो सके

अगर आप लोकेशन बिल्कुल साझा नहीं करना चाहते, तो शहर का नाम लिख दीजिए — ऐप बिल्कुल वैसे ही
काम करेगा।


जो है, वही कहता है

SajdaTime एक सहायक है, कोई धार्मिक प्राधिकरण नहीं। आपका फ़ोन ये समय सूरज की स्थिति से, प्रकाशित गणना विधियों के आधार पर निकालता है। ये किसी मस्जिद, आलिम या किसी प्राधिकरण की ओर से नहीं दिए गए हैं। यह ऐप एक व्यक्ति ने कृत्रिम बुद्धिमत्ता की मदद से बनाया है और इससे ग़लती हो सकती है। जहाँ SajdaTime और आपकी मस्जिद में अंतर हो, वहाँ अपनी मस्जिद का अनुसरण करें। अगर कोई समय या दिशा ग़लत लगे, या आप निश्चित न हों, तो कृपया उनसे या किसी और योग्य व्यक्ति से पूछ लें।

नमाज़ के समय Batoul Apps की adhan-java लाइब्रेरी से निकाले जाते हैं, जो व्यापक रूप से इस्तेमाल होने वाली ओपन सोर्स लाइब्रेरी है, और हर रिलीज़ से पहले स्वतंत्र संदर्भ सारणियों से मिलाए जाते हैं। फिर भी यह ऐप मुफ़्त और जैसा है वैसा ही दिया जा रहा है, बिना किसी वारंटी और सटीकता के किसी वादे के, इसलिए जहाँ सटीकता आपके लिए ज़रूरी हो, वहाँ केवल इसी पर भरोसा न करें।


मुफ़्त, और मुफ़्त ही रहेगा

कोई सशुल्क संस्करण नहीं, कोई सदस्यता नहीं, कुछ भी बंद नहीं। SajdaTime उम्मत के लिए सदक़ा-ए-
जारिया के तौर पर मुफ़्त दिया गया है।
```

---

## Adding these in Play Console

1. **Grow → Store presence → Store listings**
2. **Manage translations → Add your own translation language**
3. Pick the locale, paste title / short description / full description from above
4. Screenshots and the feature graphic carry over from the default listing automatically —
   there is no need to re-upload them per language
5. Save. Each translation is reviewed with the app, so add them *before* you submit rather
   than afterwards, to avoid a second review cycle

Do **not** use Play's paid machine-translation offer. These are already written.
