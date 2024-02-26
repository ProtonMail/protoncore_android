/*
 * Copyright (c) 2024 Proton Technologies AG
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

import android.app.Activity
import android.view.View
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner

interface OnUiComponentCreatedListener {
    /**
     * @param lifecycleOwner The lifecycle owner of the [component].
     * @param onBackPressedDispatcherOwner The back-pressed dispatcher owner of the [component].
     * @param savedStateRegistryOwner The saved state registry owner of the [component].
     * @param component An activity or fragment that has been created.
     */
    fun onUiComponentCreated(
        lifecycleOwner: LifecycleOwner,
        onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
        savedStateRegistryOwner: SavedStateRegistryOwner,
        component: UiComponent
    ) = Unit
}

sealed class UiComponent(val value: Any) {
    abstract fun <V : View> findViewById(@IdRes id: Int): V?
    abstract fun getIdentifier(id: String): Int

    class UiActivity(val activity: Activity) : UiComponent(activity) {
        override fun <V : View> findViewById(id: Int): V? = activity.findViewById(id)
        override fun getIdentifier(id: String): Int =
            activity.resources.getIdentifier(id, "id", activity.packageName)
    }

    class UiFragment(val fragment: Fragment) : UiComponent(fragment) {
        override fun <V : View> findViewById(id: Int): V? = fragment.requireView().findViewById(id)
        override fun getIdentifier(id: String): Int =
            fragment.resources.getIdentifier(id, "id", fragment.requireContext().packageName)
    }
}
