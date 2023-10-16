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
import androidx.activity.addCallback
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicReference

fun LifecycleOwner.launchOnBackPressed(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    block: () -> Unit
): () -> Unit {
    val callback = AtomicReference<OnBackPressedCallback?>(null)
    val observer = object : DefaultLifecycleObserver {
        // The callback is registered during RESUMED state,
        // because we want to register it as the last one.
        override fun onResume(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)

            val backPressedCallback = onBackPressedDispatcher.addCallback(
                owner = this@launchOnBackPressed,
                enabled = true
            ) {
                block()

                // Remove this callback, and trigger the listener again:
                remove()
                onBackPressedDispatcher.onBackPressed()
            }
            callback.set(backPressedCallback)
        }
    }
    lifecycle.addObserver(observer)
    return {
        lifecycle.removeObserver(observer)
        callback.get()?.remove()
    }
}
