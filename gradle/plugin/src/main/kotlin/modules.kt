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

    // User
    const val user = ":user"
    const val userDomain = "$user:user-domain"
    const val userPresentation = "$user:user-presentation"
    const val userData = "$user:user-data"

    // Contacts
    const val contacts = ":contacts"
    const val contactsDomain = "$contacts:contacts-domain"
    const val contactsPresentation = "$contacts:contacts-presentation"
    const val contactsData = "$contacts:contacts-data"

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
    // endregion
}
