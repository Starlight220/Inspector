package io.starlight.inspector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/*
Test strings taken from https://github.com/wpilibsuite/frc-docs
 */

internal class RegexTest {

    private fun String.shouldBe(
        expectedVersion: String,
        expectedUrl: String,
        expectedLines: String
    ) {
        val declaration = this
        val ctx =
            RliSet(
                baseUrl = "https://raw.githubusercontent.com/wpilibsuite/allwpilib/",
                versionScheme = "v\\d{4}\\.\\d\\.\\d(?:-(?:alpha|beta)-\\d)?|[0-9a-f]{40}",
                latestVersion = "v2025.3.2")

        val match = ctx.rliRegex.find(declaration) ?: fail("Didn't find a match!")
        val (actualVersion, actualUrl, actualLines) = match.destructured
        assertEquals(expectedVersion, actualVersion)
        assertEquals(expectedUrl, actualUrl)
        assertEquals(expectedLines, actualLines)
    }

    @Test
    fun `basic open-ended range`() =
        """
         .. remoteliteralinclude:: https://raw.githubusercontent.com/wpilibsuite/allwpilib/v2027.0.0-alpha-1/wpilibcExamples/src/main/cpp/examples/ElevatorProfiledPID/cpp/Robot.cpp
            :language: c++
            :lines: 5-
            :lineno-match:
        """
            .trimIndent()
            .shouldBe(
                "v2027.0.0-alpha-1",
                "wpilibcExamples/src/main/cpp/examples/ElevatorProfiledPID/cpp/Robot.cpp",
                "5-")

    @Test
    fun `basic closed range`() =
        """
        .. remoteliteralinclude:: https://raw.githubusercontent.com/wpilibsuite/allwpilib/v2027.0.0-alpha-1/wpilibjExamples/src/main/java/edu/wpi/first/wpilibj/examples/statespaceflywheelsysid/Robot.java
           :language: java
           :lines: 32-46
           :lineno-match:
        """
            .trimIndent()
            .shouldBe(
                "v2027.0.0-alpha-1",
                "wpilibjExamples/src/main/java/edu/wpi/first/wpilibj/examples/statespaceflywheelsysid/Robot.java",
                "32-46")

    @Test
    fun `multiple closed ranges`() =
        """
         .. rli:: https://raw.githubusercontent.com/wpilibsuite/allwpilib/v2027.0.0-alpha-1/wpilibjExamples/src/main/java/edu/wpi/first/wpilibj/examples/armsimulation/subsystems/Arm.java
            :language: java
            :lines: 27-29, 73-74, 80-83
        """
            .trimIndent()
            .shouldBe(
                "v2027.0.0-alpha-1",
                "wpilibjExamples/src/main/java/edu/wpi/first/wpilibj/examples/armsimulation/subsystems/Arm.java",
                "27-29, 73-74, 80-83")

    // https://github.com/Starlight220/Inspector/issues/16
    @Test
    fun `range after single line`() =
        """
       .. rli:: https://raw.githubusercontent.com/wpilibsuite/allwpilib/v2025.0.0-alpha-2/wpilibcExamples/src/main/cpp/examples/QuickVision/cpp/Robot.cpp
          :language: c++
          :lines: 7-8, 16-18, 20, 25-31
        """
            .trimIndent()
            .shouldBe(
                "v2025.0.0-alpha-2",
                "wpilibcExamples/src/main/cpp/examples/QuickVision/cpp/Robot.cpp",
                "7-8, 16-18, 20, 25-31")
}
