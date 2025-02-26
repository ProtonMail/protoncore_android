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

package me.proton.core.biometric.presentation

import android.text.TextUtils
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import me.proton.core.biometric.domain.AuthenticatorsResolver
import me.proton.core.biometric.domain.BiometricAuthenticator.DeviceCredential
import me.proton.core.biometric.domain.BiometricAuthenticator.Strong
import me.proton.core.biometric.domain.BiometricAuthenticator.Weak
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BiometricAuthLauncherImplTest {
    @BeforeTest
    fun setUp() {
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } answers {
            firstArg<CharSequence?>().isNullOrEmpty()
        }
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `launching biometric auth`() {
        // GIVEN
        val prompt = mockk<BiometricPrompt>(relaxUnitFun = true)
        val tested = BiometricAuthLauncherImpl(prompt)

        // WHEN
        tested.launch(
            title = "Auth",
            subtitle = "Authenticate now",
            cancelButton = "Cancel",
            confirmationRequired = true,
            allowedAuthenticators = setOf(DeviceCredential, Strong),
            authenticatorsResolver = AuthenticatorsResolver { it }
        )

        // THEN
        val infoSlot = slot<PromptInfo>()
        verify { prompt.authenticate(capture(infoSlot)) }
        assertEquals("Auth", infoSlot.captured.title)
        assertEquals("Authenticate now", infoSlot.captured.subtitle)
        assertEquals("", infoSlot.captured.negativeButtonText) // Not set if DeviceCredential is allowed.
        assertEquals(
            DEVICE_CREDENTIAL or BIOMETRIC_STRONG,
            infoSlot.captured.allowedAuthenticators
        )
        assertEquals(true, infoSlot.captured.isConfirmationRequired)
    }

    @Test
    fun `launching biometric auth with cancel button`() {
        // GIVEN
        val prompt = mockk<BiometricPrompt>(relaxUnitFun = true)
        val tested = BiometricAuthLauncherImpl(prompt)

        // WHEN
        tested.launch(
            title = "Auth",
            subtitle = "Authenticate now",
            cancelButton = "Cancel",
            confirmationRequired = false,
            allowedAuthenticators = setOf(Strong, Weak),
            authenticatorsResolver = AuthenticatorsResolver { it - Strong }
        )

        // THEN
        val infoSlot = slot<PromptInfo>()
        verify { prompt.authenticate(capture(infoSlot)) }
        assertEquals("Auth", infoSlot.captured.title)
        assertEquals("Authenticate now", infoSlot.captured.subtitle)
        assertEquals("Cancel", infoSlot.captured.negativeButtonText)
        assertEquals(
            BIOMETRIC_WEAK,
            infoSlot.captured.allowedAuthenticators
        )
        assertEquals(false, infoSlot.captured.isConfirmationRequired)
    }
}
