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

package me.proton.core.mailsendpreferences.domain.usecase

import ezvcard.VCard
import me.proton.core.contact.domain.CryptoUtils
import me.proton.core.contact.domain.getGroupForEmail
import me.proton.core.contact.domain.getProperty
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.Recipient
import me.proton.core.key.domain.entity.key.isCompromised
import me.proton.core.key.domain.entity.key.isObsolete
import me.proton.core.mailsendpreferences.domain.model.SendPreferences
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.entity.MimeType
import me.proton.core.mailsettings.domain.entity.PackageType
import me.proton.core.util.kotlin.equalsNoCase
import javax.inject.Inject

class CreateSendPreferences @Inject constructor(
    private val cryptoUtils: CryptoUtils
) {

    sealed class Result {
        data class Success(val sendPreferences: SendPreferences) : Result()

        sealed class Error : Result() {
            object NoKeysAvailable : Error()
            object NoEmailInVCard : Error()
            object TrustedKeysInvalid : Error()
            object PublicKeysInvalid : Error()
            object MailSettingsInvalid : Error()
        }
    }

    /**
     * Creates custom SendPreferences using VCard data.
     *
     * @param vCardEmail it has to be contact email in the vCard, not necessarily canonical version (can be aliased)
     */
    operator fun invoke(
        cryptoContext: CryptoContext,
        vCardEmail: String,
        publicAddress: PublicAddress,
        vCard: VCard,
        defaultMailSettings: MailSettings
    ): Result {

        val isInternal = publicAddress.recipient == Recipient.Internal
        val publicAddressKey = publicAddress.keys.firstOrNull { it.publicKey.isPrimary }

        if (publicAddressKey != null && (publicAddressKey.flags.isObsolete() || publicAddressKey.flags.isCompromised()))
            return Result.Error.PublicKeysInvalid

        vCard.getGroupForEmail(vCardEmail) ?: return Result.Error.NoEmailInVCard

        val sendingSettings = combineSendingSettings(vCard, vCardEmail, defaultMailSettings)
            ?: return Result.Error.MailSettingsInvalid

        val pinnedPublicKeys = when (val pinnedKeysOrError = cryptoUtils.extractPinnedPublicKeys(
            CryptoUtils.PinnedKeysPurpose.Encrypting,
            vCardEmail,
            vCard,
            publicAddress,
            cryptoContext
        )) {
            is CryptoUtils.PinnedKeysOrError.Success -> pinnedKeysOrError.pinnedPublicKeys
            is CryptoUtils.PinnedKeysOrError.Error.NoKeysAvailable,
            CryptoUtils.PinnedKeysOrError.Error.NoEmailInVCard -> null // no pinned key found
            else -> return Result.Error.TrustedKeysInvalid
        }

        return if (isInternal) {

            if (pinnedPublicKeys.isNullOrEmpty() && publicAddressKey == null) {
                Result.Error.NoKeysAvailable
            } else {
                Result.Success(
                    SendPreferences(
                        encrypt = true,
                        sign = true,
                        pgpScheme = PackageType.ProtonMail,
                        mimeType = sendingSettings.mimeType,
                        publicKey = pinnedPublicKeys?.firstOrNull() ?: publicAddressKey?.publicKey
                    )
                )
            }

        } else {

            if (sendingSettings.encrypt && pinnedPublicKeys == null && publicAddressKey == null) {
                Result.Error.NoKeysAvailable
            } else {
                Result.Success(
                    SendPreferences(
                        encrypt = sendingSettings.encrypt,
                        sign = if (sendingSettings.encrypt) true else sendingSettings.sign,
                        pgpScheme = sendingSettings.scheme,
                        mimeType = sendingSettings.mimeType,
                        publicKey = pinnedPublicKeys?.firstOrNull() ?: publicAddressKey?.publicKey
                    )
                )
            }

        }
    }

    /**
     * Creates default SendPreferences using Mail Settings and optional PublicKey of the recipient.
     */
    operator fun invoke(
        defaultMailSettings: MailSettings,
        publicAddress: PublicAddress?
    ): Result {

        val isInternal = publicAddress?.recipient == Recipient.Internal
        val publicAddressKey = publicAddress?.keys?.firstOrNull { it.publicKey.isPrimary }

        if (publicAddressKey != null &&
            (publicAddressKey.flags.isObsolete() || publicAddressKey.flags.isCompromised())) {
            return Result.Error.PublicKeysInvalid
        }

        val sign = defaultMailSettings.sign ?: return Result.Error.MailSettingsInvalid
        val draftMimeType = defaultMailSettings.draftMimeType?.enum ?: return Result.Error.MailSettingsInvalid

        return if (isInternal) {

            if (publicAddressKey != null) {
                Result.Success(
                    SendPreferences(
                        encrypt = true,
                        sign = true,
                        pgpScheme = PackageType.ProtonMail,
                        mimeType = draftMimeType,
                        publicKey = publicAddressKey.publicKey
                    )
                )
            } else {
                Result.Error.NoKeysAvailable
            }

        } else {

            if (publicAddressKey != null) {

                // allow only PgpInline or PgpMime, fallback to PgpMime
                val defaultPgpScheme = defaultMailSettings.pgpScheme?.enum?.takeIf {
                    it == PackageType.PgpInline || it == PackageType.PgpMime
                } ?: PackageType.PgpMime

                val defaultMimeType =
                    if (defaultPgpScheme == PackageType.PgpMime) MimeType.Mixed else MimeType.PlainText

                Result.Success(
                    SendPreferences(
                        encrypt = true,
                        sign = true,
                        pgpScheme = defaultPgpScheme,
                        mimeType = defaultMimeType,
                        publicKey = publicAddressKey.publicKey
                    )
                )
            } else {
                Result.Success(
                    SendPreferences(
                        encrypt = false,
                        sign = sign,
                        pgpScheme = PackageType.Cleartext,
                        mimeType = MimeType.PlainText,
                        publicKey = null
                    )
                )
            }

        }

    }

    data class SendingSettings(
        val encrypt: Boolean,
        val sign: Boolean,
        val scheme: PackageType,
        val mimeType: MimeType
    )

    /**
     * Combines sending settings from VCard and default MailSettings.
     */
    private fun combineSendingSettings(
        vCard: VCard,
        vCardEmail: String,
        defaultMailSettings: MailSettings
    ): SendingSettings? {
        val propertyGroup = vCard.getGroupForEmail(vCardEmail) ?: return null

        val vCardEncrypt = vCard.getProperty(propertyGroup, "x-pm-encrypt")
        val vCardSign = vCard.getProperty(propertyGroup, "x-pm-sign")
        val vCardMime = vCard.getProperty(propertyGroup, "x-pm-mimetype")
        val vCardScheme = vCard.getProperty(propertyGroup, "x-pm-scheme")

        val encrypt = if (vCardEncrypt != null) (vCardEncrypt.value?.equalsNoCase("true") == true) else false

        val sign = if (vCardSign != null) {
            (vCardSign.value?.equalsNoCase("true") == true)
        } else defaultMailSettings.sign ?: return null

        val scheme = PackageType.enumFromScheme(vCardScheme?.value ?: "", encrypt, sign)
            ?: defaultMailSettings.pgpScheme?.enum
            ?: return null

        val mimeType = (if (vCardMime?.value != null) {
            vCardMime.value
        } else defaultMailSettings.draftMimeType?.value)?.let { mimeString ->
            MimeType.enumFromContentType(mimeString)
        } ?: return null

        return SendingSettings(
            encrypt,
            sign,
            scheme,
            mimeType
        )
    }

}