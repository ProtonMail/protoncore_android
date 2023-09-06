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

package me.proton.core.notification.data.repository

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.FetcherResult
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import me.proton.core.data.arch.buildProtonStore
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.Notification
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.domain.repository.NotificationLocalDataSource
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class NotificationRepositoryImpl @Inject constructor(
    private val localDataSource: NotificationLocalDataSource,
    scopeProvider: CoroutineScopeProvider
) : NotificationRepository {

    private val store: Store<UserId, List<Notification>> = StoreBuilder.from(
        fetcher = Fetcher.ofResult {
            FetcherResult.Error.Message("Not yet implemented.")
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: UserId ->
                localDataSource.observeAllNotificationsByUser(key)
            },
            writer = { _, notifications: List<Notification> ->
                localDataSource.upsertNotifications(*notifications.toTypedArray())
            }
        ),
    )
        .buildProtonStore(scopeProvider)

    override suspend fun getNotificationById(
        userId: UserId,
        notificationId: NotificationId,
        refresh: Boolean
    ): Notification? {
        val result = if (refresh) store.fresh(userId) else store.get(userId)
        return result.firstOrNull { it.notificationId == notificationId }
    }

    override suspend fun getAllNotificationsByUser(userId: UserId, refresh: Boolean): List<Notification> =
        if (refresh) store.fresh(userId) else store.get(userId)

    override fun observeAllNotificationsByUser(userId: UserId, refresh: Boolean): Flow<List<Notification>> =
        store.stream(StoreRequest.cached(userId, refresh)).mapNotNull { it.dataOrNull() }

    override suspend fun deleteAllNotificationsByUser(userId: UserId) {
        localDataSource.deleteAllNotificationsByUser(userId)
        store.clear(userId)
    }

    override suspend fun deleteNotificationById(userId: UserId, notificationId: NotificationId) {
        localDataSource.getNotificationById(userId, notificationId)?.let {
            localDataSource.deleteNotificationsById(userId, notificationId)
            store.clear(userId)
        }
    }

    override suspend fun upsertNotifications(vararg notifications: Notification) {
        localDataSource.upsertNotifications(*notifications)
        notifications.map { it.userId }.toSet().forEach {
            store.clear(it)
        }
    }
}
