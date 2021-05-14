package io.starlight.rli

import java.io.File
import java.lang.IllegalStateException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// for local testing ONLY
private const val envFilePath = ".inspect_rli.json"

@Serializable
private data class Env(
    val root: String,
    val versionScheme: String,
    val baseUrl: String,
    val latestVersion: String
) {
  operator fun get(field: String): String? =
      when (field) {
        "root" -> root
        "versionScheme" -> versionScheme
        "baseUrl" -> baseUrl
        "latestVersion" -> latestVersion
        else -> null
      }
}

object Input {
  operator fun invoke() = InputDelegate { it }
  operator fun <T> invoke(mapper: (String) -> T) = InputDelegate(mapper)
}

class InputDelegate<T>(private val mapper: (String) -> T) : ReadOnlyProperty<Any, T> {
  override fun getValue(thisRef: Any, property: KProperty<*>): T {
    val value = getProperty(property.name)
    if (value.isNullOrBlank()) {
      throw IllegalStateException(
          """
                Input property `${property.name}` is undeclared or empty.
                Please specify a value for it in your workflow YAML.
            """.trimIndent())
    }
    return mapper(value)
  }
}

private val env: Env? = File(envFilePath).run {if (exists()) Json.decodeFromString(readText()) else null }

private fun getProperty(name: String): String? =
    if (env != null) {
      env[name]
    } else {
      System.getenv("INPUT_${name.uppercase()}")
    }
