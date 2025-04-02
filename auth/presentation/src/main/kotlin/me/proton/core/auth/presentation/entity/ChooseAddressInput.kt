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
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import me.proton.core.crypto.android.keystore.EncryptedByteArrayParceler
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

@Parcelize
data class ChooseAddressInput constructor(
    val userId: String,
    val authSecret: ChooseAddressAuthSecret,
    val recoveryEmail: String,
    val isTwoPassModeNeeded: Boolean
) : Parcelable

@Parcelize
sealed class ChooseAddressAuthSecret : Parcelable {
    @TypeParceler<EncryptedByteArray, EncryptedByteArrayParceler>
    data class Passphrase(val passphrase: EncryptedByteArray) : ChooseAddressAuthSecret()
    data class Password(val password: EncryptedString) : ChooseAddressAuthSecret()
}
