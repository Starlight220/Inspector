package io.starlight.inspector

import java.net.URL

data class Location(val file: RliFile, val indexRange: IntRange) {
    val line by lazy {
        file.use {
            var counter = 1
            repeat(indexRange.first) { i -> if (it[i] == '\n') counter++ }
            counter
        }
    }
}

data class Rli(val version: String, val url: String, val lines: LineRange) {
    val versionedUrl: String = """$version/$url"""
    val fullUrl: String = """${Constants.baseUrl}$version/$url"""
    val response: String by lazy {
        URL(fullUrl)
            .readText()
            .lineSequence()
            .filterIndexed { i, _ -> i + 1 in lines }
            .joinToString(separator = "\n")
    }
    val withLatest by lazy { copy(version = Constants.latestVersion) }
}

typealias LocatedRli = Pair<Location, Rli>
