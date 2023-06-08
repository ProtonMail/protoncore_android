/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.accountrecovery.presentation.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.accountrecovery.domain.AccountRecoveryState
import me.proton.core.accountrecovery.domain.GetAccountRecoveryChannelId
import me.proton.core.accountrecovery.domain.ShowNotification
import me.proton.core.accountrecovery.presentation.R
import me.proton.core.accountrecovery.presentation.internal.GetNotificationId
import me.proton.core.accountrecovery.presentation.internal.GetNotificationTag
import me.proton.core.accountrecovery.presentation.internal.HasNotificationPermission
import me.proton.core.accountrecovery.presentation.receiver.DismissNotificationReceiver
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

public class ShowNotificationImpl @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val getAccountRecoveryChannelId: GetAccountRecoveryChannelId,
    private val getNotificationId: GetNotificationId,
    private val getNotificationTag: GetNotificationTag,
    private val hasNotificationPermission: HasNotificationPermission,
    private val product: Product
) : ShowNotification {
    @SuppressLint("InlinedApi")
    @RequiresPermission(POST_NOTIFICATIONS)
    override fun invoke(forState: AccountRecoveryState, userId: UserId) {
        if (!hasNotificationPermission() || forState == AccountRecoveryState.None) return

        val notification = NotificationCompat.Builder(context, getAccountRecoveryChannelId())
            .setSmallIcon(getSmallIcon())
            .setContentTitle(context.getString(R.string.account_recovery_notification_channel_name))
            .setContentText(getContentText(forState.getContentTextResource()))
            .addAction(makeDismissAction(userId))
            .addAction(makeLearnMoreAction(userId))
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(getNotificationTag(userId), getNotificationId(), notification)
    }

    private fun getContentText(@StringRes resourceString: Int?): String? =
        if (resourceString != null) context.getString(resourceString)
        else null

    private fun getSmallIcon(): IconCompat =
        IconCompat.createWithResource(context, product.getSmallIconResId())

    private fun makeDismissAction(
        userId: UserId
    ): NotificationCompat.Action = NotificationCompat.Action.Builder(
        null,
        context.getString(R.string.account_recovery_notification_action_dismiss),
        PendingIntent.getBroadcast(
            context,
            0,
            DismissNotificationReceiver(context, userId),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private fun makeLearnMoreAction(
        userId: UserId
    ): NotificationCompat.Action {
        val intent = Intent(
            // TODO activity for the alert dialog
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            // TODO pass userId, so the receiver can cancel the notification if needed
        }

        val pendingIntent = TaskStackBuilder.create(context)
            .apply {
                context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
                    addNextIntent(it)
                }
            }
            .addNextIntent(intent)
            .getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Action.Builder(
            null,
            context.getString(R.string.account_recovery_notification_action_learn_more),
            pendingIntent
        ).build()
    }
}

public fun AccountRecoveryState.getContentTextResource(): Int? = when (this) {
    AccountRecoveryState.None -> null
    AccountRecoveryState.GracePeriod -> R.string.account_recovery_notification_content_grace_period
    AccountRecoveryState.ResetPassword -> R.string.account_recovery_notification_content_reset_password
    AccountRecoveryState.Cancelled -> R.string.account_recovery_notification_content_cancelled
}

public fun Product.getSmallIconResId(): Int = when (this) {
    Product.Calendar -> R.drawable.ic_proton_brand_proton_calendar
    Product.Drive -> R.drawable.ic_proton_brand_proton_drive
    Product.Mail -> R.drawable.ic_proton_brand_proton_mail
    Product.Vpn -> R.drawable.ic_proton_brand_proton_vpn
    Product.Pass -> R.drawable.ic_proton_brand_proton_pass
}

