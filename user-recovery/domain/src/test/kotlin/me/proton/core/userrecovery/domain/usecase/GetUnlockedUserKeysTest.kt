package me.proton.core.userrecovery.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserKey
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class GetUnlockedUserKeysTest {
    @MockK
    private lateinit var cryptoContext: CryptoContext

    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: GetUnlockedUserKeys

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = GetUnlockedUserKeys(cryptoContext, userManager)
    }

    @Test
    fun `get unlocked user keys`() = runTest {
        // GIVEN
        val privateKey1 = mockPrivateKey("key1")
        val privateKey2 = mockPrivateKey("key2")
        val privateKey3 = mockPrivateKey("key3")
        val key1 = mockUserKey(true, privateKey1)
        val key2 = mockUserKey(false, privateKey2)
        val key3 = mockUserKey(null, privateKey3)
        val keyStoreCrypto = mockk<KeyStoreCrypto> {
            every { decrypt(any<EncryptedByteArray>()) } answers {
                PlainByteArray(firstArg<EncryptedByteArray>().array)
            }
        }
        val unlockedKey1 = mockk<UnlockedKey>()
        val unlockedKey2 = mockk<UnlockedKey>()
        val unlockedKey3 = mockk<UnlockedKey>()
        val pgpCrypto = mockk<PGPCrypto> {
            every { unlock("key1", any()) } returns unlockedKey1
            every { unlock("key2", any()) } returns unlockedKey2
            every { unlock("key3", any()) } returns unlockedKey3
        }
        val userId = UserId("user-id")

        every { cryptoContext.keyStoreCrypto } returns keyStoreCrypto
        every { cryptoContext.pgpCrypto } returns pgpCrypto
        coEvery { userManager.getUser(userId) } returns mockk {
            every { keys } returns listOf(key1, key2, key3)
        }

        // WHEN
        val unlockedKeys = tested(userId)

        // THEN
        assertContentEquals(listOf(unlockedKey1), unlockedKeys)
    }

    private fun mockPrivateKey(
        testKey: Armored,
        testPassphrase: EncryptedByteArray? = EncryptedByteArray(byteArrayOf(1, 2, 3))
    ) = mockk<PrivateKey> {
        every { isPrimary } returns false
        every { key } returns testKey
        every { passphrase } returns testPassphrase
    }

    private fun mockUserKey(
        isActive: Boolean?,
        testPrivateKey: PrivateKey
    ): UserKey = mockk<UserKey> {
        every { active } returns isActive
        every { privateKey } returns testPrivateKey
    }
}