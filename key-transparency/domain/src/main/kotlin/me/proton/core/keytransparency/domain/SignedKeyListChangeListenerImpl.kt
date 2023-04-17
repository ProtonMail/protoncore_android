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

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.usecase.CheckAbsenceProof
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.StoreAddressChange
import me.proton.core.user.domain.SignedKeyListChangeListener
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

public class SignedKeyListChangeListenerImpl @Inject internal constructor(
    private val checkAbsenceProof: CheckAbsenceProof,
    private val storeAddressChange: StoreAddressChange,
    private val isKeyTransparencyEnabled: IsKeyTransparencyEnabled
) : SignedKeyListChangeListener {

    override suspend fun onSKLChangeRequested(
        userId: UserId,
        userAddress: UserAddress
    ): SignedKeyListChangeListener.Result {
        if (isKeyTransparencyEnabled(userId)) {
            try {
                KeyTransparencyLogger.d("Checking KT state before SKL change.")
                // Android only creates SKL on signup
                checkAbsenceProof(userId, userAddress)
            } catch (exception: KeyTransparencyException) {
                KeyTransparencyLogger.e(exception, "Error while checking KT state before SKL generation")
                return SignedKeyListChangeListener.Result.Failure(exception)
            }
        }
        return SignedKeyListChangeListener.Result.Success
    }

    public override suspend fun onSKLChangeAccepted(
        userId: UserId,
        userAddress: UserAddress,
        skl: PublicSignedKeyList
    ): SignedKeyListChangeListener.Result {
        if (isKeyTransparencyEnabled(userId)) {
            try {
                KeyTransparencyLogger.d("Storing address change after SKL upload.")
                storeAddressChange(userId, userAddress, skl)
            } catch (exception: KeyTransparencyException) {
                KeyTransparencyLogger.e(exception, "Error while saving SKL change to local storage")
                return SignedKeyListChangeListener.Result.Failure(exception)
            }
        }
        return SignedKeyListChangeListener.Result.Success
    }
}
