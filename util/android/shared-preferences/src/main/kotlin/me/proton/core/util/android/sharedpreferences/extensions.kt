/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("unused")

package me.proton.core.util.android.sharedpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import me.proton.core.util.android.sharedpreferences.PrefType.BOOLEAN
import me.proton.core.util.android.sharedpreferences.PrefType.FLOAT
import me.proton.core.util.android.sharedpreferences.PrefType.INT
import me.proton.core.util.android.sharedpreferences.PrefType.LONG
import me.proton.core.util.android.sharedpreferences.PrefType.SERIALIZABLE
import me.proton.core.util.android.sharedpreferences.PrefType.STRING
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.util.kotlin.NeedSerializable
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.deserializeList
import me.proton.core.util.kotlin.deserializeMap
import me.proton.core.util.kotlin.filterNullValues
import me.proton.core.util.kotlin.safe
import me.proton.core.util.kotlin.serialize
import kotlin.reflect.KClass

/*
 * Extensions for `SharedPreferences`
 * Author: Davide Farella
 */

/** @return `true` if receiver [SharedPreferences] has no values */
fun SharedPreferences.isEmpty() = this.all.isEmpty()

/** Clear all values in receiver [SharedPreferences], except [excludedKeys] */
fun SharedPreferences.clearAll(vararg excludedKeys: String) {
    val backupBools = excludedKeys.associate { it to get<Boolean?>(it) }.filterNullValues()
    val backupInts = excludedKeys.associate { it to get<Int?>(it) }.filterNullValues()
    val backupLongs = excludedKeys.associate { it to get<Long?>(it) }.filterNullValues()
    val backupStrings = excludedKeys.associate { it to get<String?>(it) }.filterNullValues()

    edit {
        clear()
        val allBackups = backupBools + backupInts + backupLongs + backupStrings
        allBackups.forEach { (k, v) -> put(k, v) }
    }
}

/** Clear only the values for given [keys] in receiver [SharedPreferences] */
fun SharedPreferences.clearOnly(vararg keys: String) {
    edit { keys.forEach { remove(it) } }
}

// region get or null
/** @return [Boolean] or `null` */
fun SharedPreferences.getBoolean(key: String) = nullableGet(key) { getBoolean(key, false) }

/** @return [Float] or `null` */
fun SharedPreferences.getFloat(key: String) = nullableGet(key) { getFloat(key, 0f) }

/** @return [Int] or `null` */
fun SharedPreferences.getInt(key: String) = nullableGet(key) { getInt(key, 0) }

/** @return [Long] or `null` */
fun SharedPreferences.getLong(key: String) = nullableGet(key) { getLong(key, 0) }

/** @return [String] or `null` */
fun SharedPreferences.getString(key: String) =
    nullableGet(key) { getString(key, EMPTY_STRING) }

/** @return [List] of [T] or `null` */
@NeedSerializable
inline fun <reified T : Any> SharedPreferences.getList(key: String) =
    nullableGet(key) { getString(key, EMPTY_STRING) }?.deserializeList<T>()

/** @return [Map] of [K] and [V] or `null` */
@NeedSerializable
inline fun <reified K : Any, reified V : Any> SharedPreferences.getMap(key: String) =
    nullableGet(key) { getString(key, EMPTY_STRING) }?.deserializeMap<K, V>()

/** @return [T] result of [block] if [SharedPreferences.contains] the given [key], else `null` */
fun <T> SharedPreferences.nullableGet(key: String, block: () -> T): T? {
    return if (contains(key)) {
        try {
            block()
        } catch (ignored: ClassCastException) {
            null
        }
    } else {
        null
    }
}
// endregion

// region get or default
/** @return [T] value of [key] if any, else [default] */
@JvmName("nonNullGet")
inline fun <reified T : Any> SharedPreferences.get(key: String, default: T): T {
    return when (PrefType.get<T>()) {
        BOOLEAN -> getBoolean(key, default as Boolean) as T
        FLOAT -> getFloat(key, default as Float) as T
        INT -> getInt(key, default as Int) as T
        LONG -> getLong(key, default as Long) as T
        STRING -> getString(key, default as String) as T
        SERIALIZABLE -> getString(key, default.serialize())!!.deserialize()
    }
}

/** @return [List] of [T] if any, else [default] */
inline fun <reified T : Any> SharedPreferences.getList(key: String, default: List<T>) =
    getList(key) ?: default

/** @return [Map] of [K] and [V] if any, else [default] */
inline fun <reified K : Any, reified V : Any> SharedPreferences.getMap(key: String, default: Map<K, V>) =
    getMap(key) ?: default
// endregion

/**
 * @return [T] value of [key] if any, else `null`
 * @throws IllegalArgumentException if [PrefType] is not satisfied
 */
@JvmName("nullableGet")
inline operator fun <reified T : Any?> SharedPreferences.get(key: String): T? {
    return safe(null) {
        when (PrefType.get<T>()) {
            BOOLEAN -> getBoolean(key) as T
            FLOAT -> getFloat(key) as T
            INT -> getInt(key) as T
            LONG -> getLong(key) as T
            STRING -> getString(key) as T
            SERIALIZABLE -> getString(key)?.deserialize()
        }
    }
}

/**
 * Put value [T] into [SharedPreferences] using `set` operator
 * @throws IllegalArgumentException if [PrefType] is not satisfied
 */
inline operator fun <reified T : Any> SharedPreferences.set(key: String, value: T?) = edit { put(key, value) }

/**
 * Remove entry with given [key]
 */
operator fun SharedPreferences.minusAssign(key: String) = edit { remove(key) }

/**
 * Put value [T] into [SharedPreferences]
 * @throws IllegalArgumentException if [PrefType] is not satisfied
 */
inline fun <reified T : Any> SharedPreferences.Editor.put(key: String, value: T?) {
    if (value == null) {
        remove(key)
        return
    }
    when (PrefType.get(value::class)) {
        BOOLEAN -> putBoolean(key, value as Boolean)
        FLOAT -> putFloat(key, value as Float)
        INT -> putInt(key, value as Int)
        LONG -> putLong(key, value as Long)
        STRING -> putString(key, value as String)
        SERIALIZABLE -> putString(key, value.serialize())
    }
}

/** Supported type by [SharedPreferences] */
enum class PrefType(val kClass: KClass<*>) {
    BOOLEAN(Boolean::class),
    FLOAT(Float::class),
    INT(Int::class),
    LONG(Long::class),
    STRING(String::class),
    SERIALIZABLE(Nothing::class);

    companion object {
        /**
         * @return [PrefType] for given [T]
         * @throws IllegalArgumentException if no [PrefType.kClass] is found matching [T]
         */
        inline fun <reified T> get() =
            get(T::class)

        /**
         * @return [PrefType] for given [kClass]
         * @throws IllegalArgumentException if no [PrefType.kClass] is found matching [kClass]
         */
        fun get(kClass: KClass<*>) = values().find { it.kClass == kClass }
            ?: SERIALIZABLE
    }
}
