package me.proton.core.auth.data.db

import androidx.room.TypeConverter
import me.proton.core.auth.domain.entity.AuthDeviceId

class AuthConverters {

    @TypeConverter
    fun fromAuthDeviceIdToString(value: AuthDeviceId?): String? = value?.id

    @TypeConverter
    fun fromStringToAuthDeviceId(value: String?): AuthDeviceId? = value?.let {
        AuthDeviceId(value)
    }
}
