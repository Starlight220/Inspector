package io.starlight.inspector

import io.starlight.env.Output
import java.util.*

object Report {
    private val upToDate = LinkedList<String>()
    private val outdated = LinkedList<String>()
    private val invalid = LinkedList<String>()

    /** Report an RLI as up-to-date */
    fun upToDate(url: String, lines: LineRange, loc: Location): Unit =
        upToDate.push(formatHeader(loc, Rli(url, lines)))

    /** Report an RLI as outdated and automatically fixed */
    fun outdated(rli: Rli, location: Location): Unit = outdated.push(formatHeader(location, rli))

    /** Report an RLI as invalid and requires manual attention */
    fun invalid(obj: RliStatus.Invalid) =
        with(obj) {
            invalid.push(
                "${formatHeader(location, diff.old)}\n```diff\n${buildDiffBlock(diff)}\n```\n"
            )
        }

    private fun formatHeader(location: Location, rli: Rli) =
        "> [${rli.url}#${rli.lines}](${Constants.baseUrl}${rli.url}) @ <${location.file}:${location.line}>"

    override fun toString(): String =
        """
    |# Inspector Report
    |
    |***
    |
    |### Up To Date
    |
    |<details>
    |
    |```
    |${upToDate.joinToString("\n")}
    |```
    |
    |</details>
    |
    |### Outdated - Automatically Fixed
    |
    |<details>
    |
    |```
    |${outdated.joinToString("\n")}
    |```
    |
    |</details>
    |
    |### Invalid - Manual Intervention Needed
    |
    |<details>
    |
    |${invalid.joinToString("\n")}
    |
    |</details>
    |
  """.trimMargin()

    private var needsManual by Output<Boolean>("needs-manual")
    private var isUpToDate by Output<Boolean>("up-to-date")
    private var reportFilePath by Output<String>("report-file-path")
    private var report by Output
    operator fun invoke() {
        needsManual = invalid.isNotEmpty()
        isUpToDate = !needsManual && outdated.isEmpty()
        reportFilePath = Constants.reportFile.canonicalPath
        report = toString()
        Constants.reportFile.writeText(report)
        //        println(report)
    }
}
