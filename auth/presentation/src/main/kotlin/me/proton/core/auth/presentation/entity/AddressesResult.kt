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

package me.proton.core.auth.presentation.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.proton.core.auth.domain.entity.Address
import me.proton.core.auth.domain.entity.AddressType
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.entity.FullAddressKey

/**
 * @author Dino Kadrikj.
 */
@Parcelize
data class AddressesResult(
    val addresses: List<AddressResult>
) : Parcelable {

    companion object {
        fun from(addresses: Addresses) = AddressesResult(
            addresses = addresses.addresses.map { AddressResult.from(it) }
        )
    }
}

@Parcelize
data class AddressResult(
    val id: String,
    val domainId: String?,
    val email: String,
    val canSend: Boolean,
    val canReceive: Boolean,
    val status: Int,
    val type: AddressType,
    val order: Int,
    val displayName: String?,
    val signature: String?,
    val hasKeys: Boolean,
    val keys: List<FullAddressKeyResult>
) : Parcelable {
    companion object {
        fun from(address: Address) = AddressResult(
            id = address.id,
            domainId = address.domainId,
            email = address.email,
            canSend = address.canSend,
            canReceive = address.canReceive,
            status = address.status,
            type = address.type,
            order = address.order,
            displayName = address.displayName,
            signature = address.signature,
            hasKeys = address.hasKeys,
            keys = address.keys.map { FullAddressKeyResult.from(it) }
        )
    }
}

@Parcelize
data class FullAddressKeyResult(
    val id: String,
    val version: Int,
    val flags: Int,
    val privateKey: String,
    val token: String?,
    val signature: String? = null,
    val fingerprint: String? = null,
    val fingerprints: List<String>? = null,
    val activation: String? = null,
    val primary: Boolean,
    val active: Boolean
) : Parcelable {
    companion object {
        fun from(fullAddressKey: FullAddressKey) = FullAddressKeyResult(
            id = fullAddressKey.id,
            version = fullAddressKey.version,
            flags = fullAddressKey.flags,
            primary = fullAddressKey.primary,
            privateKey = fullAddressKey.privateKey,
            token = fullAddressKey.token,
            signature = fullAddressKey.signature,
            fingerprint = fullAddressKey.fingerprint,
            fingerprints = fullAddressKey.fingerprints,
            activation = fullAddressKey.activation,
            active = fullAddressKey.active
        )
    }
}
