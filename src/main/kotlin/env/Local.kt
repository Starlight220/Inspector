package io.starlight.rli.env

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Whether the program is running on a local machine or on a GitHub Actions runner. */
val IS_LOCAL = System.getenv("CI")?.toBooleanStrictOrNull()?.not() ?: true

// for local testing ONLY
private const val envFilePath = "inputs.inspect_rli.json"
private const val outputFilePath = "outputs.inspect_rli.json"

@Serializable
internal data class Env(
    @JvmField val root: String,
    @JvmField val versionScheme: String,
    @JvmField val baseUrl: String,
    @JvmField val latestVersion: String
) {
    operator fun get(field: String): String? =
        this::class.java.fields.first { it.name == field }.get(this) as? String

    operator fun set(name: String, value: String) {
        val file = File(outputFilePath)
        val list: MutableMap<String, String> =
            if (file.exists()) {
                Json.decodeFromString(file.readText())
            } else {
                mutableMapOf()
            }
        list += name to value
        file.writeText(Json.encodeToString(list))
    }
}

internal val env: Env by lazy { Json.decodeFromString(File(envFilePath).readText()) }
