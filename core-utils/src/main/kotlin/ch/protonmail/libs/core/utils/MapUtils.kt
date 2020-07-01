package ch.protonmail.libs.core.utils

/*
 * Utils for `Map`
 * Author: Davide Farella
 */

/** @return [Map] without `null` values */
@Suppress("UNCHECKED_CAST") // All values as not null
fun <T : Any, V : Any> Map<T, V?>.filterNullValues() = filterValues { it != null } as Map<T, V>
