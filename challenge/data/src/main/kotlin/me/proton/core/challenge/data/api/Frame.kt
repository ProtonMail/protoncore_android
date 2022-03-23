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

package me.proton.core.challenge.data.api

public interface Frame {
    public val appLanguage: String
    public val timezone: String
    public val deviceName: String
    public val uid: String
    public val regionCode: String
    public val timezoneOffset: Int
    public val rooted: Boolean
    public val fontSize: String
    public val storage: Double
    public val darkMode: Boolean
    public val version: String
    public val keyDownField: List<Char>
}
