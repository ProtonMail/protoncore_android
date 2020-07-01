package ch.protonmail.libs.auth.model.response

import ch.protonmail.libs.crypto.PGP_BEGIN
import ch.protonmail.libs.crypto.PGP_END

/**
 * Common interface for models that have [accessToken]
 * @author Davide Farella
 */
internal interface HasAccessToken {
    val accessToken: String

    val isAccessTokenArmored: Boolean get() {
        val trimmed = accessToken?.trim() ?: return false
        return trimmed.startsWith(PGP_BEGIN) && trimmed.endsWith(PGP_END)
    }
}
