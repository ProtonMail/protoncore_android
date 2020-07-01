@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import android.os.Bundle
import androidx.core.os.bundleOf

/*
 * Utilities for Android's Bundle
 * Author: Davide Farella
 */

/**
 * [contains] operator for [Bundle]. Return `false` if [Bundle] is `null`
 * @see Bundle.containsKey
 */
operator fun Bundle?.contains(key: String) = this?.let { containsKey(key) } ?: false

/**
 * @return nullable [T] from receiver [Bundle]
 * It supports Kotlin Serializable values
 */
inline fun <reified T> Bundle.getAny(key: String): T? {
    // Try to get a primitive or Java Serializable
    val p = get(key) as? T
    if (p == null) {
        try {
            return getString(key)!!.deserialize()!!
        } catch (t: Throwable) {
            // noop
        }
    }
    return p
}

/**
 * Put [value] [T] into receiver [Bundle
 * It supports Kotlin Serializable values
 */
inline operator fun <reified T> Bundle.set(key: String, value: T) {
    val bundle = try {
        bundleOf(key to value)
    } catch (e: IllegalArgumentException) {
        // If values is `null` it would be stored as String, so can't be null here
        bundleOf(key to value!!.serialize())
    }
    putAll(bundle)
}
