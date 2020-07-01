@file:Suppress("unused")

package me.proton.core.util.kotlin

import kotlin.math.absoluteValue

/** An empty [String] `""` */
const val EMPTY_STRING = ""

// region CharSequence
/**
 * Infix function for run [CharSequence.contains] ignoring the case
 * @return `true` if the receiver [CharSequence] contains [other], independently by their case
 */
infix fun CharSequence.containsNoCase(other: CharSequence) = contains(other, ignoreCase = true)

/** @return `null` if receiver [CharSequence] is blank */
fun CharSequence.nullIfBlank() = if (isNotBlank()) this else null

/** @return `null` if receiver [CharSequence] is empty */
fun CharSequence.nullIfEmpty() = if (isNotEmpty()) this else null

/**
 * `get` operator for call [CharSequence.subSequence].
 * E.g. `charSequence[4, 10]`
 *
 * @return [CharSequence]
 */
operator fun CharSequence.get(from: Int, to: Int) = this.subSequence(from, to)

/**
 * Subsequence the receiver [CharSequence]
 *
 * @param start [String] where to start substring-ing, optionally define a [startIndex] for exclude
 * matching before the given index
 * If [String] is not found, [startIndex] will be used as start
 * Default is `null`
 *
 * @param end [String] where to stop substring-ing, optionally define an [endIndex] for exclusive
 * matching after the given index
 * If [String] is not found, [endIndex] will be used as end
 *
 * @param startIndex [Int] Default is `0`
 * @param endIndex [Int] Default is [String.lastIndex]
 *
 * @param startInclusive [Boolean] whether the given [start] [String] must be included in the result
 * Default is `false`
 *
 * @param endInclusive [Boolean] whether the given [end] [String] must be included in the result
 * Default is `false`
 *
 * @param ignoreMissingStart [Boolean] whether a missing start should fallback to the first index.
 * if `false` and empty [CharSequence] will be returned.
 * Default is `false`
 *
 * @param ignoreMissingEnd [Boolean] whether a missing end should fallback to the length.
 * If `false` and empty [CharSequence] will be returned.
 * Default is `false`
 *
 * @param ignoreCase [Boolean] whether [start] and [end] must be matched ignoring their case
 * Default is `false`
 */
fun CharSequence.subsequence(
    start: String? = null,
    end: String? = null,
    startIndex: Int = 0,
    endIndex: Int = length,
    startInclusive: Boolean = false,
    endInclusive: Boolean = false,
    ignoreMissingStart: Boolean = false,
    ignoreMissingEnd: Boolean = false,
    ignoreCase: Boolean = false
): CharSequence {
    // Calculate the index where to start substring-ing
    val from = if (start != null) {
        val relative = indexOf(start, startIndex, ignoreCase)
        when {
            relative == -1 -> if (ignoreMissingStart) startIndex else -1
            startInclusive -> relative
            else -> relative + start.length
        }
    } else startIndex

    // Calculate the index where to stop substring-ing
    val to = if (end != null) {
        val trimToEnd = get(0, endIndex)
        val relative = trimToEnd.indexOf(end, from, ignoreCase = ignoreCase)
        when {
            relative == -1 -> if (ignoreMissingEnd) endIndex else -1
            endInclusive -> relative + end.length
            else -> relative
        }
    } else endIndex

    return if (from == -1 || to == -1) EMPTY_STRING
    else get(from, to.coerceAtLeast(from))
}

@Deprecated(
    "Use 'subsequence'",
    ReplaceWith("subsequence(start, end, startIndex, endIndex, startInclusive, endInclusive, ignoreCase)")
)
fun CharSequence.substring(
    start: String? = null,
    end: String? = null,
    startIndex: Int = 0,
    endIndex: Int = length,
    startInclusive: Boolean = false,
    endInclusive: Boolean = false,
    ignoreMissingStart: Boolean = false,
    ignoreMissingEnd: Boolean = false,
    ignoreCase: Boolean = false
) = subsequence(start, end, startIndex, endIndex, startInclusive, endInclusive, ignoreMissingStart, ignoreMissingEnd, ignoreCase)

/**
 * @return [CharSequence]. If receiver [CharSequence] is shorted than the given [maxLength], return
 * itself. Else [CharSequence.substring] from 0 to [maxLength] and add "..." at the end of it.
 * E.g. >
val hello = "Hello world!"
println( hello.truncateToLength( 9 ) ) // Hello Wo...
 */
