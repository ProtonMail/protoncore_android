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
import android.view.View
import androidx.databinding.ViewDataBinding
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.android.core.presentation.utils.errorSnack
import me.proton.core.auth.presentation.R

/**
 * Delegate class implementing the [AuthActivity] interface.
 *
 * @author Dino Kadrikj.
 */
class AuthActivityDelegate<DB : ViewDataBinding> : AuthActivityComponent<DB> {

    /** A reference to the Activity that will handle the rotation */
    private lateinit var activity : ProtonActivity<DB>

    override fun initializeAuth(protonAuthActivity: ProtonActivity<DB>) {
        activity = protonAuthActivity

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun showLoading(loading: Boolean) {
        // noop
    }

    override fun showError(message: String?) {
        showLoading(false)
        activity.binding.root.errorSnack(message = message ?: activity.getString(R.string.auth_login_general_error))
    }

}
