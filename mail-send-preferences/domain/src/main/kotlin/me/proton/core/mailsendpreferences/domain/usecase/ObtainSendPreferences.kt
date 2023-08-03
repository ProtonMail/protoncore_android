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

import me.proton.core.contact.domain.decryptContactCard
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.useKeys
import me.proton.core.mailmessage.domain.entity.Email
import me.proton.core.mailmessage.domain.usecase.GetRecipientPublicAddresses
import me.proton.core.mailsendpreferences.domain.model.SendPreferences
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.util.kotlin.equalsNoCase
import me.proton.core.util.kotlin.toInt
import javax.inject.Inject

class ObtainSendPreferences @Inject constructor(
    private val contactEmailsRepository: ContactRepository,
    private val userManager: UserManager,
    private val mailSettingsRepository: MailSettingsRepository,
    private val cryptoContext: CryptoContext,
    private val getRecipientPublicAddresses: GetRecipientPublicAddresses,
    private val createSendPreferences: CreateSendPreferences
) {

    sealed class Result {
        data class Success(val sendPreferences: SendPreferences) : Result()

        sealed class Error : Result() {
            object AddressDisabled : Error()
            object GettingContactPreferences : Error()
            object TrustedKeysInvalid : Error()
            object NoCorrectlySignedTrustedKeys : Error()
            object PublicKeysInvalid : Error()
        }
    }

    suspend operator fun invoke(
        userId: UserId,
        emails: List<Email>
    ): Map<Email, Result> {

        val user = userManager.getUser(userId)
        val mailSettings = mailSettingsRepository.getMailSettings(userId)
        val contactEmails = contactEmailsRepository.getAllContactEmails(userId)
        val publicAddresses = getRecipientPublicAddresses(userId, emails)

        return createCustomOrDefaultSendPreferences(
            emails,
            contactEmails,
            publicAddresses,
            user,
            mailSettings
        )
    }

    private suspend fun createCustomOrDefaultSendPreferences(
        emails: List<Email>,
        contactEmails: List<ContactEmail>,
        publicAddresses: Map<Email, PublicAddress?>,
        user: User,
        mailSettings: MailSettings
    ): Map<Email, Result> = emails.map { email ->

        val contactEmail =
            contactEmails.firstOrNull { (email.equalsNoCase(it.email) || email.equalsNoCase(it.canonicalEmail)) }

        val publicAddress = publicAddresses[email] ?: return@map email to Result.Error.AddressDisabled

        val sendPreferencesOrError = if (contactEmail?.defaults == false.toInt()) { // custom Send Preferences

            val contactWithCards = contactEmailsRepository.getContactWithCards(user.userId, contactEmail.contactId)

            user.useKeys(cryptoContext) {

                val contactCards = contactWithCards.contactCards
                    .filterIsInstance<ContactCard.Signed>()
                    .map { decryptContactCard(it) }
                    .filter { it.status == VerificationStatus.Success }

                if (contactCards.isEmpty()) {
                    CreateSendPreferences.Result.Error.TrustedKeysInvalid
                } else {

                    val vCard = contactCards.first()
                    val vCardEmail = contactEmail.email

                    createSendPreferences(cryptoContext, vCardEmail, publicAddress, vCard.card, mailSettings)
                }
            }

        } else { // default Send Preferences
            createSendPreferences(mailSettings, publicAddress)
        }

        email to when (sendPreferencesOrError) {
            is CreateSendPreferences.Result.Success -> Result.Success(sendPreferencesOrError.sendPreferences)
            is CreateSendPreferences.Result.Error.TrustedKeysInvalid -> Result.Error.TrustedKeysInvalid
            is CreateSendPreferences.Result.Error.PublicKeysInvalid -> Result.Error.PublicKeysInvalid
            else -> Result.Error.GettingContactPreferences
        }

    }.toMap()

}