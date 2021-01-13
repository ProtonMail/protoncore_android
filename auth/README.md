## Overview
Modules under Auth provide Login functionality for Proton services for all Proton account types.
`Note: Currently not implemented is the private sub-account login which requires password choose.`

## Gradle
    implementation "me.proton.core:auth:{version}"

## Account Types
- ###### Regular accounts with 1 or more address. These can further be 1 or 2 password and additionally every one of them could be with Second Factor (2FA) or not.
- ###### Accounts without keys and addresses (typically VPN).
- ###### Accounts with external addresses (no keys).

## Integration guide
**Requirement**: `Network module` integration. It is **must before integrating Auth module**.

The preferred way to integrate Auth into your application is by using the **Account** and **Account-Manager** modules. They provide support for orchestrating the process, storing the data, simplifying the integration and the client will get most value out of the module.

`Note that Auth module anatomy is similar to all other (consists of presentation, domain and data submodules), which allows clients further flexibility (e.g to provide their own implementation fot the domain or different presentation/UI).`

### ***Integrating with Account modules***
First the dependencies should be supplied. As standard Auth is using **Hilt as DI framework**, so the below code should be enough. If any other DI framework is used, a bridge between Hilt and the other framework is needed (for example with Koin).

```kotlin
@Module
@InstallIn(ApplicationComponent::class)
object AuthModule {

    @Provides
    @ClientSecret
    fun provideClientSecret(): String = ""

    @Provides
    @Singleton
    fun provideAuthRepository(apiProvider: ApiProvider): AuthRepository =
        AuthRepositoryImpl(apiProvider)

    @Provides
    fun provideLocalRepository(@ApplicationContext context: Context): HumanVerificationLocalRepository =
        HumanVerificationLocalRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideRemoteRepository(apiProvider: ApiProvider): HumanVerificationRemoteRepository =
        HumanVerificationRemoteRepositoryImpl(apiProvider)
}

@Module
@InstallIn(ApplicationComponent::class)
abstract class AuthBindsModule {

    @Binds
    abstract fun provideSrpProofProvider(srpProofProviderImpl: SrpProofProviderImpl): SrpProofProvider

    @Binds
    abstract fun provideCryptoProvider(cryptoProviderImpl: CryptoProviderImpl): CryptoProvider
}
```

***The most important class is***:
```kotlin
private val authOrchestrator: AuthOrchestrator
```
Which can be obtained through Hilt dependency management mechanism in any Activity or ViewModel you need.

```kotlin
@Inject
lateinit var authOrchestrator: AuthOrchestrator
```

***AuthOrchestrator*** serves as  main point of interface to the client. It guides (orchestrates) the Login flow.

The public functions it exposes are:
```kotlin
/**
 * Register all needed workflow for internal usage.
 *
 * Note: This function have to be called [ComponentActivity.onCreate] before [ComponentActivity.onResume].
*/
fun register(context: ComponentActivity)

/**
 * Starts the Login workflow.
*/
fun startLoginWorkflow(requiredAccountType: AccountType)

/**
 * Start a TwoPassMode workflow.
*/
fun startTwoPassModeWorkflow(sessionId: SessionId, requiredAccountType: AccountType)

/**
 * Start a Human Verification workflow.
*/
fun startHumanVerificationWorkflow(sessionId: SessionId, details: HumanVerificationDetails?)
```

##### **Register**
This is needed in order to be able to listen to the activity results from the flow, and callig it is a must. It should be called in `onCreate` and before `onResume`. A context should be given.
As a result, it will provide useful information (updates) to the call site from the flow, such as:

```kotlin
fun AuthOrchestrator.onUserResult(
    block: (result: UserResult?) -> Unit
)

fun AuthOrchestrator.onScopeResult(
    block: (result: ScopeResult?) -> Unit
)

fun AuthOrchestrator.onLoginResult(
    block: (result: LoginResult?) -> Unit
)

fun AuthOrchestrator.onHumanVerificationResult(
    block: (result: HumanVerificationResult?) -> Unit
)
```
An example implementation from the call site would be something like code below.
```kotlin
with(authOrchestrator) {
    register(context)
    onLoginResult { result ->
        // TODO: do something on login result
    }
    onUserResult { result ->
        // TODO: do something with the user data result (from GET: /users)
    }
```

##### **startLoginWorkflow**
This is self-explanatory and it should be called whenever the Login UI should be launched (eg. after checking inside your app that there are no logged inn users).

##### **startTwoPassModeWorkflow**
This function is available to the client in very specific circumstances if there is a login process ongoing for 2 password account (which has stopped before entering the second password, for eg. user killed the app on that screen etc).

##### **startHumanVerificationWorkflow**
This is the entry point the HumanVerification flow is started. Network module will notify the client that a Human Verification is needed, and the client is responsible to act on this event and invoke the startHumanVerificationWorkflow function (check the HumanVerification module README.md file).

```kotlin
// Observe for human verification requirement from the API
accountManager.onHumanVerificationNeeded().onEach { (account, details) ->
    authOrchestrator.startHumanVerificationWorkflow(account.sessionId!!, details)
}.launchIn(viewModelScope)
```

***Another important class is***:
```kotlin
private val accountManager: AccountManager
```
but more about it check the Account-Manager README.md.
