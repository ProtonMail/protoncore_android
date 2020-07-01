@Suppress("MemberVisibilityCanBePrivate", "unused")
object Module {

    // region Common
    // Utils
    private const val util = ":util"
    const val kotlinUtil = "$util:util-kotlin"
    private const val androidUtil = "$util:util-android"
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
    // endregion

    // region Support
    // Network
    const val network = ":network"
    const val networkDomain = "$network:network-domain"
    const val networkData = "$network:network-data"
    // endregion

    // region Features
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
