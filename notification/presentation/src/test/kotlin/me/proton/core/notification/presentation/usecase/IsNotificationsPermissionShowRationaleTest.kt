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
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.notification.presentation.NotificationDataStoreProvider
import me.proton.core.notification.presentation.R
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsNotificationsPermissionShowRationaleTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var dataStoreProvider: NotificationDataStoreProvider

    @MockK
    private lateinit var resources: Resources

    private lateinit var tested: IsNotificationsPermissionShowRationaleImpl

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    private val testDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = testScope,
        produceFile = { tmpFolder.newFile("permissionDataStore.preferences_pb") }
    )

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        every { dataStoreProvider.permissionDataStore } returns testDataStore
        tested = IsNotificationsPermissionShowRationaleImpl(context, dataStoreProvider)
    }

    @Test
    fun showRationalTrue() = runTest {
        every { resources.getInteger(R.integer.core_feature_notifications_permission_show_rationale_count) } returns 1
        assertTrue(tested())
    }

    @Test
    fun showRationalFalse() = runTest {
        every { resources.getInteger(R.integer.core_feature_notifications_permission_show_rationale_count) } returns 0
        assertFalse(tested())
    }

    @Test
    fun showRationalFalseAfter1OnShowRational() = runTest {
        every { resources.getInteger(R.integer.core_feature_notifications_permission_show_rationale_count) } returns 1
        assertTrue(tested())
        tested.onShowRationale()
        assertFalse(tested())
    }

    @Test
    fun showRationalFalseAfter2OnShowRational() = runTest {
        every { resources.getInteger(R.integer.core_feature_notifications_permission_show_rationale_count) } returns 2
        assertTrue(tested())
        tested.onShowRationale()
        assertTrue(tested())
        tested.onShowRationale()
        assertFalse(tested())
    }
}
