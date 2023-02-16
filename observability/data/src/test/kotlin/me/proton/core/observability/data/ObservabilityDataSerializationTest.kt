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

package me.proton.core.observability.data

import kotlinx.serialization.Required
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import me.proton.core.observability.data.api.request.MetricEvent
import me.proton.core.observability.domain.entity.ObservabilityEvent
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingUnredeemedTotalV1
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.util.kotlin.ProtonCoreConfig.defaultJson
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObservabilityDataSerializationTest {
    @Test
    fun `empty labels object`() {
        val data = CheckoutGiapBillingUnredeemedTotalV1()
        val event = ObservabilityEvent(data = data)
        val encodedEvent = MetricEvent.fromObservabilityEvent(event)
        val dataObject = encodedEvent.data.jsonObject

        assertFalse(dataObject.containsKey(defaultJson.configuration.classDiscriminator))
        assertEquals(1, dataObject["Value"]?.jsonPrimitive?.long)
        assertTrue(dataObject["Labels"]?.jsonObject?.isEmpty() == true)
    }

    @Test
    fun `properties with default values are encoded`() {
        ObservabilityData::class.sealedSubclasses.forEach { klass ->
            klass.primaryConstructor!!.parameters
                .filter { it.isOptional }
                .mapNotNull { parameter -> klass.declaredMemberProperties.find { it.name == parameter.name } }
                .forEach { property ->
                    assertTrue(
                        property.hasAnnotation<Required>(),
                        "Property should be annotated with `kotlinx.serialization.Required`: $property"
                    )
                }
        }
    }
}
