package me.proton.core.util.kotlin

/**
 * Interface for classes that need a shortcuts for get into the context
 * @see invoke
 *
 * This is particularly helpful when we have an extension function inside a class.
 *
 * Example:
```
class MyInvokableClass : Invokable {
    fun String.sayHello() {
        println("Hello $this")
    }
}

val a = MyInvokableClass()
a { "Davide".sayHello() }
```
 *
 * @author Davide Farella
 */
interface Invokable

/**
 * Invoke function for easily get into the [T] ( subtype of [Invokable] ) context.
 * @return [V] result of [block]
 */
inline operator fun <T : Invokable, V> T.invoke(block: T.() -> V) = block()
