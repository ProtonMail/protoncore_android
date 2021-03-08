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

package me.proton.core.test.android.instrumented.robots.login

import android.widget.EditText
import me.proton.core.test.android.instrumented.R
import me.proton.core.test.android.instrumented.robots.BaseRobot
import me.proton.core.test.android.instrumented.robots.BaseVerify

open class UsernameSetupRobot : BaseRobot() {

    fun username(username: String): UsernameSetupRobot = setText(R.id.usernameInput, username)
    fun next(): UsernameSetupRobot = clickElement(R.id.nextButton)
    inline fun <reified T> createAddress(): T = clickElement(R.id.createAddressButton)
    inline fun <reified T> createProtonMailAddress(username: String): T = username(username).next().createAddress()

    class Verify : BaseVerify() {
        fun usernameSetupElementsDisplayed() {
            view.withId(R.id.nextButton).wait().checkDisplayed()
            view.withId(R.id.usernameInput).instanceOf(EditText::class.java).wait().checkDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
