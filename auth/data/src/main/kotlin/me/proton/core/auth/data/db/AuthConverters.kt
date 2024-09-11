package me.proton.core.auth.data.db

import androidx.room.TypeConverter
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.MemberDeviceId

class AuthConverters {

    @TypeConverter
    fun fromAuthDeviceIdToString(value: AuthDeviceId?): String? = value?.id

    @TypeConverter
    fun fromStringToAuthDeviceId(value: String?): AuthDeviceId? = value?.let {
        AuthDeviceId(value)
    }

    @TypeConverter
    fun fromMemberDeviceIdToString(value: MemberDeviceId?): String? = value?.id

    @TypeConverter
    fun fromStringToMemberDeviceId(value: String?): MemberDeviceId? = value?.let {
        MemberDeviceId(value)
    }
}
