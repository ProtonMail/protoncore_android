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

import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ApplicationExtensionsKtTest {
    @Test
    fun `callback is called when activity is created`() {
        // GIVEN
        val activityCallbacksRef = AtomicReference<ActivityLifecycleCallbacks>()
        val app = mockk<Application> {
            every { registerActivityLifecycleCallbacks(any()) } coAnswers {
                activityCallbacksRef.set(firstArg())
            }
        }
        val activity = mockk<ComponentActivity>()
        val listener = TestListener()

        // WHEN
        app.launchOnUiComponentCreated(listener)
        val activityCallbacks = requireNotNull(activityCallbacksRef.get())
        activityCallbacks.onActivityCreated(activity, null)

        // THEN
        assertEquals(activity, listener.component?.value)
        assertEquals(activity, listener.lifecycleOwner)
        assertEquals(activity, listener.onBackPressedDispatcherOwner)
        assertEquals(activity, listener.savedStateRegistryOwner)
    }

    @Test
    fun `callback is called when fragment is created`() {
        // GIVEN
        val activityCallbacksRef = AtomicReference<ActivityLifecycleCallbacks>()
        val fragmentCallbacksRef = AtomicReference<FragmentManager.FragmentLifecycleCallbacks>()
        val app = mockk<Application> {
            every { registerActivityLifecycleCallbacks(any()) } coAnswers {
                activityCallbacksRef.set(firstArg())
            }
        }
        val fragmentManager = mockk<FragmentManager> {
            every { registerFragmentLifecycleCallbacks(any(), any()) } answers {
                fragmentCallbacksRef.set(firstArg())
            }
        }
        val activity = mockk<FragmentActivity> {
            every { supportFragmentManager } returns fragmentManager
        }
        val fragment = mockk<Fragment> {
            every { requireActivity() } returns activity
        }
        val listener = TestListener()

        // WHEN
        app.launchOnUiComponentCreated(listener)
        val activityCallbacks = requireNotNull(activityCallbacksRef.get())
        activityCallbacks.onActivityCreated(activity, null)
        val fragmentCallbacks = requireNotNull(fragmentCallbacksRef.get())
        fragmentCallbacks.onFragmentCreated(fragmentManager, fragment, null)

        // THEN
        assertEquals(fragment, listener.component?.value)
        assertEquals(fragment, listener.lifecycleOwner)
        assertEquals(activity, listener.onBackPressedDispatcherOwner)
        assertEquals(fragment, listener.savedStateRegistryOwner)
    }

    @Test
    fun `register and dispose`() {
        // GIVEN
        val app = mockk<Application> {
            justRun { registerActivityLifecycleCallbacks(any()) }
            justRun { unregisterActivityLifecycleCallbacks(any()) }
        }

        // WHEN
        val disposeAction = app.launchOnUiComponentCreated(mockk())
        disposeAction()

        // THEN
        val activityCallbacksSlot = slot<ActivityLifecycleCallbacks>()
        verify { app.registerActivityLifecycleCallbacks(capture(activityCallbacksSlot)) }
        val activityCallbacks = activityCallbacksSlot.captured

        verify { app.unregisterActivityLifecycleCallbacks(capture(activityCallbacksSlot)) }
        assertSame(activityCallbacks, activityCallbacksSlot.captured)
    }
}

private class TestListener : OnUiComponentCreatedListener {
    var lifecycleOwner: LifecycleOwner? = null
        private set
    var onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner? = null
        private set
    var savedStateRegistryOwner: SavedStateRegistryOwner? = null
        private set
    var component: UiComponent? = null
        private set

    override fun invoke(
        lifecycleOwner: LifecycleOwner,
        onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
        savedStateRegistryOwner: SavedStateRegistryOwner,
        component: UiComponent
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.onBackPressedDispatcherOwner = onBackPressedDispatcherOwner
        this.savedStateRegistryOwner = savedStateRegistryOwner
        this.component = component
    }
}
