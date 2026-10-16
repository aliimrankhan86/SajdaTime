import com.batoulapps.adhan.*;
import com.batoulapps.adhan.data.DateComponents;
import java.time.*;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Mirrors com.sajdatime.core.PrayerEngine.compute (no user minutes) with adhan-java 1.2.1, to
 * regenerate the golden values in RAMADAN-LOCATION-SPEC.md section 9.2. It is a mirror, not the
 * engine: where the two disagree, the engine's RamadanGoldenTest decides and this file is wrong.
 *
 * Run (from this folder):
 *   JAR=$(find ~/.gradle/caches -name 'adhan-1.2.1.jar' | head -1)   # or download from Maven Central
 *   javac -cp "$JAR" Mirror.java && java -cp "$JAR:." Mirror
 */
public class Mirror {
  enum M { MWL, UAQ, KARACHI, TURKEY, SINGAPORE, MOON, JAFARI, ISNA }
  static CalculationParameters params(M m, boolean hanafi) {
    CalculationParameters p;
    switch (m) {
      case MWL: p = CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters(); break;
      case UAQ: p = CalculationMethod.UMM_AL_QURA.getParameters(); break;
      case KARACHI: p = CalculationMethod.KARACHI.getParameters(); break;
      case SINGAPORE: p = CalculationMethod.SINGAPORE.getParameters(); break;
      case MOON: p = CalculationMethod.MOON_SIGHTING_COMMITTEE.getParameters(); break;
      case ISNA: p = CalculationMethod.NORTH_AMERICA.getParameters(); break;
      case TURKEY: p = new CalculationParameters(18.0, 17.0, CalculationMethod.OTHER); break;
      case JAFARI: p = new CalculationParameters(16.0, 14.0, CalculationMethod.OTHER); break;
      default: throw new IllegalStateException();
    }
    p.madhab = (m == M.JAFARI) ? Madhab.SHAFI : (hanafi ? Madhab.HANAFI : Madhab.SHAFI);
    p.highLatitudeRule = HighLatitudeRule.TWILIGHT_ANGLE;
    return p;
  }
  static boolean usable(PrayerTimes t) {
    Date[] all = { t.fajr, t.sunrise, t.dhuhr, t.asr, t.maghrib, t.isha };
    for (Date d : all) if (d == null) return false;
    long[] ms = new long[6]; for (int i=0;i<6;i++) ms[i]=all[i].getTime();
    for (int i=1;i<6;i++) if (ms[i] < ms[i-1]) return false;
    for (long v : ms) if (Math.abs(v - t.dhuhr.getTime()) > 24L*3600*1000) return false;
    long mid = (t.sunrise.getTime() + t.maghrib.getTime())/2;
    return Math.abs(t.dhuhr.getTime() - mid) <= 30L*60*1000;
  }
  static boolean isRamadan(LocalDate d, int off) {
    try { return HijrahDate.from(d.plusDays(off)).get(ChronoField.MONTH_OF_YEAR) == 9; } catch (Exception e) { return false; }
  }
  static Instant inst(Date d) { return Instant.ofEpochMilli(d.getTime()).truncatedTo(ChronoUnit.MINUTES); }
  /** returns [fajr,sunrise,dhuhr,asr,maghrib,isha, approxFrom(or NaN)] */
  static Object[] compute(double lat, double lon, LocalDate date, M m, boolean hanafi, int off) {
    CalculationParameters p = params(m, hanafi);
    DateComponents dc = new DateComponents(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    Coordinates here = new Coordinates(lat, lon);
    PrayerTimes local = new PrayerTimes(here, dc, p);
    boolean ok = usable(local);
    double ref = (m == M.MOON) ? 60.0 : 45.0;
    Coordinates c = ok ? here : new Coordinates(Math.copySign(ref, lat), lon);
    PrayerTimes base = ok ? local : new PrayerTimes(c, dc, p);
    Date maghrib = base.maghrib;
    if (m == M.JAFARI) {
      CalculationParameters probe = new CalculationParameters(0.0, 4.0, CalculationMethod.OTHER);
      probe.madhab = Madhab.SHAFI; probe.highLatitudeRule = HighLatitudeRule.TWILIGHT_ANGLE;
      maghrib = new PrayerTimes(c, dc, probe).isha;
    }
    Date isha = base.isha;
    if (m == M.UAQ && isRamadan(date, off)) isha = new Date(isha.getTime() + 30L*60_000);
    return new Object[]{ inst(base.fajr), inst(base.sunrise), inst(base.dhuhr), inst(base.asr), inst(maghrib), inst(isha), ok ? Double.NaN : ref };
  }
  static String hm(Instant i, ZoneId z) { return i.atZone(z).toLocalTime().toString(); }
  public static void main(String[] a) {
    Object[][] places = {
      {"London", 51.5074, -0.1278, "Europe/London", M.MWL, false},
      {"London (Moonsighting)", 51.5074, -0.1278, "Europe/London", M.MOON, false},
      {"Makkah", 21.4225, 39.8262, "Asia/Riyadh", M.UAQ, false},
      {"Istanbul", 41.0082, 28.9784, "Europe/Istanbul", M.TURKEY, false},
      {"Karachi (Hanafi)", 24.8607, 67.0011, "Asia/Karachi", M.KARACHI, true},
      {"Jakarta", -6.2088, 106.8456, "Asia/Jakarta", M.SINGAPORE, false},
      {"Sydney", -33.8688, 151.2093, "Australia/Sydney", M.MWL, false},
      {"Tromso", 69.6496, 18.9560, "Europe/Oslo", M.MWL, false},
      {"Tromso (Moonsighting)", 69.6496, 18.9560, "Europe/Oslo", M.MOON, false},
      {"Tehran (Jafari)", 35.6892, 51.3890, "Asia/Tehran", M.JAFARI, false},
    };
    LocalDate[] dates = { LocalDate.of(2027,2,7), LocalDate.of(2027,2,8), LocalDate.of(2027,2,9), LocalDate.of(2027,2,21), LocalDate.of(2027,2,22), LocalDate.of(2027,2,23), LocalDate.of(2027,3,7), LocalDate.of(2027,3,8), LocalDate.of(2027,3,9) };
    for (Object[] pl : places) {
      ZoneId z = ZoneId.of((String)pl[3]);
      System.out.println("== " + pl[0] + " " + pl[1] + "," + pl[2] + " zone=" + z + " method=" + pl[4] + (((Boolean)pl[5]) ? " hanafi" : ""));
      Instant prevF=null, prevM=null;
      for (LocalDate d : dates) {
        Object[] r = compute((Double)pl[1], (Double)pl[2], d, (M)pl[4], (Boolean)pl[5], 0);
        Instant f=(Instant)r[0], mg=(Instant)r[4];
        long fast = Duration.between(f, mg).toMinutes();
        String delta = (prevF==null) ? "" : String.format(" dFast=%+d", fast - Duration.between(prevF,prevM).toMinutes());
        HijrahDate h = HijrahDate.from(d);
        System.out.printf("%s (%d/%d) Fajr %s Sunrise %s Dhuhr %s Asr %s Maghrib %s Isha %s fast=%dh%02dm%s%s%n", d, h.get(ChronoField.DAY_OF_MONTH), h.get(ChronoField.MONTH_OF_YEAR), hm(f,z), hm((Instant)r[1],z), hm((Instant)r[2],z), hm((Instant)r[3],z), hm(mg,z), hm((Instant)r[5],z), fast/60, fast%60, delta, Double.isNaN((Double)r[6]) ? "" : " APPROX_FROM=" + r[6]);
        prevF=f; prevM=mg;
      }
    }
    System.out.println("== Makkah UAQ Isha-Maghrib gap around the boundary, offsets -2..2");
    for (LocalDate d : new LocalDate[]{LocalDate.of(2027,2,5),LocalDate.of(2027,2,6),LocalDate.of(2027,2,7),LocalDate.of(2027,2,8),LocalDate.of(2027,2,9),LocalDate.of(2027,2,10),LocalDate.of(2027,3,7),LocalDate.of(2027,3,8),LocalDate.of(2027,3,9),LocalDate.of(2027,3,10),LocalDate.of(2027,3,11)}) {
      StringBuilder sb = new StringBuilder(d.toString());
      for (int off=-2; off<=2; off++) {
        Object[] x = compute(21.4225, 39.8262, d, M.UAQ, false, off);
        sb.append(String.format(" off%+d:%d", off, Duration.between((Instant)x[4],(Instant)x[5]).toMinutes()));
      }
      System.out.println(sb);
    }
    System.out.println("== London DST crossing 27/28 Mar 2027 (MWL)");
    ZoneId L = ZoneId.of("Europe/London");
    Instant pf=null, pm=null;
    for (LocalDate d : new LocalDate[]{LocalDate.of(2027,3,27),LocalDate.of(2027,3,28),LocalDate.of(2027,3,29)}) {
      Object[] x = compute(51.5074,-0.1278,d,M.MWL,false,0);
      Instant f=(Instant)x[0], m=(Instant)x[4];
      long fast=Duration.between(f,m).toMinutes();
      System.out.printf("%s Fajr %s Maghrib %s fast=%dh%02dm%s%n", d, hm(f,L), hm(m,L), fast/60, fast%60, pf==null?"":String.format(" dFast=%+d", fast-Duration.between(pf,pm).toMinutes()));
      pf=f; pm=m;
    }
    System.out.println("== Tromso MWL 2027: projected date ranges");
    LocalDate d = LocalDate.of(2027,1,1); boolean prev=false; LocalDate start=null;
    while (d.getYear()==2027) {
      Object[] x = compute(69.6496,18.9560,d,M.MWL,false,0);
      boolean ap = !Double.isNaN((Double)x[6]);
      if (ap && !prev) start=d;
      if (!ap && prev) System.out.println("projected " + start + " to " + d.minusDays(1));
      prev=ap; d=d.plusDays(1);
    }
    if (prev) System.out.println("projected " + start + " to 2027-12-31");
  }
}
