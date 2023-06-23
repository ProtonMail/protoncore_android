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

package me.proton.core.notification.presentation.usecase

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.notification.presentation.R
import me.proton.core.notification.presentation.internal.GetNotificationId
import me.proton.core.notification.presentation.internal.GetNotificationTag
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.notification.presentation.receiver.OnDismissNotificationReceiver
import me.proton.core.notification.domain.usecase.GetNotificationChannelId
import me.proton.core.notification.domain.usecase.ShowNotificationView
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraNotificationId
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraUserId
import javax.inject.Inject

public class ShowNotificationViewImpl @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val getNotificationChannelId: GetNotificationChannelId,
    private val getNotificationId: GetNotificationId,
    private val getNotificationTag: GetNotificationTag,
    private val hasNotificationPermission: HasNotificationPermission,
    private val product: Product
) : ShowNotificationView {
    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun invoke(notification: Notification) {
        if (!hasNotificationPermission()) return
        val payload = notification.payload as? NotificationPayload.Unencrypted ?: return

        val builder = NotificationCompat.Builder(context, getNotificationChannelId())
            .setSmallIcon(getSmallIcon())
            .setContentTitle(payload.title)
            .setSubText(payload.subtitle)
            .setContentText(payload.body)
            .setWhen(notification.time)
            .setShowWhen(true)
            .setContentIntent(makeContentIntent(notification.notificationId, notification.userId))
            .setDeleteIntent(makeOnDeleteIntent(notification.notificationId, notification.userId))
            .setAutoCancel(true)
            .addExtras(
                bundleOf(
                    ExtraNotificationId to notification.notificationId.id,
                    ExtraUserId to notification.userId.id
                )
            )

        NotificationManagerCompat.from(context).notify(
            getNotificationTag(notification),
            getNotificationId(notification),
            builder.build()
        )
    }

    private fun getSmallIcon(): IconCompat =
        IconCompat.createWithResource(context, product.getSmallIconResId())

    private fun makeOnDeleteIntent(
        notificationId: NotificationId,
        userId: UserId
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        OnDismissNotificationReceiver(context, notificationId, userId),
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun makeContentIntent(
        notificationId: NotificationId,
        userId: UserId
    ): PendingIntent {
        val intent = Intent(
            // TODO activity for the alert dialog
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            // TODO pass notificationId and userId, so the receiver can cancel the notification if needed
        }

        return TaskStackBuilder.create(context)
            .apply {
                context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
                    addNextIntent(it)
                }
            }
            .addNextIntent(intent)
            .getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            .let {
                requireNotNull(it) {
                    "Received null pending intent. Make sure that PendingIntent.FLAG_NO_CREATE flag is not applied."
                }
            }
    }
}

internal fun Product.getSmallIconResId(): Int = when (this) {
    Product.Calendar -> R.drawable.ic_proton_brand_proton_calendar
    Product.Drive -> R.drawable.ic_proton_brand_proton_drive
    Product.Mail -> R.drawable.ic_proton_brand_proton_mail
    Product.Vpn -> R.drawable.ic_proton_brand_proton_vpn
    Product.Pass -> R.drawable.ic_proton_brand_proton_pass
}
