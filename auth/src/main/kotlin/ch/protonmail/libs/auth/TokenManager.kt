// Public APIs
@file:Suppress("unused")

package ch.protonmail.libs.auth

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import me.proton.android.core.data.api.entity.response.RefreshResponse
import ch.protonmail.libs.auth.TokenManager.KeyStore.PREF_ACCESS_TOKEN
import ch.protonmail.libs.auth.TokenManager.KeyStore.PREF_ENC_ACCESS_TOKEN
import ch.protonmail.libs.auth.TokenManager.KeyStore.PREF_ENC_PRIV_KEY
import ch.protonmail.libs.auth.TokenManager.KeyStore.PREF_REFRESH_TOKEN
import ch.protonmail.libs.auth.TokenManager.KeyStore.PREF_USER_UID
import ch.protonmail.libs.auth.model.request.RefreshBody
import ch.protonmail.libs.auth.model.response.LoginResponse
import ch.protonmail.libs.core.preferences.*
import ch.protonmail.libs.crypto.OpenPGP
import me.proton.android.core.data.api.BEARER_TOKEN_TYPE
import timber.log.Timber

/**
 * @constructor is private, use [TokenManager.Provider]
 * FIXME Change workflow for ensure that, when the component is available, is able to provide a
 *  decrypted token without the requirement of an addition passphrase
 *
 * @author Davide Farella
 */
internal class TokenManager @VisibleForTesting(otherwise = PRIVATE) internal constructor(
    private val openPGP: OpenPGP,
    override val preferences: SharedPreferences
) : PreferencesProvider {

    /** @return Bearer access token if [accessToken] is available, else `null` */
    val authAccessToken get() = accessToken?.let { "$BEARER_TOKEN_TYPE $accessToken" }

    private var encryptedAccessToken by string(key = PREF_ENC_ACCESS_TOKEN)
    private var accessToken by string(key = PREF_ACCESS_TOKEN)
    internal var encryptedPrivateKey by string(key = PREF_ENC_PRIV_KEY)
    private var refreshToken by string(key = PREF_REFRESH_TOKEN)
    private var uid by string(key = PREF_USER_UID)

    fun clearAccessToken() {
        accessToken = null
    }

    fun clearAll() {
        preferences.clearAll()
    }

    /** @return [RefreshBody] if [refreshToken] is available, else `null` */
    fun createRefreshBody() = refreshToken?.let {
        RefreshBody(
            it
        )
    }

    /**
     * Set [accessToken] by decrypting [encryptedAccessToken] with [encryptedPrivateKey] and given
     * [passphrase].
     * This operation can fail: [accessToken] will be set to `null`
     */
    fun decryptAccessToken(passphrase: String?) {
        accessToken = try {
            openPGP.decryptMessage(encryptedAccessToken!!, encryptedPrivateKey!!, passphrase!!)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun handleRefresh(response: RefreshResponse, passphrase: String) {
        if (response.refreshToken != null) {
            refreshToken = response.refreshToken
        }
        encryptedPrivateKey = response.privateKey
        accessToken = response.decryptedAccessToken(passphrase)
    }

    fun handleLogin(response: LoginResponse) {
        if (response.isAccessTokenArmored) {
            encryptedAccessToken = response.accessToken
        } else {
            accessToken = response.accessToken
        }
        refreshToken = response.refreshToken
        uid = response.uid
        encryptedPrivateKey = response.privateKey
    }

    /**
     * @return access token
     * If [RefreshResponse.accessToken] is armored try to decrypt it, if fails return the old
     * [TokenManager.accessToken]
     * If [RefreshResponse.accessToken] is not armored, return itself
     */
    private fun RefreshResponse.decryptedAccessToken(passphrase: String): String? {
        return if (isAccessTokenArmored) try {
            openPGP.decryptMessage(encryptedAccessToken!!, encryptedPrivateKey!!, passphrase)
        } catch (e: Exception) {
            Timber.e(e)
            this@TokenManager.accessToken
        } else /* RefreshResponse. */ accessToken
    }

    /** Simple Provider / Factory for [TokenManager] */
    open class Provider(
        private val openPGP: OpenPGP,
        protected open val preferencesFactory: PreferencesFactory
    ) {
        /** @return [TokenManager] */
        open operator fun invoke() = TokenManager(openPGP, preferencesFactory())
    }

    /**
     * Multi-user enabled [TokenManager.Provider]
     * It will keep a backup of the last generated [TokenManager] and will be reset only when
     * [username] is changed
     */
    class MultiUserProvider(
        openPGP: OpenPGP,
        override val preferencesFactory: UsernamePreferencesFactory
    ) : Provider(openPGP, preferencesFactory) {

        /** Get and set username for [UsernamePreferencesFactory] */
        var username
            get() = preferencesFactory.username
            set(value) {
                if (value != preferencesFactory.username) {
                    preferencesFactory.username = value
                    lastInstance = null
                }
            }

        private var lastInstance: TokenManager? = null

        /**
         * @return [lastInstance] of [TokenManager] if available, else create a new one if it has
         * been invalidated
         */
        @Synchronized
        override fun invoke(): TokenManager {
            lastInstance = lastInstance ?: super.invoke()
            return lastInstance!!
        }

        /**
         * @return [TokenManager] for the given [username]
         * Update the [MultiUserProvider.username] and call regular [invoke]
         */
        @Synchronized
        operator fun invoke(username: String): TokenManager {
            this.username = username
            return invoke()
        }
    }

    /**
     * An object for migrate from [SharedPreferences] used from an older version of [TokenManager]
     * to the new [SharedPreferences]
     */
    class Migrator(
        private val oldPreferences: SharedPreferences,
        private val newPreferences: SharedPreferences
    ) {
        operator fun invoke() {
            if (oldPreferences.isEmpty()) return

            newPreferences[PREF_ENC_ACCESS_TOKEN] = oldPreferences.get<String?>(PREF_ENC_ACCESS_TOKEN)
            newPreferences[PREF_ACCESS_TOKEN] =     oldPreferences.get<String?>(PREF_ACCESS_TOKEN)
            newPreferences[PREF_ENC_PRIV_KEY] =     oldPreferences.get<String?>(PREF_ENC_PRIV_KEY)
            newPreferences[PREF_REFRESH_TOKEN] =    oldPreferences.get<String?>(PREF_REFRESH_TOKEN)
            newPreferences[PREF_USER_UID] =         oldPreferences.get<String?>(PREF_USER_UID)

            oldPreferences.clearAll()
        }
    }

    /** Set of Keys for [SharedPreferences] */
    @VisibleForTesting(otherwise = PRIVATE) internal object KeyStore {
        const val PREF_ENC_ACCESS_TOKEN = "access_token"
        const val PREF_ENC_PRIV_KEY = "priv_key"
        const val PREF_REFRESH_TOKEN = "refresh_token"
        const val PREF_USER_UID = "user_uid"
        const val PREF_ACCESS_TOKEN = "access_token_plain"
    }
}
