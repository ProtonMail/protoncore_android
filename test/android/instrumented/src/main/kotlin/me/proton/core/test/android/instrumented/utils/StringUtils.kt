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

package me.proton.core.test.android.instrumented.utils

import androidx.annotation.StringRes
import me.proton.core.test.android.instrumented.ProtonTest.Companion.getContext

object StringUtils {

    private const val letters = "abcdefghijklmnopqrstuuvwxyz"
    private const val alphaNumericWithSpecialCharacters =
        "abcdefghijklmnopqrstuuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@+_)(*&^%$#@!"
    private const val emailCharacters =
        "abcdefghijklmnopqrstuuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!#\$%&'*+-=?^_`{|}~"

    fun stringFromResource(@StringRes id: Int): String = getContext().getString(id)
    fun stringFromResource(@StringRes id: Int, arg1: String): String = getContext().getString(id, arg1)

    fun getAlphaNumericStringWithSpecialCharacters(length: Long = 10): String =
        randomString(length, alphaNumericWithSpecialCharacters)

    fun getEmailString(length: Long = 10): String =
        randomString(length, emailCharacters)

    fun randomString(stringLength: Long = 10, source: String = letters): String =
        (1..stringLength).map { source.random() }.joinToString(separator = "")
}
