Modules provides support for Human Verification flows needed for all Proton API routes which can 
return error code 9001 on which the flow should be initiated.

## Gradle:

    implementation "me.proton.core:human-verification:{version}"

## Integration
Client is responsible to "detect" the API error code `9001` from any route 
`(network library will provide handler for this)`.

Example response that requires human verification:

```json
{"Code": 9001, "Error": "Human verification required", "ErrorDescription": "",
    "Details": {
        "HumanVerificationMethods" : [
            "sms",
            "email",
            "payment",
            "invite",
            "coupon"
        ],
    "HumanVerificationToken": "........"
    }
}
```

#### API Headers
`x-pm-human-verification-token` - the token code received from Human Verification flow
`x-pm-human-verification-token-type` - the token type (verification type used ex. email, captcha..)

The headers are needed for the API call that returned 
```json
{"Code": 9001, "Error": "Human verification required"}
```
to be retried again with the headers included (values received from Human Verification flow, ex. below).

### Prerequisite
Core Network module is required, so you should have the network module already integrated and configured
in your project.

#### Dependencies
Human Verification is using Hilt, so the only dependencies that need to be defined are the following:
```kotlin
    @Provides
    fun provideLocalRepository(@ApplicationContext context: Context): HumanVerificationLocalRepository =
        HumanVerificationLocalRepositoryImpl(context)

    @Provides
    fun provideRemoteRepository(
        @CurrentUsername currentUsername: String,
        @CoreExampleApiManager apiManager: ApiManager<CoreExampleApi>
    ): HumanVerificationRemoteRepository =
        HumanVerificationRemoteRepositoryImpl(apiManager, currentUsername)
```

But, besides this, the Remote Repository requires the current logged in username and your existing
Core network module.

```kotlin
    @Provides
    @CoreExampleApiManager
    fun provideApiManager(apiFactory: ApiFactory, user: User): ApiManager<CoreExampleApi> =
        apiFactory.ApiManager(user, CoreExampleApi::class)
```
where `CoreExampleApi` would be your Client Api which implements also `HumanVerificationApi`

### Starting Human Verification
Once human verification required is detected, Client should start the HumanVerificationActivity 
by passing the `HumanVerificationToken` (if present in the 9001 `Details`) and `HumanVerificationMethods` 

#### Method #1 (using Fragment directly)
Example code:

Starting the Human Verification:
```kotlin
binding.humanVerification.setOnClickListener {
            supportFragmentManager.showHumanVerification(
                largeLayout = false,
                availableVerificationMethods = listOf("methods from API"),
                captchaToken = "captcha token, from API" // this is nullable, so pass null if absent or omitted
            )
        }
```

Listening for result:
Your activity needs to implement the onResultListener:

```kotlin
class ClientActivity : AppCompatActivity(), OnResultListener {

    override fun setResult(result: HumanVerificationResult) {
        
    }
}
```

or, if the Human Verification is started from another Fragment:

```kotlin
supportFragmentManager.setFragmentResultListener(
            HumanVerificationDialogFragment.KEY_VERIFICATION_DONE,
            this
        ) { _, bundle ->
            val tokenCode = bundle.getString(HumanVerificationDialogFragment.ARG_TOKEN_CODE)
            val tokenType = bundle.getString(HumanVerificationDialogFragment.ARG_TOKEN_TYPE)
            
        }
```
#### Method #2 (using Activity) preferred option!!
Example code:

You can start the `HumanVerificationActivity` via Intent:
```kotlin
val humanVerificationIntent = Intent(this, HumanVerificationActivity::class.java)
startActivity(humanVerificationIntent)
```

and pass the extras:
```kotlin
humanVerificationIntent.putExtras(bundleOf(
            HumanVerificationActivity.ARG_VERIFICATION_OPTIONS to verificationMethods,
            HumanVerificationActivity.ARG_CAPTCHA_TOKEN to captchaVerificationToken
        ))
```
In order to receive the results you could do it via kotlin `Channel`:
The module has a helper class `HumanVerificationBinder` to help clients start Human Verification 
and receive results easily. Check the CoreExample app from Core project.

Note: The same channel should be set to the `HumanVerificationBinder` and to the 
`HumanVerificationEnterCodeViewModel`. But, you do not need to bother with these implementation 
details and only set the Hilt dependencies as:

```kotlin
    @Provides
    @Singleton
    fun provideHumanVerificationBinder(
        @ApplicationContext context: Context,
        channel: Channel<HumanVerificationResult>,
        user: User
    ): HumanVerificationBinder {
        return HumanVerificationBinder(context, channel, user)
    }

    @Provides
    @Singleton
    fun provideApiClient(binder: HumanVerificationBinder
    ): ApiClient {
        return CoreExampleApiClient(binder)
    }
```
Anyway, the clients can implement their own custom logic, just remember the channel instance should
be the same.

Or, as an option 2 to receive results in a regular way with Activity result (for custom solutions), 
by receiving this bundle:
```kotlin
const val ARG_TOKEN_CODE = "arg.token-code"
const val ARG_TOKEN_TYPE = "arg.token-type"

bundleOf(
    ARG_TOKEN_CODE to result.tokenCode,
    ARG_TOKEN_TYPE to result.tokenType
)
```

More on listening for Activity result can be found [here](https://developer.android.com/reference/kotlin/androidx/activity/result/ActivityResultCallback). 

## Note
For more information on how to integrate, please check the [CoreExample](https://gitlab.protontech.ch/proton/mobile/android/proton-libs/-/tree/master/coreexample)

### Possibility to implement clients own Data TBD! 
