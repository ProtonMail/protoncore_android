/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.crypto.android.pgp

import com.proton.gopenpgp.crypto.KeyRing
import com.proton.gopenpgp.crypto.MIMECallbacks
import com.proton.gopenpgp.crypto.PGPMessage
import me.proton.core.crypto.common.pgp.DecryptedMimeAttachment
import me.proton.core.crypto.common.pgp.DecryptedMimeBody
import me.proton.core.crypto.common.pgp.DecryptedMimeMessage
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.exception.CryptoException

internal class DecryptMimeMessage {

    operator fun invoke(
        message: PGPMessage,
        decryptionKeyRing: KeyRing,
        verificationKeyRing: KeyRing?,
        verificationTime: Long
    ): DecryptedMimeMessage {
        val callbacks = Callbacks()
        decryptionKeyRing.decryptMIMEMessage(message, verificationKeyRing, callbacks, verificationTime)
        val decryptedMessage = callbacks.getDecryptedMessage()
        if (verificationKeyRing != null) {
            check(decryptedMessage.verificationStatus != VerificationStatus.Unknown)
        }
        return decryptedMessage
    }
}

private class Callbacks : MIMECallbacks {

    private var attachments: MutableList<DecryptedMimeAttachment> = mutableListOf()

    private var body: DecryptedMimeBody? = null

    private var verificationStatus: Long? = null

    private var errors: MutableList<Exception> = mutableListOf()

    override fun onAttachment(headers: String?, content: ByteArray?) {
        attachments.add(
            DecryptedMimeAttachment(
                headers = checkNotNull(headers),
                content = checkNotNull(content)
            )
        )
    }

    override fun onBody(content: String?, mimeType: String?) {
        body = DecryptedMimeBody(
            mimeType = checkNotNull(mimeType),
            content = checkNotNull(content)
        )
    }

    override fun onEncryptedHeaders(headers: String?) {
        // not implemented yet
    }

    override fun onError(error: Exception?) {
        errors.add(checkNotNull(error))
    }

    override fun onVerified(status: Long) {
        verificationStatus = status
    }

    fun getDecryptedMessage(): DecryptedMimeMessage {
        val body = this.body
        if (body == null) {
            // if the message can't be decrypted, an exception will be returned via onError
            check(errors.isNotEmpty())
            val errorMessage = errors.map { it.message }.joinToString(";")
            throw CryptoException("Could not decrypt PGP/MIME message: $errorMessage")
        }
        val verificationStatus = this.verificationStatus?.toVerificationStatus() ?: VerificationStatus.Unknown
        return DecryptedMimeMessage(
            headers = emptyList(), // not implemented,
            body = body,
            attachments = this.attachments,
            verificationStatus = verificationStatus
        )
    }

}
