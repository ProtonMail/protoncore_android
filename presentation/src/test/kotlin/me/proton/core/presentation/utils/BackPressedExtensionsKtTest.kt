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

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class BackPressedExtensionsKtTest {
    @get:Rule
    internal val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `callback is invoked`() {
        // GIVEN
        val lifecycleRegistryRef = AtomicReference<LifecycleRegistry>()
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle by lazy {
                LifecycleRegistry(this).also {
                    lifecycleRegistryRef.set(it)
                }
            }
        }
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val block = spyk({})

        // WHEN move to RESUMED state
        lifecycleOwner.launchOnBackPressed(onBackPressedDispatcher, block)
        val lifecycleRegistry = requireNotNull(lifecycleRegistryRef.get())
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        // THEN
        assertTrue(onBackPressedDispatcher.hasEnabledCallbacks())

        // WHEN
        onBackPressedDispatcher.onBackPressed()

        // THEN
        verify(exactly = 1) { block() }
        assertFalse(onBackPressedDispatcher.hasEnabledCallbacks())
    }

    @Test
    fun `previous back-pressed callback is preserved`() {
        // GIVEN
        val lifecycleRegistryRef = AtomicReference<LifecycleRegistry>()
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle by lazy {
                LifecycleRegistry(this).also {
                    lifecycleRegistryRef.set(it)
                }
            }
        }
        val onBackPressedDispatcher = OnBackPressedDispatcher()
        val originalBackPressedCallback = mockk<OnBackPressedCallback>(relaxed = true) {
            every { isEnabled } returns true
        }
        onBackPressedDispatcher.addCallback(originalBackPressedCallback)
        val block = spyk({})

        // WHEN move to RESUMED state
        lifecycleOwner.launchOnBackPressed(onBackPressedDispatcher, block)
        val lifecycleRegistry = requireNotNull(lifecycleRegistryRef.get())
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        // THEN
        assertTrue(onBackPressedDispatcher.hasEnabledCallbacks())

        // WHEN
        onBackPressedDispatcher.onBackPressed()

        // THEN
        verify(exactly = 1) { block() }
        verify { originalBackPressedCallback.handleOnBackPressed() }
        verify(exactly = 0) { originalBackPressedCallback.remove() }
        assertTrue(onBackPressedDispatcher.hasEnabledCallbacks())
    }

    @Test
    fun `dispose action`() {
        // GIVEN
        val lifecycleRegistryRef = AtomicReference<LifecycleRegistry>()
        val lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle by lazy {
                LifecycleRegistry(this).also {
                    lifecycleRegistryRef.set(it)
                }
            }
        }
        val onBackPressedDispatcher = OnBackPressedDispatcher()

        // WHEN
        val dispose = lifecycleOwner.launchOnBackPressed(onBackPressedDispatcher, mockk())
        val lifecycleRegistry = requireNotNull(lifecycleRegistryRef.get())
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        dispose()

        // THEN
        assertFalse(onBackPressedDispatcher.hasEnabledCallbacks())
    }
}
