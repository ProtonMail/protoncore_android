package ch.protonmail.libs.auth.model.request

import me.proton.android.core.data.api.Field.AUTH_VERSION
import me.proton.android.core.data.api.Field.MODULUS_ID
import me.proton.android.core.data.api.Field.SALT
import me.proton.android.core.data.api.Field.SRP_VERIFIER
import ch.protonmail.libs.auth.model.response.ModulusResponse
import ch.protonmail.libs.crypto.Armor
import ch.protonmail.libs.crypto.ConstantTime
import ch.protonmail.libs.crypto.PasswordUtils
import ch.protonmail.libs.crypto.SrpClient
import ch.protonmail.libs.crypto.utils.decodeBase64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.SecureRandom

@Serializable
internal class PasswordVerifier private constructor(

    @SerialName(AUTH_VERSION)
    val authVersion: Int,

    @SerialName(MODULUS_ID)
    val modulusId: String,

    @SerialName(SALT)
    val salt: String,

    @SerialName(SRP_VERIFIER)
    val srpVerifier: String
) {

    companion object {

        fun calculate(password: String, modulusResp: ModulusResponse): PasswordVerifier {
            val salt = ByteArray(10)
            SecureRandom().nextBytes(salt)
            val modulus = Armor.readClearSignedMessage(modulusResp.modulus).decodeBase64()

            val hashedPassword = PasswordUtils.hashPassword(password, salt, modulus)
            val verifier = SrpClient.generateVerifier(2048, modulus, hashedPassword)
            return PasswordVerifier(
                PasswordUtils.LAST_AUTH_VERSION,
                modulusResp.modulusId,
                ConstantTime.encodeBase64(salt, true),
                ConstantTime.encodeBase64(verifier, true)
            )
        }
    }
}
