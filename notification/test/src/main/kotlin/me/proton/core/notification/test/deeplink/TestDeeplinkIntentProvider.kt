/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.notification.test.deeplink

import android.app.PendingIntent
import android.content.Intent
import me.proton.core.notification.presentation.deeplink.DeeplinkIntentProvider

public class TestDeeplinkIntentProvider : DeeplinkIntentProvider {

    override fun getActivityIntent(path: String): Intent = Intent().setPath(path)

    override fun getBroadcastIntent(path: String): Intent = Intent().setPath(path)

    override fun getActivityPendingIntent(path: String, requestCode: Int, flags: Int): PendingIntent {
        throw NotImplementedError("No PendingIntent available for tests. Use getActivityIntent.")
    }

    override fun getBroadcastPendingIntent(path: String, requestCode: Int, flags: Int): PendingIntent {
        throw NotImplementedError("No PendingIntent available for tests. Use getBroadcastIntent.")
    }
}
