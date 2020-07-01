package ch.protonmail.libs.crypto

import com.proton.pmcrypto.armor.Armor

/**
 * Shadow class of `com.proton.pmcrypto.armor.Armor` for do not expose external libraries
 * @author Davide Farella
 */
object Armor {

    fun readClearSignedMessage(signedMessage: String) =
        Armor.readClearSignedMessage(signedMessage)!!

}
