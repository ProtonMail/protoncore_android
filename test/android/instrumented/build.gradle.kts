import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
}

libVersion = Version(0, 1, 1)

android()

dependencies {
    // Base dependencies
    implementation(
        // Kotlin
        `kotlin-jdk7`,
        `coroutines-android`,

        // Android
        `lifecycle-runtime`,
        `lifecycle-liveData`,
        `lifecycle-viewModel`
    )

    // Test dependencies
    api(
        project(Module.androidTest).apply {
            exclude(`mockk`)
            exclude(`robolectric`)
        },

        // MockK
        `mockk-android`,

        // Android
        `espresso`,
        `android-test-core`,
        `android-test-runner`,
        `android-test-rules`
    )
}
