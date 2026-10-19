package com.sajdatime.core

import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import kotlin.math.abs

/**
 * The sweep that has to pass before a "find the method my mosque follows" feature is built
 * (docs/HANDOVER.md, the 5 Oct 2026 decisions, item 2b).
 *
 * It is a measurement, not a unit test, so it only runs when asked:
 *
 *     SAJDA_SWEEP=1 ./gradlew :core:testDebugUnitTest --tests '*MethodFinderSweepTest*' -i
 *
 * and writes its report to core/build/method-finder-sweep.txt. The questions, in the order
 * the finder's wording depends on the answers:
 *
 *  1. Can Asr and Isha tell the 22 candidates apart (11 Sunni methods x 2 Asr schools)?
 *     How many candidates fit one entered board, at 1, 2, 3 and 5 minutes of tolerance?
 *  2. Why both prayers? The same table for Asr alone, Isha alone, Fajr alone and pairs.
 *  3. A board is never an exact method. Mosques round up and add a precaution minute or two.
 *     Does the right candidate survive that, and what happens to the list?
 *  4. A board can follow no method at all (a committee's own table). How often does such a
 *     board still "match" something? That is the rate at which the finder would be confidently
 *     wrong, and it is the number that decides whether it ships.
 *  5. "Gives the same times as your mosque today" is a claim about one day. When two
 *     candidates agree today, how often do they still agree on the other days of the year?
 *
 * Days where the sun does not give a usable answer (the projected polar days) are skipped
 * for the candidate concerned: the finder is hidden on those days anyway.
 */
class MethodFinderSweepTest {

    private data class Cand(val method: CalcMethod, val school: Madhab) {
        val label: String get() = "${method.name}/${if (school == Madhab.HANAFI) "H" else "S"}"
    }

    private val methods = CalcMethod.entries.filter { it != CalcMethod.AUTO && it.offeredTo(Sect.SUNNI) }
    private val cands = methods.flatMap { m -> listOf(Madhab.SHAFII, Madhab.HANAFI).map { Cand(m, it) } }

    private val lats = (0..60 step 5).map { it.toDouble() }
    private val lons = listOf(0.0, 70.0, -100.0)
    private val dates: List<LocalDate> = generateSequence(LocalDate.of(2026, 1, 3)) { it.plusDays(7) }
        .takeWhile { it.year == 2026 }.toList()
    private val tolerances = listOf(1, 2, 3, 5)

    /** Minutes since the epoch, by prayer; null when that candidate has no usable day here. */
    private fun times(c: Cand, lat: Double, lon: Double, d: LocalDate): Map<PrayerSlot, Long>? {
        val day = PrayerEngine.compute(Coordinates(lat, lon), d, CalculationPrefs(Sect.SUNNI, c.school, c.method))
        if (day.approximated) return null
        return day.times.mapValues { it.value.epochSecond / 60 }
    }

    private class Stats {
        val sizes = IntArray(23)
        var total = 0
        fun add(size: Int) { sizes[size.coerceAtMost(22)]++; total++ }
        fun pct(pred: (Int) -> Boolean): Double =
            if (total == 0) Double.NaN else 100.0 * sizes.indices.filter(pred).sumOf { sizes[it] } / total
        fun median(): Int {
            var seen = 0
            for (i in sizes.indices) { seen += sizes[i]; if (seen * 2 >= total) return i }
            return 0
        }
        fun max(): Int = sizes.indices.lastOrNull { sizes[it] > 0 } ?: 0
    }

    private fun fmt(d: Double) = if (d.isNaN()) "  n/a" else "%5.1f".format(d)

