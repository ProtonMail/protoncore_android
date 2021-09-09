/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.domain.arch

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.util.kotlin.invoke
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * API test suite for [Mapper]
 */
internal class MapperTest {

    data class BusinessModel(val i: Int)
    data class UiModel(val s: String)

    class TestMapper : Mapper<BusinessModel, UiModel> {
        fun BusinessModel.toUiModel() = UiModel(i.toString())
    }

    private val testMapper = TestMapper()

    @Test
    fun `simple model API`() {
        val uiModel = testMapper { BusinessModel(15).toUiModel() }
        assertEquals(UiModel("15"), uiModel)
    }

    @Test
    fun `models List API`() {
        val uiModels = listOf(BusinessModel(10), BusinessModel(15), BusinessModel(20))
            .map { testMapper { it.toUiModel() } }

        assertEquals(
            listOf(UiModel("10"), UiModel("15"), UiModel("20")),
            uiModels
        )
    }

    @Test
    fun `models Flow API`() = runBlockingTest {
        val uiModels = flowOf(BusinessModel(10), BusinessModel(15), BusinessModel(20))
            .map { testMapper { it.toUiModel() } }
            .toList()

        assertEquals(
            listOf(UiModel("10"), UiModel("15"), UiModel("20")),
            uiModels
        )
    }
}
