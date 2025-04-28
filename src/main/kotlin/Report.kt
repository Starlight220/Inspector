package io.starlight.inspector

import com.github.starlight220.actions.Output
import com.github.starlight220.actions.raw.setSummary
import java.io.File
import java.util.*

object Report {
    private val upToDateFiles = LinkedList<RliFile>()
    private val upToDate = LinkedList<LocatedRli>()
    private val outdated = LinkedList<LocatedRli>()
    private val invalid = LinkedList<Pair<Location, String>>()

    private inline fun <T> locationComparator(crossinline mapper: (T) -> Location): Comparator<T> =
        Comparator { first, second ->
            val loc1 = mapper(first)
            val loc2 = mapper(second)
            loc1.compareTo(loc2)
        }

    /** Report an RLI as up-to-date */
    fun upToDate(locatedRli: LocatedRli) {
        if (locatedRli.location.file in upToDateFiles) return
        if (locatedRli !in upToDate) upToDate.push(locatedRli)
    }

    /** Report an RLI as outdated and automatically fixed */
    fun outdated(rli: Rli, location: Location): Unit = outdated.push(LocatedRli(location, rli))

    /** Report an RLI as invalid and requires manual attention */
    fun invalid(obj: RliStatus.Invalid) =
        with(obj) {
            invalid.push(
                location to
                    """
                |> [${diff.new.version} |](${diff.old.withLatest.fullUrl}) [${diff.old.version}/${diff.old.url}#${diff.old.lines}](${diff.old.fullUrl}) @ <$location>
                |```diff
                |${diff.buildDiffBlock()}
                |```
                |
            """
                        .trimMargin())
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
    |${upToDateFiles.sorted().joinToString("\n") { "ALL @ <$it>" }}
    |${upToDate.sortedWith(locationComparator{it.location}).joinToString("\n") { (loc, rli) -> "${rli.url}#${rli.lines} @ <$loc>" }}
    |```
    |
    |</details>
    |
    |### Outdated - Automatically Fixed
    |
    |<details>
    |
    |```
    |${outdated.sortedWith(locationComparator { it.location }).joinToString("\n") { (location, rli) -> "${rli.url}#${rli.lines} @ <$location>"}}
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
  """
            .trimMargin()

    private fun reduceUpToDateFiles() {
        buildSet { upToDate.forEach { upToDateRli -> add(upToDateRli.location.file) } }
            .filter { rliFile ->
                outdated.none { (location, _) -> rliFile == location.file } &&
                    invalid.none { (location, _) -> rliFile == location.file }
            }
            .forEach {
                upToDateFiles.push(it)
                upToDate.removeAll { (loc, _) -> loc.file == it }
            }
    }

    private var needsManual by Output<Boolean>("needs-manual")
    private var isUpToDate by Output<Boolean>("up-to-date")
    private var reportFilePath by Output<String>("report-file-path")
    private var report by Output

    private const val REPORT_FILE_PATH = "report.md"

    operator fun invoke() {
        reduceUpToDateFiles()

        invalid.isNotEmpty().also {
            needsManual = it
            isUpToDate = !it && outdated.isEmpty()
        }
        val markdown = toString()
        report = markdown
        setSummary(markdown)

        with(File(REPORT_FILE_PATH)) {
            println("Overwritten report: ${createNewFile()}")
            reportFilePath = canonicalPath
            writeText(markdown)

            println("Wrote `${readText()}` to `$canonicalPath`")
        }
    }
}
