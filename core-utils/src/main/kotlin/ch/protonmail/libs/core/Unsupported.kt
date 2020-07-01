@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core

/** A typealias of [Nothing] for unsupported operations */
typealias Unsupported = Nothing

/**
 * [Unsupported]
 * @throws UnsupportedOperationException
 */
val unsupported: Nothing get() = throw UnsupportedOperationException("unsupported")
