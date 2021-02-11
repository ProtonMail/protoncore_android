## Overview
Modules under Account-Manager provide multi Proton account management and storage features for any
Proton Android client. It also keeps track of the primary user logged in (this is the single user
for clients that do not support multi-account or the primary logged in user for the clients that
support multi-account).

It also binds very well with Auth modules for even better user management and the advice is to
use both of them together.

## Gradle
    implementation "me.proton.core:account-manager:{version}"
    implementation "me.proton.core:account-manager-dagger:{version}" (*)

(*) Add this dependency if you want to use the Dagger module providing default implementation and DB.

## Dependency Injection
```kotlin
@Inject
lateinit var accountManager: AccountManager

@Inject
lateinit var authOrchestrator: AuthOrchestrator
```

## Account and Session States
Account and Session could be in different states in their lifecycle.
The Account is a single User that could be logged in into the system using a Session.

### Account States
```kotlin
/**
 * State emitted if this [Account] need more step(s) to be [Ready] to use.
 */
NotReady,

/**
 * A two pass mode is needed.
 *
 * Note: Usually followed by either [TwoPassModeSuccess] or [TwoPassModeFailed].
 *
 * @see [TwoPassModeSuccess]
 * @see [TwoPassModeFailed].
 */
TwoPassModeNeeded,

/**
 * The two pass mode has been successful.
 */
TwoPassModeSuccess,

/**
 * The two pass mode has failed.
 *
 * Note: Client should consider calling [startLoginWorkflow].
 */
TwoPassModeFailed,

/**
 * Choose Username and Create an Address is needed.
 *
 * Note: Usually followed by either [CreateAddressSuccess] or [CreateAddressFailed].
 *
 * @see [CreateAddressSuccess]
 * @see [CreateAddressFailed].
 */
CreateAddressNeeded,

/**
 * The address creation has been successful.
 */
CreateAddressSuccess,

/**
 * The address creation has failed.
 */
CreateAddressFailed,

/**
 * Unlock User primary key has failed.
 */
UnlockFailed,

/**
 * The [Account] is ready to use and contains a valid [Session].
 */
Ready,

/**
 * The [Account] has been disabled and do not contains valid [Session].
 */
Disabled,

/**
 * The [Account] has been removed from persistence.
 *
 * Note: Usually used by Client to clean up [Account] related resources.
 */
Removed
```

### Session States
```kotlin
/**
 * A second factor is needed.
 *
 * Note: Usually followed by either [SecondFactorSuccess] or [SecondFactorFailed].
 *
 * @see [SecondFactorSuccess]
 * @see [SecondFactorFailed].
 */
SecondFactorNeeded,

/**
 * The second factor has been successful.
 *
 * Note: Usually followed by [Authenticated].
 */
SecondFactorSuccess,

/**
 * The second factor has failed.
 *
 * Note: Client should consider calling [startLoginWorkflow].
 */
SecondFactorFailed,

/**
 * A human verification is needed.
 *
 * Note: Usually followed by either [HumanVerificationSuccess] or [HumanVerificationFailed].
 *
 * @see [HumanVerificationSuccess]
 * @see [HumanVerificationFailed].
 */
HumanVerificationNeeded,

/**
 * The human verification has been successful.
 *
 * Note: Usually followed by [Authenticated].
 */
HumanVerificationSuccess,

/**
 * The human verification has failed.
 *
 * Note: Client should consider calling [startHumanVerificationWorkflow].
 */
HumanVerificationFailed,

/**
 * This [Session] is fully authenticated, no additional step needed.
 */
Authenticated,

/**
 * This [Session] is no longer valid.
 *
 * Note: Client should consider calling [startLoginWorkflow].
 */
ForceLogout
```

## States Hanlding

Every state comes into a flow (kotlin) of states, so that client could listen and if
interested in any state could react an execute any logic.

Here is a typical states handling example, in your Activity:

```kotlin
with(authOrchestrator) {
    accountManager.observe(lifecycleScope)
        .onSessionSecondFactorNeeded { startSecondFactorWorkflow(it) }
        .onAccountTwoPassModeNeeded { startTwoPassModeWorkflow(it) }
        .onAccountCreateAddressNeeded { startChooseAddressWorkflow(it) }
        .onSessionHumanVerificationNeeded { startHumanVerificationWorkflow(it) }
        .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
        .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
        .onAccountDisabled { accountManager.removeAccount(it.userId) }
}
```

The above functions are extension functions and could be found in `AccountManagerObserver.kt` file.

## Other features
It offers several convenient functions such as:
```kotlin
accountManager.getPrimaryAccount(): FLow<Account?>
accountManager.setAsPrimary(userId: UserId)
accountManager.getAccounts(): Flow<List<Account>>
accountManager.getAccounts(state: AccountState): Flow<List<Account>>
```
For example, you should probably start a login workflow as follow:
```kotlin
accountManager.getAccounts().onEach { accounts ->
    if (accounts.isEmpty()) authOrchestrator.startLoginWorkflow(UserType.Internal)
}.launchIn(viewModelScope)
```
