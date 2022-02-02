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

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.auth.presentation.R
import me.proton.core.presentation.ui.ProtonSecureActivity
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.user.domain.UserManager

abstract class AuthActivity<ViewBindingT : ViewBinding>(
    bindingInflater: (LayoutInflater) -> ViewBindingT
) : ProtonSecureActivity<ViewBindingT>(bindingInflater) {

    open fun showLoading(loading: Boolean) {
        // No op
    }

    open fun onError(triggerValidation: Boolean, message: String? = null, isPotentialBlocking: Boolean = false) {
        // default no op
    }

    open fun showError(
        message: String?,
        action: String? = null,
        actionOnClick: (() -> Unit)? = null,
        useToast: Boolean = false
    ) {
        showLoading(false)
        if (!useToast) {
            binding.root.errorSnack(
                message = message ?: getString(R.string.auth_login_general_error),
                action = action,
                actionOnClick = actionOnClick
            )
        } else {
            // No action possible with Toast.
            errorToast(message ?: getString(R.string.auth_login_general_error))
        }
    }

    protected fun onUnlockUserError(error: UserManager.UnlockResult.Error) {
        when (error) {
            is UserManager.UnlockResult.Error.NoPrimaryKey -> onError(
                false,
                getString(R.string.auth_mailbox_login_error_no_primary_key)
            )
            is UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey -> onError(
                false,
                getString(R.string.auth_mailbox_login_error_primary_key_error)
            )
            is UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase -> onError(
                true,
                getString(R.string.auth_mailbox_login_error_invalid_passphrase)
            )
        }
    }

    protected fun onUserCheckFailed(
        error: PostLoginAccountSetup.UserCheckResult.Error,
        useToast: Boolean = false
    ) {
        when (val action = error.action) {
            null -> showError(
                message = error.localizedMessage,
                useToast = useToast
            )
            is UserCheckAction.OpenUrl -> showError(
                message = error.localizedMessage,
                action = action.name,
                actionOnClick = { openBrowserLink(action.url) },
                useToast = useToast
            )
        }
    }
}
