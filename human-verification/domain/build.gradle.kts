import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*
import util.libVersion

plugins {
    `kotlin-library`
}

libVersion = Version(0, 1, 0)

dependencies {

    implementation(

        `coroutines-core`,
        // project(Module.domain)

        // Kotlin
        `kotlin-jdk7`
    )

    api (
        project(Module.kotlinUtil),
        project(Module.networkDomain),
        `dagger-android`
    )

    testImplementation(project(Module.kotlinTest))
}

