# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixes

- Auth & Human Verification: Closing Human Verification screen on sign up goes back to plans.
- Added Presentation Compose module.
- Added AccountManager Presentation Compose module.
- Auth: Fixed Auth UserCheck Error Message (Toast for 2pass & 2factor).
- Fixed NestedPrivateKey isActive flag.

### Changes

- Human Verification, Network, Payment, Presentation: Added Human Verification v3.
- Align plans screen padding.

### Breaking changes

- Human Verification: `@CaptchaApiHost` annotation renamed to `@HumanVerificationApiHost`. 
- Network: ```LogTag.API_CALL``` is removed. ```LogTag.API_REQUEST```, ```LogTag.API_RESPONSE``` and ```LogTag.API_ERROR``` are introduced to allow clients more control over logging API calls.

### New Injection

```kotlin
@Provides
fun provideNetworkRequestOverrider(): NetworkRequestOverrider =
    NetworkRequestOverriderImpl(OkHttpClient()) // Or some other OkHttpClient dependency
```

## [3.0.0]

### Changes

- Presentation: Added SnackType to represent Snackbar types (success, error etc.).
- Auth: After 2FA code fails for the third time, an error toast is displayed; clients should handle the error by registering `AccountManager.observe(..).onSessionSecondFactorFailed` callback and showing the login screen again.
- GOpenpgp: Updated golang build to gopenpgp v2.3.0 [Changelog](https://github.com/ProtonMail/gopenpgp/blob/master/CHANGELOG.md#230-2021-11-15)
- Auth: Added info about workflow type (sign-in/up) to AddAccountResult.
- Auth: Reverse order of 'Sign in' and 'Create an account' buttons in the add account screen, also changed their styles.
- Auth: Moved UserCheck at the end of Account Setup.
- Add error handlers for the retried API calls.

### Deprecations

- View.snack extension functions that accept a background drawable ID are deprecated. Use the snack functions that accept a SnackType instead.

### Fixed

- During signup, when choosing username, "Next" button would be sometimes disabled permanently
- Build process on Windows.

### Breaking changes

- Auth: `SetupAccountCheck.UserCheck` moved/renamed to `PostLoginAccountSetup.UserCheck`.
- Auth: `SetupAccountCheck.Action` moved/renamed to `UserCheckAction`.
- Key: Renamed function: `List<KeyHolderPrivateKey>.areAllLocked()` -> `areAllInactive`.
- Key: Replaced function: `PrivateKey.isUnlockable()` -> `isActive`.

### Behavior changes

- User/UserAddress Keys are considered active (PrivateKey.isActive) **only if they can be unlocked**, and vice-versa.

### New Injection

```kotlin
@Provides
@Singleton
@AccountStateHandlerCoroutineScope
fun provideAccountStateHandlerCoroutineScope(): CoroutineScope =
   CoroutineScope(Dispatchers.Default + SupervisorJob())

@Provides
@Singleton
fun provideAccountMigrator(
    accountManager: AccountManager,
    accountRepository: AccountRepository,
    userRepository: UserRepository
): AccountMigrator = AccountMigratorImpl(accountManager, accountRepository, userRepository)

@Provides
@Singleton
fun provideAccountStateHandler(
    @AccountStateHandlerCoroutineScope
    scope: CoroutineScope,
    userManager: UserManager,
    accountManager: AccountManager,
    accountRepository: AccountRepository,
    accountMigrator: AccountMigrator
): AccountStateHandler = AccountStateHandler(scope, userManager, accountManager, accountRepository, accountMigrator)
```

### New Migration

```kotlin
val MIGRATION_XY_XZ = object : Migration(XY, XZ) {
    override fun migrate(database: SupportSQLiteDatabase) {
        AccountDatabase.MIGRATION_4.migrate(database)
        AddressDatabase.MIGRATION_3.migrate(database)
        UserDatabase.MIGRATION_1.migrate(database)
    }
}
```

### New Features

New `AccountState`:
- `MigrationNeeded`: State emitted if this [Account] need a migration to be [Ready] to use.
- `UserKeyCheckFailed`: User key check has failed.
- `UserAddressKeyCheckFailed`: User Address key check has failed.

New `AccountManagerObserver` extensions:
```kotlin
.onAccountMigrationNeeded { context.showToast("MigrationNeeded") }
.onUserKeyCheckFailed { context.errorToast("InvalidUserKey") }
.onUserAddressKeyCheckFailed { context.errorToast("InvalidUserAddressKey") }
```

New `AccountStateHandler`:
- Mandatory Account State handling that are not optional for Client.
- Note: `AccountStateHandler.start()` **must** be called in Client side (consider using Initializer).

## [2.0.0]
- Use a global version for all core artifacts

## Human Verification [1.16.4]

24 Nov, 2021

### Changes
- Fix the URLs in Human Verification Help screen.

## Plan [1.18.3]

22 Nov, 2021

### Changes
- Provide plans for signup and upgrade flow separately

## Network [1.16.0]

22 Nov, 2021

### Changes

- Added Api Connection error support, that can be used by the clients to overcome potential blocked api.
Mainly and initially this is supported now from VPN client for their guest hole feature.
- Clients should provide a new dependency for `ApiConnectionListener` or null if they do not want
to support it.

## Auth [1.18.4], Network [1.15.8], Util Kotlin [1.15.3]
## Auth [1.18.5], Network [1.15.9], Util Kotlin [1.15.4]

22 Nov, 2021

### Dependencies

- Contact 1.19.3
- Payment 1.17.5

### Changes

- Minimum (and the default) network timeout in `ApiClient` is 30 seconds
- Recover from error when creating new accounts ("Username already taken or not allowed")
- Recover from errors while setting up user keys after creating an account ("Primary key exists")

## EventManager Version [1.19.2]

Nov 19, 2021

### New Features

- Added two new callbacks to `EventListener`:
  - `onSuccess` will be called when the modifications are executed with no issues.
  - `onFailure` will be called after the modifications failed too many times and `resetAll` has run.

## EventManager Version [1.19.1]

Nov 18, 2021

### Dependencies

- Contact 1.19.1.
- EventManager 1.19.1.
- Mail Settings 1.19.1.
- User 1.19.1.
- User Settings 1.19.1.

### New Features

- Added EventManagerConfig parameter to all EventListener functions.

### Breaking Changes

- EventListener functions signature changed (userId removed, added config).

## Plan Presentation [1.18.1]

17 Nov, 2021

### Changes
- Fixes base strings with replaceable placeholders for translations.

## Binary incompatibility bump

16 Nov, 2021

### Changes
- fix binary incompatibility — bump version for all libraries that use `presentation` module

### Dependencies

- AccountManager 1.16.2
- Country 1.16.2
- EventManager 1.18.1
- HumanVerification 1.16.3
- Payment 1.17.3
- Plan 1.18.1

## Auth [1.18.3]

15 Nov, 2021

### Changes

- Added `LoginTestHelper` that can be used in instrumented tests to login, without the need to perform any UI actions

15 Nov, 2021

## Auth Presentation [1.18.2]

### Changes
- Switch between External and Username when External account type is required by the client.

12 Nov, 2021

## User Settings [1.18.2]

### Changes

- Refactor password management mailbox form to be in sync with the login form when presented to the user.

### No Breaking changes, clients can just update to the latest versions.

## Auth [1.18.1]

12 Nov, 2021

### Changes

- Refactored login process (split `LoginViewModel` into multiple classes) - no functionality change

## Mail Message [1.15.1]

### Changes
- Incremented version for Mail Message module to fix a `NoSuchMethodError` crash

## Test Android Instrumented [1.15.5]

### Changes
- Fix OnView.checkDoesNotExist()

## Test Android Instrumented [1.15.4]

### Changes
- Add missing customAction API

## Key [1.16.1]

08 Nov, 2021

### Changes
Handle the case where the signed key list has a null signature.
Modifies:
- `SignedKeyListEntity` (embedded in  `PublicAddressEntity` and `AddressEntity`)
- `PublicSignedKeyList`
- `SignedKeyListResponse`

## Test Android Instrumented [1.15.3]

05 Nov, 2021

### Changes

- Updated plan robots

## Plan [1.18.0]

04 Nov, 2021

### Changes

- Plans now support the latest UI design changes.

### No Breaking changes, clients can just update to the latest versions.
### Note: Clients should upgrade to Presentation v1.18.3

## Auth [1.17.6]

03 Nov, 2021

### Changes

- Allow Login without keys for Product VPN with AccountType External.

## Country [1.16.1], Human-Verification [1.16.2]

29 Oct, 2021

Bug fixes

- Incorrect background in HV and country picker

## Presentation [1.18.1]

28 Oct, 2021

### Changes

- Added Snackbar proton_notification_norm to color taxonomy. 
- Please use View extensions for Snackbar:
  - View.normSnack
  - View.warningSnack
  - View.errorSnack
  - View.successSnack

## Auth [1.17.5]

28 Oct, 2021

### Bug fixes

- Screen with Recovery Method is not shown sometimes

## User-Settings [1.16.1]

20 Oct, 2021

### Bug fixes

- Cannot change password for legacy users with 2fa enabled.

## Version [1.18]

25 Oct, 2021

### Dependencies

- Contact 1.18.
- Domain 1.18.
- EventManager 1.18.
- MailSettings 1.18.
- Presentation 1.18.
- User 1.18.
- UserSettings 1.18.

### New Migration

- Please apply changes as follow to your AppDatabase:
  - Add ```EventMetadataEntity``` to your AppDatabase ```entities```.
  - Add ```EventManagerConverters``` to your ```TypeConverters```.
  - Extends ```EventMetadataDatabase```.
  - Add a migration to your AppDatabase (```addMigration```):
```
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        EventMetadataDatabase.MIGRATION_0.migrate(database)
    }
}
```

### New Dagger Module

- To provide the various EventManager components:
```
@Module
@InstallIn(SingletonComponent::class)
object EventManagerModule {

    @Provides
    @Singleton
    @EventManagerCoroutineScope
    fun provideEventManagerCoroutineScope(): CoroutineScope =
        CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Provides
    @Singleton
    @JvmSuppressWildcards
    fun provideEventManagerProvider(
        eventManagerFactory: EventManagerFactory,
        eventListeners: Set<EventListener<*, *>>
    ): EventManagerProvider =
        EventManagerProviderImpl(eventManagerFactory, eventListeners)

    @Provides
    @Singleton
    fun provideEventMetadataRepository(
        db: EventMetadataDatabase,
        provider: ApiProvider
    ): EventMetadataRepository = EventMetadataRepositoryImpl(db, provider)

    @Provides
    @Singleton
    fun provideEventWorkManager(
        workManager: WorkManager,
        appLifecycleProvider: AppLifecycleProvider
    ): EventWorkManager = EventWorkManagerImpl(workManager, appLifecycleProvider)

    @Provides
    @Singleton
    @ElementsIntoSet
    @JvmSuppressWildcards
    fun provideEventListenerSet(
        userEventListener: UserEventListener,
        userAddressEventListener: UserAddressEventListener,
        userSettingsEventListener: UserSettingsEventListener,
        mailSettingsEventListener: MailSettingsEventListener,
        contactEventListener: ContactEventListener,
        contactEmailEventListener: ContactEmailEventListener,
    ): Set<EventListener<*, *>> = setOf(
        userEventListener,
        userAddressEventListener,
        userSettingsEventListener,
        mailSettingsEventListener,
        contactEventListener,
        contactEmailEventListener,
    )
}
```
- To provide AppLifecycleObserver, AppLifecycleProvider and WorkManager (needed by EventManager):
```
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    ...
    @Provides
    @Singleton
    fun provideAppLifecycleObserver(): AppLifecycleObserver =
        AppLifecycleObserver()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
    ...
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationBindsModule {
    @Binds
    abstract fun provideAppLifecycleStateProvider(observer: AppLifecycleObserver): AppLifecycleProvider
}
```

### WorkManager

**Initialization of WorkManager is up to the client.**

EventManager/EventWorker assume the client support injecting:
```
@HiltWorker
open class EventWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
...
```

Please refer to:
- https://developer.android.com/training/dependency-injection/hilt-jetpack#workmanager
- https://developer.android.com/topic/libraries/architecture/workmanager/advanced/custom-configuration.

### Changes

- Added EventManager Domain/Data and EventListeners.
- Added EventManager LogTags.
- Added EventManagerConfig and EventManagerConfigProvider.
- Added EventWorkManager and EventManagerWorker.
- Added UserEventListener.
- Added UserAddressEventListener.
- Added MailSettingsEventListener.
- Added UserSettingsEventListener.
- Added ContactEventListener.
- Added ContactEmailsEventListener.
- Added AppLifecycleObserver and AppLifecycleProvider.

## Presentation [1.17.0]

### Changes

- `ProtonInput` password field (inputType TextPassword) is now cleared when returning from background if more than 3 min have passed.

## Auth [1.17.3], Human Verification [1.16.1], User settings [1.16.1], Presentation [1.16.1]

22 Oct, 2021

### Changes

- Add missing versions for Auth and User settings modules.
- Added `ScreenContentsProtector` with shared logic for both activities and fragments: it can be added to any component, inheritance is no longer mandatory but is encouraged.
- Moved most of `ProtonSecureActivity`'s logic to `ScreenContentsProtector` added a `ProtonSecureFragment` counterpart to apply security measures to fragments automatically.
- Fix a bug when several protected fragments were shown on screen.
- Added a `by protectScreen(ProtectScreenConfiguration)` delegate to automatically protect and unprotect activities and fragments based on their lifecycles. You can either use this delegate, the `ProtonSecure` superclasses or use `ScreenContentsProtector` manually and carefully checking that all secure components are protected and then unprotected when no longer needed.

## Auth [1.17.1], User [1.16.2].

19 Oct, 2021

### Bug fixes

- Force Change password via Web only for private sub-user.

## Auth [1.16.1], User [1.16.1].

15 Oct, 2021

### Bug fixes

- Fixed Login for AccountType Username (VPN).
- Allow Login without full/keys scopes, without fetching addresses if not needed.
- Return Success for UnlockUserPrimaryKey without keys.

## User [1.15.6], Key [1.15.6], Account-Manager [1.15.6]

15 Oct, 2021

### Changes
- `AddressEntity` and `PublicAddressEntity` now store the signed key list of the address.
- `PublicAddressDatabase` migration 1 and `AddressDatabase` migration 2 to reflect the model changes.
- Updated db schema of `coreexample` to version 10 to reflect the model changes.
- Updated db schema of `account-manager` to version 10 to reflect the model changes.
- Removed `PrivateKey.signedKeyList()` to replace it with `UserAddress.signKeyList()`

## Plan and Payments [1.16.0]

15 Oct, 2021

### Changes

- Plan and Payments now rely on the newest V4 API, including changes in the API response, requests and entities.

### Breaking changes: Instead on plan id, now clients should rely and provide plan names for the paid plans they support.

## Auth [1.15.5]

15 Oct, 2021

### Changes

- Fixed: For some set of languages (e.g. Polish, Czech), app would crash during signup when choosing recovery method.

## Presentation [1.15.7]

13 Oct, 2021

### Changes

- Add new colors to the taxonomy for the sidebar component.

## Human Verification [1.15.5], Presentation [1.15.6]

13 Oct, 2021

### Changes

- Protect screens with visible password fields using WindowManager's FLAG_SECURE.
- Add `ProtonSecureActivity` that can be extended to apply security measures to any activity. At the moment, it's being extended by `AuthActivity`.

## Crypto and Key  [1.15.5]

11 Oct, 2021

### New features

- Add support to generate and verify encrypted signatures, example:

```
  // Key holder 1 signs the message and encrypts the signature for Key Holder 2
  val encryptedSignature = keyHolder1.useKeys(context) {
      signDataEncrypted(data, keyHolder2PublicAddress.publicKeyRing())
  }
  // Key holder 2 decrypts the signature and verifies it with Key Holder 1's public keys.
  val verified = keyHolder2.useKeys(context) {
      verifyDataEncrypted(data, encryptedSignature, keyHolder1PublicAddress.publicKeyRing())
  }
```

## Crypto and Key  [1.15.4]

8 Oct, 2021

### Changes

- Replaced Signature Verification validAtUtc (Long) by VerificationTime (Ignore, Now, Utc).

## Crypto [1.15.3]

### Changes

- Removed internal implementations `PasswordVerifier` and `BigIntegerCalculator`
  and use `go-srp` to compute the password verifier instead.

## Network [1.15.6], Android Test [1.15.1]

### Changes
- Use mockk-android instead of mockk for android tests.
- Fix network dependency declaration causing compilation of library module to fail.

## Human-Verification [1.15.4]

### Changes

- Removed `HumanVerificationActivity` — use `HumanVerificationOrchestrator` instead
- Fix: In signup activity, the view-model's state is preserved if the activity is temporarily destroyed
- Updated nullability of `HumanVerificationOrchestrator.setOnHumanVerificationResult` callback:
  `HumanVerificationResult` parameter is never null; check `token` property
  to see if human verification was successful or not

### Dependencies

- Auth [1.15.4]
- Presentation [1.15.5]
- User-Settings [1.15.3]

## Human Verification [1.15.3], Network [1.15.5]

Oct 6, 2021

### Changes

- Add ExtraHeaderProvider to pass arbitrary headers to all API requests.
- Allow captcha WebView to use ExtraHeaderProvider's headers.

## Network [1.15.4]

5 Oct, 2021

### Changes

- Add log in ServerTimeInterceptor on Date parse failure.

## Key [1.15.3], Network [1.15.3]

Oct 4, 2021

### Changes

- Add CacheOverride OkHttp tag and CacheOverrideInterceptor to modify cache behavior per-request.

## Presentation [1.15.4]

Sep 30, 2021

### Changes

- Add a work around for MIUI Dark Mode breaking theming.

## Presentation [1.15.3], Auth [1.15.3]

Sep 27, 2021

### Changes

- Add styling for dialogs created with MaterialAlertDialogBuilder.
- Use MaterialAlertDialogBuilder for all dialogs.
- Add styling for action menu text.

## Crypto Version [1.15.2]

Sep 20, 2021

### Dependencies

- Auth 1.15.2.
- Account 1.15.2.
- Crypto 1.15.2.
- Human-Verification 1.15.2.
- Key 1.15.2.
- Network 1.15.2.
- Util Kotlin 1.15.2.
- User 1.15.2.
- User-Settings 1.15.2.

### Api Changes

- Logger is no more injected. Instead Core use a static ```CoreLogger```. You now have to set the Logger instance, on Application create:
```
override fun onCreate() {
    super.onCreate()
    CoreLogger.set(CoreExampleLogger())
```
- There is also a new KeyStoreCrypto LogTag object you must be aware:
```
object LogTag {
    /** Tag for KeyStore initialization check failure. */
    const val KEYSTORE_INIT = "core.crypto.common.keystore.init"

    /** Tag for KeyStore encrypt failure. */
    const val KEYSTORE_ENCRYPT = "core.crypto.common.keystore.encrypt"

    /** Tag for KeyStore decrypt failure. */
    const val KEYSTORE_DECRYPT = "core.crypto.common.keystore.decrypt"
}
```

### New Features

- KeyStoreCrypto fallback if Android KeyStore is not properly working.

## Presentation [1.15.1]

Sep 21, 2021

### Changes

- Removed `FragmentManager.showForceUpdate` method; to present a "Forced Update" dialog, use `ForceUpdateActivity` instead

## User Settings [1.15.1]

Sep 20, 2021

### New Features

- Added IntEnum support for UserSettings.

## Test Android Instrumented [1.15.1]

Sep 12, 2021

### New Features

- Added PasswordManagementRobot

## Version [1.15.0]

Sep 8, 2021.

Upgrade Kotlin to 1.5.

### Dependencies

- Core Modules 1.15.0.
- Kotlin 1.5.30.
- Kotlin Coroutines 1.5.2.
- Kotlin Serialization 1.2.2.
- Gradle 7.2.
- Java 11.
- Android Gradle Plugins 7.0.2.
- Android Tools 30.0.2.
- Android Annotation 1.2.0.
- AndroidX Activity 1.3.1.
- AndroidX AppCompat 1.3.1.
- AndroidX Constraintlayout 2.1.0.
- AndroidX Fragment 1.3.6.
- AndroidX KTX 1.6.0.
- AndroidX Lifecycle 2.4.0-alpha03.
- AndroidX Paging 1.4.0.
- AndroidX Work 2.6.0.
- AndroidX Test 1.4.0.
- Android Material Components 1.4.0.
- Dagger 2.38.1
- Hilt 2.38.1.
- Timber 5.0.1.
- Turbine 0.6.1.
- OkHttp 4.9.1.
- Trust Kit 1.1.5.
- Store 4.0.2-KT15.
- Lottie 4.1.0.
- Falcon 2.2.0.
- Apache Common Codec 1.15.
- Gotev CookieStore 1.3.5.
- Google Tink 1.6.1.
- Espresso 3.4.0.
- Robolectric 4.6.1.
- Mockk 1.11.0.
- Junit KTX 1.1.3.

## User Settings Version [1.7.1]

Sep 10, 2021

### Added features

- Add better result callback for password management and recovery email change features.
- Password management and recovery email change screens now auto-close themselves on success.

### Dependencies

- UserSettings 1.7.1

## Crypto Version [1.6.3]

Sep 09, 2021

### Dependencies

- Crypto 1.6.3.
- Key 1.6.3.
- User 1.6.3.

### New Features

- Added encryptAndSign/decryptAndVerify PublicKeyRing parameter (default to KeyHolderContext.publicKeyRing).

## Presentation [1.6.1]

Sep 09, 2021

### Changes

- Revert tab ripple effect to the default.

## Auth Version [1.7.0]

Sep 03, 2021

### Bug fixes

- Fixed dialog fragments crash on orientation change, mainly this stands for ProtonCancellableDialogFragment and PasswordAnd2FADialogFragment
- Note, breaking change, the API of these dialog fragments has changed a bit.

### Dependencies

- Auth 1.7.0
- HumanVerification 1.6.0
- Presentation 1.6.0
- UserSettings 1.7.0

### New Usage Example with fragment result pattern
```
private lateinit var requestNewCodeDialogResultLauncher: FragmentDialogResultLauncher<String>

requestNewCodeDialogResultLauncher =
            parentFragmentManager.registerRequestNewCodeDialogResultLauncher(this@HumanVerificationEnterCodeFragment) {
                // your code
            }

equestNewCodeDialogResultLauncher.show("string")
```

## Crypto Version [1.6.2]

Aug 31, 2021

### Dependencies

- Crypto 1.6.2.
- Key 1.6.2.

### Bug Fixes

- Fixed HashKey signature verification (do not throw IllegalArgumentException anymore on decryption).

## Test Android Instrumented [0.6.7]

Aug 30, 2021

### New Features

- Added RecoveryEmail robot

## Test Android Instrumented [0.6.6]

Aug 27, 2021

### New Features

- Added startsWith() matcher
- Added user creation quark commands
- Minor test data classes improvements

## Presentation [1.5.5]

Aug 27, 2021

### New Features

- Added ```?attr/proton_text_accent``` and ```?attr/proton_icon_accent```.

### Style Changes

- Linear progress bar styling has changed. The track is now 50% of the brand color so that it is lighter than the indicator in light mode and darker in dark mode.

### Bug Fixes

- ProtonRadioButton and ProtonCheckbox animations are fixed in Android 6.
- ProtonRadioButton and ProtonCheckbox padding is fixed to avoid long labels overlapping the button.

## Crypto Version [1.6.1]

Aug 25, 2021

### Dependencies

- Crypto 1.6.1.
- Key 1.6.1.
- Mail Message 1.6.1.
- Util Kotlin 0.2.6.

### New Features

- Added Crypto encrypt/decrypt File with KeyPacket/SessionKey/HashKey support.

## Auth Version [1.6.1]

Aug 25, 2021

### Bug Fixes

- Fixed SetupAccountCheck to always refresh User Addresses before proceeding.

## User Settings Version [1.6.0]

Aug 17, 2021

### New Features

- Password Change: Single Pass mode password change, Two Pass mode login and mailbox password change.

### New Migration

- If you use ```Account Manager Data Db``` module, nothing to do, it's transparently applied.
- If you use your own AppDatabase, please apply changes as follow:
  - Add ```OrganizationEntity``` to your AppDatabase ```entities```.
  - Add ```OrganizationKeysEntity``` to your AppDatabase ```entities```.
  - Add a migration to your AppDatabase (```addMigration```):
```
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        OrganizationDatabase.MIGRATION_0.migrate(database)
    }
}
```

### Dependencies

- Account Manager 1.6.
- Auth 1.6.
- Crypto 1.6.
- Key 1.6.
- User 1.6.
- UserSettings 1.6.

## Crypto Version [1.5.3]

Aug 18, 2021

### Dependencies

- Crypto 1.5.3.
- Key 1.5.3.
- User 1.5.3.

### Behavior Changes

- Set X25519 as default Key type for generation (removed RSA).
- Changed Key identity as: email <email>.

## Presentation Version [1.5.4]

Aug 20, 2021

### New Features

- Added color ```?attr/proton_blender_norm``` for greying out content behind dialogs.

## Network Version [1.5.4]

Aug 18, 2021

### New Features

- Added optional OkHttp Cache (see ```ApiManagerFactory```). This cache is shared across all user, session, api or call.

### Breaking Changes

- You'll have to instantiate/use ```ApiResult.Error.NoInternet()``` instead of the static object.

### Behavior Changes

- Removed ```isConnectedToNetwork``` check before trying a call (see ```ApiManagerImpl```).
- You'll now receive ```ApiResult.Error.NoInternet``` only on ```UnknownHostException```.

## Network Version [1.5.3]

Aug 18, 2021

### Bug Fixes

- Fixed Network error retryable conditions:
```
/**
 * Return true if [ApiResult] is retryable (e.g. connection issue or http error 5XX).
 */
fun <T> ApiResult<T>.isRetryable(): Boolean = when (this) {
    is ApiResult.Success,
    is ApiResult.Error.Parse,
    is ApiResult.Error.Certificate,
    is ApiResult.Error.TooManyRequest -> false
    is ApiResult.Error.Connection -> true
    is ApiResult.Error.Http -> httpCode in 500..599
}
```

### Behavior Changes

- As a result of the above fix, any HTTP Error 5XX will trigger backoff retry, see ```ApiClient```:
```
/**
 * Retry count for exponential backoff.
 */
val backoffRetryCount: Int get() = 2

/**
 * Base value (in milliseconds) for exponential backoff logic. e.g. for value 500 first retry
 * will happen randomly after 500-1000ms, next one after 1000-2000ms ...
 */
val backoffBaseDelayMs: Int get() = 500
```

## Presentation Version [1.5.3]

Aug 17, 2021

### Bug Fixes

- Fixed ProtonCheckbox and ProtonRadioButton not updating their button drawable in certain scenarios.

## Version [1.5.2]

Aug 13, 2021

Various SignUp/SignIn fixes & improvements.

### Dependencies

- Account 1.5.2.
- Auth 1.5.2.
- Human Verification 1.5.2.
- Network 1.5.2.
- User 1.5.2.
- Plan 1.5.2.
- Presentation 1.5.2.

### Bug Fixes

- HumanVerification: Fixed concurrency issue while updating state/token.
- Account: Fixed concurrency issue while updating account/session state.
- Auth: Fixed error on login within SignUp process.
- Auth: Fixed wrong initial Fragment shown if available HV methods equal ("sms", "email").
- Auth: Fixed "Recovery Email" & "Recovery Phone number".
- Auth: Fixed blank username error.
- Auth: Fixed passwords do not match error.
- Auth Presentation: Fixed Add Account Portrait Orientation.
- Presentation: Fixed TextInputLayout Outline stroke (color/size).
- Presentation: Fixed OnBackPressedCallback & and Fragments backstack flow.
- Presentation: Fixed Fragment hideKeyboard.
- Auth: Fixed missing state changes (e.g. no network).

### New Features

- Network: Added TooManyRequest error handling per Session.
- HumanVerification: Added SMS/Email Token code validation.
- User: Added default Domain fallback.

### API Changes

- ```UserVerificationRepositoryImpl``` do not depend anymore on ```clientIdProvider``` & ```humanVerificationRepository```:
```
@Provides
@Singleton
fun provideUserVerificationRepository(
    apiProvider: ApiProvider
): UserVerificationRepository = UserVerificationRepositoryImpl(apiProvider)
```
- You must now provide a Default Domain Host to ```DomainRepositoryImpl```. 
``` 
@Provides
@DefaultDomainHost
fun provideDefaultDomainHost() = Constants.HOST // "protonmail.com"
...
@Provides
@Singleton
fun provideDomainRepository(
    @DefaultDomainHost defaultDomain : Domain,
    provider: ApiProvider
): DomainRepository = DomainRepositoryImpl(defaultDomain, provider)
```

## Test Android Instrumented [0.6.5]

Aug 12, 2021

### New Features

- CoreRobot.addText() and replaceText() are added as replacements for setText().
  addText() behaves exactly the same as setText().

### Deprecations

- CoreRobot.setText() is deprecated for confusing behavior - it adds text instead of setting new text on a view.

## Presentation Version [0.10.3]

Aug 11, 2021

### Style Changes

- ```@color/text_weak_selector``` is updated to use ```?attr/proton_text_weak``` and ```?attr/proton_text_disabled``` instead of directly using ```@color``` values.

## Presentation Version [0.10.2]

Aug 5, 2021

### New Features

- Added View.snack extension that accepts a function block allowing for full customization of the created Snackbar.

### Style Changes

- Snackbar text styles have been updated to Proton.Text.\* to match designs.
- Snackbar text color has been forced to white also in dark theme.

## Presentation Version [0.10.1]

Aug 3, 2021

### API Changes

- ProtonCheckbox and ProtonRadioButton are now open classes.
- ProtonCheckbox and ProtonRadioButton don't use compound drawables to draw the button on the right. This means that setting a drawable with e.g. setCompoundDrawablesRelative(icon, null, null, null) doesn't unexpectedly break them.
- Add Proton.Text.Hero style.
- Add ProtonInput.clearTextAndOverwriteMemory() -  the method overwrites and clears the input's text buffer. It should be used to limit the time passwords are kept in memory.

## Translations Version [2021-07-30-140045]

Jul 30, 2021

### Dependencies

- Account Manager 1.5.1.
- Auth 1.4.1.
- Country 0.1.6.
- Human Verification 1.3.4.
- Payment 0.2.3.
- Plan 0.1.2.
- Presentation 0.10.1.

### New Supported Languages

- From Agency: en, de, fr.
- From Community: cs, es-rMX, fr-rCA, pl.
- You can filter them by using:
```
android {
    defaultConfig {
        resConfigs "de", "en", "fr"
    }
}
```

## User Settings Version [1.5.1]

Jul 30, 2021

### Bug Fixes

- Fixed UserSettingsDatabase MIGRATION_0.

## Version [1.5.0]

Jul 30, 2021

Add user settings, with initial update recovery email option.

### Dependencies

- Auth 1.5.0.
- Account Manager 1.5.0.
- User 1.5.0.
- UserSettings 1.5.0.
- Plan 0.2.0.
- Presentation 0.10.0.

### New Modules

- **User Settings**: Get and update UserSettings.

### New Migration

- If you use ```Account Manager Data Db``` module, nothing to do, it's transparently applied.
- If you use your own AppDatabase, please apply changes as follow:
    - Add ```UserSettingsEntity``` to your AppDatabase ```entities```.
    - Add ```UserSettingsConverters``` to your AppDatabase ```TypeConverters```.
    - Extends ```UserSettingsDatabase``` from your AppDatabase.
    - Add a migration to your AppDatabase (```addMigration```):
```
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        UserSettingsDatabase.MIGRATION_0.migrate(database)
    }
}
```

### New Features

- Added ```UserSettings module``` with Update Recovery Email feature.
- Added ```FragmentActivity.addOnBackPressedCallback``` in presentation module.

### Recommendations

- Do not expect ```UserSettings``` properties to be stable, they could change in future.

## Crypto Version [1.1.5], Key Version [1.3.2]

Jul 28, 2021

### New Features

- Added Crypto encryptAndSignFile & decryptAndVerifyFile.

## Mail Settings Version [1.3.2]

Jul 28, 2021

### New Features

- Added MailSettingsRepository updateMailSettings & MailSettingsResponse.toMailSettings.

## Account Manager Version [1.3.2]

Jul 27, 2021

### Bug Fixes

- Changed Account Initials Count to 1.

## Presentation Version [0.9.9]

Jul 26, 2021

### New Features

- Added ```ProtonCheckbox``` and ```ProtonRadioButton``` that display their "button" (i.e. the checkbox or circle) to the right of the label.

## User Version [1.3.2]

Jul 23, 2021

### Bug Fixes

- Fixed UserAddressRepositoryImpl issue when no address returned from fetcher.

### Recommendations

- You must update to this version because it prevent to properly sign in/up without address.

## Version [1.3.1]

Jul 21, 2021

Refactor Core Database.

### Dependencies

- Account 1.3.1.
- AccountManager 1.3.1.
- Data 1.3.1.
- DataRoom 1.3.1.
- HumanVerification 1.3.1.
- Key 1.3.1.
- MailSettings 1.3.1.
- User 1.3.1.

### New Modules

- **Data Room**: New module containing all Android Room specifics.
- **AccountManager Data Db**: New module containing old AccountManagerDatabase, for backward compatibility purposes.

### New Features

- ```SupportSQLiteDatabase``` extensions: ```getTableColumns```, ```recreateTable```, ```addTableColumn```, ```dropTableColumn```, ```dropTableContent``` or ```dropTable```.
- ```UserRepository.updateUser```: function for event handing.
- ```UserAddressRepository.updateAddresses/deleteAddresses```: functions for event handling.

### Bug Fixes

- Fixed UserAddressRepositoryImpl to fetch addresses if DB table if empty.

### API Changes
  
- Removed Dagger Provides for ```AccountManagerDatabase``` from **AccountManager Dagger** module.
- Client need to provide all Database components:
```
@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AccountManagerDatabase =
        AccountManagerDatabase.buildDatabase(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseBindsModule {
    @Binds
    abstract fun provideAccountDatabase(db: AccountManagerDatabase): AccountDatabase

    @Binds
    abstract fun provideUserDatabase(db: AccountManagerDatabase): UserDatabase

    @Binds
    abstract fun provideAddressDatabase(db: AccountManagerDatabase): AddressDatabase

    @Binds
    abstract fun provideKeySaltDatabase(db: AccountManagerDatabase): KeySaltDatabase

    @Binds
    abstract fun providePublicAddressDatabase(db: AccountManagerDatabase): PublicAddressDatabase

    @Binds
    abstract fun provideHumanVerificationDatabase(db: AccountManagerDatabase): HumanVerificationDatabase

    @Binds
    abstract fun provideMailSettingsDatabase(db: AccountManagerDatabase): MailSettingsDatabase
}
```

### Recommendations

- You should **not use** ```AccountManagerDatabase``` anymore (could be deprecated in future).
- You should define your own Database, see CoreExample ```AppDatabase```, ```AppDatabaseMigrations``` and ```AppDatabaseBindsModule```.
