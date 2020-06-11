import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
    `kotlin-android`
    `kotlin-android-extensions`
}

// libVersion = Version(0, 1, 0)

android()

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.presentation),
        project(Module.humanVerificationDomain),

        // Kotlin
        `kotlin-jdk7`,

        // Android
        `android-ktx`,
        `fragment`,
        `lifecycle-viewModel`,
        `viewStateStore`,
        `coroutines-core`,
        `hilt-android`,
        `hilt-lifecycle-viewmodel`
    )

    api(
        `lifecycle-viewModel`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}

