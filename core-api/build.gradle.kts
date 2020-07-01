plugins {
    `android-library`
    `kotlin-android`
    `kotlin-android-extensions`
    `kotlin-serialization`
}

val version = Version(0, 1, 0)
archivesBaseName = archiveNameFor(ProtonCore.core_api, version)

android(version)

dependencies {
    // Core
    implementation(project(Module.core))
    implementation(project(Module.domain))

    // Kotlin
    implementation(
        `kotlin-jdk7`,
        `kotlin-reflect`,
        `coroutines-android`,
        `serialization`
    )

    // Other
    implementation(
        `retrofit`,
        `retrofit-kotlin-serialization`
    )
    implementation(`okhttp_logging`)
    api(`timber`)

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))

    // Lint
    lintChecks(project(Module.lint))
    lintPublish(project(Module.lint))
}

//publish(Module.api, version)
