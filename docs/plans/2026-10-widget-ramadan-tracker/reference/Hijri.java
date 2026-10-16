import java.time.*;
import java.time.chrono.*;
import java.time.temporal.*;

/**
 * Prints the Umm al-Qura facts in RAMADAN-LOCATION-SPEC.md section 3 from java.time itself.
 * Run: javac Hijri.java && java Hijri   (JDK 17 or later, no dependencies)
 */
public class Hijri {
  public static void main(String[] a){
    System.out.println("HijrahChronology id=" + HijrahChronology.INSTANCE.getId() + " calendarType=" + HijrahChronology.INSTANCE.getCalendarType());
    for (int y : new int[]{1446,1447,1448,1449,1450}) {
      HijrahDate r1 = HijrahDate.of(y, 9, 1);
      HijrahDate s1 = HijrahDate.of(y, 8, 1);
      HijrahDate sh1 = HijrahDate.of(y, 10, 1);
      LocalDate g1 = LocalDate.from(r1);
      System.out.println("1 Ramadan " + y + " = " + g1 + " (" + g1.getDayOfWeek() + "), Ramadan length=" + r1.lengthOfMonth() + ", Shaban length=" + s1.lengthOfMonth() + ", last day=" + LocalDate.from(sh1).minusDays(1) + ", 1 Shawwal=" + LocalDate.from(sh1));
    }
    try { System.out.println("supported years " + HijrahChronology.INSTANCE.range(ChronoField.YEAR)); } catch (Exception e) { System.out.println(e); }
    for (String d : new String[]{"2026-02-17","2026-02-18","2026-02-19","2026-02-20","2026-03-19","2026-03-20","2027-02-07","2027-02-08","2027-03-08","2027-03-09","2027-03-10"}) {
      LocalDate ld = LocalDate.parse(d);
      HijrahDate h = HijrahDate.from(ld);
      System.out.println(d + " -> " + h.get(ChronoField.DAY_OF_MONTH) + "/" + h.get(ChronoField.MONTH_OF_YEAR) + "/" + h.get(ChronoField.YEAR) + " lengthOfMonth=" + h.lengthOfMonth());
    }
    // The app's shift: the header shows HijrahDate.from(date.plusDays(offset)).
    LocalDate eve = LocalDate.parse("2027-02-07");
    for (int off=-2; off<=2; off++) {
      HijrahDate h = HijrahDate.from(eve.plusDays(off));
      System.out.println("2027-02-07 with offset " + off + " shows " + h.get(ChronoField.DAY_OF_MONTH) + "/" + h.get(ChronoField.MONTH_OF_YEAR));
    }
  }
}
