package me.proton.core.usersettings.presentation.compose.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.usersettings.domain.usecase.ObserveRegisteredSecurityKeys
import me.proton.core.usersettings.presentation.compose.SecurityKeysRoutes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityKeysInfoViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var observeRegisteredSecurityKeys: ObserveRegisteredSecurityKeys

    private val testUserId = UserId("test_user_id")

    private lateinit var tested: SecurityKeysInfoViewModel

    private lateinit var savedStateHandle: SavedStateHandle

    @BeforeTest
    fun setUp() {
        savedStateHandle = mockk {
            every { this@mockk.get<String>(SecurityKeysRoutes.Arg.KEY_USER_ID) } returns testUserId.id
        }
        MockKAnnotations.init(this)
    }

    @Test
    fun `security keys exist`() = coroutinesTest {
        // GIVEN
        coEvery { observeRegisteredSecurityKeys.invoke(testUserId, any()) } returns flowOf(
            listOf(
                Fido2RegisteredKey("format", UByteArray(10), "Test key 1"),
                Fido2RegisteredKey("format", UByteArray(10), "Test key 2"),
                Fido2RegisteredKey("format", UByteArray(10), "Test key 3"),
            )
        )
        tested = SecurityKeysInfoViewModel(savedStateHandle, observeRegisteredSecurityKeys)
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SecurityKeysState.Loading, awaitItem())
            val nextItem = awaitItem()
            assertIs<SecurityKeysState.Success>(nextItem)
            assertEquals(3, (nextItem as SecurityKeysState.Success).keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `security keys empty`() = coroutinesTest {
        // GIVEN
        coEvery { observeRegisteredSecurityKeys.invoke(testUserId, any()) } returns flowOf(emptyList())
        tested = SecurityKeysInfoViewModel(savedStateHandle, observeRegisteredSecurityKeys)
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(SecurityKeysState.Loading, awaitItem())
            val nextItem = awaitItem()
            assertIs<SecurityKeysState.Success>(nextItem)
            assertEquals(0, (nextItem as SecurityKeysState.Success).keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}