fun CharSequence.truncateToLength(maxLength: Int): CharSequence {
    return if (length <= maxLength) this
    else "${this[0, maxLength - 1]}..."
}
// endregion

// region String
/** Infix version of [String.endsWith] */
infix fun String.endsWith(suffix: String) = endsWith(suffix, ignoreCase = false)

/**
 * Infix function for run [String.equals] ignoring the case
 * @return `true` if the 2 [String] are the case, independently by their case
 */
infix fun String.equalsNoCase(other: String?) = equals(other, ignoreCase = true)

/**
 * `get` operator for call [String.substring].
 * E.g. `string[4, 10]`
 *
 * @return [String]
 */
operator fun String.get(from: Int, to: Int) = this.substring(from, to)

/** @return `null` if receiver [String] is blank */
fun String.nullIfBlank() = if (isNotBlank()) this else null

/** @return `null` if receiver [String] is empty */
fun String.nullIfEmpty() = if (isNotEmpty()) this else null

/**
 * Substring the receiver [String]
 *
 * @param start [String] where to start substring-ing, optionally define a [startIndex] for exclude
 * matching before the given index
 * If [String] is not found, [startIndex] will be used as start
 * Default is `null`
 *
 * @param end [String] where to stop substring-ing, optionally define an [endIndex] for exclusive
 * matching after the given index
 * If [String] is not found, [endIndex] will be used as end
 *
 * @param startIndex [Int] Default is `0`
 * @param endIndex [Int] Default is [String.lastIndex]
 *
 * @param startInclusive [Boolean] whether the given [start] [String] must be included in the result
 * Default is `false`
 *
 * @param endInclusive [Boolean] whether the given [end] [String] must be included in the result
 * Default is `false`
 *
 * @param ignoreMissingStart [Boolean] whether a missing start should fallback to the first index.
 * if `false` and empty [String] will be returned.
 * Default is `false`
 *
 * @param ignoreMissingEnd [Boolean] whether a missing end should fallback to the length.
 * If `false` and empty [String] will be returned.
 * Default is `false`
 *
 * @param ignoreCase [Boolean] whether [start] and [end] must be matched ignoring their case
 * Default is `false`
 */
fun String.substring(
    start: String? = null,
    end: String? = null,
    startIndex: Int = 0,
    endIndex: Int = length,
    startInclusive: Boolean = false,
    endInclusive: Boolean = false,
    ignoreMissingStart: Boolean = false,
    ignoreMissingEnd: Boolean = false,
    ignoreCase: Boolean = false
) = subsequence(
    start = start,
    end = end,
    startIndex = startIndex,
    endIndex = endIndex,
    startInclusive = startInclusive,
    endInclusive = endInclusive,
    ignoreMissingStart = ignoreMissingStart,
    ignoreMissingEnd = ignoreMissingEnd,
    ignoreCase = ignoreCase
)
    .toString()

/** Infix version of [String.startsWith] */
infix fun String.startsWith(prefix: String) = startsWith(prefix, ignoreCase = false)

/**
 * @return [String] with removed consecutive empty lines
 * @param allowed count of consecutive empty lines allowed. Default is `1`
 * @param map map each line of the receiver [String]
 */
fun String.stripEmptyLines(
    allowed: Int = 1,
    keepSurroundings: Boolean = false,
    map: (String) -> String = { it }
): String {
    // placeholder for lines outside of bounds
    val placeholder = "not empty"
    val lines = if (keepSurroundings) lines() else trim().lines()
    // Remove duplicated lines and apply eventual mapping
    return lines.asSequence().filterNotIndexed { index, _ ->
        val linesToCheck = (0 .. allowed.absoluteValue)
            .map { lines.getOrElse(index - it) { placeholder} }
        linesToCheck.all { it.isBlank() }
    }.map(map).reduce { acc, s -> "$acc\n$s" }
}

/** @return receiver [String] if not empty, else `null` */
fun String.takeIfNotEmpty() = takeIf { it.isNotEmpty() }

/** @return receiver [String] if not blank, else `null` */
fun String.takeIfNotBlank() = takeIf { it.isNotBlank() }

/**
 * @return receiver [String] concatenated with itself by the given [times]
 * E.G. `` "Hello" * 3 `` -> `HelloHelloHello`
 */
operator fun String.times(times: Int): String {
    val builder = StringBuilder()
    repeat(times) { builder.append(this) }
    return builder.toString()
}
// endregion
