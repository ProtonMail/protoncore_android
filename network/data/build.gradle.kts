import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
    `kotlin-serialization`
}

libVersion = Version(0, 2, 2)

android()

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.sharedPreferencesUtil),
        project(Module.networkDomain),

        // Kotlin
        `kotlin-jdk7`,
        `coroutines-core`,
        `serialization`,

        // Other
        `apacheCommon-codec`,
        `miniDsn`,
        `okHttp-logging`,
        `retrofit`,
        `retrofit-kotlin-serialization`,
        `trustKit`
    )

    testImplementation(
        project(Module.kotlinTest),
        project(Module.androidTest),
        `retrofit-scalars-converter`,
        `mockWebServer`
    )
}
