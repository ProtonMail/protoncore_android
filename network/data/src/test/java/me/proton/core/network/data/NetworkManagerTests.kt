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

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.network.data.util.MockNetworkManager
import me.proton.core.network.domain.NetworkStatus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class NetworkManagerTests {

    private lateinit var networkManager: MockNetworkManager

    @BeforeTest
    fun before() {
        networkManager = MockNetworkManager()
    }

    @Test
    fun `test multiple observers`() = runBlockingTest {
        val stateFlow = networkManager.observe()
        val collectedStates1 = mutableListOf<NetworkStatus>()
        val collectedStates2 = mutableListOf<NetworkStatus>()

        val flow1 = launch { stateFlow.toList(collectedStates1) }

        networkManager.networkStatus = NetworkStatus.Unmetered

        val flow2 = launch { stateFlow.toList(collectedStates2) }

        networkManager.networkStatus = NetworkStatus.Metered
        networkManager.networkStatus = NetworkStatus.Disconnected
        flow1.cancel()

        assertTrue(networkManager.registered)

        networkManager.networkStatus = NetworkStatus.Unmetered
        flow2.cancel()

        assertEquals(listOf(NetworkStatus.Unmetered, NetworkStatus.Metered, NetworkStatus.Disconnected),
            collectedStates1.toList())
        assertEquals(listOf(NetworkStatus.Metered, NetworkStatus.Disconnected, NetworkStatus.Unmetered),
            collectedStates2.toList())

        assertFalse(networkManager.registered)
    }
}
