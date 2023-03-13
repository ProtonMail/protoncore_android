package me.proton.core.auth.presentation.observability

import me.proton.core.observability.domain.metrics.common.UnlockUserStatus
import me.proton.core.user.domain.UserManager

internal fun UserManager.UnlockResult.toUnlockUserStatus(): UnlockUserStatus = when (this) {
    UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey -> UnlockUserStatus.noKeySaltsForPrimaryKey
    UserManager.UnlockResult.Error.NoPrimaryKey -> UnlockUserStatus.noPrimaryKey
    UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase -> UnlockUserStatus.primaryKeyInvalidPassphrase
    UserManager.UnlockResult.Success -> UnlockUserStatus.success
}
