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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.presentation.utils.openBrowserLink
import org.junit.After
import org.junit.Before
import org.junit.Test

class DefaultHelpOptionHandlerTest {
    private val tested = DefaultHelpOptionHandler()

    private val context = mockk<AppCompatActivity>()

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.presentation.utils.UiUtilsKt")
        every { context.openBrowserLink(any()) } returns Unit
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.presentation.utils.UiUtilsKt")
    }

    @Test
    fun `cs link working correctly`() {
        val customerSupportLink = "link-cs"
        every { context.getString(R.string.contact_support_link) } returns customerSupportLink
        tested.onCustomerSupport(context)
        verify { context.openBrowserLink(customerSupportLink) }
    }

    @Test
    fun `forgot username link working correctly`() {
        val forgotUsernameLink = "link-forgot-username"
        every { context.getString(R.string.forgot_username_link) } returns forgotUsernameLink
        tested.onForgotUsername(context)
        verify { context.openBrowserLink(forgotUsernameLink) }
    }

    @Test
    fun `forgot password link working correctly`() {
        val forgotPasswordLink = "link-forgot-password"
        every { context.getString(R.string.forgot_password_link) } returns forgotPasswordLink
        tested.onForgotPassword(context)
        verify { context.openBrowserLink(forgotPasswordLink) }
    }

    @Test
    fun `other sign in issues link working correctly`() {
        val otherIssuesLink = "link-other-issues"
        every { context.getString(R.string.common_login_problems_link) } returns otherIssuesLink
        tested.onOtherSignInIssues(context)
        verify { context.openBrowserLink(otherIssuesLink) }
    }
}
