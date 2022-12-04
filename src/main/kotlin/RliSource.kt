package io.starlight.inspector

import kotlinx.serialization.Serializable

private const val RLI_HEADER_REGEX = """\.\. (?:rli|remoteliteralinclude)::"""
private const val RLI_LINES_REGEX = """\r?\n[ ]*:lines: ((?:\d*-\d*(?:,[ ]?)?)+)"""

class RliContext(val context: RliSet)

inline fun <T> RliSet.asContext(action: context(RliContext) () -> T) = with(RliContext(this), action)
inline fun <T> Sequence<RliSet>.asEachContext(crossinline action: context(RliContext) () -> Sequence<T>): Sequence<T> = this.flatMap { it.asContext(action) }

@Serializable
data class RliSet(val baseUrl: String, val versionScheme: String, val latestVersion: String) {
    val rliRegex by lazy {
        val r =
            """$RLI_HEADER_REGEX ${Regex.fromLiteral(baseUrl).pattern}($versionScheme)/([/\w.]+)\r?\n.*$RLI_LINES_REGEX""".toRegex()
        println(r)
        r
    }
}
