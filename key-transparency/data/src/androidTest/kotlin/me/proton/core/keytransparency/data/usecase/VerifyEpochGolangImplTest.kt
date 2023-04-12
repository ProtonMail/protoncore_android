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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.type.IntEnum
import me.proton.core.keytransparency.domain.entity.CertificateIssuer
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.usecase.GetCurrentTime
import me.proton.core.keytransparency.domain.usecase.GetKeyTransparencyParameters
import me.proton.core.keytransparency.domain.usecase.VerifyEpoch
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class VerifyEpochGolangImplTest {
    private val getKeyTransparencyParameters = mockk<GetKeyTransparencyParameters>()

    private val getCurrentTime = mockk<GetCurrentTime>()

    private lateinit var verifier: VerifyEpoch

    private val epoch = Epoch(
        treeHash = "14634b97545c5ffae250d9d57aee7b268ea68800b362802b85bf4cbed43f1f4a",
        previousChainHash = "5b08e4f7c9a1c31f22884f4cb85f0dc960d001ac3a9b3b7771ff11e2c821fa9b",
        chainHash = "30734bb55fee44e41d2c21e0f906cee1f317631b179917ed86c1333bc4097e33",
        certificateChain = """
                -----BEGIN CERTIFICATE-----
                MIIG4jCCBMqgAwIBAgIRAJd5DydvXyCrIfMSYDRwTmgwDQYJKoZIhvcNAQEMBQAw
                SzELMAkGA1UEBhMCQVQxEDAOBgNVBAoTB1plcm9TU0wxKjAoBgNVBAMTIVplcm9T
                U0wgUlNBIERvbWFpbiBTZWN1cmUgU2l0ZSBDQTAeFw0yMjA1MTcwMDAwMDBaFw0y
                MjA4MTUyMzU5NTlaMCQxIjAgBgNVBAMTGWVwb2NoLjQxLjAuZGV2LnByb3Rvbi53
                dGYwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDSgPijyMAco9i2oSVE
                i+RXxjajtOnb6BMIBs4vSPuz8o9eOXU/txm0y93houwU40Qd0z4eU5E2VVD/PWSs
                yqd/hvM1qXfVekhCHR1qHM+7qmyxnzTpKT/QWVAe0k092RQFJpm+O3CuQJuMP536
                jlEjQ7tNX3D2fNfV6NFkMjGPmb+BMb3pF0FdJ6WqwdpxgRLHrOHrCiFX1DUD0DS6
                jK0ObBjJzm8N4y4cJi+gBkp8TnuLOI9M1vG3HL06cxJZB4cbxmHBToP6KchYf54l
                CVDgUJZIwD6wXHDOxHlPpFaSjIusyRUpAz2yPU20lyftAPr/I5KH2dDxi83CzTU8
                ig/DAgMBAAGjggLmMIIC4jAfBgNVHSMEGDAWgBTI2XhootkZaNU9ct5fCj7ctYaG
                pjAdBgNVHQ4EFgQURhm+3WmSU3na//W8yEpsuZgG3j4wDgYDVR0PAQH/BAQDAgWg
                MAwGA1UdEwEB/wQCMAAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMEkG
                A1UdIARCMEAwNAYLKwYBBAGyMQECAk4wJTAjBggrBgEFBQcCARYXaHR0cHM6Ly9z
                ZWN0aWdvLmNvbS9DUFMwCAYGZ4EMAQIBMIGIBggrBgEFBQcBAQR8MHowSwYIKwYB
                BQUHMAKGP2h0dHA6Ly96ZXJvc3NsLmNydC5zZWN0aWdvLmNvbS9aZXJvU1NMUlNB
                RG9tYWluU2VjdXJlU2l0ZUNBLmNydDArBggrBgEFBQcwAYYfaHR0cDovL3plcm9z
                c2wub2NzcC5zZWN0aWdvLmNvbTCCAQIGCisGAQQB1nkCBAIEgfMEgfAA7gB1AEal
                Vet1+pEgMLWiiWn0830RLEF0vv1JuIWr8vxw/m1HAAABgNEKNUoAAAQDAEYwRAIg
                K6vGSnloxUJfrxhzr32TucAWAItrvtUsDovxIhcxEmgCIGNs5TRYJLdh1jpYeag/
                oVNFk2LyJOyxYlq7kSeJ9WCKAHUAQcjKsd8iRkoQxqE6CUKHXk4xixsD6+tLx2jw
                kGKWBvYAAAGA0Qo1TgAABAMARjBEAiA/2fzM4Z3p4uEElFzL5OlsNe76a9EwvD+L
                BmbX/0lRAAIgGzCntxEtP3yPVWl7sHJw4GZTYRYWOBPWZKUa/SH5Y/IwgYYGA1Ud
                EQR/MH2CGWVwb2NoLjQxLjAuZGV2LnByb3Rvbi53dGaCYDMwNzM0YmI1NWZlZTQ0
                ZTQxZDJjMjFlMGY5MDZjZWUxLmYzMTc2MzFiMTc5OTE3ZWQ4NmMxMzMzYmM0MDk3
                ZTMzLjE2NTI3NzQ1MjguNDEuMC5kZXYucHJvdG9uLnd0ZjANBgkqhkiG9w0BAQwF
                AAOCAgEAMdIDZrqwmlHfrjhCNW4KmXYA/u2+RhtqV+MxAzQ/lSpeBaP64V8dztkH
                yZLj5rNhbiDGSlZ37Va8SW6SMUCMuhdEl5jYNeHhquYH5AGexRf8qFMsHrMW8nIY
                PQkfom9tYf3WqAlhmekMkFXvuJLSgMu7szu3QZCNTNaegzdowhrRXCYRoZPUN6qI
                bFFitSaCj6+/1gqOr1P/tGecW1a3LBIpEm59cL0uHmeiy3Zlg4IDg9IykMzvKRcA
                T+z2zRbj51Ns1GCW4V04V2MyZGW/jr9+J9zrL+vrpJI6TGM3Jt7/2X1Dj5OmwLPI
                1ywtMM41t4fQMKNAsIn2lE2WmyCUpyI2gvl1meoRcnkXEtgMMZqAFHq7W+0NQS46
                6JdrLnn/vGK/VWd5Dks/CpZ4ECiQtxKPtenyd18gwwUklwK/twlhgZMVqlPHFjtK
                VgAwqClz6GtEh3diLkZAxVvJa5Suh4E77H23IOj1byvhyMQOeCQDQ3AOLucfHvfN
                IfrFPWcQyYxG21ZXAUAFgVA3ubZVg6kQ4h4O1a7drBEA7CJ88/VcmtFHZDkThO3s
                D4h0l0ebn7xw2/dc9ylzHzqsO6ijE0DT7b34506pafSC11ufRa2mb0Xqc3cZtAAB
                O7J4yyPt8EBsGiHKHaiEPE3dXUkKa9928S5W/84Fiw5uCFlLDiU=
                -----END CERTIFICATE-----

                -----BEGIN CERTIFICATE-----
                MIIG1TCCBL2gAwIBAgIQbFWr29AHksedBwzYEZ7WvzANBgkqhkiG9w0BAQwFADCB
                iDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCk5ldyBKZXJzZXkxFDASBgNVBAcTC0pl
                cnNleSBDaXR5MR4wHAYDVQQKExVUaGUgVVNFUlRSVVNUIE5ldHdvcmsxLjAsBgNV
                BAMTJVVTRVJUcnVzdCBSU0EgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkwHhcNMjAw
                MTMwMDAwMDAwWhcNMzAwMTI5MjM1OTU5WjBLMQswCQYDVQQGEwJBVDEQMA4GA1UE
                ChMHWmVyb1NTTDEqMCgGA1UEAxMhWmVyb1NTTCBSU0EgRG9tYWluIFNlY3VyZSBT
                aXRlIENBMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAhmlzfqO1Mdgj
                4W3dpBPTVBX1AuvcAyG1fl0dUnw/MeueCWzRWTheZ35LVo91kLI3DDVaZKW+TBAs
                JBjEbYmMwcWSTWYCg5334SF0+ctDAsFxsX+rTDh9kSrG/4mp6OShubLaEIUJiZo4
                t873TuSd0Wj5DWt3DtpAG8T35l/v+xrN8ub8PSSoX5Vkgw+jWf4KQtNvUFLDq8mF
                WhUnPL6jHAADXpvs4lTNYwOtx9yQtbpxwSt7QJY1+ICrmRJB6BuKRt/jfDJF9Jsc
                RQVlHIxQdKAJl7oaVnXgDkqtk2qddd3kCDXd74gv813G91z7CjsGyJ93oJIlNS3U
                gFbD6V54JMgZ3rSmotYbz98oZxX7MKbtCm1aJ/q+hTv2YK1yMxrnfcieKmOYBbFD
                hnW5O6RMA703dBK92j6XRN2EttLkQuujZgy+jXRKtaWMIlkNkWJmOiHmErQngHvt
                iNkIcjJumq1ddFX4iaTI40a6zgvIBtxFeDs2RfcaH73er7ctNUUqgQT5rFgJhMmF
                x76rQgB5OZUkodb5k2ex7P+Gu4J86bS15094UuYcV09hVeknmTh5Ex9CBKipLS2W
                2wKBakf+aVYnNCU6S0nASqt2xrZpGC1v7v6DhuepyyJtn3qSV2PoBiU5Sql+aARp
                wUibQMGm44gjyNDqDlVp+ShLQlUH9x8CAwEAAaOCAXUwggFxMB8GA1UdIwQYMBaA
                FFN5v1qqK0rPVIDh2JvAnfKyA2bLMB0GA1UdDgQWBBTI2XhootkZaNU9ct5fCj7c
                tYaGpjAOBgNVHQ8BAf8EBAMCAYYwEgYDVR0TAQH/BAgwBgEB/wIBADAdBgNVHSUE
                FjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwIgYDVR0gBBswGTANBgsrBgEEAbIxAQIC
                TjAIBgZngQwBAgEwUAYDVR0fBEkwRzBFoEOgQYY/aHR0cDovL2NybC51c2VydHJ1
                c3QuY29tL1VTRVJUcnVzdFJTQUNlcnRpZmljYXRpb25BdXRob3JpdHkuY3JsMHYG
                CCsGAQUFBwEBBGowaDA/BggrBgEFBQcwAoYzaHR0cDovL2NydC51c2VydHJ1c3Qu
                Y29tL1VTRVJUcnVzdFJTQUFkZFRydXN0Q0EuY3J0MCUGCCsGAQUFBzABhhlodHRw
                Oi8vb2NzcC51c2VydHJ1c3QuY29tMA0GCSqGSIb3DQEBDAUAA4ICAQAVDwoIzQDV
                ercT0eYqZjBNJ8VNWwVFlQOtZERqn5iWnEVaLZZdzxlbvz2Fx0ExUNuUEgYkIVM4
                YocKkCQ7hO5noicoq/DrEYH5IuNcuW1I8JJZ9DLuB1fYvIHlZ2JG46iNbVKA3ygA
                Ez86RvDQlt2C494qqPVItRjrz9YlJEGT0DrttyApq0YLFDzf+Z1pkMhh7c+7fXeJ
                qmIhfJpduKc8HEQkYQQShen426S3H0JrIAbKcBCiyYFuOhfyvuwVCFDfFvrjADjd
                4jX1uQXd161IyFRbm89s2Oj5oU1wDYz5sx+hoCuh6lSs+/uPuWomIq3y1GDFNafW
                +LsHBU16lQo5Q2yh25laQsKRgyPmMpHJ98edm6y2sHUabASmRHxvGiuwwE25aDU0
                2SAeepyImJ2CzB80YG7WxlynHqNhpE7xfC7PzQlLgmfEHdU+tHFeQazRQnrFkW2W
                kqRGIq7cKRnyypvjPMkjeiV9lRdAM9fSJvsB3svUuu1coIG1xxI1yegoGM4r5QP4
                RGIVvYaiI76C0djoSbQ/dkIUUXQuB8AL5jyH34g3BZaaXyvpmnV4ilppMXVAnAYG
                ON51WhJ6W0xNdNJwzYASZYH+tmCWI+N60Gv2NNMGHwMZ7e9bXgzUCZH5FaBFDGR5
                S9VWqHB73Q+OyIVvIbKYcSc2w/aSuFKGSA==
                -----END CERTIFICATE-----
        """.trimIndent(),
        certificateIssuer = CertificateIssuer.ZeroSsl.let { IntEnum(it.value, it) },
        epochId = 41,
        certificateTime = 1_652_774_528
    )

    @Before
    fun setUp() {
        every { getKeyTransparencyParameters() } returns mockk {
            every { vrfPublicKey } returns "LXaI/rQp9xTxAvdYQSzUuBM3swcSJ3D2IK2eSsiYous="
            every { certificateDomain } returns "dev.proton.wtf"
        }
        coEvery { getCurrentTime() } returns 1_652_750_000L
        verifier = VerifyEpochGolangImpl(getKeyTransparencyParameters, getCurrentTime)
    }

    @Test
    fun verifyEpoch() = runTest {
        // given
        val expectedNotBefore = 1_652_745_600L
        // when
        val notBefore = verifier(epoch)
        // then
        assertEquals(expectedNotBefore, notBefore)
    }
}
