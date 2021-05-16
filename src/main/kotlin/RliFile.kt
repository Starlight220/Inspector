package io.starlight.rli

import java.io.File

class RliFile(private val file: File) {
    private var content: String = file.readText()
    private var offset: Int = 0

    fun replaceRange(range: IntRange, replacement: String) {
        val offsetRange = IntRange(range.first + offset, range.last + offset)
        val newContent = content.replaceRange(offsetRange, replacement)

        offset += newContent.length - content.length
        content = newContent
        file.writeText(newContent)
    }

    fun <R> use(const: (String) -> R) = const(content)

    fun findRlis(): Sequence<LocatedRli> =
        Constants.rliRegex.findAll(content).map { buildRli(it, this) }.filterNotNull()

    override fun toString(): String = file.toRelativeString(Constants.root)
}

fun File.walkDir(predicate: File.() -> Boolean): Set<RliFile> {
    return walk().filter(predicate).map(::RliFile).toSet()
}
