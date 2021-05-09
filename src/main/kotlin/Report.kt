import java.util.*

object report {
  private val builder by lazy { StringBuilder() }

  private val upToDate = LinkedList<String>()
  fun upToDate(url: String, lines: IntRange, loc: Location) = with(loc) {
    upToDate.push("$url:$lines @ \\<$file:$line\\>")
  }

  private val outdated = LinkedList<String>()
  fun outdated(url: String, lines: IntRange, loc: Location) = with(loc) {
    outdated.push("$url:$lines @ $file:$line")
  }

  private val invalid = LinkedList<String>()
  fun invalid(obj: RliStatus.Invalid) = with(obj) {
    invalid.push(
      """
    |
    |${diff.old.url}:${diff.old.lines} @ \<${location.file}:${location.line}\> ]:
    |[Old](${diff.old.url}):
    |```${diff.old.response}```
    |
    |[New](${diff.new.url}):
    |```${diff.new.response}```
    |""".trimMargin()
    )
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
