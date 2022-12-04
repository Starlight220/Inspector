package io.starlight.inspector

import java.io.File

/** Represents a file that contains RLIs. */
context(EnvContext)
class RliFile(private val file: File) : Comparable<RliFile> {
    private var content: String = file.readText()
    private var offset: Int = 0

    /**
     * Replace the given index [range] with [replacement].
     *
     * Accounts for multiple shifts of the same file with replacements of differing lengths.
     */
    fun replaceRange(range: IntRange, replacement: String) {
        val offsetRange = IntRange(range.first + offset, range.last + offset)
        val newContent = content.replaceRange(offsetRange, replacement)

        offset += newContent.length - content.length
        content = newContent
        file.writeText(newContent)
    }

    /**
     * Allows for immutable manipulation of the file's content.
     *
     * Since [String]s are immutable, any changes through this function will **not** be reflected in
     * the file.
     */
    fun <R> use(const: (String) -> R) = const(content)

    val isIgnored by lazy { context.ignoredFiles.any {
        file.toRelativeString(context.root).replace('\\', '/').startsWith(it)
    } }

    /** Returns a [Sequence] of RLIs in this file */
    context(RliContext)
    fun findRlis(): Sequence<LocatedRli> {
        val matches = context.rliRegex.findAll(content)
        if (matches.count() < 1) return emptySequence()

        return matches.map { buildRli(it) }
    }

    /** Dissects the [result] struct from the Regex match */
    context(RliContext)
    private fun buildRli(result: MatchResult): LocatedRli {
        // sanity check to make sure that there isn't any funny business going on
        require(result.value matches context.rliRegex)
        // index of RLI URL in file
        val idxRange = result.groups[1]?.range ?: error("Empty regex match range!\t$result")
        // (RLI version, RLI URL, RLI line range)
        val (version, url, _lines) = result.destructured
        val lines = LineRange(_lines)
        return LocatedRli(Location(this, idxRange), Rli(version, url, lines, context))
    }

    override fun toString(): String = file.toRelativeString(context.root).replace('\\', '/')
    override operator fun compareTo(other: RliFile): Int = this.file.compareTo(other.file)
    override fun equals(other: Any?): Boolean =
        if (other is RliFile) {
            this.file == other.file
        } else false
}

/**
 * Return a [Sequence] of all files that match the given [predicate].
 *
 * Searches recursively for files.
 * @see walk
 */
fun File.walkDir(predicate: (File) -> Boolean): Sequence<File> =
    walk().filter(predicate)
