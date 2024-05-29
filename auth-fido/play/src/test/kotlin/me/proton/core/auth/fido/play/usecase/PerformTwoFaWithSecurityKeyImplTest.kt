package me.proton.core.auth.fido.play.usecase

import android.app.Activity
import android.app.PendingIntent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.Fido2ApiClient
import com.google.android.gms.tasks.OnSuccessListener
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.auth.fido.play.usecase.PerformTwoFaWithSecurityKeyImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalUnsignedTypes::class)
class PerformTwoFaWithSecurityKeyImplTest {
    private lateinit var tested: PerformTwoFaWithSecurityKeyImpl

    @BeforeTest
    fun setUp() {
        mockkStatic(Fido::class)
        tested = PerformTwoFaWithSecurityKeyImpl()
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `register and invoke`() = runTest {
        // GIVEN
        val callbackSlot = slot<ActivityResultCallback<ActivityResult>>()
        val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>> {
            every { launch(any()) } answers {
                callbackSlot.captured.onActivityResult(ActivityResult(Activity.RESULT_OK, null))
            }
        }
        val activity = mockk<ComponentActivity> {
            every {
                registerForActivityResult(
                    any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(),
                    capture(callbackSlot)
                )
            } returns launcher
        }
        val onResult =
            mockk<(PerformTwoFaWithSecurityKey.Result, Fido2PublicKeyCredentialRequestOptions) -> Unit>(relaxed = true)
        val onSuccessSlot = slot<OnSuccessListener<PendingIntent>>()
        val fidoClient = mockk<Fido2ApiClient> {
            every { getSignPendingIntent(any()) } returns mockk {
                every { addOnSuccessListener(capture(onSuccessSlot)) } returns this
                every { addOnCanceledListener(any()) } returns this
                every { addOnFailureListener(any()) } returns this
            }
        }
        every { Fido.getFido2ApiClient(activity) } returns fidoClient

        // WHEN
        tested.register(activity, onResult)
        val launchResult = async(start = CoroutineStart.UNDISPATCHED) {
            tested.invoke(
                activity, Fido2PublicKeyCredentialRequestOptions(
                    ubyteArrayOf(1U, 2U, 3U),
                    timeout = 600_000U,
                    rpId = "example.test"
                )
            )
        }
        onSuccessSlot.captured.onSuccess(mockk<PendingIntent> { every { intentSender } returns mockk() })

        // THEN
        assertEquals(PerformTwoFaWithSecurityKey.LaunchResult.Success, launchResult.await())
        verify { launcher.launch(any()) }
        verify { onResult(PerformTwoFaWithSecurityKey.Result.EmptyResult, any()) }
    }
}
