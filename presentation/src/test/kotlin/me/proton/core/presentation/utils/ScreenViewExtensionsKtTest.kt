/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.presentation.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenViewExtensionsKtTest {
    @get:Rule
    internal val instantTaskExecutorRule = InstantTaskExecutorRule()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `launch block on screen displayed`() {
        // GIVEN
        val lifecycleRegistryRef = AtomicReference<LifecycleRegistry>()
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle by lazy {
                LifecycleRegistry(this).also {
                    lifecycleRegistryRef.set(it)
                }
            }
        }
        val savedStateRegistry = mockk<SavedStateRegistry> {
            every { getSavedStateProvider(any()) } returns null
            every { consumeRestoredStateForKey(any()) } returns null
            justRun { registerSavedStateProvider(any(), any()) }
        }
        var blockCallCount = 0
        val block = { blockCallCount += 1 }

        // WHEN
        lifecycleOwner.launchOnScreenView(savedStateRegistry, block)
        val lifecycleRegistry = requireNotNull(lifecycleRegistryRef.get())

        // THEN
        assertEquals(0, blockCallCount)

        // WHEN
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        // THEN
        assertEquals(1, blockCallCount)
    }

    @Test
    fun `skip launch if recreating`() {
        // GIVEN
        val lifecycleRegistryRef = AtomicReference<LifecycleRegistry>()
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle by lazy {
                LifecycleRegistry(this).also {
                    lifecycleRegistryRef.set(it)
                }
            }
        }
        val savedStateRegistry = mockk<SavedStateRegistry> {
            every { getSavedStateProvider(any()) } returns null
            every { consumeRestoredStateForKey(any()) } returns mockk() // <- simulate state for recreating
            justRun { registerSavedStateProvider(any(), any()) }
        }
        var blockCallCount = 0
        val block = { blockCallCount += 1 }

        // WHEN
        lifecycleOwner.launchOnScreenView(savedStateRegistry, block)
        val lifecycleRegistry = requireNotNull(lifecycleRegistryRef.get())

        // THEN
        assertEquals(0, blockCallCount)

        // WHEN
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        // THEN
        assertEquals(0, blockCallCount)
    }

    @Test
    fun `do not register saved state provider twice`() {
        // GIVEN
        val lifecycle = mockk<Lifecycle> {
            justRun { addObserver(any()) }
        }
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle = lifecycle
        }
        val savedStateRegistry = mockk<SavedStateRegistry> {
            every { getSavedStateProvider(any()) } returns mockk()
        }

        // WHEN
        lifecycleOwner.launchOnScreenView(savedStateRegistry, mockk())

        // THEN
        verify(exactly = 0) { savedStateRegistry.registerSavedStateProvider(any(), any()) }
    }

    @Test
    fun `dispose action`() {
        // GIVEN
        val lifecycle = mockk<Lifecycle> {
            justRun { addObserver(any()) }
            justRun { removeObserver(any()) }
        }
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle = lifecycle
        }
        val savedStateRegistry = mockk<SavedStateRegistry> {
            every { getSavedStateProvider(any()) } returns null
            justRun { unregisterSavedStateProvider(any()) }
            justRun { registerSavedStateProvider(any(), any()) }
        }

        // WHEN
        val dispose = lifecycleOwner.launchOnScreenView(savedStateRegistry, mockk())
        dispose()

        // THEN
        verify { savedStateRegistry.unregisterSavedStateProvider(any()) }
        verify { lifecycle.removeObserver(any()) }
    }
}
