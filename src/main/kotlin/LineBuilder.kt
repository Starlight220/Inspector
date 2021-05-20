package io.starlight.rli

import java.util.concurrent.atomic.AtomicInteger


fun buildDiffBlock(diff: Diff): String {
    val oldLine = AtomicInteger(diff.old.lines.first)
    val newLine = AtomicInteger(diff.new.lines.first)

    fun processLine(line: String): String? = when (line[0]) {
        '+' -> line.replaceFirst("+", "+\t \t${newLine.getAndIncrement()}\t")
        '-' -> line.replaceFirst("-", "-\t${oldLine.getAndIncrement()}\t \t")
        ' ' -> line.replaceFirst(" ", " \t${oldLine.getAndIncrement()}\t${newLine.getAndIncrement()}\t")
        '\\' -> null
        else -> error("Git Diff output line should not start with something other than `+`, `-`, ` `, or `\\`.\nGot:$line")
    }

    val block = Runtime.getRuntime()
        .exec(
            Constants.run {
                "$diffCommand ${oldTmpFile.canonicalPath} ${newTmpFile.canonicalPath}"
            }
        )
        .inputStream
        .bufferedReader()
        .readText()
        .split(Constants.diffSplitRegex)
        .asSequence()
        .drop(1) // drop header
        .flatMap { it.split(Regex("\r?\n")) }
        .filter { it.isNotEmpty() }
        .map { processLine(it) }
        .filterNot { it.isNullOrEmpty() }
        .joinToString("\n")

    return block
}


