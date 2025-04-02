/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.presentation.signin

import android.content.Context
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.auth.domain.entity.EncryptedAuthSecret
import me.proton.core.auth.domain.entity.SessionForkSelector
import me.proton.core.auth.domain.entity.SessionForkUserCode
import me.proton.core.auth.domain.usecase.CreateLoginSessionFromFork
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.devicemigration.domain.entity.ChildClientId
import me.proton.core.devicemigration.domain.entity.EdmCodeResult
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.devicemigration.domain.usecase.ObserveEdmCode
import me.proton.core.devicemigration.domain.usecase.PullEdmSessionFork
import me.proton.core.devicemigration.presentation.qr.QrBitmapGenerator
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignInViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var createLoginSessionFromFork: CreateLoginSessionFromFork

    @MockK
    private lateinit var observeEdmCode: ObserveEdmCode

    @MockK
    private lateinit var postLoginAccountSetup: PostLoginAccountSetup

    @MockK
    private lateinit var pullEdmSessionFork: PullEdmSessionFork

    @MockK
    private lateinit var qrBitmapGenerator: QrBitmapGenerator

    private lateinit var tested: SignInViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getString(any()) } returns "string-resource"
        coEvery { qrBitmapGenerator.invoke(any(), any(), any(), any()) } returns mockk()
        tested = SignInViewModel(
            accountType = mockk(),
            context = context,
            createLoginSessionFromFork = createLoginSessionFromFork,
            observeEdmCode = observeEdmCode,
            postLoginAccountSetup = postLoginAccountSetup,
            pullEdmSessionFork = pullEdmSessionFork,
            qrBitmapGenerator = qrBitmapGenerator
        )
    }

    @Test
    fun `happy path`() = coroutinesTest {
        // GIVEN
        val passphrase = EncryptedByteArray(byteArrayOf(1, 2, 3))
        val testUserId = UserId("user-id")
        val session = mockk<Session.Authenticated> {
            every { userId } returns testUserId
        }

        coJustRun { createLoginSessionFromFork(any(), any(), any()) }
        coEvery {
            postLoginAccountSetup(any(), any<EncryptedAuthSecret>(), any(), any(), any(), any())
        } returns PostLoginAccountSetup.Result.AccountReady(testUserId)
        every { pullEdmSessionFork(any(), any(), any()) } returns flowOf(
            PullEdmSessionFork.Result.Awaiting,
            PullEdmSessionFork.Result.Success(passphrase, session)
        )
        every { observeEdmCode(any()) } returns flowOf(
            EdmCodeResult(
                EdmParams(
                    ChildClientId("client-id"),
                    EncryptionKey(EncryptedByteArray(byteArrayOf(4, 5, 6))),
                    SessionForkUserCode("user-code")
                ),
                "qr-code",
                SessionForkSelector("selector")
            )
        )

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SignInState.Loading, awaitItem())
            assertEquals(
                SignInState.Idle(qrCode = "qr-code", generateBitmap = qrBitmapGenerator::invoke),
                awaitItem()
            )
            // loading while performing post-login actions:
            assertEquals(SignInState.Loading, awaitItem())

            val state = awaitItem()
            assertIs<SignInState.SuccessfullySignedIn>(state)
            val signedInEvent = assertIs<SignInEvent.SignedIn>(state.effect.peek())
            assertEquals(session.userId, signedInEvent.userId)
        }
    }

    @Test
    fun `error when observing qr code`() = coroutinesTest {
        // GIVEN
        every { observeEdmCode(any()) } returns flow {
            error("error")
        }

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SignInState.Loading, awaitItem())
            assertIs<SignInState.Failure>(awaitItem())
        }
    }

    @Test
    fun `error when pulling fork`() = coroutinesTest {
        // GIVEN
        every { observeEdmCode(any()) } returns flowOf(
            EdmCodeResult(mockk(relaxed = true), "qr-code", SessionForkSelector("selector"))
        )
        every { pullEdmSessionFork(any(), any(), any()) } returns flowOf(
            PullEdmSessionFork.Result.UnrecoverableError(Exception("error"))
        )

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SignInState.Loading, awaitItem())
            assertIs<SignInState.Failure>(awaitItem())
        }
    }

    @Test
    fun `awaiting the fork`() = coroutinesTest {
        // GIVEN
        val testUserId = UserId("user-id")
        val session = mockk<Session.Authenticated> {
            every { userId } returns testUserId
        }

        coJustRun { createLoginSessionFromFork(any(), any(), any()) }
        every { observeEdmCode(any()) } returns flowOf(
            EdmCodeResult(mockk(relaxed = true), "qr-code", SessionForkSelector("selector"))
        )
        coEvery {
            postLoginAccountSetup(any(), any<EncryptedAuthSecret>(), any(), any(), any(), any())
        } returns PostLoginAccountSetup.Result.AccountReady(testUserId)
        every { pullEdmSessionFork(any(), any(), any()) } returns flowOf(
            PullEdmSessionFork.Result.Loading,
            PullEdmSessionFork.Result.Awaiting,
            PullEdmSessionFork.Result.Loading,
            PullEdmSessionFork.Result.Success(mockk(), session),
        )

        // WHEN
        tested.state.test {
            // THEN
            assertIs<SignInState.Loading>(awaitItem())

            // stays Idle when pullEdmSessionFork returns Loading or Awaiting:
            assertIs<SignInState.Idle>(awaitItem())

            // back to loading when pullEdmSessionFork returns Success:
            assertIs<SignInState.Loading>(awaitItem())

            val state = awaitItem()
            assertIs<SignInState.SuccessfullySignedIn>(state)
            val signedInEvent = assertIs<SignInEvent.SignedIn>(state.effect.peek())
            assertEquals(session.userId, signedInEvent.userId)
        }
    }

    @Test
    fun `retrying the fork`() = coroutinesTest {
        // GIVEN
        every { observeEdmCode(any()) } returns flowOf(
            EdmCodeResult(mockk(relaxed = true), "qr-code", SessionForkSelector("selector"))
        )

        var pullCallCount = 0
        every { pullEdmSessionFork(any(), any(), any()) } answers {
            pullCallCount += 1
            if (pullCallCount == 1) {
                flowOf(
                    PullEdmSessionFork.Result.UnrecoverableError(Exception("error"))
                )
            } else {
                flowOf(
                    PullEdmSessionFork.Result.Success(mockk(), mockk()),
                )
            }
        }

        // WHEN
        tested.state.test {
            // THEN
            assertIs<SignInState.Loading>(awaitItem())

            val state1 = awaitItem()
            assertIs<SignInState.Failure>(state1)

            // WHEN
            state1.onRetry?.invoke()

            // THEN
            assertIs<SignInState.Loading>(awaitItem())
        }
    }
}
