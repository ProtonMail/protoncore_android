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
    "HumanVerificationToken": "........",
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
```kotlin
supportFragmentManager.setFragmentResultListener(
            HumanVerificationDialogFragment.KEY_VERIFICATION_DONE,
            this
        ) { _, bundle ->
            val tokenCode = bundle.getString(HumanVerificationDialogFragment.ARG_TOKEN_CODE)
            val tokenType = bundle.getString(HumanVerificationDialogFragment.ARG_TOKEN_TYPE)

            // at this point Client should proceed executing (retrying) the same 
            // request but this time with human verification headers
        }
```

#### Method #2 (using Activity) TBD!

## Note
For more information on how to integrate, please check the [CoreExample](https://gitlab.protontech.ch/proton/mobile/android/proton-libs/-/tree/master/coreexample)

### Possibility to implement clients own Data TBD! 
