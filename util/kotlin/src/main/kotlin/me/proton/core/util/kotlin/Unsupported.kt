package me.proton.core.util.kotlin

/** A typealias of [Nothing] for unsupported operations */
typealias Unsupported = Nothing

/**
 * [Unsupported]
 * @throws UnsupportedOperationException
 */
val unsupported: Nothing get() = throw UnsupportedOperationException("unsupported")
