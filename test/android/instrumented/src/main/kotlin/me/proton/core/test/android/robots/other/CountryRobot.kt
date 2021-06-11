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

package me.proton.core.test.android.robots.other

import android.widget.TextView
import me.proton.core.humanverification.R
import me.proton.core.test.android.robots.CoreRobot

/**
 * [CountryRobot] class contains country picker actions and verifications implementation
 */
class CountryRobot : CoreRobot() {

    /**
     * Sets the text of a country search field
     * @return [CountryRobot]
     */
    fun search(text: String): CountryRobot = setText(R.id.search_src_text, text)

    /**
     * Clicks a text view element with text [country]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> selectCountry(country: String?): T = clickElement(country!!, TextView::class.java)
}
