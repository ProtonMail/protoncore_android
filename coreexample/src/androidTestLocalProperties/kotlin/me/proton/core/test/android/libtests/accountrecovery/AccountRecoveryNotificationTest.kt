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

package me.proton.core.test.android.libtests.accountrecovery

import android.Manifest
import android.os.Build
import androidx.test.rule.GrantPermissionRule
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import me.proton.android.core.coreexample.MainActivity
import me.proton.core.accountmanager.data.AccountStateHandler
import me.proton.core.accountrecovery.domain.IsAccountRecoveryEnabled
import me.proton.core.accountrecovery.test.MinimalAccountRecoveryNotificationTest
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

@HiltAndroidTest
class AccountRecoveryNotificationTest : MinimalAccountRecoveryNotificationTest {
    private val grantPermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= 33) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        fusionEnabled = true,
        additionalRules = setOf(grantPermissionRule),
        beforeHilt = {
            WorkManager.initialize(it.targetContext, Configuration.Builder().build())
        }
    ) {
        every { isAccountRecoveryEnabled(any()) } returns true
        every { isNotificationsEnabled(any<UserId>()) } returns true
    }

    @Inject
    override lateinit var accountStateHandler: AccountStateHandler

    @Inject
    override lateinit var apiProvider: ApiProvider

    @Inject
    override lateinit var eventManagerProvider: EventManagerProvider

    @Inject
    override lateinit var eventMetadataRepository: EventMetadataRepository

    @Inject
    override lateinit var notificationRepository: NotificationRepository

    @Inject
    override lateinit var waitForPrimaryAccount: WaitForPrimaryAccount

    @Inject
    internal lateinit var isAccountRecoveryEnabled: IsAccountRecoveryEnabled

    @Inject
    internal lateinit var isNotificationsEnabled: IsNotificationsEnabled

    @Before
    fun setupMocks() {
        every { isAccountRecoveryEnabled(any()) } returns true
        every { isNotificationsEnabled(any<UserId>()) } returns true
    }

    override fun verifyAfterLogin() {
        // no-op
    }
}
