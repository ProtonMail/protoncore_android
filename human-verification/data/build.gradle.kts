import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
    `kotlin-serialization`
}

libVersion = Version(0, 1, 0)

android()

dependencies {

    implementation(
        project(Module.kotlinUtil),
        project(Module.humanVerificationDomain),
        project(Module.network),

        // Kotlin
        `kotlin-jdk7`,
        `serialization`,
        `coroutines-core`,

        // Other
        `okHttp-logging`,
        `retrofit`,
        `retrofit-kotlin-serialization`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}

