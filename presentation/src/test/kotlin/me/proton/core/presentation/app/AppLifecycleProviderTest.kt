/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.presentation.app

import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.proton.core.test.kotlin.flowTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AppLifecycleProviderTest {
    @Test
    fun `default state is Background`() {
        val tested = make()
        waitForMainThreadIdle()
        assertEquals(AppLifecycleProvider.State.Background, tested.state.value)
        assertEquals(Lifecycle.State.INITIALIZED, tested.lifecycle.currentState)
    }

    @Test
    fun `can be created from non-main thread`() {
        val tested = runBlocking(Dispatchers.Default) { make() }
        waitForMainThreadIdle()
        assertEquals(AppLifecycleProvider.State.Background, tested.state.value)
        assertEquals(Lifecycle.State.INITIALIZED, tested.lifecycle.currentState)
    }

    @Test
    fun `cannot be created from non-main looper`() {
        val future = Executors.newSingleThreadExecutor().submit {
            Looper.prepare()
            make(looper = Looper.myLooper()!!)
            Looper.loop()
        }

        val exception = assertFailsWith<ExecutionException> { future.get() }
        assertTrue(exception.cause?.message?.contains("Method addObserver must be called on the main thread") == true)
    }

    @Test
    fun `lifecycle changes are correctly emitted`() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        val tested = make(lifecycleOwner)
        waitForMainThreadIdle()

        flowTest(tested.state) {
            assertEquals(AppLifecycleProvider.State.Background, awaitItem()) // initial state
            assertEquals(AppLifecycleProvider.State.Foreground, awaitItem())
            assertEquals(AppLifecycleProvider.State.Background, awaitItem())
            assertEquals(AppLifecycleProvider.State.Foreground, awaitItem())
        }

        // Transition to foreground
        lifecycleOwner.registry.currentState = Lifecycle.State.RESUMED

        // Transition to background
        lifecycleOwner.registry.currentState = Lifecycle.State.CREATED

        // Transition to foreground
        lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
    }

    private fun make(
        lifecycleOwner: LifecycleOwner = TestLifecycleOwner(),
        looper: Looper = Looper.getMainLooper()
    ): AppLifecycleObserver = AppLifecycleObserver(looper, lifecycleOwner)

    /** Waits for the init block of [AppLifecycleObserver] to finish executing. */
    private fun waitForMainThreadIdle() = shadowOf(Looper.getMainLooper()).idle()

    private class TestLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override fun getLifecycle(): Lifecycle = registry
    }
}
