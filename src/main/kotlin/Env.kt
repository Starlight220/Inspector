package io.starlight.inspector

import com.github.starlight220.actions.Input
import com.github.starlight220.actions.getInputOrThrow
import java.io.File
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class EnvContext(val context: EnvSet)

inline fun <T> EnvSet.asContext(
    action:
        context(EnvContext)
        () -> T
) = with(EnvContext(this), action)

/** Context struct for the entire run. */
@Serializable
data class EnvSet(val sources: List<RliSet>, val ignoredFiles: List<String>) {
    companion object {
        fun fromInputs(): EnvSet {
            val ignoredFiles: List<String> = Json.decodeFromString(getInputOrThrow("ignoredFiles"))

            val baseUrl =
                getInputOrThrow("baseUrl").let { str -> if (str.endsWith('/')) str else "$str/" }
            val latestVersion = getInputOrThrow("latestVersion")
            val versionScheme = getInputOrThrow("versionScheme")

            return EnvSet(
                sources = listOf(RliSet(baseUrl, versionScheme, latestVersion)), ignoredFiles)
        }
    }

    /** Search root for RLI files */
    val root by Input("root", mapper = ::File)
}
