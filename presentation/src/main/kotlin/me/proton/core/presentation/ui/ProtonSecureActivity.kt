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

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.protectScreen

abstract class ProtonSecureActivity<ViewBindingT: ViewBinding>(
    bindingInflater: (LayoutInflater) -> ViewBindingT
): ProtonViewBindingActivity<ViewBindingT>(bindingInflater) {

    /** Can be overridden to modify the protections applied to the extending activity */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val configuration = ProtectScreenConfiguration()

    private val screenProtector by protectScreen(configuration)

}
