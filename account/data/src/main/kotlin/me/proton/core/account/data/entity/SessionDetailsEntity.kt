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

package me.proton.core.account.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.deserializeOrNull

@Entity(
    primaryKeys = ["sessionId"],
    indices = [Index("sessionId")],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SessionDetailsEntity(
    val sessionId: SessionId,
    val initialEventId: String?,
    val requiredAccountType: AccountType,
    val secondFactorEnabled: Boolean,
    val twoPassModeEnabled: Boolean,
    val passphrase: EncryptedByteArray?,
    val password: EncryptedString?,
    val fido2AuthenticationOptionsJson: String? = null
) {
    fun toSessionDetails() = SessionDetails(
        initialEventId = initialEventId,
        requiredAccountType = requiredAccountType,
        secondFactorEnabled = secondFactorEnabled,
        twoPassModeEnabled = twoPassModeEnabled,
        passphrase = passphrase,
        password = password,
        fido2AuthenticationOptionsJson = fido2AuthenticationOptionsJson
    )
}
