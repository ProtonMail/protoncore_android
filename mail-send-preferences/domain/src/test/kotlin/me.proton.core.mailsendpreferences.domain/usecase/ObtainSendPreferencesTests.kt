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

import ezvcard.Ezvcard
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.proton.core.contact.domain.CryptoUtilsImpl
import me.proton.core.contact.domain.decryptContactCard
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactCard
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactEmailId
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.contact.domain.entity.DecryptedVCard
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.crypto.common.pgp.getFingerprintOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.domain.type.StringEnum
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.Recipient
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.mailmessage.domain.usecase.GetRecipientPublicAddresses
import me.proton.core.mailsettings.domain.entity.ComposerMode
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.entity.MessageButtons
import me.proton.core.mailsettings.domain.entity.MimeType
import me.proton.core.mailsettings.domain.entity.PMSignature
import me.proton.core.mailsettings.domain.entity.PackageType
import me.proton.core.mailsettings.domain.entity.ShowImage
import me.proton.core.mailsettings.domain.entity.ShowMoved
import me.proton.core.mailsettings.domain.entity.SwipeAction
import me.proton.core.mailsettings.domain.entity.ViewLayout
import me.proton.core.mailsettings.domain.entity.ViewMode
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository
import me.proton.core.test.kotlin.assertEquals
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObtainSendPreferencesTests {

    private val contactEmailsRepositoryMock: ContactRepository = mockk()
    private val userManagerMock: UserManager = mockk()
    private val mailSettingsRepositoryMock: MailSettingsRepository = mockk()
    private val cryptoContextMock: CryptoContext = mockk()
    private val getRecipientPublicAddressesMock: GetRecipientPublicAddresses = mockk()
    private val pgpCryptoMock: PGPCrypto = mockk()

    private val createSendPreferences = CreateSendPreferences(CryptoUtilsImpl())

    private val userId = UserId("test-user-id")

    private lateinit var sut: ObtainSendPreferences

    @Before
    fun `before each`() {
        clearAllMocks()

        sut = ObtainSendPreferences(
            contactEmailsRepositoryMock,
            userManagerMock,
            mailSettingsRepositoryMock,
            cryptoContextMock,
            getRecipientPublicAddressesMock,
            createSendPreferences
        )

        coEvery { mailSettingsRepositoryMock.getMailSettings(userId) } returns mailSettingsSignTrue

        coEvery { contactEmailsRepositoryMock.getAllContactEmails(userId) } returns listOf(
            ContactEmail(
                userId,
                ContactEmailId("1"),
                "External Contact with pinned key",
                "contact_external_pinned_key+alias@email.com",
                defaults = 0,
                1,
                contactId = ContactId("contact_1"),
                "contact_external_pinned_key@email.com",
                labelIds = emptyList(),
                isProton = false
            ),
            contacWith2PinnedKeysContactEmail
        )

        coEvery { contactEmailsRepositoryMock.getContactWithCards(userId, ContactId("contact_1")) } returns externalContactWithPinnedKeyBrokenSignature

        coEvery { getRecipientPublicAddressesMock.invoke(userId, any()) } returns mapOf(
            "disabled_address@pm.me" to null, // address is disabled
            "unknown_external_no_keys@email.com" to unknownExternalRecipientNoKeysPublicAddress,
            "unknown_external_with_keys@email.com" to unknownExternalRecipientWithKeysPublicAddress,
            "unknown_internal@pm.me" to unknownInternalRecipientWithKeysPublicAddress,
            "unknown_internal_no_keys@pm.me" to unknownInternalRecipientNoKeysPublicAddress,
            "contact_external_pinned_key@email.com" to contactExternalPinnedKeyPublicAddress,
            "contact_external_pinned_key+alias@email.com" to contactExternalPinnedKeyPublicAddress,
            "calendar@proton.black" to contactWith2PinnedKeysPublicAddress
        )

        val userMock = mockk<User>()
        every { userMock.userId } returns userId
        every { userMock.keys } returns emptyList()
        //every {  } returns emptyList()

        coEvery { userManagerMock.getUser(userId) } returns userMock

        every { cryptoContextMock.pgpCrypto } returns pgpCryptoMock
    }

    @Test
    fun `handle unknown external recipient, default sign = true`() = runTest {

        val emails = listOf(
            "unknown_external_no_keys@email.com", "unknown_external_with_keys@email.com"
        )

        coEvery { mailSettingsRepositoryMock.getMailSettings(userId) } returns mailSettingsSignTrue

        val resultDefaultSignTrue = sut(userId, emails)

        assertTrue(resultDefaultSignTrue["unknown_external_no_keys@email.com"] is ObtainSendPreferences.Result.Success)

        with((resultDefaultSignTrue["unknown_external_no_keys@email.com"] as ObtainSendPreferences.Result.Success).sendPreferences) {
            assertFalse(encrypt)
            assertTrue(sign)
        }

        assertTrue(resultDefaultSignTrue["unknown_external_with_keys@email.com"] is ObtainSendPreferences.Result.Success)

        with((resultDefaultSignTrue["unknown_external_with_keys@email.com"] as ObtainSendPreferences.Result.Success).sendPreferences) {
            assertTrue(encrypt)
            assertTrue(sign)
        }

    }

    @Test
    fun `handle unknown external recipient, default sign = false`() = runTest {

        val emails = listOf(
            "unknown_external_no_keys@email.com", "unknown_external_with_keys@email.com"
        )

        coEvery { mailSettingsRepositoryMock.getMailSettings(userId) } returns mailSettingsSignFalse

        val resultDefaultSignFalse = sut(userId, emails)

        assertTrue(resultDefaultSignFalse["unknown_external_no_keys@email.com"] is ObtainSendPreferences.Result.Success)

        with((resultDefaultSignFalse["unknown_external_no_keys@email.com"] as ObtainSendPreferences.Result.Success).sendPreferences) {
            assertFalse(encrypt)
            assertFalse(sign)
        }

        assertTrue(resultDefaultSignFalse["unknown_external_with_keys@email.com"] is ObtainSendPreferences.Result.Success)

        with((resultDefaultSignFalse["unknown_external_with_keys@email.com"] as ObtainSendPreferences.Result.Success).sendPreferences) {
            assertTrue(encrypt)
            assertTrue(sign)
        }

    }

    @Test
    fun `handle disabled addresses`() = runTest {

        val emails = listOf("disabled_address@pm.me")

        val result = sut(userId, emails)

        assertEquals(ObtainSendPreferences.Result.Error.AddressDisabled, result["disabled_address@pm.me"]) {
            "ObtainSendPreferences should detect disabled address"
        }

    }

    @Test
    fun `handle unknown internal recipient`() = runTest {

        val emails = listOf("unknown_internal@pm.me")

        // default sign = false but we'll sign anyway

        coEvery { mailSettingsRepositoryMock.getMailSettings(userId) } returns mailSettingsSignFalse

        val resultDefaultSignFalse = sut(userId, emails)

        with((resultDefaultSignFalse["unknown_internal@pm.me"] as ObtainSendPreferences.Result.Success).sendPreferences) {
            assertTrue(encrypt)
            assertTrue(sign)
            assertEquals(PackageType.ProtonMail, pgpScheme) {
                "ObtainSendPreferences package type should be ProtonMail for unknown internal recipient"
            }
        }

    }

    @Test
    fun `handle unknown internal recipient, incorrect state with PublicAddress with no public keys`() {
        runBlocking {

            val emails = listOf("unknown_internal_no_keys@pm.me")

            // default sign = false but we'll sign anyway

            coEvery { mailSettingsRepositoryMock.getMailSettings(userId) } returns mailSettingsSignFalse

            val resultDefaultSignFalse = sut(userId, emails)

            assertTrue(resultDefaultSignFalse["unknown_internal_no_keys@pm.me"] is ObtainSendPreferences.Result.Error.GettingContactPreferences)

        }
    }

    @Test
    fun `handle createDefaultSendPreferences for unknown external recipient with public keys`() = runTest {

        val result = sut(userId, listOf(unknownExternalRecipientWithKeysPublicAddress.email))
        val sendPreferences = result[unknownExternalRecipientWithKeysPublicAddress.email] as ObtainSendPreferences.Result.Success

        with(sendPreferences.sendPreferences) {
            assertTrue(encrypt)
            assertTrue(sign)
            assertEquals(PackageType.PgpMime, pgpScheme) {
                "ObtainSendPreferences package type should be PgpMime for unknown external recipient"
            }
            assertTrue(publicKey != null)
        }

    }

    @Test
    fun `error verifying broken VCard signature`() = runTest {

        val emails = listOf("contact_external_pinned_key+alias@email.com")

        val map = sut(userId, emails)

        coVerify(exactly = 1) { contactEmailsRepositoryMock.getContactWithCards(userId, ContactId("contact_1")) }

        val result = map["contact_external_pinned_key+alias@email.com"]
        println(result)
        assertTrue(result is ObtainSendPreferences.Result.Error.TrustedKeysInvalid)
    }

    @Test
    fun `handle createCustomSendPreferences with pinned key`() = runTest {

        val emailWithPinnedKeys = "calendar@proton.black"

        every { pgpCryptoMock.getArmored(any(), any()) } returns "armored public key"
        every { pgpCryptoMock.getFingerprintOrNull("armored public key") } returns "key fingerprint"
        every { pgpCryptoMock.getFingerprintOrNull("armored key from public repository") } returns "key fingerprint"
        every { pgpCryptoMock.isKeyExpired(any()) } returns false
        every { pgpCryptoMock.isKeyRevoked(any()) } returns false

        coEvery { contactEmailsRepositoryMock.getContactWithCards(userId, ContactId("ID of contact with 2 pinned keys")) } returns contactWith2PinnedKeys

        mockkStatic(KeyHolderContext::decryptContactCard)
        every { any<KeyHolderContext>().decryptContactCard(any()) } returns DecryptedVCard(Ezvcard.parse(contactWith2PinnedKeys.contactCards.filterIsInstance<ContactCard.Signed>().first().data).first(), VerificationStatus.Success)

        val emails = listOf(emailWithPinnedKeys)

        val result = sut(userId, emails)

        coVerify(exactly = 1) { contactEmailsRepositoryMock.getContactWithCards(userId, ContactId("ID of contact with 2 pinned keys")) }

        with (result[emailWithPinnedKeys] as ObtainSendPreferences.Result.Success) {
            assertTrue(this.sendPreferences.sign)
            assertTrue(this.sendPreferences.encrypt)
            kotlin.test.assertEquals(this.sendPreferences.pgpScheme, PackageType.PgpMime)
            kotlin.test.assertEquals(this.sendPreferences.mimeType, MimeType.PlainText)
            assertTrue(this.sendPreferences.publicKey != null)
        }
    }

    @Test
    fun `extract pinned keys from VCard sorted by PREF`() = runTest {

        val emailWithPinnedKeys = "calendar@proton.black"

        every { pgpCryptoMock.getArmored(any(), any()) } returns "armored public key"
        every { pgpCryptoMock.getFingerprintOrNull("armored public key") } returns "key fingerprint"
        every { pgpCryptoMock.getFingerprintOrNull("armored key from public repository") } returns "key fingerprint"
        every { pgpCryptoMock.isKeyExpired(any()) } returns false
        every { pgpCryptoMock.isKeyRevoked(any()) } returns false

        coEvery { contactEmailsRepositoryMock.getContactWithCards(userId, ContactId("ID of contact with 2 pinned keys")) } returns contactWith2PinnedKeys

        mockkStatic(KeyHolderContext::decryptContactCard)
        every { any<KeyHolderContext>().decryptContactCard(any()) } returns DecryptedVCard(Ezvcard.parse(contactWith2PinnedKeys.contactCards.filterIsInstance<ContactCard.Signed>().first().data).first(), VerificationStatus.Success)

        val emails = listOf(emailWithPinnedKeys)

        val result = sut(userId, emails)

        coVerify(exactly = 1) { contactEmailsRepositoryMock.getContactWithCards(userId, ContactId("ID of contact with 2 pinned keys")) }

        with (result[emailWithPinnedKeys] as ObtainSendPreferences.Result.Success) {
            assertTrue(this.sendPreferences.sign)
            assertTrue(this.sendPreferences.encrypt)
            kotlin.test.assertEquals(this.sendPreferences.pgpScheme, PackageType.PgpMime)
            kotlin.test.assertEquals(this.sendPreferences.mimeType, MimeType.PlainText)
            assertTrue(this.sendPreferences.publicKey != null)
        }
    }

    private val sampleMailSettings = MailSettings(
        userId = userId,
        displayName = null,
        signature = null,
        autoSaveContacts = true,
        composerMode = IntEnum(1, ComposerMode.Maximized),
        messageButtons = IntEnum(1, MessageButtons.UnreadFirst),
        showImages = IntEnum(1, ShowImage.Remote),
        showMoved = IntEnum(1, ShowMoved.Drafts),
        viewMode = IntEnum(1, ViewMode.NoConversationGrouping),
        viewLayout = IntEnum(1, ViewLayout.Row),
        swipeLeft = IntEnum(1, SwipeAction.Spam),
        swipeRight = IntEnum(1, SwipeAction.Spam),
        shortcuts = true,
        pmSignature = IntEnum(1, PMSignature.Disabled),
        numMessagePerPage = 1,
        draftMimeType = StringEnum("text/plain", MimeType.PlainText),
        receiveMimeType = StringEnum("text/plain", MimeType.PlainText),
        showMimeType = StringEnum("text/plain", MimeType.PlainText),
        enableFolderColor = true,
        inheritParentFolderColor = true,
        rightToLeft = true,
        attachPublicKey = true,
        sign = true,
        pgpScheme = IntEnum(1, PackageType.ProtonMail),
        promptPin = true,
        stickyLabels = true,
        confirmLink = true
    )

    private val mailSettingsSignTrue = sampleMailSettings.copy(
        pgpScheme = IntEnum(16, PackageType.PgpMime),
        sign = true
    )

    private val mailSettingsSignFalse = sampleMailSettings.copy(
        pgpScheme = IntEnum(16, PackageType.PgpMime),
        sign = false
    )

    private val unknownExternalRecipientWithKeysPublicAddress = PublicAddress(
        "unknown_external_with_keys@email.com",
        recipientType = Recipient.External.value,
        "text/html",
        listOf(
            PublicAddressKey("unknown_external_with_keys@email.com", 3, PublicKey("armored key", isPrimary = false, true, true, true)),
            PublicAddressKey("unknown_external_with_keys@email.com", 3, PublicKey("armored key", isPrimary = true, true, true, true))
        ),
        null,
        ignoreKT=0
    )

    private val unknownExternalRecipientNoKeysPublicAddress = PublicAddress(
        "unknown_external_no_keys@email.com",
        recipientType = Recipient.External.value,
        "text/html",
        emptyList(),
        null,
        ignoreKT=0
    )

    private val unknownInternalRecipientWithKeysPublicAddress = PublicAddress(
        "unknown_internal@pm.me",
        recipientType = Recipient.Internal.value,
        "text/html",
        listOf(
            PublicAddressKey("unknown_internal@pm.me", 3, PublicKey("armored key", isPrimary = false, true, true, true)),
            PublicAddressKey("unknown_internal@pm.me", 3, PublicKey("armored key", isPrimary = true, true, true, true))
        ),
        null,
        ignoreKT=0
    )

    private val unknownInternalRecipientNoKeysPublicAddress = PublicAddress(
        "unknown_internal_no_keys@pm.me",
        recipientType = Recipient.Internal.value,
        "text/html",
        emptyList(),
        null,
        ignoreKT=0
    )

    private val contactExternalPinnedKeyPublicAddress = PublicAddress(
        "contact_external_pinned_key@pm.me",
        recipientType = Recipient.External.value,
        "text/html",
        listOf(
            PublicAddressKey(
                "contact_external_pinned_key@pm.me", 3, PublicKey(
                    "armored key from public repository",
                    isPrimary = true,
                    true, true, true
                )
            ),
        ),
        null,
        ignoreKT=0
    )

    private val contactWith2PinnedKeysPublicAddress = PublicAddress(
        "calendar@proton.black",
        recipientType = Recipient.External.value,
        "text/html",
        listOf(
            PublicAddressKey(
                "calendar@proton.black", 3, PublicKey(
                    "armored key from public repository",
                    isPrimary = true,
                    true, true, true
                )
            ),
        ),
        null,
        ignoreKT=0
    )

    private val externalContactWithPinnedKeyBrokenSignature: ContactWithCards =
        ContactWithCards(
            contact = Contact(
                userId,
                id = ContactId("1"),
                name = "External Contact with pinned key",
                contactEmails = listOf(
                    ContactEmail(
                        userId,
                        ContactEmailId("1"),
                        name = "External Contact with pinned key",
                        email = "contact_external_pinned_key+alias@email.com",
                        defaults = 0,
                        order = 1,
                        contactId = ContactId("contact_1"),
                        canonicalEmail = null,
                        labelIds = emptyList(),
                        isProton = false
                    )
                ),
            ),
            contactCards = listOf(
                ContactCard.Encrypted("encrypted and signed data", "signature"),
                ContactCard.Signed(
                    "BEGIN:VCARD\r\nVERSION:4.0\r\nFN;PREF=1:contact_external_pinned_key+alias@email.com\r\nITEM1.EMAIL;PREF=1:contact_external_pinned_key+alias@email.com\r\nITEM1.KEY;PREF=1:data:application/pgp-keys;base64,xjMEYIE/zBYJKwYBBAHaRw8BA\r\n QdAU0kzBdPct+/iReob+92uE1hEJPzoXnrrTqx5p8EoOa7NLWNhbGVuZGFyQHByb3Rvbi5ibGFj\r\n ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBAWCgAgBQJggT/MBgsJBwgDAgQVCAoCBBYCAQA\r\n CGQECGwMCHgEAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP09xeAQD3ioSt4E6SyV\r\n xOeS8xBQvhuEXkqBKKZCkMO10fd0P2LgD/WvtGpRv8JAll0feMgG2y1lufZtJImTeLr0ciYb7AE\r\n gnOOARggT/MEgorBgEEAZdVAQUBAQdAJYTJ0NuH3zSCNxk+gsFNTVHuPDLQQLRsyNermAbrEXID\r\n AQgHwngEGBYIAAkFAmCBP8wCGwwAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP0/e\r\n ZAQC9vSk4lPi9v1dMHsbKCChrYPR2WCMSUXykpNcDuP2TBgEA0jjgSKW351PQTmHU15UcSFY71O\r\n pD+j04Cs4EcONklw0=\r\nUID:proton-web-4e57f941-d1b4-7909-c879-73a2df5513f1\r\nITEM1.X-PM-ENCRYPT:true\r\nITEM1.X-PM-SIGN:true\r\nEND:VCARD",
                    "incorrect signature"
                )
            )
        )

    private val contacWith2PinnedKeysContactEmail = ContactEmail(
        userId,
        ContactEmailId("9JwYkiKDYQKk9txkrNOWdnWNCubdCK8bm6zWM8qgAHB-_2v06eq2PEgQTizBPBqGt0ZvQU0XgkB-pF63YL-TYQ=="),
        name = "calendar@proton.black",
        email = "calendar@proton.black",
        defaults = 0,
        order = 1,
        contactId = ContactId("ID of contact with 2 pinned keys"),
        canonicalEmail = null,
        labelIds = emptyList(),
        isProton = false
    )

    private val contactWith2PinnedKeys: ContactWithCards =
        ContactWithCards(
            contact = Contact(
                userId,
                id = ContactId("ID of contact with 2 pinned keys"),
                name = "calendar@proton.black",
                contactEmails = listOf(contacWith2PinnedKeysContactEmail),
            ),
            contactCards = listOf(
                ContactCard.Signed(
                    "BEGIN:VCARD\r\nVERSION:4.0\r\nFN;PREF=1:calendar@proton.black\r\nITEM1.EMAIL;PREF=1:calendar@proton.black\r\nUID:proton-web-4e57f941-d1b4-7909-c879-73a2df5513f1\r\nITEM1.KEY;PREF=1:data:application/pgp-keys;base64,xjMEYIE/zBYJKwYBBAHaRw8BA\r\n QdAU0kzBdPct+/iReob+92uE1hEJPzoXnrrTqx5p8EoOa7NLWNhbGVuZGFyQHByb3Rvbi5ibGFj\r\n ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBAWCgAgBQJggT/MBgsJBwgDAgQVCAoCBBYCAQA\r\n CGQECGwMCHgEAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP09xeAQD3ioSt4E6SyV\r\n xOeS8xBQvhuEXkqBKKZCkMO10fd0P2LgD/WvtGpRv8JAll0feMgG2y1lufZtJImTeLr0ciYb7AE\r\n gnOOARggT/MEgorBgEEAZdVAQUBAQdAJYTJ0NuH3zSCNxk+gsFNTVHuPDLQQLRsyNermAbrEXID\r\n AQgHwngEGBYIAAkFAmCBP8wCGwwAIQkQ9LTBFWUbz9MWIQQL9ztQ8o2jSXASlPX0tMEVZRvP0/e\r\n ZAQC9vSk4lPi9v1dMHsbKCChrYPR2WCMSUXykpNcDuP2TBgEA0jjgSKW351PQTmHU15UcSFY71O\r\n pD+j04Cs4EcONklw0=\r\nITEM1.KEY;PREF=2:data:application/pgp-keys;base64,xjMEZKMDghYJKwYBBAHaRw8BA\r\n QdAGems3V25edmSzHRFKePBU7692nBprwN3uZmapAjgH+bNLWNhbGVuZGFyQHByb3Rvbi5ibGFj\r\n ayA8Y2FsZW5kYXJAcHJvdG9uLmJsYWNrPsKPBBMWCABBBQJkowOCCZDXZ5WySpsJSRYhBIhRe96\r\n tJFIFkIiG7ddnlbJKmwlJAhsDAh4BAhkBAwsJBwIVCAMWAAIFJwkCBwIAAKfmAQDwrg8xuiTdYi\r\n xQFoK1fr/UREL2FpHE1pE9QLwO0slSsAD7BKMYfsSykTUxonqiJ+CK67EETbYIt+fIndA2GVaP5\r\n gDOOARkowOCEgorBgEEAZdVAQUBAQdAVllU+SypP4bqiTvgGIgg2O3GEpMUTE8MBkRdZEZIGRkD\r\n AQoJwngEGBYIACoFAmSjA4IJkNdnlbJKmwlJFiEEiFF73q0kUgWQiIbt12eVskqbCUkCGwwAAFd\r\n vAQCCv7i8tCLAp4Mwzh7fsOKQxqdq6tlq+FB/TPuaKGQiIgEAoSVwDasnAMoeH9GKjP29ExWORv\r\n PmxsmDMe0uzZy4xA0=\r\nITEM1.X-PM-ENCRYPT:true\r\nITEM1.X-PM-SIGN:true\r\nEND:VCARD",
                    "-----BEGIN PGP SIGNATURE-----\nVersion: ProtonMail\n\nwnQEARYKACcFgmTU8sgJkNugPN5MUMHiFiEE+Lr08JoIzKhiURy726A83kxQ\nweIAALmVAPdUP6po4T+VuAgQA6sf/bPbTNuxFjM4oqrKLKq6+6LnAQCDQCLM\nKmtqItfj4AZtXYpAdGjNDCMY7ImMT9OY1j3TDw==\n=bsrE\n-----END PGP SIGNATURE-----\n"
                )
            )
        )

}