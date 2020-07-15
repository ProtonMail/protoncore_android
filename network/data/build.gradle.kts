import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
    `kotlin-serialization`
}

libVersion = Version(0, 1, 4)

android()

dependencies {

    api(
        project(Module.kotlinUtil)
    )

    implementation(
        project(Module.sharedPreferencesUtil),
        project(Module.networkDomain),

        `kotlin-jdk7`,
        `coroutines-core`,
        `serialization`,
        `retrofit`,
        `retrofit-kotlin-serialization`,
        `apacheCommon-codec`,
        `okHttp-logging`,

        dependency("org.minidns", module = "minidns-hla") version "0.3.4",
        dependency("com.datatheorem.android.trustkit", module = "trustkit") version "1.1.2"
    )

    testImplementation(
        project(Module.kotlinTest),
        project(Module.androidTest),
        squareup("okhttp3", "mockwebserver") version `okHttp version`,
        squareup("retrofit2", "converter-scalars") version `retrofit version`
    )
}
