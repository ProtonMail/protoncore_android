package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId

@Serializable
@Schema(description = "Screen views during the login.")
@SchemaId("https://proton.me/android_core_login_screenView_total_v1.schema.json")
public data class LoginScreenViewTotalV1(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(screenId: ScreenId) : this(LabelsData(screenId))

    @Serializable
    @Suppress("ConstructorParameterNaming")
    public data class LabelsData constructor(
        val screen_id: ScreenId
    )

    @Suppress("EnumNaming", "EnumEntryName")
    public enum class ScreenId {
        chooseInternalAddress
    }
}
