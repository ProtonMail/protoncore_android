package me.proton.android.core.data.api.entity.request

internal interface SrpRequestBody {
    val srpSession: String
    val clientEphemeral: String
    val clientProof: String
    val twoFactorCode: String
}
