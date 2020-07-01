package ch.protonmail.libs.crypto

import org.mindrot.jbcrypt.BCrypt

/**
 * Shadow class of `org.mindrot.jbcrypt.BCrypt` for do not expose external libraries
 * @author Davide Farella
 */
object BCrypt {

    fun hashpw(password: String, salt: String): String = BCrypt.hashpw(password, salt)

}
