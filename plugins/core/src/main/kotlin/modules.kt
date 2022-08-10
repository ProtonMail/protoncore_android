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
public object Module {

    // region Common
    // Utils
    private const val util = ":util"
    public const val kotlinUtil: String = "$util:util-kotlin"
    private const val androidUtil = "$util:android"
    public const val androidUtilDagger: String = "$androidUtil:util-android-dagger"
    public const val sharedPreferencesUtil: String = "$androidUtil:util-android-shared-preferences"
    public const val workManagersUtil: String = "$androidUtil:util-android-work-manager"
    public const val gradleUtil: String = "$util:util-gradle"

    // Test
    private const val test = ":test"
    public const val kotlinTest: String = "$test:test-kotlin"
    public const val androidTest: String = "$test:test-android"
    public const val androidInstrumentedTest: String = "$androidTest:test-android-instrumented"
    // endregion

    // region Shared
    public const val corePlatform: String = ":core-platform"
    public const val domain: String = ":domain"
    public const val presentation: String = ":presentation"
    public const val presentationCompose: String = ":presentation-compose"
    public const val data: String = ":data"
    public const val dataRoom: String = ":data-room"
    public const val gopenpgp: String = ":gopenpgp"
    // endregion

    // region Support
    // Network
    public const val network: String = ":network"
    public const val networkDagger: String = "$network:network-dagger"
    public const val networkDomain: String = "$network:network-domain"
    public const val networkData: String = "$network:network-data"
    // endregion

    // region Features

    // Authentication
    public const val auth: String = ":auth"
    public const val authDagger: String = "$auth:auth-dagger"
    public const val authDomain: String = "$auth:auth-domain"
    public const val authPresentation: String = "$auth:auth-presentation"
    public const val authData: String = "$auth:auth-data"

    // Account
    public const val account: String = ":account"
    public const val accountDagger: String = "$account:account-dagger"
    public const val accountDomain: String = "$account:account-domain"
    public const val accountPresentation: String = "$account:account-presentation"
    public const val accountData: String = "$account:account-data"

    // AccountManager
    public const val accountManager: String = ":account-manager"
    public const val accountManagerDomain: String = "$accountManager:account-manager-domain"
    public const val accountManagerPresentation: String = "$accountManager:account-manager-presentation"
    public const val accountManagerPresentationCompose: String = "$accountManager:account-manager-presentation-compose"
    public const val accountManagerData: String = "$accountManager:account-manager-data"
    public const val accountManagerDataDb: String = "$accountManager:account-manager-data-db"
    public const val accountManagerDagger: String = "$accountManager:account-manager-dagger"

    // Crypto
    public const val crypto: String = ":crypto"
    public const val cryptoDagger: String = "$crypto:crypto-dagger"
    public const val cryptoCommon: String = "$crypto:crypto-common"
    public const val cryptoAndroid: String = "$crypto:crypto-android"

    // CryptoValidator
    public const val cryptoValidator: String = ":crypto-validator"
    public const val cryptoValidatorDagger: String = "$cryptoValidator:crypto-validator-dagger"
    public const val cryptoValidatorData: String = "$cryptoValidator:crypto-validator-data"
    public const val cryptoValidatorDomain: String = "$cryptoValidator:crypto-validator-domain"
    public const val cryptoValidatorPresentation: String = "$cryptoValidator:crypto-validator-presentation"

    // Account
    public const val eventManager: String = ":event-manager"
    public const val eventManagerDagger: String = "$eventManager:event-manager-dagger"
    public const val eventManagerDomain: String = "$eventManager:event-manager-domain"
    public const val eventManagerData: String = "$eventManager:event-manager-data"

    // Key
    public const val key: String = ":key"
    public const val keyDagger: String = "$key:key-dagger"
    public const val keyDomain: String = "$key:key-domain"
    public const val keyData: String = "$key:key-data"

    // Label
    public const val label: String = ":label"
    public const val labelDomain: String = "$label:label-domain"
    public const val labelData: String = "$label:label-data"
    public const val labelDagger: String = "$label:label-dagger"

    // Contact
    public const val contact: String = ":contact"
    public const val contactDomain: String = "$contact:contact-domain"
    public const val contactData: String = "$contact:contact-data"
    public const val contactDagger: String = "$contact:contact-dagger"

    // User
    public const val user: String = ":user"
    public const val userDagger: String = "$user:user-dagger"
    public const val userDomain: String = "$user:user-domain"
    public const val userPresentation: String = "$user:user-presentation"
    public const val userData: String = "$user:user-data"

