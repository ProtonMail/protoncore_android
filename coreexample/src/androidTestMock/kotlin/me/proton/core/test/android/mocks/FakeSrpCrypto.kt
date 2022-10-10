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

package me.proton.core.test.android.mocks

import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs

@Suppress("MaxLineLength")
class FakeSrpCrypto : SrpCrypto {
    override fun generateSrpProofs(
        username: String,
        password: ByteArray,
        version: Long,
        salt: String,
        modulus: String,
        serverEphemeral: String
    ): SrpProofs {
        return SrpProofs(
            clientEphemeral = "6PckVdFlaPSR8UhlCCnLg5Wpmob++1s1EPGOqjhR2etOgqnfQd102t0LDeM8QKO90yPsDbnSjjGcLkK7nWeCLhORNCs71XldJg8QP5ffwN4deXakWw5GOdMBqBMtHLAy9elCewg9B0wxjdYimbdKia7Zk/HyqMKK2Skd3JpRPNNUxK798G4dlISe7UaQ2L3PEIx5aRp+WmlRfT3oq0I4qspjrU64u+44KkchAi+/yQFj4PJQVDsc6NnGL28zGH7OIwmclOQgRZ70+4TiifrnoDfpewg+z37er5t/exYkhckfUaAt4EGQyc2zh8JtV1uu3/GjS8M5bKkzDZ2iHo6snA==",
            clientProof = "HkY/EcSS75nYf0IPcIfQEIx7Q5+P8t1KQJLTVibFFgeGNB8UNcBpTQO14ih3RWakP/gXrP0fq+ucYU8BluanISMaxXAR6wbPTLEYRQ0vAJJaHVNhOQJ3P31hklh6AKrXikaVU5+z9fToL/TSfb36E4Vkaj0TYenNo4rDKEnK2xuaLk2oezz8g+CjCnd1ekjCRv+zD2dEZPIrcYH6dVwM98YMxov8pzBGEaafMxNNo2GLifWKixAESAnLnLob8dnomv8tZTwZnSkOrT5aFQovQzq+42RwEprTP0fVHt7PnfFkdUzEUFLvbrnWz3QLx7b2zvaTMrW7eQ8AH48IdHyQlQ==",
            expectedServerProof = "hD3IgA8zIRu7/0uCMns2DTNv0abl++eEaGEbSCKmgknmNL9JGk0MgaUM3ut+t7jTfi3PJKtNZCU5kMN5oKFzMDiAojN8pG6XrjOorbxDN5PmfP7XLUHgFolLlxgb93sKyZPie73J/cf22VHkmlHmFTlbMZs2gqTrUDF/D4xdewlrA5weu4myP/baanaPaLR9UzFrgOMe69k6Ei2iUbOtuntUyj4zqiK1JvuyDjVZSYD91aVwx25VNLZMc+bTAWTwYjVQO+z6J7CnyDyRBu9OXzO54YwpiMdhWKvHZIPx5xxPWJzPtA4SaFl+t57/K1C23MY4OHnxKaQvllN5nKG+rQ=="
        )
    }

    override fun calculatePasswordVerifier(
        username: String,
        password: ByteArray,
        modulusId: String,
        modulus: String
    ): Auth {
        return Auth(
            version = 4,
            modulusId = "e_DzaCA0dI-vawVq4ab00WFHBMCbZ4ZbVEFBIAxEIkg_ozPFoN4QAQxTjO7vlQ5P9Y-MpUTgUAZ_CICYQz2uxA==",
            salt = "v0qLXBUgfQBIcQ==",
            verifier = "jHbPpeayCOGVuLK/cT+g/2Pw9UFgNapy2IDD1GELHej2uCaNfN2uHRoy/XGL27Ngrorb1nkt5ZWFvW/6HKjyUxn14h7ECU6jG6l+tkVLLd8RlDuxQ+PAoSJ4Zuog6H9yEupfuT0x1v6UuITS/i+PRVA4NQ47LaLXpAgiosiX9aLx5g67v8gbMSB50bydgJZakFXAbBiU/mbZwCpJzmyIlxbQSU0CGXfInN0uMbfOJ5AyBkLRZZCYVkWjyzNKABGkBS8nNx3l0p5LtNN5W4F59w27zxHYBli+zPGPgAbNqww4PjSSnhM1GFCF/VbGNnAcMlzwerI2vuzBSCqHAv2/CA=="
        )
    }
}
