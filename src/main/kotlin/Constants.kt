package io.starlight.rli

import io.starlight.rli.env.Input
import java.io.File

private const val reportFilePath = "report.md"
private const val oldTmpFilePath = "old.tmp"
private const val newTmpFilePath = "new.tmp"

private const val rliHeaderRegex = """\.\. (?:rli)|(?:remoteliteralinclude)::"""
private const val rliLinesRegex = """\r?\n[ ]{9}:lines: (\d*-\d*)"""

object Constants {
    // files
    val reportFile = File(reportFilePath)
    val oldTmpFile = File(oldTmpFilePath)
    val newTmpFile = File(newTmpFilePath)
    val root by Input(mapper = ::File)

    const val diffCommand: String = "git diff --no-index --no-prefix -U200 -- "
    val diffSplitRegex = """@@ [-]?\d+,\d+ [+]?\d+,\d+ @@""".toRegex()

    val baseUrl by Input
    val versionScheme by Input
    val latestVersion by Input

    val rliRegex by lazy {
        val r =
            """$rliHeaderRegex ${Regex.fromLiteral(baseUrl).pattern}($versionScheme)/([/\w.]+)\r?\n.*$rliLinesRegex""".toRegex()
        println(r)
        r
    }

    override fun toString(): String {
        return """
      $root
      $rliHeaderRegex
      $baseUrl
      $versionScheme
      $rliLinesRegex
      ${rliRegex.pattern}
        """.trimIndent()
    }
}
