package me.proton.core.auth.presentation.compose.sso

import me.proton.core.auth.domain.entity.DeviceSecretString

public data class MemberApprovalData(
    val email: String? = null,
    val pendingDevices: List<AuthDeviceData> = emptyList(),
    val deviceSecret: DeviceSecretString? = null
)

public fun MemberApprovalData.hasValidCode(): Boolean = deviceSecret != null