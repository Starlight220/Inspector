package io.starlight.inspector

/**
 * Represents the line range of an RLI, fetched from the remote.
 */
sealed class LineRange {
    abstract operator fun contains(e: Int): Boolean
    abstract override fun toString(): String
    abstract val start: Int

    /**
     * Both edges are specified, and are different
     */
    protected class RangedLineRange(private val range: IntRange) : LineRange() {
        override fun contains(e: Int): Boolean = e in range
        override fun toString(): String = "L${range.first}-L${range.last}"
        override val start: Int by range::first
    }

    /**
     * The RLI only includes one line, indicated by
     * ```rst
     *      :lines: 10-10
     * ```
     */
    protected class SingletonLineRange(private val element: Int) : LineRange() {
        override fun contains(e: Int): Boolean = element == e
        override fun toString(): String = "L$element"
        override val start: Int by ::element
    }

    /**
     * Only the lower bound is specified, continuing until the end of the remote file.
     *
     * Indicated by:
     * ```rst
     *      :lines: 20-
     * ```
     */
    protected class LowerBoundedLineRange(override val start: Int) : LineRange() {
        override fun contains(e: Int): Boolean = start <= e
        override fun toString(): String = "L$start-EOF"
    }

    companion object {
        operator fun invoke(s: String): LineRange =
            with(s) {
                val parts = this.split('-')
                return when (parts.size) {
                    1 -> LowerBoundedLineRange(parts[0].toInt())
                    2 -> {
                        val start = parts[0].toInt()
                        val end = parts[1].toInt()
                        if (start == end) SingletonLineRange(start) else RangedLineRange(start..end)
                    }
                    else -> throw NumberFormatException("error in parsing numbers: `$this`")
                }
            }
    }
}
