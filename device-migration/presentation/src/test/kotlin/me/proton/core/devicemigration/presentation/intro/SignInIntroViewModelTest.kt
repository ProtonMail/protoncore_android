package me.proton.core.devicemigration.presentation.intro

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.yield
import me.proton.core.auth.domain.entity.SessionForkUserCode
import me.proton.core.biometric.data.StrongAuthenticatorsResolver
import me.proton.core.biometric.domain.BiometricAuthErrorCode
import me.proton.core.biometric.domain.BiometricAuthResult
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.devicemigration.domain.entity.ChildClientId
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.devicemigration.domain.usecase.DecodeEdmCode
import me.proton.core.devicemigration.domain.usecase.PushEdmSessionFork
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.Arg.KEY_USER_ID
import me.proton.core.devicemigration.presentation.qr.QrScanOutput
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class SignInIntroViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var decodeEdmCode: DecodeEdmCode

    @MockK(relaxUnitFun = true)
    private lateinit var observabilityManager: ObservabilityManager

    @MockK
    private lateinit var pushEdmSessionFork: PushEdmSessionFork

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @MockK
    private lateinit var strongAuthenticatorsResolver: StrongAuthenticatorsResolver

    private lateinit var tested: SignInIntroViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = SignInIntroViewModel(
            context = context,
            decodeEdmCode = decodeEdmCode,
            observabilityManager = observabilityManager,
            pushEdmSessionFork = pushEdmSessionFork,
            savedStateHandle = savedStateHandle,
            strongAuthenticatorsResolver = strongAuthenticatorsResolver
        )
    }

    @Test
    fun `starting the flow with biometrics available`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(SignInIntroAction.Start)

            // THEN
            val state = awaitItem()
            assertEquals(SignInIntroState.Idle, state.state)

            val event = assertIs<SignInIntroEvent.LaunchBiometricsCheck>(state.effect?.peek())
            assertSame(strongAuthenticatorsResolver, event.resolver)
        }
    }

    @Test
    fun `on biometrics auth result success`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(SignInIntroAction.OnBiometricAuthResult(BiometricAuthResult.Success))

            // THEN
            val state = awaitItem()
            assertEquals(SignInIntroState.Idle, state.state)
            assertEquals(SignInIntroEvent.LaunchQrScanner, state.effect?.peek())
        }
    }

    @Test
    fun `on biometrics auth result user cancelled`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(
                SignInIntroAction.OnBiometricAuthResult(
                    BiometricAuthResult.AuthError(BiometricAuthErrorCode.UserCanceled, "User cancelled")
                )
            )

            // THEN
            expectNoEvents() // no state changes - state is Idle
        }
    }

    @Test
    fun `on biometrics auth result error`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(
                SignInIntroAction.OnBiometricAuthResult(
                    BiometricAuthResult.AuthError(BiometricAuthErrorCode.Lockout, "Locked out")
                )
            )

            // THEN
            val state = awaitItem()
            assertEquals(SignInIntroState.Idle, state.state)
            assertEquals(SignInIntroEvent.ErrorMessage("Locked out"), state.effect?.peek())
        }
    }

    @Test
    fun `on qr scan result success`() = coroutinesTest {
        // GIVEN
        val edmParams = EdmParams(
            ChildClientId("child-client-id"),
            EncryptionKey(EncryptedByteArray(byteArrayOf(1, 2, 3))),
            SessionForkUserCode("user-code")
        )
        coEvery { decodeEdmCode("code") } returns edmParams
        every { savedStateHandle.get<String>(KEY_USER_ID) } returns "user-id"
        coEvery { pushEdmSessionFork(any(), any()) } returns "selector"

        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(SignInIntroAction.OnQrScanResult(QrScanOutput.Success("code")))

            // THEN
            assertEquals(
                SignInIntroStateHolder(state = SignInIntroState.Verifying),
                awaitItem()
            )

            val finalState = awaitItem()
            assertEquals(SignInIntroEvent.SignedInSuccessfully, finalState.effect?.peek())
            assertEquals(SignInIntroState.SignedInSuccessfully, finalState.state)
        }
    }

    @Test
    fun `on qr scan result success with unrecognized code`() = coroutinesTest {
        // GIVEN
        coEvery { decodeEdmCode("code") } returns null
        every { context.getString(any()) } returns "error message"

        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(SignInIntroAction.OnQrScanResult(QrScanOutput.Success("code")))

            // THEN
            assertEquals(
                SignInIntroStateHolder(state = SignInIntroState.Verifying),
                awaitItem()
            )

            val finalState = awaitItem()
            assertEquals(SignInIntroEvent.ErrorMessage("error message"), finalState.effect?.peek())
            assertEquals(SignInIntroState.Idle, finalState.state)
        }
    }

    @Test
    fun `on qr scan result cancelled`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(SignInIntroAction.OnQrScanResult(QrScanOutput.Cancelled()))

            // THEN
            expectNoEvents() // no state changes - state is Idle
        }
    }

    @Test
    fun `on qr scan result manual input requested`() = coroutinesTest {
        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(SignInIntroAction.OnQrScanResult(QrScanOutput.ManualInputRequested()))

            // THEN
            val state = awaitItem()
            assertEquals(SignInIntroState.Idle, state.state)
            assertEquals(SignInIntroEvent.LaunchManualCodeInput, state.effect?.peek())
        }
    }

    @Test
    fun `on qr scan result success but pushing fork throws error`() = coroutinesTest {
        // GIVEN
        val edmParams = EdmParams(
            ChildClientId("child-client-id"),
            EncryptionKey(EncryptedByteArray(byteArrayOf(1, 2, 3))),
            SessionForkUserCode("user-code")
        )
        coEvery { decodeEdmCode("code") } returns edmParams
        every { savedStateHandle.get<String>(KEY_USER_ID) } returns "user-id"
        coEvery { pushEdmSessionFork(any(), any()) } coAnswers {
            yield()
            error("error message")
        }
        every { context.resources } returns mockk(relaxed = true)

        tested.state.test {
            assertInitialState()

            // WHEN
            tested.perform(SignInIntroAction.OnQrScanResult(QrScanOutput.Success("code")))

            // THEN
            assertEquals(
                SignInIntroStateHolder(state = SignInIntroState.Verifying),
                awaitItem()
            )

            val finalState = awaitItem()
            assertEquals(SignInIntroEvent.ErrorMessage("error message"), finalState.effect?.peek())
            assertEquals(SignInIntroState.Idle, finalState.state)
        }
    }

    private suspend fun ReceiveTurbine<SignInIntroStateHolder>.assertInitialState() {
        assertEquals(
            SignInIntroStateHolder(state = SignInIntroState.Loading),
            awaitItem()
        )
        assertEquals(
            SignInIntroStateHolder(state = SignInIntroState.Idle),
            awaitItem()
        )
    }
}
