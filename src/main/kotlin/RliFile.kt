import java.io.File

class RliFile(private val file: File) {
  constructor(path: String) : this(File(path))

  private var content: String = file.readText()
  private var offset: Int = 0

  fun replaceRange(range: IntRange, replacement: String) {
    val offsetRange = range.move(offset)
    val newContent = content.replaceRange(offsetRange, replacement)

    offset += newContent.length - content.length
    content = newContent
    file.writeText(newContent)
  }

  fun <R> use(const: (String) -> R) = const(content)

  fun findRlis(): Sequence<LocatedRli> {
    return Regex(rliRegex)
      .findAll(content)
      .map { buildRli(it, this) }
      .filterNotNull()
  }

  override fun toString(): String = file.name
}

fun IntRange.move(offset: Int): IntRange = IntRange(start + offset, endInclusive + offset)

fun File.walkDir(predicate: File.() -> Boolean): Set<RliFile> {
  return walk().filter(predicate).map(::RliFile).toSet()
}