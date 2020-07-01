import studio.forface.easygradle.dsl.android.Version
import studio.forface.easygradle.dsl.android.androidTestImplementation

plugins {
    `android-library`
    `kotlin-android`
    `kotlin-android-extensions`
    `kotlin-serialization`
}

val version = Version(0, 1, 0)
archivesBaseName = archiveNameFor(ProtonCore.crypto, version)

android(version)

dependencies {
    // Crypto libs
    //implementation(project(":crypto:pmcrypto"))
    compileOnly(fileTree("libs"))

    // Core
    implementation(project(Module.core))

    // Kotlin
    implementation(
        `kotlin-jdk7`,
        `kotlin-reflect`,
        `coroutines-android`,
        `serialization`
    )

    // Android
    // implementation(Lib.Android.appcompat)
    // implementation(Lib.Android.constraintLayout)
    // implementation(Lib.Android.ktx)
    // implementation(Lib.Android.lifecycle_runtime)
    // implementation(Lib.Android.lifecycle_liveData)
    // implementation(Lib.Android.lifecycle_viewModel)
    // implementation(Lib.Android.material)
    // compileOnly(Lib.Android.paging)

    // Other
    implementation(`apacheCommon-codec`)
    // compileOnly(Lib.viewStateStore)

    // Test
    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))

    // Lint
    lintChecks(project(Module.lint))
    lintPublish(project(Module.lint))
}

// publish(Module.crypto, version)
