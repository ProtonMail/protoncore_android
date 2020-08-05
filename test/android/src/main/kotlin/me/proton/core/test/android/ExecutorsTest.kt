@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.proton.core.test.android

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * An interface meant to be implemented by a Test Suite that uses Android code that internally uses Executors.
 * Example:
```
class MyClassTest : ExecutorsTest by executorsTest {
// test cases
}
```
 *
 * It provides a [ExecutorsTestRule] and alternative dispatchers.
 */
interface ExecutorsTest {
    @get:Rule val executorsRule: ExecutorsTestRule

    val executorService: ExecutorService
}

/** @see ExecutorsTest */
val executorsTest = object : ExecutorsTest {
    override val executorService: ExecutorService = MainThreadExecutorService()

    override val executorsRule = ExecutorsTestRule(executorService)
}

/**
 * A JUnit Test Rule that set a Main Dispatcher
 */
class ExecutorsTestRule internal constructor(
    private val executorService: ExecutorService = MainThreadExecutorService()
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        mockkStatic(Executors::class)
        every { Executors.newFixedThreadPool(any()) } returns executorService
    }

    override fun finished(description: Description?) {
        super.finished(description)
        unmockkStatic(Executors::class)
    }
}

/**
 * An [ExecutorService] that will run on the Main ( current ) thread
 */
@Suppress("NotImplementedDeclaration") // Meant to be used in test, not implemented functions can be compared
//                                                  to a relaxed mockk
class MainThreadExecutorService : ExecutorService {

    override fun execute(command: Runnable?) {
        command?.run()
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

    override fun shutdownNow(): MutableList<Runnable> {
        TODO("Not yet implemented")
    }

    override fun isShutdown(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTerminated(): Boolean {
        TODO("Not yet implemented")
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> submit(task: Runnable?, result: T): Future<T> {
        TODO("Not yet implemented")
    }

    override fun submit(task: Runnable?): Future<*> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>?): MutableList<Future<T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAll(
        tasks: MutableCollection<out Callable<T>>?,
        timeout: Long,
        unit: TimeUnit?
    ): MutableList<Future<T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?, timeout: Long, unit: TimeUnit?): T {
        TODO("Not yet implemented")
    }

}
