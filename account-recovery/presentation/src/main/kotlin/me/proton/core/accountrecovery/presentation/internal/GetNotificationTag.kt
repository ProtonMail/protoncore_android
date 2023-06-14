package me.proton.core.accountrecovery.presentation.internal

import me.proton.core.domain.entity.UserId
import javax.inject.Inject

internal class GetNotificationTag @Inject constructor() {
    operator fun invoke(userId: UserId): String = "accountRecovery-${userId.id}"
}
