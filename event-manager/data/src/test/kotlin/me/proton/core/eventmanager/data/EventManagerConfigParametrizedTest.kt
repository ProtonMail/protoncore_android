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

package me.proton.core.eventmanager.data

import io.mockk.every
import io.mockk.mockk
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.data.extension.isFetchAllowed
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.util.android.datetime.Clock
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@RunWith(Parameterized::class)
class EventManagerConfigParameterizedTest(
    private val lastFetch: Long?,
    private val expected: Boolean,
) {

    private val userId = UserId("userId")
    private val volumeId = "volumeId"
    private val driveConfig = EventManagerConfig.Drive.Volume(userId, volumeId, 10.milliseconds)

    @Test
    fun testIsFetchAllowed() {
        // GIVEN
        val metadata = mockk<EventMetadata> {
            every { fetchedAt } returns (lastFetch)
        }
        val clock = mockk<Clock> {
            every { currentEpochMillis() } returns 100
        }

        // WHEN
        val actual = driveConfig.isFetchAllowed(metadata, clock)

        // THEN
        assertEquals(expected, actual)
    }

    companion object {
        @get:Parameterized.Parameters(name = "With last fetch {0}, is fetch allowed is {1}")
        @get:JvmStatic
        val data = listOf(
            arrayOf(null, true),
            arrayOf(0L, true),
            arrayOf(89L, true),
            arrayOf(90L, false),
            arrayOf(100L, false),
            arrayOf(111L, false),
        )
    }
}
