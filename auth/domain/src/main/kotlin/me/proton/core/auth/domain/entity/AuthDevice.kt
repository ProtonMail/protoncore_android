package me.proton.core.auth.domain.entity

import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

data class AuthDeviceId(val id: String)

data class AuthDevice(
    val userId: UserId,
    val deviceId: AuthDeviceId,
    val addressId: AddressId,
    val state: AuthDeviceState,
    val name: String,
    val localizedClientName: String,
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
    Rejected(4);

    companion object {
        val map = entries.associateBy { it.value }
    }
}
