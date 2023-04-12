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

package me.proton.core.keytransparency.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.entity.AddressChange
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheck
import me.proton.core.keytransparency.domain.exception.keyTransparencyCheckNotNull
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import java.util.UUID
import javax.inject.Inject

internal class StoreAddressChange @Inject constructor(
    private val verifySignedKeyListSignature: VerifySignedKeyListSignature,
    private val getVerificationPublicKeys: GetVerificationPublicKeys,
    private val getCurrentTime: GetCurrentTime,
    private val keyTransparencyRepository: KeyTransparencyRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        address: UserAddress,
        signedKeyList: PublicSignedKeyList
    ) {
        val creationTimestamp = verifySignedKeyListSignature(address, signedKeyList)
        val addressChanges = keyTransparencyRepository.getAddressChangesForAddress(userId, address.email)
        val expectedMinEpochId = keyTransparencyCheckNotNull(
            address.signedKeyList?.expectedMinEpochId
        ) { "Updated address had no expectedMinEpochId" }
        val newAddressChange = AddressChange(
            userId,
            changeId = UUID.randomUUID().toString(),
            email = address.email,
            counter = 0,
            epochId = expectedMinEpochId,
            creationTimestamp = creationTimestamp,
            publicKeys = getVerificationPublicKeys(address),
            isObsolete = false
        )
        keyTransparencyCheck(addressChanges.size <= 2) {
            "Local storage should have at most two changes for an address"
        }
        keyTransparencyRepository.removeAddressChangesForAddress(userId, address.email)
        keyTransparencyRepository.storeAddressChange(newAddressChange)
    }

    suspend operator fun invoke(
        userId: UserId,
        address: PublicAddress,
        skl: PublicSignedKeyList,
        isObsolete: Boolean = false
    ) {
        val expectedMinEpochId = requireNotNull(skl.expectedMinEpochId) { "Expected min epochID was null" }
        val creationTimestamp = if (isObsolete) {
            // For obsolescence we keep the current time for later verification
            getCurrentTime()
        } else {
            verifySignedKeyListSignature(address, skl)
        }
        val recordedChanges = keyTransparencyRepository.getAddressChangesForAddress(userId, address.email)
        recordedChanges.forEach {
            // change timestamps must be increasing
            keyTransparencyCheck(it.creationTimestamp <= creationTimestamp) {
                "Current SKL's timestamp $creationTimestamp is smaller than recorded ${it.creationTimestamp}"
            }
        }
        if (recordedChanges.none { it.creationTimestamp == creationTimestamp }) {
            /*
             * Only save the change locally if we haven't seen the SKL already
             * to avoid overflowing the storage.
             */
            keyTransparencyRepository.storeAddressChange(
                AddressChange(
                    userId = userId,
                    changeId = UUID.randomUUID().toString(),
                    email = address.email,
                    counter = recordedChanges.size,
                    epochId = expectedMinEpochId,
                    creationTimestamp = creationTimestamp,
                    publicKeys = getVerificationPublicKeys(address),
                    isObsolete = isObsolete
                )
            )
        }
    }
}
