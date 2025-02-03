import studio.forface.easygradle.dsl.android.`android-test-runner`
import studio.forface.easygradle.dsl.android.androidTestImplementation
import studio.forface.easygradle.dsl.android.`hilt-android-testing`
import studio.forface.easygradle.dsl.android.retrofit
import studio.forface.easygradle.dsl.android.`retrofit-kotlin-serialization`
import studio.forface.easygradle.dsl.implementation
import studio.forface.easygradle.dsl.`kotlin-test`
import studio.forface.easygradle.dsl.`kotlin-test-junit`
import studio.forface.easygradle.dsl.`serialization-json`
import studio.forface.easygradle.dsl.squareup
import studio.forface.easygradle.dsl.version

plugins {
    protonAndroidLibrary
    kotlin("plugin.serialization")
    jacoco
}

protonCoverage.disabled.set(true)
publishOption.shouldBePublishedAsLib = true

android {
    namespace = "me.proton.core.test.mockproxy"
}

dependencies {
    implementation(
        junit,
        retrofit,
        okhttp,
        `android-test-runner`,
        `hilt-android-testing`,
        `kotlin-test`,
        `kotlin-test-junit`,
        `retrofit-kotlin-serialization`,
        `serialization-json`,
        squareup("okhttp3", "okhttp-tls") version `okHttp version`,
    )

    androidTestImplementation(
        `kotlin-test`
    )

    androidTestUtil(`androidx-test-orchestrator`)
}
