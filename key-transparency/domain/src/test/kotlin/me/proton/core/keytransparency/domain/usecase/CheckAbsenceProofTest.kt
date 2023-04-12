package me.proton.core.keytransparency.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.entity.key.Recipient
import me.proton.core.keytransparency.domain.Constants
import me.proton.core.keytransparency.domain.entity.Epoch
import me.proton.core.keytransparency.domain.entity.ProofPair
import me.proton.core.keytransparency.domain.entity.VerifiedState
import me.proton.core.keytransparency.domain.exception.KeyTransparencyException
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CheckAbsenceProofTest {
    private lateinit var checkAbsenceProof: CheckAbsenceProof
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val verifyProofInEpoch = mockk<VerifyProofInEpoch>()
    private val getCurrentTime = mockk<GetCurrentTime>()

    private val userId = UserId("test-user-id")
    private val email = "test@proton.black"
    private val signedKeyList = mockk<PublicSignedKeyList>()
    private val testEpochId = 10
    private val epoch = mockk<Epoch> {
        every { epochId } returns testEpochId
    }
    private val proofs = mockk<ProofPair>()

    private val excludeExternalAccounts = mockk<ExcludeExternalAccounts>()

    private val currentTime = 10_000L

    @BeforeTest
    fun setUp() {

        coEvery { getCurrentTime() } returns currentTime
        coEvery { keyTransparencyRepository.getLastEpoch(userId) } returns epoch
        coEvery { keyTransparencyRepository.getProof(userId, testEpochId, email) } returns proofs
        every { excludeExternalAccounts() } returns false
        checkAbsenceProof = CheckAbsenceProof(
            keyTransparencyRepository,
            verifyProofInEpoch,
            excludeExternalAccounts,
            getCurrentTime
        )
    }

    @Test
    fun `external addresses are not checked`() = runTest {
        // given
        val externalEmail = "external.test@gmail.com"
        val expected = VerifiedState.Absent(currentTime)
        coEvery { excludeExternalAccounts() } returns true
        // when
        val actual = checkAbsenceProof(userId, externalEmail, null, Recipient.External)
        // then
        assertEquals(expected, actual)
        coVerify(exactly = 0) {
            verifyProofInEpoch(any(), any(), any(), any())
            keyTransparencyRepository.getLastEpoch(userId)
            keyTransparencyRepository.getProof(userId, testEpochId, externalEmail)
        }
    }

    @Test
    fun `passes if it's absent`() = runTest {
        // given
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val expected = VerifiedState.Absent(certificateTime)
        coEvery { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) } returns expected
        // when
        val actual = checkAbsenceProof(userId, email, signedKeyList, Recipient.Internal)
        // then
        assertEquals(expected, actual)
    }

    @Test
    fun `passes if it's obsolete`() = runTest {
        // given
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val expected = VerifiedState.Obsolete(certificateTime)
        coEvery { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) } returns expected
        // when
        val actual = checkAbsenceProof(userId, email, signedKeyList, Recipient.Internal)
        // then
        assertEquals(expected, actual)
    }

    @Test
    fun `fails if it's existing`() = runTest {
        // given
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS + 1000
        val expected = VerifiedState.Existent(certificateTime)
        coEvery { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) } returns expected
        // when & then
        assertFailsWith<KeyTransparencyException> {
            checkAbsenceProof(userId, email, signedKeyList, Recipient.Internal)
        }
    }

    @Test
    fun `fails if it's not yet included`() = runTest {
        // given
        val expected = VerifiedState.NotYetIncluded
        coEvery { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) } returns expected
        // when & then
        assertFailsWith<KeyTransparencyException> {
            checkAbsenceProof(userId, email, signedKeyList, Recipient.Internal)
        }
    }

    @Test
    fun `fails if it's too old`() = runTest {
        // given
        val currentTime = 10_000L
        coEvery { getCurrentTime() } returns currentTime
        val certificateTime = currentTime - Constants.KT_MAX_EPOCH_INTERVAL_SECONDS - 1000
        val expected = VerifiedState.Existent(certificateTime)
        coEvery { verifyProofInEpoch.invoke(email, signedKeyList, epoch, proofs) } returns expected
        // when & then
        assertFailsWith<KeyTransparencyException> {
            checkAbsenceProof(userId, email, signedKeyList, Recipient.Internal)
        }
    }
}
