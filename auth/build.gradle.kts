plugins {
    `android-library`
    `kotlin-android`
    `kotlin-android-extensions`
    `kotlin-serialization`
}

val version = Version(0, 1, 0)
archivesBaseName = archiveNameFor(ProtonCore.auth, version)

android(version)

dependencies {
    // Core
    implementation(project(Module.core))

    // Api
    implementation(project(Module.api))

    // Crypto
    implementation(project(Module.crypto))

    // Kotlin
    implementation(
        `kotlin-jdk7`,
        `kotlin-reflect`,
        `coroutines-android`,
        `serialization`
    )

    // Android
    compileOnly(`android-annotation`)

    // Other
    implementation(`retrofit`)

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))

    // Lint
    lintChecks(project(Module.lint))
}

// publish(Module.auth, version)
