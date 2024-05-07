package me.proton.core.auth.data.api.response

import me.proton.core.auth.data.api.fido2.AuthenticationOptionsData
import me.proton.core.auth.data.api.fido2.Fido2RegisteredKeyData
import me.proton.core.auth.data.api.fido2.PublicKeyCredentialDescriptorData
import me.proton.core.auth.data.api.fido2.PublicKeyCredentialRequestOptionsResponse
import me.proton.core.util.kotlin.deserialize
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalUnsignedTypes::class)
class SecondFactorInfoResponseTest {
    @Test
    fun `full response`() {
        assertEquals(
            SecondFactorInfoResponse(
                enabled = 3,
                fido2 = Fido2Response(
                    authenticationOptions = AuthenticationOptionsData(
                        publicKey = PublicKeyCredentialRequestOptionsResponse(
                            challenge = ubyteArrayOf(12U, 128U, 181U),
                            timeout = 600000U,
                            rpId = "proton.black",
                            allowCredentials = listOf(
                                PublicKeyCredentialDescriptorData(
                                    type = "public-key",
                                    id = ubyteArrayOf(8U, 172U, 244U),
                                    transports = null
                                )
                            ),
                            userVerification = "discouraged",
                            extensions = null
                        )
                    ),
                    registeredKeys = listOf(
                        Fido2RegisteredKeyData(
                            attestationFormat = "none",
                            credentialID = ubyteArrayOf(8U, 172U, 244U),
                            name = "My FIDO2 key"
                        )
                    )
                )
            ),
            FULL_RESPONSE.deserialize()
        )
    }

    @Test
    fun `minimal response`() {
        assertEquals(
            SecondFactorInfoResponse(
                enabled = 0,
                fido2 = Fido2Response(
                    authenticationOptions = null,
                    registeredKeys = emptyList()
                )
            ),
            MINIMAL_RESPONSE.deserialize()
        )
    }
}

private const val FULL_RESPONSE = """
{
    "Enabled": 3,
    "FIDO2": {
        "AuthenticationOptions": {
            "publicKey": {
                "timeout": 600000,
                "challenge": [
                    12,
                    128,
                    181
                ],
                "userVerification": "discouraged",
                "rpId": "proton.black",
                "allowCredentials": [
                    {
                        "id": [
                            8,
                            172,
                            244
                        ],
                        "type": "public-key"
                    }
                ]
            }
        },
        "RegisteredKeys": [
            {
                "AttestationFormat": "none",
                "CredentialID": [
                    8,
                    172,
                    244
                ],
                "Name": "My FIDO2 key"
            }
        ]
    },
    "TOTP": 1
}
"""

private const val MINIMAL_RESPONSE = """
{
    "Enabled": 0,
    "FIDO2": {
        "AuthenticationOptions": null,
        "RegisteredKeys": []
    },
    "TOTP": 0
}
"""