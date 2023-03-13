package me.proton.core.auth.presentation.observability

import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.observability.domain.metrics.common.UserCheckStatus

internal fun PostLoginAccountSetup.UserCheckResult.toUserCheckStatus(): UserCheckStatus =
    when (this) {
        is PostLoginAccountSetup.UserCheckResult.Error -> UserCheckStatus.failure
        is PostLoginAccountSetup.UserCheckResult.Success -> UserCheckStatus.success
    }
