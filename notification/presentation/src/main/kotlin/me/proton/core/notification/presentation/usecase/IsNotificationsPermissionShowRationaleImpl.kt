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

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.notification.domain.usecase.IsNotificationsPermissionShowRationale
import me.proton.core.notification.presentation.NotificationDataStoreProvider
import me.proton.core.notification.presentation.R
import javax.inject.Inject

public class IsNotificationsPermissionShowRationaleImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreProvider: NotificationDataStoreProvider
) : IsNotificationsPermissionShowRationale {

    private val showRationaleCount = intPreferencesKey("showRationalCount")

    private fun observeShowRationaleCount() = dataStoreProvider.permissionDataStore.data.map {
        it[showRationaleCount] ?: 0
    }

    private fun getShowRationaleCountFromConfig() = context.resources.getInteger(
        R.integer.core_feature_notifications_permission_show_rationale_count
    )

    override suspend fun invoke(): Boolean {
        return observeShowRationaleCount().first() < getShowRationaleCountFromConfig()
    }

    override suspend fun onShowRationale() {
        dataStoreProvider.permissionDataStore.edit {
            it[showRationaleCount] = it[showRationaleCount]?.plus(1) ?: 1
        }
    }
}
