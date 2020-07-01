@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.utils

import android.database.Cursor

/*
 * Utilities for Android's Cursor
 * Author: Davide Farella
 */

/** @return [Iterator] of [Cursor] from the receiver [Cursor] */
operator fun Cursor.iterator() = object : Iterator<Cursor> {

    /** Returns `true` if the iteration has more elements */
    override fun hasNext(): Boolean {
        // Check if has more element, then back to the previous position
        return moveToNext().also { moveToPrevious() }
    }

    /** Returns the next element in the iteration */
    override fun next(): Cursor {
        moveToNext()
        return this@iterator
    }
}

/**
 * Map entries of a cursor to [T] elements
 * @return [List] of [T]
 */
inline fun <T> Cursor.map(mapper: (Cursor) -> T): List<T> {
    val collector = mutableListOf<T>()
    iterator().forEach { collector += mapper(it) }
    return collector
}

/**
 * Map entries of a cursor to [T] elements filtering null elements
 * @return [List] of [T]
 */
inline fun <T: Any> Cursor.mapNotNull(mapper: (Cursor) -> T?): List<T> {
    return map(mapper).filterNotNull()
}
