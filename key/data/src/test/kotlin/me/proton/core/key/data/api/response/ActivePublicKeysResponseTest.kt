package me.proton.core.key.data.api.response

import me.proton.core.util.kotlin.deserialize
import kotlin.test.Test
import kotlin.test.assertEquals

class ActivePublicKeysResponseTest {
    @Test
    fun `no addresses`() {
        assertEquals(
            ActivePublicKeysResponse(
                address = AddressDataResponse(keys = emptyList()),
                catchAll = AddressDataResponse(keys = emptyList()),
                protonMx = false,
                isProton = 0,
                warnings = emptyList()
            ),
            """
                {
                    "Code": 1000,
                    "Address": {
                        "Keys": [],
                        "SignedKeyList": null
                    },
                    "Warnings": [],
                    "ProtonMX": false,
                    "IsProton": 0,
                    "CatchAll": {
                        "Keys": [],
                        "SignedKeyList": null
                    }
                }
            """.trimIndent().deserialize()
        )
    }

    @Test
    fun `single address`() {
        assertEquals(
            ActivePublicKeysResponse(
                address = AddressDataResponse(
                    keys = listOf(
                        PublicAddressKeyResponse(
                            flags = 3,
                            publicKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\nVersion: ProtonMail\n\nxsBNBF67AJkBCACdxh2Ix0gDdRQpnYbwiYgRMjv+D8dG8m2OpzNwC/N66XAV\naemfAJqKckkRIwq/+c/tbFbM8URQBJP7i+01LesCVC1BiH0DTQBVnrRN4B2B\nvkwziu6AHFHP/PkQICTA0hhpWr5f7XOcQ3QGumqc15fuK10QKqh+YBAF/QPH\nMBI2l/RPFSLKwRBF5vF1n+7URfSBnunKg3alTrd4c5U0XQ9rvIG9lcqNl7MF\nHP/pZZ9nTKWafZx9CKpA01OsKquJNtOzLBr3TrBPoWCmcRPQHs8+pR3bmsy/\nWRdP/qa8N8rS1lIojHKRsSeMtrFsz5+7JKsgdUEKCt9O6DLJdX4KpJCrABEB\nAAHNJ3Byb0Bwcm90b25tYWlsLmRldiA8cHJvQHByb3Rvbm1haWwuZGV2PsLA\ndgQQAQgAIAUCXrsAmQYLCQcIAwIEFQgKAgQWAgEAAhkBAhsDAh4BAAoJEATN\nncJfuuUJMVwH/RToOpmZUqq/SUUHCdMuDGhuzQs5bkIrXljfBgXlXgcrfT7a\n+IVOEyXIs+ZTENGh7w1D2hQKRE5AoTaCi+LEqUGh8fEUMT8FkAX07JXHCSTX\n5f8INufVMwKaQ0LiVVn6ZtvLPPAaqCCahlcYbzXoW9zdgtHurqzqIG9iiY+E\n6/x6ywiCNvEKXOIdVy4K5NOgzvVv7dbOdt35Zbz2C32vSwenHIzp+c9g/YA9\ngZ7TGhZ7XCBE0uf28QsaLlemsDMIo3nsbR/3eEKrhVFBjGpa+c4tT1469hZr\nqhpceYucjimaXqeDT8cU1MVEUAngb5M/7R8cvFpXZ2bZKDngTba2GZ3OwE0E\nXrsAmQEIANkUF4ufX2EHvcESDy8woESRs02Z8/WI79yw+FY9+0d1cJwSGR0H\no3CPC2stsCGArVXSRZrA5UfUs9iZIO3GczzeEXbZNTph9ULgq6UrlPLlZ98T\nkpCQ10E9E3/zusRBFse9P7F+kcbl6Q1Tlu5dkfluyCyXzdcdo4D+iZFwsQi5\naZku/d+ikGAvy5lqrT8Vppt2QnQVnfE3659apXTNH16ps/+y2F4rx6rLtvsd\nplz8mRyRN+yG5yh8QDT8M2WqiSmFoTRyfS9k/B0ePAQlN4/S0DJy7CphJM06\nZbFp2jdap6XBkJq1JDdAayyNxR8PJxtVJGK3QYBaogWgBIfbXg0AEQEAAcLA\nXwQYAQgACQUCXrsAmQIbDAAKCRAEzZ3CX7rlCQ05B/0btbjiPWVtHXVfkduH\nR+rW0pktEC6mqtQuuOart3jBilCC3Xn+Q8f0t3fYKZ/quUkTL4fygYf8t6+/\nP91bxXVcgYdpBVVRJxlRufg+P+cI9FKl9233eGlUdFwSDk4kfrX50CDR+q32\nosKbUygfnwJSocXFcFYtnkqCHMJUZWYp1kQoK71J10OfWyVvfHBKyloDxMij\nilmu1NfZWjBZgsnB+vJV1lAM2sbEbY67xsiSYVMKllTJaDQsZTOLCNwAOcbk\nCgR7YpgfQ/Jnqkv2udiAIuZMMziy6WKSz9vOmwwqmOj/81hd5sZkpV1Cjd3e\nSsnSRwzjryPs8/9rWnYW8E+g\n=7UuI\n-----END PGP PUBLIC KEY BLOCK-----\n"
                        )
                    ), signedKeyList = SignedKeyListResponse(
                        data = "[{\"Primary\":1,\"Flags\":3,\"Fingerprint\":\"5372de721b9971518273581e04cd9dc25fbae509\",\"SHA256Fingerprints\":[\"4380c60bc440132428390868598b9872ed4efad6a87e2c7aad25807fe7f675b0\",\"bad8f749883cc2873d09e66cfce2604855b85aaaa7215311d444e2b60a96cd59\"]}]",
                        signature = "-----BEGIN PGP SIGNATURE-----\nVersion: ProtonMail\n\nwsCnBAEBCABbBYJmJ8VOCZAEzZ3CX7rlCTMUgAAAAAARABljb250ZXh0QHBy\nb3Rvbi5jaGtleS10cmFuc3BhcmVuY3kua2V5LWxpc3QWIQRTct5yG5lxUYJz\nWB4EzZ3CX7rlCQAAPnkH+QHoofINYrpjuNB4Ewa9vWLCQ/jOga9Yg6yQzeJ5\nEJ9wrgo1tj6Qkn49Uly3/wDW0jLeTz7ykXA6Ww88JFZHI0fBYCEM2hXSVmCl\nkRQlPJhHZ8B+SjK2ZMZdY/1JEPObKbnHeYbWXhjrlTK1WEQ5ZVr6FpsRwkve\nkGgF/cyU/DwnReM3zGWaLAPVPQFr+oGZkSM0aq2jSmhcknxd+WBWt5ZesiLl\n2lyFtY8wR4+gUA8DN3ilcQL9xiYJpq9MCI204vy+wZ4Cf8t7bSFAcjgEhQ+7\nM2TKjm3le/36Sf6v324GWZ3eWzwoywu1yQaWn0B+uCmtFukDKS9TJ05kwp5s\ns9g=\n=zMXd\n-----END PGP SIGNATURE-----\n",
                        minEpochId = 3,
                        maxEpochId = 69,
                        expectedMinEpochId = null
                    )
                ),
                protonMx = true,
                isProton = 0,
                warnings = emptyList()
            ),
            """
                {
                    "Code": 1000,
                    "Address": {
                        "Keys": [
                            {
                                "Flags": 3,
                                "PublicKey": "-----BEGIN PGP PUBLIC KEY BLOCK-----\nVersion: ProtonMail\n\nxsBNBF67AJkBCACdxh2Ix0gDdRQpnYbwiYgRMjv+D8dG8m2OpzNwC/N66XAV\naemfAJqKckkRIwq/+c/tbFbM8URQBJP7i+01LesCVC1BiH0DTQBVnrRN4B2B\nvkwziu6AHFHP/PkQICTA0hhpWr5f7XOcQ3QGumqc15fuK10QKqh+YBAF/QPH\nMBI2l/RPFSLKwRBF5vF1n+7URfSBnunKg3alTrd4c5U0XQ9rvIG9lcqNl7MF\nHP/pZZ9nTKWafZx9CKpA01OsKquJNtOzLBr3TrBPoWCmcRPQHs8+pR3bmsy/\nWRdP/qa8N8rS1lIojHKRsSeMtrFsz5+7JKsgdUEKCt9O6DLJdX4KpJCrABEB\nAAHNJ3Byb0Bwcm90b25tYWlsLmRldiA8cHJvQHByb3Rvbm1haWwuZGV2PsLA\ndgQQAQgAIAUCXrsAmQYLCQcIAwIEFQgKAgQWAgEAAhkBAhsDAh4BAAoJEATN\nncJfuuUJMVwH/RToOpmZUqq/SUUHCdMuDGhuzQs5bkIrXljfBgXlXgcrfT7a\n+IVOEyXIs+ZTENGh7w1D2hQKRE5AoTaCi+LEqUGh8fEUMT8FkAX07JXHCSTX\n5f8INufVMwKaQ0LiVVn6ZtvLPPAaqCCahlcYbzXoW9zdgtHurqzqIG9iiY+E\n6/x6ywiCNvEKXOIdVy4K5NOgzvVv7dbOdt35Zbz2C32vSwenHIzp+c9g/YA9\ngZ7TGhZ7XCBE0uf28QsaLlemsDMIo3nsbR/3eEKrhVFBjGpa+c4tT1469hZr\nqhpceYucjimaXqeDT8cU1MVEUAngb5M/7R8cvFpXZ2bZKDngTba2GZ3OwE0E\nXrsAmQEIANkUF4ufX2EHvcESDy8woESRs02Z8/WI79yw+FY9+0d1cJwSGR0H\no3CPC2stsCGArVXSRZrA5UfUs9iZIO3GczzeEXbZNTph9ULgq6UrlPLlZ98T\nkpCQ10E9E3/zusRBFse9P7F+kcbl6Q1Tlu5dkfluyCyXzdcdo4D+iZFwsQi5\naZku/d+ikGAvy5lqrT8Vppt2QnQVnfE3659apXTNH16ps/+y2F4rx6rLtvsd\nplz8mRyRN+yG5yh8QDT8M2WqiSmFoTRyfS9k/B0ePAQlN4/S0DJy7CphJM06\nZbFp2jdap6XBkJq1JDdAayyNxR8PJxtVJGK3QYBaogWgBIfbXg0AEQEAAcLA\nXwQYAQgACQUCXrsAmQIbDAAKCRAEzZ3CX7rlCQ05B/0btbjiPWVtHXVfkduH\nR+rW0pktEC6mqtQuuOart3jBilCC3Xn+Q8f0t3fYKZ/quUkTL4fygYf8t6+/\nP91bxXVcgYdpBVVRJxlRufg+P+cI9FKl9233eGlUdFwSDk4kfrX50CDR+q32\nosKbUygfnwJSocXFcFYtnkqCHMJUZWYp1kQoK71J10OfWyVvfHBKyloDxMij\nilmu1NfZWjBZgsnB+vJV1lAM2sbEbY67xsiSYVMKllTJaDQsZTOLCNwAOcbk\nCgR7YpgfQ/Jnqkv2udiAIuZMMziy6WKSz9vOmwwqmOj/81hd5sZkpV1Cjd3e\nSsnSRwzjryPs8/9rWnYW8E+g\n=7UuI\n-----END PGP PUBLIC KEY BLOCK-----\n",
                                "Source": 0
                            }
                        ],
                        "SignedKeyList": {
                            "MinEpochID": 3,
                            "MaxEpochID": 69,
                            "ExpectedMinEpochID": null,
                            "Data": "[{\"Primary\":1,\"Flags\":3,\"Fingerprint\":\"5372de721b9971518273581e04cd9dc25fbae509\",\"SHA256Fingerprints\":[\"4380c60bc440132428390868598b9872ed4efad6a87e2c7aad25807fe7f675b0\",\"bad8f749883cc2873d09e66cfce2604855b85aaaa7215311d444e2b60a96cd59\"]}]",
                            "ObsolescenceToken": null,
                            "Revision": 1,
                            "Signature": "-----BEGIN PGP SIGNATURE-----\nVersion: ProtonMail\n\nwsCnBAEBCABbBYJmJ8VOCZAEzZ3CX7rlCTMUgAAAAAARABljb250ZXh0QHBy\nb3Rvbi5jaGtleS10cmFuc3BhcmVuY3kua2V5LWxpc3QWIQRTct5yG5lxUYJz\nWB4EzZ3CX7rlCQAAPnkH+QHoofINYrpjuNB4Ewa9vWLCQ/jOga9Yg6yQzeJ5\nEJ9wrgo1tj6Qkn49Uly3/wDW0jLeTz7ykXA6Ww88JFZHI0fBYCEM2hXSVmCl\nkRQlPJhHZ8B+SjK2ZMZdY/1JEPObKbnHeYbWXhjrlTK1WEQ5ZVr6FpsRwkve\nkGgF/cyU/DwnReM3zGWaLAPVPQFr+oGZkSM0aq2jSmhcknxd+WBWt5ZesiLl\n2lyFtY8wR4+gUA8DN3ilcQL9xiYJpq9MCI204vy+wZ4Cf8t7bSFAcjgEhQ+7\nM2TKjm3le/36Sf6v324GWZ3eWzwoywu1yQaWn0B+uCmtFukDKS9TJ05kwp5s\ns9g=\n=zMXd\n-----END PGP SIGNATURE-----\n"
                        }
                    },
                    "Warnings": [],
                    "ProtonMX": true,
                    "IsProton": 0
                }
            """.trimIndent().deserialize()
        )
    }
}
