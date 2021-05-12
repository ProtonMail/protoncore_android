## Overview
Modules under Payment provide payments functionality for all Proton services. It supports payment
for upgrade of the current logged in account or paying for paid account during sign up.

## Gradle
    implementation "me.proton.core:payment:{version}"

## Payment options
- ###### `Upgrade for the primary user.` Can upgrade to any paid plan the primary user. This will create new subscription.
- ###### `Payment for paid plan during sign up.` This won't create new subscription, but instead the result token should be used as a header (human verification header with type "payment"). This is responsibility of a Sign Up module however.

## Integration guide
#### Dependencies
- A few dependencies should be satisfied. Below is an example of Dagger Hilt payments module how should be look like.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object PaymentsModule {

    @Provides
    @Singleton
    fun providePaymentsRepository(apiProvider: ApiProvider): PaymentsRepository =
        PaymentsRepositoryImpl(apiProvider)

    @Provides
    @Singleton
    fun provideCountriesRepository(@ApplicationContext context: Context): CountriesRepository =
        CountriesRepositoryImpl(context)


    @Provides
    @Singleton
    fun provideSecureEndpoint(): SecureEndpoint = SecureEndpoint("secure.protonmail.com")
}
```

#### Usage
- In order to start a payment process only a single class is required.
```kotlin
private val paymentsOrchestrator: PaymentsOrchestrator
```
- Calling register on PaymentsOrchestrator is mandatory with android context.
```kotlin
paymentsOrchestrator.register(context)
```
- The most important function is the startBillingWorkflow. It can be used in 2 ways (with passing SessionId for account upgrade or passing null for SessionId for sign ups).
`Payments module does not deal with plans (display plans nor plan selection).` Thus, this module should come after plan has been selected.
PlanDetails which are mandatory should be passed to the `startBillingWorkflow` function.
```kotlin
data class PlanDetails(
    val id: String,
    val name: String,
    val subscriptionCycle: SubscriptionCycle,
    val amount: Long? = null,
    val currency: Currency = Currency.EUR
)
```
`Note: amount is nullable, because final amount that will be billable to the user will be found out by the payments module, taking into account the
existing subscriptions user have, coupons, credits etc`.

```kotlin
paymentsOrchestrator.startBillingWorkFlow(
    sessionId = account.sessionId,
    selectedPlan = PlanDetails(
        "example of plan id",
        "Proton Plus",
        SubscriptionCycle.YEARLY
    ),
    codes = null
)
```

Client initiating the payment should expect the results as a callback `onPaymentResult` from paymentsOrchestrator:
```kotlin
onPaymentResult { result ->
    // do something with the payment result.
}
```
where the result is of type `BillingResult`:
```kotlin
data class BillingResult(
    val paySuccess: Boolean,
    val token: String?,
    val subscriptionCreated: Boolean
)
```
