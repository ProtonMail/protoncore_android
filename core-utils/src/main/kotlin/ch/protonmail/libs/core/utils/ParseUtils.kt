package ch.protonmail.libs.core.utils

/*
 * Utils for parsing
 * Author: Davide Farella
 */

/**
 * @return `true` if receiver [String] [equalsNoCase] to `true`
 * `false` if receiver [String] [equalsNoCase] to `false`
 * else `null`
 */
fun String.toBooleanOrNull(): Boolean? {
    if (equalsNoCase("true")) return true
    if (equalsNoCase("false")) return false
    return null
}
