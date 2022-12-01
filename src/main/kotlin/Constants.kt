package io.starlight.inspector

import com.github.starlight220.actions.Input
import java.io.File
import kotlinx.serialization.*
import kotlinx.serialization.json.*

private const val reportFilePath = "report.md"

private const val RLI_HEADER_REGEX = """\.\. (?:rli|remoteliteralinclude)::"""
private const val RLI_LINES_REGEX = """\r?\n[ ]*:lines: (\d*-\d*)"""

lateinit var Constants: ConstantSet

/** Constants Namespace */
sealed class ConstantSet {
    class InputConstants : ConstantSet() {
        override val ignoredFiles by Input<List<String>>("ignoredFiles") {
            Json.decodeFromString(it)
        }
        override val baseUrl by Input("baseUrl") { str -> if (str.endsWith('/')) str else "$str/" }
        override val latestVersion: String by Input
        override val versionScheme: String by Input
    }

    @Serializable
    data class JsonConstants(
        override val baseUrl: String,
        override val versionScheme: String,
        override val latestVersion: String,
        override val ignoredFiles: List<String>
    ) : ConstantSet()

    // files
    val reportFile = File(reportFilePath)

    /** Search root for RLI files */
    val root by Input("root", mapper = ::File)

    abstract val ignoredFiles: List<String>

    val diffCommand: String = "git diff --no-index --no-prefix -U200 -- "
    val diffSplitRegex = """@@ [-]?\d+,?\d* [+]?\d+,?\d* @@""".toRegex()

    /** Base URL for all RLIs. Contains terminating `/`. */
    abstract val baseUrl: String

    /** Version Regex */
    abstract val versionScheme: String

    /** Latest Version */
    abstract val latestVersion: String

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
