# Observability

#### How to add a new metric?

1. Create a JSON schema for the new metric.
2. Add the JSON schema file to `proton/be/json-schema-registry` repository.
   The CI job will run some checks, to make sure the schema is valid.
3. Fetch the `json-schema-registry` repository to your local machine, and put it at the
   same level as `proton-libs`.
4. Generate Kotlin classes from JSON schema files.
   Before generating, make sure you have a clean git status, in case you need to revert.

  ```shell
  $ cd proton-libs
  $ ./gradlew :observability:observability-domain:generateFromJsonSchema
  ```

5. The classes will be written to `observability/domain/src/generated`
6. Commit the new classes into git repository.
7. Use `ObservabilityManager.enqueue` to track new metric events.

Note: For testing, you can also generate the Kotlin classes before the JSON schema file is merged
to `json-schema-registry`.
