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

package me.proton.core.keytransparency.data.usecase

import io.mockk.every
import io.mockk.mockk
import me.proton.core.keytransparency.domain.entity.Proof
import me.proton.core.keytransparency.domain.entity.ProofType
import me.proton.core.keytransparency.domain.usecase.GetKeyTransparencyParameters
import me.proton.core.keytransparency.domain.usecase.VerifyProof
import org.junit.Before
import org.junit.Test

class VerifyProofGolangImplTest {

    private val getKeyTransparencyParameters = mockk<GetKeyTransparencyParameters>()

    private lateinit var verifier: VerifyProof

    @Before
    fun setUp() {

        every { getKeyTransparencyParameters() } returns mockk {
            every { vrfPublicKey } returns "LXaI/rQp9xTxAvdYQSzUuBM3swcSJ3D2IK2eSsiYous="
            every { certificateDomain } returns "dev.proton.wtf"
        }
        verifier = VerifyProofGolangImpl(getKeyTransparencyParameters)
    }

    @Test
    fun valid_existence_proof() {
        // Given
        val proof = Proof(
            type = ProofType.EXISTENCE.getIntEnum(),
            neighbors = mapOf(
                0 to "f68fc5918b211a3c001226d8b1781c72ee751983123bc3e36f8e85c3d14cb0f7",
                1 to "1970ed025d707f413a3865d19f98f3adeb09d58be3ea18263948a27b50e5af7c",
                2 to "04956cdb50a67b34cb536880b23aa670f254fb1134a58256851562d9e686ba39",
                3 to "c2e3d5e2cc431e0fd7a0ca2358798e9656fdd8228cf8f96aa1aecbfda55a4685",
                4 to "8d25c7c9eae9aa65c1de019a017ed85dfc3d5cda3128458686f47f6637740ca2",
                5 to "3477ff1986106b737a26ad91fde4ae674b182be14fd8168fdaf5b7f5a357e13b",
                6 to "85462297eae11bcbc618c296a56cfb72c1780e37dc66224e2a349e7c8ea954f3",
                7 to "b9138de198b9385d8c5b109ef4f3aae0779ca97fa73ea30515033e25b75f3a4e",
                8 to "b4efa26a2e06acb2a92d18fa53eb497f4e65efa260a010cfd5baffa44994e04d",
                9 to "c6eaa92aadccf5b4ba0769749e71456651c90e73e18c184d58e24b1fb7617df0",
                14 to "add4f6e978bcc1135bbe5994f19ae3dbc44893fee4ad0095b6a67dc27f9f72b7"
            ),
            vrfProof = "f4ebc424d55cddc6afe1df56ae0cde41d61b5dbe47e25432db4622ec83fade60e" +
                "cecbe11b88884fa81893afdaf522ed99cbd8ebf8c4140015f3d993b338e01014edfede032fb0f74ecddfff0d98df50f",
            revision = 0,
            obsolescenceToken = null
        )
        val email = "marintest@proton.black"
        val rootHash = "14634b97545c5ffae250d9d57aee7b268ea68800b362802b85bf4cbed43f1f4a"
        val signedKeyList = "[{\"Primary\":1,\"Flags\":3,\"Fingerprint\":" +
            "\"462c0961896fed8edd82ca5c5ff80b0750bee7d2\"," +
            "\"SHA256Fingerprints\":[\"fecee50ef83932be5ec3961588aa36b2d5d70090f7c494965b9217b8e12d282c\"," +
            "\"3233764adbbfabbce873b6b553caa782e3e90112cb29b3a66f3b974b6cc393f3\"]}]"

        // WHEN
        verifier(email, signedKeyList, proof, rootHash)
    }

    @Test
    fun valid_absence_proof() {
        // Given
        val proof = Proof(
            type = ProofType.ABSENCE.getIntEnum(),
            neighbors = mapOf(
                0 to "b5d0af4f6d3b0392de9b82b705eecba5b21c6119d17016d781cbe2d6aa2f031d",
                1 to "27c1d3800189b10d1d20e0bf1cc1d921f2b5cef243924217f01fff4ab2d75dc5",
                2 to "4782c3207c54f45bc08b7deb29368632d7ec2968b04607e289d9246c687189ba",
                3 to "26319ac9ef22b8ff14a81d1b3f883d555e97302ad3b79387278c65f750e1ce7e",
                4 to "fb3300e68d1cabf4425a66dda2270d91dd1155350c72d01a6b7d1e9e12219a55",
                5 to "52dd6be0aa0beed281d14d99ee9e42944b0f8e142181cfbf216655ea06decb6d",
                6 to "1aa725caf0ed61254dac30e4ba2dace3fa4e2c5581a37219aa54cf411bfb0dd4",
                7 to "e04be28989eeaff4daabb9aa275a16d9d2169c5fc93bbca575ba4e424c5bea75",
                8 to "38ae745d350a84c178b01857f497f360b5791cd78b607386ade35eb10aa03611",
                9 to "07799a3c62ba47d08af4ff66411aa330fabc7493880a2e0877a8abb0c1ff8b72",
                12 to "af2893fa0ba3e70caed6be5f7604a38e769f284d6c09505e47b70891a613c932"
            ),
            vrfProof = "50971599f49155bb070db21387a5f23c111abd89ada6f967fc413d67f6afd6a9d65541fd7" +
                "d8a2ecb8533d76a2938daff87539c3e127ce53ea8c3606b33672825ed935e81d41d080f96873cd48efc9b07",
            revision = 0,
            obsolescenceToken = null
        )
        val email = "mttestpm@gmail.com"
        val rootHash = "14634b97545c5ffae250d9d57aee7b268ea68800b362802b85bf4cbed43f1f4a"
        val signedKeyList = null
        // WHEN
        verifier(email, signedKeyList, proof, rootHash)
    }
}
