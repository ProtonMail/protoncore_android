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
import me.proton.core.plan.domain.entity.DynamicPlanEntitlement

@Serializable(EntitlementResourceSerializer::class)
internal sealed class EntitlementResource {
    @Serializable
    internal data class Description(
        @SerialName("Icon")
        val icon: String,

        @SerialName("IconName")
        val iconName: String,

        @SerialName("Text")
        val text: String,

        @SerialName("Hint")
        val hint: String? = null
    ) : EntitlementResource()

    @Serializable
    internal data class Storage(
        @SerialName("Current")
        val current: Long,

        @SerialName("Max")
        val max: Long
    ) : EntitlementResource()

    @Serializable
    internal data class Unknown(
        @SerialName("Type")
        val type: String
    ) : EntitlementResource()
}

internal fun EntitlementResource.toDynamicPlanEntitlement(): DynamicPlanEntitlement? =
    when (this) {
        is EntitlementResource.Description -> DynamicPlanEntitlement.Description(
            text = text,
            iconBase64 = icon,
            iconName = iconName,
            hint = hint
        )

        is EntitlementResource.Storage -> DynamicPlanEntitlement.Storage(
            currentMBytes = current,
            maxMBytes = max
        )

        is EntitlementResource.Unknown -> null
    }

internal class EntitlementResourceSerializer :
    JsonContentPolymorphicSerializer<EntitlementResource>(EntitlementResource::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out EntitlementResource> {
        return when (element.jsonObject["Type"]?.jsonPrimitive?.contentOrNull) {
            "description" -> EntitlementResource.Description.serializer()
            "storage" -> EntitlementResource.Storage.serializer()
            else -> EntitlementResource.Unknown.serializer()
        }
    }
}
