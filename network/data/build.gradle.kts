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

    val okHttpVersion = "4.7.2"
    val retrofitVersion = "2.9.0"

    implementation(
        project(Module.kotlinUtil),
        project(Module.networkDomain),

        `kotlin-jdk7`,
        `coroutines-core`,
        `serialization`,

        squareup("retrofit2", "retrofit") version retrofitVersion,
        squareup("okhttp3", "logging-interceptor") version okHttpVersion,
        dependency("com.jakewharton.retrofit", module = "retrofit2-kotlinx-serialization-converter") version "0.5.0"
    )

    testImplementation(
        project(Module.kotlinTest),
        project(Module.androidTest),
        squareup("okhttp3", "mockwebserver") version okHttpVersion,
        squareup("retrofit2", "converter-scalars") version retrofitVersion
    )
}
