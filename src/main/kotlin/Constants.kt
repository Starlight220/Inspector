package io.starlight.inspector

import com.github.starlight220.actions.Input
import java.io.File
import kotlinx.serialization.*
import kotlinx.serialization.json.*

private const val reportFilePath = "report.md"
private const val oldTmpFilePath = "old.tmp"
private const val newTmpFilePath = "new.tmp"

private const val RLI_HEADER_REGEX = """\.\. (?:rli|remoteliteralinclude)::"""
private const val RLI_LINES_REGEX = """\r?\n[ ]*:lines: (\d*-\d*)"""

/** Constants Namespace */
object Constants {
    // files
    val reportFile = File(reportFilePath)
    val oldTmpFile = File(oldTmpFilePath)
    val newTmpFile = File(newTmpFilePath)

    /** Search root for RLI files */
    val root by Input("root", mapper = ::File)

    val ignoredFiles by Input<List<String>>("ignoredFiles") { Json.decodeFromString(it) }

    const val diffCommand: String = "git diff --no-index --no-prefix -U200 -- "
    val diffSplitRegex = """@@ [-]?\d+,?\d* [+]?\d+,?\d* @@""".toRegex()

    /** Base URL for all RLIs. Contains terminating `/`. */
    val baseUrl by Input("baseUrl") { str -> if (str.endsWith('/')) str else "$str/" }

    /** Version Regex */
    val versionScheme by Input

    /** Latest Version */
    val latestVersion by Input

    val rliRegex by lazy {
        val r =
            """$RLI_HEADER_REGEX ${Regex.fromLiteral(baseUrl).pattern}($versionScheme)/([/\w.]+)\r?\n.*$RLI_LINES_REGEX""".toRegex()
        println(r)
        r
    }

    override fun toString(): String {
        return """
      $root
      $reportFilePath
      $RLI_HEADER_REGEX
      $baseUrl
      $versionScheme
      $RLI_LINES_REGEX
      ${rliRegex.pattern}
        """.trimIndent()
    }
}
