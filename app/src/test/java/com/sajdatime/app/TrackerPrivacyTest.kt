package com.sajdatime.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Where the prayer record may go, and what the published policy promises about it (1.6 plan, step P).
 * docs/privacy.html is a published promise: if the record's handling changes, these phrases must change
 * with it, in the same commit. privacy.html is a declared test input (app/build.gradle.kts), so editing
 * it re-runs this test instead of leaving it UP-TO-DATE.
 */
class TrackerPrivacyTest {

    private val root = File("..").canonicalFile

    @Test
    fun `nothing the tracker stores can leave the phone by backup or transfer`() {
        val rules = File(root, "app/src/main/res/xml/data_extraction_rules.xml").readText()
        for (section in listOf("cloud-backup", "device-transfer")) {
            val body = Regex("<$section>(.*?)</$section>", RegexOption.DOT_MATCHES_ALL).find(rules)!!.groupValues[1]
            assertTrue("$section keeps the file domain", body.contains("""<exclude domain="file" path="." />"""))
        }
    }

    @Test
    fun `the privacy page makes the four promises about the prayer record`() {
        val page = File(root, "docs/privacy.html").readText()
        val section = Regex("""<h2 id="prayer-record">(.*?)<h2""", RegexOption.DOT_MATCHES_ALL).find(page)
        assertTrue("privacy.html has a prayer record section", section != null)
        val text = section!!.groupValues[1].replace(Regex("\\s+"), " ")
        for (promise in listOf("kept on this phone only", "goes wherever you choose", "removes it from this phone", "never counted")) {
            assertTrue("privacy.html prayer record says \"$promise\"", text.contains(promise))
        }
    }
}
