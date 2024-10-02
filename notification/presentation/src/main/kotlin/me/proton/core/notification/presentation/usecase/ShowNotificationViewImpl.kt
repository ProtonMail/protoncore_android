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
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationPayload
import me.proton.core.notification.domain.entity.isDismissible
import me.proton.core.notification.domain.usecase.GetNotificationChannelId
import me.proton.core.notification.domain.usecase.ShowNotificationView
import me.proton.core.notification.presentation.NotificationDeeplink
import me.proton.core.notification.presentation.R
import me.proton.core.notification.presentation.deeplink.DeeplinkIntentProvider
import me.proton.core.notification.presentation.internal.GetNotificationId
import me.proton.core.notification.presentation.internal.GetNotificationTag
import me.proton.core.notification.presentation.internal.HasNotificationPermission
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraNotificationId
import me.proton.core.notification.domain.usecase.ShowNotificationView.Companion.ExtraUserId
import javax.inject.Inject

public class ShowNotificationViewImpl @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val getNotificationChannelId: GetNotificationChannelId,
    private val getNotificationId: GetNotificationId,
    private val getNotificationTag: GetNotificationTag,
    private val hasNotificationPermission: HasNotificationPermission,
    private val deeplinkIntentProvider: DeeplinkIntentProvider,
    private val product: Product
) : ShowNotificationView {

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun show(
        userId: UserId,
        notificationId: Int,
        notificationTag: String,
        payload: NotificationPayload,
        dismissible: Boolean,
        build: NotificationCompat.Builder.() -> Unit
    ) {
        if (!hasNotificationPermission()) return
        if (payload !is NotificationPayload.Unencrypted) return

        val builder = NotificationCompat.Builder(context, getNotificationChannelId()).apply {
            setOngoing(!dismissible)
            setAutoCancel(dismissible)
            setSmallIcon(getSmallIcon())
            setContentTitle(payload.title)
            setSubText(payload.subtitle)
            setContentText(payload.body) // collapsed - truncated to single line
            setStyle(NotificationCompat.BigTextStyle().bigText(payload.body)) // expanded
            addExtras(bundleOf(ExtraUserId to userId.id))
        }

        build.invoke(builder)

        NotificationManagerCompat.from(context).notify(notificationTag, notificationId, builder.build())
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun invoke(
        userId: UserId,
        notificationId: Int,
        notificationTag: String,
        payload: NotificationPayload,
        contentDeeplink: String?,
        deleteDeeplink: String?,
        dismissible: Boolean
    ) {
        show(
            userId = userId,
            notificationId = notificationId,
            notificationTag = notificationTag,
            payload = payload,
            dismissible = dismissible,
            build = {
                contentDeeplink?.let { setContentIntent(makeContentIntent(contentDeeplink)) }
                deleteDeeplink?.let { setDeleteIntent(makeOnDeleteIntent(deleteDeeplink)) }
            }
        )
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun invoke(notification: Notification) {
        show(
            userId = notification.userId,
            notificationId = getNotificationId(notification),
            notificationTag = getNotificationTag(notification),
            payload = notification.payload,
            dismissible = notification.isDismissible,
            build = {
                addExtras(bundleOf(ExtraNotificationId to notification.notificationId.id))
                setWhen(notification.time * 1000L)
                setShowWhen(true)
                setContentIntent(makeContentIntent(notification))
                setDeleteIntent(makeOnDeleteIntent(notification))
            }
        )
    }

    private fun getSmallIcon(): IconCompat =
        IconCompat.createWithResource(context, product.getSmallIconResId())

    private fun makeOnDeleteIntent(
        deeplinkPath: String
    ): PendingIntent = deeplinkIntentProvider.getBroadcastPendingIntent(
        path = deeplinkPath,
        flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun makeContentIntent(
        deeplinkPath: String
    ): PendingIntent = deeplinkIntentProvider.getActivityPendingIntent(
        path = deeplinkPath,
        flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun makeOnDeleteIntent(
        notification: Notification
    ): PendingIntent = makeOnDeleteIntent(
        deeplinkPath = NotificationDeeplink.Delete.get(notification.userId, notification.notificationId),
    )

    private fun makeContentIntent(
        notification: Notification
    ): PendingIntent = makeContentIntent(
        deeplinkPath = NotificationDeeplink.Open.get(notification.userId, notification.notificationId, notification.type),
    )
}

internal fun Product.getSmallIconResId(): Int = when (this) {
    Product.Calendar -> R.drawable.ic_proton_brand_proton_calendar
    Product.Drive -> R.drawable.ic_proton_brand_proton_drive
    Product.Mail -> R.drawable.ic_proton_brand_proton_mail
    Product.Vpn -> R.drawable.ic_proton_brand_proton_vpn
    Product.Pass -> R.drawable.ic_proton_brand_proton_pass
}
