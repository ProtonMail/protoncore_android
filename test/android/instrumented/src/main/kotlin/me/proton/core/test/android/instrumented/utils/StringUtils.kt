/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */

package me.proton.core.test.android.instrumented.utils

import androidx.annotation.StringRes
import me.proton.core.test.android.instrumented.CoreTest.Companion.targetContext

object StringUtils {

    fun stringFromResource(@StringRes id: Int): String = targetContext.getString(id)
    fun stringFromResource(@StringRes id: Int, arg1: String): String = targetContext.getString(id, arg1)

    fun getAlphaNumericStringWithSpecialCharacters(length: Long = 10): String {
        val source = "abcdefghijklmnopqrstuuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@+_)(*&^%$#@!"
        return (1..length).map { source.random() }.joinToString { "" }
    }

    fun getEmailString(length: Long = 10): String {
        val source = "abcdefghijklmnopqrstuuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!#\$%&'*+-=?^_`{|}~"
        return (1..length).map { source.random() }.joinToString { "" }
    }
}
