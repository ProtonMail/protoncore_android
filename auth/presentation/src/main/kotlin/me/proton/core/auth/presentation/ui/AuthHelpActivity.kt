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
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.databinding.ActivityAuthHelpBinding
import me.proton.core.auth.presentation.HelpOptionHandler
import me.proton.core.presentation.utils.onClick
import javax.inject.Inject

@AndroidEntryPoint
class AuthHelpActivity : AuthActivity<ActivityAuthHelpBinding>(ActivityAuthHelpBinding::inflate) {

    @Inject
    lateinit var helpOptionHandler: HelpOptionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener {
                finish()
            }

            helpOptionCustomerSupport.root.onClick {
                helpOptionHandler.onCustomerSupport(this@AuthHelpActivity)
            }

            helpOptionOtherIssues.root.onClick {
                helpOptionHandler.onOtherSignInIssues(this@AuthHelpActivity)
            }
            helpOptionForgotPassword.root.onClick {
                helpOptionHandler.onForgotPassword(this@AuthHelpActivity)
            }
            helpOptionForgotUsername.root.onClick {
                helpOptionHandler.onForgotUsername(this@AuthHelpActivity)
            }
        }
    }
}
