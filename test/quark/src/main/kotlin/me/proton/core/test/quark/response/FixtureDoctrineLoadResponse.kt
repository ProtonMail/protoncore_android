package me.proton.core.test.quark.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FixtureDoctrineLoadResponse(
    @SerialName("ID")
    val userId: String,

    @SerialName("ID (decrypt)")
    val decryptedUserId: Long,

    @SerialName("Name")
    val name: String,

    @SerialName("Password")
    val password: String,

    @SerialName("Status")
    val status: String,

    @SerialName("Recovery")
    val recoveryEmail: String,

    @SerialName("RecoveryPhone")
    val recoveryPhone: String,

    @SerialName("AuthVersion")
    val authVersion: Int,

    @SerialName("Email")
    val email: String,

    @SerialName("AddressID")
    val addressId: String,

    @SerialName("AddressID (decrypt)")
    val decryptedAddressId: Long,

    @SerialName("KeySalt")
    val keySalt: String,

    @SerialName("KeyFingerprint")
    val keyFingerprint: String
)
