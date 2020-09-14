import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
    `kotlin-android-extensions`
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

libVersion = Version(0, 1, 5)

android()

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.domain),
        project(Module.networkDomain),
        project(Module.presentation),
        project(Module.humanVerificationDomain),

        // Kotlin
        `kotlin-jdk7`,
        `coroutines-android`,

        // Android
        `android-ktx`,
        `appcompat`,
        `constraint-layout`,
        `fragment`,
        `hilt-android`,
        `hilt-androidx-viewModel`,
        `lifecycle-viewModel`,
        `material`,
        `viewStateStore`
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

