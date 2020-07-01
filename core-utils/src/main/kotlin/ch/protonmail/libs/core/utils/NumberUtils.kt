package ch.protonmail.libs.core.utils

/*
 * Utilities for numbers
 * Author: Davide Farella
 */

/**
 * @return `true` if receiver [Int] is `1`, `false` if `0`, else throw exception
 * @throws IllegalArgumentException if receiver [Int] is not `1` or `0`
 */
fun Int.toBoolean() = when(this) {
    0 -> false
    1 -> true
    else -> throw IllegalArgumentException("Expected '0' or '1', but '$this' is found")
}

/** @return `true` if receiver [Int] is `1`, else `false` */
fun Int.toBooleanOrFalse() = this == 1
