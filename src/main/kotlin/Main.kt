import java.io.File

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    println("Specify project root to search for RLIs!")
    return
  }

  File(args[0])
    .walkDir { extension == "rst" }
    .flatMap { it.findRLIs() }
    .forEach { it.status() }

  File("report.md").writeText(report.toString())

}

const val latestVersion = "v2021.3.1"
const val rliRegex =
  """\.\. (?:rli)|(?:remoteliteralinclude):: https:\/\/raw\.githubusercontent\.com\/wpilibsuite\/allwpilib\/(v\d{4}\.\d\.\d(?:-beta-\d)?)\/([\/\w.]+)\r?\n.*\r?\n[ ]{9}:lines: (\d*-\d*)"""
const val baseURL = "https://raw.githubusercontent.com/wpilibsuite/allwpilib"

fun File.findRLIs(): Sequence<LocatedRli> {
  val text = readText()
  return Regex(rliRegex)
    .findAll(text)
    .map { buildRli(it, name, text) }
    .filterNotNull()
}

fun buildRli(result: MatchResult, file: String, text: String): LocatedRli? {
  val line = text.indexToLineNumber(result.range.first)
  val (version, path, lines) = result.destructured
  if (version == latestVersion) {
    report.upToDate(url = path, path = file, line)
    return null
  }
  return LocatedRli(Location(file, line), Rli("$baseURL/$version/$path", lines.toLineRange()))
}
