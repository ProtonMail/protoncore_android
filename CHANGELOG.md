# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The changelog is automatically generated from commits that adhere
to [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) spec.
See [README.md](README.md#allowed-commit-types) for commit types which are used to generate the changelog.
If needed, you can also manually update this file (provided the general structure is kept).

## [Unreleased]

## [25.2.0] - 2024-07-11

### Features

- Configurator - add quark user creation
- test:
  - distribute Android configurator app from post merge pipeline

### Bug Fixes

- account-manager:
  - Fix compose plurals for security keys settings item.

## [25.1.1] - 2024-07-09

### Bug Fixes

- plan: Fixed missing double % for Upgrade Storage translation.

## [25.1.0] - 2024-07-08

### Chores

- Remove binary compatibility validator.

### Features

- account-manager:
  - Add Observe security keys for the primary user.
  - Add "Security keys" item into the account settings screen.
- auth:
  - Enable local flag for FIDO2.

### Bug Fixes

- auth:
  - Moved Terms & Conditions and Privacy Policy to Confirm password screen.
  - Added config for max concurrent free user.
- user-settings:
  - When observing registered security keys, if FIDO2 flag is disabled, return an empty list.

### Internationalization

- Upgrade translations from crowdin (d6e61bcd).

## [25.0.2] - 2024-07-02

### Bug Fixes

- auth: CancelCreateAccountDialog must cancel CreateAccountNeeded Account only.
- plan: Don't raise any unredeemed flow for Credential-less user.
- test: Remove logcat cleanup before measure block.
- auth-presentation: Crash-fix ("Only fullscreen opaque activities can request orientation").
- human-verification: The dialog for re-sending email verification code would not show up.
- human-verification-presentation: Crash-fix ("Only fullscreen opaque activities can request orientation").

### Refactoring

- auth: Refactor to use SecondFactorProof instead of the duplicate SecondFactorFido.kt.

## [25.0.1] - 2024-06-24

### Chores

- presentation:
  - Add ic_proton_shield_2_bolt.
- sentry:
  - Updating package/tag prefixes.

### Bug Fixes

- country:
  - Fix country code for Dominican Rep.
- key:
  - The `internalOnly` parameter was not honored when calling `getPublicAddressInfo` method.
- payment-iap:
  - Added added logs to PerformGiapPurchaseImpl.
  - Added missing CheckoutGiapBillingAcknowledgeTotal metric.
  - Fixed GooglePurchaseStateHandler crashes (added logs).
- plan:
  - Fixed PurchaseStateHandler crashes (added logs).
- user-recovery:
  - Skip setting up user device recovery, if feature flag is disabled.

### Internationalization

- Upgrade translations from crowdin (bf5a84eb).

## [25.0.0] - 2024-06-18

### Breaking Changes

**Migrations**

- AccountDatabase.MIGRATION_8.
- UserSettingsDatabase.MIGRATION_7.
- PublicAddressDatabase.MIGRATION_3.
- EventMetadataDatabase.MIGRATION_3.
- New Entities: PublicAddressInfoEntity + PublicAddressKeyDataEntity.

**Features**

- key:
  - Store info about active public keys for a given email address.

### Chores

- auth:
  - Update error message when a password is too common.
- event-manager:
  - Extend event listener with notification when fetching events fails.

### Features

- test:
  - Implemented android client performance sdk with test examples.
- configurator:
  - Improved Configurator app (e.g. Retrofit + display FFs).
- event-manager:
  - Control fetching duration per event manager configuration.
- key:
  - Deprecate old `core/v4/keys` endpoint, and add a new one (`/core/v4/keys/all`).
- presentation:
  - Add two icons.
  - Add text style "hero".
  - Add ProtonThemeTv.
- user-settings:
  - Validate if password is too common, when changing password.

### Bug Fixes

- account-manager:
  - Fixed disableCredentialLessAccounts (filter Ready accounts).
- account-recovery:
  - Fixed recovery session consume endpoint version.
- auth:
  - Allow Product to show/hide Long Login Toast.
  - Allow Product to show/hide Congrats screen after successful Sign Up.
  - Fix IllegalState exception add account activity
- auth-presentation:
  - Fixed crash in `TermsConditionsDialogFragment`.
- plan-presentation:
  - Fixed Title in "Current subscription" view overlapping with the price.
- user:
  - Remove User getUsedPercentage `require(used <= max)` check.
- user-settings:
  - Changing password was not possible in VPN, if the user had two-password mode enabled.

### Internationalization

- Upgrade translations from crowdin (e96f506a).
- Upgrade translations from crowdin (39c70b27).
- Upgrade translations from crowdin (2ad2f908).

## [24.0.0] - 2024-05-15

### Breaking Changes

**Features**

- Added Device Recovery Handler.

  - If user has no recovery secret, create it and upload to BE.
  - If there is no recovery file, create it and store locally.
  
  MIGRATION:
  - Add `DeviceRecoveryDatabase.MIGRATION_0`.
  - Provide `DeviceRecoveryDatabase` via dagger.
  - Add `RecoveryFileEntity` to your `@Database.entities`.
- user-recovery:
  - Overwrite RecoveryFile if its key count different than expected.

    MIGRATION: DeviceRecoveryDatabase.MIGRATION_1

**Bug Fixes**

- user-recovery:
  - Remove RecoverySecret from DB.

    MIGRATION:
    - Add `UserKeyDatabase.MIGRATION_1`.

### Chores

- Add DB migrations for device recovery into CoreExample.

  DeviceRecoveryDatabase.MIGRATION_0
  UserKeyDatabase.MIGRATION_1
  DeviceRecoveryDatabase.MIGRATION_1

### Features

- user-recovery:
  - Show a notification if a user key has been recovered.

### Bug Fixes

- account-manager:
  - SessionForceLogout disable/lock the Account.
  - AccountManager remove/disable Account must not be cancelled by caller.
- user-recovery:
  - Verify recovery secret signature before generating recovery file.

## [23.2.0] - 2024-05-15

### Features

- coreexample:
  - Configure asset links for FIDO2.

### Bug Fixes

- auth:
  - Fix HV activity orientation.
  - Fix account state after human verification dismiss on login.
- proguard-rules:
  - Add missing proguard rules for `configuration` and `telemetry` modules.

### Internationalization

- Upgrade translations from crowdin (1e764907).

## [23.1.0] - 2024-05-13

### Features

- account-manager:
  - Add SignOutDialogActivity userid.

## [23.0.1] - 2024-05-07

### Bug Fixes

- plan:
  - Fixed Subscription Update error handling (400 status code).
- user-settings:
  - Unlock user if locked before changing password (e.g. Vpn).

### Internationalization

- Upgrade translations from crowdin (e585e157).

## [23.0.0] - 2024-04-30

### Breaking Changes

**Features**

- user-settings:
  - Add `UserSettingsEntity.sessionAccountRecovery` property.

    MIGRATION: UserSettingsDatabase.MIGRATION_6.

### Features

- Configuration module host selection improvements.
- account-manager:
  - Update UI for changing password.
- auth:
  - Added Cancel Create Account Dialog.
  - Add common pass check.
- user-settings:
  - Check if session account recovery is enabled, before displaying the password reset button.
  - Don't show "encryption password" tab in VPN.
  - Update UI for changing password.

### Bug Fixes

- plan:
  - Update UI of `UpgradeStorageInfo` to avoid truncated text.
  - Fixed Subscription Update error handling (e.g. network issue).

### Internationalization

- Upgrade translations from crowdin (9115a3a2).

## [22.2.0] - 2024-04-22

### Chores

- network:
  - Add a warning log if clientId is missing in `HumanVerificationNeededHandler`.
- payments-iap:
  - Remove check on buttonStates in onPayClicked

### Features

- crypto:
  - Added serializeKeys and deserializeKeys functions.
- gopenpgp:
  - Update to gopenpgp v2.8.0-alpha.1-proton.

### Bug Fixes

- auth:
  - Fallback to Device ChallengeFrame if no details frames exists.
  - Fix credless HV duplicate invocation.
- event-manager:
  - Set initial eventId after signup/login.
- user:
  - Removed Organization Keys support (deprecated).

### Internationalization

- Upgrade translations from crowdin (14570e36).

### Refactoring

- user-recovery:
  - Use serializeKeys and deserializeKeys functions.

## [22.1.0] - 2024-04-10

### Chores

- Fixed Account Recovery Presentation Compose dependencies.
- network:
  - Allow adding OKHttp interceptors through dependency injection
- user-recovery:
  - Disable Device Recovery feature.

### Features

- Configuration module improvements.
- account-manager:
  - Added SignOut Dialog.
- crypto:
  - Added encryptDataWithPassword/decryptDataWithPassword.
- key:
  - Add key reactivation.
- report:
  - Added attach log file functionality
- user:
  - Add key reactivation.
- user-recovery:
  - Added Device Recovery Notification & Dialog.
  - Added User Recovery modules.

    Added GetRecoveryFile/GetRecoveryPrivateKeys.
    Added GetRecoveryInactivePrivateKeys.
    Added DeviceRecoveryHandler/DeviceRecoveryHandlerInitializer.

### Bug Fixes

- Avoid build failures when running core as a local build

  When running mail project including core as a local build
  (by enabling `local.git.proton-libs=../proton-libs` in gradle.properties)
  the build fails as it can't resolve what `extensions` is.
  Casting BaseFlavor extension to ExtensionAware explicitly solves this.
- account-manager-presentation-compose:
  - Update text color of "Sign in" button in account settings info view.
- plan-presentation:
  - Fixed upgrade layout visibility race condition.
- send-preferences:
  - Force refreshing contact cards to define send preferences

    To ensure up-to-date sending preferences are used also immediately after
    they were changed from another client, we force refresh data that is not
    coming through the event loop and which is needed to define them.
    
    MAILANDR-1250

### Internationalization

- Upgrade translations from crowdin (b3f95c25).
- Upgrade translations from crowdin (ff392f83).

## [22.0.0] - 2024-03-28

### Breaking Changes

**Features**

- Added Purchase, Manager, Repository, StateHandlers and Workers.

  Migration: PaymentDatabase.MIGRATION_1.

### Chores

- Added Configurator application.
- feature-flag:
  - Group all call to WorkManager in FeatureFlagWorkerManager.

    Extract interface from FeatureFlagWorkerManager.
- injection:
  - Modules should be public to allow @TestInstallIn to replace them and avoid hilt to generate a wrapper.

    Modules with bindings can be interfaces.
- presentation:
  - Update color for ic_proton_servers* icons.

### Features

- Added CreateAccountNeeded handling.
- account-manager:
  - Added Account Settings Composables.
- account-recovery:
  - Enabled Account Recovery Reset support.
  - Added AccountRecovery Start/Reset metrics.
  - Added PasswordResetDialog.
  - Added AccountRecoveryInfo.
- user-settings:
  - Added PasswordManagement Account Recovery state handling.
- util-android-datetime:
  - Added DurationFormat and DateTimeFormat.

### Bug Fixes

- Update icons for support for Android 6.
- account-manager:
  - Disable CredentialLess accounts on a new Ready Account.
- account-recovery:
  - Added back ProtonDagger plugin.
  - Various fixes regarding Material 2/3 support.
- feature-flag:
  - Removed FeatureFlag metrics (except FeatureFlagAwaitTotal).
- network:
  - Pass `x-pm-payment-info-token` header if payment token is present.

    For now, the value of `x-pm-payment-info-token` is the same as `x-pm-human-verification-token`. However, using `x-pm-human-verification-token-type: payment` is deprecated, but `x-pm-payment-info-token` is not implemented in every endpoint we need.
- payment-iap:
  - In `GetStorePriceImpl`, default to `null` if billing client has failed.
- plan:
  - Do not call `getDynamicSubscriptions` if user is credential-less.
- presentation-compose:
  - Add padding for the second row of `ProtonSettingsItem`.
- user-settings:
  - Added Password/Recovery success Toasts.

### Internationalization

- Upgrade translations from crowdin (1b7e5389).
- Upgrade translations from crowdin (1e272664).

### Refactoring

- Deprecated RememberFlow extensions.
- account-manager:
  - Renamed AccountSettingsViewModel to AccountSettingsInfoViewModel.
- account-recovery:
  - Replaced Clock/DateTime/Duration usages by Proton Util Android DataTime module.
- user-settings:
  - Refactored PasswordManagement Fragment/ViewModel.
- util-kotlin:
  - Deprecate `Map.filterNullValues` function and add `Map.filterNotNullValues` instead.

    The naming `filterNullValues` is not consistent with Kotlin's naming convention, where `filterNotNull` will remove the `null` values from a list.

## [21.2.0] - 2024-03-14

### Chores

- observability:
  - Add new label value `GiapStatus.networkError`.
- payment:
  - Upgrade Google Play Billing library from 5.1.0 to 6.2.0.
- presentation:
  - Update ic_proton_servers* icons.
  - Update flags for Mauritania and Bangladesh.

### Features

- Added test rules package.
- account-recovery:
  - Adapt password reset available dialog.
- key:
  - Add reset user password.
- plan:
  - Fix unknown locale currency crash.
- user-settings:
  - Add reset user password.

### Bug Fixes

- Fix config manager not returning null on empty config.
- auth:
  - Pass sessionId of credential-less user when creating a regular account.
- payment:
  - Do not use credential-less userId for payments.
- plan:
  - Use dynamic-plans endpoint for unredeemed Google purchases.
  - Do not use credential-less userId for payments.
- telemetry:
  - Fix FK exception for TelemetryManager enqueue.
- user:
  - Avoid calling some API endpoints for credential-less users.
  - Pass sessionId of credential-less user when creating a regular account.
- user-settings:
  - Avoid calling some API endpoints for credential-less users.

### Internationalization

- Upgrade translations from crowdin (59063970).
- Upgrade translations from crowdin (985d1cdd).

### Refactoring

- user:
  - Add local and remote data sources for user repository.

### Theming

- Make base typography styles public.

  This allows using the same style naming as in designs.

## [21.1.0] - 2024-03-06

### Features

- account-recovery:
  - Added IsAccountRecoveryResetEnabled FF.
- feature-flags:
  - Allow custom context for flags.

    A FeatureFlagContextProvider is added that can be used by clients to
    provide custom context properties when fetching feature flags.

### Bug Fixes

- network:
  - Fixed Missing Scope error codes (scopes: locked/password).

## [21.0.0] - 2024-02-28

### Library Upgrades

- Kotlin 1.9.22.
- Kotlin Coroutine 1.8.0 (1.7.3).
- Kotlin Serialization 1.6.3.
- Android Target API 34.
- Android Gradle Plugin 8.2.2.
- Compose 1.6.2 (compiler 1.5.10) -> BOM 2024.02.01.
- Dagger 2.49.
- Hilt 1.2.0 (KSP) -> Hilt Navigation Compose 1.2.0.
- Room 2.6.1.
- Work 2.8.1.

### Chores

- Updated Paparazzi snapshots.
- plan: Enable local flag for storage split.

### Features

- account-manager: Add product metrics to settings signup/signin view.
- auth: Setup product metrics for each activity/fragment that needs them.
- crypto: Add API to encrypt a pgp message to an addtional key.
- gopenpgp: Update to gopenpgp v2.7.5-proton.
- presentation: Added fallback to openMarketLink (open in browser).
- telemetry: Setup product metrics for each activity/fragment that needs them.

### Bug Fixes

- AccountSettingsInfo fixes, refactor and adaptations.
- presentation-compose: hiltViewModelOrNull now use HasDefaultViewModelProviderFactory.

### Internationalization

- Upgrade translations from crowdin (5e52a7a8).

### Refactoring

- account-manager-presentation-compose: Added AccountSettingsInfo state function.
- telemetry: Aligning ProductMetrics functions parameters name.

## [20.0.0] - 2024-02-22

### Breaking Changes

**Features**

- Added CredentialLess Support.

  DB Migrations:
  - UserDatabase.MIGRATION_5: Added User.type.
  - AccountDatabase.MIGRATION_7: Account.username nullable.

### Chores

- event-manager:
  - Update endpoint path for getting events to v5.
- user:
  - Add user service and subscription check for Drive.

### Features

- account-manager:
  - Add settings signup/signin view.
- auth-presentation:
  - Add product metrics for "Credential-less Add Account" screen.
- payment:
  - Add support for payments for credential-less users.

### Bug Fixes

- plan:
  - Fix get more storage button when payments are not available.
  - Fix storage split text UI and OptionalViewModel.kt for Mail.
- plan-presentation:
  - Remove left-padding from the title in plan entitlement view.

## [19.1.0] - 2024-02-08

### Chores

- plan:
  - Disable storage-split feature flag.

    Enable it only for CoreExample app.

### Features

- Added Custom Tabs support for LoginSso Activity.
- auth:
  - Wait for the feature flag before picking which Welcome screen should be displayed.
  - EA signup with payment details.
- auth-presentation:
  - Show the new Welcome screen, if credential-less flag is enabled.
  - Add fragment for credential-less.
- event-manager:
  - Added App Standby Buckets support.

    The idea is to refresh the events enough often compare to the usage, and the limitation in time and jobs in a window.
    For example if the app is only used two or three times a week, refresh the events each 8 hours is fairly enough.
    So to get the background interval we choose to refresh the events once per window.
    
    | bucket      | window | minutes | jobs limitation | bg interval | user usage
    -----------------------------------------------------------------------------
    | active      | 10min  | N/A     | 75              | 30min       | used
    | working set | 2h     | 10      | 120             | 2h          | daily
    | frequent    | 8h     | 10      | 200             | 8h          | weekly
    | rare        | 24h    | 10      | 48              | 24h         | rarely
    | restricted  | 24h    | 1       | 1               | 72h         | -
- observability:
  - Added Feature Flag Metrics.
- plan:
  - Add dynamic storage title and icon to current subscription.
  - View for displaying a warning, when storage usage is high.
- presentation-compose:
  - Add `BoxWithNotificationDot` component.

### Bug Fixes

- auth:
  - Changed CredentialLess FF to a Kill-Switch (CredentialLessDisabled).
- plugins-coverage:
  - Exclude classes that end with `$DefaultImpls`.

    Those classes are generated by Kotlin, e.g. when an interface method uses default values.

### Internationalization

- Upgrade translations from crowdin (727a2125).

### Refactoring

- Move IsCredentialLessEnabled from account-manager to auth.
- auth-presentation:
  - Extract current functionality of `AddAccountActivity` into `AddAccountFragment`.
- plan-presentation:
  - Additional parameters for `UpgradeStorageInfo` view.
- user:
  - Methods to get used percentage of base (Mail), Drive and total space.

    `User.getUsedBaseSpacePercentage`, `User.getUsedDriveSpacePercentage`, `User.getUsedTotalSpacePercentage`.

## [19.0.0] - 2024-01-29

### Breaking Changes

**Features**

- Add new fields into `User` (for storage split).

  MIGRATION: UserDatabase.MIGRATION_4

### Chores

- Updated Copyright Template for 2024.
- observability:
  - All Core observability metrics inherit from `CoreObservabilityData`.
- payment:
  - Increase unit test coverage.
- plan:
  - Refactor some existing feature flags to use `IsFeatureFlagEnabledImpl`.

### Features

- account-manager:
  - Add feature flag for credential-less.
- crypto:
  - Add methods for encrypting and decrypting text with a password.
  - Added Base64Encoded typealias.
  - Added generateNewToken extension.
- feature-flag:
  - Add generic class for checking local and remote feature flag values.
- key:
  - Add recovery secret and recovery secret signature.
- observability-tools:
  - Add "base class" argument for JSON generator.
- plan:
  - For Free users, show a warning banner if storage is (nearly) full.
  - Add feature flag for `SplitStorage`.
  - Update colors of storage entitlement, if storage is nearly full.
  - Update `DynamicEntitlementProgressView` to support additional tag text for storage split.
- user:
  - Add recovery secret and recovery secret signature.
- user-data:
  - Add support for storage split in `UserSpaceEventListener`.
- user-settings:
  - Added Set Recovery Secret (+Worker).
  - Add device recovery user settings and FF.
- util-kotlin:
  - Added basic Retry extension.

### Bug Fixes

- country:
  - MA flag color.
- payment-iap:
  - Added Billing functions basic retry.
- plan:
  - Fix plan UI tests and fix CanUpgradeToPaid conditions.

### Internationalization

- Upgrade translations from crowdin (8a2b1b43).
- Upgrade translations from crowdin (cf07a5a6).
- Upgrade translations from crowdin (99a2a2e6).
- Upgrade translations from crowdin (cde18282).

## [18.4.2] - 2023-12-19

### Bug Fixes

- event-manager:
  - Removed confusing onSuccess/onFailure logs.
  - Removed Mutex for EventWorkerManager enqueue/cancel (previously introduced).
  - Await cancel/enqueue Operation.
  - Fixed potential unneeded duplicate enqueue.
- human-verification:
  - Fix loading email verification page under DoH.
- label:
  - Fixed parsing issue for null Label path (assuming root folder).
- telemetry:
  - Added Telemetry Metrics Priority.

## [18.4.1] - 2023-12-14

### Bug Fixes

- event-manager:
  - Prevent concurrent use of EventWorkerManager enqueue/cancel.
  - Fixed null QueryMap if not provided.

## [18.4.0] - 2023-12-13

### Features

- event-manager:
  - Allow passing query params to event loop request

    As per API docs, the "Unread counters" for messages and conversations
    are not returned by default but must be explicitly requested (historical
    performance reasons on the backend).
    This allows passing optional query params to set them
    
    MAILANDR-1093

## [18.3.0] - 2023-12-13

### Features

- payment:
  - Add observability metrics for the payment button.
  - Remove activity reference from GoogleBillingRepository.
  - Add `ProtonPaymentButton`.
  - Add use cases for performing a full GIAP flow.
- plan:
  - Added Giap Billing Product Query OE.
  - Integrate ProtonPaymentButton.
  - Add support for GIAP prices in Dynamic Subscription.
  - Set GIAP preferred payment provider.
  - Rebase ProtonPaymentButton.
  - Add GIAP Adjusted Prices support.

### Bug Fixes

- payment:
  - After GIAP is billed successfully, keep the `Loading` state of the button.

    For any other event, set all buttons to `Idle`.
- plan:
  - Fix error message when Play Store prices are unavailable.
  - Remove Subscription price and currency if not found for Google managed.

## [18.2.4] - 2023-12-13

### Bug Fixes

- plan:
  - Finish the `UnredeemedPurchaseActivity` after clicking the Cancel button.

## [18.2.3] - 2023-12-11

### Bug Fixes

- feature-flag:
  - Wait Unleash FeatureFlags initialization if not completed.

## [18.2.2] - 2023-12-08

### Chores

- human-verification:
  - Remove unused layouts and strings for HV2.
- network:
  - Upgraded OkHttp to 4.12.0 (Okio 3.6.0).

### Bug Fixes

- auth:
  - Confirm password tests fix.
  - Use proper measurement group for product metrics.
- challenge:
  - Pass immutable copies to ChallengeFrameDetails.
- data:
  - Added Read or Write Mutex for AndroidFileContext.
- event-manager:
  - Added Info Logs for EventManagerImpl.
  - Improve accuracy for immediateMinimumInitialDelay.
  - Don't enqueue a new EventWorker if already enqueued and not an immediate request.
  - Added requiresBatteryNotLow/requiresStorageNotLow configuration for background EventWorker.
- human-verification:
  - Use proper measurement group for product metrics.
- network:
  - Added HTTP 421 as Retryable.

### Internationalization

- Upgrade translations from crowdin (2d0a4dfa).
- user-settings:
  - Use "email address" phrase instead of just "email".

## [18.2.1] - 2023-11-22

### Bug Fixes

- user-settings:
  - Added UserSettingsViewModel Primary userId support.

## [18.2.0] - 2023-11-21

### Chores

- auth: Increase coverage for auth module.
- presentation: Fix password match validation.
- push: Update push route.

### Features

- payment-iap:
  - Make sure query and purchase start states are consumed.

    Because state flow won't yield on emit e.g. QueryingProductDetails won't be
    consumed if error state is set before producer coroutine yields. This breaks
    "try again" scenarios that wait for error to be re-emitted but instead consumer
    doesn't get any state update as error state is preserved through the whole
    process.

### Bug Fixes

- auth: Add the product dimensions defined by `ProductMetricsDelegate`.

### Internationalization

- Upgrade translations from crowdin (e62b2060).

## [18.1.1] - 2023-11-14

### Bug Fixes

- event-manager: Properly cancel any previous corresponding config EventWorker.

## [18.1.0] - 2023-11-10

### Features

- auth:
  - Added External SSO Remote Feature Flag.
- auth-presentation:
  - Add telemetry for signup-recovery screen.
- telemetry:
  - Add Signup pass screen metrics.

### Bug Fixes

- auth:
  - Fixed SSO Login flow.
- event-manager:
  - Added configurable EventManager Worker immediate minimum initial delay.
  - Added Mutex on EventManagerProvider get.
- plan:
  - Change `plans_pay_other` string to plural.

## [18.0.0] - 2023-11-08

### Breaking Changes

**Bug Fixes**

- event-manager:
  - EventMetadataDatabase migration to force fetch and persist EventsResponse.

    MIGRATION: EventMetadataDatabase.MIGRATION_2.

### Chores

- auth:
  - Display info-level toast if SSO sign-in is required.
- presentation:
  - Add `Context.normToast` extension for displaying info-level toasts.

### Features

- gopenpgp:
  - Update to gopenpgp v2.7.4-proton.
- telemetry:
  - Add WelcomeScreen, LoginScreen and SignUp metrics.

### Bug Fixes

- data:
  - Added UniqueId to generate FileContext Directory/Filename.
- event-manager:
  - Added UniqueId to generate FileContext Directory/Filename.
  - Splitted EventMetadata and EventsResponse.
- human-verification:
  - Changed Human Verification errors log level to info.

### Internationalization

- Upgrade translations from crowdin (10c2cadc).

## [17.3.0] - 2023-11-07

### Features

- account-recovery:
  - Added Remote Feature Flag for Account Recovery.
- event-manager:
  - Added Remote Feature Flag for Core EventManager.
- notification:
  - Added Remote Feature Flag for Notification.
- payment-iap:
  - Support multiple cycles for GIAP.

### Bug Fixes

- network:
  - Update server time only on successful response.

### Refactoring

- Removed CoreExample Deprecated FeatureFlag.

## [17.2.1] - 2023-10-31

### Bug Fixes

- observability:
  - Update Observability Metrics version.

## [17.2.0] - 2023-10-30

### Features

- feature-flag:
  - Improved Unleash FeatureFlag refresh strategy.
- notification:
  - Added Permission Request Show Rationale config.
- observability:
  - Added http1xx/http3xx/cancellation ApiStatus and Log unknown.
- presentation:
  - Added Custom Snackbar Duration (short: 2.5s, long: 5s).
- telemetry:
  - Add WelcomeScreen and LoginScreen metrics.
  - Add view and screen metrics api.

### Bug Fixes

- account-manager:
  - Prevent refresh if session changed in the meantime.
- notification:
  - Cancel ObservePushNotifications on Account not Ready.

### Internationalization

- Upgrade translations from crowdin (c1a757f2).

### Reverted Changes

- "feat(telemetry): Add WelcomeScreen and LoginScreen metrics."

  This reverts commit d75d59b9ac16890b4b7f3a4637713a0aadbb485e.

## [17.1.0] - 2023-10-23

### Chores

- payment-iap:
  - Report to Sentry response code + message on BillingClientError.
  - BillingClientError sentry logs updated with response code+message.

### Features

- feature-flag:
  - Added Unleash FeatureFlag persistence.
- payment-iap:
  - Allow for simplified upgrade flow for Google IAP.
- plan:
  - Allow for simplified upgrade flow for Google IAP.

### Bug Fixes

- plan:
  - Crash when observing currency, if country is not specified in default Locale.

## [17.0.3] - 2023-10-12

### Chores

- Update runners to k8 runners.
- observability:
  - Additional status labels for the "login after sigup" metric.

### Bug Fixes

- plan:
  - Fixed Dynamic Plan List filtering (free OR [matching cycle + matching cycle]).
- util-android-sentry:
  - Properly decorate the crash events with a custom tag.

### Internationalization

- Upgrade translations from crowdin (9ee29f10).
- plan:
  - Add numbered parameters for `plans_used_space`.

## [17.0.2] - 2023-10-04

### Bug Fixes

- Fixed Dynamic Upgrade Plan Fragment (orientation changed).
- Fixed Dynamic Entitlement Icon Url with double slash.

## [17.0.1] - 2023-10-03

### Bug Fixes

- plan: On GetDynamicPlans error, fallback to empty Dynamic Plan Selection cycles and currencies.

## [17.0.0] - 2023-09-29

### Breaking Changes

**Features**

- network:
  - Add AppClock implementation which uses time synchronized with API.

### Bug Fixes

- plan:
  - Fixed Dynamic Plan metrics.

## [16.1.0] - 2023-09-28

### Features

- Configuration module improvements.
- notification:
  - Added Notification Permission Request Enabled config.
- plan:
  - Enabled Dynamic Plan local Feature Flag.
  - Added Dynamic Cycle/Currency selection, Vendor Query param and DefaultCycle.

### Bug Fixes

- notification:
  - Add `FLAG_ACTIVITY_SINGLE_TOP` flag when launching the app from a notification.
- plan:
  - Keep Dynamic Upgrade Layout invisible until all constraints are known.
  - Increased Dynamic Decoration Storage Bar thickness.
  - Changed Dynamic Entitlement to 2-lines & ellipsize end.
  - Added Dynamic Decoration Badge PlanId handling.
  - Changed Dynamic Decoration Badge Background.

### Internationalization

- Upgrade translations from crowdin (441bf239).

## [16.0.0] - 2023-09-14

### Breaking Changes

**Features**

- telemetry:
  - Added Telemetry modules.

    Migrations: TelemetryDatabase.MIGRATION_0 and UserSettingsDatabase.MIGRATION_3.

**Refactoring**

- Removed `Logger.log` function.
- user:
  - Removed deprecated User API.

### Chores

- Added environment config gradle plugin
- notification:
  - Update API responses.

### Features

- Added configuration module.
- account-recovery:
  - Update the UI of account recovery dialogs.
- notification:
  - Handler for deeplinks for apps where there are multiple activities involved during startup.
  - Improvements for notification views.

    - Enable multi-line body text in notifications.
    - Correct notification time - `notification.time` is in seconds, and `setWhen` method expects ms.
- plan:
  - Added Dynamic Badge Decoration.

### Bug Fixes

- account-recovery:
  - Launch `AccountRecoveryDialogActivity` in a separate task.

    When this activity finishes, remove the task so it's no longer visible
    in the "Recent Apps" screen.
- mail-send-preferences:
  - Fix extractPinnedPublicKeys() in CryptoUtilsImpl.

    Data was passed incorrectly as raw bytes, instead of Armored.
- notification:
  - Delete a notification once it's been consumed.
- plan:
  - Fixed Dynamic Plan Promo Percentage.
  - Removed Dynamic Plan Offer.
  - Removed Dynamic Plan filtering (hasServiceFor).
  - Show Currency for Free Dynamic Plan or Subscription.
  - Improved Dynamic Upgrade Plan Layout visibility.
  - Fixed Dynamic Instance/Price ID.

### Internationalization

- Upgrade translations from crowdin (f1cf3a63).

### Refactoring

- plan:
  - Extracted ObserveUserId/ObserveUserCurrency from DynamicPlanSelectionViewModel.

## [15.1.0] - 2023-09-01

### Chores

- payment-iap:
  - Add sentry log for unknown giap error/throwable status.

### Features

- coverage:
  - Add commonRules.kt
  - Update coverage rules and introduce new ExcludeFromCoverage annotation.

    .
- feature-flag:
  - Added FeatureFlagRefreshStarter to use with FeatureFlagInitializer.
- gopenpgp:
  - Update to gopenpgp v2.7.3-proton and reset key transparency to v0.0.1.
- plan:
  - Added Dynamic Plan Minimal UI tests.
  - Integrated Dynamic Activities into PlansOrchestrator Workflows.

### Bug Fixes

- plan:
  - Fixed DynamicEntitlementDescriptionView ImageLoader.
  - Fixed Dynamic Decoration Starred.
  - Fixed DynamicPlanInstanceResource Vendor mapping.
  - Fixed DynamicPlanResource enums mapping.
  - Fixed Dynamic Subscription management info text.

## [15.0.0] - 2023-08-23

### Breaking Changes

**Features**

- user:
  - Added User creation time.

    Migrations: UserDatabase.MIGRATION_3, AccountDatabase.MIGRATION_6.

### Chores

- observability:
  - Enqueue observability data when showing the (dynamic) plan selection screen.
  - Add proguard/r8 rule to keep the `SchemaId` annotation.
- plan:
  - Enqueue observability data when showing the (dynamic) plan selection screen.

### Features

- payment:
  - Add initial observability metrics.
  - Added Dynamic Plan/Subscription parsing error logs.
- plan:
  - Add initial observability metrics.
  - Add fragment for displaying plans during signup.
  - Added Dynamic Plan/Subscription parsing error logs.
  - Added Dynamic Upgrade Plan Activity/Fragment/ViewModel.
- util-android-device:
  - Add Sentry local flag.
  - Add Sentry events metadata.
- util-android-sentry:
  - Add Sentry local flag.
  - Add Sentry events metadata.
  - Sentry for Account team.

### Bug Fixes

- key-transparency:
  - Normalize email domain for verification.
- payment:
  - Updated Dynamic Plan/Subscription according latest API.
- plan:
  - Hide Dynamic Upgrade when no Payment available.
  - Updated Dynamic Plan/Subscription according latest API.
  - Fixed Dynamic Fragments when no Plan available.
- proguard-rules:
  - Add newest proguard rules for retrofit.

### Internationalization

- Upgrade translations from crowdin (1285c829).

## [14.1.0] - 2023-08-15

### Features

- feature-flag:
  - Added Unleash Toggles.
- gopenpgp:
  - Update to gopenpgp v2.7.2-proton.
- mail-send-preferences:
  - Add mail-send-preferences module and ObtainSendPreferences use case.
- payment:
  - Add DynamicSubscription.
- plan:
  - Added DynamicUser (None, Primary, ByUserId).
  - Added Dynamic Plan Selection Fragment.
  - Added Dynamic Plan List Fragment.
  - Added Dynamic Subscription Fragment/ViewModel.
  - Add DynamicSubscription.
  - Add `GetDynamicPlans` use case and related data classes.
  - Add view model for showing dynamic plans.
  - Add `GetDynamicPlans` use case and related data classes.
  - Added common Dynamic Plan Views.
  - Add support for additional plans. Default current plan layout.
  - Add domain classes for plans and payments v5.
  - Added Dynamic Plan Feature Flag.

### Bug Fixes

- observability:
  - Fix ObservabilityWorkerManagerImpl enqueue policy.

### Internationalization

- Upgrade translations from crowdin (3b6d57cf).
- Upgrade translations from crowdin (9f2d1cda).

### Refactoring

- plan:
  - Extract `PlanDuration` and `PlanVendorData` to separate files.

## [14.0.1] - 2023-07-25

### Refactoring

- Moved LogTag to domain layer.

## [14.0.0] - 2023-07-21

### Chores

- Add default file type drawable.

  Added the latest version of the default file type icon used in mail android v6 for attachments
- data:
  - Added FileContext/AndroidFileContext.
- observability:
  - Log a case when `customerId` is empty.
  - Additional label values for creating payment tokens.
  - Additional label values for username availability metrics.
- payment-iap:
  - Log a case when `customerId` is empty.

### Features

- contact:
  - Add "isProton" field to ContactEmail.
- key:
  - Decrypt and returns unverified key.
- pow:
  - Add pow challenge solver duration for backend.
  - Fix pow issue on login with unauth session.
- util:
  - Add Core Sentry integration.

### Bug Fixes

- crypto-android:
  - Added retryOnce mechanism for Android KeyStore Key generation.
- event-manager:
  - Don't enqueue on EventMetadata update failure (e.g. SQLiteConstraintException).
- presentation-compose:
  - Fix Material 3 coloring as incorrect scrim was used as default.

### Internationalization

- Upgrade translations from crowdin (740ccb4d).

### Refactoring

- auth:
  - Migrate sign-up metrics to Result API.
  - Migrated SignUp metrics to Result API.
  - Migrate sign-up metrics to Result API.
- observability:
  - Removed obsolete runWithObservability extension.
- payment:
  - Migrated Checkout Payment Methods Metrics to Result API.
  - Migrated Checkout Subscribe and Validate Metrics to Result API.
  - Migrated Checkout Acknowledge Purchase Metrics to Result API.
  - Migrated Checkout Launch Billing Metrics to Result API.
  - Migrated Checkout Query Product Metrics to Result API.
  - Migrated Checkout Query Purchases Metrics to Result API.
  - Migrated Checkout Create Payment Token Metrics to Result API.
- user:
  - Migrate sign-up metrics to Result API.

## [13.0.1] - 2023-07-10

### Chores

- Added Grafana Dashboards tools.
- observability:
  - ObservabilityEvent set data to JsonElement.
  - Update grafana checkout availability dashboards formula.
- user:
  - allow to configure refreshKeySalts on UserManager.unlock

### Bug Fixes

- plan:
  - Make offer endtime nullable.
- user:
  - Avoid address creation for non private sub users.

## [13.0.0] - 2023-07-04

### Chores

- Upgraded Kotlin to 1.8.10 (+Dagger 2.46.1, +Compose to 1.4.3, +AppCompat 1.6.1, +Lifecycle 2.6.1, +Robolectric 4.10.3).
- Upgraded Gradle to 8.0.2.

### Bug Fixes

- detekt:
  - Removed not existing import (net.rubygrapefruit.platform.file.FilePermissionException).
- payment:
  - Added Payment Activities in AndroidManifest.

## [12.0.0] - 2023-07-03

### Chores

- Minor quark comamnd fixes.
- Added Recovery Grafana Dashboard.
- crypto-android:
  - Make `SrpCrypto` safe to call from main thread (and avoid ANR).
- observability:
  - Additional label values for account creation.
  - Add account type for signup observability metrics.

### Features

- account-recovery:
  - Implement observability metrics.
  - Add observability metrics.
  - Ask the user for password, before cancelling account recovery attempt.
- event-manager:
  - Added configurable EventWorkerManager repeat/backoff intervals.
- notification:
  - Integrated DeeplinkManager into Notification modules.
  - Added DeeplinkManager, DeeplinkIntentProvider and DeeplinkBroadcastReceiver.
  - Observe Push objects to display notifications.
  - Added Dagger CoreNotificationModule.
  - Updated Notification Response/Payload structure (NotificationDatabase.MIGRATION_1).
  - Added NotificationEventListener.
  - Add initial notification data module.
  - Add notification modules, domain entity and interfaces.

### Bug Fixes

- auth-presentation:
  - Changed Create Address Subtitle string.
- observability:
  - Events serialization improvements.

### Internationalization

- Upgrade translations from crowdin (c3fb9362).

### Refactoring

- SessionManager now hold a Mutex for any session write (centralized).

  Session is now separated into Authenticated and Unauthenticated.
- Account Recovery (use UserRecovery.State, use NavHost).
- account-recovery:
  - Move classes related to notifications from `account-recovery` to `notification` module.
- notification:
  - Move classes related to notifications from `account-recovery` to `notification` module.

## [11.0.0] - 2023-06-19

### Chores

- QuarkCommand improvements
- Upgraded Kover to 0.7.1.
- Added ObservabilityContext and onResultEnqueue functions.
- Update grafana dashboards.

### Features

- account-recovery:
  - Add recovery state observer.
  - Cancelling account recovery attempt.
  - Add Notification unit tests.
  - Add Notification unit tests.
  - Notifications for account recovery.
  - Account Recovery popup and tests added.
  - Set up notification channel for account recovery.
- auth:
  - Added SSO UI Tests.
  - Added SSO IdentityProvider Metrics.
  - Added SignIn with SSO WebView handling.

### Bug Fixes

- auth:
  - Make SessionInfo ServerProof optional (SSO flow).
- presentation:
  - Added Cookie option to ProtonWebViewActivity.
  - Added shouldOverrideUrlLoading in ProtonWebViewActivity to intercept loading/redirect.

### Internationalization

- Upgrade translations from crowdin (b4741134).
- Upgrade translations from crowdin (d2c6b526).

### Refactoring

- Added BaseRetrofitApi scope for `Api.safeCall`.

  fixup! feat(auth): Added SignIn with SSO WebView handling.
- Added ProtonWebView Client/Activity.

## [10.7.1] - 2023-05-31

### Chores

- plan:
  - Update plan misconfig report message.

## [10.7.0] - 2023-05-30

**Note:** do not consume this release.

### Chores

- Upgraded Kover to 0.7.0.
- Fix gradle build cache on ci.
- observability:
  - Update checkout grafana.
  - Update GIAP status enum.
- presentation:
  - Add new icons.

### Features

- auth:
  - Added SignIn with SSO handling.
  - Added MinimalSignInExternalTests.
  - Added SSO Local Feature Flag (R.bool.core_feature_auth_sso_enabled).
  - Added New SSO Login Activity.
  - Update login screen.
    - Add SSO button (VPN-only).
    - Display Help button in top-right corner.
- crypto:
  - Add API to add context on embedded signatures.
  - Add verification context for SKLs.
- plan:
  - Add offers and promotions support.
- user:
  - Add signature context for new SKLs.
- util-kotlin:
  - Added ResultCoroutineContext.

### Bug Fixes

- key-transparency:
  - strip plus alias from email address.
- payment-iap:
  - Prevent crash when Google Billing library returns a `null` response code.
- plan:
  - Defending a plans misconfiguration.
  - Add family plan support.
  - Unknown plans fix.
  - Update plan mapping.

## [10.6.0] - 2023-05-18

**Note:** do not consume this release.

### Chores

- presentation:
  - Add new icons.

### Features

- crypto:
  - Add API to add context on embedded signatures.
- key:
  - Add API to add context on embedded signatures.
- key-transparency:
  - Add verification context for SKLs.
- observability:
  - Add GetSubscriptions event.
- user:
  - Add signature context for new SKLs.
- util-kotlin:
  - Added ResultCoroutineContext.

### Bug Fixes

- key-transparency:
  - strip plus alias from email address.

## [10.5.0] - 2023-05-03

**Note:** do not consume this release.

### Chores

- observability:
  - Increment schema version metrics that use `HttpApiStatus`.
- presentation:
  - Add filled icons for cog wheel and earth.

### Features

- crypto:
  - Add BadContext value to VerificationStatus.
- gopenpgp:
  - Update to gopenpgp v2.7.1-proton.
- key-transparency:
  - Add backend feature flag for KT.
  - Use feature flag via provider.
- plan:
  - Fix info text visibility conditions.
  - Added Plan Mapping & Pass Plus support.

### Bug Fixes

- auth:
  - Improved EA to PA Conversion screen for edge cases.

    When initial setup/fetch fail -> Added Snackbar with error message and retry Button.
    When Username is already set -> Allow the postLogin process to continue.
    Use Authenticated Session for public endpoints (getDomains, checkUsername).
    Added Username input error help text ("This field is required.").
    Improved Inputs & Buttons state handling.
- key-transparency:
  - Enforce that the SKL creation time increases.

    We need to save the last creation time checked in the verified epoch.
    And we need to verify that the SKL creation times increase
    monotonically.
- observability:
  - Additional status label for HTTP requests: `notConnected`.
  - Updated HV PageLoad status (added: 400, 404, 422).
  - Add new label values for account creation metric.
- presentation-compose:
  - Don't apply Material3 tint to elements with tonal elevation (e.g. bottom bar).

### Internationalization

- Upgrade translations from crowdin (04696da1).

### Theming

- Fixed wrong style for ProtonButton.

  Applying `style="@style/ProtonButton"` prevent to override globally in ProtonTheme level.

## [10.4.0] - 2023-04-18

### Features

- crypto:
  - Add SRP challenge api for device verification.
- human-verification:
  - Add proof of work to the core library.

### Bug Fixes

- feature-flags:
  - Fully disable in memory caching for feature flags

    MAILAND-3071
  - Clear store cache when updating feature flag value.

    This ensures when the value of a feature flag is updated and then
    immediately queried by the client, the updated value is returned over
    the previous, outdated one that store keeps in its memory cache.
    
    MAILAND-3071

## [10.3.0] - 2023-04-13

### Features

- account-manager:
  - Migrate DB for key transparency.
- coreexample:
  - Integrate self audit in example app.
- crypto:
  - Add getCurrentTime() function.
- gopenpgp:
  - Add key transparency lib to golang build.
- key:
  - Verify public addresses in key transparency.
  - Add key transparency values to model.
- key-transparency:
  - Implement PublicAddressVerifier.

    The PublicAddressVerifier is used by the key module to
    verify the public addresses fetched from the API.
  - Implement SignedKeyListChangeListener.
  - Implement self audit logic.
- user:
  - Add SignedKeyListChangeListener for signup.

    During signup, the app generates new address keys and
    a new signed key list. To check that the KT state is correct
    at that time, the user manager will call the
    SignedKeyListChangeListener.
  - Add key transparency values to model.

### Bug Fixes

- presentation-compose:
  - Fix accent colors not updating with theme change.

## [10.2.0] - 2023-04-12

### Chores

- Added Cache4k (in-memory key-value, time-based/size-based evictions).
- Bump Android Gradle Plugin to 7.4.2.
- Update links from `protonmail.com` to `proton.me`.
- auth:
  - Add Proton Pass theming and welcome activity UI.
  - In VPN, during signup, bring back the ability to switch to Internal account.
- presentation:
  - Add Proton Pass theming and welcome activity UI.

### Features

- event-manager:
  - Extend EventListener with getEventMetadata.

    This allows specialized event listeners to get event metadata before processing certain event.
    For example onResetAll does not carry information if refresh was requested from BE or is it because of internal event manager state.
- feature-flag:
  - Update value defaults to "global" user id when null.

    MAILANDR-3071
  - Rollback feature flag only changes the value.

    MAILAND-3071
  - Rollback local feature flag value when update request fails.

    MAILAND-3071
  - Feature flag manager can update feature flag values.

    MAILAND-3071

### Bug Fixes

- Fixes bottom sheet corner radius to align with figma designs.
- auth:
  - Automatically switch from EA to PA on Proton domain.
- event-manager:
  - Drive events should use Refresh and More from api.
- feature-flag:
  - Move worker related op to remote data source.

    - Update api definition.
    
    MAILAND-3071
- presentation-compose:
  - Add textAccent and iconAccent colors.
  - Add typography without color.

    Add ProtonTypography styles with unspecified color. For each style the
    pattern is:
    1. A style with suffix 'Unspecified' is introduced. It doesn't specify
       any color.
    2. A style with suffix 'Norm' is introduced. It replaces the style with
       no suffix.
    3. Style with no suffix is deprecated, the *Norm style should be used
       instead.
    
    E.g. for the default style:
    1. defaultUnspecified is added.
    2. defaultNorm is added.
    3. default is deprecated but it still points to defaultNorm for
       backwards compatibility.

### Internationalization

- Upgrade translations from crowdin (5ed6e14a).

### Refactoring

- Split EventManagerConfig.Drive into share based and volume based.

  Drive can fetch events either through Shares or through Volumes, thus two different endpoints.

## [10.1.0] - 2023-04-04

### Chores

- auth:
  - Update Welcome Screen UI.
- domain:
  - Add Proton Pass product enum.
- presentation:
  - Update Welcome Screen UI.
- user-settings:
  - Clean up recovery screen texts.

### Features

- crypto:
  - Add API to add and verify signature contexts.

    Add the possibility to add a context when generating detached
    signatures.
    And add the possibility to enforce that a signature has a given
    context during verification.
- key:
  - Add API to add and verify signature contexts.

    Add the possibility to add a context when generating detached
    signatures.
    And add the possibility to enforce that a signature has a given
    context during verification.

### Bug Fixes

- auth:
  - Make Auth Activities Screen Orientation portrait.
  - Make Auth Activities scrollable (SoftInput).
- challenge:
  - Fix `IndexOutOfBoundsException` during auto-fill.
- presentation:
  - The hyperlink color same as text color.
  - Added Snackbar auto detect weblinks.
- report:
  - Added simplified ReportOrchestrator startBugReport function.

## [10.0.0] - 2023-03-29

### Chores

- auth:
  - Payments availability pre-check.
- payment:
  - Add unit tests for signup in context of paid plans.
  - Payments availability pre-check.
- plan:
  - Add mem cache into Plans repository.
- presentation:
  - Update splash icon for VPN.
  - Update splash icon for Drive.
  - Update splash icon for Calendar.
  - Update splash icon for Mail.
  - Remove adaptive app icons (unused).

## [9.19.0] - 2023-03-29

### Chores

- observability:
  - Update Grafana dashboards.
  - Add command to download dashboards from Grafana.

### Features

- network:
  - Make alt. cert verification method available for main route.

    Clients should normally use regular method but for some testing environments
    alt. routing method of cert verification will be used.

### Bug Fixes

- auth:
  - Changed Choose Address subtitle.

### Theming

- ProtonTextView.Subtitle lineHeight 20sp for API>=28.

## [9.18.0] - 2023-03-24

### Features

- gopenpgp:
  - Update to gopenpgp v2.6.1-proton and go-srp v0.0.7.
- user:
  - Added User event listener that handles event which carries updated UserSpace.

### Bug Fixes

- auth:
  - Wrong Congrats Fragment BackStack.
- observability:
  - Handle a case for `BillingResponseCode.FEATURE_NOT_SUPPORTED`.
  - Log a case when there is no matching Google product.
- payment-iap:
  - Handle a case for `BillingResponseCode.FEATURE_NOT_SUPPORTED`.
  - Log a case when there is no matching Google product.

## [9.17.1] - 2023-03-22

### Chores

- payment-iap:
  - Add log tag for the purchase token.

### Bug Fixes

- auth:
  - Don't ask TwoPass for VPN.

## [9.17.0] - 2023-03-21

### Chores

- Add updated file type drawables.

  Added the latest version of file types used in mail android v6 for attachments
- observability:
  - Update JSON file for "Sign In" and "Checkout" dashboards.
  - Update Grafana dashboard JSON files.

### Features

- auth:
  - Add observability for converting from EA to IA.
- observability:
  - Add observability for converting from EA to IA.

### Bug Fixes

- event-manager:
  - Fix NoSuchElementException in EventWorkerManagerImpl.isRunning.
- key:
  - Fix updating key passphrase error.
- network:
  - Treat ApiResult.Error.Certificate errors as retryable.
- payment-iap:
  - Add missing `OK` case for observability label value.
- presentation:
  - Fix saving state for password input fields.

## [9.16.1] - 2023-03-15

### Bug Fixes

- auth:
  - Don't setup keys for existing Username Account.

### Internationalization

- Upgrade translations from crowdin (6e8d2a4c).

## [9.16.0] - 2023-03-14

### Features

- auth:
  - Converting EA into PA.
  - Prompt for Username EA Login Cap C.

### Bug Fixes

- crypto:
  - Handle null values when decrypting empty messages.

    When decrypting an empty OpenPGP message, gopenpgp will return
    null in the getBinary().
    This null value was not properly handled by the crypto module and would
    result in a NullPointerException.
    
    CP-5565 MAILANDR-435

### Internationalization

- Upgrade translations from crowdin (03364a5f).

### Refactoring

- Make ServerProof validation an usecase.

## [9.15.0] - 2023-03-09

### Chores

- account-manager:
  - More extensive logging for token refresh.
- network:
  - More extensive logging for token refresh.

### Features

- Remove Proton Account Sign Up switch for VPN.

## [9.14.0] - 2023-03-08

### Chores

- Avoid creating SharedPreferences during hilt graph creation
- Upgraded Android Gradle Plugin to 7.4.1.

### Features

- plan:
  - If plan value is zero, take the value from default plan.

### Bug Fixes

- HV3DialogFragment setFragmentResult is now called after viewModel onHumanVerificationResult.
- presentation-compose:
  - Use a main dispatcher in modal runAction to avoid running them in a unconfined dispatcher in ui tests.

## [9.13.0] - 2023-03-01

### Chores

- observability:
  - Don't check for telemetry when checking if observability is enabled.
  - Add JSON files for observability dashboards.

### Features

- observability:
  - Add metrics for (unauth) sessions.

## [9.12.0] - 2023-02-24

### Chores

- coreexample:
  - Add `-dev` suffix for app version header for DEBUG builds.

### Features

- observability:
  - Enable observability.

### Bug Fixes

- auth:
  - Fixed SecondFactor on back click.
- observability:
  - Use proper delays for scheduling sending the obserability events.

## [9.11.2] - 2023-02-23

### Bug Fixes

- auth:
  - Use current session for randomModulus if exist.

## [9.11.1] - 2023-02-22

### Bug Fixes

- payment-iap:
  - Fix user api exception handling.

## [9.11.0] - 2023-02-21

### Chores

- Added Observability main module.

### Features

- observability:
  - Add observability metrics for checkout.
  - Observability metrics for Human Verification.
- payment:
  - Add observability metrics for checkout.
- plan:
  - Add observability metrics for checkout.

### Bug Fixes

- Revert PRAGMA foreign_keys. Keep PRAGMA legacy_alter_table always ON (default in Android).
- observability:
  - Fix json encoding for metric data.

    - Add `@Required` annotation for fields with default values.
  - Fix json schema generator.
- user:
  - Remove deleted user keys from local db.

    When we receive an event for a user, we have to remove the keys
    that are no longer in the model from the local DB.
    
    CP-5365
  - Remove deleted address keys from local db.

    When we receive an event for a user address, we have to remove the keys
    that are no longer in the model from the local DB.
    
    CP-5365

### Internationalization

- Upgrade translations from crowdin (ba33a454).

### Refactoring

- Added prefix and version to Proton endpoints (auth, keys, users, addresses, organizations, settings).

## [9.10.1] - 2023-02-17

### Bug Fixes

- Revert PRAGMA foreign_keys. Keep PRAGMA legacy_alter_table always ON (default in Android).

## [9.10.0] - 2023-02-16

### Chores

- observability:
  - Update signup metrics to use `launchOnScreenView`.
  - Adjust max event count and delay for sending the events.
  - Provide dagger dependencies.

### Features

- observability:
  - Make `runWithObservability` more generic.
  - Add extensions for launching coroutines on "screen view".
  - Observability metrics for signup.
  - Add a delay before sending the events, it there's only one event enqueued.
  - Generate JSON schemas from Kotlin classes.
  - Worker for sending observability events.
  - Implement API call for sending observability events.
  - Add observability data unit tests.
  - Add observability data.
  - Add `ObservabilityManager` and related classes.
  - Generate Kotlin classes from JSON schemas.
  - Gradle task for generating Kotlin classes from JSON schema files with metrics definitions.
- observability-data:
  - Add enabled flag unit tests.
  - Add enabled flag unit tests.
  - Add enabled flag.
- payment:
  - Payment token body adjustment.
- payment-iap:
  - Check for user credits during the payments providers checks.
  - Remove the payment error when user cancelled the payment and set card as default provider when all providers are enabled.
  - Fix gpay button appearance.
- util-kotlin:
  - Add `serializeToJson` extension function.

### Bug Fixes

- Fixed foreign key reference to be renamed while recreating/altering target table.
- Fixed Second Factor error handling on 401 (e.g. 3x wrong).
- observability:
  - Do not send class discriminator ("type" property) when sending observability events.
  - Add missing `@Body` annotation.

## [9.9.3] - 2023-02-17

### Bug Fixes

- Revert PRAGMA foreign_keys. Keep PRAGMA legacy_alter_table always ON (default in Android).

## [9.9.2] - 2023-02-16

### Bug Fixes

- Fixed foreign key reference to be renamed while recreating/altering target table.

## [9.9.1] - 2023-01-27

### Bug Fixes

- auth: Get Domains when Sign Up flows start.

## [9.9.0] - 2023-01-25

### Breaking Changes

**Features**

- Added Unauthenticated Session Support.

  MIGRATION: Add AccountDatabase.MIGRATION_5.

**Refactoring**

- Removed ClientSecret.

### Chores

- plugins-detekt:
  - Enabled NullableTypeSpacing rule.
  - Added rules introduced in 1.22.0.
  - Added libraries rule introduced in 1.22.0.
  - Updated config.yml to match updated KtLint Wrapper in 1.22.0.

### Features

- crypto:
  - Add API to decrypt PGP/MIME messages.

    PGPCrypto now offers to decrypt PGP/MIME messages, using the implementation of gopenpgp.
- key:
  - Add API to decrypt PGP/MIME messages.

    KeyHolderContext now offers to decrypt PGP/MIME messages.
- network:
  - Added Opportunistic Unauthenticated Session Request.
- presentation:
  - Update Button and DefaultText styles.

### Bug Fixes

- auth-presentation:
  - Update text styles and margins to match the designs.
- presentation:
  - Set proper text size for Borderless and Secondary buttons.
- presentation-compose:
  - BottomSheetHeader not able to expand with content.

### Refactoring

- auth:
  - Removed deprecated Auth Session scope field.

### Theming

- Updated Accent Colors.

## [9.8.1] - 2023-01-11

### Features

- crypto:
  - Add parameter to trim spaces for detached signatures.

    Add a parameter to control whether trailing spaces should be trimmed
    from content when generating and verifying text detached signatures.
    
    The default value of the parameter keeps the behavior as it was before.
- gopenpgp:
  - Update to gopenpgp v2.5.0 and go-srp v0.0.5.
- human-verification:
  - Let clients extend HumanVerificationListenerImpl.
- network:
  - Support DNS A records for DoH responses.

### Bug Fixes

- user:
  - Fixed Key format generation logic if 1 address 0 keys (Key Migration Phase 2).

### Internationalization

- Upgrade translations from crowdin (31ed3514).
- Upgrade translations from crowdin (215a5774).

## [9.8.0] - 2022-12-22

### Chores

- Updated Project-wide Gradle settings (enableJetifier=false).
- Upgraded Libraries versions.

  - AndroidX Activity: 1.6.1.
  - AndroidX AppCompat: 1.5.1.
  - AndroidX Collection: 1.2.0.
  - AndroidX Compose: 1.3.2.
  - AndroidX Compose Foundation: 1.3.1.
  - AndroidX Compose Material: 1.3.1.
  - AndroidX Compose Material3: 1.0.1.
  - AndroidX ConstraintLayout: 2.1.4.
  - AndroidX CoordinatorLayout: 1.2.0.
  - AndroidX Core KTX: 1.9.0.
  - AndroidX Espresso: 3.5.0.
  - AndroidX Fragment: 1.5.5.
  - AndroidX RecyclerView: 1.2.1.
  - AndroidX Room: 2.4.3.
  - AndroidX Test: 1.5.0.
  - AndroidX Test Monitor: 1.6.0.
  - AndroidX Test Orchestrator: 1.4.2.
  - AndroidX Test UiAutomator: 2.3.0-alpha01.
  - Dagger Hilt: 2.44.2.
  - Detekt: 1.22.0.
  - Google Tink: 1.7.0.
  - Kotlin: 1.7.20.
  - Kotlin Serialization: 1.4.1.
  - Material Components For Android: 1.7.0.
  - Mockk: 1.13.3.
  - OkHttp: 4.10.0.
  - Robolectric: 4.9.1.
  - Store4: 4.0.5.
  - Turbine: 0.12.1.
- Upgraded Gradle Wrapper to 7.6.

## [9.7.1] - 2022-12-20

### Features

- auth:
  - External Address Cap A/B support for Sign In and Sign Up. You now have 2 parameters:
    ```kotlin
    authOrchestrator.startAddAccountWorkflow(
        requiredAccountType = AccountType.Internal, // For Sign In.
        creatableAccountType = AccountType.Internal, // For Sign Up.
        product = product
    )
    ```
    For example, Drive could allow Sign In External Address but Sign Up Internal Address only:
    ```kotlin
    authOrchestrator.startAddAccountWorkflow(
        requiredAccountType = AccountType.External, // For Sign In.
        creatableAccountType = AccountType.Internal, // For Sign Up.
        product = Product.Drive
    )
    ```
  - Added Challenge Payload to Create External Account.

### Bug Fixes

- human-verification: Copy set-cookie headers in overridden webview requests.

### Internationalization

- Upgrade translations from crowdin (b7080093).

### Refactoring

- auth:
  - Removed unneeded ExternalAccountEnterCode.
  - Split ChooseUsername, ChooseInternalEmail and ChooseExternalEmail.
- human-verification: Removed unneeded classes.

## [9.7.0] - 2022-12-09

### Breaking Changes

**Refactoring**

- test: API changes for various test helpers.
    - If your test inherits `ArchTest` or `CoroutineTest`, apply a default delegate, e.g. `class MyClass: ArchTest by ArchTest()`. This is to ensure we can get a reference to the same object, which would not be possible with properties without a backing field.
    - Remove `mainDispatcher`, `ioDispatcher` and `compDispatcher` from `CoroutinesTest` (use `coroutinesRule.dispatchers.<dispatcher>` instead).
    - Change `TestDispatcherProvider` and `TestCoroutineScopeProvider` from objects into classes.
    - Remove `TestDispatcherProvider.cleanupTestCoroutines` — not needed anymore when using `runTest`.
    - Added `TestScope.flowTest` helper (`TurbineUtils.kt`) to run Turbine test inside an unconfined test dispatcher.

- test-android: Remove `runBlockingWithTimeout` (use `runTest` instead).

**Fixes**

- labels: Support newly added LabelType.SystemFolder.

### Chores

- Update `turbine` to 0.9.0.
- Update `kotlin-coroutines` to 1.6.4.

  Note: some APIs from `kotlinx-coroutines-test` are deprecated, and will become an error in 1.7.0.

### Features

- auth: Fix GPay dark mode.

### Bug Fixes

- challenge:
  - Removed "uuid" from Challenge frames (ANDROID_ID).
  - Improve Key/Paste/Copy handling logic.

### Internationalization

- Upgrade translations from crowdin (213e46d3).

## [9.6.0] - 2022-11-25

### Chores

- human-verification:
  - Remove HV2.
- include-core-build:
  - Added Gitlab Job Token support.
  - Added Include Git Repo support.

### Bug Fixes

- human-verification:
  - Show error snack if HV failed loading.
  - Include extra headers (e.g. proxy token) whenever the root domain matches.
- key-data:
  - Remove old public keys when refreshing.

    When fetching public keys from the backend, we need to
    remove old keys that were previously stored in the
    local db and are no longer in the API response.
- network:
  - Notify client about alternative proxies failures.
- presentation-compose:
  - Update typography values according to designs.

### Internationalization

- Upgrade translations from crowdin (cb8fc206).

## [9.5.0] - 2022-11-18

### Chores

- include-core-build:
  - Added meta.properties marker support.
- network:
  - Fix TimeoutOverride is not being respected.
  - Decrease connection timeout to 3 sec.

### Features

- auth:
  - Adjust account setup for Internal accounts.

    - The two-pass mode is executed before setting up address keys.
    - For Internal accounts, make sure to setup keys for all addresses.
  - Setup keys for external accounts.
- key:
  - Use proper flags when creating user address key for an external account.
  - Add constants for `PublicAddressKeyFlags` values.
- user:
  - Additional extension functions for `List<UserAddress>` and `UserAddress`.
  - Use proper flags when creating user address key for an external account.
- util-kotlin:
  - Add `String.random()` extension.

### Internationalization

- Upgrade translations from crowdin (559b7749).

## [9.4.1] - 2022-11-14

### Bug Fixes

- network-data: Fixed potential SerializationException on deserialize SerializableCookies.

## [9.4.0] - 2022-11-10

### Breaking Changes

**Features**

- user-setting-presentation-compose: Use Optional ViewModel hiltViewModelOrNull for Device Settings.

### Features

- presentation-compose: Added Optional ViewModel hiltViewModelOrNull function.

## [9.3.1] - 2022-11-09

### Features

- gopenpgp:
  - Update to gopenpgp v2.4.10 and go-srp v0.0.5
- key:
  - Add internal/external ext functions to public addresses and keys.

### Bug Fixes

- user-data:
  - Fixed deserialization issue for UserAddressEvent (action=0).

### Internationalization

- Upgrade translations from crowdin (7d057c46).

## [9.3.0] - 2022-11-07

### Chores

- Renamed Gradle Project names (allow accessors).

### Features

- auth: Uniform username for any account type.
- plan: Fix current plan currency.
- user-settings:
  - Added DeviceSettingsHandler (observe DeviceSettings changes).
  - Added Device Settings Feature Flag ("ShowDataCollectSettings").
  - Added TelemetrySettingsToggleItem & CrashReportSettingsToggleItem Composables.
  - Added DeviceSettingsRepository (DeviceSettings: isTelemetryEnabled & isCrashReportEnabled).

## [9.2.2] - 2022-10-28

### Features

- plan:
  - Fix current plan currency.

## [9.2.1] - 2022-10-27

### Chores

- payment:
  - Constraint payment activities to portrait.

### Refactoring

- plan:
  - Use amount instead of renew amount in current plan.

## [9.2.0] - 2022-10-26

### Chores

- Upgraded libs versions.

  - Kotlin 1.6.21.
  - Kotlin Coroutines 1.5.2.
  - Kotlin Serialization 1.3.3.
  - Java 11 (+ desugar_jdk_libs 1.2.2).
  - Gradle 7.4.2.
  - AGP 7.3.1.
  - SDK Tools 30.0.3.
  - Dagger/Hilt 2.44.
  - Mockk 1.13.2.
  - Activity 1.5.1 (prev was 1.5.0).
  - Compose 1.2.0-rc02 (prev was 1.2.0-alpha01).
  - Core SplashScreen 1.0.0 (prev was 1.0.0-beta2).
  - Fragment 1.5.4 (prev was 1.3.6).
  - Hilt Navigation Compose 1.0.0 (prev was 1.0.0-rc01).
  - Lifecycle 2.5.1 (prev was 2.4.0).
  - Material3 1.0.0 (prev was 1.0.0-beta01).
  - Navigation 2.5.3 (prev was 2.4.0).
  - Startup 1.1.1 (prev was 1.1.0).

## [9.1.3] - 2022-10-26

### Features

- Added Include Core Build Gradle Settings Plugin.
- auth: Handle error for unsupported external account.

### Bug Fixes

- key:
  - Throw when nested private key can't be decrypted

    Instead of returning an inactive nested private key if the passphrase
    can't be decrypted or verified,
    `KeyHolderContext.decryptAndVerifyNestedPrivateKey()` will throw, and
    let the caller handle errors.
- payment:
  - Memory leak in billing screen.
- plan:
  - Fix PayButton loading state.
  - Fix PayButton for existing payment method.
  - Fix plan card payment PayButton state.
  - Fix plans order, star and descriptions.

### Internationalization

- Upgrade translations from crowdin (3f00767c).

### Refactoring

- user: Use decryptAndVerifyNestedPrivateKeyOrNull for address keys.

## [9.1.2] - 2022-10-14

### Chores

- plugins-detekt:
  - Added '*Previews' to `UnusedPrivateMember.ignoreAnnotated`

    This allows to define a custom annotation like the following
    ```kotlin
    @Preview(device = Devices.PHONE)
    @Preview(device = Devices.FOLDABLE)
    @Preview(device = Devices.TABLET)
    annotation class AdaptivePreviews
    ```

### Bug Fixes

- Use deserialize instead of deserializeOrNull in event listeners.

  The goal is to prevent exceptions from being swallowed and to make issues easier to detect.
- plan:
  - Introduce SubscribedPlan.kt entity.
  - Fix subscription plan amount.

## [9.1.1] - 2022-10-10

### Bug Fixes

- plan:
  - The compilation should not fail, if `payment-iap` module is not added.

## [9.1.0] - 2022-10-07

### Features

- payment:
  - For GIAP, acknowledge the purchase after the subscription is assigned.

    MIGRATION:
    1. Add and provide `PaymentDatabase`.
    2. Add a database migration: `PaymentDatabase.MIGRATION_0`.
  - Redeem unacknowledged Google purchase for (paid) users.
  - Support GIAP payment option
- payment-iap:
  - Detect if there is an unredeemed Google purchase.

    `UnredeemedPurchaseInitializer` will be added to your manifest (androidx.startup).
    In case there is an unredeemed Google purchase, an alert dialog will
    be shown (in a separate, transparent activity), asking the user to redeem.
  - Match a Google purchase with a Proton user.
- plan:
  - Add plan cycles UI and unit tests.
  - Add support for unknown plan cycles.
  - Redeem unacknowledged Google purchase for (paid) users.
- presentation-compose:
  - Create ProtonTheme 3 extending material 3 theme.

### Bug Fixes

- Make sure to add **"core library desugaring"** to support `Optional` API from Java 8.
- Pass test dispatcher to runTest method.

  This is to solve test failures which started happening after updating
  AGP to 7.1.3 (error `Detected use of different schedulers..`)
- network:
  - Don't retry DohApiHandler when it failed.
  - Introduces fine grained control of timeouts.
  - Reduce proxy validity to 90min.
  - Remove timeouts for refreshing proxies.

    Refreshing proxies can take a long time - in VPN it requires a user
    action (getting VPN permission for Guest Hole). Instead use timeout for
    running proxies after they are refreshed. There are still timeouts for
    using DoH services so as long as Guest Hole doesn't kick-in there's
    still a limit for refreshing proxies.
  - Remove Bearer prefix when logging token.
  - Retry request only once on 408 code.
- payment, plan:
  - Don't crash when checking for unredeemed purchases, if Billing services are unavailable.
- payment-iap:
  - Propagate errors received in `BillingClientStateListener.onBillingSetupFinished`.
- presentation:
  - Prevent crash in `AppLifecycleObserver` when constructed on a non-main thread.

### Internationalization

- Upgrade translations from crowdin (544a83d1).

### Refactoring

- payment:
  - Introduce value classes for payment/purchase tokens.
  - Rename `PaymentToken` into `PaymentTokenResult`.
- payment-iap:
  - Add `GoogleBillingRepository` for interacting with Google Play Billing Client.

## [9.0.2] - 2022-09-22

### Chores

- Added CoreLogger RuntimeException for Login flow.
- Updated AndroidX Activity to 1.5.0.

### Features

- crypto-android:
  - Synchronized KeyStore Encrypt/Decrypt functions.
    Make sure only one KeyStore operation at a time, to minimize KeyStore concurrency issues.
- presentation-compose:
  - Added composable to remember a request to prompt the user to create a document.
  - Added possibility to modify launcher Intent for OpenMultipleDocuments activity.

### Bug Fixes

- Avoid processing empty detekt reports list
- auth:
  - Keep ConfirmPasswordActivity in the same task as current resumed.
- human-verification:
  - Keep HumanVerificationActivity in the same task as current resumed.
  - Use AppCompatActivity to support forcing night mode.
- key:
  - Use only Active Private Keys from KeyHolder.
- util-android-strict-mode:
  - Ignore StrictMode LeakedClosableViolation.

### Internationalization

- Upgrade translations from crowdin (81a4b7a9).

### Refactoring

- presentation-compose:
  - Deprecated rememberFilePickerLauncher composable, should be replaced with rememberOpenMultipleDocumentsLauncher.

## [9.0.1] - 2022-09-14

### Bug Fixes

- plan-presentation: Fixed DateFormat for PlanItemView (fr: "12 mai 2024", en: "May 12, 2024").

### Internationalization

- Upgrade translations from crowdin (2a21b3ae).

### Refactoring

- Removed unneeded core-platform module.
- network-data: Removed TrustKit dependency.

## [9.0.0] - 2022-09-02

### Breaking Changes

**Features**

- human-verification:
  - Provide a shared (singleton) instance of `OkHttpClient`.

    The provided `OkHttpClient` instance is not customized in any way,
    and can be used to contruct your own HTTP clients (`OkHttpClient.newBuilder`).

    BREAKING-CHANGE: `ApiManagerFactory` takes `OkHttpClient` as a constructor argument. To inject, use `@SharedOkHttpClient` annotation.
- network:
  - The base API URL needs to be provided in client app dagger module.

    The URL needs to be an instance of `okhttp3.HttpUrl` with `@BaseProtonApiUrl` qualifier.
    In turn, clients no longer need to provide `ClientIdProvider`.
  - Provide a shared (singleton) instance of `OkHttpClient`.

    The provided `OkHttpClient` instance is not customized in any way,
    and can be used to contruct your own HTTP clients (`OkHttpClient.newBuilder`).

    BREAKING-CHANGE: `ApiManagerFactory` takes `OkHttpClient` as a constructor argument. To inject, use `@SharedOkHttpClient` annotation.

**Refactoring**

- auth:
  - Moved MissingScope state handling in Initializer.

    **There is no need to add any client side handling code anymore.**
- human-verification:
  - Moved HumanVerification state handling in Initializer.

    **There is no need to add any client side handling code anymore**.

### Chores

- plugins-detekt:
  - Reduced Detekt exclude rules, especially for tests.
- report-presentation:
  - Rename "Report an issue" to "Report a problem".

### Features

- Add `util-android-dagger` module for providing `AppLifecycleProvider`, `CoroutineScopeProvider` and `DispatcherProvider`.
- Include `*-dagger` modules in their parent modules.

  The `*-dagger` modules are included by default in their 'parent' modules (i.e.: `key-dagger` is included by default inside `key` module and maven dependency). This will cause build issues with Hilt because some of the common dependencies that you were providing at the moment will be duplicated in these new modules. To solve this you could either:
  
  - Remove those dependencies, either whole module classes from your app or just the conflicting @Provides and @Binds.
  - Exclude the dagger modules until you have time to address this problem. To do this, you can use `exclude`:
  
  Kotlin version:
  ```kotlin
  implementation("me.proton.core:some-feature:1.2.3") {
      configurations.all { exclude(module = "some-feature-dagger") }
  }
  ```
  
  Groovy version:
  ```groovy
  implementation("me.proton.core:some-feature:1.2.3") {
      configurations.all { exclude module: "some-feature-dagger" }
  }
  ```
  
  This same change can also be used to remove the dagger module and provide your own dependencies when needed. However, you'd have to provide *all* the dependencies included in this module, as with Hilt there's no way to selectively override dependencies.
- All Core modules have a corresponding `*-dagger` module.
- feature-flag:
  - Added Feature Flag not found info log.
- human-verification-presentation:
  - HumanVerificationActivity now use `ProtonTheme.DialogWhenLarge`.

    HumanVerificationActivity will be displayed either full-screen (small, normal) or as a dialog (large, xlarge).
- network:
  - Added "proton.me" Public Keys Pinning.
  - Removed old Public Key Pinning.
  - `ApiManagerFactory` will be provided by `network-dagger` module.

    The client apps will additionally need to provide:
    - array of doh providers (`@DohProviderUrls`)
    - array of certificate pins (`@CertificatePins`)
    - list of alternative api pins (`@AlternativeApiPins`)
  - Provide `ServerTimeListener` (for updating pgp-crypto time).
- payment:
  - Added PaymentManager.

    - GetPaymentProviders: returns a set of PaymentProvider which can be offered to the user.
    - IsUpgradeAvailable: returns true if there is an upgrade flow available.
    - IsSubscriptionAvailable: returns true if there is (current) subscription flow available.
  - Added Google In-App Purchase support.
  - Add the new endpoint for payment status and remove the old one.

    DAGGER: You should provide an `AppStore` in your dagger module:
    - For builds distributed in Google Play Store:
    ```
    @Provides
    fun provideAppStore() = AppStore.GooglePlay
    ```
    - For builds distributed in F-Droid:
    ```
    @Provides
    fun provideAppStore() = AppStore.FDroid
    ```
  - redesign payment for IAP support.

    feat(plan): redesign plans for IAP support.
- payment-iap:
  - Added Google In-App Purchase module.
- plan:
  - Updated the Current Plan used/max values calculation.
  - Map (Proton) plan names to plan names for Google Play store.
- plugins-detekt:
  - Exclude Database annotated classes from `UnnecessaryAbstractClass` rule
  - Allow to add a configuration file for Custom Detekt Rules in config/detekt/custom-rules.yml. The path can be changed by `protonDetekt { customRulesConfigFile = file(path-to-file.yml) }`.
  - Allow custom threshold as `protonDetekt { threshold = 123 }`.
- presentation:
  - Automatically clear ProtonInput TextPassword onDetachedFromWindow/onSaveInstanceState.
  - Added ProtonInput setSingleLine.
- proguard-rules:
  - Add `proguard-rules` module.

    The module contains common proguard rules that are shared by multiple Core modules.
    Currently it contains rules for classes marked with `@Serializable` (kotlin-serialization).

    Note: the "official" rules from the kotlinx.serialization do not cover all cases we have,
    so we use a different set of rules.
- util:
  - Add a StrictMode.VmPolicy builder extension function.

### Bug Fixes

- auth-presentation:
  - Fixed Terms & Conditions WebView issues.
- challenge-presentation:
  - Fixed potential NoSuchElementException in ProtonMetadataInput.
- feature-flag:
  - Fetch unknown local FeatureId, and remember unknown remote FeatureId.
- human-verification:
  - Fixed Change Password HumanVerification flow.

    HumanVerification handling is not anymore coupled to the Login or SignUp flows, but apply to any network call.
    Start HumanVerification Activity onHumanVerificationNeeded (usually handled in top level main Activity).
  - Delete invalid `HumanVerificationEntity` objects from DB.

    In the past (HV2) it was possible to have `HumanVerificationNeeded` state,
    and `null` captcha verification token. In HV3, for `HumanVerificationNeeded` state,
    the captcha verification token should always be present.
    
    The migration will delete all `HumanVerificationEntity`
    objects except those that are in "success" state.

    MIGRATION: `HumanVerificationDatabase.MIGRATION_1`
- network:
  - Make sure we update the last HV time even if the coroutine is cancelled.
- plan:
  - Fixed Free Plan user count.
- plan-presentation:
  - Fixed not showing Currency Dropdown.
- plugins-detekt:
  - Fetch Detekt config from main branch on GitHub.
  - Configuration file is now updated entirely, before the update might be partial, as we were using the async BufferedWriter.
- presentation-compose:
  - Make ProtonCenteredProgress being centered.
  - Add overlineWeak to ProtonTypography.
- user:
  - Fixed UserAddressKey format generation logic (Key Migration Phase 2).

### Internationalization

- Upgrade translations from crowdin (07889828).
- Upgrade translations from crowdin (cb51aedf).
- Upgrade translations from crowdin (7e773d3e).

### Performance Improvements

- auth/challenge/user:
  - Eliminate IO on the main thread during login and signup (gathering challenge data).
- network:
  - Use IO dispatcher when getting `ApiManager` instance from `ApiProvider.get`.

    The first call to `ApiManagerFactory.create` can be expensive (50-100ms), and performs disk IO.

### Refactoring

- Use `@Binds` whenever possible (instead of `@Provide`).
- Use `CoroutineScopeProvider` and `DispatcherProvider` when possible.

  - Use `CoroutineScopeProvider` instead of creating custom (global) `CoroutineScope` instances.
  - Use `DispatcherProvider` instead of calling methods on `kotlinx.coroutines.Dispatchers` object.
- contact:
  - Rename `contact-hilt` into `contact-dagger`.
- crypto-validator:
  - Run validation on IO thread (due to accessing shared prefs).
- feature-flag:
  - Refactored Feature Flag modules.

    Fixed issues:
    - `FeatureFlagRepositoryImpl get` doesn't return a nullable `FeatureFlag`.
    - Fetching for a different userId will discard others. `FeatureFlagEntity` should have `userId` as primaryKey.
    - Missing Local/Remote DataSource for consistency.
    - Add `FeatureFlagManagerImpl` prefetch `Worker`.
- human-verification-data:
  - Renamed DB HumanVerificationEntity captchaVerificationToken to verificationToken.

    MIGRATION: HumanVerificationDatabase.MIGRATION_2.
- network:
  - Add explicit parameter for DoH URLs in `ApiManagerFactory`.
  - Use `URI.resolve` for constructing the URL for `dns-query` endpoint.
- plugins-detekt:
  - Deprecated `configFilePath` in favor of `configFile` and minor refactors.
- user:
  - Split `CoreUserManagerModule` into smaller modules.

### Theming

- Added `ProtonTheme.DialogWhenLarge` for Activity that will be displayed either full-screen (small, normal) or as a dialog (large, xlarge).
- Updated Presentation and Presentation-Compose modules Color Taxonomy.

  Added 1 global color (Pampas).
  Added 1 reference color (proton_background_deep).
  Changed Dark/Light Theme colors assignment.

## [8.5.0] - 2022-07-15

### Features

- crypto:
  - Add ArmoredKey type to crypto
- gopenpgp:
  - Update to gopenpgp v2.4.8 and go-srp v0.0.5
- key:
  - Add getArmored with header to KeyHolderCrypto.
- push:
  - Added Push modules, to fetch and persist current PushEntities.

    New DB migration: `PushDatabase.MIGRATION_0` for PushEntity.
    New event listener: `PushEventListener`, to keep PushEntities in sync.
    New FetchPushes[Remote|Worker] and DeletePush[Remote|Worker], for Repository offline action.
    Use `PushRepository` to get and observe `Push` objects.

### Bug Fixes

- account-manager-presentation:
  - Support different text sizes in account_view.xml.
  - Support different text sizes in account_primary_view.xml.
- auth:
  - Added scrolling to Add Account screen.
- auth-presentation:
  - Fixed Domains dropdown focus issue.
- crypto-android:
  - Added retryOnce mechanism for Android KeyStore Key initialization.
- event-manager:
  - Filtered out ApiException HTTP 401 from CoreLogger error tag.
- human-verification:
  - Fixed HV3 Dialog Lifecycle and Main thread issues.
  - Potential crash during HV (blank "reason phrase").
- network:
  - Fix invalidating alt. backend after the failure.

    activeAltBackend == backend condition was incorrect to detect
    failure of the alt. backend for scenarios involving concurrent
    calls. Logic responsible for reacting to backend failures is
    now called explicitly after original API attempt.
  - Execute DoH services in parallel.
  - Force respecting ApiClient.timeoutSeconds for calls taking longer in retrofit.

    We noticed that sometimes retrofit can take longer to fail e.g. with

    "SocketTimeoutException: SSL handshake timed out". If timeout is not respected alt. routing
    calls will not have enough time to finish in a predefined window.
- presentation:
  - Add content description to the three dots button in account_view.xml.
  - Fix "disabled" buttons in dark mode.

    Color of disabled buttons is darker in dark mode and lighter in light
    mode.
  - Use "accent" color for outlined buttons.

    Toggle buttons use regular color because they are filled when toggled.
  - Stop overriding navigationBarColor and statusBarColor in Transparent themes.

    This was overriding the initial fix for navigation bar theming, when
    in fact there is no need for that. The same values that are used in
    ProtonTheme should be used in the Transparent themes as well.
  - Fix navigation bar theming.

    There is an issue for API levels below 27 where handling of light/dark
    navigation bar cannot be handled. In these cases we need to use the
    default black navigation bar.
- report:
  - Adds OS indicator in `Client` for bug reports.

    We use this field to label the subject of the tickets and CS relies on being able to spot Android tickets straight away.
- user-settings:
  - Fixed Username/DisplayName fallbacks (setup key/address, change password/recovery email).

### Internationalization

- Upgrade translations from crowdin (bc25dd06).
- Upgrade translations from crowdin (8fb65c0b).
- Upgrade translations from crowdin (690ded5a).

### Reverted Changes

- auth, plan, user-settings:
  - Prevent potential issues, when keyboard input is directed to a fragment which is not visible.

    This reverts commit 95892ccdca5fb009e95e5095ea6411d2535c9b34.
    This commit is breaking some assumptions of SignUp/HV flows in some specific circumstances.

### Theming

- presentation:
  - Changed TextInputLayout hint colors (end icon, helper text).

## [8.4.0] - 2022-06-24

### Features

- presentation:
  - Add `ic_proton_verified_badge` drawable.

### Bug Fixes

- human-verification:
  - User may be stuck on HV screen.
  - Fixes for HV3 when DoH is active.

    - Allow self-signed certificates for HV webview POST requests.
    - Workaround for CSP header.

    BREAKING-CHANGE: Removed `context` parameter from the constructor of `NetworkRequestOverriderImpl`.

### Refactoring

- human-verification:
  - Avoid hard-coded values.

    - Host value for `X-PM-DoH-Host` header.
    - Host value when checking if we're using an alternative host.

## [8.3.0] - 2022-06-23

### Features

- crypto:
  - Added the same KeyStore workaround as Tink `AndroidKeystoreAesGcm`.
- user:
  - Switched default User Address Key model generation to new format (ref: Key Migration Phase 2).

### Bug Fixes

- account-manager:
  - Log invalid user and address keys events as errors

    When the account-manager detects an account with invalid user or address keys
    (they don't decrypt with the user password), we start logging these events as
    errors instead of information.
    Also improve the error message for clarity.
- auth:
  - Fix (potential) crash in `CreatingUserFragment`.

    HV can be triggered also during the login process, and at
    that moment, `CreatingUserFragment` will already be gone, so
    it could crash when calling `parentFragmentManager`.
  - Improved support for orientation changes.
  - Prevent potential issues, when keyboard input is directed to a fragment which is not visible.

    Previously, we were adding the fragments on top of each other.
    The issue was, if keyboard was visible, and you rotated the screen,
    the focus would go to the first fragment on the back stack
    (which would not be visible).
- auth-presentation:
  - Fix crashes for `RecoveryEmailFragment`.
- event-manager:
  - Changed EventWorker Log Level for ApiException to Debug.
- plan:
  - Prevent potential issues, when keyboard input is directed to a fragment which is not visible.

    Previously, we were adding the fragments on top of each other.
    The issue was, if keyboard was visible, and you rotated the screen,
    the focus would go to the first fragment on the back stack
    (which would not be visible).
- presentation:
  - Make changes to support accessibility according to guidelines.
- user:
  - Invalidate User/UserAddress Memory Cache on insertOrUpdate.
- user-settings:
  - Prevent potential issues, when keyboard input is directed to a fragment which is not visible.

    Previously, we were adding the fragments on top of each other.
    The issue was, if keyboard was visible, and you rotated the screen,
    the focus would go to the first fragment on the back stack
    (which would not be visible).
  - User input is not preserved after rotating.

### Internationalization

- Upgrade translations from crowdin (2f3a1326).

### Refactoring

- auth:
  - No need to create `ChooseUsernameFragment` if we're recreating the activity.
- presentation:
  - Remove dependency on kotlin-reflect.

### Theming

- presentation:
  - Updated Proton Button colors (background + text).

## [8.2.0] - 2022-06-14

### Features

- presentation-compose:
  - Added `ProtonAlertDialogButton` and more flexibility for overriding button colors on `ProtonButton`.
  - Added `ProtonColors.interactionNorm` with enabled/disabled state.
- util-kotlin:
  - Added `Int.toBooleanOrTrue()`.

### Bug Fixes

- plugins-detekt:
  - Ignore '@Preview' annotated functions from 'UnusedPrivateMember'
- user-setting:
  - Updated UserSettings Boolean conversions to be more resilient to unexpected values from server.

### Internationalization

- Upgrade translations from crowdin (8faa0ac5).
- Upgrade translations from crowdin (8f6544b6).

### Refactoring

- user-setting:
  - Removed deprecated UserSettings twoFA u2fKeys property.

## [8.1.0] - 2022-06-03

### Features

- label:
  - Added LabelRepository updateLabelIsExpanded.
- presentation-compose:
  - Added ProtonSidebarItem/ProtonListItem iconModifier/textModifier.

### Bug Fixes

- user:
  - Fixed UserAddressRepository getAddress/observeAddress.

    Always get all addresses first, then filter on addressId.
    Added observeAddress for consistency.
  - Fixed UserAddressRepository fetching addresses even if exist in Database.
- user-settings:
  - Fixed Non-admin Organization Member cannot change password.

### Internationalization

- Upgrade translations from crowdin (8a9fab5e).

## [8.0.0] - 2022-05-25

### Chores

- auth:
  - Update forgot password url.

### Features

- gopenpgp:
  - Update to gopenpgp v2.4.7 and go-srp v0.0.5.
- network:
  - Improvements for responses with `Retry-After` header.

    If a response's HTTP code is 429 or 503, and if `Retry-After` is <= 10 seconds,
    the request will be automatically retried. Otherwise, an error will be directly
    returned to the client.
    
    You can use `ApiResult.retryAfter()` method to check the
    `Retry-After` header value returned by the server.
  - Retry a request if server returns HTTP 408 (request timeout).

### Bug Fixes

- auth:
  - Clear password field, if user entered wrong password/credentials.
  - More suitable label when logging in with internal/username account.
- network:
  - Fix issue with reading alt. routing certs on older Androids.

    Turns out that on some Android versions (like 8.1)
    split(Pattern.compile("(?=-----BEGIN CERTIFICATE-----)"))
    gives ["", "cert1", "cert2"] as a result, leading to reading
    empty list of certificates and captcha not loading on alt.
    routing.
  - Don't persist last DoH refresh.

    lastAlternativesRefresh shouldn't be persisted as it's a value coming
    from monotonic clock (which is not consistent between app launches or
    reboots).
  - Use elapsedRealtime instead of nanoTime as mono clock.

    elapsedRealtime will give correct durations also when device was in
    a deep sleep, which is not the case for nanoTime.
- payment:
  - Remove not needed ThreeDSSupport field in PaymentMethodDetails.kt
  - Credit card expiration year should contain 4 digits.
- presentation:
  - Incorrect height of AlertDialogs with single-choice items.

    As a workaround, set a custom layout for `singleChoiceItemLayout`,
    which uses `drawableLeftCompat`/`drawableRightCompat`.
    Using relative attributes (e.g. `drawableStartCompat`) would result in incorrect height.
  - Remove unneeded padding from AlertDialog's button bar.
- user:
  - Incorrect URL for unlocking password scope.

### Internationalization

- Upgrade translations from crowdin (537d3637).
- Upgrade translations from crowdin (93469489).

### Reverted Changes

- network:
  - Remove `TooManyRequestInterceptor`.

    The network layer will no longer prevent executing subsequent requests
    during the cool-down period (after the server has returned a `Retry-After` header).

### Theming

- Update color of some "branding" UI elements.

  In dark mode, we should use `brand_lighten_20` for "branding" elements.
- Update core-splashscreen library.

  Includes a fix for an issue with MIUI, when the logo is not shown on the splash screen in dark mode.
- update header string for VPN plan.
- Use an icon for tor with 24dp size (the same as the other icons).
- Replaced Snackbar colors (new colors: Mauvelous, Texas Rose, Puerto Rico; adjusted: Pomegranate, Sunglow, Apple).
- Replaced Sidebar colors (new: Haiti, Valhalla, Jacarta).
- Update create account Lottie animation.
- Added ProtonBottomSheetDialog Theme to ProtonTheme Material styles overrides.
- Update the image in "congratulations" screen after creating a new account.
- Update icons usage.
- Updated Sidebar & Primary Account Switcher colors.
- Fixed Vector Drawable crash for Android 6.0.
- Remove temporary tint, add support for different icons for plan feature items and add new plans texts.
- Add updated accent colors to taxonomy.
- Updated Logos colors (with brand references).
- Updated Splash/Welcome/SignIn/SignUp Screens.

  Added SplashScreen API support (https://developer.android.com/guide/topics/ui/splash-screen).
  Client MainActivity migration: Call installSplashScreen() before onCreate, and optionally setKeepOnScreenCondition to minimize transition between Splash and next Activity.
- Removed VPN specific brand colors.
- Update color taxonomy.
- Add new logos, replace existing ones.
- Update corner radius for rebranding.
- presentation-compose:
  - Added PROTON_PROGRESS_TEST_TAG to ProtonCenteredProgress.

## [7.2.3] - 2022-05-05

### Fixes

- Added Signup type for Domain availability check:
  - Usage of getAvailableDomains ("api.host/domains/available?Type=signup") is now only for Signup.
  - SetupPrimaryKeys now assume freshly created internal users (vai POST /users) already have an internal address with the right domain to use.

## [7.2.2] - 2022-05-04

### Theme

- Changed Proton Product names (e.g. "ProtonMail" -> "Proton Mail").

### Fixes

- account-manager:
  - ViewModel Flow in Account[List|Primary]View (fix Compose usage). 
- humanverification:
  - Log url used to load captcha page.
- network:
  - Persist resetting expired alt. backend.

### Features

- presentation-compose:
  - Added ProtonTopAppBar Composable.
  - Changed ProtonSettingsTopBar to use ProtonTopAppBar.

## [7.2.1] - 2022-05-03

### Fixes

- Removed default domain (DefaultDomainHost qualifier doesn't exist anymore). Please update your DomainRepository injection as follow:
```
@Provides
@Singleton
fun provideDomainRepository(
    provider: ApiProvider
): DomainRepository = DomainRepositoryImpl(provider)
```

## [7.2.0] - 2022-05-02

### Features

- Added automatic Changelog based on conventional commits.

  See more details in [README.md](README.md#conventional-commits).
  See [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/).

- crypto:
  - New crypto function `PGPCrypto.generateRandomBytes()` to generate random bytes without any encoding.
  - `KeyHolderContext.generateNestedPrivateKey` and `NestedPrivateKey.generateNestedPrivateKey` have an optional parameter, `generatePassphrase: ()->ByteArray` to let the caller choose how the passphrase is generated and encoded. The default value keeps the same behavior as before.

- theme: ProtonTheme composable considers user's preference to set the Theme.

### Fixes

- account-manager:
  - Memory leak in Account[List|Primary]View.

- bug-report:
  - Improve displaying error messages in Bug Report form

- human-verification:
  - Add a gap between calling code and phone number.
  - Allow forced dark mode on web views for ToS and captcha.

- key:
  - Added a way to control the passphrase of generated nested private keys.
  - Added an optional parameter `generatePassphrase` that let the caller of the generation function choose the format of the generated passphrase.

- payment:
  - Clear consumed Payment Token (HumanVerification token).
  - Correct placement of the billing period label.

- plan:
  - Current plan used space color indicator.
  - Default plan max space to take into account RewardsMaxSpace.
  - Use user currency in the current plan subscription screen for free plan.
  - Fallback to default plan for connections and addresses counters.
  
### Refactor

- Improved EventManager States handling.

  - Don't re-enqueue the Worker if it is currently running. This help to prevent canceling long running operation.
  - Cancel/suspend will always directly stop the Worker. The Worker will be resumed if previously suspended and will re-fetch data (could have changed in the meantime).
  - OnComplete is always the last step. It should not be used for long running operation or as a place for saving data.
  - OnSuccess is called after successfully notifying all the listeners.
  - OnFailure is called after too many retries, when at least one listener constantly failed to apply the set of modifications.
  - OnResetAll is called after onFailure.
  - Example of success: Enqueued -> Fetching -> Persisted -> NotifyPrepare -> NotifyEvents -> Success -> NotifySuccess -> NotifyComplete -> Completed.
  - Example of failure: ... Persisted -> NotifyPrepare -> NotifyEvents -> max retries reached -> Failure -> NotifyFailure -> NotifyResetAll -> NotifyComplete -> Completed.
  - Example of normal reset: ... Persisted -> Refresh > 0 -> NotifyResetAll -> NotifyComplete -> Completed.

## [7.1.11]

### Changes

When providing the AuthRepository & UserRepository there is a new `Product` parameter that is needed:
```kotlin
@Singleton
fun provideAuthRepository(
    apiProvider: ApiProvider,
    @ApplicationContext context: Context,
    product: Product
): AuthRepository = AuthRepositoryImpl(apiProvider, context, product)

@Provides
@Singleton
fun provideUserRepositoryImpl(
    db: UserDatabase,
    provider: ApiProvider,
    @ApplicationContext context: Context,
    cryptoContext: CryptoContext,
    product: Product
): UserRepository = UserRepositoryImpl(db, provider, context, cryptoContext, product)

```

### Fixes
- Fix EventManager Completed state and potential edge cases.
- Fix UserAddressRepository returning wrong address (FetchedTagAddress).
- Challenge entities fixed. Clients should update their DB with the following migration:
```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        ChallengeDatabase.MIGRATION_1.migrate(database)
    }
}
```

### New

- Added HelpOptionHandler interface so that clients can have more control over the Sign In Help
screen options. Core also provides default implementation.

```kotlin
@Provides
@Singleton
fun provideHelpOptionHandler(): HelpOptionHandler = DefaultHelpOptionHandler()
```

## [7.1.10]

### Changes
- Updated gopenpgp to v2.4.6.
- Added subheadline typography into Compose theme.
- Added error logging to HV WebView. Errors are logged with `LogTag.HV_REQUEST_ERROR` tag.
- Merge Drive Compose module to Core.
- Removed useless `ApiConnectionException`, and `ApiResult.Error.Connection.path/query`.

### Fixes
- Fix alternative routing for token refresh.
- Fixes respecting ApiClient.shouldUseDoh.
- Fix issues with CryptoValidator when activities were re-launched.
- Fix potential issue in database migrations (checking if a column exists before creating it)
- Don't wrap unexpected Exception as ApiResult.Error.Connection.

## [7.1.9]

### Changes
- MailSettingsRepository now supports offline changes.
- Added ContactCard type 1 (Encrypted, but not signed).
- Updated Icons set.

### Fixes
- Remove creating account screen when there is an error in the sign up flow.
- Signup UI tests failing because of the changes in the domain selection.
- Do not log throwable objects, if the original log record doesn't include a throwable (for logs that come via `java.util.logging.Logger`).
- Fixed EventManager isStarted never updated.

## [7.1.8]

## New
- Added fingerprinting to the login flow. Clients should update their AuthModule.kt dependencies:

AuthRepository now requires application context. This is due to requirement some of the data in the
fingerprint collection require context to read it:
```kotlin
@Provides
@Singleton
fun provideAuthRepository(
    apiProvider: ApiProvider,
    @ApplicationContext context: Context
): AuthRepository = AuthRepositoryImpl(apiProvider, context)
```

Also, provide the LoginChallengeConfig in the same file:
```kotlin
@Provides
@Singleton
fun provideLoginChallengeConfig(): LoginChallengeConfig = LoginChallengeConfig()
```

### Fixes
- Several fixes for sign up related to lifecycle misusage.
- Fixed EventManager incorrect isStarted value (now rely on Job.isActive).
- App versioning now accepts 4 numeric values (1.2.3.4).

## [7.1.7]

## New
- Added functions to get a timestamp from a verified signature, to the crypto module and to the KeyHolder's context:
    `getVerifiedTimestampOfText` and `getVerifiedTimestampOfData`

### Changes
- Added validation for Client Version header (x-pm-appversion).
- Refactored DoH and added support for unblocking the alternatives.
- Users can now select from different domains in Internal account sign up.

### New Injection
- ApiManagerFactory now needs an extra `ClientVersionValidator` dependency, which is already included in the Network dagger module.

```kotlin
@Provides
@Singleton
fun provideApiFactory(
    ...,
    clientVersionValidator: ClientVersionValidator,
    apiConnectionListener: ApiConnectionListener? = null,
): ApiManagerFactory {
    return ApiManagerFactory(
        ...,
        clientVersionValidator = clientVersionValidator,
        apiConnectionListener = apiConnectionListener,
    )
}

```

All clients except VPN should also provide null for DohAlternativesListener.
```kotlin
@Provides
    @Singleton
    fun provideDohAlternativesListener(): DohAlternativesListener? = null
```

### Fixed

- Links, outlined buttons and borderless buttons use text-accent color.
- Fix Http status code not being parsed in HV with DoH.

## [7.1.6]

## New
- Added support for challenge library. It is used for anti abuse purposes, and basically besides the initial
integration, clients are not needed to interact with it. To integrate, client DB version should be increased and
challenge DB integrated:

1.
```kotlin
@Binds
abstract fun provideChallengeDatabase(appDatabase: AppDatabase): ChallengeDatabase
```

2. Along with a db migration:
```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        ChallengeDatabase.MIGRATION_0.migrate(database)
    }
}
```

3. Add ChallengeFrameEntity::class to the DB entities list.


4. And in AuthModule.kt add the dependency:
```kotlin
@Provides
@Singleton
fun provideChallengeConfig(): SignupChallengeConfig = SignupChallengeConfig()
```

## Fixed

- Fix progress indicator not appearing on username screen while loading domains.
- IOExceptions are no longer logged with error level for EventManager code to avoid exception collection in logs.
- Display error and add retry button if captcha loading fails.
- If the user has more than 999 contacts, it was not possible to fetch user's contacts

## [7.1.5]

### Fixed

- Removed Label dependency from Contact modules.
- Fix invalid strings resources for Plans.
- Catch potential EventManager internal enqueue exception edge cases (e.g. userId doesn't exist anymore).

## [7.1.4]

### New

- `ProtonSettings` basic composables to build settings screens.
- Enable clients to pass additional filtering function for the plans, according to their needs.
- Added Room LabelConverters. Please add `LabelConverters` to your `TypeConverters`.

### Fixed

- Stop decoding the base64 when decrypting the HashKey (to keep compatibility with drive web & iOS).
- Encode the random bytes in base64 when generating the hash key.
- Revert cookie saving behavior to fix DoH issue.
- Use flowWithLifecycle to avoid crashes on activity/fragment transactions.

## [7.1.3]

### New
  - Added functions to encrypt with compression to the crypto module and to the KeyHolder's context:
    `encryptAndSignTextWithCompression` and `encryptAndSignDataWithCompression`

### Changes
- Now instead of clients supplying supported plan names, the plans module relies completely on the
API which will return the plans that are relevant to your app only.
But, clients have still control tools on what they want to show. Mainly the tools are:

```kotlin
    // if you want to completely not support paid plans during signup process, set this one to false.
    @Provides
    @SupportSignupPaidPlans
    fun provideSupportSignupPaidPlans() = true

    // if you want to completely not support paid plans for upgrade, set this one to false.
    @Provides
    @SupportUpgradePaidPlans
    fun provideSupportUpgradePaidPlans() = true

    // this flag controls whether you want to support your product only plans exclusively, or you want to support plans that are a combo of your and other products.
    @Provides
    @ProductOnlyPaidPlans
    fun provideProductOnlyPaidPlans() = false
```
- Add missing icons for Mail.
- Update gopenpgp to v2.4.5
- Added UserManager/UserAddressManager observeUser/observeAddresses (deprecated getUserFlow/getAddressesFlow).
- Downgraded Material lib to 1.4.0 (user-data androidTest issues).
- Downgraded TrustKit to 1.1.3 (1.1.5 is not existing https://github.com/datatheorem/TrustKit-Android/releases).
- Added EventManagerConfigProvider.

### Fixes
- Use viewLifecycleOwner.lifeCycleScope for coroutines launched in Fragments.
- Fix crash on `ProtonInput.setOnActionListener` when the `KeyEvent` param passed is null. 
- Fix incorrect design of the alert dialog
- Fix for VPN plans features showing unrelated items.
- Fix bug where a user with VPN basic plan couldn't upgrade to higher plan.
- Fixed Cancellation handling for EventManager/Worker.

## [7.1.2]

### Changes
- Disabled AutoFill for several Auth inputs.
- Replace icons with improved versions in icon set.
- Make whole plan item tile clickable for expand/collapse.

### Fixes
- Fixed Android 6 crashes in upgrade screens (all ImageViews now use app:srcCompat).

## [7.1.1]

### New
- Metrics modules (Dagger, Data, Domain).

### Fixes

- Fixed Login for Product VPN with TwoPassMode enabled.

## [7.1.0]

### New
- Added FeatureFlag modules ("feature-flag") - needed by Payment modules.

### New Migration

- Please apply changes as follow to your AppDatabase:
  - Add ```FeatureFlagEntity``` to your AppDatabase ```entities```.
  - Extends ```FeatureFlagDatabase```.
  - Add a migration to your AppDatabase (```addMigration```):
```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        FeatureFlagDatabase.MIGRATION_0.migrate(database)
        FeatureFlagDatabase.MIGRATION_1.migrate(database)
    }
}
```

### Breaking changes
- Supported plans should be provided by SupportedPlan list instead of String.

### Changes
- Plans and Payments have been updated to the latest version.
- Added new sets of icons shared with the clients.
- Logs from the root `java.util.logging.Logger` are redirected to `CoreLogger`
- New implementation of cookie store:
  - Removed dependency on `net.gotev:cookie-store`
  - Class `ProtonCookieStore` is now a subclass of `okhttp3.CookieJar` (previously it was a subclass of `java.net.CookieStore`)
- Add support for Payments feature flag.

### Fixes
- Don't log CancellationExceptions in `EventWorker.doWork`.
- Fixes wrong link on plans view for VPN.
- Allow sign in action by pressing enter key / done action in the password field.
- Crash when HV2 received no valid methods (either the user or the methods were banned).
- Fixed "Invalid cookie" log error.

## [7.0.0]

### Breaking changes

- `AppLifecycleObserver` now inherits from `DefaultLifecycleObserver`, and the custom methods `onEnterForeground` and `onEnterBackground` have been removed.
- Client apps can and should revert to the old HV2 implementation. To achieve this, we need to provide 2 dependencies:
```
@Provides
/** Can be either HumanVerificationVersion.HV2 or HV3. */
fun provideHumanVerificationVersion() = HumanVerificationVersion.HV2

@Provides
@CaptchaApiHost
/** Legacy Captcha api host dependency. Should point to 'api.$HOST'. */
fun provideCaptchaApiHost(): String = Constants.API_HOST
```

### Changes

- Disable Crypto Keys Cache.
- Keep cancelled EventMetadata/EventManagerConfig, to start syncing from previous eventId.
- Removed lifecycle-extensions which has been deprecated; removed lifecycle-compiler; using lifecycle-common instead
- Support prefilling login username for add account workflow
- Added possibility to show additional help button when login fails with potential blocking.
- Updated strings for (bug) report module
- The internal Kotlin plugin (`me.proton.kotlin` / `me.proton.core.gradle-plugins.kotlin`) has been removed, but it's still accessible via Maven
- Improvements for Username Signup
- Add Feature Flags module.

### Fixes

- Fixed missing refresh UserSettings for some use cases.
- Fixed ContactRepository usage while offline.
- Fixed LabelRepository usage while offline.
- Fixed EventManager to retry fetching on Force Update error.
- Fixed AppLifecycleObserver State Flow (add LifecycleObserver as soon as AppLifecycleObserver is initialized).
- Fixed TwoPassModeActivity to let the User retry unlocking primary key.
- Several bugfixes for Human Verification:
  - Use VPN theme in VPN app.
  - Don't show plans screen for VPN.
  - Make navigating to and from the the HV screen look like part of the sign up or log in flows.
  - Don't retry a past failed HV attempt by default when restarting the app.
- Fix ConfirmPassword dialog crash on API 23.
- Fix AccountStateHandler for accounts without keys. This prevents a crash by unhandled exceptions.
- Fix crash on HumanVerificationWebViewClient.onResourceLoadingError when it was called from a background thread.
- Add ProtonStore to get the calling stack trace of Store operations.
- We don't generate keys for external accounts anymore. This means they can only be used in VPN or converted into internal accounts on login.
- Fix/Add support for user readable error messages.
- Ability to run Core Smoke tests with languages other than English.
- Fixed issues with HV2 and DoH:
  - HV2 shows tabs for 'coupon' and 'payment' methods, they should be filtered.
  - HV2 catpcha is not loaded properly when DoH is working.
  - HV2 codes are not sent when DoH is triggered on signup.
- Fixed bug with CacheOverride Retrofit tag not returning the proper header values.

### New Injection

```kotlin
// If not-null additional help button appears after login fails with blocking. Used only by VPN.
@Provides
@Singleton
fun provideLoginBlockingHelp() : BlockingHelp? = null
```

## [6.0.0]

### Dependency Updates

- Now depends on Kotlin 1.6.10.
- Now mandatory target Android Sdk 31.
- Updated dependencies version:
  - Kotlin: 1.6.10.
  - Kotlin Coroutines: 1.5.2.
  - Kotlin Serialization: 1.3.2.
  - Store4: 4.0.4-KT15.
  - Room: 2.4.1.
  - Dagger: 2.40.5.
  - Core KTX: 1.7.0.
  - Lifecycle: 2.4.0.
  - Material: 1.5.0.
  - Paging: 3.1.0.
  - Compose: 1.2.0-alpha01.
  - Hilt Navigation Compose: 1.0.0-rc01.
  - Navigation: 2.4.0-rc01.
  - Gotev CookieStore: 1.4.0.
  - Mockk: 1.12.2.
  - Turbine: 0.7.0.
- Fixed ProtonJacocoPlugins.
- Removed Turbine expectItem (no more existing). Replaced by awaitItem.
- Removed Kotlin Android Extensions (replaced by Kotlin Parcelize).
- Removed GestureScope. Replaced by TouchInjectionScope.
- Removed Date toGMTString.
- Removed unneeded Kotlin compiler arguments.
- Removed unneeded inline keyword.
- Removed 'inline' modifier. Replaced by 'value' instead.
- Removed Duration.seconds/minutes. Replaced by 'Int.seconds' extension property.
- Replaced CoreRobot setText by addText.
- Removed JCenter from all repositories.
- Removed unneeded `kotlin-jdk7` dependency.
- Removed unneeded `kotlin-jdk8` dependency.
- Updated Binary Compatibility Dump.

## [5.2.2]

### Fixes

- Several bugfixes for Human Verification:
  - Use VPN theme in VPN app.
  - Don't show plans screen for VPN.
  - Make navigating to and from the the HV screen look like part of the sign up or log in flows.
  - Don't retry a past failed HV attempt by default when restarting the app.

## [5.2.1]

### Changes

- Bump Store4 to 4.0.4-KT15 (compatible with Kotlin 1.5 & 1.6, Duration).
- Update gopenpgp to v2.4.2: fixes related to the new AEAD feature of openPGP keys.

### Fixes

- Fix ApiConnectionException parsing on ApiResultUtils, which was breaking GuestHole implementation.

## [5.2.0]

### Added

- New crypto-validator module to check the integrity of the KeyStoreCrypto used. See [the README.md](crypto-validator/README.md) for more info.
- Label modules (Dagger, Data, Domain).

### New Migration

- Please apply changes as follow to your AppDatabase:
  - Add ```LabelEntity``` to your AppDatabase ```entities```.
  - Extends ```LabelDatabase```.
  - Add a migration to your AppDatabase (```addMigration```):
```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
       LabelDatabase.MIGRATION_0.migrate(database)
    }
}
```

### Changes

- Updated ProtonCore 2022 Copyright file.
- Added Country keep_flags.xml (keep flags resources from shrink process).
- Fixed PlansOrchestrator checkRegistered message.
- Added ProtonSidebarSubscriptionItem.
- Ripple colors are updated to match the design spec.
- Replace ComponentActivity with ActivityResultCaller in Auth and Plans orchestrator.
- Changed Events endpoint for Core Entities loop ("core/v4/events", fix label issue).

### Fixes

- DNS over Http feature (DoH) shouldn't interfere with Guest Hole (VPN) anymore.
- Fixed crashes in `safeApiCall` wrapped network requests when an unknown exception was thrown.

### Security Fixes

- The client starts verifying that the SRP server proof is correct. `InvalidSRPServerProofException`
is now thrown on invalid proofs by :
  - `me.proton.core.auth.domain.usecase.PerformLogin`
  - `me.proton.core.usersettings.domain.usecase.PerformUpdateUserPassword`
  - `me.proton.core.usersettings.domain.usecase.PerformUpdateLoginPassword`
  - `me.proton.core.usersettings.domain.usecase.PerformUpdateRecoveryEmail`
  - `me.proton.core.auth.domain.usecase.scopes.ObtainLockedScope`
  - `me.proton.core.auth.domain.usecase.scopes.ObtainPasswordScope`
- Check the format of migrated address keys token.

## [5.1.1]

### Changes

- Force change password is now based on `TemporaryPassword` API flag.
- Improving AccountPrimaryItem (added AccountPrimaryState).

## [5.1.0]

### Added

- `ProtonSidebar` (Column) & `ProtonSidebarLazy` (LazyColumn) `Composables`.
- `ProtonTheme.Vpn.Transparent` theme for transparent activities (`ForceUpdateActivity`, `ConfirmPasswordActivity`), which includes VPNs colors.

### Fixes

- Update go-srp to v0.0.3, fixes an issue with sign-in for legacy accounts.
- Update gopenpgp to v2.4.0, fixes an issue with PGP/MIME signature verification.

## [5.0.1]

### Fixes

- Fix crash due to missing Room converters for `ClientIdType`.

## [5.0.0]

### Changes

- Add support for string plurals for plans features.
- Bring back inRoot() function to UI tests.
- Added withTimeout() function to UI tests.
- Core EventListeners in EventManager are now open and can be extended.

- Add support for Missing Scope on any route that requires. Currently there is support for password
and locked scopes, whos resolution is to ask the user to provide it's password in order to obtain the
scope. The request will be auto-retried after the scope has been obtained.
How to use it:
```kotlin
with(authOrchestrator) {
    missingScopeListener.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
        .onConfirmPasswordNeeded { startConfirmPasswordWorkflow(it) }
        .onMissingScopeSuccess { context.showToast("Success Test") }
        .onMissingScopeFailed { context.showToast("Failed test") }
    }
```

### Fixes

- Human Verification: session cookie is now stored when DoH is enabled.
- Human Verification: Human Verification v3 is now compatible with DoH.
- Network: fixed a bug related to request timeouts that prevented DoH from working most of the time.

### Breaking changes

- `NetworkRequestOverriderImpl` now needs a `Context` parameter to be able to read the self-signed certificates.

## [4.1.0]

### Changes

- Report: New module for sending Bug Report (see [Report Readme](report/README.md) for more details).
- After login, when migrating from external to internal account type, `UserCheck` is also called.
- Fix payment refresh state after upgrading for logged in users.

## [4.0.0]

### Behavior changes

- Human Verification methods are no longer validated when they're received from the API. It's now HV3 who should validate them internally.

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
