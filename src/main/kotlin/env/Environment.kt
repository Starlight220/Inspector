package io.starlight.rli.env

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val IDENTITY: (String) -> String = { it }

/**
 * Property Delegate type for Action Inputs.
 *
 * @param name the property name. Defaults to the property name.
 * @param mapper a function that converts the object to a String. Defaults to [toString].
 *
 * If the property name is identical to the input name, and the property is a String; use the
 * Companion object for a cleaner usage syntax.
 */
open class Input<T>(private val name: String? = null, private val mapper: (String) -> T) :
    ReadOnlyProperty<Any, T> {
    companion object : Input<String>(null, IDENTITY) {
        /** Get an [Input] String property delegate with the given name. */
        operator fun invoke(name: String) = Input(name, IDENTITY)
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

/**
 * Property Delegate type for Action Outputs.
 *
 * @param name the property name. Defaults to the property name.
 * @param mapper a function that converts the object to a String. Defaults to [toString].
 *
 * If the property name is identical to the input name, and the property is a String; use the
 * Companion object for a cleaner usage syntax.
 */
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

/**
 * Get an input for this action.
 *
 * This is a low-level function. Use the [Input] delegate type instead.
 */
fun getInput(name: String): String? =
    if (IS_LOCAL) {
        env[name]
    } else {
        System.getenv("INPUT_${name.uppercase()}")
    }

/**
 * Set an output for this action.
 *
 * This is a low-level function. Use the [Output] delegate type instead.
 */
fun setOutput(name: String, value: String): Unit {
    if (IS_LOCAL) env[name] = value
    else println("::set-output name=${name.escaped()}::${value.escaped()}")
}

/**
 * GitHub Actions doesn't like multiline strings, so this escapes the newlines into something that
 * GH Actions accepts.
 *
 * Source: https://github.community/t/set-output-truncates-multiline-strings/16852/5
 */
fun String.escaped() = replace("%", "%25").replace("\n", "%0A").replace("\r", "%0D")
