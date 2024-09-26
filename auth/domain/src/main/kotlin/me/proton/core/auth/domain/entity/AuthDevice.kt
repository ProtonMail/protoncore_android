package me.proton.core.auth.domain.entity

import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.StringEnum
import me.proton.core.user.domain.entity.AddressId

data class AuthDeviceId(val id: String)

data class AuthDevice(
    val userId: UserId,
    val deviceId: AuthDeviceId,
    val addressId: AddressId?,
    val state: AuthDeviceState,
    val name: String,
    val localizedClientName: String,
    val platform: StringEnum<AuthDevicePlatform>?,
    val createdAtUtcSeconds: Long,
    val activatedAtUtcSeconds: Long?,
    val rejectedAtUtcSeconds: Long?,
    val activationToken: EncryptedMessage?,
    val lastActivityAtUtcSeconds: Long
)

enum class AuthDeviceState(val value: Int) {
    Inactive(0),
    Active(1),
    PendingActivation(2),
    PendingAdminActivation(3),
    Rejected(4),
    NoSession(5);

    companion object {
        val map = entries.associateBy { it.value }
    }
}

enum class AuthDevicePlatform(val value: String) {
    Android("Android"),
    AndroidTv("AndroidTv"),
    AppleTv("AppleTv"),
    IOS("iOS"),
    Linux("Linux"),
    MacOS("macOS"),
    Web("Web"),
    Windows("Windows");

    companion object {
        val map = entries.associateBy { it.value }
        fun enumOf(value: String?) = value?.let { StringEnum(it, map[it]) }
    }
}
