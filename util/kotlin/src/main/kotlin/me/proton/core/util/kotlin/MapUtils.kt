@file:Suppress("unused")

package me.proton.core.util.kotlin

import kotlin.reflect.KClass

/*
 * Utils for `Map`
 * Author: Davide Farella
 */

/** @return [Map] without `null` values */
@Deprecated(
    "The name of this function is not consistent with Kotlin's conventional naming.",
    replaceWith = ReplaceWith("filterNotNullValues()", "me.proton.core.util.kotlin.filterNotNullValues")
)
fun <T : Any, V : Any> Map<T, V?>.filterNullValues() = filterNotNullValues()

/** @return [Map] without `null` values */
@Suppress("UNCHECKED_CAST") // All values as not null
fun <T : Any, V : Any> Map<T, V?>.filterNotNullValues() = filterValues { it != null } as Map<T, V>

/** @return [Map] of [K] and [V] by filtering by values which are instance of [javaClass] */
@Suppress("UNCHECKED_CAST")
fun <K, V : Any> Map<K, Any?>.filterValues(javaClass: Class<V>) =
    filterValues { it != null && it::class.java == javaClass } as Map<K, V>

/** @return [Map] of [K] and [V] by filtering by values which are instance of [kClass] */
@Suppress("UNCHECKED_CAST")
fun <K, V : Any> Map<K, Any?>.filterValues(kClass: KClass<V>) =
    filterValues { it != null && it::class == kClass } as Map<K, V>

/** @return [Map] of [K] and [V] by filtering by values which are instance of [V] */
inline fun <K, reified V : Any> Map<K, Any?>.filterValues() = filterValues(V::class)
