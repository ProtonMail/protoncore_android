import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
}

libVersion = Version(0, 2, 0)

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
        project(Module.kotlinTest),

        // Android
        `android-arch-testing`,
        `robolectric`
    )
}
