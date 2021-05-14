package io.starlight.rli

import java.util.*

object Report {
  private val upToDate = LinkedList<String>()
  fun upToDate(url: String, lines: IntRange, loc: Location) =
      with(loc) { upToDate.push("$url:$lines @ <$file:$line>") }

  private val outdated = LinkedList<String>()
  fun outdated(url: String, lines: IntRange, loc: Location) =
      with(loc) { outdated.push("$url:$lines @ $file:$line") }

  private val invalid = LinkedList<String>()
  fun invalid(obj: RliStatus.Invalid) =
      with(obj) {
        Constants.oldTmpFile.writeText(diff.old.response)
        Constants.newTmpFile.writeText(diff.new.response)

        invalid.push(
            "${diff.old.url}#L${diff.old.lines.first}-L${diff.old.lines.last} @ <${location.file}:${location.line}>\n```diff\n${calculateDiff()}\n```\n")
      }

  private fun calculateDiff(): String =
      Runtime.getRuntime()
          .exec(
              Constants.run {
                "$diffCommand ${oldTmpFile.canonicalPath} ${newTmpFile.canonicalPath}"
              })
          .inputStream
          .bufferedReader()
          .readText()
          .split(Constants.diffSplitRegex)
          .asSequence()
          .drop(1) // drop header
          .joinToString("\n")

  override fun toString(): String =
      """
    |### Up To Date
    |```
    |${upToDate.joinToString("\n")}
    |```
    |
    |### Outdated - Automatically Fixed
    |```
    |${outdated.joinToString("\n")}
    |```
    |
    |### Invalid - Manual Intervention Needed
    |
    |${invalid.joinToString("\n")}
    |
  """.trimMargin()
}
