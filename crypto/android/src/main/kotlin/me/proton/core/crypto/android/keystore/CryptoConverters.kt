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

package me.proton.core.crypto.android.keystore

import android.os.Parcel
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import kotlinx.parcelize.Parceler
import me.proton.core.crypto.common.keystore.EncryptedByteArray

/**
 * Crypto [TypeConverter] for [RoomDatabase].
 */
class CryptoConverters {

    @TypeConverter
    fun fromEncryptedByteArrayToByteArray(value: EncryptedByteArray?): ByteArray? =
        value?.array

    @TypeConverter
    fun fromByteArrayToEncryptedByteArray(value: ByteArray?): EncryptedByteArray? =
        value?.let { EncryptedByteArray(it) }
}

object EncryptedByteArrayParceler : Parceler<EncryptedByteArray> {
    override fun EncryptedByteArray.write(parcel: Parcel, flags: Int) {
        parcel.writeInt(array.size)
        parcel.writeByteArray(array)
    }

    override fun create(parcel: Parcel): EncryptedByteArray {
        val size = parcel.readInt()
        val buf = ByteArray(size)
        parcel.readByteArray(buf)
        return EncryptedByteArray(buf)
    }
}
