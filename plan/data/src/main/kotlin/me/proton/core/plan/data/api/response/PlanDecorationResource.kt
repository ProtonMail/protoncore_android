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

import android.util.Base64
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.proton.core.plan.domain.entity.DynamicPlanDecoration

@Serializable(PlanDecorationResourceSerializer::class)
internal sealed class PlanDecorationResource {
    @Serializable
    internal data class Star(
        @SerialName("Icon")
        val icon: String
    ) : PlanDecorationResource()

    @Serializable
    internal data class Unknown(
        @SerialName("Type")
        val type: String
    ) : PlanDecorationResource()
}

internal fun PlanDecorationResource.toDynamicPlanDecoration(): DynamicPlanDecoration? =
    when (this) {
        is PlanDecorationResource.Star -> DynamicPlanDecoration.Star(
            Base64.decode(
                icon,
                Base64.DEFAULT
            ).decodeToString()
        )

        is PlanDecorationResource.Unknown -> null
    }

internal class PlanDecorationResourceSerializer :
    JsonContentPolymorphicSerializer<PlanDecorationResource>(PlanDecorationResource::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out PlanDecorationResource> {
        return when (element.jsonObject["Type"]?.jsonPrimitive?.contentOrNull) {
            "Star" -> PlanDecorationResource.Star.serializer()
            else -> PlanDecorationResource.Unknown.serializer()
        }
    }
}
