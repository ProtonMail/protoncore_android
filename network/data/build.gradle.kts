import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `android-library`
    `kotlin-serialization`
}

libVersion = Version(0, 1, 2)

android()

dependencies {

    val okHttpVersion = "4.7.2"
    val retrofitVersion = "2.9.0"

    api(
        project(Module.kotlinUtil)
    )

    implementation(
        project(Module.sharedPreferencesUtil),
        project(Module.networkDomain),

        `kotlin-jdk7`,
        `coroutines-core`,
        `serialization`,
        `retrofit-kotlin-serialization`,

        squareup("retrofit2", "retrofit") version retrofitVersion,
        squareup("okhttp3", "logging-interceptor") version okHttpVersion,
        dependency("org.minidns", module = "minidns-hla") version "0.3.4",
        dependency("commons-codec", module = "commons-codec") version "1.14",
        dependency("com.datatheorem.android.trustkit", module = "trustkit") version "1.1.2"
    )

    testImplementation(
        project(Module.kotlinTest),
        project(Module.androidTest),
        squareup("okhttp3", "mockwebserver") version okHttpVersion,
        squareup("retrofit2", "converter-scalars") version retrofitVersion
    )
}
