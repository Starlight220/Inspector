package io.starlight.inspector

import io.starlight.env.Output
import java.util.*

object Report {
    private val upToDate = LinkedList<Triple<String, LineRange, Location>>()
    private val outdated = LinkedList<LocatedRli>()
    private val invalid = LinkedList<Pair<Location, String>>()

    private inline fun <T> locationComparator(crossinline mapper: (T) -> Location): Comparator<T> =
            Comparator { first, second ->
        val loc1 = mapper(first)
        val loc2 = mapper(second)
        loc1.compareTo(loc2)
    }

    /** Report an RLI as up-to-date */
    fun upToDate(url: String, lines: LineRange, loc: Location) =
        upToDate.push(Triple(url, lines, loc))

    /** Report an RLI as outdated and automatically fixed */
    fun outdated(rli: Rli, location: Location): Unit = outdated.push(location to rli)

    /** Report an RLI as invalid and requires manual attention */
    fun invalid(obj: RliStatus.Invalid) =
        with(obj) {
            invalid.push(
                location to
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
    |${upToDate.sortedWith(locationComparator{it.third}).joinToString("\n") { (url, lines, loc) -> "${url}#${lines} @ <${loc.file}:${loc.line}>" }}
    |```
    |
    |</details>
    |
    |### Outdated - Automatically Fixed
    |
    |<details>
    |
    |```
    |${outdated.sortedWith(locationComparator { it.first }).joinToString("\n") { (location, rli) -> "${rli.url}#${rli.lines} @ <${location.file}:${location.line}>"}}
    |```
    |
    |</details>
    |
    |### Invalid - Manual Intervention Needed
    |
    |<details>
    |
    |${invalid.sortedWith(locationComparator { it.first }).joinToString("\n"){ it.second }}
    |
    |</details>
    |
  """.trimMargin()

    private var needsManual by Output<Boolean>("needs-manual")
    private var isUpToDate by Output<Boolean>("up-to-date")
    private var reportFilePath by Output<String>("report-file-path")
    private var report by Output
    operator fun invoke() {
        val _needsManual = invalid.isNotEmpty()
        needsManual = _needsManual
        isUpToDate = !_needsManual && outdated.isEmpty()
        reportFilePath = Constants.reportFile.canonicalPath
        report = toString()
        Constants.reportFile.writeText(report)
        println(report)
    }
}
