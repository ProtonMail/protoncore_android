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

package me.proton.core.eventmanager.data.work

import android.app.usage.UsageStatsManager
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_ACTIVE
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_FREQUENT
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_RARE
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_RESTRICTED
import android.app.usage.UsageStatsManager.STANDBY_BUCKET_WORKING_SET
import android.content.Context
import android.os.Build
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.eventmanager.data.R
import me.proton.core.presentation.app.AppLifecycleProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.seconds

@Suppress("MaxLineLength")
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class EventWorkerManagerRepeatIntervalBackgroundTest(
    private val stanbyBucket: Int,
    private val repeatIntervalBackground: Int
) {

    @get:Rule
    val mockKRule = MockKRule(this)

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var workManager: WorkManager

    @MockK
    lateinit var appLifecycleProvider: AppLifecycleProvider

    @MockK
    lateinit var usageStatsManager: UsageStatsManager

    @InjectMockKs
    lateinit var manager: EventWorkerManagerImpl
    @Before
    fun setup(){
        every { context.resources } returns mockk {
            every { getBoolean(R.bool.core_feature_event_manager_worker_repeat_internal_background_by_bucket) } returns true
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_background_seconds) } returns 1800
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_exempted_seconds) } returns 1800
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_active_seconds) } returns 1800
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_working_set_seconds) } returns 7200
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_frequent_seconds) } returns 28800
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_rare_seconds) } returns 86400
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_bucket_restricted_seconds) } returns 259200
            every { getInteger(R.integer.core_feature_event_manager_worker_repeat_internal_foreground_seconds) } returns 30
        }
        every { context.getSystemService(Context.USAGE_STATS_SERVICE) } returns usageStatsManager
        every { appLifecycleProvider.state } returns MutableStateFlow(AppLifecycleProvider.State.Background)
    }
    @Test
    fun getRepeatIntervalBackground() = runTest {
        // GIVEN
        every { usageStatsManager.appStandbyBucket } returns stanbyBucket

        // THEN
        assertEquals(repeatIntervalBackground.seconds, manager.getRepeatIntervalBackground())
    }

    companion object {
        @get:ParameterizedRobolectricTestRunner.Parameters(name = "Given {0} app standby bucket when getRepeatIntervalBackground Should returns {1}")
        @get:JvmStatic
        val data = listOf(
            arrayOf(5, 1800),
            arrayOf(STANDBY_BUCKET_ACTIVE, 1800),
            arrayOf(STANDBY_BUCKET_WORKING_SET, 7200),
            arrayOf(STANDBY_BUCKET_FREQUENT, 28800),
            arrayOf(STANDBY_BUCKET_RARE, 86400),
            arrayOf(STANDBY_BUCKET_RESTRICTED, 259200),
            arrayOf(STANDBY_BUCKET_RESTRICTED + 5, 259200),
        ).toList()
    }
}
