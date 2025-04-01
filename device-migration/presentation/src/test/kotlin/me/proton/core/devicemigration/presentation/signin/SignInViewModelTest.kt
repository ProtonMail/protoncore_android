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
import android.content.res.Resources
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.auth.domain.entity.SessionForkSelector
import me.proton.core.auth.domain.entity.SessionForkUserCode
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.devicemigration.domain.entity.ChildClientId
import me.proton.core.devicemigration.domain.entity.EdmCodeResult
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.devicemigration.domain.usecase.ObserveEdmCode
import me.proton.core.devicemigration.domain.usecase.PullEdmSessionFork
import me.proton.core.devicemigration.presentation.qr.QrBitmapGenerator
import me.proton.core.network.domain.session.Session
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SignInViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var observeEdmCode: ObserveEdmCode

    @MockK
    private lateinit var pullEdmSessionFork: PullEdmSessionFork

    @MockK
    private lateinit var qrBitmapGenerator: QrBitmapGenerator

    private lateinit var tested: SignInViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { resources.getString(any()) } returns "error-from-resources"
        every { context.resources } returns resources
        coEvery { qrBitmapGenerator.invoke(any(), any(), any(), any()) } returns mockk()
        tested = SignInViewModel(observeEdmCode, pullEdmSessionFork, qrBitmapGenerator)
    }

    @Test
    fun `happy path`() = coroutinesTest {
        // GIVEN
        val passphrase = EncryptedByteArray(byteArrayOf(1, 2, 3))
        val session = mockk<Session.Authenticated>()
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
            assertEquals(SignInStateHolder(state = SignInState.Loading), awaitItem())
            assertEquals(
                SignInStateHolder(
                    state = SignInState.Idle(
                        qrCode = "qr-code",
                        generateBitmap = qrBitmapGenerator::invoke
                    )
                ), awaitItem()
            )
            // loading while performing post-login actions:
            assertEquals(SignInStateHolder(state = SignInState.Loading), awaitItem())

            // TODO verify logged in
//            val (effect, state) = awaitItem()
//            val signedInEvent = assertIs<SignInEvent.SignedIn>(effect?.peek())
//            assertEquals(session.userId, signedInEvent.userId)
//            assertEquals(SignInState.SuccessfullySignedIn, state)
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
            assertEquals(SignInStateHolder(state = SignInState.Loading), awaitItem())
            val (effect, state) = awaitItem()
            assertNull(effect)
            assertIs<SignInState.UnrecoverableError>(state)
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
            assertEquals(SignInStateHolder(state = SignInState.Loading), awaitItem())
            val (effect, state) = awaitItem()
            assertNull(effect)
            assertIs<SignInState.UnrecoverableError>(state)
        }
    }

    @Test
    fun `awaiting the fork`() = coroutinesTest {
        // GIVEN
        every { observeEdmCode(any()) } returns flowOf(
            EdmCodeResult(mockk(relaxed = true), "qr-code", SessionForkSelector("selector"))
        )
        every { pullEdmSessionFork(any(), any(), any()) } returns flowOf(
            PullEdmSessionFork.Result.Loading,
            PullEdmSessionFork.Result.Awaiting,
            PullEdmSessionFork.Result.Loading,
            PullEdmSessionFork.Result.Success(mockk(), mockk()),
        )

        // WHEN
        tested.state.test {
            // THEN
            assertIs<SignInState.Loading>(awaitItem().state)

            // stays Idle when pullEdmSessionFork returns Loading or Awaiting:
            assertIs<SignInState.Idle>(awaitItem().state)

            // back to loading when pullEdmSessionFork returns Success:
            assertIs<SignInState.Loading>(awaitItem().state)
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
            assertIs<SignInState.Loading>(awaitItem().state)

            val (effect1, state1) = awaitItem()
            assertNull(effect1)
            assertIs<SignInState.UnrecoverableError>(state1)

            // WHEN
            state1.onRetry()

            // THEN
            assertIs<SignInState.Loading>(awaitItem().state)
        }
    }
}