package ch.protonmail.libs.auth

import ch.protonmail.libs.auth.TokenManager.KeyStore.PREF_REFRESH_TOKEN
import ch.protonmail.libs.auth.TokenManager.Migrator
import ch.protonmail.libs.core.preferences.UsernamePreferencesFactory
import ch.protonmail.libs.core.preferences.get
import ch.protonmail.libs.core.preferences.set
import ch.protonmail.libs.testAndroid.mocks.mockSharedPreferences
import ch.protonmail.libs.testAndroid.mocks.newMockSharedPreferences
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import kotlin.test.Test

/**
 * Test suite for [TokenManager.Migrator]
 * @author Davide Farella
 */
internal class TokenManagerMigratorTest {

    private val oldPreferences = newMockSharedPreferences
    private val tokenManagerPreferences = newMockSharedPreferences
    private val migrator = Migrator(oldPreferences, tokenManagerPreferences)

    @Before
    fun setupPreferences() {
        oldPreferences[PREF_REFRESH_TOKEN] = "token"
    }

    @Test
    fun `migrateFrom copy preferences correctly`() {
        assertNull(tokenManagerPreferences.get<String?>(PREF_REFRESH_TOKEN))
        migrator()
        assertEquals("token", tokenManagerPreferences.get<String?>(PREF_REFRESH_TOKEN))
    }

    @Test
    fun `migrateFrom clear old pref and runs once`() {
        assertEquals("token", oldPreferences.get<String?>(PREF_REFRESH_TOKEN))
        migrator()
        assertNull(oldPreferences.get<String?>(PREF_REFRESH_TOKEN))

        // Getting the editor in the `verify` block will verify the calls on
        // `SharedPreferences.edit()`, so we need to get the editor before the `verify` calls.
        // The mocked preferences always return the same editor, so any operation is run on the
        // same instance
        val editor = tokenManagerPreferences.edit()
        verify(exactly = 1) { editor.putString(PREF_REFRESH_TOKEN, "token") }
        migrator()
        verify(exactly = 1) { editor.putString(PREF_REFRESH_TOKEN, "token") }
    }
}

/**
 * Test suite for [TokenManager.Provider]s
 * @author Davide Farella
 */
internal class TokenManagerProvidersTest {

    private val mockMultiUserPreferencesFactory = object : UsernamePreferencesFactory() {
        override fun invoke() = newMockSharedPreferences
    }

    @Test
    fun `base Provider works correctly`() {
        val provider = TokenManager.Provider(mockk()) { mockSharedPreferences }
        assertNotNull(provider())
    }

    @Test
    fun `multi user provider returns right instance by direct username request`() {
        val provider = TokenManager.MultiUserProvider(mockk(), mockMultiUserPreferencesFactory)

        val first = provider()
        val second = provider()
        assertSame(first, second)

        val third = provider("username")
        assertNotSame(second, third)
        val forth = provider()
        assertSame(third, forth)

        val fifth = provider("another username")
        assertNotSame(forth, fifth)
    }

    @Test
    fun `multi user provider returns right instance by username change`() {
        val provider = TokenManager.MultiUserProvider(mockk(), mockMultiUserPreferencesFactory)

        val first = provider()
        val second = provider()
        assertSame(first, second)

        provider.username = "username"
        val third = provider()
        assertNotSame(second, third)
        val forth = provider()
        assertSame(third, forth)

        provider.username = "another username"
        val fifth = provider()
        assertNotSame(forth, fifth)
    }
}
