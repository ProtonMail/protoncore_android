/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.presentation.utils

import android.content.Context
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Reusable
class InvalidPasswordProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var commonPasswords: Set<String>? = null

    suspend fun init() {
        readPasswords()
    }

    fun isPasswordCommon(password: String): Boolean = commonPasswords?.contains(password) ?: false

    private suspend fun readPasswords() = withContext(Dispatchers.IO) {
        if (commonPasswords == null) {
            commonPasswords = context.readFromAssets(FILE_NAME_COMMON_PASSWORDS)
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }
    }

    private fun Context.readFromAssets(resource: String): String =
        assets.open(resource).reader().use { it.readText() }

    companion object {
        const val FILE_NAME_COMMON_PASSWORDS = "ignis_10k.txt"
    }
}
