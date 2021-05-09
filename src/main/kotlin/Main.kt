import java.io.File

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    println("Specify project root to search for RLIs!")
    return
  }

  File(args[0])
    .walkDir { extension == "rst" }
    .flatMap { it.findRlis() }
    .forEach { it.status() }

  with(report.toString()) {
    File("report.md").writeText(this)
    println(this)
  }
}

fun buildRli(result: MatchResult, file: RliFile): LocatedRli? {
  assert(result.value matches rliRegex.toRegex())
  val idxRange = result.groups[1]?.range ?: IntRange.EMPTY
  val (version, path, _lines) = result.destructured
  val lines = _lines.toLineRange()
  val loc = Location(file, idxRange)
  if (version == latestVersion) {
    report.upToDate(url = path, lines, loc)
    return null
  }
  return loc to Rli("$baseURL/$version/$path", lines)
}
