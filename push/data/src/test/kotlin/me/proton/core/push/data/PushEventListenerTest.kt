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

package me.proton.core.push.data

import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Action
import me.proton.core.eventmanager.domain.entity.Event
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.push.data.local.db.PushDatabase
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.local.PushLocalDataSource
import me.proton.core.push.domain.repository.PushRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PushEventListenerTest {
    private lateinit var pushDatabase: PushDatabase
    private lateinit var pushLocalDataSource: PushLocalDataSource
    private lateinit var pushRepository: PushRepository
    private lateinit var tested: PushEventListener

    @BeforeTest
    fun setUp() {
        pushDatabase = mockk()
        pushLocalDataSource = mockk()
        pushRepository = mockk()
        tested = PushEventListener(pushDatabase, pushLocalDataSource, pushRepository)
    }

    @Test
    fun `no push events`() = runBlockingTest {
        val userId = UserId("test-user-id")
        val config = EventManagerConfig.Core(userId)
        val response = EventsResponse("""{ "More": 0 }""")

        assertNull(tested.deserializeEvents(config, response))
    }

    @Test
    fun `empty push events`() = runBlockingTest {
        val userId = UserId("test-user-id")
        val config = EventManagerConfig.Core(userId)
        val response = EventsResponse("""{ "Pushes": [] }""")

        assertEquals(true, tested.deserializeEvents(config, response)?.isEmpty())
    }

    @Test
    fun `null push events`() = runBlockingTest {
        val userId = UserId("test-user-id")
        val config = EventManagerConfig.Core(userId)
        val response = EventsResponse("""{ "Pushes": null }""")

        assertNull(tested.deserializeEvents(config, response))
    }

    @Test
    fun `create event`() = runBlockingTest {
        val userId = UserId("test-user-id")
        val config = EventManagerConfig.Core(userId)
        val response = EventsResponse(
            """
            {
              "Pushes": [
                {
                  "ID": "c-qng5Bet_qx9rN10sx8RXYed_8G4ANEiKEGbHAR_V6jrZt2cP-MUwX94i8Y0MWk0G6m02zeNBZa0h4Ks2eWVA==",
                  "Action": 1,
                  "Push": {
                    "ID": "c-qng5Bet_qx9rN10sx8RXYed_8G4ANEiKEGbHAR_V6jrZt2cP-MUwX94i8Y0MWk0G6m02zeNBZa0h4Ks2eWVA==",
                    "ObjectID": "NnkPBpCBiKvVlC8d-Os-ILMGUayTnLbx-PLegYl8Osuzp-jwdfhNk61DJgx1_w22PXoqye2n48iEV3ho-ScNHA==",
                    "Type": "Messages"
                  }
                }
              ]
            }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                Event(
                    Action.Create,
                    "c-qng5Bet_qx9rN10sx8RXYed_8G4ANEiKEGbHAR_V6jrZt2cP-MUwX94i8Y0MWk0G6m02zeNBZa0h4Ks2eWVA==",
                    Push(
                        userId,
                        PushId("c-qng5Bet_qx9rN10sx8RXYed_8G4ANEiKEGbHAR_V6jrZt2cP-MUwX94i8Y0MWk0G6m02zeNBZa0h4Ks2eWVA=="),
                        "NnkPBpCBiKvVlC8d-Os-ILMGUayTnLbx-PLegYl8Osuzp-jwdfhNk61DJgx1_w22PXoqye2n48iEV3ho-ScNHA==",
                        "Messages"
                    )
                )
            ),
            tested.deserializeEvents(config, response)
        )
    }

    @Test
    fun `delete event`() = runBlockingTest {
        val userId = UserId("test-user-id")
        val config = EventManagerConfig.Core(userId)
        val response = EventsResponse(
            """
            {
              "Pushes": [
                {
                  "ID": "c-qng5Bet_qx9rN10sx8RXYed_8G4ANEiKEGbHAR_V6jrZt2cP-MUwX94i8Y0MWk0G6m02zeNBZa0h4Ks2eWVA==",
                  "Action": 0
                }
              ]
            }
            """.trimIndent()
        )

        assertContentEquals(
            listOf(
                Event<String, Push>(
                    Action.Delete,
                    "c-qng5Bet_qx9rN10sx8RXYed_8G4ANEiKEGbHAR_V6jrZt2cP-MUwX94i8Y0MWk0G6m02zeNBZa0h4Ks2eWVA==",
                    entity = null
                )
            ),
            tested.deserializeEvents(config, response)
        )
    }
}
