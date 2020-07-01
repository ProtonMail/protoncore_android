package ch.protonmail.libs.auth.model.request

internal interface SrpRequestBody {
    val srpSession: String
    val clientEphemeral: String
    val clientProof: String
    val twoFactorCode: String
}
