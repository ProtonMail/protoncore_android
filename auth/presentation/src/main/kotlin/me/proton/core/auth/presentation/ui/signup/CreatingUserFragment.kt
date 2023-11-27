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

package me.proton.core.auth.presentation.ui.signup

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import me.proton.core.auth.presentation.R
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed

@ProductMetrics(
    group = "account.any.signup",
    flow = "mobile_signup_full"
)
@ScreenDisplayed(
    event = "fe.create_account.displayed"
)
class CreatingUserFragment : SignupFragment(R.layout.fragment_creating_user) {
    override fun onBackPressed() {
        setFragmentResult(FRAGMENT_RESULT_REQUEST_KEY, bundleOf(KEY_CANCELLED to true))
        parentFragmentManager.popBackStack()
    }

    internal companion object {
        const val FRAGMENT_RESULT_REQUEST_KEY = "CreatingUserFragment.requestKey"
        const val KEY_CANCELLED = "key.cancelled"
    }
}
