package io.starlight.inspector

import io.starlight.env.Output
import java.util.*

object Report {
    private val upToDate = LinkedList<String>()
    private val outdated = LinkedList<String>()
    private val invalid = LinkedList<String>()

    /** Report an RLI as up-to-date */
    fun upToDate(url: String, lines: LineRange, loc: Location) =
        upToDate.push("${url}#${lines} @ <${loc.file}:${loc.line}>")

    /** Report an RLI as outdated and automatically fixed */
    fun outdated(rli: Rli, location: Location): Unit =
        outdated.push("${rli.url}#${rli.lines} @ <${location.file}:${location.line}>")

    /** Report an RLI as invalid and requires manual attention */
    fun invalid(obj: RliStatus.Invalid) =
        with(obj) {
            invalid.push(
                """
                |> [${Constants.latestVersion} |](${diff.old.withLatest.fullUrl}) [${diff.old.version}/${diff.old.url}#${diff.old.lines}](${diff.old.fullUrl}) @ <${location.file}:${location.line}>
                |```diff
                |${buildDiffBlock(diff)}
                |```
                |
            """.trimMargin()
            )
        }

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
        println(report)
    }
}
