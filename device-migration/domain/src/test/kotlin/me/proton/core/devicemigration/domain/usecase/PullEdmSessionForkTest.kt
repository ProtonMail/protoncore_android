package me.proton.core.devicemigration.domain.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.entity.SessionForkSelector
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.fork.DecryptPassphrasePayload
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.Session
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PullEdmSessionForkTest {
    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var decryptPassphrasePayload: DecryptPassphrasePayload

    private lateinit var tested: PullEdmSessionFork

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = PullEdmSessionFork(
            authRepository = authRepository,
            decryptPassphrasePayload = decryptPassphrasePayload
        )
    }

    @Test
    fun `pull forked session`() = runTest {
        // GIVEN
        val session = mockk<Session.Authenticated>()
        val passphrase = mockk<EncryptedByteArray>()
        coEvery { authRepository.getForkedSession(any()) } returns Pair("payload", session)
        coEvery { decryptPassphrasePayload(any(), any(), any(), any()) } returns passphrase

        // WHEN
        tested(
            encryptionKey = EncryptionKey(mockk()),
            selector = SessionForkSelector("selector")
        ).test {
            // THEN
            assertEquals(PullEdmSessionFork.Result.Loading, awaitItem())
            assertEquals(PullEdmSessionFork.Result.Success(passphrase, session), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `pull forked session with polling`() = runTest {
        // GIVEN
        val session = mockk<Session.Authenticated>()
        val passphrase = mockk<EncryptedByteArray>()

        val http422Exception = ApiException(ApiResult.Error.Http(422, "Invalid selector"))
        val connectionException = ApiException(ApiResult.Error.Connection())
        var counter = 0

        coEvery { authRepository.getForkedSession(any()) } answers {
            counter += 1
            when (counter) {
                1 -> throw http422Exception
                2 -> throw connectionException
                else -> Pair("payload", session)
            }
        }
        coEvery { decryptPassphrasePayload(any(), any(), any(), any()) } returns passphrase

        // WHEN
        tested(
            encryptionKey = EncryptionKey(mockk()),
            selector = SessionForkSelector("selector")
        ).test {
            // THEN
            assertEquals(PullEdmSessionFork.Result.Loading, awaitItem())
            assertEquals(PullEdmSessionFork.Result.Awaiting, awaitItem())

            assertEquals(PullEdmSessionFork.Result.Loading, awaitItem())
            assertEquals(PullEdmSessionFork.Result.NoConnection, awaitItem())

            assertEquals(PullEdmSessionFork.Result.Loading, awaitItem())
            assertEquals(PullEdmSessionFork.Result.Success(passphrase, session), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `pull forked session with unrecoverable exception`() = runTest {
        // GIVEN
        val passphrase = mockk<EncryptedByteArray>()

        val exception = Exception("Something went wrong")
        coEvery { authRepository.getForkedSession(any()) } throws exception
        coEvery { decryptPassphrasePayload(any(), any(), any(), any()) } returns passphrase

        // WHEN
        tested(
            encryptionKey = EncryptionKey(mockk()),
            selector = SessionForkSelector("selector")
        ).test {
            // THEN
            assertEquals(PullEdmSessionFork.Result.Loading, awaitItem())
            assertEquals(PullEdmSessionFork.Result.UnrecoverableError(exception), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `pull forked session with unrecoverable http error`() = runTest {
        // GIVEN
        val httpException = ApiException(ApiResult.Error.Http(500, "Server error"))
        val passphrase = mockk<EncryptedByteArray>()

        coEvery { authRepository.getForkedSession(any()) } throws httpException
        coEvery { decryptPassphrasePayload(any(), any(), any(), any()) } returns passphrase

        // WHEN
        tested(
            encryptionKey = EncryptionKey(mockk()),
            selector = SessionForkSelector("selector")
        ).test {
            // THEN
            assertEquals(PullEdmSessionFork.Result.Loading, awaitItem())
            assertEquals(PullEdmSessionFork.Result.UnrecoverableError(httpException), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `pull forked session with runtime error`() = runTest {
        // GIVEN
        val passphrase = mockk<EncryptedByteArray>()

        val exception = RuntimeException("Something went wrong")
        coEvery { authRepository.getForkedSession(any()) } throws exception
        coEvery { decryptPassphrasePayload(any(), any(), any(), any()) } returns passphrase

        // WHEN
        tested(
            encryptionKey = EncryptionKey(mockk()),
            selector = SessionForkSelector("selector")
        ).test {
            // THEN
            assertEquals(PullEdmSessionFork.Result.Loading, awaitItem())
            assertEquals(PullEdmSessionFork.Result.UnrecoverableError(exception), awaitItem())
            awaitComplete()
        }
    }
}
