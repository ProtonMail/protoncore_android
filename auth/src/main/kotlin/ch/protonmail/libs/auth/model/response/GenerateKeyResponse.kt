package ch.protonmail.libs.auth.model.response

import kotlinx.serialization.Serializable

@Serializable
internal data class GenerateKeyResponse(
    val keySalt: String,
    val privateKey: String,
    val generateMailboxPassword: String
)
