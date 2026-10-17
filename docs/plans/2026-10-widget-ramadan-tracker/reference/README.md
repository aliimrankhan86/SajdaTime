# Reference programs used in planning

Two small Java programs that produced the numbers in `../RAMADAN-LOCATION-SPEC.md`. They are kept so the
implementer can regenerate or extend the golden values without trusting the planning session.

- `Hijri.java`: the Umm al-Qura calendar facts (section 3) straight from `java.time`. No dependencies.
- `Mirror.java`: a line by line mirror of `core/PrayerEngine.compute` over `adhan-java 1.2.1` (section
  9.2). Needs the adhan jar: `find ~/.gradle/caches -name 'adhan-1.2.1.jar'`. **It is a mirror, not the
  engine.** `RamadanGoldenTest` runs the real engine and wins any disagreement.

Neither is part of any Gradle module and neither ships. Do not move them into `core/` or `app/`.
