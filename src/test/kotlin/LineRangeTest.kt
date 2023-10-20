package io.starlight.inspector


import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals


internal class LineRangeTest {
    private infix fun String.shouldBe(expected: Iterable<Int>): () -> Unit = {
        val range = LineRange(this)
        val actual = range.iterator().asSequence().toList()
        assertEquals(expected.toList(), actual)
    }

    /**
     * Infinite ranges can't be tested for equality,
     * so compare string representation.
     */
    private infix fun String.shouldBeRangeStartingFrom(expected: Int): () -> Unit = {
        val range = LineRange(this)
        val actual = range.toString()
        assertEquals("L$expected-EOF", actual)
    }

    @TestFactory
    fun single() = listOf(
        "11" to listOf(11),
        "0" to listOf(0),
        "999" to listOf(999),
    ).map {(input, expected) ->
        DynamicTest.dynamicTest(input, input shouldBe expected)
    }

    @TestFactory
    fun range() = listOf(
        "1-2" to 1..2,
        "44-61" to 44..61,
        "12-900" to 12..900,
        "17-71" to 17..71,
        "77-77" to listOf(77),
    ).map {(input, expected) ->
        DynamicTest.dynamicTest(input, input shouldBe expected)
    }

    @TestFactory
    fun lowerBounded() =
        listOf(
            "1-" to 1,
            "0-" to 0,
            "5-" to 5,
        ).map { (input, expected) ->
            DynamicTest.dynamicTest(input, input shouldBeRangeStartingFrom expected)
        }


    @TestFactory
    fun multiple() = listOf(
        "1,2,3" to listOf(1, 2, 3),
        "1, 2, 3" to listOf(1, 2, 3),
        "1-5, 12-34" to (1..5) + (12..34),
        "67-68,76-81" to listOf(67, 68) + (76..81),
        "7-8,16-18,20,25-31" to listOf(7, 8) + (16..18) + listOf(20) + (25..31),
        "28-30, 71-72, 79-82" to (28..30) + (71..72) + (79..82),
        "5-20,23-56,58-61,63-64,69-76" to (5..20) + (23..56) + (58..61) + listOf(63, 64) + (69..76),
    ).map { (input, expected) ->
        DynamicTest.dynamicTest(input, input shouldBe expected)
    }
}