    @Test
    fun sweep() {
        assumeTrue("set SAJDA_SWEEP=1 to run the finder sweep", System.getenv("SAJDA_SWEEP") != null)
        val out = StringBuilder()
        fun line(s: String = "") { out.appendLine(s); println(s) }
        val t0 = System.currentTimeMillis()

        // Precompute: [lat][lon][date] -> per candidate times (null = unusable).
        val table = HashMap<Triple<Int, Int, Int>, List<Map<PrayerSlot, Long>?>>()
        for ((li, lat) in lats.withIndex()) for ((oi, lon) in lons.withIndex()) for ((di, d) in dates.withIndex()) {
            table[Triple(li, oi, di)] = cands.map { times(it, lat, lon, d) }
        }
        line("candidates=${cands.size} lats=${lats.size} lons=${lons.size} dates=${dates.size} computed in ${System.currentTimeMillis() - t0} ms")

        fun fits(a: Map<PrayerSlot, Long>, board: Map<PrayerSlot, Long>, slots: List<PrayerSlot>, tol: Int) =
            slots.all { abs(a.getValue(it) - board.getValue(it)) <= tol }

        // ---------------- 1 and 2: how many candidates fit one board -------------------------
        val subsets = listOf(
            "Asr only" to listOf(PrayerSlot.ASR),
            "Isha only" to listOf(PrayerSlot.ISHA),
            "Fajr only" to listOf(PrayerSlot.FAJR),
            "Fajr+Isha" to listOf(PrayerSlot.FAJR, PrayerSlot.ISHA),
            "Asr+Isha" to listOf(PrayerSlot.ASR, PrayerSlot.ISHA),
            "Fajr+Asr+Isha" to listOf(PrayerSlot.FAJR, PrayerSlot.ASR, PrayerSlot.ISHA),
        )
        line()
        line("== 1 and 2. Candidates that fit an exact board (the board IS one candidate). Lower is better.")
        line("   subset            tol  size1%  size<=2%  size<=3%  size<=4%  median  max")
        for ((name, slots) in subsets) for (tol in tolerances) {
            val st = Stats()
            for ((key, row) in table) for (ti in cands.indices) {
                val truth = row[ti] ?: continue
                st.add(row.count { it != null && fits(it, truth, slots, tol) })
            }
            line("   %-16s %3d  %s  %s  %s  %s  %6d  %3d".format(
                name, tol, fmt(st.pct { it == 1 }), fmt(st.pct { it <= 2 }), fmt(st.pct { it <= 3 }), fmt(st.pct { it <= 4 }), st.median(), st.max()))
        }

        // Which candidates are indistinguishable from which, on Asr+Isha at 1 minute.
        line()
        line("== 1b. Candidate pairs that give the same Asr+Isha (within 1 min) in at least 90% of cases:")
        val ai = listOf(PrayerSlot.ASR, PrayerSlot.ISHA)
        val twin = Array(cands.size) { IntArray(cands.size) }
        val seen = Array(cands.size) { IntArray(cands.size) }
        for ((_, row) in table) for (a in cands.indices) for (b in cands.indices) {
            val ra = row[a] ?: continue; val rb = row[b] ?: continue
            seen[a][b]++; if (fits(ra, rb, ai, 1)) twin[a][b]++
        }
        var anyTwin = false
        for (a in cands.indices) for (b in a + 1 until cands.size) {
            if (seen[a][b] > 0 && twin[a][b] * 10 >= seen[a][b] * 9) {
                anyTwin = true
                line("   ${cands[a].label} ~ ${cands[b].label}: ${"%.0f".format(100.0 * twin[a][b] / seen[a][b])}%")
            }
        }
        if (!anyTwin) line("   none")

        // Same, by latitude band, for Asr+Isha at 2 minutes.
        line()
        line("== 1c. Asr+Isha, tolerance 2 min, by latitude (share of cases with exactly 1 / at most 3 candidates; median size):")
        for ((li, lat) in lats.withIndex()) {
            val st = Stats()
            for (oi in lons.indices) for (di in dates.indices) {
                val row = table.getValue(Triple(li, oi, di))
                for (ti in cands.indices) {
                    val truth = row[ti] ?: continue
                    st.add(row.count { it != null && fits(it, truth, ai, 2) })
                }
            }
            line("   lat %2d: usable=%5d  size1=%s%%  size<=3=%s%%  median=%d  max=%d".format(
                lat.toInt(), st.total, fmt(st.pct { it == 1 }), fmt(st.pct { it <= 3 }), st.median(), st.max()))
        }

        // ---------------- 3: noisy boards ----------------------------------------------------
        line()
        line("== 3. A real board is not exact. Board = true times then perturbed; tolerance 2 min, Asr+Isha.")
        line("   perturbation                          found%  size<=3%  median  max")
        val perturbations: List<Pair<String, (Long, PrayerSlot) -> Long>> = listOf(
            "none" to { m, _ -> m },
            "+1 min on both" to { m, _ -> m + 1 },
            "-1 min on both" to { m, _ -> m - 1 },
            "+2 min on both" to { m, _ -> m + 2 },
            "Isha +2, Asr 0" to { m, s -> if (s == PrayerSlot.ISHA) m + 2 else m },
            "round up to 5 min (clock)" to { m, _ -> ((m + 4) / 5) * 5 },
            "round to nearest 5 (clock)" to { m, _ -> ((m + 2) / 5) * 5 },
        )
        for ((name, f) in perturbations) {
            var found = 0; var n = 0; val st = Stats()
            for ((_, row) in table) for (ti in cands.indices) {
                val truth = row[ti] ?: continue
                val board = ai.associateWith { f(truth.getValue(it), it) }
                val set = row.indices.filter { row[it] != null && fits(row[it]!!, board, ai, 2) }
                n++; if (ti in set) found++
                st.add(set.size)
            }
            line("   %-36s %s  %s  %6d  %3d".format(name, fmt(100.0 * found / n), fmt(st.pct { it in 1..3 }), st.median(), st.max()))
        }

        // ---------------- 4 and 5, for the two candidate designs -----------------------------
        val fai = listOf(PrayerSlot.FAJR, PrayerSlot.ASR, PrayerSlot.ISHA)
        val variants = listOf("Asr+Isha" to ai, "Fajr+Asr+Isha" to fai)

        line()
        line("== 4. Boards that follow NO listed method (a committee's own table): the true times of a candidate with each")
        line("   entered prayer moved by a precaution. Share that still gets a non-empty list = rate of a confident wrong match.")
        line("   Offsets tried: Fajr -8,-4,+4,+8; Asr -3,0,+3; Isha -10,-5,+5,+10,+15 (Cartesian product, only the entered prayers move).")
        line("   design             tol  non-empty%   (target: at most 10%)")
        val fOff = listOf(-8, -4, 4, 8); val aOff = listOf(-3, 0, 3); val iOff = listOf(-10, -5, 5, 10, 15)
        for ((name, slots) in variants) for (tol in listOf(1, 2)) {
            var hit = 0; var n = 0
            for ((_, row) in table) for (ti in cands.indices) {
                val truth = row[ti] ?: continue
                for (df in (if (PrayerSlot.FAJR in slots) fOff else listOf(0))) for (da in aOff) for (di in iOff) {
                    val board = mapOf(
                        PrayerSlot.FAJR to truth.getValue(PrayerSlot.FAJR) + df,
                        PrayerSlot.ASR to truth.getValue(PrayerSlot.ASR) + da,
                        PrayerSlot.ISHA to truth.getValue(PrayerSlot.ISHA) + di,
                    )
                    // Skip the boards that are not really outside the model (every offset zero on the entered prayers).
                    if (da == 0 && df == 0 && di == 0) continue
                    n++; if (row.any { it != null && fits(it, board, slots, tol) }) hit++
                }
            }
            line("   %-17s %3d   %s".format(name, tol, fmt(100.0 * hit / n)))
        }
        line("   By how far Isha alone is moved, Asr 0, Fajr 0 (the simplest precaution), non-empty%:")
        line("   design             tol   +3     +5     +10    +15    +30")
        for ((name, slots) in variants) for (tol in listOf(1, 2)) {
            val parts = listOf(3, 5, 10, 15, 30).map { k ->
                var hit = 0; var n = 0
                for ((_, row) in table) for (ti in cands.indices) {
                    val truth = row[ti] ?: continue
                    val board = mapOf(
                        PrayerSlot.FAJR to truth.getValue(PrayerSlot.FAJR),
                        PrayerSlot.ASR to truth.getValue(PrayerSlot.ASR),
                        PrayerSlot.ISHA to truth.getValue(PrayerSlot.ISHA) + k,
                    )
                    n++; if (row.any { it != null && fits(it, board, slots, tol) }) hit++
                }
                fmt(100.0 * hit / n)
            }
            line("   %-17s %3d  %s %s %s %s %s".format(name, tol, parts[0], parts[1], parts[2], parts[3], parts[4]))
        }

        line()
        line("== 5. Candidates that agree TODAY: how often do they agree on the rest of the year? (lon 0, tolerance = the design's)")
        line("   mean-agree = average share of sampled days a today-matching WRONG candidate still agrees; drift = share of such")
        line("   (today, wrong candidate) cases that are off by more than 5 min on at least 20% of the days. Targets: 90%+ and at most 10%.")
        for ((name, slots) in variants) for (tol in listOf(1, 2)) {
            line("   -- $name, tol $tol:   lat   cases   mean-agree%   drift%")
            for ((li, lat) in lats.withIndex()) {
                var cases = 0; var sumAgree = 0.0; var drift = 0
                for (d0 in dates.indices) {
                    val row0 = table.getValue(Triple(li, 0, d0))
                    for (t in cands.indices) {
                        val tt = row0[t] ?: continue
                        for (c in cands.indices) {
                            if (c == t) continue
                            val cc = row0[c] ?: continue
                            if (!fits(cc, tt, slots, tol)) continue
                            var agree = 0; var big = 0; var days = 0
                            for (d in dates.indices) {
                                val r = table.getValue(Triple(li, 0, d))
                                val a = r[t] ?: continue; val b = r[c] ?: continue
                                days++
                                if (fits(b, a, slots, tol)) agree++
                                if (!fits(b, a, slots, 5)) big++
                            }
                            if (days == 0) continue
                            cases++; sumAgree += 100.0 * agree / days
                            if (big * 5 >= days) drift++
                        }
                    }
                }
                line("                          %3d  %6d   %s        %s".format(lat.toInt(), cases,
                    fmt(if (cases == 0) Double.NaN else sumAgree / cases),
                    fmt(if (cases == 0) Double.NaN else 100.0 * drift / cases)))
            }
        }


        // ---------------- 6: which pairs drift ------------------------------------------------
        line()
        line("== 6. Which (true, wrong) pairs drift? Fajr+Asr+Isha, tol 1, latitudes 25..40, lon 0. Pairs ordered by drift cases.")
        val pairCases = HashMap<Pair<Int, Int>, IntArray>()
        for (li in lats.indices.filter { lats[it] in 25.0..40.0 }) for (d0 in dates.indices) {
            val row0 = table.getValue(Triple(li, 0, d0))
            for (t in cands.indices) {
                val tt = row0[t] ?: continue
                for (c in cands.indices) {
                    if (c == t) continue
                    val cc = row0[c] ?: continue
                    if (!fits(cc, tt, fai, 1)) continue
                    var big = 0; var days = 0
                    for (d in dates.indices) {
                        val r = table.getValue(Triple(li, 0, d)); val a = r[t] ?: continue; val b = r[c] ?: continue
                        days++; if (!fits(b, a, fai, 5)) big++
                    }
                    if (days == 0) continue
                    val e = pairCases.getOrPut(t to c) { IntArray(2) }
                    e[0]++; if (big * 5 >= days) e[1]++
                }
            }
        }
        for ((k, v) in pairCases.entries.sortedByDescending { it.value[1] }.take(14)) {
            line("   true ${cands[k.first].label}  vs wrong ${cands[k.second].label}: matched-today cases=${v[0]}, drifting=${v[1]}")
        }

        // ---------------- 7 and 8: the narrow design -----------------------------------------
        // Fajr+Asr+Isha, 1 minute, and only a definite answer: the finder speaks only when
        // every candidate that fits belongs to ONE group of rules that give identical times.
        // Twins are found from the data, not assumed: two candidates are twins when they agree
        // within 1 minute on all three prayers in at least 99% of all sampled cases.
        val parent = IntArray(cands.size) { it }
        fun find(x: Int): Int { var r = x; while (parent[r] != r) r = parent[r]; return r }
        val agree3 = Array(cands.size) { IntArray(cands.size) }
        val seen3 = Array(cands.size) { IntArray(cands.size) }
        for ((_, row) in table) for (a in cands.indices) for (b in cands.indices) {
            val ra = row[a] ?: continue; val rb = row[b] ?: continue
            seen3[a][b]++; if (fits(ra, rb, fai, 1)) agree3[a][b]++
        }
        for (a in cands.indices) for (b in a + 1 until cands.size) {
            if (seen3[a][b] > 0 && agree3[a][b] * 100 >= seen3[a][b] * 99) parent[find(b)] = find(a)
        }
        val groups = cands.indices.groupBy { find(it) }.values.toList()
        line()
        line("== 7. Twin groups on Fajr+Asr+Isha (1 min, 99% of cases): ${groups.size} groups from ${cands.size} candidates")
        for (g in groups.filter { it.size > 1 }) line("   " + g.joinToString(" = ") { cands[it].label })

        fun groupsOf(set: List<Int>) = set.map { find(it) }.toSet()

        line()
        line("   Exact boards, Fajr+Asr+Isha tol 1: share where the answer is ONE group (a definite answer), by latitude:")
        var defTotal = 0; var defHit = 0; var wrong = 0
        for ((li, lat) in lats.withIndex()) {
            var n = 0; var one = 0
            for (oi in lons.indices) for (di in dates.indices) {
                val row = table.getValue(Triple(li, oi, di))
                for (ti in cands.indices) {
                    val truth = row[ti] ?: continue
                    val set = row.indices.filter { row[it] != null && fits(row[it]!!, truth, fai, 1) }
                    n++
                    if (groupsOf(set).size == 1) { one++; if (find(ti) !in groupsOf(set)) wrong++ }
                }
            }
            defTotal += n; defHit += one
            line("   lat %2d: definite %s%% of %d".format(lat.toInt(), fmt(100.0 * one / n), n))
        }
        line("   overall definite: ${fmt(100.0 * defHit / defTotal)}%   definite-but-wrong (must be 0): $wrong")

        line()
        line("== 8. Out-of-model boards (constant precaution on the committee's table), narrow design:")
        line("   a definite answer is given for what share of such boards, and over the year (lon 0) how long does it keep matching")
        line("   the committee's own table (all three prayers within 2 min)? Targets: definite at most 2%, persistence 90%+.")
        val offsets = listOf(Triple(-4, 0, 3), Triple(0, 0, 5), Triple(-8, 0, 10), Triple(4, 3, -5), Triple(-4, -3, 8), Triple(0, 3, 15), Triple(-8, 0, 0), Triple(0, 0, 3))
        var nB = 0; var defB = 0; var sumPersist = 0.0; var persistCases = 0; var lowPersist = 0
        for ((li, _) in lats.withIndex()) for (d0 in dates.indices) {
            val row0 = table.getValue(Triple(li, 0, d0))
            for (t in cands.indices) {
                val tt = row0[t] ?: continue
                for ((df, da, di) in offsets) {
                    val board = mapOf(PrayerSlot.FAJR to tt.getValue(PrayerSlot.FAJR) + df, PrayerSlot.ASR to tt.getValue(PrayerSlot.ASR) + da, PrayerSlot.ISHA to tt.getValue(PrayerSlot.ISHA) + di)
                    nB++
                    val set = row0.indices.filter { row0[it] != null && fits(row0[it]!!, board, fai, 1) }
                    if (set.isEmpty() || groupsOf(set).size != 1) continue
                    defB++
                    val x = set.first()
                    var agree = 0; var days = 0
                    for (d in dates.indices) {
                        val r = table.getValue(Triple(li, 0, d)); val a = r[t] ?: continue; val b = r[x] ?: continue
                        val own = mapOf(PrayerSlot.FAJR to a.getValue(PrayerSlot.FAJR) + df, PrayerSlot.ASR to a.getValue(PrayerSlot.ASR) + da, PrayerSlot.ISHA to a.getValue(PrayerSlot.ISHA) + di)
                        days++; if (fits(b, own, fai, 2)) agree++
                    }
                    if (days > 0) { persistCases++; val pct = 100.0 * agree / days; sumPersist += pct; if (pct < 60) lowPersist++ }
                }
            }
        }
        line("   boards tried: $nB   definite answers: $defB = ${fmt(100.0 * defB / nB)}%   persistence (mean) ${fmt(if (persistCases == 0) Double.NaN else sumPersist / persistCases)}%   of those, persisting under 60% of days: ${fmt(if (persistCases == 0) Double.NaN else 100.0 * lowPersist / persistCases)}%")
        line()
        line("done in ${System.currentTimeMillis() - t0} ms")
        File("build/method-finder-sweep.txt").apply { parentFile?.mkdirs() }.writeText(out.toString())
    }
}
