package io.starlight.inspector

import io.starlight.env.Input
import java.io.File

private const val reportFilePath = "report.md"
private const val oldTmpFilePath = "old.tmp"
private const val newTmpFilePath = "new.tmp"

private const val rliHeaderRegex = """\.\. (?:rli)|(?:remoteliteralinclude)::"""
private const val rliLinesRegex = """\r?\n[ ]*:lines: (\d*-\d*)"""

/** Constants Namespace */
object Constants {
    // files
    val reportFile = File(reportFilePath)
    val oldTmpFile = File(oldTmpFilePath)
    val newTmpFile = File(newTmpFilePath)

    /** Search root for RLI files */
    val root by Input(mapper = ::File)

    const val diffCommand: String = "git diff --no-index --no-prefix -U200 -- "
    val diffSplitRegex = """@@ [-]?\d+,?\d* [+]?\d+,?\d* @@""".toRegex()

    /** Base URL for all RLIs. Contains terminating `/`. */
    val baseUrl by Input { str -> if (str.endsWith('/')) str else "$str/" }

    /** Version Regex */
    val versionScheme by Input

    /** Latest Version */
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
      $reportFilePath     
      $rliHeaderRegex
      $baseUrl
      $versionScheme
      $rliLinesRegex
      ${rliRegex.pattern}
        """.trimIndent()
    }
}
