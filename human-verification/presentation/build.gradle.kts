import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
    `kotlin-android-extensions`
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

// libVersion = Version(0, 1, 0)

android()

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.presentation),
        project(Module.humanVerificationDomain),
        project(Module.networkDomain),
        // Kotlin
        `kotlin-jdk7`,

        // Android
        `android-ktx`,
        `fragment`,
        `viewStateStore`,
        `coroutines-core`
    )

    api(
        `lifecycle-viewModel`,
        `hilt-android`,
        `hilt-androidx-viewModel`
    )

    kapt(
        `assistedInject-processor-dagger`,
        `hilt-android-compiler`,
        `hilt-androidx-compiler`
    )
    compileOnly(`assistedInject-annotations-dagger`)

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}

