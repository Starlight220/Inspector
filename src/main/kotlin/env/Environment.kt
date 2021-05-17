package io.starlight.rli.env

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val IDENTITY: (String) -> String = { it }

open class Input<T>(private val name: String? = null, private val mapper: (String) -> T) :
    ReadOnlyProperty<Any, T> {
    companion object : Input<String>(null, IDENTITY) {
        operator fun <T> invoke(name: String) = Input(name, IDENTITY)
    }

    @Throws(IllegalStateException::class)
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val key = name ?: property.name
        val value = getInput(key)
        if (value.isNullOrBlank()) {
            throw IllegalStateException(
                """
                Input property `${key}` is undeclared or empty.
                Please specify a value for it in your workflow YAML.
            """.trimIndent()
            )
        }
        return mapper(value)
    }
}

open class Output<T>(
    private val name: String? = null,
    private val mapper: (T) -> String = { it.toString() }
) : ReadWriteProperty<Any, T> {
    private var value: T? = null
    companion object : Output<String>() {
        private val map = mutableMapOf<String, String>()

        @Throws(UnsupportedOperationException::class)
        override fun getValue(thisRef: Any, property: KProperty<*>): String =
            map[property.name]
                ?: throw UnsupportedOperationException(
                    "Reading from an output isn't supported, especially if it's unset."
                )

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            val key = property.name
            map[key] = value
            setOutput(key, value)
        }
    }

    @Throws(UnsupportedOperationException::class)
    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        value
            ?: throw UnsupportedOperationException(
                "Reading from an output isn't supported, especially if it's unset."
            )

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
        setOutput(name ?: property.name, mapper(value))
    }
}

private fun getInput(name: String): String? =
    if (IS_LOCAL) {
        env[name]
    } else {
        System.getenv("INPUT_${name.uppercase()}")
    }

private fun setOutput(name: String, value: String): Unit {
    if (IS_LOCAL) env[name] = value
    else {
        val cmd = "echo \"::set-output name=${name.escaped()}::${value.escaped()}\""
        System.err.println(cmd)
        Runtime.getRuntime().exec(cmd)
    }
}

/**
 * GitHub Actions doesn't like multiline strings, so this escapes the newlines into something that
 * GH Actions accepts.
 *
 * Source: https://github.community/t/set-output-truncates-multiline-strings/16852/5
 */
fun String.escaped() =
    replace('%', 0x25.toChar()).replace('\n', 0x0A.toChar()).replace('\r', 0x0D.toChar())
