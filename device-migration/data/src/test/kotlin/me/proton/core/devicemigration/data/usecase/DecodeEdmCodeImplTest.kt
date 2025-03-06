package me.proton.core.devicemigration.data.usecase

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DecodeEdmCodeImplTest {
    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto
    private lateinit var tested: DecodeEdmCodeImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { keyStoreCrypto.encrypt(any<PlainByteArray>()) } answers {
            EncryptedByteArray(firstArg<PlainByteArray>().array)
        }
        tested = DecodeEdmCodeImpl(keyStoreCrypto)
    }

    @Test
    fun `decode invalid string`() {
        assertNull(tested(""))
        assertNull(tested("   "))
        assertNull(tested("::"))
        assertNull(tested("UserCode:EncryptionKey:ChildClientID"))
        assertNull(tested(":RW5jcnlwdGlvbktleQ==:ChildClientID"))
        assertNull(tested("UserCode::ChildClientID"))
        assertNull(tested("UserCode:RW5jcnlwdGlvbktleQ==:"))
        assertNull(tested("UserCode:RW5jcnlwdGlvbktleQ:ChildClientID")) // base64 padding missing
        assertNull(tested("  :RW5jcnlwdGlvbktleQ==:  "))
    }

    @Test
    fun `decode valid string`() {
        val params = tested("UserCode:RW5jcnlwdGlvbktleQ==:ChildClientID")
        assertNotNull(params)
        assertEquals("ChildClientID", params.childClientId.value)
        assertContentEquals("EncryptionKey".encodeToByteArray(), params.encryptionKey.value.array)
        assertEquals("UserCode", params.userCode.value)
    }

    @Test
    fun `decode valid string with extra`() {
        val params = tested("UserCode:RW5jcnlwdGlvbktleQ==:ChildClientID:ExtraParam")
        assertNotNull(params)
        assertEquals("ChildClientID", params.childClientId.value)
        assertContentEquals("EncryptionKey".encodeToByteArray(), params.encryptionKey.value.array)
        assertEquals("UserCode", params.userCode.value)
    }
}
