package io.starlight.rli

import java.net.URL

/** from string index to line number in same string */
fun String.indexToLineNumber(index: Int): Int {
  var line = 0
  repeat(index) { i -> if (this[i] == '\n') line++ }
  return line
}

/** rli lines section */
fun String.toLineRange(): IntRange {
  val parts = this.split('-')
  return when (parts.size) {
    1 -> parts[0].toInt().run { this..this }
    2 -> parts[0].toInt()..parts[1].toInt()
    else -> throw NumberFormatException("error in parsing numbers: `$this`")
  }
}

data class Location(val file: RliFile, val indexRange: IntRange) {
  val line
    get() = file.use { it.indexToLineNumber(indexRange.first) }
}

data class Rli(val url: String, val lines: IntRange) {
  val response: String by lazy {
    URL(Constants.baseUrl + url)
        .readText()
        .splitToSequence('\n')
        .filterIndexed { i, _ -> i in lines }
        .joinToString(separator = "\n")
  }
}

typealias LocatedRli = Pair<Location, Rli>

@JvmInline value

class Diff(private val diff: Pair<Rli, Rli>) {
  val old
    get() = diff.first
  val new
    get() = diff.second

  operator fun component1() = old
  operator fun component2() = new
}

infix fun Rli.diff(other: Rli): Diff? =
    if (this.response != other.response) Diff(this to other) else null
