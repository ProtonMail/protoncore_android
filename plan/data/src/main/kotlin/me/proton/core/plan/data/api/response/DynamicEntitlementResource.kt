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

package me.proton.core.plan.data.api.response

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.proton.core.plan.domain.entity.DynamicEntitlement

@Serializable(DynamicEntitlementResourceSerializer::class)
sealed class DynamicEntitlementResource {
    @Serializable
    data class Description(
        @SerialName("IconName")
        val iconName: String,

        @SerialName("Text")
        val text: String? = null, // TODO: Remove nullability.

        @SerialName("Hint")
        val hint: String? = null
    ) : DynamicEntitlementResource()

    @Serializable
    data class Storage(
        @SerialName("Current")
        val current: Long,

        @SerialName("Max")
        val max: Long
    ) : DynamicEntitlementResource()

    @Serializable
    data class Unknown(
        @SerialName("Type")
        val type: String
    ) : DynamicEntitlementResource()
}

fun DynamicEntitlementResource.toDynamicPlanEntitlement(iconsEndpoint: String): DynamicEntitlement? =
    when (this) {
        is DynamicEntitlementResource.Description -> if (text == null) null else DynamicEntitlement.Description(
            text = text,
            iconUrl = "$iconsEndpoint/$iconName",
            hint = hint
        )

        is DynamicEntitlementResource.Storage -> DynamicEntitlement.Storage(
            currentBytes = current,
            maxBytes = max
        )

        is DynamicEntitlementResource.Unknown -> null
    }

class DynamicEntitlementResourceSerializer :
    JsonContentPolymorphicSerializer<DynamicEntitlementResource>(DynamicEntitlementResource::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out DynamicEntitlementResource> {
        return when (element.jsonObject["Type"]?.jsonPrimitive?.contentOrNull) {
            "description" -> DynamicEntitlementResource.Description.serializer()
            "storage" -> DynamicEntitlementResource.Storage.serializer()
            else -> DynamicEntitlementResource.Unknown.serializer()
        }
    }
}
