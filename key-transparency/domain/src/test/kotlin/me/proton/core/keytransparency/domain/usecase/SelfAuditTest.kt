package me.proton.core.keytransparency.domain.usecase

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.entity.AddressChange
import me.proton.core.keytransparency.domain.entity.AddressChangeAuditResult
import me.proton.core.keytransparency.domain.entity.SelfAuditResult
import me.proton.core.keytransparency.domain.entity.UserAddressAuditResult
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.user.domain.entity.UserAddress
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SelfAuditTest {

    private val repository: KeyTransparencyRepository = mockk()
    private val verifyAddressChangeWasIncluded: VerifyAddressChangeWasIncluded = mockk()
    private val auditUserAddress: AuditUserAddress = mockk()
    private val userId = UserId("test-user-id")

    private val testTimestamp = 10L
    private val getCurrentTime = mockk<GetCurrentTime>()
    private lateinit var selfAudit: SelfAudit

    @BeforeTest
    fun setUp() {
        coEvery { getCurrentTime() } returns testTimestamp
        selfAudit = SelfAudit(
            repository,
            verifyAddressChangeWasIncluded,
            auditUserAddress,
            getCurrentTime
        )
    }

    @Test
    fun testSelfAudit() = runTest {
        // given
        val userAddress1 = mockk<UserAddress> { every { email } returns "email1@proton.black" }
        val userAddress2 = mockk<UserAddress> { every { email } returns "email2@proton.black" }
        val addressChange1 = mockk<AddressChange> { every { email } returns userAddress1.email }
        val addressChange2 = mockk<AddressChange> {
            every { email } returns "contact@proton.black"
        }
        val blobs = listOf(
            addressChange1,
            addressChange2
        )
        coEvery { repository.getAllAddressChanges(userId) } returns blobs
        val auditResult1 = UserAddressAuditResult.Success
        val auditResult2 = UserAddressAuditResult.Warning.Disabled
        coEvery { auditUserAddress(userId, userAddress1) } returns auditResult1
        coEvery { auditUserAddress(userId, userAddress2) } returns auditResult2
        coJustRun { verifyAddressChangeWasIncluded(userId, addressChange1) }
        coJustRun { verifyAddressChangeWasIncluded(userId, addressChange2) }
        // when
        val selfAuditResult = selfAudit(userId, listOf(userAddress1, userAddress2))
        // then
        coVerify(exactly = 1) {
            auditUserAddress(userId, userAddress1)
            auditUserAddress(userId, userAddress2)
            verifyAddressChangeWasIncluded(userId, addressChange1)
            verifyAddressChangeWasIncluded(userId, addressChange2)
        }
        assertEquals(testTimestamp, selfAuditResult.timestamp)
        assertIs<SelfAuditResult.Success>(selfAuditResult)
        assertEquals(
            mapOf(
                userAddress1 to auditResult1,
                userAddress2 to auditResult2
            ),
            selfAuditResult.selfAddressAudits
        )
        assertEquals(
            mapOf(
                addressChange1.email to AddressChangeAuditResult.Success,
                addressChange2.email to AddressChangeAuditResult.Success
            ),
            selfAuditResult.contactAudits
        )
    }
}
