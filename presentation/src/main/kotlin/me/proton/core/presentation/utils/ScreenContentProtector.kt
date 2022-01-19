/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import android.app.Activity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import me.proton.core.presentation.BuildConfig
import me.proton.core.util.kotlin.CoreLogger
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Adds security measures to the displayed screens.
 */
class ScreenContentProtector(
    private val configuration: ProtectScreenConfiguration = ProtectScreenConfiguration(),
) {

    /** Sets up the necessary protections based on [configuration] */
    fun protect(activity: Activity) {
        increaseCounter(activity.hashCode())
        if (configuration.preventScreenRecording) preventScreenRecording(activity)
    }

    /** Reverts the protections added in [configuration] */
    fun unprotect(activity: Activity) {
        val activityProtectionCount = decreaseCounter(activity.hashCode())
        if (activityProtectionCount > 0) return
        clearPreventScreenRecording(activity)
    }

    private fun preventScreenRecording(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun clearPreventScreenRecording(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun increaseCounter(id: Int) {
        val count = protectionScreenCounts[id] ?: 0
        protectionScreenCounts[id] = count + 1
    }

    private fun decreaseCounter(id: Int): Int {
        val count = protectionScreenCounts[id] ?: 0
        val updatedCount = count - 1
        when {
            updatedCount > 0 -> protectionScreenCounts[id] = updatedCount
            updatedCount == 0 -> protectionScreenCounts.remove(id)
            else -> CoreLogger.e(TAG, IllegalStateException("protectionCount < 0. This should never happen."))
        }
        return maxOf(updatedCount, 0)
    }

    companion object {
        /** Used to check if any still visible screen needs protection when `unprotect` is called */
        private var protectionScreenCounts = mutableMapOf<Int, Int>()

        const val TAG = "ScreenContentProtector"
    }
}

/** Used to configure which protections should be applied to the activity */
class ProtectScreenConfiguration(
    /** Prevent screen capture etc. to record user password. Also masks app in app switcher. */
    val preventScreenRecording: Boolean = BuildConfig.DEBUG.not()
)

/** Delegate to handle screen protection based on the Activity's lifecycle */
class ActivityScreenContentProtectionDelegate(
    private val activity: ComponentActivity,
    configuration: ProtectScreenConfiguration,
) : ReadOnlyProperty<ComponentActivity, ScreenContentProtector> {

    private val screenProtector = ScreenContentProtector(configuration)

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            screenProtector.protect(activity)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            screenProtector.unprotect(activity)
        }
    }

    init {
        activity.lifecycle.addObserver(lifecycleObserver)
    }

    override fun getValue(thisRef: ComponentActivity, property: KProperty<*>): ScreenContentProtector {
        return screenProtector
    }
}

/** Delegate to handle screen protection based on the Fragment view's lifecycle */
class FragmentScreenContentProtectionDelegate(
    private val fragment: Fragment,
    configuration: ProtectScreenConfiguration,
) : ReadOnlyProperty<Fragment, ScreenContentProtector> {

    private val screenProtector = ScreenContentProtector(configuration)

    private var activity: Activity? = null

    init {
        // Needed since viewLifecycleOwner is not available until the view is created
        fragment.viewLifecycleOwnerLiveData.observeForever { owner ->
            val isViewCreated = owner != null
            activity = if (isViewCreated) {
                fragment.requireActivity().also { screenProtector.protect(it) }
            } else {
                activity?.let { screenProtector.unprotect(it) }
                null
            }
        }
    }

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): ScreenContentProtector {
        return screenProtector
    }
}

/** Creates a delegate that will add screen protection automatically based on the Activity's lifecycle. */
fun ComponentActivity.protectScreen(configuration: ProtectScreenConfiguration = ProtectScreenConfiguration()) =
    ActivityScreenContentProtectionDelegate(this, configuration)

/** Creates a delegate that will add screen protection automatically based on the Fragment view's lifecycle. */
fun Fragment.protectScreen(configuration: ProtectScreenConfiguration = ProtectScreenConfiguration()) =
    FragmentScreenContentProtectionDelegate(this, configuration)
