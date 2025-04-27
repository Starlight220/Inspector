package io.starlight.inspector

import com.github.starlight220.actions.debug
import java.io.FileNotFoundException
import java.net.URL

/**
 * Caches remote file requests.
 *
 * This should add a significant performance improvement as it caches web requests to the same
 * remote file.
 */
object RemoteCache {
    private val map: MutableMap<String, String> = mutableMapOf()

    operator fun get(url: String): String =
        map.getOrPut(url) {
            try {
                URL(url).readText()
            } catch (e: FileNotFoundException) {
                debug("Remote resource not found for URL `${url}`: ${e.message}")
                ""
            }
        }

    operator fun get(url: String, lines: LineRange): String =
        this[url]
            .lineSequence()
            .filterIndexed { i, _ -> i + 1 in lines }
            .joinToString(separator = "\n")
}
