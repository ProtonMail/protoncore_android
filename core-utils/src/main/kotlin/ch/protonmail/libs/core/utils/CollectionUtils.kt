@file:JvmName("CollectionUtils")
@file:Suppress(
    "unused" // Public APIs
)

package ch.protonmail.libs.core.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

/*
 * A file containing extensions for Collections
 * Author: Davide Farella
 */

// region Iterable
/**
 * Execute concurrently a lambda [block] for each [T] element in the [Iterable]
 * @see forEach
 * @return [Unit]
 */
suspend inline fun <T> Iterable<T>.forEachAsync(crossinline block: suspend (T) -> Unit) =
    coroutineScope {
        map { async { block(it) } }.forEach { it.await() }
    }

/**
 * Map concurrently each [T] element in the [Iterable]
 * @see map
 * @return [List] of [V]
 */
suspend inline fun <T, V> Iterable<T>.mapAsync(crossinline mapper: suspend (T) -> V) =
    coroutineScope {
        map { async { mapper(it) } }.map { it.await() }
    }

/**
 * Map concurrently each [T] element in the [Iterable], skipping null values
 * @see mapNotNull
 * @return [List] of [V]
 */
suspend inline fun <T, V : Any> Iterable<T>.mapNotNullAsync(crossinline mapper: suspend (T) -> V?) =
    coroutineScope {
        map { async { mapper(it) } }.mapNotNull { it.await() }
    }
// endregion


// region Collection
/** @return receiver [Collection] if not empty, else `null` */
fun <C : Collection<T>, T> C.takeIfNotEmpty() = takeIf { isNotEmpty() }

/** Map [Pair.first] of receiver [Collection] of [Pair]s */
fun <C : Collection<Pair<Ain, B>>, Ain, B, Aout> C.mapFirst(mapper: (Ain) -> Aout) =
    map { mapper(it.first) to it.second }

/** Map [Pair.second] of receiver [Collection] of [Pair]s */
fun <C : Collection<Pair<A, Bin>>, A, Bin, Bout> C.mapSecond(mapper: (Bin) -> Bout) =
    map { it.first to mapper(it.second) }
// endregion


// region MutableCollection
/**
 * Change first the element matching the [predicate] with [newItem].
 * [newItem] will *NOT* be added if no item matches the [predicate]
 * [newItem] will be added at the end of the collection, if it is ordered
 *
 * @return `true` if element has been changed successfully
 *
 * @receiver [C] is [MutableCollection] of [T]
 */
inline fun <C : MutableCollection<T>, T> C.changeFirst(newItem: T, predicate: (T) -> Boolean) =
    removeFirst(predicate) && add(newItem)

/**
 * Remove first the element matching the [predicate]
 * @return `true` if element has been removed successfully
 *
 * @receiver [C] is [MutableCollection] of [T]
 */
inline fun <C : MutableCollection<T>, T> C.removeFirst(predicate: (T) -> Boolean) =
    find(predicate)?.let { remove(it) } ?: false

/**
 * Replace first the element matching the [predicate] with [newItem].
 * [newItem] will be added even if no item matches the [predicate]
 * [newItem] will be added at the end of the collection, if it is ordered
 *
 * @return `true` if element has been replaced or simply added successfully
 *
 * @receiver [C] is [MutableCollection] of [T]
 */
inline fun <C : MutableCollection<T>, T> C.replaceFirst(newItem: T, predicate: (T) -> Boolean) =
    removeFirst(predicate) or add(newItem)
// endregion


// region Map
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
// endregion


// region Pair
/** Filer items of [Pair.first] of receiver [Pair] of [Collection]s */
fun <P : Pair<Collection<A>, Collection<B>>, A, B> P.filterFirst(predicate: (A) -> Boolean) =
    first.filter(predicate) to second

/** Filer items of [Pair.second] of receiver [Pair] of [Collection]s */
fun <P : Pair<Collection<A>, Collection<B>>, A, B> P.filterSecond(predicate: (B) -> Boolean) =
    first to second.filter(predicate)

/** Filer "not" items of [Pair.first] of receiver [Pair] of [Collection]s */
fun <P : Pair<Collection<A>, Collection<B>>, A, B> P.filterNotFirst(predicate: (A) -> Boolean) =
    first.filterNot(predicate) to second

/** Filer "not" items of [Pair.second] of receiver [Pair] of [Collection]s */
fun <P : Pair<Collection<A>, Collection<B>>, A, B> P.filterNotSecond(predicate: (B) -> Boolean) =
    first to second.filterNot(predicate)

/** Map items of [Pair.first] of receiver [Pair] of [Collection]s */
fun <P : Pair<Collection<Ain>, Collection<B>>, Ain, B, Aout> P.mapFirst(mapper: (Ain) -> Aout) =
    first.map(mapper) to second

/** Map items of [Pair.second] of receiver [Pair] of [Collection]s */
fun <P : Pair<Collection<A>, Collection<Bin>>, A, Bin, Bout> P.mapSecond(mapper: (Bin) -> Bout) =
    first to second.map(mapper)
// endregion


// region Sequence
/** @see [Sequence.filterNot] with indexes */
inline fun <T> Sequence<T>.filterNotIndexed(crossinline predicate: (index: Int, t: T) -> Boolean) =
    filterIndexed { index, t -> !predicate(index, t) }
// endregion
