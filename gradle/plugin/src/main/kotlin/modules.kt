/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Module {

    // region Common
    // Utils
    private const val util = ":util"
    const val kotlinUtil = "$util:util-kotlin"
    private const val androidUtil = "$util:android"
    const val sharedPreferencesUtil = "$androidUtil:util-android-shared-preferences"
    const val workManagersUtil = "$androidUtil:util-android-work-manager"
    const val gradleUtil = "$util:util-gradle"

    // Test
    private const val test = ":test"
    const val kotlinTest = "$test:test-kotlin"
    const val androidTest = "$test:test-android"
    const val androidInstrumentedTest = "$androidTest:test-android-instrumented"
    // endregion

    // region Shared
    const val domain = ":domain"
    const val presentation = ":presentation"
    const val data = ":data"
    const val gopenpgp = ":gopenpgp"
    // endregion

    // region Support
    // Network
    const val network = ":network"
    const val networkDomain = "$network:network-domain"
    const val networkData = "$network:network-data"
    // endregion

    // region Features

    // Authentication
    const val auth = ":auth"
    const val authDomain = "$auth:auth-domain"
    const val authPresentation = "$auth:auth-presentation"
    const val authData = "$auth:auth-data"

    // Account
    const val account = ":account"
    const val accountDomain = "$account:account-domain"
    const val accountPresentation = "$account:account-presentation"
    const val accountData = "$account:account-data"

    // AccountManager
    const val accountManager = ":account-manager"
    const val accountManagerDomain = "$accountManager:account-manager-domain"
    const val accountManagerPresentation = "$accountManager:account-manager-presentation"
    const val accountManagerData = "$accountManager:account-manager-data"
    const val accountManagerDagger = "$accountManager:account-manager-dagger"

    // Crypto
    const val crypto = ":crypto"
    const val cryptoCommon = "$crypto:crypto-common"
    const val cryptoAndroid = "$crypto:crypto-android"

    // Key
    const val key = ":key"
    const val keyDomain = "$key:key-domain"
    const val keyData = "$key:key-data"

    // Contact
    const val contact = ":contact"
    const val contactDomain = "$contact:contact-domain"
    const val contactData = "$contact:contact-data"

    // User
    const val user = ":user"
    const val userDomain = "$user:user-domain"
    const val userPresentation = "$user:user-presentation"
    const val userData = "$user:user-data"

    // Payment
    const val payment = ":payment"
    const val paymentDomain = "$payment:payment-domain"
    const val paymentData = "$payment:payment-data"
    const val paymentPresentation = "$payment:payment-presentation"

    // Countries
    const val country = ":country"
    const val countryDomain = "$country:country-domain"
    const val countryData = "$country:country-data"
    const val countryPresentation = "$country:country-presentation"

    // Settings
    const val settings = ":settings"
    const val settingsDomain = "$settings:settings-domain"
    const val settingsPresentation = "$settings:settings-presentation"
    const val settingsData = "$settings:settings-data"

    // Human Verification
    const val humanVerification = ":human-verification"
    const val humanVerificationDomain = "$humanVerification:human-verification-domain"
    const val humanVerificationPresentation = "$humanVerification:human-verification-presentation"
    const val humanVerificationData = "$humanVerification:human-verification-data"

    // Mail Message
    const val mailMessage = ":mail-message"
    const val mailMessageDomain = "$mailMessage:mail-message-domain"
    const val mailMessagePresentation = "$mailMessage:mail-message-presentation"
    const val mailMessageData = "$mailMessage:mail-message-data"

    // Mail Settings
    const val mailSettings = ":mail-settings"
    const val mailSettingsDomain = "$mailSettings:mail-settings-domain"
    const val mailSettingsPresentation = "$mailSettings:mail-settings-presentation"
    const val mailSettingsData = "$mailSettings:mail-settings-data"
    // Plan
    const val plan = ":plan"
    const val planDomain = "$plan:plan-domain"
    const val planData = "$plan:plan-data"
    const val planPresentation = "$plan:plan-presentation"
    // endregion
}
