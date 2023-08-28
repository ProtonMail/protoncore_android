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

package me.proton.core.plan.domain.entity

import me.proton.core.domain.type.StringEnum
import java.util.EnumSet
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicPlanEnumMappingTest {

    @Test
    fun dynamicPlanFeatureEnumSetOf() {
        assertEquals(
            actual = DynamicPlanFeature.enumSetOf(1),
            expected = EnumSet.of(DynamicPlanFeature.CatchAll)
        )
        assertEquals(
            actual = DynamicPlanFeature.enumSetOf(0),
            expected = EnumSet.noneOf(DynamicPlanFeature::class.java)
        )
    }

    @Test
    fun dynamicPlanServiceEnumSetOf() {
        assertEquals(
            actual = DynamicPlanService.enumSetOf(1),
            expected = EnumSet.of(DynamicPlanService.Mail, DynamicPlanService.Calendar)
        )
        assertEquals(
            actual = DynamicPlanService.enumSetOf(2),
            expected = EnumSet.of(DynamicPlanService.Drive)
        )
        assertEquals(
            actual = DynamicPlanService.enumSetOf(4),
            expected = EnumSet.of(DynamicPlanService.Vpn)
        )
        assertEquals(
            actual = DynamicPlanService.enumSetOf(8),
            expected = EnumSet.of(DynamicPlanService.Pass)
        )
        assertEquals(
            actual = DynamicPlanService.enumSetOf(0),
            expected = EnumSet.noneOf(DynamicPlanService::class.java)
        )

        assertEquals(
            actual = DynamicPlanService.enumSetOf(15),
            expected = EnumSet.of(
                DynamicPlanService.Mail,
                DynamicPlanService.Calendar,
                DynamicPlanService.Drive,
                DynamicPlanService.Vpn,
                DynamicPlanService.Pass
            )
        )
    }

    @Test
    fun dynamicPlanLayoutEnumOf() {
        assertEquals(
            actual = DynamicPlanLayout.enumOf("default"),
            expected = StringEnum(DynamicPlanLayout.Default.code, DynamicPlanLayout.Default)
        )
        assertEquals(
            actual = DynamicPlanLayout.enumOf("unknown"),
            expected = StringEnum<DynamicPlanLayout>("unknown", null)
        )
        assertEquals(
            actual = DynamicPlanLayout.enumOfOrDefault(null),
            expected = StringEnum(DynamicPlanLayout.Default.code, DynamicPlanLayout.Default)
        )
    }
}
