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

// Public APIs
@file:Suppress("unused")

package me.proton.core.util.android.sharedpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import me.proton.core.util.kotlin.NeedSerializable
import me.proton.core.util.kotlin.serialize

/*
 * Extensions for `SharedPreferences` delegation
 * Author: Davide Farella
 */

/** Interface providing extensions for Delegation for [SharedPreferences] */
interface SharedPreferencesDelegationExtensions {
    val preferences: SharedPreferences
}

/** @return [SharedPreferencesDelegationExtensions] from receiver [SharedPreferences] */
@PublishedApi internal val SharedPreferences.ext get() = let { prefs ->
    object : SharedPreferencesDelegationExtensions {
        override val preferences get() = prefs
    }
}

// region Delegation by explicit Type
/** @return ( by Delegation ) Mutable Property of type [Boolean] */
fun SharedPreferencesDelegationExtensions.boolean(default: Boolean, key: String? = null) = required(
    explicitKey = key,
    getter = { k -> getBoolean(k, default) },
    setter = { k, v -> edit { putBoolean(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type Nullable [Boolean] */
fun SharedPreferencesDelegationExtensions.boolean(key: String? = null) = optional(
    explicitKey = key,
    getter = { k -> getBoolean(k) },
    setter = { k, v -> edit { putBoolean(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type [Int] */
fun SharedPreferencesDelegationExtensions.int(default: Int, key: String? = null) = required(
    explicitKey = key,
    getter = { k -> getInt(k, default) },
    setter = { k, v -> edit { putInt(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type Nullable [Int] */
fun SharedPreferencesDelegationExtensions.int(key: String? = null) = optional(
    explicitKey = key,
    getter = { k -> getInt(k) },
    setter = { k, v -> edit { putInt(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type [Long] */
fun SharedPreferencesDelegationExtensions.long(default: Long, key: String? = null) = required(
    explicitKey = key,
    getter = { k -> getLong(k, default) },
    setter = { k, v -> edit { putLong(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type Nullable [Long] */
fun SharedPreferencesDelegationExtensions.long(key: String? = null) = optional(
    explicitKey = key,
    getter = { k -> getLong(k) },
    setter = { k, v -> edit { putLong(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type [String] */
fun SharedPreferencesDelegationExtensions.string(default: String, key: String? = null) = required(
    explicitKey = key,
    getter = { k -> getString(k, default)!! },
    setter = { k, v -> edit { putString(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type Nullable [String] */
fun SharedPreferencesDelegationExtensions.string(key: String? = null) = optional(
    explicitKey = key,
    getter = { k -> getString(k) },
    setter = { k, v -> edit { putString(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type [List] of [T] */
@NeedSerializable
inline fun <reified T : Any> SharedPreferencesDelegationExtensions.list(
    default: List<T>,
    key: String? = null
) = required(
    explicitKey = key,
    getter = { k -> getList(k, default) },
    setter = { k, v -> edit { putString(k, v.serialize()) } }
)

/** @return ( by Delegation ) Mutable Property of type Nullable [List] of [T] */
@NeedSerializable
inline fun <reified T : Any> SharedPreferencesDelegationExtensions.list(key: String? = null) = optional(
    explicitKey = key,
    getter = { k -> getList<T>(k) },
    setter = { k, v -> edit { putString(k, v.serialize()) } }
)

/** @return ( by Delegation ) Mutable Property of type [Map] of [K] and [V] */
@NeedSerializable
inline fun <reified K : Any, reified V : Any> SharedPreferencesDelegationExtensions.map(
    default: Map<K, V>,
    key: String? = null
) = required(
    explicitKey = key,
    getter = { k -> getMap(k, default) },
    setter = { k, v -> edit { putString(k, v.serialize()) } }
)

/** @return ( by Delegation ) Mutable Property of type Nullable [Map] of [K] and [V] */
@NeedSerializable
inline fun <reified K : Any, reified V : Any> SharedPreferencesDelegationExtensions.map(key: String? = null) = optional(
    explicitKey = key,
    getter = { k -> getMap<K, V>(k) },
    setter = { k, v -> edit { putString(k, v.serialize()) } }
)

// endregion

// region Delegation by Type inference
/** @return ( by Delegation ) Mutable Property of type [T] */
@JvmName("nonNullableInvoke") // For avoid clashing with Nullable `invoke` function
inline operator fun <reified T : Any> SharedPreferencesDelegationExtensions.invoke(
    default: T,
    key: String? = null
) = any(default, key)

/** @return ( by Delegation ) Mutable Property of type [T] */
@JvmName("nonNullableAny") // For avoid clashing with Nullable `any` function
inline fun <reified T : Any> SharedPreferencesDelegationExtensions.any(
    default: T,
    key: String? = null
) = required(
    explicitKey = key,
    getter = { k -> get(k, default) },
    setter = { k, v -> edit { put(k, v) } }
)

/** @return ( by Delegation ) Mutable Property of type [T] */
@JvmName("nullableInvoke") // For avoid clashing with non Nullable `invoke` function
inline operator fun <reified T : Any?> SharedPreferencesDelegationExtensions.invoke(
    key: String? = null
) = any<T>(key)

/** @return ( by Delegation ) Mutable Property of type [T] */
@JvmName("nullableAny") // For avoid clashing with non Nullable `any` function
inline fun <reified T : Any?> SharedPreferencesDelegationExtensions.any(
    key: String? = null
) = optional(
    explicitKey = key,
    getter = { k -> get<T>(k) },
    setter = { k, v -> edit { unsafePut(k, v) } }
)
// endregion

// region SharedPreferences
fun SharedPreferences.boolean(default: Boolean, key: String? = null) = ext.boolean(default, key)
fun SharedPreferences.boolean(key: String? = null) = ext.boolean(key)
fun SharedPreferences.int(default: Int, key: String? = null) = ext.int(default, key)
fun SharedPreferences.int(key: String? = null) = ext.int(key)
fun SharedPreferences.long(default: Long, key: String? = null) = ext.long(default, key)
fun SharedPreferences.long(key: String? = null) = ext.long(key)
fun SharedPreferences.string(default: String, key: String? = null) = ext.string(default, key)
fun SharedPreferences.string(key: String? = null) = ext.string(key)
@NeedSerializable
inline fun <reified T : Any> SharedPreferences.list(default: List<T>, key: String? = null) = ext.list(default, key)
@NeedSerializable
inline fun <reified T : Any> SharedPreferences.list(key: String? = null) = ext.list<T>(key)
@NeedSerializable
inline fun <reified K : Any, reified V : Any> SharedPreferences.map(default: Map<K, V>, key: String? = null) =
    ext.map(default, key)
@NeedSerializable
inline fun <reified K : Any, reified V : Any> SharedPreferences.map(key: String? = null) = ext.map<K, V>(key)
inline fun <reified T : Any> SharedPreferences.any(default: T, key: String? = null) = this(default, key)
inline fun <reified T : Any?> SharedPreferences.any(key: String? = null) = this<T>(key)
inline operator fun <reified T : Any> SharedPreferences.invoke(default: T, key: String? = null) = ext(default, key)
inline operator fun <reified T : Any?> SharedPreferences.invoke(key: String? = null) = ext<T>(key)
// endregion

// region Delegation utils
/** [SharedPreferences.Editor.put] without a bound of [T] to [Any] */
@PublishedApi // Required for inline
internal inline fun <reified T> SharedPreferences.Editor.unsafePut(key: String, value: T) {
    put(key, value as Any)
}
// endregion
