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

package me.proton.core.auth.presentation.ui.signup

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import me.proton.core.auth.presentation.R
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack

/**
 * Base fragment for all signup steps in the flow.
 */
abstract class SignupFragment<DB : ViewDataBinding> : ProtonFragment<DB>() {

    abstract fun onBackPressed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.addOnBackPressedCallback { onBackPressed() }
    }

    open fun showLoading(loading: Boolean = true) {
        // no-op
    }

    open fun showError(message: String?) {
        showLoading(false)
        binding.root.errorSnack(message = message ?: getString(R.string.auth_login_general_error))
    }
}
