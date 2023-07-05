# Observability

#### How to add a new metric?

1. Create a Kotlin class by extending `ObservableData`.
    - Add `@Serializable` annotation.
    - Add `@Schema(description = "...")` annotation.
    - Add `@SchemaId("...")` annotation.
2. Generate JSON schema files by
   running
  ```shell
  cd proton-libs
  ./gradlew :observability:observability-tools:run --args="generate --output-dir=../../../json-schema-registry/observability/client"
  ```
   **Note**: path for `output-dir` should be absolute, or relative to `proton-libs/observability/tools`.
4. Commit the new JSON schema files to `proton/be/json-schema-registry` repository
   and create a new Merge Request.
   The CI job will run some checks, to make sure the schemas are valid.
5. Merge JSON schemas to the `main` branch of `json-schema-registry`.
6. Use `ObservabilityManager.enqueue` to track new metric events.

#### Download dashboards from Grafana

You can export the dashboards from Grafana as JSON files. The JSON files can be later used if
you need to restore/re-import the dashboards into Grafana.
You need to pass your api key from Grafana (`grafana-api-key`).
You can optionally pass a query to filter dashboards (`query`, default to "Android").
```shell
cd proton-libs
./gradlew :observability:observability-tools:run --args="download --grafana-api-key=your_api_key --grafana-url=https://grafana-url --output-dir=../dashboard"
```
