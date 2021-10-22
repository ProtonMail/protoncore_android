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

package me.proton.core.plan.presentation.ui

import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import me.proton.core.presentation.ui.ProtonFragment

abstract class BasePlansFragment : ProtonFragment {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    protected fun close() {
        parentFragmentManager.setFragmentResult(
            KEY_PLAN_SELECTED, bundleOf(BUNDLE_KEY_PLAN to null)
        )
    }

    companion object {
        const val KEY_PLAN_SELECTED = "key.plan_selected"
        const val BUNDLE_KEY_PLAN = "bundle.plan"
        const val BUNDLE_KEY_BILLING_DETAILS = "bundle.billing_details"
        const val ARG_INPUT = "arg.plansInput"
    }
}
