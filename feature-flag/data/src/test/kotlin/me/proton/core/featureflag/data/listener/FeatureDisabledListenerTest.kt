/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.featureflag.data.listener

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.domain.feature.FeatureDisabledListener
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import javax.inject.Provider
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@RunWith(Parameterized::class)
class FeatureDisabledListenerTest(
    private val minimumFetchIntervalSeconds: Long,
    private val delayBetweenOnFeatureDisabledSeconds: Long,
    private val expectedRepositoryCalls: Int,
) {
    private lateinit var featureDisabledListener: FeatureDisabledListener
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val featureFlagRepository = mockk<FeatureFlagRepository>(relaxed = true)
    private val featureFlagRepositoryProvider = Provider { featureFlagRepository }

    @Test
    fun testTwoCallsOnFeatureDisabled() = runTest {
        //Given
        featureDisabledListener = FeatureDisabledListenerImpl(
            featureFlagRepositoryProvider = featureFlagRepositoryProvider,
            sessionProvider = sessionProvider,
            minimumFetchInterval = minimumFetchIntervalSeconds.toDuration(DurationUnit.SECONDS),
            monoClock = { testScheduler.currentTime },
        )

        //When
        featureDisabledListener.onFeatureDisabled(null)
        advanceTimeBy(delayBetweenOnFeatureDisabledSeconds.toDuration(DurationUnit.SECONDS))
        featureDisabledListener.onFeatureDisabled(null)

        //Then
        coVerify(exactly = expectedRepositoryCalls) {
            featureFlagRepository.getAll(any())
        }
    }

    companion object {
        @get:Parameterized.Parameters(
            "minimumFetchIntervalSeconds: {0}, delayBetweenOnFeatureDisabledSeconds: {1}, expectedRepositoryCalls: {2}"
        )
        @get:JvmStatic
        val data = listOf(
            arrayOf(3, 2, 1),
            arrayOf(3, 4, 2),
        )
    }
}
