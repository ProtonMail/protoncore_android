@file:Suppress("unused")

package me.proton.core.util.kotlin

/*
 * Utilities for numbers
 * Author: Davide Farella
 */

/**
 * @return `true` if receiver [Int] is `1`, `false` if `0`, else throw exception
 * @throws IllegalArgumentException if receiver [Int] is not `1` or `0`
 */
fun Int.toBoolean() = when (this) {
    0 -> false
    1 -> true
    else -> throw IllegalArgumentException("Expected '0' or '1', but '$this' is found")
}

/** @return `true` if receiver [Int] is `1`, else `false` */
fun Int.toBooleanOrFalse() = this == 1

/** @return `false` if receiver [Int] is `0`, else `true` */
fun Int.toBooleanOrTrue() = this != 0

/**
 * @return `1` if receiver [Boolean] is `true` or `0` if receiver is `false`.
 */
fun Boolean.toInt() = when (this) {
    true -> 1
    false -> 0
}
