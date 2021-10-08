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

import android.os.Bundle
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityAuthHelpBinding
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.openBrowserLink

/**
 * Authentication help Activity which offers common authentication problems help.
 * @author Dino Kadrikj.
 */
class AuthHelpActivity : AuthActivity<ActivityAuthHelpBinding>(ActivityAuthHelpBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener {
                finish()
            }

            helpOptionCustomerSupport.root.onClick {
                openBrowserLink(getString(R.string.contact_support_link))
            }
            helpOptionOtherIssues.root.onClick {
                openBrowserLink(getString(R.string.common_login_problems_link))
            }
            helpOptionForgotPassword.root.onClick {
                openBrowserLink(getString(R.string.forgot_password_link))
            }
            helpOptionForgotUsername.root.onClick {
                openBrowserLink(getString(R.string.forgot_username_link))
            }
        }
    }
}
