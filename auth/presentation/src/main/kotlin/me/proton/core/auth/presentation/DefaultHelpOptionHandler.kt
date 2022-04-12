/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.auth.presentation

import androidx.appcompat.app.AppCompatActivity
import me.proton.core.presentation.utils.openBrowserLink

open class DefaultHelpOptionHandler : HelpOptionHandler {

    override fun onForgotUsername(context: AppCompatActivity) {
        context.openBrowserLink(context.getString(R.string.forgot_username_link))
    }

    override fun onForgotPassword(context: AppCompatActivity) {
        context.openBrowserLink(context.getString(R.string.forgot_password_link))
    }

    override fun onCustomerSupport(context: AppCompatActivity) {
        context.openBrowserLink(context.getString(R.string.contact_support_link))
    }

    override fun onOtherSignInIssues(context: AppCompatActivity) {
        context.openBrowserLink(context.getString(R.string.common_login_problems_link))
    }
}
