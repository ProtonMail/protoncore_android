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

package me.proton.core.auth.domain.entity

/**
 * Represents an Address entity (with full properties).
 *
 * @author Dino Kadrikj.
 */
data class Address(
    val id: String,
    val domainId: String? = null,
    val email: String,
    val canSend: Boolean,
    val canReceive: Boolean,
    val status: Int,
    val type: AddressType,
    val order: Int,
    val displayName: String? = null,
    val signature: String? = null,
    val hasKeys: Boolean,
    val keys: List<FullAddressKey>
)

/**
 * Represents full-property Address Key, usually returned from the API.
 */
data class FullAddressKey(
    val id: String,
    val version: Int,
    val flags: Int,
    val privateKey: String,
    val token: String? = null,
    val signature: String? = null,
    val fingerprint: String? = null,
    val fingerprints: List<String>? = null,
    val activation: String? = null,
    val primary: Boolean,
    val active: Boolean
)

/**
 * Address Type values. Currently they are in normal ordering, but we set custom value for eventual future mix/change
 * of values.
 */
enum class AddressType(val value: Int) {
    ORIGINAL(1),
    ALIAS(2),
    CUSTOM(3),
    PREMIUM(4),
    EXTERNAL(5);

    companion object {
        fun getByValue(value: Int): AddressType =
            values().find {
                value == it.value
            } ?: ORIGINAL
    }
}
