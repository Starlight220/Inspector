package io.starlight.inspector

import java.io.File

data class Diff(val old: Rli, val new: Rli)

private const val GIT_DIFF_CMD = "git diff --no-index --no-prefix -U200 --"
private val diffSplitRegex: Regex = """@@ -?\d+,?\d* [+]?\d+,?\d* @@""".toRegex()

fun Diff.buildDiffBlock(): String {
    val oldFile =
        File.createTempFile("old", ".tmp").apply {
            deleteOnExit()
            writeText(old.response + "\n")
        }
    val newFile =
        File.createTempFile("new", ".tmp").apply {
            deleteOnExit()
            writeText(new.response + "\n")
        }

    val oldLine = old.lines.iterator()
    val newLine = new.lines.iterator()

    return Runtime.getRuntime()
        .exec("$GIT_DIFF_CMD ${oldFile.canonicalPath} ${newFile.canonicalPath}")
        .inputStream
        .bufferedReader()
        .readText()
        .split(diffSplitRegex)
        .asSequence()
        .drop(1) // drop header
        .flatMap { it.lineSequence() }
        .filter { it.isNotEmpty() }
        .map {
            when (it[0]) {
                '+' -> it.replaceFirst("+", "+\t \t${newLine()}\t")
                '-' -> it.replaceFirst("-", "-\t${oldLine()}\t \t")
                ' ' -> it.replaceFirst(" ", " \t${oldLine()}\t${newLine()}\t")
                '\\' -> null
                else ->
                    error(
                        "Git Diff output line should not start with something other than `+`, `-`, ` `, or `\\`.\nGot:$it")
            }
        }
        .filterNot { it.isNullOrEmpty() }
        .joinToString("\n")
}
