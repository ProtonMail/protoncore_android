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

package me.proton.core.test.android.robots

import android.widget.Button
import android.widget.ImageButton
import androidx.annotation.IdRes
import me.proton.core.auth.R
import me.proton.core.test.android.instrumented.Robot
import me.proton.core.test.android.instrumented.ui.espresso.OnView

/**
 * [CoreRobot] Contains common Core specific view actions implementation
 */
open class CoreRobot : Robot {

    /**
     * Adds text to an element which has an [id] with [value]
     * @param T next Robot to be returned
     */
    inline fun <reified T> addText(@IdRes id: Int, value: String): T {
        view
            .isInputField(id)
            .typeText(value)
        return T::class.java.newInstance()
    }

    /**
     * Sets new text to an element which has an [id] with [value]
     * @param T next Robot to be returned
     */
    inline fun <reified T> replaceText(@IdRes id: Int, value: String): T {
        view
            .isInputField(id)
            .clearText()
            .typeText(value)
        return T::class.java.newInstance()
    }

    /**
     * Adds more text to an element which has an [id] with [value]
     * @param T next Robot to be returned
     */
    @Deprecated(
        "Use addText for the same functionality or replaceText to set new text.",
        ReplaceWith("addText<T>(id, value)")
    )
    inline fun <reified T> setText(@IdRes id: Int, value: String): T = addText(id, value)

    /**
     * Clicks an element with [id] of class [clazz] (default: android.widget.Button)
     * @param T next Robot to be returned
     */
    fun <T> clickElement(@IdRes id: Int, next: Class<T>, clazz: Class<*> = Button::class.java): T {
        view
            .instanceOf(clazz)
            .withId(id)
            .isEnabled()
            .click()
        return next.newInstance()
    }

    inline fun <reified T> clickElement(@IdRes id: Int, clazz: Class<*> = Button::class.java): T =
        clickElement(id, T::class.java, clazz)

    /**
     * Clicks provided [element]
     * @param T next Robot to be returned
     */
    inline fun <reified T> clickElement(element: OnView): T {
        element.click()
        return T::class.java.newInstance()
    }

    /**
     * Clicks close button
     * @param T next Robot to be returned
     */
    open fun <T> close(next: Class<T>): T {
        view
            .instanceOf(ImageButton::class.java)
            .withParent(
                view.withId(R.id.toolbar)
            )
            .click()
        return next.newInstance()
    }

    inline fun <reified T> close(): T = close(T::class.java)

    /**
     * Clicks an element with [text] of class [clazz] (default: android.widget.Button)
     * @param T next Robot to be returned
     */
    fun <T> clickElement(text: String, next: Class<T>, clazz: Class<*> = Button::class.java): T {
        view
            .instanceOf(clazz)
            .withText(text)
            .click()
        return next.newInstance()
    }

    inline fun <reified T> clickElement(text: String, clazz: Class<*> = Button::class.java): T =
        clickElement(text, T::class.java, clazz)

    /**
     * Clicks Android 'Back' button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> back(): T {
        device.clickBackBtn()
        return T::class.java.newInstance()
    }
}
