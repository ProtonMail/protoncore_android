package me.proton.core.auth.presentation.viewmodel

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.feature.IsCredentialLessEnabled
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AddAccountViewModelTest {
    @MockK
    private lateinit var isCredentialLessEnabled: IsCredentialLessEnabled
    private lateinit var tested: AddAccountViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = AddAccountViewModel(isCredentialLessEnabled)
    }

    @Test
    fun credentialLessDisabled() = runTest {
        every { isCredentialLessEnabled(any()) } returns false
        assertEquals(AddAccountViewModel.Screen.AddAccountFragment, tested.getNextScreen())
    }

    @Test
    fun credentialLessEnabled() = runTest {
        every { isCredentialLessEnabled(any()) } returns true
        assertEquals(AddAccountViewModel.Screen.CredentialLessFragment, tested.getNextScreen())
    }
}