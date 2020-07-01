@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*
 * Utilities for Android's Intent
 * Author: Davide Farella
 */

/** [contains] operator for [Intent] extras */
operator fun Intent.contains(key: String) = extras.contains(key)

/** @return nullable [T] from receiver [Intent] extras */
inline operator fun <reified T> Intent.get(key: String) = extras?.getAny<T>(key)

/** Put [value] [T] into receiver [Intent] extras */
inline operator fun <reified T> Intent.set(key: String, value: T) {
    extras?.set(key, value)
}

/**
 * @return [ReadWriteProperty] delegate for property of type [T] into receiver [FragmentActivity]
 * for access ( read / write ) [Intent] extras
 */
inline fun <reified T: Any?> FragmentActivity.intent(
    key: String,
    default: T? = null
) = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val value = intent[key] ?: default
        check(null is T || value != null) {
            "Property '${property.name}' is declared as not nullable, " +
                    "but value for '$key' is 'null'. " +
                    "Declare a non-null default value or set the property as nullable"
        }

        return value as T
    }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        intent[key] = value
    }
}
