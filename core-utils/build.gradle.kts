plugins {
    `android-library`
    `kotlin-android`
    `kotlin-android-extensions`
    `kotlin-serialization`
}

val version = Version(0, 2, 21)
archivesBaseName = archiveNameFor(ProtonCore.core_utils, version)

android(version)

dependencies {

    // Kotlin
    implementation(
        `kotlin-jdk7`,
        `kotlin-reflect`,
        `coroutines-android`,
        `serialization`
    )

    // Android
    api(`android-ktx`)
    implementation(
        `appcompat`,
        `lifecycle-runtime`,
        `lifecycle-liveData`,
        `lifecycle-viewModel`,
        `material`
    )
    compileOnly(
        `constraint-layout`,
        `paging-runtime`,
        `android-work-runtime`
    )

    // Other
    implementation(`retrofit`)
    implementation(`okhttp_logging`)
    api(`timber`)
    compileOnly(`viewStateStore`)

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))

    // Lint
    lintChecks(project(Module.lint))
    lintPublish(project(Module.lint))
}

//publish(Module.core, version)
