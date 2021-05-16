package io.starlight.rli

fun main() {

    Constants.root.walkDir { extension == "rst" }.flatMap { it.findRlis() }.forEach { it.status() }

    Report()
}

fun buildRli(result: MatchResult, file: RliFile): LocatedRli? {
    require(result.value matches Constants.rliRegex)
    val idxRange = result.groups[1]?.range ?: IntRange.EMPTY
    val (version, path, _lines) = result.destructured
    val lines = _lines.toLineRange()
    val loc = Location(file, idxRange)
    if (version == Constants.latestVersion) {
        Report.upToDate(url = path, lines, loc)
        return null
    }
    return loc to Rli("$version/$path", lines)
}
