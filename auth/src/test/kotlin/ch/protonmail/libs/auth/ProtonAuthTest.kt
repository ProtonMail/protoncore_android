@file:Suppress("EXPERIMENTAL_API_USAGE") // Coroutines test

package ch.protonmail.libs.auth

import ch.protonmail.libs.auth.AuthResult.Failure.NoNetwork
import ch.protonmail.libs.auth.AuthResult.Success.Login
import ch.protonmail.libs.core.connection.NetworkManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertSame
import kotlin.test.Test

/**
 * Test suite for [AuthService]
 * @author Davide Farella
 */
internal class ProtonAuthTest {

    /** Test constructor for [ProtonAuth] */
    @Suppress("TestFunctionName")
    private fun AuthService(networkManager: NetworkManager = mockk()) =
        ProtonAuth(networkManager, mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk())

    @Test(expected = IllegalStateException::class)
    fun `when clientSecret is not initialized throw exception`() {
        AuthService()
    }

    @Test
    fun `doWithNetwork return NoNetwork if network is not available`() = runBlockingTest {
        ProtonAuthConfig.clientSecret = ""
        val networkManager = mockk<NetworkManager> {
            every { canUseNetwork() } returns false
        }

        val manager = AuthService(networkManager)
        assertSame(NoNetwork, manager.doWithNetwork { mockk<Login>() })
    }
}
