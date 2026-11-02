package com.sajdatime.app

import java.io.File

/**
 * Every app string folder: English `values` first, then each translation found on disk.
 *
 * Found, not listed, since Bengali (30 Oct 2026): four wording tests each kept their own copy of the
 * language list, so a new language passed every one of them unchecked until someone remembered to
 * edit each file. Now a new `values-<lang>/` is checked by all of them the moment it exists, and
 * [LanguagePickerTest] fails until the picker offers it. Matches `values-bn`, `values-in` and
 * `values-pt-rBR`, never `values-night` or `values-v31`.
 */
internal val stringFolders: List<String> = listOf("values") +
    File("..", "app/src/main/res").listFiles().orEmpty()
        .filter { Regex("""values-[a-z]{2,3}(-r[A-Z]{2})?""").matches(it.name) && File(it, "strings.xml").isFile }
        .map { it.name }
        .sorted()
