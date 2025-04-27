package io.starlight.inspector

/** The RLI's location. */
data class Location(val file: RliFile, val indexRange: IntRange) {
    fun compareTo(other: Location): Int {
        return this.file.compareTo(other.file).takeUnless { it == 0 }
            ?: this.indexRange.first.compareTo(other.indexRange.first)
    }

    override fun equals(other: Any?): Boolean = other is Location && this.compareTo(other) == 0

    override fun hashCode(): Int = 31 * file.hashCode() + indexRange.hashCode()

    override fun toString(): String = "$file:$line"

    val line by lazy {
        file.use {
            var counter = 1
            repeat(indexRange.first) { i -> if (it[i] == '\n') counter++ }
            counter
        }
    }
}

/**
 * RLI data.
 *
 * @param version the version of this RLI's content
 * @param url the RLI source
 * @param lines the RLId lines from the source
 */
// context(RliContext) //
// https://youtrack.jetbrains.com/issue/KT-55261/Data-class-with-context-receiver-fails
data class Rli(
    val version: String,
    val url: String,
    val lines: LineRange,
    private val context: RliSet
) {
    val fullUrl: String = """${context.baseUrl}$version/$url"""
    val response: String by lazy { RemoteCache[fullUrl, lines] }
    val withLatest by lazy { copy(version = context.latestVersion) }
}

data class LocatedRli(val location: Location, val rli: Rli)
