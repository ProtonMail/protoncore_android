package me.proton.core.key.data.extension

import me.proton.core.key.data.entity.EmailAddressType
import me.proton.core.key.data.entity.PublicAddressInfoEntity
import me.proton.core.key.data.entity.PublicAddressInfoWithKeys
import me.proton.core.key.data.entity.PublicAddressKeyDataEntity
import me.proton.core.key.data.entity.SignedKeyListEntity
import me.proton.core.key.domain.entity.key.KeyFlags
import me.proton.core.key.domain.entity.key.PublicAddressInfo
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicAddressKeyData
import me.proton.core.key.domain.entity.key.PublicAddressKeySource
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import kotlin.test.Test
import kotlin.test.assertEquals

class PublicAddressInfoMapperKtTest {
    private val testEmail = "test@example.text"
    private val testCatchAllEmail = "catchall@example.text"
    private val testUnverifiedEmail = "unverified@example.text"

    private val testPublicAddressInfoEntity = PublicAddressInfoEntity(
        testEmail,
        listOf("warning1"),
        protonMx = true,
        isProton = 1,
        addressSignedKeyList = SignedKeyListEntity("addressData", "addressSignature", 1, 2, null),
        catchAllSignedKeyList = SignedKeyListEntity("catchAllData", "catchAllSignature", 2, 3, 2)
    )

    private val testPublicAddressInfo = PublicAddressInfo(
        testEmail,
        address = PublicAddressKeyData(
            listOf(
                PublicAddressKey(
                    testEmail,
                    flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
                    publicKey = PublicKey(
                        "publicKey1",
                        isPrimary = true,
                        isActive = true,
                        canEncrypt = true,
                        canVerify = true
                    ),
                    source = PublicAddressKeySource.Internal
                )
            ),
            PublicSignedKeyList("addressData", "addressSignature", 1, 2, null)
        ),
        catchAll = PublicAddressKeyData(
            listOf(
                PublicAddressKey(
                    testCatchAllEmail,
                    flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
                    publicKey = PublicKey(
                        "publicKey2",
                        isPrimary = false,
                        isActive = true,
                        canEncrypt = true,
                        canVerify = true
                    ),
                    source = PublicAddressKeySource.Wkd
                )
            ),
            PublicSignedKeyList("catchAllData", "catchAllSignature", 2, 3, 2)
        ),
        unverified = PublicAddressKeyData(
            listOf(
                PublicAddressKey(
                    testUnverifiedEmail,
                    flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
                    publicKey = PublicKey(
                        "publicKey3",
                        isPrimary = false,
                        isActive = true,
                        canEncrypt = true,
                        canVerify = true
                    ),
                    source = PublicAddressKeySource.Koo
                )
            ),
            signedKeyList = null
        ),
        warnings = listOf("warning1"),
        protonMx = true,
        isProton = 1
    )

    private val testAddressKeyEntities = listOf(
        PublicAddressKeyDataEntity(
            testEmail,
            EmailAddressType.REGULAR,
            flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
            publicKey = "publicKey1",
            isPrimary = true,
            source = 0
        ),
        PublicAddressKeyDataEntity(
            testCatchAllEmail,
            EmailAddressType.CATCH_ALL,
            flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
            publicKey = "publicKey2",
            isPrimary = false,
            source = 1
        ),
        PublicAddressKeyDataEntity(
            testUnverifiedEmail,
            EmailAddressType.UNVERIFIED,
            flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
            publicKey = "publicKey3",
            isPrimary = false,
            source = 2
        )
    )

    @Test
    fun `entity to address info`() {
        assertEquals(
            expected = testPublicAddressInfo,
            actual = testPublicAddressInfoEntity.toPublicAddressInfo(testAddressKeyEntities)
        )
    }

    @Test
    fun `publicAddressInfo to entity`() {
        assertEquals(
            PublicAddressInfoWithKeys(
                testPublicAddressInfoEntity,
                testAddressKeyEntities
            ),
            testPublicAddressInfo.toEntity(),
        )
    }
}
