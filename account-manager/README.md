## Overview
Modules under Account-Manager provide multi Proton account management and storage features for any
Proton Android client. It also keeps track of the primary user logged in (this is the single user
for clients that do not support multi-account or the primary logged in user for the clients that
support multi-account).

It also binds very well with Auth modules for even better user management and the advice is to
use both of them together.

## Gradle
    implementation "me.proton.core:account-manager:{version}"
    implementation "me.proton.core:account-manager-dagger:{version}"

## Account and Session States
Account and Session could be in different states in their lifecycle. The Account is a single User
that is logged in into the system, and this Account could have multiple Sessions bound to it (this
will be very useful when a Single Sign On will come for multiple Proton applications on a single
device).

## Dependency Injection
```kotlin
  @Inject
  lateinit var accountManager: AccountManager
```

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
     */
    TwoPassModeNeeded,

    /**
     * The two pass mode has been successful.
     *
     * Note: Usually followed by [Ready].
     */
    TwoPassModeSuccess,

    /**
     * The two pass mode has failed.
     *
     * Note: Client should consider calling [startLoginWorkflow].
     */
    TwoPassModeFailed,

    /**
     * The [Account] is ready to use and contains a valid [Session].
     */
    Ready,

    /**
     * The [Account] has been disabled and does not contain valid [Session].
     */
    Disabled,

    /**
     * The [Account] has been removed from persistence.
     *
     * Note: Usually used by Client to clean up [Account] related resources.
     */
    Removed
}
```

### Session States
```kotlin
    /**
     * A second factor is needed.
     *
     * Note: Usually followed by either [SecondFactorSuccess] or [SecondFactorFailed].
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

`Note that every state comes into a flow (kotlin) of states, so that client could listen and if
interested in any state could react an execute any logic.`
A code example would be:

```kotlin
  accountManager.observe(scope)
    .onAccountReady {
        // any logic when the account is ready
    }
    .onSessionSecondFactorNeeded {
        // any logic when second factor is required
    }
```

The above functions are extension functions and could be found in
`me.proton.core.accountmanager.presentation.AccountManagerObserver.kt` file.

## Other features
It offers a lot of convenient functions such as:

```kotlin
accountManager.getPrimaryAccount()
accountManager.getAccounts()
accountManager.onHumanVerificationNeeded()
```

The last one (onHumanVerificationNeeded) is an example of very easy and convenient method how to
integrate the HumanVerification module which together with `AuthOrchestrator` from the Auth module
the complete integration would look like this:

```kotlin
accountManager.onHumanVerificationNeeded().onEach { (account, details) ->
    authOrchestrator.startHumanVerificationWorkflow(account.sessionId!!, details)
}.launchIn(scope)
```
