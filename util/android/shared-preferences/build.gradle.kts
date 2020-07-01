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

        // Kotlin
        `kotlin-jdk7`,
        `serialization`,

        // Android
        `android-ktx`
    )

    testImplementation(project(Module.androidTest))
}

