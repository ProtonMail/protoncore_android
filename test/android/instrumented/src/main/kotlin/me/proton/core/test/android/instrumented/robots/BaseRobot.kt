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

package me.proton.core.test.android.instrumented.robots

import android.widget.Button
import android.widget.EditText
import androidx.annotation.IdRes
import me.proton.core.auth.R
import me.proton.core.test.android.instrumented.CoreRobot

/**
 * [BaseRobot] Contains common Core specific view actions
 */
open class BaseRobot : CoreRobot {

    inline fun <reified T> setText(@IdRes id: Int, value: String): T {
        view
            .instanceOf(EditText::class.java)
            .withId(id)
            .typeText(value)
        return T::class.java.newInstance()
    }

    inline fun <reified T> clickElement(@IdRes id: Int, clazz: Class<*> = Button::class.java): T {
        view
            .instanceOf(clazz)
            .withId(id)
            .click()
        return T::class.java.newInstance()
    }

    inline fun <reified T> clickElement(text: String, clazz: Class<*> = Button::class.java): T {
        view
            .instanceOf(clazz)
            .withText(text)
            .click()
        return T::class.java.newInstance()
    }

    inline fun <reified T> close(): T {
        view
            .withId(R.id.closeButton)
            .click()
        return T::class.java.newInstance()
    }
}
