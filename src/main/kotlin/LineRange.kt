package io.starlight.inspector

/**
 * Represents the line range of an RLI, fetched from the remote.
 */
sealed class LineRange {
    abstract operator fun contains(e: Int): Boolean
    abstract override fun toString(): String

    /**
     * An iterator over the RLId lines
     */
    abstract fun iterator() : IntIterator

    /**
     * Both edges are specified, and are different
     */
    protected class RangedLineRange(internal val range: IntRange) : LineRange() {
        override fun contains(e: Int): Boolean = e in range
        override fun toString(): String = "L${range.first}-L${range.last}"
        override fun equals(other: Any?): Boolean = other is RangedLineRange && this.range == other.range
        override fun hashCode(): Int = range.hashCode()
        override fun iterator(): IntIterator = range.iterator()
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
        override fun equals(other: Any?): Boolean = other is SingletonLineRange && this.element == other.element
        override fun hashCode(): Int = Integer.hashCode(element)
        override fun iterator(): IntIterator {
            return object : IntIterator() {
                var hasNext = true

                /**
                 * Returns `true` if the iteration has more elements.
                 */
                override fun hasNext(): Boolean = hasNext

                /** Returns the next value in the sequence without boxing. */
                override fun nextInt(): Int {
                    if (hasNext) {
                        hasNext = false
                        return element
                    } else {
                        throw NoSuchElementException("Singleton iterator called more than once")
                    }
                }

            }
        }
    }

    /**
     * Only the lower bound is specified, continuing until the end of the remote file.
     *
     * Indicated by:
     * ```rst
     *      :lines: 20-
     * ```
     */
    protected class LowerBoundedLineRange(val start: Int) : LineRange() {
        override fun contains(e: Int): Boolean = start <= e
        override fun toString(): String = "L$start-EOF"
        override fun equals(other: Any?): Boolean = other is LowerBoundedLineRange && this.start == other.start
        override fun hashCode(): Int = Integer.hashCode(start)
        override fun iterator(): IntIterator {
            return object : IntIterator() {
                var next = start
                override fun hasNext(): Boolean = true
                override fun nextInt(): Int = next++
            }
        }
    }

    protected class MultiLineRange(private val ranges: List<IntRange>) : LineRange() {
        private val flattened: List<Int> by lazy { ranges.flatten() }
        override fun contains(e: Int): Boolean = flattened.contains(e)

        override fun toString(): String = ranges.joinToString(separator = ",") { "L${it.first}-L${it.last}" }

        override fun iterator(): IntIterator {
            return object : IntIterator() {
                val iter = flattened.iterator()
                override fun hasNext(): Boolean = iter.hasNext()

                override fun nextInt(): Int = iter.next()
            }
        }

    }

    companion object {
        operator fun invoke(s: String): LineRange {
            if (!s.contains(",")) {
                val parts = s.split('-')
                if (parts.size == 1 || parts[1].isBlank()) {
                    return LowerBoundedLineRange(parts[0].toInt())
                }
                val start = parts[0].toInt()
                val end = parts[1].toInt()
                return if (start == end) SingletonLineRange(start) else RangedLineRange(start..end)
            } else {
                s.split(",").map { untrimmed ->
                    val it = untrimmed.trim()
                    val parts = it.split('-')
                    check(parts.size == 2) { "error in parsing numbers: `$it`" }
                    val start = parts[0].toInt()
                    val end = parts[1].toInt()
                    start..end
                }.let { return MultiLineRange(it) }
            }
        }
    }
}

operator fun IntIterator.invoke(): String {
    return nextInt().toString()
}
