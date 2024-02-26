/*
 * Copyright (c) 2020 Proton Technologies AG
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
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import me.proton.core.presentation.utils.OnUiComponentCreatedListener
import me.proton.core.presentation.utils.UiComponent

/**
 * Base Proton Fragment from which all project fragments should extend.
 *
 * @author Dino Kadrikj.
 */
abstract class ProtonFragment : Fragment, OnUiComponentCreatedListener {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onUiComponentCreated(this, requireActivity(), this, UiComponent.UiFragment(this))
    }

    /**
     * It is a shorthand for `launchIn(viewLifecycleOwner.lifecycleScope)`.
     *
     * @see launchIn
     * @see getViewLifecycleOwner
     */
    fun Flow<*>.launchInViewLifecycleScope() = launchIn(viewLifecycleOwner.lifecycleScope)
}
