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

package me.proton.android.core.coreexample.hilttests.accountrecovery

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
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
import me.proton.core.test.quark.Quark
import me.proton.test.fusion.FusionConfig
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.BeforeTest

@HiltAndroidTest
class AccountRecoveryNotificationTest : MinimalAccountRecoveryNotificationTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= 33) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 10)
    val composeTestRule: ComposeTestRule = createAndroidComposeRule<MainActivity>().apply {
        FusionConfig.Compose.testRule.set(this)
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
    override lateinit var quark: Quark

    @Inject
    internal lateinit var isAccountRecoveryEnabled: IsAccountRecoveryEnabled

    @Inject
    internal lateinit var isNotificationsEnabled: IsNotificationsEnabled

    @BeforeTest
    override fun prepare() {
        hiltRule.inject()

        every { isAccountRecoveryEnabled.invoke(any<UserId>()) } returns true
        every { isNotificationsEnabled.invoke(any<UserId>()) } returns true

        super.prepare()
    }

    override fun verifyAfterLogin() {
        // no-op
    }
}
