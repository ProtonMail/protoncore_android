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

package me.proton.core.data.room.db

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import org.junit.Assert.assertTrue
import org.junit.Test

class CommonConvertersTest {

    @Test
    fun convertUserId() {
        val commonConverters = CommonConverters()

        // Test that null as a source gives null as destination
        assertTrue(commonConverters.fromStringToUserId(null) == null)
        assertTrue(commonConverters.fromUserIdToString(null) == null)

        // Test that empty as a source gives empty as destination
        assertTrue(commonConverters.fromStringToUserId("") == UserId(""))
        assertTrue(commonConverters.fromUserIdToString(UserId("")) == "")

        // Test inversion, f(g(x)) = x and g(f(x)) = x
        val userId = UserId("0Q-bf5ZERFG7hxj6tq9Ins5FlYr_SgbcaMqHsstVmPu0p-sgHLOkRcnjRCeVrIzfenXm-PSBHOi3P_5No5TEGQ==")
        assertTrue(commonConverters.fromStringToUserId(commonConverters.fromUserIdToString(userId)) == userId)
        val str = "l8vWAXHBQmv0u7OVtPbcqMa4iwQaBqowINSQjPrxAr-Da8fVPKUkUcqAq30_BCxj1X0nW70HQRmAa-rIvzmKUA=="
        assertTrue(commonConverters.fromUserIdToString(commonConverters.fromStringToUserId(str)) == str)
    }

    @Test
    fun convertSessionId() {
        val commonConverters = CommonConverters()

        // Test that null as a source gives null as destination
        assertTrue(commonConverters.fromStringToSessionId(null) == null)
        assertTrue(commonConverters.fromSessionIdToString(null) == null)

        // Test that empty as a source gives empty as destination
        assertTrue(commonConverters.fromStringToSessionId("") == SessionId(""))
        assertTrue(commonConverters.fromSessionIdToString(SessionId("")) == "")

        // Test inversion, f(g(x)) = x and g(f(x)) = x
        val sessionId = SessionId("0Q-bf5ZERFG7hxj6tq9Ins5FlYr_SgbcaMqHsstVmPu0p-sgHLOkRcnjRCeVrIzfenXm-PSBHOi3P_5No5TEGQ==")
        assertTrue(commonConverters.fromStringToSessionId(commonConverters.fromSessionIdToString(sessionId)) == sessionId)
        val str = "l8vWAXHBQmv0u7OVtPbcqMa4iwQaBqowINSQjPrxAr-Da8fVPKUkUcqAq30_BCxj1X0nW70HQRmAa-rIvzmKUA=="
        assertTrue(commonConverters.fromSessionIdToString(commonConverters.fromStringToSessionId(str)) == str)
    }
}