    // Payment-Common
    public const val paymentCommon: String = ":payment-common"
    public const val paymentCommonDagger: String = "$paymentCommon:payment-common-dagger"
    public const val paymentCommonData: String = "$paymentCommon:payment-common-data"
    public const val paymentCommonDomain: String = "$paymentCommon:payment-common-domain"
    public const val paymentCommonPresentation: String = "$paymentCommon:payment-common-presentation"

    // Payment
    public const val payment: String = ":payment"
    public const val paymentDagger: String = "$payment:payment-dagger"
    public const val paymentDomain: String = "$payment:payment-domain"
    public const val paymentData: String = "$payment:payment-data"
    public const val paymentPresentation: String = "$payment:payment-presentation"

    // Payment-IAP
    public const val paymentIap: String = ":payment-iap"
    public const val paymentIapPresentation: String = "$paymentIap:payment-iap-presentation"

    // Countries
    public const val country: String = ":country"
    public const val countryDagger: String = "$country:country-dagger"
    public const val countryData: String = "$country:country-data"
    public const val countryDomain: String = "$country:country-domain"
    public const val countryPresentation: String = "$country:country-presentation"

    // Settings
    public const val userSettings: String = ":user-settings"
    public const val userSettingsDagger: String = "$userSettings:user-settings-dagger"
    public const val userSettingsDomain: String = "$userSettings:user-settings-domain"
    public const val userSettingsPresentation: String = "$userSettings:user-settings-presentation"
    public const val userSettingsData: String = "$userSettings:user-settings-data"

    // Human Verification
    public const val humanVerification: String = ":human-verification"
    public const val humanVerificationDagger: String = "$humanVerification:human-verification-dagger"
    public const val humanVerificationDomain: String = "$humanVerification:human-verification-domain"
    public const val humanVerificationPresentation: String = "$humanVerification:human-verification-presentation"
    public const val humanVerificationData: String = "$humanVerification:human-verification-data"

    // Mail Message
    public const val mailMessage: String = ":mail-message"
    public const val mailMessageDagger: String = "$mailMessage:mail-message-dagger"
    public const val mailMessageDomain: String = "$mailMessage:mail-message-domain"
    public const val mailMessagePresentation: String = "$mailMessage:mail-message-presentation"
    public const val mailMessageData: String = "$mailMessage:mail-message-data"

    // Mail Settings
    public const val mailSettings: String = ":mail-settings"
    public const val mailSettingsDagger: String = "$mailSettings:mail-settings-dagger"
    public const val mailSettingsDomain: String = "$mailSettings:mail-settings-domain"
    public const val mailSettingsPresentation: String = "$mailSettings:mail-settings-presentation"
    public const val mailSettingsData: String = "$mailSettings:mail-settings-data"

    // Plan
    public const val plan: String = ":plan"
    public const val planDagger: String = "$plan:plan-dagger"
    public const val planDomain: String = "$plan:plan-domain"
    public const val planData: String = "$plan:plan-data"
    public const val planPresentation: String = "$plan:plan-presentation"

    public const val push: String = ":push"
    public const val pushDomain: String = "$push:push-domain"
    public const val pushData: String = "$push:push-data"
    public const val pushDagger: String = "$push:push-dagger"

    // Reports
    public const val report: String = ":report"
    public const val reportDomain: String = ":report:report-domain"
    public const val reportData: String = ":report:report-data"
    public const val reportPresentation: String = ":report:report-presentation"
    public const val reportDagger: String = ":report:report-dagger"

    // Feature flags
    public const val featureFlag: String = ":feature-flag"
    public const val featureFlagData: String = "$featureFlag:feature-flag-data"
    public const val featureFlagDomain: String = "$featureFlag:feature-flag-domain"
    public const val featureFlagDagger: String = "$featureFlag:feature-flag-dagger"

    // Metrics
    public const val metrics: String = ":metrics"
    public const val metricsDomain: String = "$metrics:metrics-domain"
    public const val metricsData: String = "$metrics:metrics-data"
    public const val metricsDagger: String = "$metrics:metrics-dagger"

    // Challenge
    public const val challenge: String = ":challenge"
    public const val challengeDagger: String = ":challenge:challenge-dagger"
    public const val challengeDomain: String = ":challenge:challenge-domain"
    public const val challengeData: String = ":challenge:challenge-data"
    public const val challengePresentation: String = ":challenge:challenge-presentation"

    // Proguard rules
    public const val proguardRules: String = ":proguard-rules"

    // endregion
}
