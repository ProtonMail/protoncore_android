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

package me.proton.core.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.viewbinding.ViewBinding

abstract class ProtonSecureActivity<ViewBindingT: ViewBinding>(
    bindingInflater: (LayoutInflater) -> ViewBindingT
): ProtonViewBindingActivity<ViewBindingT>(bindingInflater) {

    /** Can be overridden to modify the protections applied to the extending activity */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val configuration = Configuration(preventScreenRecording = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        protectActivity()
    }

    /** Sets up the necessary protections based on [configuration] */
    private fun protectActivity() {
        if (configuration.preventScreenRecording) preventScreenRecording()
    }

    private fun preventScreenRecording() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    /** Used to configure which protections should be applied to the activity */
    class Configuration(
        /** Prevent screen capture etc. to record user password. Also masks app in app switcher. */
        val preventScreenRecording: Boolean
    )
}
