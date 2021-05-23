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

data class Rli(val url: String, val lines: LineRange) {
    val response: String by lazy {
        URL(Constants.baseUrl + url)
            .readText()
            .lineSequence()
            .filterIndexed { i, _ -> i + 1 in lines }
            //            .map {
            //                it.ifBlank {
            //                    val s = it.replace(" ", "%")
            //                    System.err.println("$url:$lines > $s")
            //                    s
            //                }
            //            }
            .joinToString(separator = "\n")
    }
}

typealias LocatedRli = Pair<Location, Rli>
