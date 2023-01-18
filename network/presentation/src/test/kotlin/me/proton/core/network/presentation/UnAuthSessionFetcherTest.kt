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

package me.proton.core.network.presentation

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.session.unauth.OpportunisticUnAuthTokenRequest
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DefaultCoroutineScopeProvider
import me.proton.core.util.kotlin.DefaultDispatcherProvider
import org.junit.Before
import org.junit.Test

internal class UnAuthSessionFetcherTest : ArchTest by ArchTest(), CoroutinesTest by UnconfinedCoroutinesTest() {

    lateinit var unAuthSessionFetcher: UnAuthSessionFetcher
    private lateinit var scopeProvider: CoroutineScopeProvider
    private val opportunisticUnAuthTokenRequest = mockk<OpportunisticUnAuthTokenRequest>(relaxed = true)

    @Before
    fun beforeEveryTest() {
        scopeProvider = DefaultCoroutineScopeProvider(DefaultDispatcherProvider())
        unAuthSessionFetcher = UnAuthSessionFetcher(scopeProvider, opportunisticUnAuthTokenRequest)
    }

    @Test
    fun `use case correctly called`() = runTest {
        // WHEN
        unAuthSessionFetcher.fetch()
        // THEN
        coVerify(exactly = 1) { opportunisticUnAuthTokenRequest() }
    }
}