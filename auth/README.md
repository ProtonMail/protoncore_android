## Overview
Modules under Auth provide Login functionality for Proton services for all Proton account types.

## Gradle
    implementation "me.proton.core:auth:{version}"

## Account Types
- ##### Regular accounts with 1 or more address. These can further be 1 or 2 password and additionally every one of them could be with Second Factor (2FA) or not.
- ##### Accounts without keys and addresses (typically VPN).
- ##### Accounts with external addresses (no keys).

## Integration guide
**Requirement**: `Network module`. It **must be integrated before Auth modules**.

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

    ...

    @Provides
    @Singleton
    fun provideUserCheck(
        @ApplicationContext context: Context,
        accountManager: AccountManager,
        userManager: UserManager
    ): SetupAccountCheck.UserCheck = DefaultUserCheck(context, accountManager, userManager)

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
 * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
 */
fun register(context: ComponentActivity)

/**
 * Starts the Login workflow.
 *
 * @see [onLoginResult]
 */
fun startLoginWorkflow(requiredUserType: UserType)

/**
 * Start a Second Factor workflow.
 *
 * @see [onSecondFactorResult]
 */
fun startSecondFactorWorkflow(account: Account, requiredUserType: UserType)

/**
 * Start a TwoPassMode workflow.
 *
 * @see [onTwoPassModeResult]
 */
fun startTwoPassModeWorkflow(account: Account, requiredUserType: UserType)

/**
 * Start the Choose/Create Address workflow.
 *
 * @see [onChooseAddressResult]
 */
fun startChooseAddressWorkflow(account: Account)

/**
 * Start a Human Verification workflow.
 */
fun startHumanVerificationWorkflow(account: Account)
```

Register is needed in order to be able to listen to the activity results from the flow. It should be called before `onResume` - an Activity context is needed.
As a result, it will provide useful information (updates) to the call site from the flow, such as:

```kotlin
fun AuthOrchestrator.onLoginResult(
    block: (result: LoginResult?) -> Unit
): AuthOrchestrator

fun AuthOrchestrator.onTwoPassModeResult(
    block: (result: TwoPassModeResult?) -> Unit
): AuthOrchestrator

fun AuthOrchestrator.onSecondFactorResult(
    block: (result: SecondFactorResult?) -> Unit
): AuthOrchestrator

fun AuthOrchestrator.onChooseAddressResult(
    block: (result: ChooseAddressResult?) -> Unit
): AuthOrchestrator
```
An example implementation from the call site would be something like code below.
```kotlin
with(authOrchestrator) {
    register(context)
    onLoginResult { result ->
        // If result == null -> login has been cancelled.
    }
}
```

***Another important class is***:
```kotlin
private val accountManager: AccountManager
```
For more info about it check the Account-Manager README.md.
