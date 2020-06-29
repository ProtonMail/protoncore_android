Modules provides support for using Proton API and offers common networking tools.

### Gradle:

    implementation "me.proton.core:network:{version}"

### ApiManager
To start using API client first creates `ApiFactory` singleton with `ApiClient` interface implementation. E.g. with Dagger:
```kotlin
@Singleton @Provides
fun provideApiFactory(apiClient: ApiClient): ApiFactory = ApiFactory(apiClient)
```
Now `ApiFactory` can be used to create per-user instances of `ApiManager<Api>` that implements client-defined retrofit interface `Api`. `Api` is needs to be based on suspending functions, inherit from `BaseRetrofitApi` and can be composed from multiple interfaces.
```kotlin
interface MyRetrofitApi : BaseRetrofitApi, AuthModuleApi, ... {
    @GET("clientEndpoint1") suspend fun myEndpoint(@Body myBody): MyResponse
    ...
}
```
Response class needs to be annotated according to `kotlinx.serialization`.
```kotlin
@Serializable
class MyResponse(@SerialName("FieldA") val fieldA: Int, ...)
```
Now instances of `ApiManager` supporting `MyRetrofitApi` and tied to given `UserData` can be created with `ApiFactory`
```kotlin
val apiManager1 = apiFactory.ApiManager(
    "https://api.protonvpn.ch/", user1Data, MyRetrofitApi::class, networkManager, customErrorHandlers, ...)
```
API calls wit `ApiManager` are suspending, return `ApiResult<T>` (no exceptions should be thrown) and perform error handling logic (like retries, DoH, token refresh, force logout, force upgrade as well as custom handling logic through `ApiErrorHandler` plugins):
```kotlin
val result: ApiResult<MyResponse> = apiManager1 { myEnpoint(body) }
```
Please note that `{ myEnpoint(body) }` lambda might be executed multiple times (e.g. with retries) so be aware of the side effects - it might make sense to have additional layer defined between client code and apiManager calls e.g.:
```kotlin
class MyApi(private val api: ApiManager<MyRetrofitApi>) {
    suspend fun myEndpoint(param: MyBody) = api { myEndpoint(param) }
}
```
### NetworkManager
Supports checking and observing network states (with kotlin flow):
```kotlin
val networkManager = NetworkManager(context)
networkManager.networkStatus
myScope.launch { networkManager.observe().collect { newState -> ... } }
```