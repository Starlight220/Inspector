package io.starlight.rli

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
  val root by Input(::File)

  const val diffCommand: String = "git diff --no-index -- "
  val diffSplitRegex = """@@ [-]?\d+,\d+ [+]?\d+,\d+ @@""".toRegex()

  val baseUrl by Input()
  val versionScheme by Input()
  val latestVersion by Input()

  val rliRegex by lazy {
    """$rliHeaderRegex ${Regex.fromLiteral(baseUrl).pattern}($versionScheme)/([/\w.]+)\r?\n.*$rliLinesRegex""".toRegex()
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
