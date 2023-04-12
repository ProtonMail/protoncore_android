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

package me.proton.core.keytransparency.domain

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.usecase.CheckAbsenceProof
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.StoreAddressChange
import me.proton.core.user.domain.SignedKeyListChangeListener
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class SignedKeyListChangeListenerImplTest {

    private lateinit var listener: SignedKeyListChangeListenerImpl
    private val storeAddressChange = mockk<StoreAddressChange>()
    private val checkAbsenceProof = mockk<CheckAbsenceProof>()
    private val isKeyTransparencyEnabled = mockk<IsKeyTransparencyEnabled>()

    @BeforeTest
    fun setUp() {
        listener = SignedKeyListChangeListenerImpl(
            checkAbsenceProof,
            storeAddressChange,
            isKeyTransparencyEnabled
        )
    }

    @Test
    fun `onChangeRequested does nothing if KT is deactivated`() = runTest {
        // given
        val userId = UserId("test")
        val oldSKL = mockk<PublicSignedKeyList>()
        val testEmail = "kt.test@proton.me"
        val userAddress = mockk<UserAddress> {
            every { email } returns testEmail
            every { signedKeyList } returns oldSKL
        }
        coEvery { isKeyTransparencyEnabled() } returns false
        // when
        val result = listener.onSKLChangeRequested(
            userId,
            userAddress
        )
        assertIs<SignedKeyListChangeListener.Result.Success>(result)
        // then
        coVerify(exactly = 0) {
            checkAbsenceProof(userId, userAddress)
        }
    }

    @Test
    fun `onChangeRequested fails if address has no absence proof`() = runTest {
        // given
        val userId = UserId("test")
        val oldSKL = mockk<PublicSignedKeyList>()
        val testEmail = "kt.test@proton.me"
        val userAddress = mockk<UserAddress> {
            every { email } returns testEmail
            every { signedKeyList } returns oldSKL
        }
        coEvery { isKeyTransparencyEnabled() } returns true
        coEvery { checkAbsenceProof(userId, userAddress) } throws KeyTransparencyException("test: no absence proof")
        // when
        val result = listener.onSKLChangeRequested(
            userId,
            userAddress
        )
        assertIs<SignedKeyListChangeListener.Result.Failure>(result)
        assertIs<KeyTransparencyException>(result.reason)
        // then
        coVerify {
            checkAbsenceProof(userId, userAddress)
        }
    }

    @Test
    fun `onChangeAccepted stores the SKL in local storage`() = runTest {
        // given
        val userId = UserId("test")
        val oldSKL = mockk<PublicSignedKeyList>()
        val testEmail = "kt.test@proton.me"
        val testAddressId = AddressId("address-id")
        val userAddress = mockk<UserAddress> {
            every { addressId } returns testAddressId
            every { email } returns testEmail
            every { signedKeyList } returns oldSKL
        }
        coEvery { isKeyTransparencyEnabled() } returns true
        val newSKL = mockk<PublicSignedKeyList>()
        coJustRun { storeAddressChange(userId, userAddress, newSKL) }
        // when
        val result = listener.onSKLChangeAccepted(
            userId,
            userAddress,
            newSKL
        )
        // then
        coVerify {
            storeAddressChange(userId, userAddress, newSKL)
        }
        assertIs<SignedKeyListChangeListener.Result.Success>(result)
    }

    @Test
    fun `onChangeAccepted does nothing if KT is deactivated`() = runTest {
        // given
        val userId = UserId("test")
        val oldSKL = mockk<PublicSignedKeyList>()
        val testEmail = "kt.test@proton.me"
        val userAddress = mockk<UserAddress> {
            every { email } returns testEmail
            every { signedKeyList } returns oldSKL
        }
        coEvery { isKeyTransparencyEnabled() } returns false
        val newSKL = mockk<PublicSignedKeyList>()
        // when
        val result = listener.onSKLChangeAccepted(
            userId,
            userAddress,
            newSKL
        )
        // then
        coVerify(exactly = 0) {
            storeAddressChange(userId, userAddress, newSKL)
        }
        assertIs<SignedKeyListChangeListener.Result.Success>(result)
    }
}
