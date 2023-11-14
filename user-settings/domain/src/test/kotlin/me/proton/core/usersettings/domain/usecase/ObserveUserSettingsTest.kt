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

package me.proton.core.usersettings.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import org.junit.Test
import kotlin.test.assertEquals

class ObserveUserSettingsTest {
    private val repository = mockk<UserSettingsRepository>()
    private val observeUserSettings = ObserveUserSettings(repository)

    private val userId = UserId("user-id")

    @Test
    fun test() = runTest {
        val userSettings = mockk<UserSettings>()
        every { repository.getUserSettingsFlow(userId) } returns flowOf(
            DataResult.Success(
                ResponseSource.Local,
                userSettings
            )
        )

        val result = observeUserSettings(userId).first()

        assertEquals(DataResult.Success(ResponseSource.Local, userSettings), result)
    }
}
