# Observability

#### How to add a new metric?

1. Create a Kotlin class by extending `ObservableData`.
    - Add `@Serializable` annotation.
    - Add `@Schema(description = "...")` annotation.
    - Add `@SchemaId("...")` annotation.
2. Generate JSON schema files by
   running `./gradlew :observability:observability-generator:run --args="generate --output-dir=../../../json-schema-registry/observability/client"`.
   **Note**: path for `output-dir` should be absolute, or relative to `proton-libs/observability/generator`.
3. Commit the new JSON schema files to `proton/be/json-schema-registry` repository
   and create a new Merge Request.
   The CI job will run some checks, to make sure the schemas are valid.
4. Merge JSON schemas to the `main` branch of `json-schema-registry`.
5. Use `ObservabilityManager.enqueue` to track new metric events.
