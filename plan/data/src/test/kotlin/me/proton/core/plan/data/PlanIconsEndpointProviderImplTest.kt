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

package me.proton.core.plan.data

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.test.kotlin.assertEquals
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class PlanIconsEndpointProviderImplTest {

    //region mocks
    @MockK
    private lateinit var networkPrefs:NetworkPrefs
    //endregion
    private lateinit var tested: PlanIconsEndpointProviderImpl

    private val baseApiUrl = "https://test-api-url.com"
    private val baseAltApiUrl = "https://test-api-url-alt.com"
    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        tested = PlanIconsEndpointProviderImpl(baseApiUrl.toHttpUrl(), networkPrefs)
    }

    @Test
    fun `test without active alt base url`() {
        every { networkPrefs.activeAltBaseUrl } returns null
        val result = tested.get()

        assertEquals("https://test-api-url.com/payments/v5/resources/icons/", result)
    }

    @Test
    fun `test with active alt base url`() {
        every { networkPrefs.activeAltBaseUrl } returns baseAltApiUrl
        val result = tested.get()

        assertEquals("https://test-api-url-alt.com/payments/v5/resources/icons/", result)
    }
}
