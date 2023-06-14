package me.proton.core.accountrecovery.presentation.internal

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.accountrecovery.presentation.R
import javax.inject.Inject

internal class GetNotificationId @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(): Int =
        context.resources.getInteger(R.integer.core_feature_account_recovery_notification_id)
}
