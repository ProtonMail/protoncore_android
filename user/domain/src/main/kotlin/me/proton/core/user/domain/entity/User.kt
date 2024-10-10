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

package me.proton.core.user.domain.entity

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.extension.areAllInactive
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.hasServiceForMail
import me.proton.core.user.domain.extension.hasServiceForVpn
import me.proton.core.user.domain.extension.hasSubscriptionForMail
import me.proton.core.user.domain.extension.hasSubscriptionForVpn

/**
 * Represent an authenticated User.
 *
 * [User] without valid Session is removed from local persistence.
 */
data class User(
    val userId: UserId,
    /** Optional email address. If null, [name] is not null. */
    val email: String?,
    /** Optional name. If null, [email] is not null.  */
    val name: String?,
    /** Optional display name. */
    val displayName: String?,
    /** Currency expressed in ISO 4217 (3 letters). */
    val currency: String,
    /** Monetary credits. This value is affected by [currency]. */
    val credit: Int,
    /** User type. */
    val type: Type?,
    /** Create time in milliseconds since the epoch. */
    val createdAtUtc: Long,
    /** Used space size in Bytes. */
    val usedSpace: Long,
    /** Max space size in Bytes. */
    val maxSpace: Long,
    /** Max upload size in Bytes. */
    val maxUpload: Long,
    /** Organization member role, if any. */
    val role: Role?,
    /** Whether the user controls their own keys or not. All free users are private. */
    val private: Boolean,
    /**
     * Services flags.
     *
     * @see [hasServiceForMail]
     * @see [hasServiceForVpn]
     */
    val services: Int,
    /**
     * Subscription flags.
     *
     * @see [hasSubscriptionForMail]
     * @see [hasSubscriptionForVpn]
     */
    val subscribed: Int,
    /** Invoice delinquent. */
    val delinquent: Delinquent?,
    /** Account Recovery. */
    val recovery: UserRecovery?,
    /**
     * User Private Keys used by crypto functions (e.g. encrypt, decrypt, sign, verify).
     *
     * Example:
     * ```
     * user.useKeys(context) {
     *     val text = "text"
     *
     *     val encryptedText = encryptText(text)
     *     val signedText = signText(text)
     *
     *     val decryptedText = decryptText(encryptedText)
     *     val isVerified = verifyText(decryptedText, signedText)
     * }
     * ```
     * @see [useKeys]
     * @see [areAllInactive]
     * @see [UserManager.unlockWithPassword]
     * @see [UserManager.unlockWithPassphrase]
     * @see [UserManager.lock]
     * */
    override val keys: List<UserKey>,
    val flags: Map<String, Boolean>,
    val maxBaseSpace: Long? = null,
    val maxDriveSpace: Long? = null,
    val usedBaseSpace: Long? = null,
    val usedDriveSpace: Long? = null
) : KeyHolder

enum class Type(val value: Int) {
    Proton(1),
    Managed(2),
    External(3),
    CredentialLess(4);

    companion object {
        val map = values().associateBy { it.value }
    }
}

enum class Delinquent(val value: Int) {
    None(0),
    InvoiceAvailable(1),
    InvoiceOverdue(2),
    InvoiceDelinquent(3),
    InvoiceMailDisabled(4);

    companion object {
        val map = values().associateBy { it.value }
    }
}

enum class Role(val value: Int) {
    NoOrganization(0),
    OrganizationMember(1),
    OrganizationAdmin(2);

    companion object {
        val map = values().associateBy { it.value }
    }
}
