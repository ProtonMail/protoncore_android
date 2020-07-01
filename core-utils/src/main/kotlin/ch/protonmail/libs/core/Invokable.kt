package ch.protonmail.libs.core

/**
 * Interface for classes that need a shortcuts for get into the context
 * @author Davide Farella
 */
interface Invokable

/**
 * Invoke function for easily get into the [T] ( subtype of [Invokable] ) context.
 * @return [V] result of [block]
 */
inline operator fun <T : Invokable, V> T.invoke(block: T.() -> V) = block()
