package io.starlight.inspector

import java.io.File

/** Represents a file that contains RLIs. */
class RliFile(private val file: File) {
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

    /** Returns a [Sequence] of RLIs in this file */
    fun findRlis(): Sequence<LocatedRli> =
        Constants.rliRegex.findAll(content).map { this.buildRli(it) }.filterNotNull()

    /** Dissects the [result] struct from the Regex match */
    private fun buildRli(result: MatchResult): LocatedRli? {
        // sanity check to make sure that there isn't any funny business going on
        require(result.value matches Constants.rliRegex)
        // index of RLI URL in file
        val idxRange = result.groups[1]?.range ?: IntRange.EMPTY
        // (RLI version, RLI URL, RLI line range)
        val (version, url, _lines) = result.destructured
        val lines = LineRange(_lines)
        val loc = Location(this, idxRange)
        if (version == Constants.latestVersion) {
            // if the RLI is up-to-date, no need to waste time on it
            Report.upToDate(url, lines, loc)
            return null
        }
        return loc to Rli("$version/$url", lines)
    }

    override fun toString(): String = file.toRelativeString(Constants.root)
}

/**
 * Return a [Sequence] of all files that match the given [predicate].
 *
 * Searches recursively for files.
 * @see walk
 */
fun File.walkDir(predicate: File.() -> Boolean): Sequence<RliFile> =
    walk().filter(predicate).map(::RliFile)
