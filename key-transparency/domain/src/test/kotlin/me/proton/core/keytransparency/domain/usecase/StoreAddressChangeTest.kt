package me.proton.core.keytransparency.domain.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails

class StoreAddressChangeTest {

    private val verifySignedKeyListSignature: VerifySignedKeyListSignature = mockk()
    private val getVerificationPublicKeys: GetVerificationPublicKeys = mockk()
    private val getCurrentTime = mockk<GetCurrentTime>()
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private lateinit var storeAddressChange: StoreAddressChange

    private val currentTime = 10_000L

    @BeforeTest
    fun setUp() {
        coEvery { getCurrentTime() } returns currentTime
        storeAddressChange = StoreAddressChange(
            verifySignedKeyListSignature,
            getVerificationPublicKeys,
            getCurrentTime,
            keyTransparencyRepository
        )
    }

    @Test
    fun `user address - store an address change locally, no change stored yet`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "test@proton.black"
        val testAddressId = AddressId("address-id")
        val expectedMinEpochId = 10
        val address = mockk<UserAddress> {
            every { addressId } returns testAddressId
            every { email } returns testEmail
            every { signedKeyList?.expectedMinEpochId } returns expectedMinEpochId
        }
        val skl = mockk<PublicSignedKeyList>()
        val creationTimestamp = 10L
        val publicKeys = listOf("public-key")
        every { getVerificationPublicKeys(address) } returns publicKeys
        every { verifySignedKeyListSignature(address, skl) } returns creationTimestamp
        coEvery { keyTransparencyRepository.getAddressChangesForAddress(userId, testEmail) } returns emptyList()
        coJustRun {
            keyTransparencyRepository.removeAddressChangesForAddress(userId, testEmail)
        }
        coJustRun {
            keyTransparencyRepository.storeAddressChange(any())
        }
        // when
        storeAddressChange(userId, address, skl)
        // then
        coVerify(exactly = 1) {
            keyTransparencyRepository.removeAddressChangesForAddress(userId, testEmail)
            keyTransparencyRepository.storeAddressChange(
                match {
                    it.userId == userId &&
                        it.email == testEmail &&
                        it.epochId == expectedMinEpochId &&
                        it.creationTimestamp == creationTimestamp &&
                        it.publicKeys == publicKeys
                }
            )
        }
    }

    @Test
    fun `public address - if the skl needs an expected min epoch id`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "test@proton.black"
        val address = mockk<PublicAddress> {
            every { email } returns testEmail
        }
        val skl = mockk<PublicSignedKeyList> {
            every { expectedMinEpochId } returns null
        }
        // when
        assertFails { storeAddressChange(userId, address, skl) }
    }

    @Test
    fun `public address - the skl needs a valid signature & timestamp`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "test@proton.black"
        val address = mockk<PublicAddress> {
            every { email } returns testEmail
        }
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data" // not obsolete skl
            every { expectedMinEpochId } returns 10
        }
        coEvery {
            verifySignedKeyListSignature(address, skl)
        } throws KeyTransparencyException("test error: invalid sig")
        // when
        assertFails { storeAddressChange(userId, address, skl) }
        // then
        verify { verifySignedKeyListSignature(address, skl) }
    }

    @Test
    fun `public address - if the change is already in LS, we don't store it`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "test@proton.black"
        val address = mockk<PublicAddress> {
            every { email } returns testEmail
        }
        val expectedMinEpochIdVal = 10
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data" // not obsolete skl
            every { expectedMinEpochId } returns expectedMinEpochIdVal
        }
        val timestamp = 100L
        coEvery { verifySignedKeyListSignature(address, skl) } returns timestamp
        coEvery { keyTransparencyRepository.getAddressChangesForAddress(userId, testEmail) } returns listOf(
            mockk {
                every { creationTimestamp } returns timestamp // change is already in LS
            }
        )
        val publicKeys = listOf("public-key")
        every { getVerificationPublicKeys(address) } returns publicKeys
        // when
        storeAddressChange(userId, address, skl)
        // then
        coVerify {
            verifySignedKeyListSignature(address, skl)
        }
        coVerify(exactly = 0) {
            keyTransparencyRepository.storeAddressChange(any())
        }
    }

    @Test
    fun `public address - if the change is not in LS, we store it`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "test@proton.black"
        val address = mockk<PublicAddress> {
            every { email } returns testEmail
        }
        val expectedMinEpochIdVal = 10
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data" // not obsolete skl
            every { expectedMinEpochId } returns expectedMinEpochIdVal
        }
        val timestamp = 100L
        coEvery { verifySignedKeyListSignature(address, skl) } returns timestamp
        coEvery { keyTransparencyRepository.getAddressChangesForAddress(userId, testEmail) } returns listOf(
            mockk {
                every { creationTimestamp } returns timestamp - 100
            }
        )
        val publicKeys = listOf("public-key")
        every { getVerificationPublicKeys(address) } returns publicKeys
        coJustRun { keyTransparencyRepository.storeAddressChange(any()) }
        // when
        storeAddressChange(userId, address, skl)
        // then
        coVerify {
            verifySignedKeyListSignature(address, skl)
            keyTransparencyRepository.storeAddressChange(
                match {
                    it.creationTimestamp == timestamp &&
                        it.email == testEmail &&
                        it.epochId == expectedMinEpochIdVal &&
                        it.publicKeys == publicKeys
                }
            )
        }
    }

    @Test
    fun `public address - use current time for obsolete skl`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "test@proton.black"
        val address = mockk<PublicAddress> {
            every { email } returns testEmail
        }
        val expectedMinEpochIdVal = 10
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns null // obsolete skl
            every { expectedMinEpochId } returns expectedMinEpochIdVal
        }
        val timestamp = currentTime
        coEvery { keyTransparencyRepository.getAddressChangesForAddress(userId, testEmail) } returns listOf(
            mockk {
                every { creationTimestamp } returns timestamp - 100
            }
        )
        val publicKeys = listOf("public-key")
        every { getVerificationPublicKeys(address) } returns publicKeys
        coJustRun { keyTransparencyRepository.storeAddressChange(any()) }
        // when
        storeAddressChange(userId, address, skl, isObsolete = true)
        // then
        coVerify {
            keyTransparencyRepository.storeAddressChange(
                match {
                    it.creationTimestamp == timestamp &&
                        it.email == testEmail &&
                        it.epochId == expectedMinEpochIdVal &&
                        it.publicKeys == publicKeys
                }
            )
        }
    }

    @Test
    fun `public address - if the change is older than what's already in LS, we throw an error`() = runTest {
        // given
        val userId = UserId("test-user-id")
        val testEmail = "test@proton.black"
        val address = mockk<PublicAddress> {
            every { email } returns testEmail
        }
        val expectedMinEpochIdVal = 10
        val skl = mockk<PublicSignedKeyList> {
            every { data } returns "data" // not obsolete skl
            every { expectedMinEpochId } returns expectedMinEpochIdVal
        }
        val timestamp = 100L
        coEvery { verifySignedKeyListSignature(address, skl) } returns timestamp
        coEvery { keyTransparencyRepository.getAddressChangesForAddress(userId, testEmail) } returns listOf(
            mockk {
                every { creationTimestamp } returns timestamp + 1 // change in LS is more recent than one from API
            }
        )
        val publicKeys = listOf("public-key")
        every { getVerificationPublicKeys(address) } returns publicKeys
        // when
        assertFails {
            storeAddressChange(userId, address, skl)
        }
        // then
        coVerify {
            verifySignedKeyListSignature(address, skl)
        }
        coVerify(exactly = 0) {
            keyTransparencyRepository.storeAddressChange(any())
        }
    }
}
