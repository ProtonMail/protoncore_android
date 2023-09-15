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

package me.proton.core.notification.presentation.deeplink

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

public interface DeeplinkIntentProvider {

    /**
     * Set [path] using [Intent.setData] with [DeeplinkManager.uriPrefix].
     */
    public fun Intent.setPath(path: String): Intent = apply {
        data = Uri.parse("${DeeplinkManager.uriPrefix}/$path")
    }

    /**
     * Return a main [Activity] Intent.
     *
     * Note: Use [getBroadcastIntent] for any non-UI operation in your [DeeplinkManager.register] action.
     *
     * @see [DeeplinkManager.register]
     * @see [DeeplinkManager.handle]
     */
    public fun getActivityIntent(path: String): Intent

    /**
     * Return a [DeeplinkBroadcastReceiver] Intent.
     *
     * Note: You cannot launch a popup dialog in your [DeeplinkManager.register] action.
     *
     * @see [DeeplinkManager.register]
     * @see [DeeplinkManager.handle]
     */
    public fun getBroadcastIntent(path: String): Intent

    /**
     * Return a [PendingIntent] for a main [Activity].
     *
     * Note: Use [getBroadcastIntent] for any non-UI operation in your [DeeplinkManager.register] action.
     *
     * @see [DeeplinkManager.register]
     * @see [DeeplinkManager.handle]
     */
    public fun getActivityPendingIntent(path: String, requestCode: Int = 0, flags: Int = 0): PendingIntent

    /**
     * Return a [PendingIntent] for [DeeplinkBroadcastReceiver].
     *
     * Note: You cannot launch a popup dialog in your [DeeplinkManager.register] action.
     *
     * @see [DeeplinkManager.register]
     * @see [DeeplinkManager.handle]
     */
    public fun getBroadcastPendingIntent(path: String, requestCode: Int = 0, flags: Int = 0): PendingIntent
}

public class DeeplinkIntentProviderImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : DeeplinkIntentProvider {

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    override fun getActivityIntent(
        path: String,
    ): Intent {
        // Returns a "good" intent to launch a front-door activity in a package.
        // The current implementation looks first for a main activity.
        val intent = requireNotNull(packageManager.getLaunchIntentForPackage(packageName))
        return intent
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .setPath(path)
    }

    override fun getBroadcastIntent(
        path: String
    ): Intent {
        val intent = Intent(context, DeeplinkBroadcastReceiver::class.java)
        return intent.setPath(path)
    }

    override fun getActivityPendingIntent(
        path: String,
        requestCode: Int,
        flags: Int
    ): PendingIntent {
        return PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ requestCode,
            /* intent = */ getActivityIntent(path),
            /* flags = */ flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getBroadcastPendingIntent(
        path: String,
        requestCode: Int,
        flags: Int
    ): PendingIntent {
        return PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ requestCode,
            /* intent = */ getBroadcastIntent(path),
            /* flags = */ flags or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
