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

package me.proton.core.user.data.db

import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.user.domain.entity.AddressId
import org.junit.Assert.assertTrue
import org.junit.Test

class UserConvertersTest {

    @Test
    fun convertAddressId() {
        val commonConverters = UserConverters()

        // Test that null as a source gives null as destination
        assertTrue(commonConverters.fromStringToAddressId(null) == null)
        assertTrue(commonConverters.fromAddressIdToString(null) == null)

        // Test that empty as a source gives empty as destination
        assertTrue(commonConverters.fromStringToAddressId("") == AddressId(""))
        assertTrue(commonConverters.fromAddressIdToString(AddressId("")) == "")

        // Test inversion, f(g(x)) = x and g(f(x)) = x
        val addressId =
            AddressId("0Q-bf5ZERFG7hxj6tq9Ins5FlYr_SgbcaMqHsstVmPu0p-sgHLOkRcnjRCeVrIzfenXm-PSBHOi3P_5No5TEGQ==")
        assertTrue(commonConverters.fromStringToAddressId(commonConverters.fromAddressIdToString(addressId)) == addressId)
        val str = "l8vWAXHBQmv0u7OVtPbcqMa4iwQaBqowINSQjPrxAr-Da8fVPKUkUcqAq30_BCxj1X0nW70HQRmAa-rIvzmKUA=="
        assertTrue(commonConverters.fromAddressIdToString(commonConverters.fromStringToAddressId(str)) == str)
    }

    @Test
    fun convertKeyId() {
        val commonConverters = UserConverters()

        // Test that null as a source gives null as destination
        assertTrue(commonConverters.fromStringToKeyId(null) == null)
        assertTrue(commonConverters.fromKeyIdToString(null) == null)

        // Test that empty as a source gives empty as destination
        assertTrue(commonConverters.fromStringToKeyId("") == KeyId(""))
        assertTrue(commonConverters.fromKeyIdToString(KeyId("")) == "")

        // Test inversion, f(g(x)) = x and g(f(x)) = x
        val keyId = KeyId("0Q-bf5ZERFG7hxj6tq9Ins5FlYr_SgbcaMqHsstVmPu0p-sgHLOkRcnjRCeVrIzfenXm-PSBHOi3P_5No5TEGQ==")
        assertTrue(commonConverters.fromStringToKeyId(commonConverters.fromKeyIdToString(keyId)) == keyId)
        val str = "l8vWAXHBQmv0u7OVtPbcqMa4iwQaBqowINSQjPrxAr-Da8fVPKUkUcqAq30_BCxj1X0nW70HQRmAa-rIvzmKUA=="
        assertTrue(commonConverters.fromKeyIdToString(commonConverters.fromStringToKeyId(str)) == str)
    }
}
