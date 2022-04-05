/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.crypto.validator.presentation

import android.app.Application
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.validator.domain.prefs.CryptoPrefs
import me.proton.core.crypto.validator.presentation.ui.CryptoValidatorErrorDialogActivity
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class CryptoValidatorTest : ArchTest, CoroutinesTest {

    lateinit var keyStoreValidator: CryptoValidator
    private val application = mockk<Application>()
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val cryptoPrefs = mockk<CryptoPrefs>(relaxed = true)

    private var activityCallback: Application.ActivityLifecycleCallbacks? = null

    @Before
    fun setup() {
        mockkObject(CryptoValidatorErrorDialogActivity.Companion)
        every { application.registerActivityLifecycleCallbacks(any()) } answers {
            activityCallback = firstArg() as? Application.ActivityLifecycleCallbacks
        }
        every { keyStoreCrypto.isUsingKeyStore() } returns true
        every { CryptoValidatorErrorDialogActivity.show(any()) } returns Unit
        keyStoreValidator = CryptoValidator(
            application,
            keyStoreCrypto,
            cryptoPrefs
        )
    }

    @After
    fun tearDown() {
        unmockkObject(CryptoValidatorErrorDialogActivity.Companion)
    }

    @Test
    fun `Will return if keystore is secure`() {
        // GIVEN
        every { keyStoreCrypto.isUsingKeyStore() } returns true
        // WHEN
        keyStoreValidator.validate()
        activityCallback?.callOnActivityResumed()
        // THEN
        verify { keyStoreCrypto.isUsingKeyStore() }
        verify(exactly = 0) { CryptoValidatorErrorDialogActivity.show(any()) }
    }

    @Test
    fun `Won't validate if insecure keystore option was enabled`() {
        // GIVEN
        every { cryptoPrefs.useInsecureKeystore } returns true
        // WHEN
        keyStoreValidator.validate()
        activityCallback?.callOnActivityResumed()
        // THEN
        verify(exactly = 0) { keyStoreCrypto.isUsingKeyStore() }
    }

    @Test
    fun `Waits until app is in foreground`() = runBlockingTest {
        // GIVEN
        every { cryptoPrefs.useInsecureKeystore } returns false
        // WHEN
        keyStoreValidator.validate()
        // THEN
        verify(exactly = 0) { CryptoValidatorErrorDialogActivity.show(any()) }
    }

    @Test
    fun `If KeyStoreCrypto is not using KeyStore, shows an error screen`() = runBlockingTest {
        // GIVEN
        every { keyStoreCrypto.isUsingKeyStore() } returns false
        every { cryptoPrefs.useInsecureKeystore } returns false
        // WHEN
        keyStoreValidator.validate()
        activityCallback?.callOnActivityResumed()
        // THEN
        verify { CryptoValidatorErrorDialogActivity.show(any()) }
    }
}

private fun Application.ActivityLifecycleCallbacks.callOnActivityResumed() {
    onActivityResumed(mockk())
}
