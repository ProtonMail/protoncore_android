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

package me.proton.core.auth.presentation.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.android.core.presentation.utils.errorSnack
import me.proton.core.auth.presentation.R

abstract class AuthActivity<DB : ViewDataBinding> : ProtonActivity<DB>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    open fun showLoading(loading: Boolean) {
        // No op
    }

    open fun showError(message: String?) {
        showLoading(false)
        binding.root.errorSnack(message = message ?: getString(R.string.auth_login_general_error))
    }
}
