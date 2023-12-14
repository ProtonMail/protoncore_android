# Observability

#### I. How to add a new metric?

**Step 1** - Create your Kotlin classes.

```kotlin
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.ObservabilityData

// 1. Create a sealed class that extends the `ObservabilityData`:
sealed class DriveObservabilityData : ObservabilityData() {
  @Suppress("UNCHECKED_CAST")
  override val dataSerializer: SerializationStrategy<ObservabilityData>
    get() = serializer() as SerializationStrategy<ObservabilityData>
}

// 2. Create a Kotlin class that will represent your metric, by extending your custom sealed class:
@Serializable
@Schema(description = "Optional description..")
// The format of the `id` param below:
// "https://proton.me/android_<project>_<feature>_<sub-feature>_<metric-type>_v<version-number>.schema.json"
@SchemaId("https://proton.me/android_drive_signup_login_total_v4.schema.json")
data class SignupLoginTotal(               // The name of the class can be anything, usually matches the SchemaId.
    override val Labels: LabelsData,       // The property MUST be named `Labels`.
    @Required override val Value: Long = 1 // The property MUST be named `Value`.
) : DriveObservabilityData() {
    constructor(status: ApiStatus) : this(LabelsData(status))

    @Serializable
    data class LabelsData(
        // Specify any labels you need.
        // WARNING: Make sure to minimize the cartesian product of possible label values.
        // For example DO NOT use `Int`, because that would mean 4 billion possible values.
        val apiStatus: ApiStatus
    )

    enum class ApiStatus {
        // Specify available values for a label.
        http2xx,
        http4xx,
        http5xx,
        unknown
    }
}
```

**Step 2** - Prepare `json-schema-registry` repository.

```shell
git clone <.../proton/be/json-schema-registry>
cd json-schema-registry
npm install # Installs a git-hook that will format your JSON files.
```

**Step 3** - Generate JSON schema files from Kotlin classes.

```shell
cd proton-app

# Run a tool to generate JSON schema files:
# ./gradlew <gradle path to :observability:tools>:run --args="generate --output-dir=<path_to_json-schema-registry/observability/client> <fqn_base_class>"
# For example:
./gradlew :observability:tools:run --args="generate --output-dir=../../../json-schema/observability/client me.proton.core.drive.observability.domain.DriveObservabilityData"

# For help, run:
./gradlew :observability:tools:run --args="generate --help" 
```

**Step 4** - Merge JSON schema files.

- Commit the new JSON schema files to `proton/be/json-schema-registry` repository
and create a new Merge Request.
- Merge JSON schemas to the `main` branch of `json-schema-registry`.
- **WARNING**: once the JSON schemas are merged into `json-schema-registry`, they still need to be
  merged into Slim-API repository. Make sure to ask BE devs to do that.

**Step 5** - Add your metric to Grafana.
- Add your dashboard on Grafana (if not yet added).
- Make sure that the data source of your Grafana dashboard is configurable via a variable.
- Add a new Panel with your new metric. **Note**: on Grafana, the metric names start
  with `client_android_` even though in the Kotlin classes we've only specified `android_`.
  The `client_` part is prepended by BE.

#### II. How to send the data from your app?

```kotlin
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import me.proton.core.util.kotlin.coroutine.result

// Low-level API - use `ObservabilityManager.enqueue`:
observabilityManager.enqueue(SignupLoginTotal(ApiStatus.http2xx))

// High-level API - the "Result API"
// Record the result, in the place where it's produced (e.g. in repository),
// by wrapping your existing call in `result(..) { .. }
class AuthRepositoryImpl {
  override suspend fun performLogin() = result("performLogin") {
    provider.get<AuthenticationApi>().invoke { /* .. */ }.valueOrThrow
  }
}

// Read the result that you recorded, and send the observability event, usually in a ViewModel:
class LoginViewModel(
  override val observabilityManager: ObservabilityManager  
): ObservabilityContext, ViewModel() {
  fun startLoginWorkflow() = viewModelScope.launchWithResultContext {
    onResultEnqueueObservability("performLogin") { result: Result<*> ->
        // TODO: convert the result into the desired ObservabilityData subclass.
    }
    
    authRepository.performLogin()
  }
}
```

#### III. Download dashboards from Grafana

You can export the dashboards from Grafana as JSON files. The JSON files can be later used if
you need to restore/re-import the dashboards into Grafana.
You need to pass your api key from Grafana (`grafana-api-key`).
You can optionally pass a query to filter dashboards (`query`, default to "Android").

```shell
cd proton-libs
./gradlew :observability:observability-tools:run --args="download --grafana-api-key=your_api_key --grafana-url=https://grafana-url --output-dir=../dashboard"
```
