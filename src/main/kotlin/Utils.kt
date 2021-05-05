import java.io.File
import java.net.URL
import java.util.*

fun File.walkDir(predicate: File.() -> Boolean): Set<File> {
  return walk().filter(predicate).toSet()
}

fun String.indexToLineNumber(index: Int): Int {
  var line = 0
  repeat(index) { i ->
    if (this[i] == '\n') line++
  }
  return line
}

fun String.toLineRange(): IntRange {
  val parts = this.split('-')
  return when (parts.size) {
    1 -> parts[0].toInt().run { this..this }
    2 -> parts[0].toInt()..parts[1].toInt()
    else -> throw NumberFormatException("error in parsing numbers: `$this`")
  }
}

data class Location(val file: String, val line: Int)

data class Rli(val url: String, val lines: IntRange) {
  val response: String by lazy {
    URL(url).readText().splitToSequence('\n').filterIndexed { i, _ -> i in lines }
      .joinToString(separator = "\n")
  }
}

typealias LocatedRli = Pair<Location, Rli>

fun LocatedRli(file: String, line: Int, url: String, lines: IntRange) =
  Location(file, line) to Rli(url, lines)


object report {
  private val builder by lazy { StringBuilder() }

  private val upToDate = LinkedList<String>()
  fun upToDate(url: String, path: String, line: Int) {
    upToDate.push("$url @ $path:$line")
  }

  private val outdated = LinkedList<String>()
  fun outdated(url: String, path: String, line: Int) {
    outdated.push("$url @ $path:$line")
  }

  private val invalid = LinkedList<String>()
  fun invalid(url: String, path: String, line: Int) {
    invalid.push("$url @ $path:$line")
  }

  operator fun invoke(log: String) {
    builder.appendLine(log)
  }

  override fun toString(): String = """
    |### Up To Date
    |```
    |${upToDate.joinToString("\n")}
    |```
    |
    |### Outdated
    |```
    |${outdated.joinToString("\n")}
    |```
    |
    |### Invalid - Manual Intervention Needed
    |```
    |${invalid.joinToString("\n")}
    |```
  """.trimMargin()
}

sealed class RliStatus(protected val rli: Rli, protected val location: Location) {
  constructor(base: LocatedRli) : this(base.second, base.first)

  class Valid(base: LocatedRli) : RliStatus(base) {
    override fun invoke() {
      report.outdated(rli.url, location.file, location.line)
//      report("RLI of <${rli.url}> in `${location.file}:${location.line}` is out-of-date but can be automatically updated!")
    }
  }

  class Invalid(base: LocatedRli) : RliStatus(base) {
    override fun invoke() {
      report.invalid(rli.url, location.file, location.line)
//      report("RLI of <${rli.url}> in `${location.file}:${location.line}` is invalid and requires manual attention!")
    }
  }


  abstract operator fun invoke()
}

val LocatedRli.status: RliStatus
  get() = with(second) {
    val new = Rli(
      url.replace(Regex("""v\d{4}\.\d\.\d(?:-beta-\d)?"""), latestVersion),
      lines
    )
    if (new.response == this.response) {
      return@with RliStatus.Valid(this@status)
    } else {
      return@with RliStatus.Invalid(this@status)
    }
  }