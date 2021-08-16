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

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

/**
 * Call the specified function [block] onBackPressed, from [FragmentActivity]'s dispatcher.
 *
 * When the [lifecycleOwner] is destroyed, it will automatically be removed from the list of callbacks.
 *
 * @param lifecycleOwner [LifecycleOwner], by default this [FragmentActivity].
 * @param block to invoke onBackPressed.
 * @return created [OnBackPressedCallback] - call [OnBackPressedCallback.remove] if you'd like to remove the
 * callback prior to destruction of the associated lifecycle.
 */
fun FragmentActivity.addOnBackPressedCallback(
    lifecycleOwner: LifecycleOwner = this,
    block: () -> Unit
): OnBackPressedCallback {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            block.invoke()
        }
    }
    onBackPressedDispatcher.addCallback(lifecycleOwner, callback)
    return callback
}

/**
 * Call the specified function [block] onBackPressed, from [FragmentActivity]'s dispatcher.
 *
 * When this [Fragment] is destroyed, it will automatically be removed from the list of callbacks.
 *
 * @param block to invoke onBackPressed.
 * @return created [OnBackPressedCallback] - call [OnBackPressedCallback.remove] if you'd like to remove the
 * callback prior to destruction of the associated lifecycle.
 */
fun Fragment.addOnBackPressedCallback(
    block: () -> Unit
): OnBackPressedCallback = requireActivity().addOnBackPressedCallback(this, block)
