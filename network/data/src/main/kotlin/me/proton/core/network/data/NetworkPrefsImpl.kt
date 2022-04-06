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
package me.proton.core.network.data

import android.content.Context
import android.content.SharedPreferences
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.android.sharedpreferences.PreferencesProvider
import me.proton.core.util.android.sharedpreferences.list
import me.proton.core.util.android.sharedpreferences.long
import me.proton.core.util.android.sharedpreferences.string

class NetworkPrefsImpl(context: Context) : NetworkPrefs, PreferencesProvider {

    override val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var activeAltBaseUrl: String? by string()
    override var lastPrimaryApiFail: Long by long(default = Long.MIN_VALUE)
    override var alternativeBaseUrls: List<String>? by list()
    override var lastAlternativesRefresh: Long by long(default = Long.MIN_VALUE)

    companion object {
        private const val PREFS_NAME = "me.proton.core.network"
    }
}
