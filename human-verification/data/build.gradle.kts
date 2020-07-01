import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
}

//libVersion = Version(0, 1, 0)

android()

dependencies {

    implementation(

        project(Module.kotlinUtil),
        // project(Module.data)

        // Kotlin
        `kotlin-jdk7`,

        // Android
        `android-ktx`
    )

    testImplementation(project(Module.androidTest))
    androidTestImplementation(project(Module.androidInstrumentedTest))
}

