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

package me.proton.core.keytransparency.domain.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.entity.AddressChange
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.EpochId
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.SelfAuditResult
import me.proton.core.keytransparency.domain.entity.VerifiedEpoch
import me.proton.core.network.domain.ApiException
import me.proton.core.user.domain.entity.AddressId

/**
 * Repository for data operations regarding key transparency.
 */
@SuppressWarnings("TooManyFunctions")
public interface KeyTransparencyRepository {


    /**
     * Fetch the proof from the server
     * for a given [epochId] and [email]
     *
     * @param userId: the id of the user
     * @param epochId: the epoch id for which the proof is for
     * @param email: the email for which the proof is for
     *
     * @throws ApiException if the epoch is too old or not existent
     *
     * @return the pair of proofs (regular and catchall)
     */
    public suspend fun getProof(
        userId: UserId,
        epochId: EpochId,
        email: String
    ): ProofPair

    /**
     * Fetch the epoch from the server
     * for a given [epochId]
     *
     * @param userId: the id of the user
     * @param epochId: the epoch id for which the proof is for
     *
     * @throws ApiException if the epoch is too old or not existent
     * @return the epoch
     */
    public suspend fun getEpoch(userId: UserId, epochId: EpochId): Epoch

    /**
     * Fetch the most recent epoch from the server
     *
     * @param userId: the id of the user
     * @return the most recent epoch
     */
    public suspend fun getLastEpoch(userId: UserId): Epoch

    /**
     * Fetch the verified epoch
     *
     * @param userId: the id of the user
     * @param addressId: the id of the address associated to the verified epoch
     *
     * @throws ApiException if the epoch doesn't exit
     * @return the verified epoch
     */
    public suspend fun getVerifiedEpoch(userId: UserId, addressId: AddressId): VerifiedEpoch

    /**
     * Upload a new verified epoch for the address
     *
     * @param userId: the id of the user
     * @param addressId: the id of the address associated to the verified epoch
     */
    public suspend fun uploadVerifiedEpoch(
        userId: UserId,
        addressId: AddressId,
        verifiedEpoch: VerifiedEpoch
    )

    /**
     * Record the address change locally
     *
     * @param addressChange: the address change to store
     */
    public suspend fun storeAddressChange(addressChange: AddressChange)


    /**
     * Remove the recorded address change locally
     *
     * @param addressChange: the address change to remove
     */
    public suspend fun removeAddressChange(addressChange: AddressChange)

    /**
     * Remove all the recorded address change locally
     * for the provided email.
     *
     * @param userId: the id of the user
     * @param email: the targeted email.
     */
    public suspend fun removeAddressChangesForAddress(userId: UserId, email: String)

    /**
     * Fetch all recorded changes for a user.
     *
     * @param userId: the id of the user
     *
     * @return the list of address changes recorded
     */
    public suspend fun getAllAddressChanges(userId: UserId): List<AddressChange>

    /**
     * Fetch all recorded changes for a user and a given address
     *
     * @param userId: the id of the user
     * @param email: the email of the address
     *
     * @return the list of address changes recorded
     */
    public suspend fun getAddressChangesForAddress(userId: UserId, email: String): List<AddressChange>

    /**
     * Get the recorded timestamp for the last run of self audit
     * for the given user.
     *
     * @param userId: the id of the user
     *
     * @return the timestamp, or null if self audit was not run.
     */
    public suspend fun getTimestampOfSelfAudit(userId: UserId): Long?

    /**
     * Stores the result of self audit locally
     *
     * @param userId: the id of the user
     * @param result: the result of self audit
     *
     */
    public suspend fun storeSelfAuditResult(userId: UserId, result: SelfAuditResult)
}
